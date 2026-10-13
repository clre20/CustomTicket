package clre20.customTicket.gui;

import clre20.customTicket.CustomTicket;
import clre20.customTicket.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 所有自訂介面的基底：點擊一律取消，且只有左鍵會傳入 handleClick。 */
public abstract class CustomGuiHolder implements InventoryHolder {

    protected final CustomTicket plugin = CustomTicket.getInstance();
    protected Inventory inventory;

    protected void createInventory(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, TextUtil.parse(title));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    /** 左鍵點擊（含玩家自己的背包），事件已被取消。 */
    public abstract void handleClick(InventoryClickEvent event, Player player);

    public void handleClose(InventoryCloseEvent event, Player player) {
    }

    /** 下一刻執行，避免在點擊事件中直接切換介面。 */
    protected void later(Runnable runnable) {
        if (!plugin.isEnabled()) return;
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    protected boolean isTopSlot(InventoryClickEvent event) {
        return event.getRawSlot() >= 0 && event.getRawSlot() < inventory.getSize();
    }

    protected void fill(Material material) {
        ItemStack filler = button(material, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, filler);
        }
    }

    public static ItemStack button(Material material, String name, String... lore) {
        return button(material, name, Arrays.asList(lore));
    }

    public static ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(TextUtil.parseItem(name));
            if (!lore.isEmpty()) meta.lore(TextUtil.parseLore(lore));
            meta.addItemFlags(ItemFlag.values());
        });
        return item;
    }

    /** 將多行文字轉為「目前設定」的 lore 區塊。 */
    protected static List<String> valueLines(List<String> values, String empty) {
        List<String> lines = new ArrayList<>();
        if (values.isEmpty()) {
            lines.add("&8  " + empty);
        } else {
            for (String v : values) lines.add("&8  » &r" + v);
        }
        return lines;
    }
}
