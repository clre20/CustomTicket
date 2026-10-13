package clre20.customTicket.config;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final CustomTicket plugin;

    public ConfigManager(CustomTicket plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        applyJarDefaults();
        updateConfig();
    }

    /** 確保 getDefaults() 一定是 jar 內的 config.yml (UTF-8)。 */
    private void applyJarDefaults() {
        try (InputStream in = plugin.getResource("config.yml")) {
            if (in == null) return;
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            plugin.getConfig().setDefaults(defaults);
        } catch (Exception e) {
            plugin.getLogger().warning("無法讀取預設設定檔: " + e.getMessage());
        }
    }

    /** 自動補齊新版本新增的設定欄位（保留註解）。 */
    private void updateConfig() {
        FileConfiguration config = plugin.getConfig();
        Configuration defaults = config.getDefaults();
        if (defaults == null) return;

        List<String> added = new ArrayList<>();
        for (String key : defaults.getKeys(true)) {
            if (config.contains(key, true)) continue;
            if (defaults.isConfigurationSection(key)) {
                config.createSection(key);
            } else {
                config.set(key, defaults.get(key));
                added.add(key);
            }
            config.setComments(key, defaults.getComments(key));
            config.setInlineComments(key, defaults.getInlineComments(key));
        }

        int jarVersion = defaults.getInt("config-version", 1);
        if (config.getInt("config-version", 0) < jarVersion) {
            // 日後如需遷移舊欄位，在此處理
            config.set("config-version", jarVersion);
            added.add("config-version");
        }

        if (!added.isEmpty()) {
            plugin.saveConfig();
            Bukkit.getConsoleSender().sendMessage(TextUtil.parse(
                    "&8[&6C-Ticket&8] &a[Config] 已自動補齊 " + added.size() + " 個設定欄位: &e" + String.join(", ", added)));
        }
    }

    public String prefix() {
        return plugin.getConfig().getString("prefix", "&8[&6C-Ticket&8] &r");
    }

    public String raw(String path) {
        return plugin.getConfig().getString(path, path);
    }

    public String rawMessage(String key, Object... pairs) {
        return TextUtil.replace(raw("messages." + key), pairs);
    }

    public List<String> rawMessageList(String key, Object... pairs) {
        List<String> result = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("messages." + key)) {
            result.add(TextUtil.replace(line, pairs));
        }
        return result;
    }

    public String guiTitle(String key, Object... pairs) {
        return TextUtil.replace(raw("gui." + key), pairs);
    }

    public void send(CommandSender sender, String key, Object... pairs) {
        sender.sendMessage(TextUtil.parse(prefix() + rawMessage(key, pairs)));
    }

    public void sendRaw(CommandSender sender, String text) {
        sender.sendMessage(TextUtil.parse(prefix() + text));
    }

    public int inputTimeoutSeconds() {
        return Math.max(5, plugin.getConfig().getInt("chat-input-timeout", 60));
    }

    public boolean updateCheckEnabled() {
        return plugin.getConfig().getBoolean("update-checker.enabled", true);
    }

    public boolean updateNotifyOp() {
        return plugin.getConfig().getBoolean("update-checker.notify-op", true);
    }
}
