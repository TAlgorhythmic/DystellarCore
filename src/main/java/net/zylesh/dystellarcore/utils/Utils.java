package net.zylesh.dystellarcore.utils;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.nethandler.server.CBPacketTitle;
import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTitle;
import com.lunarclient.bukkitapi.title.LCTitleBuilder;
import com.lunarclient.bukkitapi.title.TitleType;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Utils {

    public static String getTimeFormat(LocalDateTime expirationDate) {
        if (expirationDate == null) return "Never";
        LocalDateTime now = LocalDateTime.now();
        long between = Duration.between(now, expirationDate).toDays();
        long betweenHours = Duration.between(now, expirationDate).toHours() - (between * 24);
        long betweenMinutes = Duration.between(now, expirationDate).toMinutes() - (betweenHours * 60);
        return (between > 0 ? between + " days, " : "") + (betweenHours > 0 ? betweenHours + " hours and " : "") + (betweenMinutes > 0 ? betweenMinutes + " minutes." : "");
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (LunarClientAPI.getInstance().isRunningLunarClient(player)) {
            LCPacketTitle titlePacket = LCTitleBuilder.of(title, TitleType.TITLE).fadeInFor(Duration.ofSeconds(fadeIn)).displayFor(Duration.ofSeconds(stay)).fadeOutFor(Duration.ofSeconds(fadeOut)).build();
            LCPacketTitle subtitlePacket = LCTitleBuilder.of(subtitle, TitleType.SUBTITLE).fadeInFor(Duration.ofSeconds(fadeIn)).displayFor(Duration.ofSeconds(stay)).fadeOutFor(Duration.ofSeconds(fadeOut)).build();
            LunarClientAPI.getInstance().sendPacket(player, titlePacket);
            LunarClientAPI.getInstance().sendPacket(player, subtitlePacket);
        } else if (CheatBreakerAPI.getInstance().isRunningCheatBreaker(player)) {
            CBPacketTitle titlePacket = new CBPacketTitle(com.cheatbreaker.api.object.TitleType.TITLE.name().toLowerCase(), title, stay * 1000L, fadeIn * 1000L, fadeOut * 1000L);
            CBPacketTitle subtitlePacket = new CBPacketTitle(com.cheatbreaker.api.object.TitleType.SUBTITLE.name().toLowerCase(), subtitle, stay * 1000L, fadeIn * 1000L, fadeOut * 1000L);
            CheatBreakerAPI.getInstance().sendPacket(player, titlePacket);
            CheatBreakerAPI.getInstance().sendPacket(player, subtitlePacket);
        } else {
            ProtocolInjector.PacketTitle titlePacket = new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.TITLE, fadeIn, stay, fadeOut);
            ProtocolInjector.PacketTitle subtitlePacket = new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.SUBTITLE, fadeIn, stay, fadeOut);
            player.sendPacket(titlePacket);
            player.sendPacket(subtitlePacket);
        }
    }

    public static <T> boolean contains(T[] array, T item) {
        for (T t : array) {
            if (Objects.equals(t, item)) return true;
        }
        return false;
    }

    public static <T> boolean replaceFirstNull(T[] array, T item) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                array[i] = item;
                return true;
            }
        }
        return false;
    }

    public static <T> boolean remove(T[] array, T item) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], item)) {
                array[i] = null;
                return true;
            }
        }
        return false;
    }

    public static <T> boolean nullExists(T[] array) {
        for (T t : array) {
            if (t == null) return true;
        }
        return false;
    }

    /**
     * Expects arrays length to be the same!
     * prioritizes array1, if it has null values then replace them with values in array2
     * @param array1 first array
     * @param array2 second array
     * @param <T> whatever object type
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] mergeArraysCopy(Class<T> clazz, T[] array1, T[] array2) {
        T[] arrayCopy = (T[]) Array.newInstance(clazz, array1.length);
        System.arraycopy(array1, 0, arrayCopy, 0, array1.length);
        for (int i = 0; i < arrayCopy.length; i++) {
            if (arrayCopy[i] == null) arrayCopy[i] = array2[i];
        }
        return arrayCopy;
    }

    public static <T> boolean switchArrayPos(T[] array1, T[] array2, T item) {
        for (int i = 0; i < array1.length; i++) {
            if (Objects.equals(array1[i], item)) {
                array1[i] = null;
                array2[i] = item;
                return true;
            }
        }
        return false;
    }

    public static <T> void returnItemsToArray1(T[] array1, T[] array2) {
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] == null) {
                array1[i] = array2[i];
                array2[i] = null;
            }
        }
    }

    public static void resetEffects(Player p) {
        p.setFireTicks(0);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(12.0f);
        p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
    }

    public static void removeArmor(Player p) {
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    public static byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + (Character.digit(s.charAt(i + 1), 16)));
        }
        return data;
    }
}
