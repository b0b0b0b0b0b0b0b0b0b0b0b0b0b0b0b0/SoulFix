package bm.b0b0b0.SoulFix.config.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class SoulFixSettings extends YamlSerializable {

    public SoulFixSettings() {
        super(SoulFixSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("sqlite | mysql"))
    public String storageType = "sqlite";

    @NewLine
    public StorageSettings storage = new StorageSettings();

    @NewLine
    @Comment(@CommentValue("Локаль по умолчанию и fallback"))
    public LocaleSettings locale = new LocaleSettings();

    @NewLine
    public PermissionsSettings permissions = new PermissionsSettings();

    @NewLine
    public SlotsSettings slots = new SlotsSettings();

    @NewLine
    public EconomySettings economy = new EconomySettings();

    @NewLine
    public CooldownSettings cooldown = new CooldownSettings();

    @NewLine
    public RepairSettings repair = new RepairSettings();

    @NewLine
    public AnimationSettings animation = new AnimationSettings();

    @NewLine
    public IntegrationsSettings integrations = new IntegrationsSettings();

    public static final class StorageSettings {
        public String sqliteFile = "data/data.db";
        public String mysqlHost = "127.0.0.1";
        public int mysqlPort = 3306;
        public String mysqlDatabase = "soulfix";
        public String mysqlUser = "root";
        public String mysqlPassword = "";
        public int poolSize = 10;
        public long connectionTimeoutMs = 30000L;
    }

    public static final class LocaleSettings {
        public String defaultLocale = "ru";
        public String fallbackLocale = "en";
    }

    public static final class PermissionsSettings {
        public String use = "soulfix.use";
        public String admin = "soulfix.admin";
        public String bypassCooldown = "soulfix.bypass.cooldown";
    }

    public static final class SlotsSettings {
        @Comment(@CommentValue("Слотов в одном ряду GUI (7×4 = 28 мест)"))
        public int rowSize = 7;

        @Comment(@CommentValue("Открыто изначально без покупки (максимум из совпавших permission)"))
        public Map<String, Integer> permissionTiers = defaultTiers();

        @Comment(@CommentValue("Сколько слотов можно докупить (максимум из совпавших permission)"))
        public Map<String, Integer> purchaseLimitTiers = defaultPurchaseLimits();
    }

    public static final class EconomySettings {
        @Comment(@CommentValue("auto | playerpoints | vault"))
        public String mode = "auto";

        @Comment(@CommentValue("Базовая цена 1-го покупаемого слота (PlayerPoints)"))
        public int playerPointsCost = 500;

        @Comment(@CommentValue("Базовая цена 1-го покупаемого слота (Vault / EssentialsX)"))
        public double vaultCost = 1000.0;

        @Comment(@CommentValue("Подпись валюты в GUI и чате (UTF-8: монет, coins, ₽, 🪙)"))
        public String currencyLabel = "монет";

        @Comment(@CommentValue("Откуда подпись: config | vault | playerpoints"))
        public String currencyLabelSource = "config";

        @NewLine
        public ScalingSettings scaling = new ScalingSettings();

        public static final class ScalingSettings {
            @Comment(@CommentValue("Включить рост цены за каждый уже купленный слот"))
            public boolean enabled = true;

            @Comment(@CommentValue("Прибавка %% к цене за каждый купленный слот (0 = только база)"))
            public double percentPerPurchasedSlot = 8.0;

            @Comment(@CommentValue("true = составной рост (1+pct)^n, false = линейный 1+n×pct"))
            public boolean compound = false;

            @Comment(@CommentValue("Потолок: цена слота не выше база × этот множитель (напр. 5.0)"))
            public double maxMultiplier = 5.0;

            @Comment(@CommentValue("Абсолютный потолок за 1 слот, 0 = только maxMultiplier"))
            public double maxCostCap = 0.0;
        }
    }

    public static final class CooldownSettings {
        public int baseSeconds = 5;
        public int perItemSeconds = 3;
    }

    public static final class RepairSettings {
        public List<String> blockedMaterials = List.of(
                "BARRIER",
                "STRUCTURE_VOID"
        );
        public boolean requireDamage = true;
    }

    public static final class AnimationSettings {
        public int durationTicks = 60;
        public String particle = "ENCHANT";
        public int particleCount = 8;
        public double particleOffset = 0.4;
        public String sound = "BLOCK_ANVIL_USE";
        public float soundVolume = 0.8f;
        public float soundPitch = 1.2f;
        public boolean bossBarEnabled = true;
        public String bossBarColor = "GREEN";
        public String bossBarStyle = "SEGMENTED_10";
        public String bossBarTitleKey = "animation.bossbar";
    }

    public static final class IntegrationsSettings {
        public boolean placeholderApiEnabled = true;
    }

    private static Map<String, Integer> defaultTiers() {
        Map<String, Integer> tiers = new LinkedHashMap<>();
        tiers.put("soulfix.slots.default", 1);
        tiers.put("soulfix.slots.vip", 3);
        tiers.put("soulfix.slots.premium", 4);
        tiers.put("soulfix.slots.legend", 5);
        return tiers;
    }

    private static Map<String, Integer> defaultPurchaseLimits() {
        Map<String, Integer> limits = new LinkedHashMap<>();
        limits.put("soulfix.slots.default", 7);
        limits.put("soulfix.slots.vip", 14);
        limits.put("soulfix.slots.premium", 21);
        limits.put("soulfix.slots.legend", 28);
        return limits;
    }
}
