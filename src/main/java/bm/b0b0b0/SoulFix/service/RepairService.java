package bm.b0b0b0.soulFix.service;

import bm.b0b0b0.soulFix.message.MessageService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class RepairService {

    private final JavaPlugin plugin;
    private final CooldownService cooldownService;
    private final RepairItemValidator validator;
    private final RepairAnimationService animationService;
    private final MessageService messageService;
    private final Map<UUID, List<ItemStack>> securedItems = new HashMap<>();

    public RepairService(
            JavaPlugin plugin,
            CooldownService cooldownService,
            RepairItemValidator validator,
            RepairAnimationService animationService,
            MessageService messageService
    ) {
        this.plugin = plugin;
        this.cooldownService = cooldownService;
        this.validator = validator;
        this.animationService = animationService;
        this.messageService = messageService;
    }

    public boolean isAnimating(UUID playerId) {
        return securedItems.containsKey(playerId);
    }

    public void returnSecuredToPlayer(Player player) {
        List<ItemStack> items = securedItems.remove(player.getUniqueId());
        if (items == null || items.isEmpty()) {
            return;
        }
        for (ItemStack itemStack : items) {
            giveToPlayer(player, itemStack);
        }
        messageService.send(player, "repair.returned");
    }

    public void startRepair(Player player, List<ItemStack> items, Runnable onStart, Runnable onComplete, Runnable onAbort) {
        if (isAnimating(player.getUniqueId())) {
            messageService.send(player, "repair.in-progress");
            runOnMain(onAbort);
            return;
        }
        cooldownService.isOnCooldown(player).thenAccept(onCooldown -> {
            if (onCooldown) {
                cooldownService.remainingSecondsAsync(player).thenAccept(seconds ->
                        runOnMain(() -> {
                            messageService.send(player, "repair.cooldown", "seconds", String.valueOf(seconds));
                            if (onAbort != null) {
                                onAbort.run();
                            }
                        }));
                return;
            }
            List<ItemStack> repairable = new ArrayList<>();
            for (ItemStack itemStack : items) {
                if (validator.isRepairable(itemStack)) {
                    repairable.add(itemStack.clone());
                }
            }
            if (repairable.isEmpty()) {
                runOnMain(() -> {
                    messageService.send(player, "repair.no-items");
                    if (onAbort != null) {
                        onAbort.run();
                    }
                });
                return;
            }
            runOnMain(() -> {
                securedItems.put(player.getUniqueId(), cloneAll(repairable));
                if (onStart != null) {
                    onStart.run();
                }
                animationService.play(player, () -> {
                    finishRepair(player);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
            });
        });
    }

    private void finishRepair(Player player) {
        List<ItemStack> secured = securedItems.remove(player.getUniqueId());
        if (secured == null || secured.isEmpty()) {
            return;
        }
        List<ItemStack> repaired = new ArrayList<>();
        for (ItemStack itemStack : secured) {
            repaired.add(validator.repair(itemStack));
        }
        for (ItemStack itemStack : repaired) {
            giveToPlayer(player, itemStack);
        }
        cooldownService.applyCooldown(player.getUniqueId(), repaired.size());
        messageService.send(player, "repair.success", "count", String.valueOf(repaired.size()));
    }

    private void giveToPlayer(Player player, ItemStack itemStack) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
        leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }

    private List<ItemStack> cloneAll(List<ItemStack> items) {
        List<ItemStack> clones = new ArrayList<>();
        for (ItemStack itemStack : items) {
            clones.add(itemStack.clone());
        }
        return clones;
    }

    private void runOnMain(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
