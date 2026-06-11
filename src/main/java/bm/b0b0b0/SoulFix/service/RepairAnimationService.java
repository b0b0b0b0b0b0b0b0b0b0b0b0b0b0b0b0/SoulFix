package bm.b0b0b0.SoulFix.service;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.config.settings.SoulFixSettings;
import bm.b0b0b0.SoulFix.message.MessageService;
import java.util.Locale;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class RepairAnimationService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messageService;

    public RepairAnimationService(JavaPlugin plugin, PluginConfig config, MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
    }

    public void play(Player player, Runnable onComplete) {
        SoulFixSettings.AnimationSettings animation = config.animation();
        int duration = Math.max(1, animation.durationTicks);
        BossBar bossBar = null;
        if (animation.bossBarEnabled) {
            bossBar = BossBar.bossBar(
                    Component.empty(),
                    0.0f,
                    parseColor(animation.bossBarColor),
                    parseStyle(animation.bossBarStyle)
            );
            player.showBossBar(bossBar);
        }
        BossBar finalBossBar = bossBar;
        BukkitTask[] holder = new BukkitTask[1];
        holder[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                tick++;
                float progress = Math.min(1.0f, tick / (float) duration);
                spawnEffects(player, animation);
                if (finalBossBar != null) {
                    int percent = Math.round(progress * 100.0f);
                    finalBossBar.name(messageService.component(
                            player,
                            animation.bossBarTitleKey,
                            "progress",
                            String.valueOf(percent)
                    ));
                    finalBossBar.progress(progress);
                }
                if (tick >= duration) {
                    if (holder[0] != null) {
                        holder[0].cancel();
                    }
                    if (finalBossBar != null) {
                        player.hideBossBar(finalBossBar);
                    }
                    onComplete.run();
                }
            }
        }, 0L, 1L);
    }

    private void spawnEffects(Player player, SoulFixSettings.AnimationSettings animation) {
        Particle particle = parseParticle(animation.particle);
        if (particle != null) {
            player.getWorld().spawnParticle(
                    particle,
                    player.getLocation().add(0, 1, 0),
                    animation.particleCount,
                    animation.particleOffset,
                    animation.particleOffset,
                    animation.particleOffset,
                    0.01
            );
        }
        Sound sound = parseSound(animation.sound);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, animation.soundVolume, animation.soundPitch);
        }
    }

    private Particle parseParticle(String name) {
        try {
            return Particle.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Particle.ENCHANT;
        }
    }

    private Sound parseSound(String name) {
        try {
            return Sound.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Sound.BLOCK_ANVIL_USE;
        }
    }

    private BossBar.Color parseColor(String name) {
        try {
            return BossBar.Color.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.GREEN;
        }
    }

    private BossBar.Overlay parseStyle(String name) {
        try {
            return BossBar.Overlay.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Overlay.PROGRESS;
        }
    }
}
