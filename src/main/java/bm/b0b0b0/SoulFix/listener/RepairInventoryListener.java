package bm.b0b0b0.SoulFix.listener;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.gui.RepairMenu;
import bm.b0b0b0.SoulFix.message.MessageService;
import bm.b0b0b0.SoulFix.service.RepairItemValidator;
import bm.b0b0b0.SoulFix.service.RepairService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class RepairInventoryListener implements Listener {

    private final PluginConfig config;
    private final RepairItemValidator validator;
    private final MessageService messageService;
    private final RepairService repairService;

    public RepairInventoryListener(
            PluginConfig config,
            RepairItemValidator validator,
            MessageService messageService,
            RepairService repairService
    ) {
        this.config = config;
        this.validator = validator;
        this.messageService = messageService;
        this.repairService = repairService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof RepairMenu menu)) {
            return;
        }
        handleRepairClick(event, menu);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof RepairMenu menu)) {
            return;
        }
        if (menu.isAnimating()) {
            event.setCancelled(true);
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < top.getSize()) {
                if (!menu.isEditableSlot(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack dragged = event.getView().getCursor();
                if (dragged != null && !dragged.getType().isAir() && !validator.isRepairable(dragged)) {
                    event.setCancelled(true);
                    rejectPlacement(event.getWhoClicked(), dragged);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder(false) instanceof RepairMenu menu) {
            menu.onClose();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        repairService.cleanupOnQuit(event.getPlayer());
    }

    private void handleRepairClick(InventoryClickEvent event, RepairMenu menu) {
        if (menu.isAnimating()) {
            event.setCancelled(true);
            return;
        }
        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        if (rawSlot >= 0 && rawSlot < topSize) {
            if (menu.isEditableSlot(rawSlot)) {
                ItemStack cursor = event.getView().getCursor();
                ItemStack current = event.getCurrentItem();
                if (isPlacingIntoSlot(event, cursor, current)) {
                    ItemStack incoming = incomingItem(event, cursor, current);
                    if (incoming != null && !incoming.getType().isAir() && !validator.isRepairable(incoming)) {
                        event.setCancelled(true);
                        rejectPlacement(event.getWhoClicked(), incoming);
                    }
                }
                return;
            }
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                menu.handleClick(rawSlot);
            }
            return;
        }
        if (event.getClickedInventory() != null && event.getClickedInventory() != event.getView().getTopInventory()) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || event.getClick() == ClickType.SHIFT_LEFT
                    || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
                if (!(event.getWhoClicked() instanceof Player player)) {
                    return;
                }
                ItemStack moving = event.getCurrentItem();
                if (moving == null || moving.getType().isAir()) {
                    return;
                }
                if (!validator.isRepairable(moving)) {
                    rejectPlacement(player, moving);
                    return;
                }
                if (!menu.hasEmptyEditableSlot()) {
                    messageService.send(player, "repair.slots-full");
                    return;
                }
                menu.shiftInsert(moving);
            }
        }
    }

    private boolean isPlacingIntoSlot(InventoryClickEvent event, ItemStack cursor, ItemStack current) {
        return switch (event.getAction()) {
            case PLACE_ALL, PLACE_ONE, PLACE_SOME, SWAP_WITH_CURSOR -> cursor != null && !cursor.getType().isAir();
            case HOTBAR_SWAP -> true;
            default -> current == null || current.getType().isAir();
        };
    }

    private ItemStack incomingItem(InventoryClickEvent event, ItemStack cursor, ItemStack current) {
        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            if (event.getWhoClicked() instanceof Player player) {
                return player.getInventory().getItem(event.getHotbarButton());
            }
        }
        if (cursor != null && !cursor.getType().isAir()) {
            return cursor;
        }
        return current;
    }

    private void rejectPlacement(org.bukkit.entity.HumanEntity whoClicked, ItemStack itemStack) {
        if (!(whoClicked instanceof Player player)) {
            return;
        }
        if (itemStack.getType().getMaxDurability() <= 0 || config.isMaterialBlocked(itemStack.getType())) {
            messageService.send(player, "repair.blocked");
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof Damageable damageable && damageable.getDamage() <= 0) {
            messageService.send(player, "repair.not-damaged");
            return;
        }
        messageService.send(player, "repair.blocked");
    }
}
