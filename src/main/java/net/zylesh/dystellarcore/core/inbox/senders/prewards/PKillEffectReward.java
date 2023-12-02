package net.zylesh.dystellarcore.core.inbox.senders.prewards;

import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.senders.Reward;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PKillEffectReward extends Reward {

    private final String killEffect;

    protected PKillEffectReward(Inbox inbox, String title, String from, String killEffect, String... messageLines) {
        super(inbox, title, from, messageLines);
        this.killEffect = killEffect;
    }

    @Override
    public void initializeIcons() {
        this.icon = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + title);
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        Arrays.asList(this.message).forEach(s -> lore.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Reward" + ChatColor.WHITE + ": " + ChatColor.GOLD + killEffect);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore.add(" ");
        lore.add(ChatColor.GREEN + "Click to claim.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        this.readIcon = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta1 = icon.getItemMeta();
        meta1.setDisplayName(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + title + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Claimed");
        List<String> lore1 = new ArrayList<>();
        lore1.add(" ");
        Arrays.asList(this.message).forEach(s -> lore1.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Reward" + ChatColor.WHITE + ": " + ChatColor.GOLD + killEffect);
        lore1.add(" ");
        lore1.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore1.add(" ");
        lore1.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore1.add(" ");
        lore1.add(ChatColor.YELLOW + "Right Click to delete.");
        meta1.setLore(lore1);
        icon.setItemMeta(meta1);
    }

    @Override
    public void onLeftClick() {

    }

    @Override
    public void onRightClick() {

    }

    @Override
    public boolean claim() {
        return false;
    }
}
