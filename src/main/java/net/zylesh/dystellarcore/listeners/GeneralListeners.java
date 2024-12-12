package net.zylesh.dystellarcore.listeners;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.config.ConfValues;
import net.zylesh.dystellarcore.services.messaging.Types;
import net.zylesh.dystellarcore.utils.PluginMessageScheduler;
import net.zylesh.dystellarcore.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class GeneralListeners implements Listener {

    private final Set<UUID> awaitingPlayers = new HashSet<>();

    public GeneralListeners() {
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("op")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void command(ServerCommandEvent event) {
        if (event.getCommand().startsWith("op")) {
            event.setCommand("whatever");
        }
    }

    @EventHandler
    public void remoteCommand(RemoteServerCommandEvent event) {
        if (event.getCommand().startsWith("op")) {
            event.setCommand("whatever");
        }
    }

    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {
        if (ConfValues.PREVENT_WEATHER && event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
		Player p = event.getPlayer();
        PluginMessageScheduler.playerJoined(p);
        awaitingPlayers.add(p.getUniqueId());
        Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> Utils.sendPluginMessage(p, Types.REGISTER), 15L);
        Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
            if (awaitingPlayers.contains(event.getPlayer().getUniqueId())) {
                p.kickPlayer(ChatColor.RED + "You are not allowed to join this server. (Contact us if you think this is an error)");
                awaitingPlayers.remove(event.getPlayer().getUniqueId());
            }
        }, 35L);
    }
}
