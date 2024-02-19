package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreCommand implements CommandExecutor {

    public IgnoreCommand() {
        Bukkit.getPluginCommand("ignore").setExecutor(this);
        Bukkit.getPluginCommand("block").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (strings.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /ignore <player>");
            return true;
        }
        Player pInt = Bukkit.getPlayer(strings[0]);
        if (pInt == null || !pInt.isOnline()) {
            p.sendMessage(ChatColor.RED + "This player does not exist or is not online.");
            return true;
        }
        if (User.get(p).getIgnoreList().add(pInt.getUniqueId())) {
            p.sendMessage(ChatColor.YELLOW + "You are now ignoring " + ChatColor.WHITE + pInt.getName());
        } else {
            p.sendMessage(ChatColor.RED + "You were already ignoring " + ChatColor.YELLOW + pInt.getName());
        }
        return true;
    }
}
