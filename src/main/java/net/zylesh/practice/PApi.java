package net.zylesh.practice;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.punishments.Ban;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.core.punishments.RankedBan;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import net.zylesh.practice.practicecore.Main;
import net.zylesh.practice.practicecore.events.PlayerRegisterEvent;
import net.zylesh.practice.serialize.PMariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public final class PApi {

    private PApi() {}

    public static Location SPAWN_LOCATION;
    public static World GAMES_WORLD;

    public static final Map<String, Ladder> LADDERS = new HashMap<>();
    public static final Map<String, Ladder> LADDERS_BY_DISPLAYNAME = new HashMap<>();
    public static final Map<String, PArena> ARENAS = new HashMap<>();
    public static final Map<String, PArena> ARENAS_BY_DISPLAYNAME = new HashMap<>();

    public static final Map<Integer, PKillEffect> KILL_EFFECTS_BY_SLOT = new HashMap<>();

    public static void registerPlayer(UUID player) {
        DystellarCore.getAsyncManager().submit(() -> {
            PUser playerUser = PMariaDB.loadPlayerFromDatabase(player, true);
            if (playerUser == null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.INSTANCE, () -> Bukkit.getPlayer(player).kickPlayer(ChatColor.RED + "Could not fetch data, kicked out for security."), 20L);
                return;
            }
            PUser.getUsers().put(player, playerUser);
            boolean isChanged = false;
            for (String s : PApi.LADDERS_BY_DISPLAYNAME.keySet()) {
                if (!playerUser.elo.containsKey(LADDERS_BY_DISPLAYNAME.get(s))) {
                    playerUser.elo.put(LADDERS_BY_DISPLAYNAME.get(s), 1000);
                    isChanged = true;
                }
            }
            if (isChanged)
                PMariaDB.savePlayerToDatabase(playerUser);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DystellarCore.getInstance(), () -> Bukkit.getPluginManager().callEvent(new PlayerRegisterEvent(playerUser)));
        });
    }

    public static void unregisterPlayer(UUID player) {
        DystellarCore.getAsyncManager().submit(() -> PMariaDB.savePlayerToDatabase(PUser.getUsers().remove(player)));
    }

    public static void initializeAll() {
        if (Main.INSTANCE.getConfig().contains("spawnlobby")) {
            SPAWN_LOCATION = LocationSerialization.stringToLocation(Main.INSTANCE.getConfig().getString("spawnlobby"));
        }
        GAMES_WORLD = Bukkit.getWorld(Main.INSTANCE.getConfig().getString("games-world"));
        PKillEffect.initialize();
    }

    public void onBanPunishment(Punishment punishment, UUID uuid) {
        if (!(punishment instanceof Ban) && !(punishment instanceof RankedBan)) return;

    }

    public static int getParsedNumber(String s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (String.valueOf(s.charAt(i)).matches("[0-9]")) {
                builder.append(s.charAt(i));
            }
        }
        return Integer.parseInt(builder.toString());
    }
}

