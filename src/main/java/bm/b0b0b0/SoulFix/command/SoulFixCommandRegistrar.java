package bm.b0b0b0.SoulFix.command;

import bm.b0b0b0.SoulFix.SoulFixRuntime;
import bm.b0b0b0.SoulFix.config.ConfigurationLoader;
import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.message.MessageService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoulFixCommandRegistrar {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messageService;
    private final SoulFixRuntime runtime;
    private final ConfigurationLoader configurationLoader;

    public SoulFixCommandRegistrar(
            JavaPlugin plugin,
            PluginConfig config,
            MessageService messageService,
            SoulFixRuntime runtime,
            ConfigurationLoader configurationLoader
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
        this.runtime = runtime;
        this.configurationLoader = configurationLoader;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(
                Commands.literal("soulfix")
                        .requires(source -> source.getSender().hasPermission(config.permissionUse())
                                || source.getSender().hasPermission(config.permissionAdmin()))
                        .executes(this::openMenu)
                        .then(Commands.literal("slots").executes(this::showSlots))
                        .then(Commands.literal("admin")
                                .requires(source -> source.getSender().hasPermission(config.permissionAdmin()))
                                .executes(this::usage)
                                .then(Commands.literal("reload").executes(this::reload))
                                .then(Commands.literal("giveslots")
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(this::giveSlots))))
                                .then(Commands.literal("removeslots")
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(this::removeSlots))))
                                .then(Commands.literal("resetcooldown")
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .executes(this::resetCooldown))))
                        .build()
        ));
    }

    private int openMenu(CommandContext<CommandSourceStack> context) {
        if (!requireReady(context.getSource().getSender())) {
            return 0;
        }
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            messageService.send(sender, "command.player-only");
            return 0;
        }
        if (!player.hasPermission(config.permissionUse())) {
            messageService.send(player, "command.no-permission");
            return 0;
        }
        runtime.repairGuiService().openRepairMenu(player);
        return Command.SINGLE_SUCCESS;
    }

    private int showSlots(CommandContext<CommandSourceStack> context) {
        if (!requireReady(context.getSource().getSender())) {
            return 0;
        }
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            messageService.send(sender, "command.player-only");
            return 0;
        }
        runtime.slotService().profile(player.getUniqueId()).thenAccept(profile -> {
            int base = runtime.slotService().baseSlots(player);
            int purchased = profile.purchasedSlots();
            int maxPurchased = runtime.slotService().maxPurchasableSlots(player);
            int total = runtime.slotService().totalSlots(player, profile);
            Bukkit.getScheduler().runTask(plugin, () -> messageService.send(
                    player,
                    "slots.info",
                    "base_slots",
                    String.valueOf(base),
                    "purchased_slots",
                    String.valueOf(purchased),
                    "max_purchased",
                    String.valueOf(maxPurchased),
                    "total_slots",
                    String.valueOf(total)
            ));
        });
        return Command.SINGLE_SUCCESS;
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        configurationLoader.reload(plugin);
        messageService.reload();
        if (runtime.isReady() && runtime.integrationReload() != null) {
            runtime.integrationReload().run();
        }
        messageService.send(context.getSource().getSender(), "command.reload-success");
        return Command.SINGLE_SUCCESS;
    }

    private int giveSlots(CommandContext<CommandSourceStack> context) {
        if (!requireReady(context.getSource().getSender())) {
            return 0;
        }
        Player target = resolvePlayer(context);
        if (target == null) {
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(context, "amount");
        runtime.slotService().addPurchasedSlots(
                target.getUniqueId(),
                amount,
                config.repairGridSlotCount()
        ).thenAccept(profile ->
                Bukkit.getScheduler().runTask(plugin, () -> messageService.send(
                        context.getSource().getSender(),
                        "admin.giveslots",
                        "player",
                        target.getName(),
                        "amount",
                        String.valueOf(amount)
                )));
        return Command.SINGLE_SUCCESS;
    }

    private int removeSlots(CommandContext<CommandSourceStack> context) {
        if (!requireReady(context.getSource().getSender())) {
            return 0;
        }
        Player target = resolvePlayer(context);
        if (target == null) {
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(context, "amount");
        runtime.slotService().profile(target.getUniqueId()).thenCompose(profile -> {
            int next = Math.max(0, profile.purchasedSlots() - amount);
            return runtime.slotService().setPurchasedSlots(target.getUniqueId(), next);
        }).thenAccept(profile -> Bukkit.getScheduler().runTask(plugin, () -> messageService.send(
                context.getSource().getSender(),
                "admin.removeslots",
                "player",
                target.getName(),
                "amount",
                String.valueOf(amount)
        )));
        return Command.SINGLE_SUCCESS;
    }

    private int resetCooldown(CommandContext<CommandSourceStack> context) {
        if (!requireReady(context.getSource().getSender())) {
            return 0;
        }
        Player target = resolvePlayer(context);
        if (target == null) {
            return 0;
        }
        runtime.cooldownService().resetCooldown(target.getUniqueId()).thenAccept(profile ->
                Bukkit.getScheduler().runTask(plugin, () -> messageService.send(
                        context.getSource().getSender(),
                        "admin.resetcooldown",
                        "player",
                        target.getName()
                )));
        return Command.SINGLE_SUCCESS;
    }

    private int usage(CommandContext<CommandSourceStack> context) {
        messageService.send(context.getSource().getSender(), "admin.usage");
        return Command.SINGLE_SUCCESS;
    }

    private boolean requireReady(CommandSender sender) {
        if (runtime.isReady()) {
            return true;
        }
        messageService.send(sender, "error.database");
        return false;
    }

    private Player resolvePlayer(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "player");
        return Bukkit.getPlayerExact(name);
    }
}
