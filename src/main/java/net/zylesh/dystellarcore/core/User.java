package net.zylesh.dystellarcore.core;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class User {

    protected static final Map<UUID, User> users = new HashMap<>();

    public static User get(Player p) {
        return users.get(p.getUniqueId());
    }

    private final UUID id;
    private boolean globalChatEnabled;
    private Suffix suffix;
    private Set<Punishment> punishment = new HashSet<>();
    private String language = "en";

    public User(UUID id) {
        this.id = id;
    }

    public void punish(Punishment punishment) {
        punishment.onPunishment(this);
        this.punishment.add(punishment);
    }

    public boolean isGlobalChatEnabled() {
        return globalChatEnabled;
    }

    public Suffix getSuffix() {
        return suffix;
    }

    public void setSuffix(Suffix suffix) {
        this.suffix = suffix;
    }

    public void setGlobalChatEnabled(boolean globalChatEnabled) {
        this.globalChatEnabled = globalChatEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Punishment getPunishment() {
        return punishment;
    }

    public UUID getUUID() {
        return id;
    }

    /**
     * Do not use if you don't know what you are doing, if you want to punish a player use user.punish(punishment) instead.
     * this method only works on offline players!
     */
    public void setPunishment(Punishment punishment) {
        this.punishment = punishment;
    }

    public static class UserListener implements Listener {

        public UserListener() {
            Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
        }

        @EventHandler
        public void onJoin(PlayerLoginEvent event) {
            if (MariaDB.ENABLED) {
                User user = MariaDB.loadPlayerFromDatabase(event.getPlayer().getUniqueId());
                if (user == null) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Failed to load data from database, contact administration if you think this could be an error.");
                    return;
                }
                if (user.getPunishment() && !user.getPunishment().allowJoinMinigames() && !DystellarCore.ALLOW_BANNED_PLAYERS) {
                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED, user.getPunishment().getMessage());
                }
                users.put(event.getPlayer().getUniqueId(), user);
            }
        }

        @EventHandler
        public void onLeave(PlayerQuitEvent event) {
            if (MariaDB.ENABLED) {
                MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId()));
            }
        }

        @EventHandler
        public void onKick(PlayerKickEvent event) {
            if (MariaDB.ENABLED) {
                MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId()));
            }
        }
    }
}
