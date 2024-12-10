package net.zylesh.dystellarcore;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.zylesh.dystellarcore.arenasapi.AbstractArena;
import net.zylesh.dystellarcore.commands.*;
import net.zylesh.dystellarcore.config.ConfValues;
import net.zylesh.dystellarcore.core.*;
import net.zylesh.dystellarcore.core.PacketListener;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.InboxSender;
import net.zylesh.dystellarcore.core.inbox.senders.CoinsReward;
import net.zylesh.dystellarcore.core.inbox.senders.EloGainNotifier;
import net.zylesh.dystellarcore.core.inbox.senders.Message;
import net.zylesh.dystellarcore.core.inbox.senders.prewards.PKillEffectReward;
import net.zylesh.dystellarcore.core.punishments.Punishment;
import net.zylesh.dystellarcore.listeners.GeneralListeners;
import net.zylesh.dystellarcore.listeners.ResourceListener;
import net.zylesh.dystellarcore.listeners.Scoreboards;
import net.zylesh.dystellarcore.listeners.SpawnMechanics;
import net.zylesh.dystellarcore.serialization.*;
import net.zylesh.dystellarcore.services.Services;
import net.zylesh.dystellarcore.services.messaging.Types;
import net.zylesh.dystellarcore.utils.Hooks;
import net.zylesh.practice.PKillEffect;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.zylesh.dystellarcore.commands.UnpunishCommand.createInventory;
import static net.zylesh.dystellarcore.commands.UnpunishCommand.invs;

public final class DystellarCore extends JavaPlugin implements PluginMessageListener, Listener {

    private static DystellarCore INSTANCE;

    public static DystellarCore getInstance() {
        return INSTANCE;
    }

    public static final String CHANNEL = "dyst:ellar";

    public final File conf = new File(getDataFolder(), "config.yml");
    public final File si = new File(getDataFolder(), "spawnitems.yml");
    public final File am = new File(getDataFolder(), "automated-messages.txt");
    public final File m = new File(getDataFolder(), "lang-en.yml");

    private final YamlConfiguration lang = YamlConfiguration.loadConfiguration(m);
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(conf);
    private final YamlConfiguration spawnitems = YamlConfiguration.loadConfiguration(si);
    @Override
    public void onEnable() {
        INSTANCE = this;
        Hooks.registerHooks();
        loadConfig();
        initialize();

        if (ConfValues.AUTOMATED_MESSAGES_ENABLED)
            Services.startAutomatedMessagesService();

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().registerEvents(this, this);

        // Listeners start
        if (ConfValues.SCOREBOARD_ENABLED) new Scoreboards();
        if (ConfValues.HANDLE_SPAWN_MECHANICS) new SpawnMechanics();
        if (ConfValues.PACK_ENABLED) new ResourceListener();
        new User.UserListener(); new Inbox.SenderListener(); new Punish();
        new PacketListener(); new GeneralListeners();
        // Listeners end

        // Commands start
        if (ConfValues.HANDLE_SPAWN_PROTECTION) new EditmodeCommand();
        new SetSpawnCommand(); new GameModeCommand(); new HealCommand();
        new FlyCommand(); new FreezeCommand(); new BroadcastCommand();
        new JoinCommand(); new MSGCommand(); new ReplyCommand();
        new BanCommand(); new BlacklistCommand(); new MuteCommand();
        new NoteCommand(); new PunishmentsCommand(); new NotesCommand();
        new GiveItemCommand(); new ItemMetaCommand(); new PingCommand();
        new ToggleChatCommand(); new TogglePrivateMessagesCommand();
        new ToggleGlobalTabComplete(); new IgnoreCommand(); new IgnoreListCommand();
        new InboxCommand(); new FriendCommand(); new SuffixCommand();
        new WandCommand(); new UnpunishCommand();
        // Commands end

        Services.registerAntiSignExploit(); // TODO: version conditional
        AbstractArena.init();
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, CHANNEL);
    }

    private void loadConfig() {
        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Dystellar] Loading configuration...");
            saveResource("config.yml", false);
            saveResource("spawnitems.yml", false);
            saveResource("lang-en.yml", false);
            config.load(conf);
            spawnitems.load(si);
            lang.load(m);
            String currentVersion = lang.getString("config-version");
            InputStreamReader reader0 = new InputStreamReader(getResource("lang-en.yml"));
            YamlConfiguration rawLang = YamlConfiguration.loadConfiguration(reader0);
            reader0.close();
            String newVersion = rawLang.getString("config-version");
            if (currentVersion.equals("1.0") && newVersion.equals("1.1")) {
                saveResource("lang-en.yml", true);
                lang.load(m);
            }
            Msgs.init();
            if (am.createNewFile()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClassLoader().getResourceAsStream("automated-messages.txt")))); PrintWriter writer = new PrintWriter(am)) {
                    reader.lines().forEach(writer::println);
                }
            }
            Bukkit.getConsoleSender().sendMessage("[Dystellar] Configuration loaded successfully");
            try {
                MariaDB.loadFromConfig();
                Bukkit.getLogger().info("Testing database configuration provided in config.yml");
                MariaDB.dataSourceTestInit();
                initDb();
                Bukkit.getLogger().info("Your configuration looks great!");
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Failed to initialize database, check your configuration. Server will now shutdown.");
                Bukkit.getServer().shutdown();
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getLang() {
        return lang;
    }

    private void initDb() throws IOException, SQLException {
        String setup;
        try (InputStream in = getClassLoader().getResourceAsStream("database.sql")) {
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining());
            String[] queries = setup.split(";");
            for (String query : queries) {
                if (query.isEmpty()) continue;
                try (Connection connection = MariaDB.getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.execute();
                }
            }
        }
        getLogger().info("§2Database setup complete.");
    }

    private void initialize() {
        ConfValues.init(getConfig());
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
            lang.save(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final byte MESSAGE = 0;
    private static final byte PKILL_EFFECT = 1;
    private static final byte COINS_REWARD = 2;
    private static final byte ELO_GAIN_NOTIFIER = 3;

    public void addInboxMessage(UUID target, InboxSender sender, Player issuer /* The player that issued the command, for just in case uuid (player) introduced is not online.*/) {
        if (User.getUsers().containsKey(target)) {
            User.get(target).getInbox().addSender(sender);
        } else sendInbox(sender, issuer, target);
    }

    private void sendInbox(InboxSender sender, Player player, UUID target) {
        List<Object> obj = new ArrayList<>();
        Integer id = sender.getId();
        String submission = sender.getSubmissionDate().format(DateTimeFormatter.ISO_DATE_TIME);
        if (sender instanceof PKillEffectReward) {
            PKillEffectReward reward = (PKillEffectReward) sender;
            String effect = reward.getKillEffect().name();
            String title = reward.getTitle();
            String msg = reward.getSerializedMessage();
            String from = reward.getFrom();
            Boolean claimed = reward.isClaimed();
            Boolean deleted = reward.isDeleted();
            Collections.addAll(obj, target.toString(), PKILL_EFFECT, id, submission, effect, title, msg, from, claimed, deleted);
        } else if (sender instanceof CoinsReward) {
            CoinsReward reward = (CoinsReward) sender;
            Integer coins = reward.getCoins();
            String title = reward.getTitle();
            String msg = reward.getSerializedMessage();
            String from = reward.getFrom();
            Boolean claimed = reward.isClaimed();
            Boolean deleted = reward.isDeleted();
            Collections.addAll(obj, target.toString(), COINS_REWARD, id, submission, coins, title, msg, from, claimed, deleted);
        } else if (sender instanceof EloGainNotifier) {
            EloGainNotifier reward = (EloGainNotifier) sender;
            Integer elo = reward.getElo();
            byte compatibility = reward.getCompatibilityType();
            String ladder = reward.getLadder();
            String msg = reward.getSerializedMessage();
            String from = reward.getFrom();
            Boolean claimed = reward.isClaimed();
            Boolean deleted = reward.isDeleted();
            if (compatibility == EloGainNotifier.PRACTICE) Collections.addAll(obj, target.toString(), ELO_GAIN_NOTIFIER, id, submission, elo, compatibility, ladder, msg, from, claimed, deleted);
            else if (compatibility == EloGainNotifier.SKYWARS) Collections.addAll(obj, target.toString(), ELO_GAIN_NOTIFIER, id, submission, elo, compatibility, msg, from, claimed, deleted);
        } else if (sender instanceof Message) {
            Message reward = (Message) sender;
            String msg = reward.getSerializedMessage();
            String from = reward.getFrom();
            Boolean deleted = reward.isDeleted();
            Collections.addAll(obj, target.toString(), MESSAGE, id, submission, msg, from, deleted);
        }
        sendPluginMessage(player, INBOX_UPDATE, obj.toArray(new Object[0]));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
        awaitingPlayers.add(p.getUniqueId());
        Bukkit.getScheduler().runTaskLater(this, () -> sendPluginMessage(p, REGISTER), 15L);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (awaitingPlayers.contains(event.getPlayer().getUniqueId())) {
                p.kickPlayer(ChatColor.RED + "You are not allowed to join this server. (Contact us if you think this is an error)");
                awaitingPlayers.remove(event.getPlayer().getUniqueId());
            }
        }, 35L);
    }

    private final Set<UUID> awaitingPlayers = new HashSet<>();

    @Override
    public void onPluginMessageReceived(String s, @NotNull Player p, byte[] bytes) {
        if (!s.equalsIgnoreCase(CHANNEL)) return;
        Types.handle(p, bytes);
    }

    public final Map<UUID, UUID> requests = new HashMap<>();

    private final Map<String, Map.Entry<Runnable, Runnable>> runnables = new ConcurrentHashMap<>();

    public void executeIfOnline(Player p, String s, Runnable ifTrue, Runnable ifFalse) {
        runnables.put(s, new AbstractMap.SimpleImmutableEntry<>(ifTrue, ifFalse));
        sendPluginMessage(p, DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK, s);
    }
}
