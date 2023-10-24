package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MSGCommand implements CommandExecutor {

    public MSGCommand() {
        Bukkit.getPluginCommand("msg").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length - 1 >= 1) {
                Player playerInt = Bukkit.getPlayer(strings[0]);
                if (strings[0].isEmpty() || strings[1].isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
                } else {
                    if (playerInt.isOnline()) {
                        User playerUser = User.get(player);
                        User playerUserInt = User.get(playerInt);
                        if (playerUserInt.isPrivateMessagesActive()) {
                            playerUser.setLastMessagedPlayer(playerUserInt);
                            playerUserInt.setLastMessagedPlayer(playerUser);
                            StringBuilder message = new StringBuilder();
                            for (int i = 1; i < strings.length; i++) {
                                message.append(strings[i]).append(" ");
                            }
                            player.sendMessage(MSG_SEND_FORMAT
                                    .replaceAll("-sender", player.getPlayerListName())
                                    .replaceAll("-receiver", playerInt.getPlayerListName())
                                    .replaceAll("-message", message.toString()));
                            playerInt.sendMessage(MSG_RECEIVE_FORMAT
                                    .replaceAll("-sender", player.getPlayerListName())
                                    .replaceAll("-receiver", playerInt.getPlayerListName())
                                    .replaceAll("-message", message.toString()));
                        } else {
                            player.sendMessage(PLAYER_MSG_DISABLED.replaceAll("-player", strings[0]));
                        }
                    } else {
                        player.sendMessage(PLAYER_NOT_ONLINE.replaceAll("-player", strings[0]));
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