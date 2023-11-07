package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MSGCommand implements CommandExecutor {

    public MSGCommand() {
        Bukkit.getPluginCommand("message").setExecutor(this);
        Bukkit.getPluginCommand("msg").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            if (!playerUser.isPrivateMessagesActive()) {
                player.sendMessage(ChatColor.RED + "You can't send messages while having private messages disabled. Enable them with " + ChatColor.YELLOW + "/tpm");
                return true;
            }
            if (strings.length - 1 >= 1) {
                Player playerInt = Bukkit.getPlayer(strings[0]);
                if (strings[0].isEmpty() || strings[1].isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
                } else {
                    if (playerInt.isOnline()) {
                        User playerUserInt = User.get(playerInt);
                        if (playerUserInt.isPrivateMessagesActive()) {
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
                            player.sendMessage(DystellarCore.PLAYER_MSG_DISABLED.replaceAll("-player", strings[0]));
                        }
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