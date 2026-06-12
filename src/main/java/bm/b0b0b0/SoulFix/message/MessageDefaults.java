package bm.b0b0b0.SoulFix.message;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MessageDefaults {

    private MessageDefaults() {
    }

    public static Map<String, Object> ru() {
        Map<String, Object> messages = new LinkedHashMap<>();
        messages.put("prefix", "&#BB55FF» ");

        messages.put("gui.repair.title", "&#BB55FFПочинка");
        messages.put("gui.repair.border", " ");
        messages.put("gui.repair.button.repair", "&#55FF55Починить");
        messages.put("gui.repair.button.repair-lore", List.of(
                "&#AAAAAAПоложи сломанные вещи",
                "&#AAAAAAв свободные слоты выше"
        ));
        messages.put("gui.repair.rank.default", "Игрок");
        messages.put("gui.repair.rank.vip", "VIP");
        messages.put("gui.repair.rank.premium", "Premium");
        messages.put("gui.repair.rank.mvp", "MVP");
        messages.put("repair.row-locked", "&#FF5555Ряд недоступен. Нужна привилегия: &#FFD700{rank}");
        messages.put("repair.purchase-blocked", "&#FF5555Сначала купи следующий доступный слот.");
        messages.put("gui.repair.locked", "&#FFAA00Купить слот");
        messages.put("gui.repair.locked-lore", List.of(
                "&#AAAAAAЦена: &#FFFFFF{cost} {currency}",
                "&#AAAAAAКуплено: &#FFFFFF{purchased_slots}/{max_purchased}",
                "&#55FF55Нажми, чтобы купить"
        ));
        messages.put("gui.repair.locked-max", "&#FF5555Лимит");
        messages.put("gui.repair.purchase-wait", "&#FF5555Слот закрыт");
        messages.put("gui.repair.purchase-wait-lore", List.of(
                "&#AAAAAAСначала купи следующий доступный слот"
        ));
        messages.put("gui.repair.row-blocked", "&#FF5555Ряд закрыт");
        messages.put("gui.repair.row-blocked-lore", List.of(
                "&#AAAAAAНужна привилегия: &#FFD700{rank}"
        ));
        messages.put("gui.repair.buy-one", "&#55FF55+1 слот");
        messages.put("gui.repair.buy-one-lore", List.of(
                "&#AAAAAAЦена: &#FFFFFF{cost} {currency}",
                "&#AAAAAAКуплено: &#FFFFFF{purchased_slots}/{max_purchased}"
        ));
        messages.put("gui.repair.buy-five", "&#55FF55+5 слотов");
        messages.put("gui.repair.buy-five-lore", List.of(
                "&#AAAAAAЦена: &#FFFFFF{cost} {currency}",
                "&#AAAAAAКуплено: &#FFFFFF{purchased_slots}/{max_purchased}"
        ));
        messages.put("gui.repair.buy-ten", "&#55FF55+10 слотов");
        messages.put("gui.repair.buy-ten-lore", List.of(
                "&#AAAAAAЦена: &#FFFFFF{cost} {currency}",
                "&#AAAAAAКуплено: &#FFFFFF{purchased_slots}/{max_purchased}"
        ));
        messages.put("gui.repair.buy-max", "&#FFD700До лимита");
        messages.put("gui.repair.buy-max-lore", List.of(
                "&#AAAAAAДокупить: &#FFFFFF{amount} сл.",
                "&#AAAAAAЦена: &#FFFFFF{cost} {currency}"
        ));
        messages.put("gui.repair.locked-max-lore", List.of(
                "&#AAAAAAКуплено: &#FFFFFF{purchased_slots}/{max_purchased}",
                "&#FF5555Лимит привилегии — нужен ранг выше"
        ));

        messages.put("animation.bossbar", "&#BB55FFПочинка &#FFFFFF{progress}%");

        messages.put("command.no-permission", "&#FF5555Недостаточно прав.");
        messages.put("command.player-only", "&#FF5555Только для игроков.");
        messages.put("command.reload-success", "&#55FF55Конфигурация перезагружена.");

        messages.put("slots.info", "&#BB55FFСлоты: &#FFFFFF{tier_slots} &#7F7F7F+ &#FFFFFF{purchased_slots}&#7F7F7F/&#FFFFFF{max_purchased} &#7F7F7F= &#FFFFFF{total_slots} &#7F7F7F(из &#FFFFFF{grid_slots}&#7F7F7F)");
        messages.put("slots.max-reached", "&#FF5555Лимит покупки для твоей привилегии: &#FFFFFF{purchased_slots}/{max_purchased}. Нужен ранг выше.");
        messages.put("slots.purchase-success", "&#55FF55Куплено &#FFFFFF{amount} &#55FF55сл. Итого: &#FFFFFF{purchased_slots}/{max_purchased} &#7F7F7F(доступно &#FFFFFF{total_slots}&#7F7F7F)");
        messages.put("slots.not-enough-points", "&#FF5555Не хватает {currency}. Нужно: &#FFFFFF{cost}");
        messages.put("slots.shop-unavailable", "&#FF5555Покупка недоступна. Экономика: &#FFFFFF{provider}");
        messages.put("slots.purchase-failed", "&#FF5555Ошибка покупки. Средства возвращены — попробуй снова.");

        messages.put("repair.cooldown", "&#FF5555Подожди &#FFFFFF{seconds} &#FF5555сек.");
        messages.put("repair.no-items", "&#FF5555Нет сломанных предметов в слотах.");
        messages.put("repair.slots-full", "&#FF5555Все доступные слоты заняты.");
        messages.put("repair.not-damaged", "&#FF5555Предмет целый — починка не нужна.");
        messages.put("repair.blocked", "&#FF5555Этот предмет нельзя починить.");
        messages.put("repair.in-progress", "&#FFAA00Идёт починка...");
        messages.put("repair.success", "&#55FF55Готово! Починено: &#FFFFFF{count}");
        messages.put("repair.returned", "&#FFAA00Предметы возвращены.");

        messages.put("admin.giveslots", "&#55FF55Выдано &#FFFFFF{amount} &#55FF55слотов → &#FFFFFF{player}");
        messages.put("admin.removeslots", "&#55FF55Снято &#FFFFFF{amount} &#55FF55слотов у &#FFFFFF{player}");
        messages.put("admin.resetcooldown", "&#55FF55Кулдаун сброшен: &#FFFFFF{player}");
        messages.put("admin.player-not-found", "&#FF5555Игрок не в сети: &#FFFFFF{player}");
        messages.put("admin.usage", "&#FFAA00/soulfix admin <giveslots|removeslots|resetcooldown|reload|setup>");
        messages.put("admin.setup", List.of(
                "&#BB55FFНастройка SoulFix (слоты снизу вверх)",
                "&#AAAAAA37 — бесплатно всем с soulfix.use, покупка 38–43 по порядку",
                "&#AAAAAA+28–34 — 1-я нода в slots.row-unlock-permissions",
                "&#AAAAAA+19–25 — 2-я нода, +10–16 — 3-я (кумулятивно, по порядку)",
                "&#AAAAAAslots.row-rank-keys — нода → gui.repair.rank.* в lang",
                "&#AAAAAAgui/general.yml → repair-rows: [10-16], [19-25], [28-34], [37-43]",
                "&#AAAAAApurchase-limit-tiers — макс. покупаемых слотов (38–43), не считая бесплатный 37"
        ));

        messages.put("error.database", "&#FF5555База данных не готова. Подожди секунду.");
        messages.put("error.playerpoints-missing", "&#FF5555Нет PlayerPoints. Используй Vault или economy.mode: auto.");
        return messages;
    }

    public static Map<String, Object> en() {
        Map<String, Object> messages = new LinkedHashMap<>();
        messages.put("prefix", "&#BB55FF» ");

        messages.put("gui.repair.title", "&#BB55FFRepair");
        messages.put("gui.repair.border", " ");
        messages.put("gui.repair.button.repair", "&#55FF55Repair");
        messages.put("gui.repair.button.repair-lore", List.of(
                "&#AAAAAAPut damaged items",
                "&#AAAAAAinto free slots above"
        ));
        messages.put("gui.repair.rank.default", "Player");
        messages.put("gui.repair.rank.vip", "VIP");
        messages.put("gui.repair.rank.premium", "Premium");
        messages.put("gui.repair.rank.mvp", "MVP");
        messages.put("repair.row-locked", "&#FF5555Row locked. Required rank: &#FFD700{rank}");
        messages.put("repair.purchase-blocked", "&#FF5555Buy the next available slot first.");
        messages.put("gui.repair.locked", "&#FFAA00Buy slot");
        messages.put("gui.repair.locked-lore", List.of(
                "&#AAAAAAPrice: &#FFFFFF{cost} {currency}",
                "&#AAAAAAPurchased: &#FFFFFF{purchased_slots}/{max_purchased}",
                "&#55FF55Click to buy"
        ));
        messages.put("gui.repair.locked-max", "&#FF5555Limit");
        messages.put("gui.repair.locked-max-lore", List.of(
                "&#AAAAAAPurchased: &#FFFFFF{purchased_slots}/{max_purchased}",
                "&#FF5555Rank purchase limit — need higher rank"
        ));
        messages.put("gui.repair.purchase-wait", "&#FF5555Slot locked");
        messages.put("gui.repair.purchase-wait-lore", List.of(
                "&#AAAAAABuy the next available slot first"
        ));
        messages.put("gui.repair.row-blocked", "&#FF5555Row locked");
        messages.put("gui.repair.row-blocked-lore", List.of(
                "&#AAAAAARequired rank: &#FFD700{rank}"
        ));
        messages.put("gui.repair.buy-one", "&#55FF55+1 slot");
        messages.put("gui.repair.buy-one-lore", List.of(
                "&#AAAAAAPrice: &#FFFFFF{cost} {currency}",
                "&#AAAAAAPurchased: &#FFFFFF{purchased_slots}/{max_purchased}"
        ));
        messages.put("gui.repair.buy-five", "&#55FF55+5 slots");
        messages.put("gui.repair.buy-five-lore", List.of(
                "&#AAAAAAPrice: &#FFFFFF{cost} {currency}",
                "&#AAAAAAPurchased: &#FFFFFF{purchased_slots}/{max_purchased}"
        ));
        messages.put("gui.repair.buy-ten", "&#55FF55+10 slots");
        messages.put("gui.repair.buy-ten-lore", List.of(
                "&#AAAAAAPrice: &#FFFFFF{cost} {currency}",
                "&#AAAAAAPurchased: &#FFFFFF{purchased_slots}/{max_purchased}"
        ));
        messages.put("gui.repair.buy-max", "&#FFD700To limit");
        messages.put("gui.repair.buy-max-lore", List.of(
                "&#AAAAAABuy: &#FFFFFF{amount} slots",
                "&#AAAAAAPrice: &#FFFFFF{cost} {currency}"
        ));
        messages.put("gui.repair.slot-free", "&#55FF55Free slot");
        messages.put("gui.repair.slot-free-lore", List.of(
                "&#AAAAAAPut a damaged item here"
        ));

        messages.put("animation.bossbar", "&#BB55FFRepairing &#FFFFFF{progress}%");

        messages.put("command.no-permission", "&#FF5555Insufficient permissions.");
        messages.put("command.player-only", "&#FF5555Players only.");
        messages.put("command.reload-success", "&#55FF55Configuration reloaded.");

        messages.put("slots.info", "&#BB55FFSlots: &#FFFFFF{tier_slots} &#7F7F7F+ &#FFFFFF{purchased_slots}&#7F7F7F/&#FFFFFF{max_purchased} &#7F7F7F= &#FFFFFF{total_slots} &#7F7F7F(of &#FFFFFF{grid_slots}&#7F7F7F)");
        messages.put("slots.max-reached", "&#FF5555Purchase limit for your rank: &#FFFFFF{purchased_slots}/{max_purchased}. Need a higher rank.");
        messages.put("slots.purchase-success", "&#55FF55Bought &#FFFFFF{amount} &#55FF55slots. Total: &#FFFFFF{purchased_slots}/{max_purchased} &#7F7F7F(available &#FFFFFF{total_slots}&#7F7F7F)");
        messages.put("slots.not-enough-points", "&#FF5555Not enough {currency}. Need: &#FFFFFF{cost}");
        messages.put("slots.shop-unavailable", "&#FF5555Shop unavailable. Economy: &#FFFFFF{provider}");
        messages.put("slots.purchase-failed", "&#FF5555Purchase failed. Funds refunded — try again.");

        messages.put("repair.cooldown", "&#FF5555Wait &#FFFFFF{seconds} &#FF5555sec.");
        messages.put("repair.no-items", "&#FF5555No damaged items in slots.");
        messages.put("repair.slots-full", "&#FF5555All available slots are full.");
        messages.put("repair.not-damaged", "&#FF5555Item is not damaged.");
        messages.put("repair.blocked", "&#FF5555This item cannot be repaired.");
        messages.put("repair.in-progress", "&#FFAA00Repair in progress...");
        messages.put("repair.success", "&#55FF55Done! Repaired: &#FFFFFF{count}");
        messages.put("repair.returned", "&#FFAA00Items returned.");

        messages.put("admin.giveslots", "&#55FF55Gave &#FFFFFF{amount} &#55FF55slots → &#FFFFFF{player}");
        messages.put("admin.removeslots", "&#55FF55Removed &#FFFFFF{amount} &#55FF55slots from &#FFFFFF{player}");
        messages.put("admin.resetcooldown", "&#55FF55Cooldown reset: &#FFFFFF{player}");
        messages.put("admin.player-not-found", "&#FF5555Player not online: &#FFFFFF{player}");
        messages.put("admin.usage", "&#FFAA00/soulfix admin <giveslots|removeslots|resetcooldown|reload|setup>");
        messages.put("admin.setup", List.of(
                "&#BB55FFSoulFix setup (slots bottom to top)",
                "&#AAAAAA37 — free with soulfix.use, buy 38–43 in order",
                "&#AAAAAA+28–34 — 1st node in slots.row-unlock-permissions",
                "&#AAAAAA+19–25 — 2nd node, +10–16 — 3rd (cumulative, in order)",
                "&#AAAAAAslots.row-rank-keys — permission → gui.repair.rank.* in lang",
                "&#AAAAAAgui/general.yml → repair-rows: [10-16], [19-25], [28-34], [37-43]",
                "&#AAAAAApurchase-limit-tiers — max buyable slots (38–43), excluding free slot 37"
        ));

        messages.put("error.database", "&#FF5555Database not ready. Wait a moment.");
        messages.put("error.playerpoints-missing", "&#FF5555Purchases disabled: PlayerPoints not installed.");
        return messages;
    }
}
