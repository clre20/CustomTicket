package clre20.customTicket.command;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.config.ConfigManager;
import clre20.customTicket.gui.TicketEditorGui;
import clre20.customTicket.manager.EditorManager.EditorSession;
import clre20.customTicket.manager.TicketManager;
import clre20.customTicket.model.Ticket;
import clre20.customTicket.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CTicketCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN = "customticket.admin";
    private static final int MAX_GIVE = 2304;
    private static final List<String> SUBCOMMANDS = List.of(
            "create", "del", "delete", "give", "edit", "help", "list", "reload");

    private final CustomTicket plugin;

    public CTicketCommand(CustomTicket plugin) {
        this.plugin = plugin;
    }

    private ConfigManager cfg() {
        return plugin.getConfigManager();
    }

    private TicketManager tickets() {
        return plugin.getTicketManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            handleVersion(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!sender.hasPermission(ADMIN)) {
            cfg().send(sender, "no-permission");
            return true;
        }
        switch (sub) {
            case "create" -> handleCreate(sender, label, args);
            case "del", "delete" -> handleDelete(sender, label, args);
            case "give" -> handleGive(sender, label, args);
            case "edit" -> handleEdit(sender, label, args);
            case "help" -> handleHelp(sender, label);
            case "list" -> handleList(sender, label);
            case "reload" -> handleReload(sender);
            default -> cfg().send(sender, "unknown-command", "%label%", label);
        }
        return true;
    }

    private void handleVersion(CommandSender sender) {
        cfg().send(sender, "version", "%version%", plugin.getUpdateChecker().currentVersion());
        if (!sender.hasPermission(ADMIN)) return;
        cfg().send(sender, "update-checking");
        plugin.getUpdateChecker().check(result -> plugin.getUpdateChecker().report(sender, result));
    }

    private void handleCreate(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            cfg().send(sender, "player-only");
            return;
        }
        if (args.length < 2) {
            cfg().send(sender, "usage-create", "%label%", label);
            return;
        }
        String name = args[1];
        if (!TicketManager.isValidName(name)) {
            cfg().send(sender, "invalid-name");
            return;
        }
        if (tickets().exists(name)) {
            cfg().send(sender, "ticket-exists", "%name%", name);
            return;
        }
        if (isBusy(player, name)) return;
        openEditor(player, new Ticket(name), true);
    }

    private void handleEdit(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            cfg().send(sender, "player-only");
            return;
        }
        if (args.length < 2) {
            cfg().send(sender, "usage-edit", "%label%", label);
            return;
        }
        Ticket ticket = tickets().get(args[1]);
        if (ticket == null) {
            cfg().send(sender, "ticket-not-found", "%name%", args[1]);
            return;
        }
        if (isBusy(player, ticket.getId())) return;
        openEditor(player, ticket.copy(), false);
    }

    /** 其他玩家正在編輯同名門票時回傳 true。 */
    private boolean isBusy(Player player, String name) {
        UUID editor = plugin.getEditorManager().getEditor(name);
        if (editor == null || editor.equals(player.getUniqueId())) return false;
        Player other = Bukkit.getPlayer(editor);
        cfg().send(player, "editor-busy", "%name%", name, "%player%", other == null ? editor : other.getName());
        return true;
    }

    private void openEditor(Player player, Ticket draft, boolean isNew) {
        plugin.getChatInputManager().cancelSilently(player.getUniqueId());
        EditorSession session = plugin.getEditorManager().start(player.getUniqueId(), draft, isNew);
        new TicketEditorGui(session).open(player);
    }

    private void handleDelete(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            cfg().send(sender, "usage-delete", "%label%", label);
            return;
        }
        Ticket ticket = tickets().get(args[1]);
        if (ticket == null || !tickets().delete(ticket.getId())) {
            cfg().send(sender, "ticket-not-found", "%name%", args[1]);
            return;
        }
        plugin.getCooldownManager().clearTicket(ticket.getId());
        cfg().send(sender, "ticket-deleted", "%name%", ticket.getId());
    }

    private void handleGive(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            cfg().send(sender, "usage-give", "%label%", label);
            return;
        }
        Ticket ticket = tickets().get(args[1]);
        if (ticket == null) {
            cfg().send(sender, "ticket-not-found", "%name%", args[1]);
            return;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                cfg().send(sender, "player-not-found", "%player%", args[2]);
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            cfg().send(sender, "usage-give", "%label%", label);
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                amount = -1;
            }
            if (amount < 1 || amount > MAX_GIVE) {
                cfg().send(sender, "invalid-amount");
                return;
            }
        }

        ItemStack template = tickets().buildItem(ticket, 1);
        int maxStack = Math.max(1, template.getMaxStackSize());
        int remaining = amount;
        while (remaining > 0) {
            ItemStack stack = template.clone();
            stack.setAmount(Math.min(maxStack, remaining));
            remaining -= stack.getAmount();
            for (ItemStack leftover : target.getInventory().addItem(stack).values()) {
                target.getWorld().dropItem(target.getLocation(), leftover);
            }
        }

        cfg().send(sender, "ticket-given", "%player%", target.getName(), "%amount%", amount, "%name%", ticket.getDisplayName());
        if (target != sender) {
            cfg().send(target, "ticket-received", "%amount%", amount, "%name%", ticket.getDisplayName());
        }
    }

    private void handleHelp(CommandSender sender, String label) {
        for (String line : cfg().rawMessageList("help", "%label%", label)) {
            sender.sendMessage(TextUtil.parse(line));
        }
    }

    private void handleList(CommandSender sender, String label) {
        if (tickets().getAll().isEmpty()) {
            cfg().send(sender, "list-empty");
            return;
        }
        sender.sendMessage(TextUtil.parse(cfg().rawMessage("list-header", "%count%", tickets().getAll().size())));
        for (Ticket t : tickets().getAll()) {
            Component hover = TextUtil.parse(t.getDisplayName());
            for (String line : t.getLore()) hover = hover.appendNewline().append(TextUtil.parse(line));
            hover = hover.appendNewline().appendNewline().append(TextUtil.parse(cfg().rawMessage("list-hover")));
            Component entry = TextUtil.parse(cfg().rawMessage("list-entry", "%name%", t.getId(), "%display%", t.getDisplayName()))
                    .hoverEvent(HoverEvent.showText(hover))
                    .clickEvent(ClickEvent.suggestCommand("/" + label + " give " + t.getId() + " "));
            sender.sendMessage(entry);
        }
    }

    private void handleReload(CommandSender sender) {
        cfg().load();
        tickets().loadAll();
        plugin.getItemSyncManager().syncOnlinePlayers();
        cfg().send(sender, "reloaded");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ADMIN)) return List.of();
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(SUBCOMMANDS);
        } else {
            String sub = args[0].toLowerCase(Locale.ROOT);
            boolean takesTicket = sub.equals("del") || sub.equals("delete") || sub.equals("give") || sub.equals("edit");
            if (args.length == 2 && takesTicket) {
                options.addAll(tickets().getIds());
            } else if (args.length == 3 && sub.equals("give")) {
                for (Player p : Bukkit.getOnlinePlayers()) options.add(p.getName());
            } else if (args.length == 4 && sub.equals("give")) {
                options.addAll(List.of("1", "16", "64"));
            }
        }
        String current = args[args.length - 1].toLowerCase(Locale.ROOT);
        options.removeIf(o -> !o.toLowerCase(Locale.ROOT).startsWith(current));
        return options;
    }
}
