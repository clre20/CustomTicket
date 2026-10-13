package clre20.customTicket.gui;

import clre20.customTicket.manager.CommandExecutorService;
import clre20.customTicket.model.Ticket;
import clre20.customTicket.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 玩家右鍵門票後的確認介面。 */
public class ConfirmUseGui extends CustomGuiHolder {

    private static final int SLOT_YES = 11;
    private static final int SLOT_TICKET = 13;
    private static final int SLOT_NO = 15;

    private final String ticketId;
    private final EquipmentSlot hand;
    private boolean handled;

    public ConfirmUseGui(Ticket ticket, EquipmentSlot hand, Player player) {
        this.ticketId = ticket.getId();
        this.hand = hand;
        createInventory(27, plugin.getConfigManager().guiTitle("confirm-title", "%name%", ticket.getId()));

        List<String> prompt = new ArrayList<>();
        if (ticket.getPrompt().isEmpty()) {
            prompt.add("&e確定要使用 &r" + ticket.getDisplayName() + " &e嗎？");
        } else {
            for (String line : ticket.getPrompt()) prompt.add(CommandExecutorService.applyPlaceholders(player, line));
        }

        ItemStack preview = plugin.getTicketManager().buildPreview(ticket);
        preview.editMeta(meta -> meta.lore(TextUtil.parseLore(prompt)));
        inventory.setItem(SLOT_TICKET, preview);

        List<String> yesLore = new ArrayList<>(prompt);
        yesLore.addAll(List.of("", "&7將消耗 &e1 &7張門票"));
        inventory.setItem(SLOT_YES, button(Material.LIME_CONCRETE, "&a&l是", yesLore));
        inventory.setItem(SLOT_NO, button(Material.RED_CONCRETE, "&c&l否", "&7不使用門票"));
        fill(Material.GRAY_STAINED_GLASS_PANE);
    }

    @Override
    public void handleClick(InventoryClickEvent event, Player player) {
        if (!isTopSlot(event) || handled) return;
        int slot = event.getRawSlot();
        if (slot == SLOT_NO) {
            handled = true;
            later(player::closeInventory);
        } else if (slot == SLOT_YES) {
            handled = true;
            later(player::closeInventory);
            use(player);
        }
    }

    private void use(Player player) {
        Ticket ticket = plugin.getTicketManager().get(ticketId);
        if (ticket == null) {
            plugin.getConfigManager().send(player, "ticket-invalid");
            return;
        }
        // 再次確認手上仍是同一種門票，避免切換物品造成漏洞
        ItemStack inHand = player.getInventory().getItem(hand);
        if (!ticket.getId().equalsIgnoreCase(String.valueOf(plugin.getTicketManager().getTicketId(inHand)))) {
            plugin.getConfigManager().send(player, "ticket-missing");
            return;
        }
        if (ticket.hasPermission() && !player.hasPermission(ticket.getPermission())) {
            plugin.getConfigManager().send(player, "ticket-no-permission");
            return;
        }
        long remaining = plugin.getCooldownManager().remainingSeconds(player.getUniqueId(), ticket.getId());
        if (remaining > 0) {
            plugin.getConfigManager().send(player, "ticket-cooldown", "%time%", remaining);
            return;
        }

        // 先扣除門票再執行指令
        if (inHand.getAmount() <= 1) {
            player.getInventory().setItem(hand, null);
        } else {
            inHand.setAmount(inHand.getAmount() - 1);
            player.getInventory().setItem(hand, inHand);
        }
        plugin.getCooldownManager().apply(player.getUniqueId(), ticket.getId(), ticket.getCooldown());
        plugin.getConfigManager().send(player, "ticket-used", "%name%", ticket.getDisplayName());
        // 等介面關閉後的下一刻再執行，避免在點擊事件中執行指令
        List<String> commands = List.copyOf(ticket.getCommands());
        later(() -> {
            if (player.isOnline()) plugin.getCommandExecutorService().executeAll(player, commands);
        });
    }
}
