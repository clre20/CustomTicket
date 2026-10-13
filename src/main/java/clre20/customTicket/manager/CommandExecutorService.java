package clre20.customTicket.manager;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * 執行門票指令，支援前綴:
 * [console] 控制台 (預設)、[player] 玩家身分、[op] 暫時給予 OP、[message] 傳送訊息。
 */
public class CommandExecutorService {

    public static final String CONSOLE = "[console]";
    public static final String PLAYER = "[player]";
    public static final String OP = "[op]";
    public static final String MESSAGE = "[message]";

    private final CustomTicket plugin;

    public CommandExecutorService(CustomTicket plugin) {
        this.plugin = plugin;
    }

    public void executeAll(Player player, List<String> commands) {
        for (String line : commands) {
            try {
                execute(player, line);
            } catch (Exception e) {
                Throwable root = e;
                while (root.getCause() != null && root.getCause() != root) root = root.getCause();
                plugin.getLogger().log(Level.WARNING, "執行門票指令失敗 (" + line + ")，原因: "
                        + root.getClass().getSimpleName() + ": " + root.getMessage(), e);
            }
        }
    }

    private void execute(Player player, String line) {
        if (line == null || line.isBlank()) return;
        String trimmed = line.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);

        String type = CONSOLE;
        for (String prefix : new String[]{CONSOLE, PLAYER, OP, MESSAGE}) {
            if (lower.startsWith(prefix)) {
                type = prefix;
                trimmed = trimmed.substring(prefix.length()).trim();
                break;
            }
        }

        String content = applyPlaceholders(player, trimmed);
        if (type.equals(MESSAGE)) {
            player.sendMessage(TextUtil.parse(content));
            return;
        }
        if (content.startsWith("/")) content = content.substring(1);
        if (content.isBlank()) return;

        switch (type) {
            case PLAYER -> player.performCommand(content);
            case OP -> {
                boolean wasOp = player.isOp();
                try {
                    if (!wasOp) player.setOp(true);
                    player.performCommand(content);
                } finally {
                    if (!wasOp) player.setOp(false);
                }
            }
            default -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content);
        }
    }

    public static String applyPlaceholders(Player player, String text) {
        Location loc = player.getLocation();
        return TextUtil.replace(text,
                "%player%", player.getName(),
                "%uuid%", player.getUniqueId(),
                "%world%", loc.getWorld() == null ? "" : loc.getWorld().getName(),
                "%x%", loc.getBlockX(),
                "%y%", loc.getBlockY(),
                "%z%", loc.getBlockZ());
    }

    /** 取得指令的類型標籤（介面顯示用）。 */
    public static String typeLabel(String line) {
        String lower = line == null ? "" : line.trim().toLowerCase(Locale.ROOT);
        if (lower.startsWith(PLAYER)) return "&b玩家";
        if (lower.startsWith(OP)) return "&cOP";
        if (lower.startsWith(MESSAGE)) return "&d訊息";
        return "&a控制台";
    }
}
