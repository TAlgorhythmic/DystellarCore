package net.zylesh.dystellarcore.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements CommandExecutor {

    public JoinCommand() {
        Bukkit.getPluginCommand("join").setExecutor(this);
        Bukkit.getPluginCommand("j").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player)commandSender;
        if (strings.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /join <server>");
            return true;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(strings[0]);
        p.sendMessage(ChatColor.GREEN + "Connecting...");
        p.sendPluginMessage(DystellarCore.getInstance(), "BungeeCord", out.toByteArray());
        Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
            if (p.isOnline()) {
                p.sendMessage(ChatColor.RED + "Error trying to connect to server.");
            }
        }, 30L);
        return true;
    }
}
