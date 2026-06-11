package bm.b0b0b0.soulFix.gui;

import bm.b0b0b0.soulFix.config.PluginConfig;
import bm.b0b0b0.soulFix.config.settings.GuiFixSettings;
import bm.b0b0b0.soulFix.integration.SlotEconomyManager;
import bm.b0b0b0.soulFix.message.MessageService;
import bm.b0b0b0.soulFix.model.PlayerRepairProfile;
import bm.b0b0b0.soulFix.service.CooldownService;
import bm.b0b0b0.soulFix.service.RepairService;
import bm.b0b0b0.soulFix.service.SlotPurchaseService;
import bm.b0b0b0.soulFix.service.SlotService;
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
        Component title = messageService.guiText(player, config.gui().repairTitleKey);
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
        for (int slot : editableSlots) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType().isAir()) {
                return true;
            }
        }
        return false;
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
        fillDecorations(profile);
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

    private void fillDecorations(PlayerRepairProfile profile) {
        List<Integer> ordered = config.gui().repairSlots;
        int allowed = Math.min(totalSlots, ordered.size());
        List<Integer> active = GuiLayoutHelper.activeRepairSlots(ordered, allowed);
        editableSlots.clear();
        editableSlots.addAll(active);
        Set<Integer> activeSet = new HashSet<>(active);
        purchasableBarrierSlots.clear();
        long cooldownSeconds = cooldownService.remainingSeconds(player.getUniqueId(), profile.cooldownUntilEpochMs());
        String[] pairs = guiPairs(profile, cooldownSeconds);
        boolean canBuyMore = canBuyMore(profile);
        GuiFixSettings.GuiElementSettings locked = config.gui().repairElements.get("locked-slot");
        GuiFixSettings.GuiElementSettings lockedMax = config.gui().repairElements.get("locked-max");
        GuiFixSettings.GuiElementSettings barrierElement = canBuyMore ? locked : lockedMax;
        for (int slot : ordered) {
            if (!activeSet.contains(slot)) {
                purchasableBarrierSlots.add(slot);
                inventory.setItem(slot, itemFactory.build(player, barrierElement, pairs));
            }
        }
        GuiFixSettings.GuiElementSettings info = config.gui().repairElements.get("info");
        inventory.setItem(info.slot, itemFactory.build(player, info, pairs));
    }

    private boolean canBuyMore(PlayerRepairProfile profile) {
        return profile.purchasedSlots() < slotService.maxPurchasableSlots(player) && purchaseService.isShopAvailable();
    }

    private void fillFrameFiller() {
        GuiFixSettings.GuiElementSettings border = config.gui().repairElements.get("border");
        for (int slot : config.gui().repairTopFillers) {
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
        for (int slot : config.gui().repairBottomFillers) {
            inventory.setItem(slot, itemFactory.filler(player, border));
        }
        for (int slot : GuiLayoutHelper.frameSlots(config.gui().repairSize)) {
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
        int remaining = Math.max(0, slotService.maxPurchasableSlots(player) - profile.purchasedSlots());
        for (Map.Entry<Integer, String> entry : actions.entrySet()) {
            if ("DECORATION".equals(entry.getValue())
                    || "BUY_SLOT".equals(entry.getValue())
                    || "INFO".equals(entry.getValue())) {
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
        if ("BUY_MAX".equals(action)) {
            amount = remaining;
        }
        double cost = economyManager.totalCost(purchased, Math.max(0, amount));
        List<String> values = new ArrayList<>(List.of(basePairs));
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
                "provider", economyManager.activeProviderId(),
                "purchased", String.valueOf(profile.purchasedSlots()),
                "max_purchased", String.valueOf(slotService.maxPurchasableSlots(player)),
                "base_slots", String.valueOf(slotService.baseSlots(player)),
                "purchased_slots", String.valueOf(profile.purchasedSlots()),
                "total_slots", String.valueOf(totalSlots),
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
