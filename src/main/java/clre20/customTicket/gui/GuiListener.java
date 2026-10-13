package clre20.customTicket.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** 將介面事件分派給對應的 CustomGuiHolder，並強制只接受左鍵。 */
public class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof CustomGuiHolder gui)) return;
        event.setCancelled(true);
        if (event.getClick() != ClickType.LEFT) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        gui.handleClick(event, player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof CustomGuiHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof CustomGuiHolder gui)) return;
        if (event.getPlayer() instanceof Player player) gui.handleClose(event, player);
    }
}
