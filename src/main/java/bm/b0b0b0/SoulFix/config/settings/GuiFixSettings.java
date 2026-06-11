package bm.b0b0b0.soulFix.config.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.NewLine;
import net.elytrium.serializer.language.object.YamlSerializable;

public final class GuiFixSettings extends YamlSerializable {

    public GuiFixSettings() {
        super(SoulFixSerializerConfig.INSTANCE);
    }

    @Comment(@CommentValue("Ключ заголовка окна в lang/*.yml"))
    public String repairTitleKey = "gui.repair.title";

    @NewLine
    @Comment(@CommentValue("Размер инвентаря починки (54 = 6 рядов)"))
    public int repairSize = 54;

    @Comment(@CommentValue("7×4 = 28 слотов, слева направо сверху вниз. Первые N открыты, остальные — барьеры с покупкой."))
    public List<Integer> repairSlots = List.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );

    @Comment(@CommentValue("Фиолетовое стекло в верхнем ряду между кнопками +1 / +5 / +10 / макс"))
    public List<Integer> repairTopFillers = List.of(2, 6);

    @Comment(@CommentValue("Фиолетовое стекло в нижнем ряду вокруг кнопки «Починить»"))
    public List<Integer> repairBottomFillers = List.of(46, 47, 48, 50, 51, 52);

    @NewLine
    @Comment(@CommentValue("Элементы GUI: slot -1 рамка, -2 барьер покупки, -3 лимит. action: REPAIR, BUY_ONE, BUY_SLOT, INFO, DECORATION"))
    public Map<String, GuiElementSettings> repairElements = defaultRepairElements();

    public static final class GuiElementSettings {
        @Comment(@CommentValue("Слот инвентаря. Отрицательный = шаблон, не привязан к одному слоту"))
        public int slot = 0;

        @Comment(@CommentValue("Material Bukkit, напр. CHEST, BARRIER, ANVIL"))
        public String material = "STONE";

        @Comment(@CommentValue("Ключ названия предмета в lang/*.yml"))
        public String nameKey = "";

        @Comment(@CommentValue("Ключи строк lore в lang/*.yml"))
        public List<String> loreKeys = List.of();

        @Comment(@CommentValue("Действие по клику: REPAIR, BUY_ONE, BUY_SLOT, DECORATION, ..."))
        public String action = "NONE";
    }

    private static Map<String, GuiElementSettings> defaultRepairElements() {
        Map<String, GuiElementSettings> elements = new LinkedHashMap<>();

        GuiElementSettings border = new GuiElementSettings();
        border.slot = -1;
        border.material = "PURPLE_STAINED_GLASS_PANE";
        border.nameKey = "gui.repair.border";
        border.action = "DECORATION";
        elements.put("border", border);

        GuiElementSettings buyOne = new GuiElementSettings();
        buyOne.slot = 1;
        buyOne.material = "CHEST";
        buyOne.nameKey = "gui.repair.buy-one";
        buyOne.loreKeys = List.of("gui.repair.buy-one-lore");
        buyOne.action = "BUY_ONE";
        elements.put("buy-one", buyOne);

        GuiElementSettings buyFive = new GuiElementSettings();
        buyFive.slot = 3;
        buyFive.material = "TRAPPED_CHEST";
        buyFive.nameKey = "gui.repair.buy-five";
        buyFive.loreKeys = List.of("gui.repair.buy-five-lore");
        buyFive.action = "BUY_FIVE";
        elements.put("buy-five", buyFive);

        GuiElementSettings info = new GuiElementSettings();
        info.slot = 4;
        info.material = "PAPER";
        info.nameKey = "gui.repair.info";
        info.loreKeys = List.of("gui.repair.info-lore");
        info.action = "INFO";
        elements.put("info", info);

        GuiElementSettings buyTen = new GuiElementSettings();
        buyTen.slot = 5;
        buyTen.material = "ENDER_CHEST";
        buyTen.nameKey = "gui.repair.buy-ten";
        buyTen.loreKeys = List.of("gui.repair.buy-ten-lore");
        buyTen.action = "BUY_TEN";
        elements.put("buy-ten", buyTen);

        GuiElementSettings buyMax = new GuiElementSettings();
        buyMax.slot = 7;
        buyMax.material = "BEACON";
        buyMax.nameKey = "gui.repair.buy-max";
        buyMax.loreKeys = List.of("gui.repair.buy-max-lore");
        buyMax.action = "BUY_MAX";
        elements.put("buy-max", buyMax);

        GuiElementSettings repairButton = new GuiElementSettings();
        repairButton.slot = 49;
        repairButton.material = "ANVIL";
        repairButton.nameKey = "gui.repair.button.repair";
        repairButton.loreKeys = List.of("gui.repair.button.repair-lore");
        repairButton.action = "REPAIR";
        elements.put("repair-button", repairButton);

        GuiElementSettings locked = new GuiElementSettings();
        locked.slot = -2;
        locked.material = "BARRIER";
        locked.nameKey = "gui.repair.locked";
        locked.loreKeys = List.of("gui.repair.locked-lore");
        locked.action = "BUY_SLOT";
        elements.put("locked-slot", locked);

        GuiElementSettings lockedMax = new GuiElementSettings();
        lockedMax.slot = -3;
        lockedMax.material = "BARRIER";
        lockedMax.nameKey = "gui.repair.locked-max";
        lockedMax.loreKeys = List.of("gui.repair.locked-max-lore");
        lockedMax.action = "BUY_SLOT";
        elements.put("locked-max", lockedMax);

        return elements;
    }
}
