package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.core.inbox.InboxSender;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SenderContainer {

    private final InboxSender sender;
    private ItemStack icon;

    public SenderContainer(InboxSender sender) {
        this.sender = sender;
        init();
    }

    private void init() {
        icon = new ItemStack(sender.getUnreadIcon().getType());
        String title = sender.getUnreadIcon().getItemMeta().getDisplayName();
        List<String> strings = new ArrayList<>(sender.getUnreadIcon().getItemMeta().getLore());
        strings.add(" ");
        strings.add(ChatColor.DARK_AQUA + "ID: " + ChatColor.YELLOW + sender.getId());
        strings.add(ChatColor.GREEN + "Click to copy");
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(strings);
        icon.setItemMeta(meta);
    }

    public InboxSender getSender() {
        return sender;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
