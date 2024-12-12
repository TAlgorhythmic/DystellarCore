package net.zylesh.dystellarcore.services.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.zylesh.dystellarcore.commands.FriendCommand;
import net.zylesh.dystellarcore.commands.InboxCommand;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Sendable;
import net.zylesh.dystellarcore.core.inbox.senders.CoinsReward;
import net.zylesh.dystellarcore.core.inbox.senders.Message;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.serialization.Consts;
import net.zylesh.dystellarcore.serialization.InboxSerialization;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.dystellarcore.serialization.Punishments;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.zylesh.dystellarcore.commands.UnpunishCommand.createInventory;
import static net.zylesh.dystellarcore.commands.UnpunishCommand.invs;
import static org.bukkit.Bukkit.getLogger;

public class Types {

    public static final byte REGISTER = 0;
    public static final byte REGISTER_RECEIVED = 1;
    public static final byte INBOX_UPDATE = 2;
    public static final byte INBOX_MANAGER_UPDATE = 3;

    public static final byte GLOBAL_TAB_REGISTER = 4;
    public static final byte GLOBAL_TAB_UNREGISTER = 5;
    public static final byte FRIEND_ADD_REQUEST = 6;
    public static final byte FRIEND_ADD_REQUEST_APPROVE = 7;
    public static final byte FRIEND_ADD_REQUEST_DENY = 8;
    public static final byte FRIEND_ADD_REQUEST_DISABLED = 9;
    public static final byte FRIEND_ADD_REQUEST_ACCEPT = 10;
    public static final byte FRIEND_ADD_REQUEST_REJECT = 11;
    private static final byte DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS = 12;
    private static final byte DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS_RESPONSE = 13;
    public static final byte DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK = 14;
    public static final byte DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK_RESPONSE = 15;
    public static final byte REMOVE_FRIEND = 16;
    public static final byte DEMAND_FIND_PLAYER = 17;
    public static final byte DEMAND_FIND_PLAYER_RESPONSE = 18;
    public static final byte DEMAND_FIND_PLAYER_NOT_ONLINE = 19;
    public static final byte INBOX_SEND = 20;
    public static final byte SHOULD_SEND_PACK = 21;
    public static final byte SHOULD_SEND_PACK_RESPONSE = 22;
    public static final byte DEMAND_PUNISHMENTS_DATA = 23;
    public static final byte PUNISHMENTS_DATA_RESPONSE = 24;
    public static final byte REMOVE_PUNISHMENT_BY_ID = 25;
    public static final byte PUNISHMENT_ADD_PROXYBOUND = 26;
    public static final byte PUNISHMENT_ADD_CLIENTBOUND = 27;

    public static void handle(Player p, byte[] data) {

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        byte id = in.readByte();
        switch (id) {
            case DEMAND_PUNISHMENTS_DATA:
				Handler.handlePunData(in); break;
            case PUNISHMENTS_DATA_RESPONSE:
                Handler.handlePunDataRes(in); break;
            case REGISTER_RECEIVED: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                awaitingPlayers.remove(player.getUniqueId());
            } break;
            case INBOX_UPDATE: {
                UUID uuid = UUID.fromString(in.readUTF());
                if (!User.getUsers().containsKey(uuid)) return;
                User user = User.get(uuid);
                byte type = in.readByte();
                switch (type) {
                    case PKILL_EFFECT: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        PKillEffect effect = PKillEffect.valueOf(in.readUTF());
                        String title = in.readUTF();
                        String[] message = in.readUTF().split(":;");
                        String from = in.readUTF();
                        boolean claimed = in.readBoolean();
                        boolean deleted = in.readBoolean();
                        PKillEffectReward reward = new PKillEffectReward(user.getInbox(), iid, from, message, submission, deleted, title, claimed, effect);
                        reward.initializeIcons();
                        user.getInbox().addSender(reward);
                        break;
                    }
                    case COINS_REWARD: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        int coins = in.readInt();
                        String title = in.readUTF();
                        String[] message = in.readUTF().split(":;");
                        String from = in.readUTF();
                        boolean claimed = in.readBoolean();
                        boolean deleted = in.readBoolean();
                        CoinsReward reward = new CoinsReward(user.getInbox(), iid, from, message, submission, deleted, title, claimed, coins);
                        reward.initializeIcons();
                        user.getInbox().addSender(reward);
                        break;
                    }
                    case ELO_GAIN_NOTIFIER: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        int elo = in.readInt();
                        byte compatibility = in.readByte();
                        if (compatibility == EloGainNotifier.PRACTICE) {
                            String ladder = in.readUTF();
                            String[] message = in.readUTF().split(":;");
                            String from = in.readUTF();
                            boolean claimed = in.readBoolean();
                            boolean deleted = in.readBoolean();
                            EloGainNotifier reward = new EloGainNotifier(user.getInbox(), iid, elo, compatibility, ladder, from, message, submission, deleted, claimed);
                            reward.initializeIcons();
                            user.getInbox().addSender(reward);
                        } else if (compatibility == EloGainNotifier.SKYWARS) {
                            String[] message = in.readUTF().split(":;");
                            String from = in.readUTF();
                            boolean claimed = in.readBoolean();
                            boolean deleted = in.readBoolean();
                            EloGainNotifier reward = new EloGainNotifier(user.getInbox(), iid, elo, compatibility, null, from, message, submission, deleted, claimed);
                            reward.initializeIcons();
                            user.getInbox().addSender(reward);
                        }
                        break;
                    }
                    case MESSAGE: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        String[] message = in.readUTF().split(":;");
                        String from = in.readUTF();
                        boolean deleted = in.readBoolean();
                        Message reward = new Message(user.getInbox(), iid, from, message, submission, deleted);
                        reward.initializeIcons();
                        user.getInbox().addSender(reward);
                        break;
                    }
                }
                break;
            }
            case INBOX_MANAGER_UPDATE: {
                InboxCommand.g().init();
                break;
            }
            case FRIEND_ADD_REQUEST_APPROVE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "Friend request sent!");
                } else {
                    getLogger().warning("Friend request approve operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST_DENY: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.remove(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "This player is not online.");
                } else {
                    getLogger().warning("Friend request deny operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST_DISABLED: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.remove(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "This player is not accepting friend requests.");
                } else {
                    getLogger().warning("Friend request deny operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                String name = in.readUTF();
                requests.put(player.getUniqueId(), uuid);
                PacketPlayOutChat chat = new PacketPlayOutChat(ChatSerializer.a("[\"\",{\"text\":\"You've received a friend request from \",\"color\":\"dark_aqua\"},{\"text\":\"" + name + "\",\"color\":\"light_purple\"},{\"text\":\"!\",\"color\":\"dark_aqua\"},{\"text\":\" \",\"bold\":true,\"color\":\"green\"},{\"text\":\"[Accept]\",\"bold\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/f accept\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§aClick to accept!\"}},{\"text\":\" \",\"bold\":true,\"color\":\"red\"},{\"text\":\"[Reject]\",\"bold\":true,\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/f reject\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§cClick to reject!\"}}]"));
                player.sendPacket(chat);
                Bukkit.getScheduler().runTaskLater(this, () -> requests.remove(player.getUniqueId()), 800L);
                break;
            }
            case DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User u = User.get(player);
                if (u == null) return;
                boolean response;
                response = u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] == Consts.BYTE_TRUE;
                sendPluginMessage(player, DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS_RESPONSE, response);
            }
            case DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK_RESPONSE: {
                String pe = in.readUTF();
                if (runnables.containsKey(pe)) {
                    if (in.readBoolean())
                        runnables.get(pe).getKey().run();
                    else
                        runnables.get(pe).getValue().run();
                }
                break;
            }
            case REMOVE_FRIEND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                User u = User.get(player);
                if (u == null) {
                    getLogger().warning(player.getName() + " is supposed to delete a player from its friends list as stated by the packet received, but he is not online...");
                    return;
                }
                u.friends.remove(uuid);
                asyncManager.submit(() -> {
                    String name = MariaDB.loadName(uuid.toString());
                    if (name != null) {
                        player.sendMessage(ChatColor.RED + name + " has removed been removed from your friends list. (He removed you)");
                    }
                });
                break;
            }
            case DEMAND_FIND_PLAYER_RESPONSE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                String pla = in.readUTF();
                String srv = in.readUTF();
                if (srv.equals("null")) {
                    player.sendMessage(ChatColor.DARK_AQUA + "This player is joining the server right now!" + ChatColor.GRAY + " (Login screen)");
                } else {
                    player.sendMessage(ChatColor.DARK_AQUA + pla + ChatColor.WHITE + " is currently playing at " + ChatColor.YELLOW + srv + ChatColor.WHITE + ".");
                }
                break;
            }
            case DEMAND_FIND_PLAYER_NOT_ONLINE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                player.sendMessage(ChatColor.RED + "This player is not online.");
                break;
            }
            case FRIEND_ADD_REQUEST_ACCEPT: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                FriendCommand.requestAccepted(player, uuid, unsafe);
                break;
            }
            case FRIEND_ADD_REQUEST_REJECT: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                FriendCommand.requestRejected(player, unsafe);
                break;
            }
            case INBOX_SEND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User user = User.get(player);
                Sendable sender = InboxSerialization.stringToSender(in.readUTF(), user.getInbox());
                user.getInbox().addSender(sender);
                break;
            }
            case SHOULD_SEND_PACK_RESPONSE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User user = User.get(player);
                if (user.extraOptions[Consts.EXTRA_OPTION_RESOURCEPACK_PROMPT_POS] == Consts.BYTE_TRUE) {
                    player.openInventory(packConfirmation);
                    prompts.add(player);
                } else {
                    sendResourcePack(player);
                }
                break;
            }
            case PUNISHMENT_ADD_CLIENTBOUND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                String serialized = in.readUTF();
                Punishment punishment = Punishments.deserialize(serialized);
                User user = User.get(player);
                user.punish(punishment);
                break;
            }
            case REMOVE_PUNISHMENT_BY_ID: {
                String unsafe = in.readUTF();
                int pId = in.readInt();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) return;
                User user = User.get(player);
                Punishment punishmentToRemove = null;
                for (Punishment pun : user.getPunishments()) {
                    if (pun.hashCode() == pId) {
                        punishmentToRemove = pun;
                        break;
                    }
                }

                if (punishmentToRemove == null || !user.getPunishments().remove(punishmentToRemove)) return;
                player.sendMessage(ChatColor.GREEN + "The punishment with ID " + pId + " was removed from your punishments list!");
                String[] details = new String[] {
                        ChatColor.DARK_GREEN + "Punishment details:",
                        "===============================",
                        ChatColor.DARK_AQUA + "Type" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getClass().getSimpleName(),
                        ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + punishmentToRemove.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME),
                        ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + (punishmentToRemove.getExpirationDate() == null ? "Never" : punishmentToRemove.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME)),
                        ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + ChatColor.GRAY + punishmentToRemove.getReason(),
                        "==============================="
                };
                player.sendMessage(details);
                break;
            }
        }    }
}
