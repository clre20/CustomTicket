package clre20.customTicket.manager;

import clre20.customTicket.CustomTicket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** 透過聊天欄收集玩家輸入的文字。 */
public class ChatInputManager {

    private record InputSession(Consumer<String> onInput, Runnable onCancel, BukkitTask timeoutTask) {
    }

    private final CustomTicket plugin;
    private final Map<UUID, InputSession> sessions = new ConcurrentHashMap<>();

    public ChatInputManager(CustomTicket plugin) {
        this.plugin = plugin;
    }

    /** 關閉介面並等待玩家輸入，回呼一律在主執行緒執行。 */
    public void requestInput(Player player, String promptKey, Consumer<String> onInput, Runnable onCancel) {
        UUID uuid = player.getUniqueId();
        cancelSilently(uuid);
        long timeoutTicks = plugin.getConfigManager().inputTimeoutSeconds() * 20L;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            InputSession session = sessions.remove(uuid);
            if (session == null) return;
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) plugin.getConfigManager().send(p, "input-timeout");
            if (session.onCancel() != null) session.onCancel().run();
        }, timeoutTicks);
        // 先登記再關閉介面，讓介面的關閉處理得知玩家正在輸入
        sessions.put(uuid, new InputSession(onInput, onCancel, task));
        player.closeInventory();
        plugin.getConfigManager().send(player, promptKey);
        plugin.getConfigManager().send(player, "input-cancel-hint");
    }

    public boolean isWaiting(UUID uuid) {
        return sessions.containsKey(uuid);
    }

    /** 由聊天監聽器呼叫（非同步執行緒）。 */
    public void handleInput(Player player, String message) {
        InputSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;
        session.timeoutTask().cancel();
        String text = message.trim();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (text.equalsIgnoreCase("cancel") || text.equals("取消")) {
                plugin.getConfigManager().send(player, "input-cancelled");
                if (session.onCancel() != null) session.onCancel().run();
                return;
            }
            session.onInput().accept(text);
        });
    }

    /** 玩家離線時清除，不觸發回呼。 */
    public void cancelSilently(UUID uuid) {
        InputSession session = sessions.remove(uuid);
        if (session != null) session.timeoutTask().cancel();
    }

    public void clearAll() {
        for (InputSession session : sessions.values()) session.timeoutTask().cancel();
        sessions.clear();
    }
}
