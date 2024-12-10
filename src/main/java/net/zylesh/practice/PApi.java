package net.zylesh.practice;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import net.zylesh.practice.serialize.PMariaDB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import gg.zylesh.practice.practicecore.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PApi {

    private PApi() {}

    public static Location SPAWN_LOCATION;
    public static World GAMES_WORLD;

    public static final Map<String, Ladder> LADDERS = new HashMap<>();
    public static final Map<String, Ladder> LADDERS_BY_DISPLAYNAME = new HashMap<>();

    public static final Map<Integer, PKillEffect> KILL_EFFECTS_BY_SLOT = new HashMap<>();

    public static boolean registerPlayer(UUID player) {
        PUser playerUser = PMariaDB.loadPlayerFromDatabase(player, true);
        if (playerUser == null) {
            return false;
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
        return true;
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

