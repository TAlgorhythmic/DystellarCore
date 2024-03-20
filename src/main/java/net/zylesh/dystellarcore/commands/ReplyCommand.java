package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.utils.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand implements CommandExecutor {

    public ReplyCommand() {
        Bukkit.getPluginCommand("reply").setExecutor(this);
        Bukkit.getPluginCommand("r").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            if (playerUser.getPrivateMessagesMode() == User.PMS_DISABLED) {
                player.sendMessage(ChatColor.RED + "You can't send messages while having private messages disabled. Enable them with " + ChatColor.YELLOW + "/pms");
                return true;
            }
            if (strings.length - 1 >= 0) {
                if (playerUser.getLastMessagedPlayer() != null) {
                    Player lastMessaged = Bukkit.getPlayer(playerUser.getLastMessagedPlayer().getUUID());
                    if (lastMessaged != null) {
                        if (playerUser.getLastMessagedPlayer().getIgnoreList().contains(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "This player is ignoring you.");
                            return true;
                        }
                        if (!Validate.validateString(strings)) {
                            player.sendMessage(ChatColor.RED + "Looks like you are using uncommon type of characters. (Contact staff if you think this is an error)");
                            Bukkit.getLogger().warning(player.getName() + " tried to use invalid characters.");
                            return true;
                        }
                        StringBuilder message = new StringBuilder();
                        for (String string : strings) {
                            message.append(string).append(" ");
                        }
                        player.sendMessage(DystellarCore.MSG_SEND_FORMAT
                                .replaceAll("-sender", player.getPlayerListName())
                                .replaceAll("-receiver", lastMessaged.getPlayerListName())
                                .replaceAll("-message", message.toString()));
                        lastMessaged.sendMessage(DystellarCore.MSG_RECEIVE_FORMAT
                                .replaceAll("-sender", player.getPlayerListName())
                                .replaceAll("-receiver", lastMessaged.getPlayerListName())
                                .replaceAll("-message", message.toString()));
                    } else {
                        player.sendMessage(ChatColor.RED + "This player is not online.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have anyone to reply to.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /r <message>");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "You must be a player in order to execute this command.");
        }
        return true;
    }
}
