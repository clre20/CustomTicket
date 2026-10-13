package clre20.customTicket.listener;

import clre20.customTicket.CustomTicket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** 門票物品同步、更新提醒與離線清理。 */
public class PlayerListener implements Listener {

    private final CustomTicket plugin;

    public PlayerListener(CustomTicket plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getItemSyncManager().syncPlayer(player);

        if (player.hasPermission("customticket.admin") && plugin.getConfigManager().updateNotifyOp()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) plugin.getUpdateChecker().notifyIfAvailable(player);
            }, 40L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // 開啟箱子等容器時同步裡面的門票
        plugin.getItemSyncManager().syncInventory(event.getInventory());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getChatInputManager().cancelSilently(event.getPlayer().getUniqueId());
        plugin.getEditorManager().end(event.getPlayer().getUniqueId());
    }
}
