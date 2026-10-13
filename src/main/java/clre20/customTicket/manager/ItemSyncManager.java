package clre20.customTicket.manager;

import clre20.customTicket.model.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** 讓已發出的門票物品在門票編輯後自動套用最新外觀。 */
public class ItemSyncManager {

    private final TicketManager ticketManager;

    public ItemSyncManager(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    public void syncOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            syncPlayer(player);
        }
    }

    public void syncPlayer(Player player) {
        syncInventory(player.getInventory());
        syncInventory(player.getEnderChest());
    }

    /** 回傳更新的格數。 */
    public int syncInventory(Inventory inventory) {
        if (inventory == null) return 0;
        ItemStack[] contents = inventory.getContents();
        int changed = 0;
        for (int i = 0; i < contents.length; i++) {
            ItemStack updated = refresh(contents[i]);
            if (updated != null) {
                inventory.setItem(i, updated);
                changed++;
            }
        }
        return changed;
    }

    /** 若物品是過期的門票，回傳更新後的物品；否則回傳 null。 */
    public ItemStack refresh(ItemStack item) {
        String id = ticketManager.getTicketId(item);
        if (id == null) return null;
        Ticket ticket = ticketManager.get(id);
        if (ticket == null) return null;
        if (ticketManager.getTicketRevision(item) == ticket.getRevision()) return null;
        return ticketManager.buildItem(ticket, item.getAmount());
    }
}
