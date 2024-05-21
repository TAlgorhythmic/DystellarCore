package net.zylesh.dystellarcore;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.server.v1_7_R4.*;
import net.zylesh.dystellarcore.arenasapi.AbstractArena;
import net.zylesh.dystellarcore.commands.*;
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
import net.zylesh.dystellarcore.listeners.PluginMessageScheduler;
import net.zylesh.dystellarcore.listeners.Scoreboards;
import net.zylesh.dystellarcore.listeners.SpawnMechanics;
import net.zylesh.dystellarcore.serialization.*;
import net.zylesh.dystellarcore.utils.Validate;
import net.zylesh.practice.PKillEffect;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.conversations.ConversationTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.zylesh.dystellarcore.commands.UnpunishCommand.createInventory;
import static net.zylesh.dystellarcore.commands.UnpunishCommand.invs;

public final class DystellarCore extends JavaPlugin implements PluginMessageListener, Listener {

    private static DystellarCore INSTANCE;

    private static final ScheduledExecutorService asyncManager = Executors.newScheduledThreadPool(2);

    public static ScheduledExecutorService getAsyncManager() {
        return asyncManager;
    }

    public static DystellarCore getInstance() {
        return INSTANCE;
    }

    private static final String channel = "dyst:ellar";

    private final File conf = new File(getDataFolder(), "config.yml");
    private final YamlConfiguration config = YamlConfiguration.loadConfiguration(conf);
    private final File si = new File(getDataFolder(), "spawnitems.yml");
    private final YamlConfiguration spawnitems = YamlConfiguration.loadConfiguration(si);
    private final File am = new File(getDataFolder(), "automated-messages.txt");
    private final File m = new File(getDataFolder(), "lang-en.yml");
    private final YamlConfiguration lang = YamlConfiguration.loadConfiguration(m);

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
    public static boolean ALLOW_SIGNS;
    public static boolean AUTOMATED_MESSAGES_ENABLED = true;
    public static int AUTOMATED_MESSAGES_RATE = 360;
    public static final List<String> AUTOMATED_MESSAGES = new ArrayList<>();
    private static final AtomicInteger i = new AtomicInteger();
    public static boolean PREVENT_WEATHER = true;
    public static boolean PACK_ENABLED = false;
    public static String PACK_LINK;
    public static boolean DEBUG_MODE = false;

    public static final ItemStack NULL_GLASS = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
    static {
        ItemMeta meta = NULL_GLASS.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + " ");
        NULL_GLASS.setItemMeta(meta);
    }

    @Override
    public void onEnable() {
        try {
            if (ConversationTracker.checkPoint() != 1061574390) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "Server Shutting down :D");
                Bukkit.getServer().shutdown();
            }
        } catch (Exception e) {
            Bukkit.getServer().shutdown();
        }
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
        if (AUTOMATED_MESSAGES_ENABLED && !AUTOMATED_MESSAGES.isEmpty()) {
            asyncManager.scheduleAtFixedRate(() -> {
                PacketPlayOutChat chat = new PacketPlayOutChat(ChatSerializer.a(AUTOMATED_MESSAGES.get(i.get())));
                synchronized (Bukkit.getOnlinePlayers()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendPacket(chat);
                    }
                }
                i.incrementAndGet();
                if (i.get() >= AUTOMATED_MESSAGES.size()) i.set(0);
            }, AUTOMATED_MESSAGES_RATE, AUTOMATED_MESSAGES_RATE, TimeUnit.SECONDS);
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, channel);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, channel, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().registerEvents(this, this);
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
        new Inbox.SenderListener();
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
        new TogglePrivateMessagesCommand();
        new ToggleGlobalTabComplete();
        new PacketListener();
        new PluginMessageScheduler();
        new IgnoreCommand();
        new IgnoreListCommand();
        new InboxCommand();
        new GeneralListeners();
        new FriendCommand();
        new SuffixCommand();
        new WandCommand();
        new UnpunishCommand();
        // Some exploits fix.
        PacketListener.registerPacketHandler(new IPacketListener() {
            @Override
            public void onPacketReceive(Packet packet, Player player, AtomicBoolean cancel) {
                if (packet instanceof PacketPlayInTabComplete) {
                    if (player.hasPermission("dystellar.bypassall")) return;
                    PacketPlayInTabComplete tabComplete = (PacketPlayInTabComplete) packet;
                    String s = tabComplete.c();
                    if (s == null || s.length() < 3 || s.matches("[a-zA-Z\\-_]*:")) cancel.set(true);
                } else if (packet instanceof PacketPlayInUpdateSign) {
                    PacketPlayInUpdateSign update = (PacketPlayInUpdateSign) packet;
                    for (String s : update.f()) {
                        if (!Validate.validateSign(s)) {
                            cancel.set(true);
                            return;
                        }

                    }
                }
            }

            @Override
            public void onPacketSend(Packet packet, Player player, AtomicBoolean cancel) {

            }
        });
        AbstractArena.init();
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, channel);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, channel);
    }

    private void loadConfig() {
        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Dystellar] Loading configuration...");
            if (!conf.exists()) saveResource("config.yml", true);
            if (!si.exists()) saveResource("spawnitems.yml", true);
            if (!m.exists()) saveResource("lang-en.yml", true);
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
        ALLOW_SIGNS = getConfig().getBoolean("block-signs-crafting");
        AUTOMATED_MESSAGES_ENABLED = getConfig().getBoolean("automated-messages-enabled");
        AUTOMATED_MESSAGES_RATE = getConfig().getInt("automated-messages-rate");
        PREVENT_WEATHER = getConfig().getBoolean("prevent-weather-changing");
        DEBUG_MODE = getConfig().getBoolean("debug-mode");
        try (BufferedReader reader = new BufferedReader(new FileReader(am))) {
            reader.lines().forEach(s -> {
                if (s.startsWith("-=")) {
                    AUTOMATED_MESSAGES.add(s.substring(2));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        PACK_ENABLED = getConfig().getBoolean("send-texturepack");
        if (PACK_ENABLED) {
            PACK_LINK = getConfig().getString("texturepack-link");

            ItemMeta confirmMeta = CONFIRM.getItemMeta();
            confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
            CONFIRM.setItemMeta(confirmMeta);

            ItemMeta denyMeta = DENY.getItemMeta();
            denyMeta.setDisplayName(ChatColor.RED + "Deny");
            List<String> loreDeny = List.of(ChatColor.WHITE + "If you click this option you will get kicked.");
            denyMeta.setLore(loreDeny);
            DENY.setItemMeta(denyMeta);

            ItemMeta infoMeta = INFO.getItemMeta();
            infoMeta.setDisplayName(ChatColor.DARK_AQUA + "Info:");
            List<String> loreInfo = List.of(
                    ChatColor.WHITE + "This server uses a custom resource pack",
                    ChatColor.WHITE + "to enhance your game experience. Click",
                    ChatColor.WHITE + "\"Confirm\" to download and apply."
            );
            infoMeta.setLore(loreInfo);
            INFO.setItemMeta(infoMeta);

            packConfirmation = Bukkit.createInventory(null, 9, ChatColor.DARK_AQUA + "Resource Pack Confirmation");
            packConfirmation.setItem(2, CONFIRM);
            packConfirmation.setItem(4, INFO);
            packConfirmation.setItem(6, DENY);
        }
        Suffix.initialize();
    }

    private static final ItemStack CONFIRM = new ItemStack(Material.WOOL, 1, (short) 5);
    private static final ItemStack INFO = new ItemStack(Material.WOOL);
    private static final ItemStack DENY = new ItemStack(Material.WOOL, 1, (short) 14);

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
        awaitingPlayers.add(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskLater(this, () -> sendPluginMessage(event.getPlayer(), REGISTER), 15L);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (awaitingPlayers.contains(event.getPlayer().getUniqueId())) {
                event.getPlayer().kickPlayer(ChatColor.RED + "You are not allowed to join this server. (Contact us if you think this is an error)");
                awaitingPlayers.remove(event.getPlayer().getUniqueId());
            }
        }, 35L);
    }

    private final Set<UUID> awaitingPlayers = new HashSet<>();

    @Override
    public void onPluginMessageReceived(String s, Player p, byte[] bytes) {
        if (!s.equalsIgnoreCase(channel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        byte id = in.readByte();
        switch (id) {
            case DEMAND_PUNISHMENTS_DATA: {
                String string = in.readUTF();
                Player player = Bukkit.getPlayer(string);
                if (player == null) return;
                User user = User.get(player);
                sendPluginMessage(player, PUNISHMENTS_DATA_RESPONSE, Punishments.serializePunishments(user.getPunishments()));
                break;
            }
            case PUNISHMENTS_DATA_RESPONSE: {
                String string = in.readUTF();
                Player player = Bukkit.getPlayer(string);
                if (player == null) return;
                UUID target = UUID.fromString(in.readUTF());
                Set<Punishment> punishments = Punishments.deserializePunishments(in.readUTF(), new HashSet<>());
                invs.put(p.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(target, new Punishment[27]));
                Inventory inv = createInventory(p, punishments);
                p.openInventory(inv);
                break;
            }
            case REGISTER_RECEIVED: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                awaitingPlayers.remove(player.getUniqueId());
            } break;
            case INBOX_UPDATE: {
                UUID uuid = UUID.fromString(in.readUTF());
                if (!User.getUsers().containsKey(uuid)) return;
                User user = User.get(uuid);
                byte type = in.readByte();
                switch (type) {
                    case PKILL_EFFECT: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        PKillEffect effect = PKillEffect.valueOf(in.readUTF());
                        String title = in.readUTF();
                        String[] message = in.readUTF().split(":;");
                        String from = in.readUTF();
                        boolean claimed = in.readBoolean();
                        boolean deleted = in.readBoolean();
                        PKillEffectReward reward = new PKillEffectReward(user.getInbox(), iid, from, message, submission, deleted, title, claimed, effect);
                        reward.initializeIcons();
                        user.getInbox().addSender(reward);
                        break;
                    }
                    case COINS_REWARD: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        int coins = in.readInt();
                        String title = in.readUTF();
                        String[] message = in.readUTF().split(":;");
                        String from = in.readUTF();
                        boolean claimed = in.readBoolean();
                        boolean deleted = in.readBoolean();
                        CoinsReward reward = new CoinsReward(user.getInbox(), iid, from, message, submission, deleted, title, claimed, coins);
                        reward.initializeIcons();
                        user.getInbox().addSender(reward);
                        break;
                    }
                    case ELO_GAIN_NOTIFIER: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        int elo = in.readInt();
                        byte compatibility = in.readByte();
                        if (compatibility == EloGainNotifier.PRACTICE) {
                            String ladder = in.readUTF();
                            String[] message = in.readUTF().split(":;");
                            String from = in.readUTF();
                            boolean claimed = in.readBoolean();
                            boolean deleted = in.readBoolean();
                            EloGainNotifier reward = new EloGainNotifier(user.getInbox(), iid, elo, compatibility, ladder, from, message, submission, deleted, claimed);
                            reward.initializeIcons();
                            user.getInbox().addSender(reward);
                        } else if (compatibility == EloGainNotifier.SKYWARS) {
                            String[] message = in.readUTF().split(":;");
                            String from = in.readUTF();
                            boolean claimed = in.readBoolean();
                            boolean deleted = in.readBoolean();
                            EloGainNotifier reward = new EloGainNotifier(user.getInbox(), iid, elo, compatibility, null, from, message, submission, deleted, claimed);
                            reward.initializeIcons();
                            user.getInbox().addSender(reward);
                        }
                        break;
                    }
                    case MESSAGE: {
                        int iid = in.readInt();
                        LocalDateTime submission = LocalDateTime.parse(in.readUTF(), DateTimeFormatter.ISO_DATE_TIME);
                        String[] message = in.readUTF().split(":;");
                        String from = in.readUTF();
                        boolean deleted = in.readBoolean();
                        Message reward = new Message(user.getInbox(), iid, from, message, submission, deleted);
                        reward.initializeIcons();
                        user.getInbox().addSender(reward);
                        break;
                    }
                }
                break;
            }
            case INBOX_MANAGER_UPDATE: {
                InboxCommand.g().init();
                break;
            }
            case FRIEND_ADD_REQUEST_APPROVE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "Friend request sent!");
                } else {
                    Bukkit.getLogger().warning("Friend request approve operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST_DENY: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.remove(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "This player is not online.");
                } else {
                    Bukkit.getLogger().warning("Friend request deny operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST_DISABLED: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                if (FriendCommand.requestsCache.remove(player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "This player is not accepting friend requests.");
                } else {
                    Bukkit.getLogger().warning("Friend request deny operation for " + player.getName() + " received, but this player didn't send any friend request. Ignoring packet...");
                }
                break;
            }
            case FRIEND_ADD_REQUEST: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                String name = in.readUTF();
                requests.put(player.getUniqueId(), uuid);
                PacketPlayOutChat chat = new PacketPlayOutChat(ChatSerializer.a("[\"\",{\"text\":\"You've received a friend request from \",\"color\":\"dark_aqua\"},{\"text\":\"" + name + "\",\"color\":\"light_purple\"},{\"text\":\"!\",\"color\":\"dark_aqua\"},{\"text\":\" \",\"bold\":true,\"color\":\"green\"},{\"text\":\"[Accept]\",\"bold\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/f accept\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§aClick to accept!\"}},{\"text\":\" \",\"bold\":true,\"color\":\"red\"},{\"text\":\"[Reject]\",\"bold\":true,\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/f reject\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§cClick to reject!\"}}]"));
                player.sendPacket(chat);
                Bukkit.getScheduler().runTaskLater(this, () -> requests.remove(player.getUniqueId()), 800L);
                break;
            }
            case DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User u = User.get(player);
                if (u == null) return;
                boolean response;
                response = u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] == Consts.BYTE_TRUE;
                sendPluginMessage(player, DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS_RESPONSE, response);
            }
            case DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK_RESPONSE: {
                String pe = in.readUTF();
                if (runnables.containsKey(pe)) {
                    if (in.readBoolean())
                        runnables.get(pe).getKey().run();
                    else
                        runnables.get(pe).getValue().run();
                }
                break;
            }
            case REMOVE_FRIEND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                User u = User.get(player);
                if (u == null) {
                    Bukkit.getLogger().warning(player.getName() + " is supposed to delete a player from its friends list as stated by the packet received, but he is not online...");
                    return;
                }
                u.friends.remove(uuid);
                asyncManager.submit(() -> {
                    String name = MariaDB.loadName(uuid.toString());
                    if (name != null) {
                        player.sendMessage(ChatColor.RED + name + " has removed been removed from your friends list. (He removed you)");
                    }
                });
                break;
            }
            case DEMAND_FIND_PLAYER_RESPONSE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                String pla = in.readUTF();
                String srv = in.readUTF();
                if (srv.equals("null")) {
                    player.sendMessage(ChatColor.DARK_AQUA + "This player is joining the server right now!" + ChatColor.GRAY + " (Login screen)");
                } else {
                    player.sendMessage(ChatColor.DARK_AQUA + pla + ChatColor.WHITE + " is currently playing at " + ChatColor.YELLOW + srv + ChatColor.WHITE + ".");
                }
                break;
            }
            case DEMAND_FIND_PLAYER_NOT_ONLINE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                player.sendMessage(ChatColor.RED + "This player is not online.");
                break;
            }
            case FRIEND_ADD_REQUEST_ACCEPT: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                FriendCommand.requestAccepted(player, uuid, unsafe);
                break;
            }
            case FRIEND_ADD_REQUEST_REJECT: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                FriendCommand.requestRejected(player, unsafe);
                break;
            }
            case INBOX_SEND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User user = User.get(player);
                InboxSender sender = InboxSerialization.stringToSender(in.readUTF(), user.getInbox());
                user.getInbox().addSender(sender);
                break;
            }
            case SHOULD_SEND_PACK_RESPONSE: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                User user = User.get(player);
                if (user.extraOptions[Consts.EXTRA_OPTION_RESOURCEPACK_PROMPT_POS] == Consts.BYTE_TRUE) {
                    player.openInventory(packConfirmation);
                    prompts.add(player);
                } else {
                    sendResourcePack(player);
                }
                break;
            }
            case PUNISHMENT_ADD_CLIENTBOUND: {
                String unsafe = in.readUTF();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) {
                    getLogger().warning("Received a packet but the player who's supposed to affect is not online.");
                    return;
                }
                String serialized = in.readUTF();
                Punishment punishment = Punishments.deserialize(serialized);
                User user = User.get(player);
                user.punish(punishment);
                break;
            }
            case REMOVE_PUNISHMENT_BY_ID: {
                String unsafe = in.readUTF();
                int pId = in.readInt();
                Player player = Bukkit.getPlayer(unsafe);
                if (player == null || !player.isOnline()) return;
                User user = User.get(player);
                Punishment punishmentToRemove = null;
                for (Punishment pun : user.getPunishments()) {
                    if (pun.hashCode() == pId) {
                        punishmentToRemove = pun;
                        break;
                    }
                }

                if (punishmentToRemove == null || !user.getPunishments().remove(punishmentToRemove)) return;
                player.sendMessage(ChatColor.GREEN + "The punishment with ID " + pId + " was removed from your punishments list!");
                String[] details = new String[] {
                        ChatColor.DARK_GREEN + "Punishment details:",
                        "===============================",
                        ChatColor.DARK_AQUA + "Type" + ChatColor.WHITE + ": " + ChatColor.GRAY + p.getClass().getSimpleName(),
                        ChatColor.DARK_AQUA + "Creation Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + punishmentToRemove.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME),
                        ChatColor.DARK_AQUA + "Expiration Date" + ChatColor.WHITE + ": " + ChatColor.GRAY + (punishmentToRemove.getExpirationDate() == null ? "Never" : punishmentToRemove.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME)),
                        ChatColor.DARK_AQUA + "Reason" + ChatColor.WHITE + ": " + ChatColor.GRAY + punishmentToRemove.getReason(),
                        "==============================="
                };
                player.sendMessage(details);
                break;
            }
        }
    }

    private void sendResourcePack(Player p) {
        p.setResourcePack(PACK_LINK);
    }

    private static final Set<Player> prompts = new HashSet<>();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory().equals(packConfirmation)) {
            event.setCancelled(true);
            ItemStack i = event.getCurrentItem();
            if (i == null || i.getType().equals(Material.AIR)) return;
            Player p = (Player) event.getWhoClicked();
            if (i.equals(CONFIRM)) {
                prompts.remove(p);
                p.closeInventory();
                User user = User.get(p);
                user.extraOptions[Consts.EXTRA_OPTION_RESOURCEPACK_PROMPT_POS] = Consts.BYTE_FALSE;
                Bukkit.getScheduler().runTaskLater(this, () -> sendResourcePack(p), 5L);
            } else if (i.equals(DENY)) {
                prompts.remove(p);
                p.kickPlayer(ChatColor.RED + "Resource Pack denied.");
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(packConfirmation)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(packConfirmation) && prompts.contains((Player) event.getPlayer())) {
            event.getPlayer().openInventory(packConfirmation);
        }
    }

    private static Inventory packConfirmation;

    public final Map<UUID, UUID> requests = new HashMap<>();

    private final Map<String, Map.Entry<Runnable, Runnable>> runnables = new ConcurrentHashMap<>();

    public void executeIfOnline(Player p, String s, Runnable ifTrue, Runnable ifFalse) {
        runnables.put(s, new AbstractMap.SimpleImmutableEntry<>(ifTrue, ifFalse));
        sendPluginMessage(p, DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK, s);
    }

    public void sendPluginMessage(Player player, byte typeId, Object...extraData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(typeId); // Subchannel
        if (extraData != null) {
            for (Object o : extraData) {
                if (o instanceof String) out.writeUTF((String) o);
                else if (o instanceof Byte) out.writeByte((byte) o);
                else if (o instanceof Integer) out.writeInt((int) o);
                else if (o instanceof Float) out.writeFloat((float) o);
                else if (o instanceof Double) out.writeDouble((double) o);
                else if (o instanceof Boolean) out.writeBoolean((boolean) o);
                else if (o instanceof Long) out.writeLong((long) o);
                else if (o instanceof Character) out.writeChar((char) o);
                else if (o instanceof Short) out.writeShort((short) o);
            }
        }
        player.sendPluginMessage(this, channel, out.toByteArray());
    }

    private static final byte REGISTER = 0;
    private static final byte REGISTER_RECEIVED = 1;
    private static final byte INBOX_UPDATE = 2;
    private static final byte INBOX_MANAGER_UPDATE = 3;
    public static final byte GLOBAL_TAB_REGISTER = 4;
    public static final byte GLOBAL_TAB_UNREGISTER = 5;
    public static final byte FRIEND_ADD_REQUEST = 6;
    public static final byte FRIEND_ADD_REQUEST_APPROVE = 7;
    public static final byte FRIEND_ADD_REQUEST_DENY = 8;
    public static final byte FRIEND_ADD_REQUEST_DISABLED = 9;
    public static final byte FRIEND_ADD_REQUEST_ACCEPT = 10;
    public static final byte FRIEND_ADD_REQUEST_REJECT = 11;
    private static final byte DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS = 12;
    private static final byte DEMAND_IS_PLAYER_ACCEPTING_FRIEND_REQUESTS_RESPONSE = 13;
    public static final byte DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK = 14;
    public static final byte DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK_RESPONSE = 15;
    public static final byte REMOVE_FRIEND = 16;
    public static final byte DEMAND_FIND_PLAYER = 17;
    public static final byte DEMAND_FIND_PLAYER_RESPONSE = 18;
    public static final byte DEMAND_FIND_PLAYER_NOT_ONLINE = 19;
    public static final byte INBOX_SEND = 20;
    public static final byte SHOULD_SEND_PACK = 21;
    public static final byte SHOULD_SEND_PACK_RESPONSE = 22;
    public static final byte DEMAND_PUNISHMENTS_DATA = 23;
    public static final byte PUNISHMENTS_DATA_RESPONSE = 24;
    public static final byte REMOVE_PUNISHMENT_BY_ID = 25;
    public static final byte PUNISHMENT_ADD_PROXYBOUND = 26;
    public static final byte PUNISHMENT_ADD_CLIENTBOUND = 27;
}
