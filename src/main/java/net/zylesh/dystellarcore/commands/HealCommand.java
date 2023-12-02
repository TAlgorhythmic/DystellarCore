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

public class HealCommand implements CommandExecutor {

    public HealCommand() {
        Bukkit.getPluginCommand("heal").setExecutor(this);
        Bukkit.getPluginCommand("hl").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) {
            if (!(commandSender instanceof Player)) return true;
            Player player = (Player) commandSender;
            if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(ChatColor.RED + "You are not allowed to use this command in game.");
                return true;
            }
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(12.0f);
        } else {
            Player player = Bukkit.getPlayer(strings[0]);
            if (player == null) {
                commandSender.sendMessage(ChatColor.RED + "This player is not online.");
                return true;
            }
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(12.0f);
        }
        return true;
    }
}
