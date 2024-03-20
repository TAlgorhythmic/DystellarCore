package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.practice.PUser;
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
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;
        if (strings.length < 1) {
            if (!player.hasPermission("dystellar.plus")) {
                player.sendMessage(ChatColor.RED + "You need " + ChatColor.GREEN + "plus " + ChatColor.RED + "rank to enable fly mode.");
                return true;
            }
            if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(ChatColor.RED + "You are not allowed to use this command in-game.");
                return true;
            }
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight()) {
                player.sendMessage(ChatColor.GREEN + "You have enabled the fly mode.");
            } else {
                player.setFlying(false);
                player.sendMessage(ChatColor.YELLOW + "You have disabled the fly mode.");
            }
        } else {
            if (!player.getName().equalsIgnoreCase(strings[0]) && !player.hasPermission("dystellar.mod")) {
                player.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            Player p = Bukkit.getPlayer(strings[0]);
            if (p == null || !p.isOnline()) {
                player.sendMessage(ChatColor.RED + "This player is not online.");
                return true;
            }
            p.setAllowFlight(!p.getAllowFlight());
            if (p.getAllowFlight()) {
                player.sendMessage(ChatColor.GREEN + "You have enabled the fly mode for " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + ".");
                p.sendMessage(ChatColor.GREEN + "An admin has enabled flight for you.");
            } else {
                p.setFlying(false);
                player.sendMessage(ChatColor.YELLOW + "You have disabled the fly mode for " + ChatColor.DARK_AQUA + player.getName() + ChatColor.YELLOW + ".");
                p.sendMessage(ChatColor.GREEN + "An admin has disabled flight for you.");
            }
        }
        return true;
    }
}
