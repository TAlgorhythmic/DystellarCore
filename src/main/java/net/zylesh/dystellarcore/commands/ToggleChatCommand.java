package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleChatCommand implements CommandExecutor {

    public ToggleChatCommand() {
        Bukkit.getPluginCommand("toggleglobalchat").setExecutor(this);
        Bukkit.getPluginCommand("togglechat").setExecutor(this);
        Bukkit.getPluginCommand("tgc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            playerUser.toggleGlobalChat();
            if (playerUser.isGlobalChatEnabled()) {
                player.sendMessage(ChatColor.GREEN + "You've enabled global chat.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "You've disabled global chat.");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "You must be a player in order to execute this command.");
        }
        return true;
    }
}
