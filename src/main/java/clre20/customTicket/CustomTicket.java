package clre20.customTicket;

import clre20.customTicket.command.CTicketCommand;
import clre20.customTicket.config.ConfigManager;
import clre20.customTicket.gui.CustomGuiHolder;
import clre20.customTicket.gui.GuiListener;
import clre20.customTicket.listener.ChatInputListener;
import clre20.customTicket.listener.PlayerListener;
import clre20.customTicket.listener.TicketUseListener;
import clre20.customTicket.manager.ChatInputManager;
import clre20.customTicket.manager.CommandExecutorService;
import clre20.customTicket.manager.CooldownManager;
import clre20.customTicket.manager.EditorManager;
import clre20.customTicket.manager.ItemSyncManager;
import clre20.customTicket.manager.TicketManager;
import clre20.customTicket.update.UpdateChecker;
import clre20.customTicket.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomTicket extends JavaPlugin {

    private static CustomTicket instance;

    private ConfigManager configManager;
    private TicketManager ticketManager;
    private ItemSyncManager itemSyncManager;
    private CooldownManager cooldownManager;
    private CommandExecutorService commandExecutorService;
    private ChatInputManager chatInputManager;
    private EditorManager editorManager;
    private UpdateChecker updateChecker;

    public static CustomTicket getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.load();

        ticketManager = new TicketManager(this);
        ticketManager.loadAll();
        itemSyncManager = new ItemSyncManager(ticketManager);
        cooldownManager = new CooldownManager();
        commandExecutorService = new CommandExecutorService(this);
        chatInputManager = new ChatInputManager(this);
        editorManager = new EditorManager();
        updateChecker = new UpdateChecker(this);

        PluginCommand command = getCommand("cticket");
        if (command != null) {
            CTicketCommand executor = new CTicketCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().severe("plugin.yml 中找不到 cticket 指令！");
        }

        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new GuiListener(), this);
        pm.registerEvents(new TicketUseListener(this), this);
        pm.registerEvents(new ChatInputListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);

        itemSyncManager.syncOnlinePlayers();

        log("&8==========================================");
        log("&6 CustomTicket &7(C-Ticket) &ev" + getPluginMeta().getVersion() + " &a已啟用");
        log("&7 作者: Clre20 | 支援版本: 1.21.x / 26.x");
        log("&8==========================================");

        if (configManager.updateCheckEnabled()) {
            updateChecker.check(result -> {
                if (result.success() && result.newer()) {
                    updateChecker.report(Bukkit.getConsoleSender(), result);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        // 關閉所有本插件的介面，避免重載後殘留
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof CustomGuiHolder) {
                player.closeInventory();
            }
        }
        if (chatInputManager != null) chatInputManager.clearAll();
        if (editorManager != null) editorManager.clearAll();
        instance = null;
    }

    private void log(String text) {
        Bukkit.getConsoleSender().sendMessage(TextUtil.parse(text));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public ItemSyncManager getItemSyncManager() {
        return itemSyncManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CommandExecutorService getCommandExecutorService() {
        return commandExecutorService;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    public EditorManager getEditorManager() {
        return editorManager;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
