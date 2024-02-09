package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.zylesh.dystellarcore.core.User.*;

public class TogglePrivateMessagesCommand implements CommandExecutor {

    public TogglePrivateMessagesCommand() {
        Bukkit.getPluginCommand("toggleprivatemessages").setExecutor(this);
        Bukkit.getPluginCommand("togglemessages").setExecutor(this);
        Bukkit.getPluginCommand("tpm").setExecutor(this);
        Bukkit.getPluginCommand("pms").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            playerUser.togglePms();
            switch (playerUser.getPrivateMessagesMode()) {
                case PMS_ENABLED:
                    player.sendMessage(ChatColor.GREEN + "You are now accepting all private messages.");
                    break;
                case PMS_ENABLED_WITH_IGNORELIST:
                    player.sendMessage(ChatColor.YELLOW + "You are now blocking messages from players in your ignore list.");
                    break;
                case PMS_ENABLED_FRIENDS_ONLY:
                    player.sendMessage(ChatColor.GOLD + "You are now only accepting messages from friends.");
                    break;
                case PMS_DISABLED:
                    player.sendMessage(ChatColor.RED + "You are now blocking all messages.");
                    break;
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "You must be a player in order to execute this command.");
        }
        return true;
    }
}
