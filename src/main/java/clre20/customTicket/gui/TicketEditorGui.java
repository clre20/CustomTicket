package clre20.customTicket.gui;

import clre20.customTicket.manager.EditorManager.EditorSession;
import clre20.customTicket.model.Ticket;
import clre20.customTicket.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 建立 / 編輯門票的主介面。 */
public class TicketEditorGui extends EditorGui {

    private static final int SLOT_ITEM = 13;
    private static final int SLOT_NAME = 19;
    private static final int SLOT_LORE = 20;
    private static final int SLOT_COMMANDS = 21;
    private static final int SLOT_PROMPT = 22;
    private static final int SLOT_PERMISSION = 23;
    private static final int SLOT_COOLDOWN = 24;
    private static final int SLOT_GLOW = 25;
    private static final int SLOT_SAVE = 48;
    private static final int SLOT_CANCEL = 50;

    public TicketEditorGui(EditorSession session) {
        super(session);
        createInventory(54, plugin.getConfigManager().guiTitle("editor-title", "%name%", session.getDraft().getId()));
        render();
    }

    private void render() {
        inventory.clear();
        Ticket t = session.getDraft();

        if (t.getBaseItem() == null) {
            inventory.setItem(SLOT_ITEM, button(Material.BARRIER, "&c尚未設定門票物品",
                    "&7左鍵點擊下方背包中的物品", "&7即可設為門票物品"));
        } else {
            ItemStack preview = plugin.getTicketManager().buildPreview(t);
            preview.editMeta(meta -> {
                List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
                lore.add(TextUtil.parseItem(" "));
                lore.add(TextUtil.parseItem("&8[門票預覽] &7點擊背包物品可更換"));
                meta.lore(lore);
            });
            inventory.setItem(SLOT_ITEM, preview);
        }

        inventory.setItem(SLOT_NAME, button(Material.NAME_TAG, "&e門票名稱",
                "&7目前: &r" + t.getDisplayName(), "", "&a左鍵 &7修改"));

        List<String> lore = new ArrayList<>(List.of("&7目前:"));
        lore.addAll(valueLines(t.getLore(), "(無)"));
        lore.addAll(List.of("", "&a左鍵 &7修改"));
        inventory.setItem(SLOT_LORE, button(Material.WRITABLE_BOOK, "&e門票描述", lore));

        List<String> cmds = new ArrayList<>(List.of("&7共 &e" + t.getCommands().size() + " &7條指令:"));
        cmds.addAll(valueLines(t.getCommands(), "(無)"));
        cmds.addAll(List.of("", "&a左鍵 &7管理指令"));
        inventory.setItem(SLOT_COMMANDS, button(Material.COMMAND_BLOCK, "&e右鍵執行的指令", cmds));

        List<String> prompt = new ArrayList<>(List.of("&7使用門票時於確認介面顯示:"));
        prompt.addAll(valueLines(t.getPrompt(), "(無)"));
        prompt.addAll(List.of("", "&a左鍵 &7修改"));
        inventory.setItem(SLOT_PROMPT, button(Material.OAK_SIGN, "&e提示詞", prompt));

        inventory.setItem(SLOT_PERMISSION, button(Material.TRIPWIRE_HOOK, "&e使用權限",
                "&7目前: &f" + (t.hasPermission() ? t.getPermission() : "無 (所有人可用)"), "", "&a左鍵 &7修改"));

        inventory.setItem(SLOT_COOLDOWN, button(Material.CLOCK, "&e冷卻時間",
                "&7目前: &f" + (t.getCooldown() > 0 ? t.getCooldown() + " 秒" : "無"), "", "&a左鍵 &7修改"));

        inventory.setItem(SLOT_GLOW, button(t.isGlow() ? Material.ENCHANTED_BOOK : Material.BOOK, "&e附魔光效",
                "&7目前: " + (t.isGlow() ? "&a開啟" : "&c關閉"), "", "&a左鍵 &7切換"));

        inventory.setItem(SLOT_SAVE, button(Material.LIME_CONCRETE, "&a&l確定並儲存",
                "&7儲存門票設定", "&7已發出的門票會自動更新"));
        inventory.setItem(SLOT_CANCEL, button(Material.RED_CONCRETE, "&c&l取消", "&7放棄所有變更"));

        fill(Material.GRAY_STAINED_GLASS_PANE);
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        Ticket t = session.getDraft();

        if (!isTopSlot(event)) {
            // 點擊自己背包的物品 → 複製為門票物品（不會拿走原物品）
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;
            ItemStack base = clicked.clone();
            base.editMeta(meta -> {
                meta.getPersistentDataContainer().remove(plugin.getTicketManager().KEY_TICKET_ID);
                meta.getPersistentDataContainer().remove(plugin.getTicketManager().KEY_TICKET_REV);
            });
            t.setBaseItem(base);
            plugin.getConfigManager().send(player, "item-set", "%item%", base.getType().name());
            render();
            return;
        }

        switch (event.getRawSlot()) {
            case SLOT_NAME -> askInput(player, "input-name", t::setDisplayName, () -> reopen(player));
            case SLOT_LORE -> askInput(player, "input-lore", text -> t.setLore(splitLines(text)), () -> reopen(player));
            case SLOT_PROMPT -> askInput(player, "input-prompt", text -> t.setPrompt(splitLines(text)), () -> reopen(player));
            case SLOT_PERMISSION -> askInput(player, "input-permission",
                    text -> t.setPermission(text.equals("無") || text.equalsIgnoreCase("none") ? "" : text.replace(" ", "")),
                    () -> reopen(player));
            case SLOT_COOLDOWN -> askInput(player, "input-cooldown", text -> {
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) throw new NumberFormatException();
                    t.setCooldown(value);
                } catch (NumberFormatException e) {
                    plugin.getConfigManager().send(player, "invalid-number");
                }
            }, () -> reopen(player));
            case SLOT_GLOW -> {
                t.setGlow(!t.isGlow());
                render();
            }
            case SLOT_COMMANDS -> later(() -> new CommandListGui(session).open(player));
            case SLOT_SAVE -> save(player);
            case SLOT_CANCEL -> {
                plugin.getEditorManager().end(player.getUniqueId());
                plugin.getConfigManager().send(player, "editor-cancelled");
                later(player::closeInventory);
            }
            default -> {
            }
        }
    }

    private void save(Player player) {
        Ticket t = session.getDraft();
        if (t.getBaseItem() == null) {
            plugin.getConfigManager().send(player, "no-item-set");
            return;
        }
        if (session.isNew() && plugin.getTicketManager().exists(t.getId())) {
            plugin.getConfigManager().send(player, "ticket-exists", "%name%", t.getId());
            return;
        }
        if (!plugin.getTicketManager().save(t.copy())) return;
        plugin.getEditorManager().end(player.getUniqueId());
        plugin.getItemSyncManager().syncOnlinePlayers();
        plugin.getConfigManager().send(player, "ticket-saved", "%name%", t.getId());
        later(player::closeInventory);
    }

    private void reopen(Player player) {
        new TicketEditorGui(session).open(player);
    }

    /** 以 | 分行，輸入「清除」代表清空。 */
    static List<String> splitLines(String text) {
        if (text.equals("清除") || text.equalsIgnoreCase("clear")) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(text.split("\\|", -1)));
    }
}
