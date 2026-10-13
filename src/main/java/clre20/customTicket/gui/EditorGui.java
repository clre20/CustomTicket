package clre20.customTicket.gui;

import clre20.customTicket.manager.EditorManager.EditorSession;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.function.Consumer;

/** 編輯器系列介面的基底：離開所有編輯介面（且非聊天輸入中）時自動放棄草稿。 */
public abstract class EditorGui extends CustomGuiHolder {

    protected final EditorSession session;

    protected EditorGui(EditorSession session) {
        this.session = session;
    }

    @Override
    public void handleClose(InventoryCloseEvent event, Player player) {
        later(() -> {
            if (!player.isOnline()) return;
            if (plugin.getEditorManager().get(player.getUniqueId()) != session) return;
            if (plugin.getChatInputManager().isWaiting(player.getUniqueId())) return;
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof EditorGui gui
                    && gui.session == session) return;
            plugin.getEditorManager().end(player.getUniqueId());
            plugin.getConfigManager().send(player, "editor-cancelled");
        });
    }

    /** 透過聊天欄輸入，完成或取消後回到指定介面。 */
    protected void askInput(Player player, String promptKey, Consumer<String> onInput,
                            Runnable reopen) {
        later(() -> plugin.getChatInputManager().requestInput(player, promptKey, text -> {
            if (plugin.getEditorManager().get(player.getUniqueId()) != session) return;
            onInput.accept(text);
            reopen.run();
        }, () -> {
            if (player.isOnline() && plugin.getEditorManager().get(player.getUniqueId()) == session) reopen.run();
        }));
    }
}
