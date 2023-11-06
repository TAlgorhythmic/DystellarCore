package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TogglePrivateMessagesCommand implements CommandExecutor {

    public TogglePrivateMessagesCommand() {
        Bukkit.getPluginCommand("toggleprivatemessages").setExecutor(this);
        Bukkit.getPluginCommand("togglemessages").setExecutor(this);
        Bukkit.getPluginCommand("tpm").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            playerUser.setPrivateMessagesActive(!playerUser.isPrivateMessagesActive());
            if (playerUser.isPrivateMessagesActive()) {
                player.sendMessage(ChatColor.GREEN + "You enabled private messages.");
            } else {
                player.sendMessage(ChatColor.RED + "You disabled private messages.");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "You must be a player in order to execute this command.");
        }
        return true;
    }
}
