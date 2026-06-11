package bm.b0b0b0.SoulFix;

import bm.b0b0b0.SoulFix.command.SoulFixCommandRegistrar;
import bm.b0b0b0.SoulFix.config.ConfigurationLoader;
import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.database.DatabaseBootstrap;
import bm.b0b0b0.SoulFix.gui.GuiItemFactory;
import bm.b0b0b0.SoulFix.gui.RepairGuiService;
import bm.b0b0b0.SoulFix.integration.EconomyHookStatus;
import bm.b0b0b0.SoulFix.integration.PlaceholderApiHook;
import bm.b0b0b0.SoulFix.integration.SlotEconomyManager;
import bm.b0b0b0.SoulFix.listener.RepairInventoryListener;
import bm.b0b0b0.SoulFix.message.MessageLoader;
import bm.b0b0b0.SoulFix.message.MessageService;
import bm.b0b0b0.SoulFix.repository.SqlPlayerProfileRepository;
import bm.b0b0b0.SoulFix.service.CooldownService;
import bm.b0b0b0.SoulFix.service.RepairAnimationService;
import bm.b0b0b0.SoulFix.service.RepairItemValidator;
import bm.b0b0b0.SoulFix.service.RepairService;
import bm.b0b0b0.SoulFix.service.SlotPurchaseService;
import bm.b0b0b0.SoulFix.service.SlotService;
import bm.b0b0b0.SoulFix.util.PluginConsole;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoulFix extends JavaPlugin {

    private ConfigurationLoader configurationLoader;
    private MessageService messageService;
    private DatabaseBootstrap databaseBootstrap;
    private bm.b0b0b0.SoulFix.SoulFixRuntime runtime;
    private SlotEconomyManager economyManager;
    private PlaceholderApiHook placeholderApiHook;

    @Override
    public void onEnable() {
        PluginConsole.startupHeader(getPluginMeta().getVersion());

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            PluginConsole.warn("Could not create plugin data folder");
        }

        configurationLoader = new ConfigurationLoader();
        PluginConfig pluginConfig = configurationLoader.load(this);
        PluginConsole.step("Settings loaded (config.yml, gui/general.yml)");

        MessageLoader messageLoader = new MessageLoader(
                this,
                pluginConfig.defaultLocale(),
                pluginConfig.fallbackLocale()
        );
        messageLoader.load();
        messageService = new MessageService(messageLoader);
        PluginConsole.step("Lang loaded (lang/" + pluginConfig.defaultLocale() + ".yml, lang/"
                + pluginConfig.fallbackLocale() + ".yml)");

        economyManager = new SlotEconomyManager(pluginConfig);
        logEconomyStatus(economyManager.hook());

        runtime = new bm.b0b0b0.SoulFix.SoulFixRuntime();
        new SoulFixCommandRegistrar(this, pluginConfig, messageService, runtime, configurationLoader).register();
        PluginConsole.step("Commands registered (/soulfix)");

        databaseBootstrap = new DatabaseBootstrap(this, pluginConfig);
        PluginConsole.step("Database connecting (" + databaseTarget(pluginConfig) + ")...");
        databaseBootstrap.start().whenComplete((provider, throwable) -> {
            if (throwable != null) {
                Bukkit.getScheduler().runTask(this, () -> {
                    PluginConsole.errorBlock("Database startup failed: " + throwable.getMessage());
                    getServer().getPluginManager().disablePlugin(this);
                });
                return;
            }
            Bukkit.getScheduler().runTask(this, () -> finishEnable(pluginConfig, provider));
        });
    }

    private void finishEnable(PluginConfig pluginConfig, bm.b0b0b0.SoulFix.database.DataSourceProvider provider) {
        SqlPlayerProfileRepository repository = new SqlPlayerProfileRepository(
                provider.dataSource(),
                databaseBootstrap.executor()
        );
        PluginConsole.step("Database ready (" + pluginConfig.storageType() + ")");

        SlotService slotService = new SlotService(pluginConfig, repository);
        CooldownService cooldownService = new CooldownService(pluginConfig, repository, slotService);
        RepairItemValidator repairItemValidator = new RepairItemValidator(pluginConfig);
        RepairAnimationService repairAnimationService = new RepairAnimationService(this, pluginConfig, messageService);
        RepairService repairService = new RepairService(
                this,
                cooldownService,
                repairItemValidator,
                repairAnimationService,
                messageService
        );

        GuiItemFactory guiItemFactory = new GuiItemFactory(messageService);

        SlotPurchaseService slotPurchaseService = new SlotPurchaseService(
                this,
                slotService,
                economyManager,
                messageService
        );

        RepairGuiService repairGuiService = new RepairGuiService(
                this,
                pluginConfig,
                messageService,
                guiItemFactory,
                slotService,
                cooldownService,
                repairService,
                slotPurchaseService,
                economyManager
        );

        placeholderApiHook = new PlaceholderApiHook(this, slotService, cooldownService);
        if (pluginConfig.placeholderApiEnabled()) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                placeholderApiHook.registerIfPresent();
                PluginConsole.step("PlaceholderAPI hooked (%soulfix_base_slots%, %soulfix_total_slots%, %soulfix_cooldown%)");
            } else {
                PluginConsole.warn("PlaceholderAPI not found — placeholders disabled");
            }
        }

        getServer().getPluginManager().registerEvents(
                new RepairInventoryListener(pluginConfig, repairItemValidator, messageService),
                this
        );
        PluginConsole.step("Listeners registered");

        Runnable integrationReload = () -> {
            logEconomyStatus(economyManager.hook());
            if (configurationLoader.pluginConfig().placeholderApiEnabled()) {
                placeholderApiHook.registerIfPresent();
            }
        };

        runtime.bind(repairGuiService, slotService, cooldownService, integrationReload);

        PluginConsole.startupComplete();
    }

    private String databaseTarget(PluginConfig pluginConfig) {
        if ("mysql".equalsIgnoreCase(pluginConfig.storageType())) {
            return pluginConfig.storage().mysqlHost + ":" + pluginConfig.storage().mysqlPort
                    + "/" + pluginConfig.storage().mysqlDatabase;
        }
        return pluginConfig.storage().sqliteFile;
    }

    private void logEconomyStatus(EconomyHookStatus status) {
        if (status.warning()) {
            PluginConsole.warn(status.consoleMessage());
            return;
        }
        PluginConsole.step(status.consoleMessage());
    }

    @Override
    public void onDisable() {
        PluginConsole.shutdownHeader();

        if (runtime != null) {
            runtime.unbind();
        }
        if (databaseBootstrap != null) {
            databaseBootstrap.shutdown();
            PluginConsole.step("Database connection closed");
        }

        PluginConsole.shutdownComplete();
    }
}

