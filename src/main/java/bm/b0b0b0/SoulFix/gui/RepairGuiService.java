package bm.b0b0b0.soulFix.gui;

import bm.b0b0b0.soulFix.config.PluginConfig;
import bm.b0b0b0.soulFix.integration.SlotEconomyManager;
import bm.b0b0b0.soulFix.message.MessageService;
import bm.b0b0b0.soulFix.service.CooldownService;
import bm.b0b0b0.soulFix.service.RepairService;
import bm.b0b0b0.soulFix.service.SlotPurchaseService;
import bm.b0b0b0.soulFix.service.SlotService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RepairGuiService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messageService;
    private final GuiItemFactory itemFactory;
    private final SlotService slotService;
    private final CooldownService cooldownService;
    private final RepairService repairService;
    private final SlotPurchaseService purchaseService;
    private final SlotEconomyManager economyManager;

    public RepairGuiService(
            JavaPlugin plugin,
            PluginConfig config,
            MessageService messageService,
            GuiItemFactory itemFactory,
            SlotService slotService,
            CooldownService cooldownService,
            RepairService repairService,
            SlotPurchaseService purchaseService,
            SlotEconomyManager economyManager
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
        this.itemFactory = itemFactory;
        this.slotService = slotService;
        this.cooldownService = cooldownService;
        this.repairService = repairService;
        this.purchaseService = purchaseService;
        this.economyManager = economyManager;
    }

    public void openRepairMenu(Player player) {
        slotService.totalSlots(player).thenCompose(total ->
                slotService.profile(player.getUniqueId()).thenAccept(profile ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            RepairMenu menu = new RepairMenu(
                                    plugin,
                                    player,
                                    config,
                                    messageService,
                                    itemFactory,
                                    slotService,
                                    cooldownService,
                                    repairService,
                                    purchaseService,
                                    economyManager,
                                    total,
                                    profile
                            );
                            player.openInventory(menu.getInventory());
                        })));
    }
}
