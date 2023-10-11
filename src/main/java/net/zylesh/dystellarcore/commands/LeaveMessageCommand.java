package net.zylesh.dystellarcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.zylesh.dystellarcore.serialization.*;

public class LeaveMessageCommand implements CommandExecutor {

    public LeaveMessageCommand() {
        Bukkit.getPluginCommand("leavemessage").setExecutor(this);
        Bukkit.getPluginCommand("lm").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!MariaDB.ENABLED) return true;
        zcx cx
        return true;
    }
}
