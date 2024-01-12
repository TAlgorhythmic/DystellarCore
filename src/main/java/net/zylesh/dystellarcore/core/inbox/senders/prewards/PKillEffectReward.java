package net.zylesh.dystellarcore.core.inbox.senders.prewards;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.senders.Reward;
import net.zylesh.practice.PKillEffect;
import net.zylesh.practice.PUser;
import net.zylesh.practice.serialize.PMariaDB;
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
import java.util.concurrent.atomic.AtomicReference;

public final class PKillEffectReward extends Reward {

    public static final byte ID = 3;

    private final PKillEffect killEffect;

    public PKillEffectReward(Inbox inbox, String title, String from, PKillEffect killEffect, String... messageLines) {
        super(inbox, title, from, messageLines);
        this.killEffect = killEffect;
    }

    public PKillEffectReward(Inbox inbox, int id, String from, String[] messageLines, LocalDateTime submissionDate, boolean isDeleted, String title, boolean isClaimed, PKillEffect effect) {
        super(inbox, id, from, messageLines, submissionDate, isDeleted, title, isClaimed);
        this.killEffect = effect;
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
        this.isClaimed = claim();
        if (isClaimed) inbox.update();
    }

    public PKillEffect getKillEffect() {
        return killEffect;
    }

    @Override
    public void onRightClick() {
        if (isClaimed) {
            inbox.deleteSender(this);
            Player p = Bukkit.getPlayer(inbox.getUser().getUUID());
            if (p != null) p.playSound(p.getLocation(), Sound.CLICK, 1.4f, 1.4f);
            delete();
        }
    }

    @Override
    public byte getSerialID() {
        return ID;
    }

    @Override
    public boolean claim() {
        if (isClaimed) return false;
        AtomicReference<PUser> user = new AtomicReference<>(PUser.get(inbox.getUser().getUUID()));
        Player p = Bukkit.getPlayer(inbox.getUser().getUUID());
        if (user.get() == null) {
            DystellarCore.getAsyncManager().submit(() -> {
                user.set(PMariaDB.loadPlayerFromDatabase(inbox.getUser().getUUID(), false));
                if (user.get() == null) {
                    user.set(new PUser(inbox.getUser().getUUID()));
                }
                user.get().ownedEffects.add(killEffect);
            });
        } else {
            user.get().ownedEffects.add(killEffect);
        }
        p.sendMessage(ChatColor.GREEN + "Kill Effect claimed!");
        p.playSound(Bukkit.getPlayer(inbox.getUser().getUUID()).getLocation(), Sound.LEVEL_UP, 1.12f, 1.12f);
        return true;
    }

    @Override
    public PKillEffectReward clone(Inbox inbox) {
        return new PKillEffectReward(inbox, id, from, message, submissionDate, isDeleted, title, isClaimed, killEffect);
    }
}
