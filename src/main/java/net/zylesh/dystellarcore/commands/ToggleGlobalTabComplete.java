package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleGlobalTabComplete implements CommandExecutor {

    public ToggleGlobalTabComplete() {
        Bukkit.getPluginCommand("toggleglobaltabcomplete").setExecutor(this);
        Bukkit.getPluginCommand("tgtc").setExecutor(this);
        Bukkit.getPluginCommand("toggletabcomplete").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        p.sendMessage(ChatColor.GREEN + "Processing request...");
        User user = User.get(p);
        user.setGlobalTabComplete(!user.isGlobalTabComplete());
        if (user.isGlobalTabComplete()) {
            DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.GLOBAL_TAB_REGISTER);
            p.sendMessage(ChatColor.GREEN + "Global tab complete enabled.");
        } else {
            DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.GLOBAL_TAB_UNREGISTER);
            p.sendMessage(ChatColor.YELLOW + "Global tab complete disabled.");
        }
        return true;
    }
}
