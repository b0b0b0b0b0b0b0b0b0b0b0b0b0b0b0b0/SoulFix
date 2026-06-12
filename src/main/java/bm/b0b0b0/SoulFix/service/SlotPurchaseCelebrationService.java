package bm.b0b0b0.SoulFix.service;

import bm.b0b0b0.SoulFix.config.PluginConfig;
import bm.b0b0b0.SoulFix.config.settings.SoulFixSettings;
import bm.b0b0b0.SoulFix.message.MessageService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public final class SlotPurchaseCelebrationService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messageService;
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();

    public SlotPurchaseCelebrationService(
            JavaPlugin plugin,
            PluginConfig config,
            MessageService messageService
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
    }

    public SoulFixSettings.PurchaseCelebrationSettings settings() {
        return config.purchaseCelebration();
    }

    public void celebrate(Player player, int amount, int purchasedSlots, int maxPurchased, int totalSlots, String cost, String currency) {
        SoulFixSettings.PurchaseCelebrationSettings celebration = config.purchaseCelebration();
        if (!celebration.enabled || !player.isOnline()) {
            return;
        }
        String[] pairs = new String[]{
                "amount", String.valueOf(amount),
                "purchased_slots", String.valueOf(purchasedSlots),
                "max_purchased", String.valueOf(maxPurchased),
                "total_slots", String.valueOf(totalSlots),
                "cost", cost,
                "currency", currency
        };
        cancel(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (celebration.titleEnabled) {
                messageService.showTitle(
                        player,
                        celebration.titleFadeInTicks,
                        celebration.titleStayTicks,
                        celebration.titleFadeOutTicks,
                        celebration.titleKey,
                        celebration.subtitleKey,
                        pairs
                );
            }
            playSounds(player, celebration.sounds, celebration.soundVolume, celebration.soundPitch);
            applyPotionEffects(player, celebration.potionEffects);
            if (celebration.fireworkEnabled) {
                spawnFirework(player, celebration.fireworkColors);
            }
            BossBar bossBar = null;
            if (celebration.bossBarEnabled) {
                bossBar = BossBar.bossBar(
                        messageService.guiText(player, celebration.bossBarKey, pairs),
                        0.0f,
                        parseBossColor(celebration.bossBarColor),
                        parseBossOverlay(celebration.bossBarStyle)
                );
                player.showBossBar(bossBar);
                activeBossBars.put(player.getUniqueId(), bossBar);
            }
            startSequence(player, celebration, pairs, bossBar);
        });
    }

    public void cancel(UUID playerId) {
        BukkitTask task = activeTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
        BossBar bossBar = activeBossBars.remove(playerId);
        if (bossBar != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.hideBossBar(bossBar);
            }
        }
    }

    private void startSequence(
            Player player,
            SoulFixSettings.PurchaseCelebrationSettings celebration,
            String[] pairs,
            BossBar bossBar
    ) {
        UUID playerId = player.getUniqueId();
        int duration = Math.max(1, celebration.durationTicks);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel(playerId);
                    return;
                }
                tick++;
                if (celebration.particlesEnabled && tick % Math.max(1, celebration.particleIntervalTicks) == 0) {
                    spawnParticles(player, celebration, tick, duration);
                }
                if (celebration.actionBarEnabled) {
                    messageService.showActionBar(player, celebration.actionBarKey, pairs);
                }
                if (bossBar != null) {
                    float progress = Math.min(1.0f, tick / (float) duration);
                    bossBar.progress(progress);
                    bossBar.name(messageService.guiText(player, celebration.bossBarKey, pairs));
                }
                if (tick >= duration) {
                    cancel(playerId);
                }
            }
        }, 0L, 1L);
        activeTasks.put(playerId, task);
    }

    private void spawnParticles(
            Player player,
            SoulFixSettings.PurchaseCelebrationSettings celebration,
            int tick,
            int duration
    ) {
        Particle particle = parseParticle(celebration.particle);
        if (particle == null) {
            return;
        }
        Location base = player.getLocation().add(0, 1.0, 0);
        double angle = (tick * celebration.particleSpin) % 360.0;
        double radians = Math.toRadians(angle);
        double radius = celebration.particleRadius;
        Location ring = base.clone().add(Math.cos(radians) * radius, 0, Math.sin(radians) * radius);
        player.getWorld().spawnParticle(
                particle,
                ring,
                celebration.particleCount,
                celebration.particleOffset,
                celebration.particleOffset,
                celebration.particleOffset,
                celebration.particleSpeed
        );
        if (celebration.risingParticles) {
            double rise = (tick / (double) duration) * celebration.riseHeight;
            player.getWorld().spawnParticle(
                    particle,
                    base.clone().add(0, rise, 0),
                    Math.max(1, celebration.particleCount / 2),
                    0.2,
                    0.2,
                    0.2,
                    0.01
            );
        }
    }

    private void playSounds(Player player, List<String> sounds, float volume, float pitch) {
        Location location = player.getLocation();
        for (String name : sounds) {
            Sound sound = parseSound(name);
            if (sound != null) {
                player.playSound(location, sound, volume, pitch);
            }
        }
    }

    private void applyPotionEffects(Player player, List<SoulFixSettings.PurchaseCelebrationSettings.PotionEffectSettings> effects) {
        for (SoulFixSettings.PurchaseCelebrationSettings.PotionEffectSettings effectSettings : effects) {
            if (!effectSettings.enabled) {
                continue;
            }
            PotionEffectType type = parsePotionType(effectSettings.type);
            if (type == null) {
                continue;
            }
            player.addPotionEffect(new PotionEffect(
                    type,
                    Math.max(1, effectSettings.durationTicks),
                    Math.max(0, effectSettings.amplifier),
                    false,
                    effectSettings.showParticles,
                    effectSettings.showIcon
            ));
        }
    }

    private void spawnFirework(Player player, List<String> colors) {
        Location location = player.getLocation().add(0, 0.2, 0);
        Firework firework = (Firework) player.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).flicker(true).trail(true);
        if (colors.isEmpty()) {
            builder.withColor(Color.YELLOW, Color.ORANGE);
        } else {
            List<Color> parsed = colors.stream().map(this::parseColor).filter(color -> color != null).toList();
            if (parsed.isEmpty()) {
                builder.withColor(Color.YELLOW, Color.ORANGE);
            } else {
                builder.withColor(parsed.toArray(Color[]::new));
            }
        }
        meta.addEffect(builder.build());
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }

    private Color parseColor(String name) {
        try {
            return switch (name.toUpperCase(Locale.ROOT)) {
                case "RED" -> Color.RED;
                case "ORANGE" -> Color.ORANGE;
                case "YELLOW" -> Color.YELLOW;
                case "GREEN" -> Color.GREEN;
                case "AQUA" -> Color.AQUA;
                case "BLUE" -> Color.BLUE;
                case "PURPLE" -> Color.PURPLE;
                case "WHITE" -> Color.WHITE;
                case "FUCHSIA" -> Color.FUCHSIA;
                default -> Color.fromRGB(Integer.decode(name));
            };
        } catch (IllegalArgumentException exception) {
            return Color.YELLOW;
        }
    }

    private Particle parseParticle(String name) {
        try {
            return Particle.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Particle.TOTEM_OF_UNDYING;
        }
    }

    private Sound parseSound(String name) {
        try {
            return Sound.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private PotionEffectType parsePotionType(String name) {
        return Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
    }

    private BossBar.Color parseBossColor(String name) {
        try {
            return BossBar.Color.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.YELLOW;
        }
    }

    private BossBar.Overlay parseBossOverlay(String name) {
        try {
            return BossBar.Overlay.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return BossBar.Overlay.NOTCHED_20;
        }
    }
}
