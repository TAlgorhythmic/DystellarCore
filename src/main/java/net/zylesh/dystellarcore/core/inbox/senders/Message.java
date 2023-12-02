package net.zylesh.dystellarcore.core.inbox.senders;

import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.InboxSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message implements InboxSender {

    protected final String[] message;
    protected final LocalDateTime submissionDate = LocalDateTime.now();
    protected final Inbox inbox;
    protected final String from;
    protected ItemStack icon;
    protected boolean isDeleted = false;
    private final int id;

    public Message(Inbox inbox, String from, String... messageLines) {
        this.message = messageLines;
        this.inbox = inbox;
        this.from = from;
        this.id = (int) (Math.random() * 1000000);
        initializeIcons();
    }

    @Override
    public void initializeIcons() {
        this.icon = new ItemStack(Material.PAPER);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "Message");
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        Arrays.asList(this.message).forEach(s -> lore.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Right Click to delete.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
    }

    @Override
    public ItemStack getUnreadIcon() {
        return icon;
    }

    @Override
    public void setUnreadIcon(ItemStack itemStack) {}

    @Override
    public ItemStack getReadIcon() {
        return icon;
    }

    @Override
    public void setReadIcon(ItemStack itemStack) {}

    @Override
    public void onLeftClick() {}

    @Override
    public void onRightClick() {
        inbox.deleteSender(this);
        Player p = Bukkit.getPlayer(inbox.getUser().getUUID());
        if (p != null) p.playSound(p.getLocation(), Sound.CLICK, 1.4f, 1.4f);
        delete();
    }

    @Override
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        isDeleted = true;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int compareTo(InboxSender o) {
        return submissionDate.compareTo(o.getSubmissionDate());
    }
}
