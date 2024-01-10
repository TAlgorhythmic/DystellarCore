package net.zylesh.dystellarcore;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInTabComplete;
import net.zylesh.dystellarcore.commands.*;
import net.zylesh.dystellarcore.core.IPacketListener;
import net.zylesh.dystellarcore.core.PacketListener;
import net.zylesh.dystellarcore.core.Suffix;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.InboxSender;
import net.zylesh.dystellarcore.core.inbox.senders.CoinsReward;
import net.zylesh.dystellarcore.core.inbox.senders.EloGainNotifier;
import net.zylesh.dystellarcore.core.inbox.senders.Message;
import net.zylesh.dystellarcore.core.inbox.senders.prewards.PKillEffectReward;
import net.zylesh.dystellarcore.listeners.Scoreboards;
import net.zylesh.dystellarcore.listeners.SpawnMechanics;
import net.zylesh.dystellarcore.serialization.LocationSerialization;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.practice.PKillEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class DystellarCore extends JavaPlugin implements PluginMessageListener {

    private static DystellarCore INSTANCE;

    private static final ScheduledExecutorService asyncManager = Executors.newScheduledThreadPool(2);

    public static ScheduledExecutorService getAsyncManager() {
        return asyncManager;
    }

    public static DystellarCore getInstance() {
        return INSTANCE;
    }

    private static final String channel = "dyst:";

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
    public static boolean ALLOW_SIGNS;

    public static final ItemStack NULL_GLASS = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
    static {
        ItemMeta meta = NULL_GLASS.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + " ");
        NULL_GLASS.setItemMeta(meta);
    }

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
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, channel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, channel, this);
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
        new PacketListener();
        new InboxCommand();
        PacketListener.registerPacketHandler(new IPacketListener() {
            @Override
            public void onPacketReceive(Packet packet, Player player, AtomicBoolean cancel) {
                if (packet instanceof PacketPlayInTabComplete) {
                    PacketPlayInTabComplete tabComplete = (PacketPlayInTabComplete) packet;
                    String s = tabComplete.c();
                    if (s == null || s.length() < 3 || s.matches("[a-zA-Z\\-_]*:")) cancel.set(true);

                }
            }

            @Override
            public void onPacketSend(Packet packet, Player player, AtomicBoolean cancel) {

            }
        });
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
            Bukkit.getConsoleSender().sendMessage("[Dystellar] Configuration loaded successfully");
            try {
                MariaDB.loadFromConfig();
                if (MariaDB.ENABLED) {
                    Bukkit.getLogger().info("Testing database configuration provided in config.yml");
                    MariaDB.dataSourceTestInit();
                    initDb();
                    Bukkit.getLogger().info("Your configuration looks great!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().severe("Failed to initialize database, check your configuration. Server will now shutdown.");
                Bukkit.getServer().shutdown();
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

    private static final byte MESSAGE = 0;
    private static final byte PKILL_EFFECT = 1;
    private static final byte COINS_REWARD = 2;
    private static final byte ELO_GAIN_NOTIFIER = 3;

    public void addInboxMessage(UUID target, InboxSender sender, Player issuer /* The player that issued the command, for just in case uuid (player) introduced is not online.*/) {
        if (User.getUsers().containsKey(target)) {
            User.get(target).getInbox().addSender(sender);
            return;
        }
        sendInbox(sender, issuer, target);
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

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if (!s.equals(channel)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        byte id = in.readByte();
        UUID uuid = UUID.fromString(in.readUTF());
        if (!User.getUsers().containsKey(uuid)) return;
        User user = User.get(uuid);
        switch (id) {
            case REGISTER: sendPluginMessage(player, REGISTER_RECEIVED); break;
            case INBOX_UPDATE: {
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
        }
    }

    private void sendPluginMessage(Player player, byte typeId, Object...extraData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(typeId);
        if (extraData != null) {
            for (Object o : extraData) {
                if (o instanceof String) out.writeUTF((String) o);
                else if (o instanceof Byte) out.writeByte((Byte) o);
                else if (o instanceof Integer) out.writeInt((Integer) o);
                else if (o instanceof Float) out.writeFloat((Float) o);
                else if (o instanceof Double) out.writeDouble((Double) o);
                else if (o instanceof Boolean) out.writeBoolean((Boolean) o);
                else if (o instanceof Long) out.writeLong((Long) o);
                else if (o instanceof Character) out.writeChar((Character) o);
                else if (o instanceof Short) out.writeShort((Short) o);
            }
        }
        player.sendPluginMessage(this, channel, out.toByteArray());
    }

    private static final byte REGISTER = 0;
    private static final byte REGISTER_RECEIVED = 1;
    private static final byte INBOX_UPDATE = 2;
    private static final byte INBOX_MANAGER_UPDATE = 3;
}
