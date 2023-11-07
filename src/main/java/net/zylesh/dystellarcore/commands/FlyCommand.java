package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.practice.practicecore.Practice;
import net.zylesh.skywars.SkywarsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    public FlyCommand() {
        Bukkit.getPluginCommand("fly").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) {
            if (!(commandSender instanceof Player)) return true;
            Player player = (Player) commandSender;
            if (!player.hasPermission("dystellar.plus")) {
                player.sendMessage(ChatColor.RED + "You need " + ChatColor.GREEN + "plus " + ChatColor.RED + "rank to enable fly mode.");
                return true;
            }
            if ((DystellarCore.PRACTICE_HOOK && Practice.getPlayerUser(player.getUniqueId()).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(ChatColor.RED + "You are not allowed to use this command ingame.");
                return true;
            }
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight()) {
                player.sendMessage(ChatColor.GREEN + "You have enabled the fly mode.");
            } else {
                player.setFlying(false);
                player.sendMessage(ChatColor.YELLOW + "You have disabled the fly mode.");
            }
        }
        return true;
    }
}