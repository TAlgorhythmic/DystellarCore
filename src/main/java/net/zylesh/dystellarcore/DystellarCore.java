package net.zylesh.dystellarcore;

import net.zylesh.dystellarcore.commands.*;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class DystellarCore extends JavaPlugin {

    private static DystellarCore INSTANCE;

    public static DystellarCore getInstance() {
        return INSTANCE;
    }

    private final File conf = new File(getDataFolder(), "config.yml");
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(conf);

    public static boolean SKYWARS_HOOK = false;
    public static boolean PRACTICE_HOOK = false;
    public static String BROADCAST_FORMAT;
    public static String SCOREBOARD_TITLE;
    public static List<String> SCOREBOARD_LINES;
    public static List<String> FREEZE_MESSAGE;
    public static String UNFREEZE_MESSAGE;
    public static boolean SCOREBOARD_ENABLED = false;
    public static boolean VOID_TELEPORT = false;
    public static boolean JOIN_TELEPORT = false;
    public static Location SPAWN_LOCATION;
    public static boolean ALLOW_BANNED_PLAYERS = false;
    public static String BAN_MESSAGE;
    public static String BLACKLIST_MESSAGE;
    public static String RANKED_BAN_MESSAGE;
    public static String MUTE_MESSAGE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equals("SkyWars-Core")) {
                SKYWARS_HOOK = true;
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Dystellar] Hooked into SkyWars plugin.");
                break;
            }
            if (plugin.getName().equals("Practice-Core")) {
                PRACTICE_HOOK = true;
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Dystellar] Hooked into Practice plugin.");
                break;
            }
        }
        loadConfig();
        new SetSpawnCommand();
        new GameModeCommand();
        new HealCommand();
        new FlyCommand();
        new FreezeCommand();

    }

    private void loadConfig() {
        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Dystellar] Loading configuration...");
            if (!conf.exists()) {
                saveResource("config.yml", true);
            }
            config.load(conf);
            Bukkit.getConsoleSender().sendMessage("[Dystellar] Configuration loaded successfully");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        BROADCAST_FORMAT = getConfig().getString("broadcast-format");
        SCOREBOARD_TITLE = getConfig().getString("scoreboard.title");
        SCOREBOARD_LINES = getConfig().getStringList("scoreboard.lines");
        FREEZE_MESSAGE = getConfig().getStringList("freeze-message");
        UNFREEZE_MESSAGE = getConfig().getString("unfreeze-message");
        SCOREBOARD_ENABLED = getConfig().getBoolean("scoreboard-enabled");
        VOID_TELEPORT = getConfig().getBoolean("teleport-on-void");
        JOIN_TELEPORT = getConfig().getBoolean("teleport-on-join");
        SPAWN_LOCATION = LocationSerialization.stringToLocation(getConfig().getString("spawn-location"));
        ALLOW_BANNED_PLAYERS = getConfig().getBoolean("allow-banned-players");
        List<String> ban_msg = getConfig().getStringList("ban-message");
        StringBuilder builder = new StringBuilder();
        for (String ban : ban_msg) {
            builder.append(ban).append("\n");
        }
        builder.delete(builder.length() - 3, builder.length());
        BAN_MESSAGE = builder.toString();
        List<String> blacklist_msg = getConfig().getStringList("blacklist-message");
        builder = new StringBuilder();
        for (String blacklist : blacklist_msg) {
            builder.append(blacklist).append("\n");
        }
        BLACKLIST_MESSAGE = builder.toString();
        RANKED_BAN_MESSAGE = getConfig().getString("ranked-ban-message");
        MUTE_MESSAGE = getConfig().getString("mute-message");
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public void saveConfig() {
        try {
            config.save(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
