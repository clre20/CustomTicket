package clre20.customTicket.listener;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.util.TextUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/** 攔截正在輸入設定值的玩家聊天訊息。 */
public class ChatInputListener implements Listener {

    private final CustomTicket plugin;

    public ChatInputListener(CustomTicket plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getChatInputManager().isWaiting(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        plugin.getChatInputManager().handleInput(event.getPlayer(), TextUtil.toPlainText(event.message()));
    }
}
