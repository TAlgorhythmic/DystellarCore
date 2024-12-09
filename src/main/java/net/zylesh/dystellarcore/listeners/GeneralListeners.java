package net.zylesh.dystellarcore.listeners;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.utils.PluginMessageScheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class GeneralListeners implements Listener {

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
        if (DystellarCore.PREVENT_WEATHER && event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        PluginMessageScheduler.playerJoined(event.getPlayer());
    }
}
