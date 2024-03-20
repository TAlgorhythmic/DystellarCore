package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.serialization.Mapping;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class IgnoreListCommand implements CommandExecutor {

    private static final String[] help = {
            ChatColor.DARK_AQUA + "/ignorelist list",
            ChatColor.DARK_AQUA + "/ignorelist remove <player>"
    };

    public IgnoreListCommand() {
        Bukkit.getPluginCommand("ignorelist").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (strings.length < 1) {
            p.sendMessage(help);
            return true;
        }
        User user = User.get(p);
        switch (strings[0]) {
            case "list": {
                DystellarCore.getAsyncManager().submit(() -> {
                    Set<Mapping> mappings = MariaDB.getUUIDMappings(user.getIgnoreList().toArray(new UUID[]{}));
                    if (mappings.isEmpty()) {
                        p.sendMessage(ChatColor.DARK_AQUA + "Your ignore list is empty. You have no enemies \uD83D\uDE0E");
                        return;
                    }
                    String[] message = new String[mappings.size() + 3];
                    message[0] = ChatColor.DARK_AQUA + "People that you are currently ignoring:";
                    message[1] = ChatColor.WHITE.toString() + ChatColor.STRIKETHROUGH + "----------------------------------";
                    message[message.length - 1] = ChatColor.WHITE.toString() + ChatColor.STRIKETHROUGH + "----------------------------------";
                    int pos = 2;
                    for (Mapping m : mappings) {
                        if (message[pos] != null || pos >= message.length-1) {
                            Bukkit.getLogger().warning("Algorithm error.");
                            break;
                        }
                        message[pos] = ChatColor.DARK_AQUA + m.getName();
                        pos++;
                    }
                    p.sendMessage(message);
                });
                break;
            }
            case "remove": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /ignorelist remove <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                if (pInt == null || !pInt.isOnline()) {
                    DystellarCore.getAsyncManager().submit(() -> {
                        UUID uuid = MariaDB.loadUUID(strings[1]);
                        if (uuid == null) {
                            p.sendMessage(ChatColor.RED + "This player is not registered in our database.");
                            return;
                        }
                        if (user.getIgnoreList().remove(uuid)) {
                            p.sendMessage(ChatColor.GREEN + "You've removed " + strings[1] + " from your ignore list.");
                        } else {
                            p.sendMessage(ChatColor.RED + "This player is not in your ignore list.");
                        }
                    });
                } else {
                    if (user.getIgnoreList().remove(pInt.getUniqueId())) {
                        p.sendMessage(ChatColor.GREEN + "You've removed " + strings[1] + " from your ignore list.");
                    } else {
                        p.sendMessage(ChatColor.RED + "This player is not in your ignore list.");
                    }
                }
            }
        }
        return true;
    }
}
