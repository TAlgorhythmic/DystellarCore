package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeCommand implements CommandExecutor, Listener {

    public FreezeCommand() {
        Bukkit.getPluginCommand("freeze").setExecutor(this);
        Bukkit.getPluginCommand("ss").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    private static final Set<UUID> frozenPlayers = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /ss <player>");
            return true;
        }
        Player player = Bukkit.getPlayer(strings[0]);
        if (player == null) {
            commandSender.sendMessage(ChatColor.RED + "This player does not exist or is not online.");
            return true;
        }
        if (frozenPlayers.remove(player.getUniqueId())) {
            commandSender.sendMessage(ChatColor.GREEN + player.getName() + " is now free to go.");
            player.sendMessage(DystellarCore.UNFREEZE_MESSAGE);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            return true;
        }
        frozenPlayers.add(player.getUniqueId());
        for (String s1 : DystellarCore.FREEZE_MESSAGE) player.sendMessage(s1);
        commandSender.sendMessage(ChatColor.YELLOW + player.getName() + " is now frozen.");
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100));
        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()) event.getPlayer().teleport(event.getFrom());
        }
    }
}
