package clre20.customTicket.gui;

import clre20.customTicket.manager.CommandExecutorService;
import clre20.customTicket.manager.EditorManager.EditorSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;
import java.util.List;

/** 單條指令的操作選單。 */
public class CommandActionGui extends EditorGui {

    private static final int SLOT_INFO = 4;
    private static final int SLOT_EDIT = 10;
    private static final int SLOT_UP = 12;
    private static final int SLOT_DOWN = 14;
    private static final int SLOT_DELETE = 16;
    private static final int SLOT_BACK = 22;

    private final int index;

    public CommandActionGui(EditorSession session, int index) {
        super(session);
        this.index = index;
        createInventory(27, plugin.getConfigManager().guiTitle("command-action-title", "%index%", index + 1));
        render();
    }

    private void render() {
        String cmd = session.getDraft().getCommands().get(index);
        inventory.setItem(SLOT_INFO, button(Material.PAPER, "&e#" + (index + 1) + " " + CommandExecutorService.typeLabel(cmd), "&f" + cmd));
        inventory.setItem(SLOT_EDIT, button(Material.WRITABLE_BOOK, "&e編輯", "&7重新輸入此指令"));
        inventory.setItem(SLOT_UP, button(Material.LIME_STAINED_GLASS_PANE, "&a上移"));
        inventory.setItem(SLOT_DOWN, button(Material.YELLOW_STAINED_GLASS_PANE, "&e下移"));
        inventory.setItem(SLOT_DELETE, button(Material.LAVA_BUCKET, "&c刪除", "&7刪除此指令"));
        inventory.setItem(SLOT_BACK, button(Material.ARROW, "&e返回", "&7回到指令列表"));
        fill(Material.GRAY_STAINED_GLASS_PANE);
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        if (!isTopSlot(event)) return;
        List<String> commands = session.getDraft().getCommands();
        if (index >= commands.size()) {
            later(() -> new CommandListGui(session).open(player));
            return;
        }
        switch (event.getRawSlot()) {
            case SLOT_EDIT -> askInput(player, "input-command", text -> {
                if (!text.isBlank() && index < commands.size()) commands.set(index, text);
            }, () -> new CommandListGui(session).open(player));
            case SLOT_UP -> {
                if (index > 0) Collections.swap(commands, index, index - 1);
                later(() -> new CommandListGui(session).open(player));
            }
            case SLOT_DOWN -> {
                if (index < commands.size() - 1) Collections.swap(commands, index, index + 1);
                later(() -> new CommandListGui(session).open(player));
            }
            case SLOT_DELETE -> {
                commands.remove(index);
                later(() -> new CommandListGui(session).open(player));
            }
            case SLOT_BACK -> later(() -> new CommandListGui(session).open(player));
            default -> {
            }
        }
    }
}
