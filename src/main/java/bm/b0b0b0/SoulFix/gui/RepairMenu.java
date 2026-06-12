package bm.b0b0b0.SoulFix.gui;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.config.settings.GuiFixSettings;
import bm.b0b0b0.SoulFix.integration.SlotEconomyManager;
import bm.b0b0b0.SoulFix.message.MessageService;
import bm.b0b0b0.SoulFix.model.PlayerRepairProfile;
import bm.b0b0b0.SoulFix.service.CooldownService;
import bm.b0b0b0.SoulFix.service.RepairService;
import bm.b0b0b0.SoulFix.service.SlotPurchaseService;
import bm.b0b0b0.SoulFix.service.SlotService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class RepairMenu implements InventoryHolder {

    private final JavaPlugin plugin;
    private final Player player;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final SlotService slotService;
    private final CooldownService cooldownService;
    private final RepairService repairService;
    private final SlotPurchaseService purchaseService;
    private final SlotEconomyManager economyManager;
    private final Inventory inventory;
    private final Set<Integer> editableSlots = new HashSet<>();
    private final Set<Integer> purchasableBarrierSlots = new HashSet<>();
    private final Map<Integer, String> actions;
    private int totalSlots;
    private boolean animating;

    public RepairMenu(
            JavaPlugin plugin,
            Player player,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            SlotService slotService,
            CooldownService cooldownService,
            RepairService repairService,
            SlotPurchaseService purchaseService,
            SlotEconomyManager economyManager,
            int totalSlots,
            PlayerRepairProfile profile
    ) {
        this.plugin = plugin;
        this.player = player;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.slotService = slotService;
        this.cooldownService = cooldownService;
        this.repairService = repairService;
        this.purchaseService = purchaseService;
        this.economyManager = economyManager;
        this.totalSlots = totalSlots;
        this.actions = GuiLayoutHelper.actionBySlot(config.gui());
        long cooldownSeconds = cooldownService.remainingSeconds(player.getUniqueId(), profile.cooldownUntilEpochMs());
        String[] pairs = guiPairs(profile, cooldownSeconds);
        Component title = messageService.guiText(player, config.gui().repairTitleKey, pairs);
        this.inventory = Bukkit.createInventory(this, config.gui().repairSize, title);
        render(profile);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player player() {
        return player;
    }

    public boolean isAnimating() {
        return animating || repairService.isAnimating(player.getUniqueId());
    }

    public boolean isEditableSlot(int slot) {
        return editableSlots.contains(slot);
    }

    public boolean hasEmptyEditableSlot() {
        return firstEmptyEditableSlot() >= 0;
    }

    public int firstEmptyEditableSlot() {
        for (int slot : editableSlots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType().isAir()) {
                return slot;
            }
        }
        return -1;
    }

    public void shiftInsert(ItemStack source) {
        int slot = firstEmptyEditableSlot();
        if (slot < 0 || source == null || source.getType().isAir()) {
            return;
        }
        inventory.setItem(slot, source.clone());
        source.setAmount(0);
    }

    public String actionAt(int slot) {
        if (purchasableBarrierSlots.contains(slot)) {
            return "BUY_SLOT";
        }
        return actions.getOrDefault(slot, "NONE");
    }

    public void handleClick(int rawSlot) {
        if (isAnimating()) {
            return;
        }
        if (!isEditableSlot(rawSlot) && !purchasableBarrierSlots.contains(rawSlot)) {
            if (explainLockedSlot(rawSlot)) {
                return;
            }
        }
        String action = actionAt(rawSlot);
        if ("REPAIR".equals(action)) {
            startRepair();
            return;
        }
        if ("BUY_SLOT".equals(action)) {
            purchaseSlots(1);
            return;
        }
        if ("BUY_ONE".equals(action)) {
            purchaseSlots(1);
            return;
        }
        if ("BUY_FIVE".equals(action)) {
            purchaseSlots(5);
            return;
        }
        if ("BUY_TEN".equals(action)) {
            purchaseSlots(10);
            return;
        }
        if ("BUY_MAX".equals(action)) {
            purchaseSlots(-1);
        }
    }

    public void onClose() {
        if (isAnimating()) {
            animating = false;
            repairService.returnSecuredToPlayer(player);
            return;
        }
        returnEditableSlotItems();
    }

    private void purchaseSlots(int amount) {
        CompletableFuture<Boolean> future = switch (amount) {
            case 1 -> purchaseService.buyOne(player);
            case 5 -> purchaseService.buyFive(player);
            case 10 -> purchaseService.buyTen(player);
            default -> purchaseService.buyMax(player);
        };
        future.thenAccept(success -> {
            if (success) {
                refreshAsync();
            }
        });
    }

    private void startRepair() {
        List<ItemStack> items = collectEditableItems();
        repairService.startRepair(
                player,
                items,
                () -> {
                    animating = true;
                    clearEditableSlots();
                },
                () -> {
                    animating = false;
                    refreshAsync();
                },
                () -> animating = false
        );
    }

    private List<ItemStack> collectEditableItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot : editableSlots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && !itemStack.getType().isAir()) {
                items.add(itemStack.clone());
            }
        }
        return items;
    }

    private void clearEditableSlots() {
        for (int slot : editableSlots) {
            inventory.setItem(slot, null);
        }
    }

    private void returnEditableSlotItems() {
        for (int slot : editableSlots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            inventory.setItem(slot, null);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
            leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
        }
    }

    public void refreshAsync() {
        slotService.totalSlots(player).thenCompose(total ->
                slotService.profile(player.getUniqueId()).thenApply(profile -> {
                    this.totalSlots = total;
                    Bukkit.getScheduler().runTask(plugin, () -> render(profile));
                    return null;
                }));
    }

    private void render(PlayerRepairProfile profile) {
        Map<Integer, ItemStack> saved = snapshotEditableSlots();
        inventory.clear();
        fillRepairGrid(profile, saved);
        fillButtons(profile);
        fillFrameFiller();
        if (!repairService.isAnimating(player.getUniqueId())) {
            restoreEditableSlots(saved);
        }
    }

    private Map<Integer, ItemStack> snapshotEditableSlots() {
        Map<Integer, ItemStack> saved = new HashMap<>();
        for (int slot : editableSlots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && !itemStack.getType().isAir()) {
                saved.put(slot, itemStack.clone());
            }
        }
        return saved;
    }

    private void restoreEditableSlots(Map<Integer, ItemStack> saved) {
        for (Map.Entry<Integer, ItemStack> entry : saved.entrySet()) {
            if (editableSlots.contains(entry.getKey())) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
        }
    }

    private void fillRepairGrid(PlayerRepairProfile profile, Map<Integer, ItemStack> saved) {
        editableSlots.clear();
        purchasableBarrierSlots.clear();
        long cooldownSeconds = cooldownService.remainingSeconds(player.getUniqueId(), profile.cooldownUntilEpochMs());
        String[] basePairs = guiPairs(profile, cooldownSeconds);
        boolean canBuyMore = canBuyMore(profile);
        int purchaseRow = config.purchaseRowIndex();
        GuiFixSettings.GuiElementSettings slotBarrier = requireElement("slot-barrier");
        GuiFixSettings.GuiElementSettings rowBarrier = requireElement("row-barrier");
        GuiFixSettings.GuiElementSettings buySlot = requireElement("buy-slot");
        GuiFixSettings.GuiElementSettings purchaseLimit = requireElement("purchase-limit");

        for (int row = 0; row < config.gui().repairRows.size(); row++) {
            List<Integer> slots = config.repairRow(row);
            if (row == purchaseRow) {
                fillPurchaseRow(slots, saved, profile, canBuyMore, buySlot, slotBarrier, purchaseLimit, basePairs);
                continue;
            }
            if (slotService.isRowUnlocked(player, row)) {
                for (int slot : slots) {
                    editableSlots.add(slot);
                    restoreSlotItem(slot, saved);
                }
                continue;
            }
            fillLockedPrivilegeRow(row, slots, rowBarrier, basePairs);
        }
    }

    private void fillLockedPrivilegeRow(
            int row,
            List<Integer> slots,
            GuiFixSettings.GuiElementSettings rowBarrier,
            String[] basePairs
    ) {
        String rank = messageService.raw(player, slotService.rankLangKeyForRow(row));
        String[] pairs = withPairs(basePairs, "rank", rank);
        ItemStack item = itemFactory.build(player, rowBarrier, pairs);
        for (int slot : slots) {
            inventory.setItem(slot, item);
        }
    }

    private void fillPurchaseRow(
            List<Integer> slots,
            Map<Integer, ItemStack> saved,
            PlayerRepairProfile profile,
            boolean canBuyMore,
            GuiFixSettings.GuiElementSettings buySlot,
            GuiFixSettings.GuiElementSettings slotBarrier,
            GuiFixSettings.GuiElementSettings purchaseLimit,
            String[] basePairs
    ) {
        int unlocked = slotService.unlockedInPurchaseRow(profile);
        int bought = profile.purchasedSlots();
        int buyCap = slotService.maxBuyableSlots(player);
        for (int index = 0; index < slots.size(); index++) {
            int slot = slots.get(index);
            if (index < unlocked) {
                editableSlots.add(slot);
                restoreSlotItem(slot, saved);
                continue;
            }
            if (index == unlocked && bought < buyCap) {
                if (canBuyMore) {
                    purchasableBarrierSlots.add(slot);
                }
                inventory.setItem(slot, itemFactory.build(player, buySlot, basePairs));
                continue;
            }
            if (bought >= buyCap) {
                inventory.setItem(slot, itemFactory.build(player, purchaseLimit, basePairs));
                continue;
            }
            inventory.setItem(slot, itemFactory.build(player, slotBarrier, basePairs));
        }
    }

    private GuiFixSettings.GuiElementSettings requireElement(String key) {
        GuiFixSettings.GuiElementSettings element = config.gui().repairElements.get(key);
        if (element == null) {
            throw new IllegalStateException("Missing gui repair element: " + key);
        }
        return element;
    }

    private String[] withPairs(String[] basePairs, String... extra) {
        List<String> values = new ArrayList<>(List.of(basePairs));
        values.addAll(List.of(extra));
        return values.toArray(String[]::new);
    }

    private void restoreSlotItem(int slot, Map<Integer, ItemStack> saved) {
        ItemStack savedItem = saved.get(slot);
        if (savedItem != null) {
            inventory.setItem(slot, savedItem);
            return;
        }
        inventory.setItem(slot, null);
    }

    private boolean explainLockedSlot(int rawSlot) {
        int purchaseRow = config.purchaseRowIndex();
        for (int row = 0; row < purchaseRow; row++) {
            if (!config.repairRow(row).contains(rawSlot)) {
                continue;
            }
            if (slotService.isRowUnlocked(player, row)) {
                return false;
            }
            String rank = messageService.raw(player, slotService.rankLangKeyForRow(row));
            messageService.send(
                    player,
                    "repair.row-locked",
                    "rank",
                    rank
            );
            return true;
        }
        if (config.purchaseRowSlots().contains(rawSlot) && !purchasableBarrierSlots.contains(rawSlot)) {
            messageService.send(player, "repair.purchase-blocked");
            return true;
        }
        return false;
    }

    private boolean canBuyMore(PlayerRepairProfile profile) {
        return profile.purchasedSlots() < slotService.maxBuyableSlots(player) && purchaseService.isShopAvailable();
    }

    private void fillFrameFiller() {
        GuiFixSettings.GuiElementSettings border = config.gui().repairElements.get("border");
        for (int slot : config.gui().repairTopFillers) {
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
        for (int slot : config.gui().repairBottomDecor) {
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
        for (int slot : GuiLayoutHelper.frameSlots(config.gui().repairSize)) {
            if (config.gui().repairBottomDecor.contains(slot)) {
                continue;
            }
            ItemStack current = inventory.getItem(slot);
            if (current != null && !current.getType().isAir()) {
                continue;
            }
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
    }

    private void fillButtons(PlayerRepairProfile profile) {
        long cooldownSeconds = cooldownService.remainingSeconds(player.getUniqueId(), profile.cooldownUntilEpochMs());
        String[] pairs = guiPairs(profile, cooldownSeconds);
        int remaining = Math.max(0, slotService.maxBuyableSlots(player) - profile.purchasedSlots());
        for (Map.Entry<Integer, String> entry : actions.entrySet()) {
            if ("DECORATION".equals(entry.getValue())
                    || "BUY_SLOT".equals(entry.getValue())) {
                continue;
            }
            GuiFixSettings.GuiElementSettings element = findElement(entry.getValue());
            if (element == null) {
                continue;
            }
            String[] buttonPairs = buttonPairs(entry.getValue(), pairs, remaining, profile.purchasedSlots());
            inventory.setItem(entry.getKey(), itemFactory.build(player, element, buttonPairs));
        }
    }

    private String[] buttonPairs(String action, String[] basePairs, int remaining, int purchased) {
        int amount = switch (action) {
            case "BUY_FIVE" -> Math.min(5, remaining);
            case "BUY_TEN" -> Math.min(10, remaining);
            case "BUY_MAX" -> remaining;
            default -> 1;
        };
        double cost = economyManager.totalCost(purchased, Math.max(0, amount));
        List<String> values = new ArrayList<>();
        for (int index = 0; index + 1 < basePairs.length; index += 2) {
            if ("cost".equals(basePairs[index])) {
                continue;
            }
            values.add(basePairs[index]);
            values.add(basePairs[index + 1]);
        }
        values.add("amount");
        values.add(String.valueOf(amount));
        values.add("cost");
        values.add(economyManager.formatCost(cost));
        return values.toArray(String[]::new);
    }

    private String[] guiPairs(PlayerRepairProfile profile, long cooldownSeconds) {
        return new String[]{
                "currency", economyManager.currencyLabel(),
                "cost", economyManager.formatCost(economyManager.nextSlotCost(profile.purchasedSlots())),
                "balance", economyManager.formatBalance(player.getUniqueId()),
                "provider", economyManager.activeProviderId(),
                "purchased", String.valueOf(profile.purchasedSlots()),
                "max_purchased", String.valueOf(slotService.maxBuyableSlots(player)),
                "tier_slots", String.valueOf(slotService.unlockedPermissionRowSlots(player)),
                "purchased_slots", String.valueOf(profile.purchasedSlots()),
                "total_slots", String.valueOf(totalSlots),
                "grid_slots", String.valueOf(config.repairGridSlotCount()),
                "cooldown_remaining", cooldownSeconds <= 0 ? "нет" : cooldownSeconds + "с"
        };
    }

    private GuiFixSettings.GuiElementSettings findElement(String action) {
        for (GuiFixSettings.GuiElementSettings element : config.gui().repairElements.values()) {
            if (action.equals(element.action)) {
                return element;
            }
        }
        return null;
    }
}
