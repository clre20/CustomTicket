package clre20.customTicket.manager;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.model.Ticket;
import clre20.customTicket.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class TicketManager {

    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private final CustomTicket plugin;
    private final File dataFolder;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    public final NamespacedKey KEY_TICKET_ID;
    public final NamespacedKey KEY_TICKET_REV;

    public TicketManager(CustomTicket plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.KEY_TICKET_ID = new NamespacedKey(plugin, "ticket_id");
        this.KEY_TICKET_REV = new NamespacedKey(plugin, "ticket_rev");
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    // ------------------------------------------------------------------
    //  載入 / 儲存
    // ------------------------------------------------------------------

    public void loadAll() {
        tickets.clear();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("無法建立資料夾: " + dataFolder.getPath());
            return;
        }
        File[] files = dataFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4);
            if (!isValidName(id)) {
                plugin.getLogger().warning("略過名稱不合法的門票檔案: " + file.getName());
                continue;
            }
            try {
                tickets.put(key(id), load(id, YamlConfiguration.loadConfiguration(file)));
            } catch (Exception e) {
                plugin.getLogger().warning("讀取門票 " + file.getName() + " 失敗: " + e.getMessage());
            }
        }
        plugin.getLogger().info("已載入 " + tickets.size() + " 張門票。");
    }

    private Ticket load(String id, YamlConfiguration yml) {
        Ticket t = new Ticket(id);
        String encoded = yml.getString("item", "");
        if (!encoded.isEmpty()) {
            try {
                t.setBaseItem(ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded)));
            } catch (Exception e) {
                plugin.getLogger().warning("門票 " + id + " 的物品資料損毀，改用 material 欄位: " + e.getMessage());
            }
        }
        if (t.getBaseItem() == null) {
            Material material = Material.matchMaterial(yml.getString("material", "PAPER"));
            t.setBaseItem(new ItemStack(material == null || !material.isItem() ? Material.PAPER : material));
        }
        t.setDisplayName(yml.getString("name", "&6" + id));
        t.setLore(yml.getStringList("lore"));
        t.setPrompt(yml.getStringList("prompt"));
        t.setCommands(yml.getStringList("commands"));
        t.setPermission(yml.getString("permission", ""));
        t.setCooldown(yml.getInt("cooldown", 0));
        t.setGlow(yml.getBoolean("glow", false));
        t.setRevision(yml.getLong("revision", 0L));
        return t;
    }

    /** 儲存門票並更新版本號，回傳是否成功。 */
    public boolean save(Ticket ticket) {
        ticket.setRevision(System.currentTimeMillis());
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("revision", ticket.getRevision());
        ItemStack base = ticket.getBaseItem() == null ? new ItemStack(Material.PAPER) : ticket.getBaseItem();
        yml.set("item", Base64.getEncoder().encodeToString(base.serializeAsBytes()));
        yml.set("material", base.getType().name());
        yml.setInlineComments("material", List.of("僅供閱讀，實際物品以 item 欄位為準"));
        yml.set("name", ticket.getDisplayName());
        yml.set("lore", ticket.getLore());
        yml.set("prompt", ticket.getPrompt());
        yml.set("commands", ticket.getCommands());
        yml.set("permission", ticket.getPermission());
        yml.set("cooldown", ticket.getCooldown());
        yml.set("glow", ticket.isGlow());
        try {
            if (!dataFolder.exists()) dataFolder.mkdirs();
            yml.save(new File(dataFolder, ticket.getId() + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("儲存門票 " + ticket.getId() + " 失敗: " + e.getMessage());
            return false;
        }
        tickets.put(key(ticket.getId()), ticket);
        return true;
    }

    public boolean delete(String id) {
        Ticket removed = tickets.remove(key(id));
        if (removed == null) return false;
        File file = new File(dataFolder, removed.getId() + ".yml");
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("無法刪除檔案: " + file.getPath());
        }
        return true;
    }

    // ------------------------------------------------------------------
    //  查詢
    // ------------------------------------------------------------------

    public Ticket get(String id) {
        return id == null ? null : tickets.get(key(id));
    }

    public boolean exists(String id) {
        return get(id) != null;
    }

    public Collection<Ticket> getAll() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        for (Ticket t : tickets.values()) ids.add(t.getId());
        return ids;
    }

    private static String key(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------
    //  物品
    // ------------------------------------------------------------------

    public ItemStack buildItem(Ticket ticket, int amount) {
        ItemStack item = buildPreview(ticket);
        item.editMeta(meta -> {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY_TICKET_ID, PersistentDataType.STRING, ticket.getId());
            pdc.set(KEY_TICKET_REV, PersistentDataType.LONG, ticket.getRevision());
        });
        item.setAmount(Math.max(1, amount));
        return item;
    }

    /** 不含 PDC 標記的外觀預覽（用於介面顯示）。 */
    public ItemStack buildPreview(Ticket ticket) {
        ItemStack item = ticket.getBaseItem() == null ? new ItemStack(Material.PAPER) : ticket.getBaseItem().clone();
        item.setAmount(1);
        item.editMeta(meta -> applyLook(meta, ticket));
        return item;
    }

    private void applyLook(ItemMeta meta, Ticket ticket) {
        meta.displayName(TextUtil.parseItem(ticket.getDisplayName()));
        meta.lore(ticket.getLore().isEmpty() ? null : TextUtil.parseLore(ticket.getLore()));
        meta.setEnchantmentGlintOverride(ticket.isGlow() ? Boolean.TRUE : null);
    }

    /** 取得物品上的門票 ID，非門票回傳 null。 */
    public String getTicketId(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(KEY_TICKET_ID, PersistentDataType.STRING);
    }

    public long getTicketRevision(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return -1L;
        Long rev = item.getItemMeta().getPersistentDataContainer().get(KEY_TICKET_REV, PersistentDataType.LONG);
        return rev == null ? -1L : rev;
    }
}
