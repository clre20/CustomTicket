package clre20.customTicket.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private final String id;
    private ItemStack baseItem;
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private List<String> commands = new ArrayList<>();
    private List<String> prompt = new ArrayList<>();
    private String permission = "";
    private int cooldown;
    private boolean glow;
    private long revision;

    public Ticket(String id) {
        this.id = id;
        this.displayName = "&6" + id;
    }

    /** 深層複製，供編輯器草稿使用。 */
    public Ticket copy() {
        Ticket t = new Ticket(id);
        t.baseItem = baseItem == null ? null : baseItem.clone();
        t.displayName = displayName;
        t.lore = new ArrayList<>(lore);
        t.commands = new ArrayList<>(commands);
        t.prompt = new ArrayList<>(prompt);
        t.permission = permission;
        t.cooldown = cooldown;
        t.glow = glow;
        t.revision = revision;
        return t;
    }

    public String getId() {
        return id;
    }

    public ItemStack getBaseItem() {
        return baseItem;
    }

    public void setBaseItem(ItemStack baseItem) {
        if (baseItem == null || baseItem.getType() == Material.AIR) {
            this.baseItem = null;
            return;
        }
        ItemStack copy = baseItem.clone();
        copy.setAmount(1);
        this.baseItem = copy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = new ArrayList<>(commands);
    }

    public List<String> getPrompt() {
        return prompt;
    }

    public void setPrompt(List<String> prompt) {
        this.prompt = new ArrayList<>(prompt);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission == null ? "" : permission;
    }

    public boolean hasPermission() {
        return !permission.isBlank();
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }
}
