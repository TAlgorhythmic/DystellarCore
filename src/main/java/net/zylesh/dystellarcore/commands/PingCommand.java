package net.zylesh.dystellarcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    public PingCommand() {
        Bukkit.getPluginCommand("ping").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            CraftPlayer player = (CraftPlayer) commandSender;
            if (strings.length < 1) {
                player.sendMessage(ChatColor.YELLOW + "Your ping: " + ChatColor.AQUA + player.getHandle().ping + "ms");
                return true;
            }
            CraftPlayer playerInt = (CraftPlayer) Bukkit.getPlayer(strings[0]);
            if (playerInt == null || !playerInt.isOnline()) {
                player.sendMessage(ChatColor.RED + strings[0] + " is not online.");
                return true;
            }
            player.sendMessage(ChatColor.YELLOW + strings[0] + "'s ping: " + ChatColor.AQUA + playerInt.getHandle().ping + "ms");
            return true;
        }
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /ping <player>");
            return true;
        }
        CraftPlayer playerInt = (CraftPlayer) Bukkit.getPlayer(strings[0]);
        if (!playerInt.isOnline()) {
            commandSender.sendMessage(ChatColor.RED + strings[0] + " is not online.");
            return true;
        }
        commandSender.sendMessage(ChatColor.YELLOW + strings[0] + "'s ping: " + ChatColor.AQUA + playerInt.getHandle().ping + "ms");
        return true;
    }
}
