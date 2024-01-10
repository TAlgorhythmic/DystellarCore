package net.zylesh.dystellarcore.core.inbox.senders;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.inbox.Claimable;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.practice.Ladder;
import net.zylesh.practice.PApi;
import net.zylesh.practice.PUser;
import net.zylesh.skywars.SkywarsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EloGainNotifier extends Message implements Claimable {

    public static final byte ID = 1;

    public static final byte PRACTICE = 0;
    public static final byte SKYWARS = 1;

    private ItemStack readIcon;
    private boolean isClaimed = false;
    private final int elo;
    private final byte compatibilityType;
    private final String ladder;

    public EloGainNotifier(Inbox inbox, int elo, byte compatibilityType, @Nullable String ladder, String from, String... messageLines) {
        super(inbox, from, messageLines);
        this.elo = elo;
        this.compatibilityType = compatibilityType;
        this.ladder = ladder;
    }
    public EloGainNotifier(Inbox inbox, int id, int elo, byte compatibilityType, @Nullable String ladder, String from, String[] messageLines, LocalDateTime submissionDate, boolean isDeleted, boolean claimed) {
        super(inbox, id, from, messageLines, submissionDate, isDeleted);
        this.elo = elo;
        this.compatibilityType = compatibilityType;
        this.ladder = ladder;
        this.isClaimed = claimed;
    }

    public int getElo() {
        return elo;
    }

    public String getLadder() {
        return ladder;
    }

    public byte getCompatibilityType() {
        return compatibilityType;
    }

    @Override
    public void initializeIcons() {
        this.icon = new ItemStack(Material.COOKIE);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_AQUA + "Elo Notifier");
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        Arrays.asList(this.message).forEach(s -> lore.add(ChatColor.WHITE + s));
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "From" + ChatColor.WHITE + ": " + this.from);
        lore.add(" ");
        lore.add(ChatColor.DARK_AQUA + "Submission Date" + ChatColor.WHITE + ": " + submissionDate.format(DateTimeFormatter.ISO_DATE_TIME));
        lore.add(" ");
        lore.add(ChatColor.GREEN + "Click to claim.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        this.readIcon = new ItemStack(Material.COOKIE);
        ItemMeta meta1 = icon.getItemMeta();
        meta1.setDisplayName(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + "Elo Notifier" + ChatColor.GRAY + " - " + ChatColor.YELLOW + "Claimed");
        List<String> lore1 = new ArrayList<>();
        lore1.add(" ");
        Arrays.asList(this.message).forEach(s -> lore1.add(ChatColor.WHITE + s));
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
    public ItemStack getReadIcon() {
        return readIcon;
    }

    @Override
    public void onLeftClick() {
        this.isClaimed = claim();
        if (this.isClaimed) inbox.update();
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
    public boolean isClaimed() {
        return isClaimed;
    }

    @Override
    public byte getSerialID() {
        return ID;
    }

    @Override
    public boolean claim() {
        if (isClaimed) return false;
        switch (this.compatibilityType) {
            case PRACTICE: {
                Ladder ladder = PApi.LADDERS.get(this.ladder);
                PUser playerUser = PUser.get(inbox.getUser().getUUID());
                playerUser.getPlayer().playSound(playerUser.getPlayer().getLocation(), Sound.LEVEL_UP, 1.12f, 1.12f);
                playerUser.elo.put(ladder, playerUser.elo.get(ladder) + elo);
                return true;
            }
            case SKYWARS: {
                // TODO
                if (!DystellarCore.SKYWARS_HOOK) {
                    Bukkit.getPlayer(inbox.getUser().getUUID()).sendMessage(ChatColor.AQUA + "Join skywars to claim this compensation.");
                    return false;
                }
                net.zylesh.skywars.common.PlayerUser playerUser = SkywarsAPI.getPlayerUser(Bukkit.getPlayer(inbox.getUser().getUUID()));
                playerUser.getBukkitPlayer().playSound(playerUser.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 1.12f, 1.12f);
                playerUser.elo += elo;
                return true;
            }
        }
        return false;
    }

    @Override
    public EloGainNotifier clone(Inbox inbox) {
        return new EloGainNotifier(inbox, id, elo, compatibilityType, ladder, from, message, submissionDate, isDeleted, isClaimed);
    }
}
