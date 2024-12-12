package net.zylesh.dystellarcore.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.practice.PUser;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

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
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
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
        p.getActivePotionEffects().forEach(potionEffect -> {
            if (!potionEffect.getType().equals(PotionEffectType.BLINDNESS)) p.removePotionEffect(potionEffect.getType());
        });
    }

    public static void removeArmor(Player p) {
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
    }

    public static String bytesToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        builder.append(bytes.length).append(":;:");
        for (byte b : bytes) {
            builder.append(b).append(";");
        }
        return builder.toString();
    }

    public static byte[] stringToBytes(String s, boolean compatibilityLayer) {
        if (compatibilityLayer) return new byte[50];
        String[] split = s.split(":;:");
        byte[] data = new byte[Integer.parseInt(split[0])];
        String[] split2 = split[1].split(";");
        for (int i = 0; i < split2.length; i++) {
            data[i] = Byte.parseByte(split2[i]);
        }
        return data;
    }

    public static boolean arePlayersInSameGame(PUser user, PUser user1) {
        return user.getLastGame() != null && user.getLastGame().equals(user1.getLastGame()) && !user.getLastGame().isEnded();
    }

	public static void sendPluginMessage(Player player, byte typeId, Object...extraData) {
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
        player.sendPluginMessage(DystellarCore.getInstance(), DystellarCore.CHANNEL, out.toByteArray());
    }
}
