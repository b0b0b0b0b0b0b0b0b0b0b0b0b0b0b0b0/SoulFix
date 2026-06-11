package bm.b0b0b0.soulFix.gui;

import bm.b0b0b0.soulFix.config.settings.GuiFixSettings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GuiLayoutHelper {

    private GuiLayoutHelper() {
    }

    public static Set<Integer> frameSlots(int size) {
        Set<Integer> slots = new HashSet<>();
        int rows = size / 9;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < 9; column++) {
                if (row == 0 || row == rows - 1 || column == 0 || column == 8) {
                    slots.add(row * 9 + column);
                }
            }
        }
        return slots;
    }

    public static Map<Integer, String> actionBySlot(GuiFixSettings settings) {
        Map<Integer, String> actions = new HashMap<>();
        for (GuiFixSettings.GuiElementSettings element : settings.repairElements.values()) {
            if (element.slot >= 0) {
                actions.put(element.slot, element.action);
            }
        }
        return actions;
    }

    public static List<Integer> activeRepairSlots(List<Integer> configuredSlots, int allowedCount) {
        return configuredSlots.stream().limit(Math.max(0, allowedCount)).toList();
    }
}
