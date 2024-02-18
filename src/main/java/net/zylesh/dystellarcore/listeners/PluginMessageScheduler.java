package net.zylesh.dystellarcore.listeners;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PluginMessageScheduler implements Listener {

    private static final Set<Task> tasks = Collections.synchronizedSet(new HashSet<>());

    public static void scheduleTask(Task task) {
        if (Bukkit.getOnlinePlayers().isEmpty())
            tasks.add(task);
        else {
            Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
            Player player = iterator.next();
            task.run(player);
        }
    }

    public PluginMessageScheduler() {
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!tasks.isEmpty()) {
            tasks.forEach(task -> task.run(event.getPlayer()));
        }
    }

    public interface Task {
        void run(Player player);
    }
}
