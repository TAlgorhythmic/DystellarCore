package net.zylesh.dystellarcore;

import net.zylesh.dystellarcore.commands.*;
import net.zylesh.dystellarcore.core.Suffix;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.listeners.Scoreboards;
import net.zylesh.dystellarcore.listeners.SpawnMechanics;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public final class DystellarCore extends JavaPlugin {

    private static DystellarCore INSTANCE;

    private static final ScheduledExecutorService asyncManager = Executors.newScheduledThreadPool(2);

    public static ScheduledExecutorService getAsyncManager() {
        return asyncManager;
    }

    public static DystellarCore getInstance() {
        return INSTANCE;
    }

    private final File conf = new File(getDataFolder(), "config.yml");
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(conf);
    private final File si = new File(getDataFolder(), "spawnitems.yml");
    private final YamlConfiguration spawnitems = YamlConfiguration.loadConfiguration(si);

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
    public static boolean HANDLE_SPAWN_PROTECTION = false;
    public static boolean HANDLE_SPAWN_MECHANICS = false;
    public static String PLAYER_MSG_DISABLED;
    public static String MSG_SEND_FORMAT;
    public static String MSG_RECEIVE_FORMAT;
    public static List<String> WARN_MESSAGE;
    public static String KICK_MESSAGE;
    public static int REFRESH_RATE_SCORE;

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
        initialize();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        if (HANDLE_SPAWN_MECHANICS) new SpawnMechanics();
        if (HANDLE_SPAWN_PROTECTION) new EditmodeCommand();
        if (SCOREBOARD_ENABLED) new Scoreboards();
        new SetSpawnCommand();
        new GameModeCommand();
        new HealCommand();
        new FlyCommand();
        new FreezeCommand();
        new BroadcastCommand();
        new JoinCommand();
        new User.UserListener();
        new MSGCommand();
        new Punish();
        new ReplyCommand();
        new BanCommand();
        new BlacklistCommand();
        new MuteCommand();
        new NoteCommand();
        new PunishmentsCommand();
        new NotesCommand();
        new GiveItemCommand();
        new ItemMetaCommand();
        new PingCommand();
        new ToggleChatCommand();
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
    }

    private void loadConfig() {
        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Dystellar] Loading configuration...");
            if (!conf.exists()) saveResource("config.yml", true);
            if (!si.exists()) saveResource("spawnitems.yml", true);
            config.load(conf);
            spawnitems.load(si);
            MariaDB.loadFromConfig();
            Bukkit.getConsoleSender().sendMessage("[Dystellar] Configuration loaded successfully");
            if (MariaDB.ENABLED) {
                try {
                    Bukkit.getLogger().info("Testing database configuration provided in config.yml");
                    MariaDB.dataSourceTestInit();
                    initDb();
                    Bukkit.getLogger().info("Your configuration looks great!");
                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getLogger().severe("Failed to initialize database, check your configuration. Server will now shutdown.");
                    Bukkit.getServer().shutdown();
                }
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void initDb() throws IOException, SQLException {
        String setup;
        try (InputStream in = getClassLoader().getResourceAsStream("database.sql")) {
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining());
            String[] queries = setup.split(";");
            for (String query : queries) {
                if (query.isEmpty()) continue;
                try (Connection connection = MariaDB.DS.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.execute();
                }
            }
        }
        getLogger().info("§2Database setup complete.");
    }

    private void initialize() {
        BROADCAST_FORMAT = ChatColor.translateAlternateColorCodes('&', getConfig().getString("broadcast-format"));
        SCOREBOARD_TITLE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("scoreboard.title"));
        SCOREBOARD_LINES = new ArrayList<>();
        for (String line : getConfig().getStringList("scoreboard.lines")) {
            SCOREBOARD_LINES.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        REFRESH_RATE_SCORE = getConfig().getInt("scoreboard.refresh-rate");
        FREEZE_MESSAGE = new ArrayList<>();
        for (String line : getConfig().getStringList("freeze-message")) {
            FREEZE_MESSAGE.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        UNFREEZE_MESSAGE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("unfreeze-message"));
        SCOREBOARD_ENABLED = getConfig().getBoolean("scoreboard-enabled");
        VOID_TELEPORT = getConfig().getBoolean("teleport-on-void");
        JOIN_TELEPORT = getConfig().getBoolean("teleport-on-join");
        if (getConfig().contains("spawn-location")) SPAWN_LOCATION = LocationSerialization.stringToLocation(getConfig().getString("spawn-location"));
        ALLOW_BANNED_PLAYERS = getConfig().getBoolean("allow-banned-players");
        List<String> ban_msg = getConfig().getStringList("ban-message");
        StringBuilder builder = new StringBuilder();
        for (String ban : ban_msg) {
            builder.append(ChatColor.translateAlternateColorCodes('&', ban)).append("\n");
        }
        builder.delete(builder.length() - 3, builder.length());
        BAN_MESSAGE = builder.toString();
        List<String> blacklist_msg = getConfig().getStringList("blacklist-message");
        builder = new StringBuilder();
        for (String blacklist : blacklist_msg) {
            builder.append(ChatColor.translateAlternateColorCodes('&', blacklist)).append("\n");
        }
        BLACKLIST_MESSAGE = builder.toString();
        RANKED_BAN_MESSAGE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ranked-ban-message"));
        MUTE_MESSAGE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("mute-message"));
        HANDLE_SPAWN_PROTECTION = getConfig().getBoolean("handle-spawn-protection");
        HANDLE_SPAWN_MECHANICS = getConfig().getBoolean("handle-spawn-mechanics");
        PLAYER_MSG_DISABLED = ChatColor.translateAlternateColorCodes('&', getConfig().getString("player-msg-disabled"));
        MSG_SEND_FORMAT = ChatColor.translateAlternateColorCodes('&', getConfig().getString("msg-send-format"));
        MSG_RECEIVE_FORMAT = ChatColor.translateAlternateColorCodes('&', getConfig().getString("msg-receive-format"));
        WARN_MESSAGE = new ArrayList<>();
        for (String line : getConfig().getStringList("warn-message")) {
            WARN_MESSAGE.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        KICK_MESSAGE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("kick-message"));
        Suffix.initialize();
    }

    @Override
    public YamlConfiguration getConfig() {
        return config;
    }

    public YamlConfiguration getSpawnitems() {
        return spawnitems;
    }

    @Override
    public void saveConfig() {
        try {
            config.save(conf);
            spawnitems.save(si);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
