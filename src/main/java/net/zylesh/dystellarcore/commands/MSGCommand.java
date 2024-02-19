package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.practice.PUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.zylesh.dystellarcore.core.User.*;
import static net.zylesh.practice.PUser.*;

public class MSGCommand implements CommandExecutor {

    public MSGCommand() {
        Bukkit.getPluginCommand("message").setExecutor(this);
        Bukkit.getPluginCommand("msg").setExecutor(this);
        Bukkit.getPluginCommand("tell").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            if (playerUser.getPrivateMessagesMode() == PMS_DISABLED) {
                player.sendMessage(ChatColor.RED + "You can't send messages while blocking all private messages. Enable them with " + ChatColor.YELLOW + "/pms");
                return true;
            }
            if (strings.length - 1 >= 1) {
                Player playerInt = Bukkit.getPlayer(strings[0]);
                if (strings[0].isEmpty() || strings[1].isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
                } else {
                    if (playerInt != null && playerInt.isOnline()) {
                        User playerUserInt = User.get(playerInt);
                        switch (playerUserInt.getPrivateMessagesMode()) {
                            case PMS_ENABLED:
                                break;
                            case PMS_ENABLED_WITH_IGNORELIST:
                                if (playerUserInt.getIgnoreList().contains(player.getUniqueId())) {
                                    player.sendMessage(ChatColor.RED + "This player is ignoring you.");
                                    return true;
                                }
                                break;
                            case PMS_ENABLED_FRIENDS_ONLY:
                                // TODO
                                break;
                            case PMS_DISABLED:
                                player.sendMessage(DystellarCore.PLAYER_MSG_DISABLED.replaceAll("-player", strings[0]));
                                return true;
                        }
                        if (DystellarCore.PRACTICE_HOOK) {
                            PUser pUserInt = PUser.get(playerInt);
                            if (pUserInt.isInGame() && pUserInt.getLastGame().isRanked()) {
                                switch (pUserInt.getDoNotDisturbMode()) {
                                    case ENABLED_PMS_ONLY:
                                    case ENABLED: {
                                        player.sendMessage(ChatColor.RED + playerInt.getName() + " is in do not disturb mode.");
                                        return true;
                                    }
                                }
                            }
                        }
                        playerUser.setLastMessagedPlayer(playerUserInt);
                        playerUserInt.setLastMessagedPlayer(playerUser);
                        StringBuilder message = new StringBuilder();
                        for (int i = 1; i < strings.length; i++) {
                            message.append(strings[i]).append(" ");
                        }
                        player.sendMessage(DystellarCore.MSG_SEND_FORMAT
                                .replaceAll("-sender", player.getPlayerListName())
                                .replaceAll("-receiver", playerInt.getPlayerListName())
                                .replaceAll("-message", message.toString()));
                        playerInt.sendMessage(DystellarCore.MSG_RECEIVE_FORMAT
                                .replaceAll("-sender", player.getPlayerListName())
                                .replaceAll("-receiver", playerInt.getPlayerListName())
                                .replaceAll("-message", message.toString()));
                    } else {
                        player.sendMessage(ChatColor.RED + strings[0] + " is not online.");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "You must be a player in order to execute this command.");
        }
        return true;
    }
}