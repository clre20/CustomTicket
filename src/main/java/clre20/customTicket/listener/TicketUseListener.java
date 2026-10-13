package clre20.customTicket.listener;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.gui.ConfirmUseGui;
import clre20.customTicket.model.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** 玩家手持門票右鍵時開啟確認介面。 */
public class TicketUseListener implements Listener {

    private final CustomTicket plugin;

    public TicketUseListener(CustomTicket plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        ItemStack item = event.getItem();
        String id = plugin.getTicketManager().getTicketId(item);
        if (id == null) return;

        // 門票不可被放置或觸發原本的物品效果
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);

        Player player = event.getPlayer();
        // 主手是門票時忽略副手事件，避免重複處理
        if (hand == EquipmentSlot.OFF_HAND
                && plugin.getTicketManager().getTicketId(player.getInventory().getItemInMainHand()) != null) {
            return;
        }
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof ConfirmUseGui) return;

        Ticket ticket = plugin.getTicketManager().get(id);
        if (ticket == null) {
            plugin.getConfigManager().send(player, "ticket-invalid");
            return;
        }
        if (!player.hasPermission("customticket.use")
                || (ticket.hasPermission() && !player.hasPermission(ticket.getPermission()))) {
            plugin.getConfigManager().send(player, "ticket-no-permission");
            return;
        }
        long remaining = plugin.getCooldownManager().remainingSeconds(player.getUniqueId(), ticket.getId());
        if (remaining > 0) {
            plugin.getConfigManager().send(player, "ticket-cooldown", "%time%", remaining);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof ConfirmUseGui) return;
            new ConfirmUseGui(ticket, hand, player).open(player);
        });
    }
}
