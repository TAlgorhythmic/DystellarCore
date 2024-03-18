package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.serialization.Consts;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendCommand implements CommandExecutor, Listener {

    public static final Map<UUID, UUID> messagesCache = new HashMap<>();
    public static final Set<UUID> requestsCache = new HashSet<>();
    private static final Set<UUID> cooldowns = new HashSet<>();

    private static final Map<Player, Map<String, UUID>> uuidsCache = new ConcurrentHashMap<>(); // Avoid list connections spam to the database

    public static void requestAccepted(Player p, UUID uuid, String name) {
        if (!requestsCache.remove(p.getUniqueId())) {
            return;
        }
        User u = User.get(p);
        boolean sendTip = false;
        if (u.friends.isEmpty() && u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] == Consts.BYTE_FALSE) {
            u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] = Consts.BYTE_TRUE;
            sendTip = true;
        }
        u.friends.add(uuid);
        p.sendMessage(ChatColor.GREEN + name + " has accepted your friend request!");
        if (sendTip) p.sendMessage(Consts.FIRST_FRIEND_TIP_MSG);
    }

    public static void requestRejected(Player p, String name) {
        if (!requestsCache.remove(p.getUniqueId())) {
            return;
        }
        p.sendMessage(ChatColor.RED + name + " has rejected your friend request.");
    }

    private static final String[] help = {
            ChatColor.WHITE + "===========================",
            ChatColor.DARK_AQUA + "/friend add <player>",
            ChatColor.DARK_AQUA + "/friend remove <player>",
            ChatColor.DARK_AQUA + "/friend find <player>",
            ChatColor.DARK_AQUA + "/friend list",
            ChatColor.DARK_AQUA + "/friend accept",
            ChatColor.DARK_AQUA + "/friend reject",
            ChatColor.DARK_AQUA + "/friend togglerequests",
            ChatColor.WHITE + "==========================="
    };

    public FriendCommand() {
        Bukkit.getPluginCommand("friend").setExecutor(this);
        Bukkit.getPluginCommand("f").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        if (strings.length < 1) {
            commandSender.sendMessage(help);
            return true;
        }
        Player p = (Player) commandSender;
        switch (strings[0]) {
            case "add": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f add <player>");
                    return true;
                }
                if (cooldowns.contains(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You are on cooldown, wait 20 seconds before performing this action again.");
                    return true;
                }
                requestsCache.add(p.getUniqueId());
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST, strings[1]);
                break;
            }
            case "remove": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f remove <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                User u = User.get(p);
                if (pInt != null && pInt.isOnline()) {
                    if (!u.friends.remove(pInt.getUniqueId())) {
                        p.sendMessage(ChatColor.RED + "This player is not in your friends list. (Your friend probably changed his minecraft nickname and we do not have his new data)");
                        return true;
                    }
                    User uInt = User.get(pInt);
                    uInt.friends.remove(u.getUUID());
                    p.sendMessage(ChatColor.RED + pInt.getName() + " has been removed from your friends list.");
                    pInt.sendMessage(ChatColor.RED + p.getName() + " has removed been removed from your friends list. (He removed you)");
                } else {
                    DystellarCore.getAsyncManager().submit(() -> {
                        UUID uuid;
                        if (uuidsCache.containsKey(p)) {
                            uuid = uuidsCache.get(p).get(strings[1]);
                        } else {
                            uuid = MariaDB.loadUUID(strings[1]);
                            Map<String, UUID> map = new HashMap<>();
                            map.put(strings[1], uuid);
                            uuidsCache.put(p, map);
                        }
                        if (uuid == null) {
                            p.sendMessage(ChatColor.RED + "This player does not const in our database.");
                            return;
                        }
                        if (!u.friends.remove(uuid)) {
                            p.sendMessage(ChatColor.RED + "This player is not in your friends list.");
                            return;
                        }
                        DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.REMOVE_FRIEND, uuid.toString());
                    });
                }
                break;
            }
            case "find": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f find <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                if (pInt != null && pInt.isOnline()) {
                    p.sendMessage(ChatColor.GREEN + pInt.getName() + " is in your exact same server!");
                    return true;
                } else {
                    DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.DEMAND_FIND_PLAYER, strings[1]);
                }
                break;
            }
            case "list": {
                if (cooldowns.contains(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You are on cooldown, wait 20 seconds before performing this action again.");
                    return true;
                }
                cooldowns.add(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> cooldowns.remove(p.getUniqueId()), 400L);
                User u = User.get(p);
                DystellarCore.getAsyncManager().submit(() -> {
                    List<String> friends = new ArrayList<>();
                    synchronized (u.friends) {
                        for (UUID uuid : u.friends) {
                            User us = User.get(uuid);
                            if (us == null) {
                                String name = MariaDB.loadName(uuid.toString());
                                if (name != null) friends.add(name);
                            } else {
                                friends.add(us.getName());
                            }
                        }
                    }
                    p.sendMessage(ChatColor.DARK_GREEN + "Friends list:");
                    for (String st : friends) {
                        p.sendMessage(" - " + ChatColor.DARK_AQUA + st);
                    }
                });
                break;
            }
            case "accept": {
                if (!DystellarCore.getInstance().requests.containsKey(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You don't have any pending friend requests. (Or it has expired)");
                    return true;
                }
                User u = User.get(p);
                boolean sendTip = false;
                if (u.friends.isEmpty() && u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] == Consts.BYTE_FALSE) {
                    u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] = Consts.BYTE_TRUE;
                    sendTip = true;
                }
                UUID uuid = DystellarCore.getInstance().requests.remove(p.getUniqueId());
                u.friends.add(uuid);
                p.sendMessage(ChatColor.GREEN + "Friend request accepted!");
                if (sendTip) p.sendMessage(Consts.FIRST_FRIEND_TIP_MSG);
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST_ACCEPT, uuid.toString());
                break;
            }
            case "reject": {
                if (!DystellarCore.getInstance().requests.containsKey(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "You don't have any pending friend requests. (Or it has expired)");
                    return true;
                }
                UUID uuid = DystellarCore.getInstance().requests.remove(p.getUniqueId());
                p.sendMessage(ChatColor.RED + "Friend request rejected.");
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST_REJECT, uuid.toString());
                break;
            }
            case "togglerequests": {
                break;
            }
        }
        return true;
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void leave(PlayerKickEvent event) {
        remove(event.getPlayer());
    }

    private static void remove(Player p) {
        messagesCache.remove(p.getUniqueId());
        requestsCache.remove(p.getUniqueId());
    }
}
