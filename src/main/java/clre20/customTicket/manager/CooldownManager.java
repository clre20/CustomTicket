package clre20.customTicket.manager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 每位玩家、每種門票的使用冷卻（僅存於記憶體）。 */
public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /** 剩餘冷卻秒數（無條件進位），0 代表可使用。 */
    public long remainingSeconds(UUID player, String ticketId) {
        Map<String, Long> map = cooldowns.get(player);
        if (map == null) return 0;
        Long until = map.get(ticketId.toLowerCase(Locale.ROOT));
        if (until == null) return 0;
        long remaining = until - System.currentTimeMillis();
        if (remaining <= 0) {
            map.remove(ticketId.toLowerCase(Locale.ROOT));
            return 0;
        }
        return (remaining + 999) / 1000;
    }

    public void apply(UUID player, String ticketId, int seconds) {
        if (seconds <= 0) return;
        cooldowns.computeIfAbsent(player, k -> new HashMap<>())
                .put(ticketId.toLowerCase(Locale.ROOT), System.currentTimeMillis() + seconds * 1000L);
    }

    public void clearTicket(String ticketId) {
        String key = ticketId.toLowerCase(Locale.ROOT);
        for (Map<String, Long> map : cooldowns.values()) map.remove(key);
    }
}
