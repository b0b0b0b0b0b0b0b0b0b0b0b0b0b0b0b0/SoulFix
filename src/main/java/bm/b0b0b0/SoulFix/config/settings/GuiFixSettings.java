package bm.b0b0b0.SoulFix.config.settings;

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

    @Comment({
            @CommentValue("Сверху вниз в YAML. Разблокировка снизу вверх: [3]=37–43 всем, [2]+VIP, [1]+Premium, [0]+MVP"),
            @CommentValue("Номера слотов — под ресурспак")
    })
    public List<List<Integer>> repairRows = List.of(
            List.of(10, 11, 12, 13, 14, 15, 16),
            List.of(19, 20, 21, 22, 23, 24, 25),
            List.of(28, 29, 30, 31, 32, 33, 34),
            List.of(37, 38, 39, 40, 41, 42, 43)
    );

    @Comment(@CommentValue("Нижняя декоративная полоса (кроме кнопки «Починить» в repairElements)"))
    public List<Integer> repairBottomDecor = List.of(45, 46, 47, 48, 50, 51, 52, 53);

    @Comment(@CommentValue("Фиолетовое стекло в верхнем ряду между кнопками"))
    public List<Integer> repairTopFillers = List.of(2, 4, 6);

    @NewLine
    @Comment(@CommentValue("Элементы GUI: slot -1 шаблон. action: REPAIR, BUY_ONE, BUY_SLOT, DECORATION"))
    public Map<String, GuiElementSettings> repairElements = defaultRepairElements();

    public static final class GuiElementSettings {
        public int slot = 0;
        public String material = "STONE";
        public String nameKey = "";
        public List<String> loreKeys = List.of();
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

        GuiElementSettings slotBarrier = new GuiElementSettings();
        slotBarrier.slot = -1;
        slotBarrier.material = "BARRIER";
        slotBarrier.nameKey = "gui.repair.purchase-wait";
        slotBarrier.loreKeys = List.of("gui.repair.purchase-wait-lore");
        slotBarrier.action = "DECORATION";
        elements.put("slot-barrier", slotBarrier);

        GuiElementSettings rowBarrier = new GuiElementSettings();
        rowBarrier.slot = -1;
        rowBarrier.material = "BARRIER";
        rowBarrier.nameKey = "gui.repair.row-blocked";
        rowBarrier.loreKeys = List.of("gui.repair.row-blocked-lore");
        rowBarrier.action = "DECORATION";
        elements.put("row-barrier", rowBarrier);

        GuiElementSettings purchaseLimit = new GuiElementSettings();
        purchaseLimit.slot = -1;
        purchaseLimit.material = "BARRIER";
        purchaseLimit.nameKey = "gui.repair.locked-max";
        purchaseLimit.loreKeys = List.of("gui.repair.locked-max-lore");
        purchaseLimit.action = "DECORATION";
        elements.put("purchase-limit", purchaseLimit);

        GuiElementSettings buySlot = new GuiElementSettings();
        buySlot.slot = -1;
        buySlot.material = "BARRIER";
        buySlot.nameKey = "gui.repair.locked";
        buySlot.loreKeys = List.of("gui.repair.locked-lore");
        buySlot.action = "BUY_SLOT";
        elements.put("buy-slot", buySlot);

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

        return elements;
    }
}
