package net.zylesh.dystellarcore.config;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfValues {

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
}
