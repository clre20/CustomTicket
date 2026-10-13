package clre20.customTicket.gui;

import clre20.customTicket.manager.CommandExecutorService;
import clre20.customTicket.manager.EditorManager.EditorSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

/** 門票指令列表。 */
public class CommandListGui extends EditorGui {

    private static final int MAX_COMMANDS = 45;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_ADD = 49;

    public CommandListGui(EditorSession session) {
        super(session);
        createInventory(54, plugin.getConfigManager().guiTitle("commands-title", "%name%", session.getDraft().getId()));
        render();
    }

    private void render() {
        inventory.clear();
        List<String> commands = session.getDraft().getCommands();
        for (int i = 0; i < commands.size() && i < MAX_COMMANDS; i++) {
            String cmd = commands.get(i);
            inventory.setItem(i, button(Material.PAPER, "&e#" + (i + 1) + " " + CommandExecutorService.typeLabel(cmd),
                    "&f" + cmd, "", "&a左鍵 &7編輯 / 刪除 / 排序"));
        }
        for (int i = MAX_COMMANDS; i < 54; i++) {
            inventory.setItem(i, button(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
        inventory.setItem(SLOT_BACK, button(Material.ARROW, "&e返回", "&7回到門票編輯"));
        if (commands.size() < MAX_COMMANDS) {
            inventory.setItem(SLOT_ADD, button(Material.LIME_DYE, "&a新增指令",
                    "&7前綴: &a[console] &7控制台 (預設)",
                    "&7      &b[player] &7玩家身分",
                    "&7      &c[op] &7暫時給予 OP 執行",
                    "&7      &d[message] &7傳送訊息給玩家",
                    "&7變數: &f%player% %uuid% %world% %x% %y% %z%",
                    "", "&a左鍵 &7新增"));
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        if (!isTopSlot(event)) return;
        int slot = event.getRawSlot();
        List<String> commands = session.getDraft().getCommands();

        if (slot < MAX_COMMANDS) {
            if (slot < commands.size()) later(() -> new CommandActionGui(session, slot).open(player));
            return;
        }
        if (slot == SLOT_BACK) {
            later(() -> new TicketEditorGui(session).open(player));
        } else if (slot == SLOT_ADD && commands.size() < MAX_COMMANDS) {
            askInput(player, "input-command", text -> {
                if (!text.isBlank()) session.getDraft().getCommands().add(text);
            }, () -> new CommandListGui(session).open(player));
        }
    }
}
