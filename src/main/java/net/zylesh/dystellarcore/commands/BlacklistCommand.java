package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.Blacklist;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlacklistCommand implements CommandExecutor {

    public BlacklistCommand() {
        Bukkit.getPluginCommand("blacklist").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.admin")) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (strings.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /blacklist <player> <reason>");
            return true;
        }
        Player playerInt = Bukkit.getPlayer(strings[0]);
        if (playerInt != null && playerInt.isOnline()) {
            User userInt = User.get(playerInt);
            StringBuilder reason = new StringBuilder();
            for (int i = 2; i < strings.length; i++) {
                if (i == 2) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            Blacklist blacklist = new Blacklist(reason.toString());
            userInt.punish(blacklist);
        } else {
            DystellarCore.getAsyncManager().execute(() -> {
                Mapping mapping = MariaDB.loadMapping(strings[1]);
                if (mapping == null) {
                    commandSender.sendMessage(ChatColor.RED + "This player does not exist in the database.");
                } else {
                    User user = MariaDB.loadPlayerFromDatabase(mapping.getUUID(), mapping.getIP(), mapping.getName());
                    StringBuilder reason = new StringBuilder();
                    for (int i = 2; i < strings.length; i++) {
                        if (i == 2) reason.append(strings[i]);
                        else reason.append(" ").append(strings[i]);
                    }
                    Blacklist ban = new Blacklist(reason.toString());
                    user.addPunishment(ban);
                    MariaDB.savePlayerToDatabase(user);
                }
            });
        }
        // TODO check
        return true;
    }
}

