package net.zylesh.dystellarcore.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.practice.practicecore.Main;
import net.zylesh.practice.practicecore.Practice;
import net.zylesh.practice.practicecore.domain.stuff.Party;
import net.zylesh.practice.practicecore.domain.stuff.PlayerUser;
import net.zylesh.skywars.SkywarsAPI;
import net.zylesh.skywars.common.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ChatFormat implements Listener {

    private static Field recipientsField;
    static {
        try {
            recipientsField = AsyncPlayerChatEvent.class.getDeclaredField("recipients");
            recipientsField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
    }

    public ChatFormat(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getDisplayName();
        if (!User.get(event.getPlayer()).isGlobalChatEnabled()) event.setCancelled(true);
        if (DystellarCore.PRACTICE_HOOK) {
            PlayerUser player = Practice.getPlayerUser(event.getPlayer().getUniqueId());
            if (player.isInParty() && player.isPartyChatActive()) {
                event.setCancelled(true);
                Party party = player.getParty();
                for (PlayerUser pl : player.getParty().getPlayers()) {
                    if (party.getLeader().equals(player)) {
                        pl.getPlayer().sendMessage(ChatColor.DARK_PURPLE + player.getPlayer().getDisplayName() + ChatColor.RESET + ": " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
                    } else {
                        pl.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + player.getPlayer().getDisplayName() + ChatColor.RESET + ": " + ChatColor.AQUA + event.getMessage());
                    }
                }
            }
        } else if (DystellarCore.SKYWARS_HOOK) {
            net.zylesh.skywars.common.PlayerUser playerUser = SkywarsAPI.getPlayerUser(event.getPlayer());
            if (playerUser.isInGame()) {
                event.getRecipients().clear();
                Set<Player> recipients = new HashSet<>();
                for (Team t : playerUser.getGame().players) {
                    for (net.zylesh.skywars.common.PlayerUser playerUser1 : t.getPlayers()) {
                        recipients.add(playerUser1.getBukkitPlayer());
                    }
                }
                try {
                    recipientsField.set(event, recipients);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        User user = User.get(event.getPlayer());
        if (event.getPlayer().hasPermission("dystellar.plus")) {
            event.setFormat(PlaceholderAPI.setPlaceholders(event.getPlayer(), "%luckperms_prefix%" + playerName + " " + user.getSuffix() + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', event.getMessage())));
        } else {
            event.setFormat(PlaceholderAPI.setPlaceholders(event.getPlayer(), "%luckperms_prefix%" + playerName + " " + user.getSuffix() + ChatColor.WHITE + ": " + event.getMessage()));
        }
    }
}
