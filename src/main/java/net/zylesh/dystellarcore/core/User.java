package net.zylesh.dystellarcore.core;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.dystellarcore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class User {

    protected static final Map<UUID, User> users = new ConcurrentHashMap<>();

    public static User get(Player p) {
        return users.get(p.getUniqueId());
    }

    public synchronized static User get(UUID uuid) {
        return users.get(uuid);
    }

    public static Map<UUID, User> getUsers() {
        return users;
    }

    private final UUID id;
    private boolean globalChatEnabled;
    private boolean privateMessagesActive = true;
    private Suffix suffix = Suffix.NONE;
    private final TreeSet<Punishment> punishments = new TreeSet<>();
    private String language = "en";
    private User lastMessagedPlayer;
    private final String ip;
    private final String name;
    private final Set<String> notes = new HashSet<>();

    public User(UUID id, String ip, String name) {
        this.id = id;
        this.ip = ip;
        this.name = name;
    }

    public void punish(Punishment punishment) {
        this.punishments.add(punishment);
        punishment.onPunishment(this);
    }

    public Set<String> getNotes() {
        return notes;
    }

    public void addNote(String note) {
        notes.add(note);
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
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

    public Set<Punishment> getPunishments() {
        return punishments;
    }

    public UUID getUUID() {
        return id;
    }

    /**
     * Do not use if you don't know what you are doing, if you want to punish a player use user.punish(punishment) instead.
     * this method only works on offline players!
     */
    public void addPunishment(Punishment punishment) {
        this.punishments.add(punishment);
    }

    public User getLastMessagedPlayer() {
        return lastMessagedPlayer;
    }

    public void setLastMessagedPlayer(User lastMessagedPlayer) {
        this.lastMessagedPlayer = lastMessagedPlayer;
    }

    public boolean isPrivateMessagesActive() {
        return privateMessagesActive;
    }

    public void setPrivateMessagesActive(boolean privateMessagesActive) {
        this.privateMessagesActive = privateMessagesActive;
    }

    public static class UserListener implements Listener {

        public UserListener() {
            Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
            DystellarCore.getAsyncManager().scheduleAtFixedRate(() -> {
                synchronized (users) {
                    for (User user : users.values()) {
                       MariaDB.savePlayerToDatabase(user);
                    }
                }
            }, 10L, 10L, TimeUnit.MINUTES);
        }

        @EventHandler
        public void onJoin(AsyncPlayerPreLoginEvent event) {
            if (MariaDB.ENABLED) {
                User user = MariaDB.loadPlayerFromDatabase(event.getUniqueId(), event.getAddress().getHostName(), event.getName());
                Mapping map = MariaDB.loadMapping(event.getAddress().getHostName());
                if (user == null) user = new User(event.getUniqueId(), event.getAddress().getHostName(), event.getName());

                if (!user.getPunishments().isEmpty() && !DystellarCore.ALLOW_BANNED_PLAYERS) {
                    LocalDateTime now = LocalDateTime.now();
                    for (Punishment punishment : user.punishments) {
                        if (punishment.getExpirationDate().isBefore(now) && !punishment.allowJoinMinigames()) {
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getMessage().replaceAll("<reason>", punishment.getReason()).replaceAll("<time>", Utils.getTimeFormat(punishment.getExpirationDate())));
                            return;
                        }
                    }
                }
                if (map != null && map.getPunishments() != null && !map.getPunishments().isEmpty()) {
                    LocalDateTime now = LocalDateTime.now();
                    for (Punishment punishment : map.getPunishments()) {
                        if (punishment.getExpirationDate().isBefore(now)) {
                            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, punishment.getMessage().replaceAll("<reason>", punishment.getReason()).replaceAll("<time>", Utils.getTimeFormat(punishment.getExpirationDate())));
                        }
                    }
                }
                users.put(event.getUniqueId(), user);
            }
        }



        @EventHandler
        public void onLeave(PlayerQuitEvent event) {
            if (MariaDB.ENABLED) {
                DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
            }
        }

        @EventHandler
        public void onKick(PlayerKickEvent event) {
            if (MariaDB.ENABLED) {
                DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
            }
        }
    }
}
