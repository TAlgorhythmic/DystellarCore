package net.zylesh.dystellarcore.serialization;

import net.zylesh.dystellarcore.core.punishments.Punishment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Punishments {

    public static String serialize(Punishment p) {
        String classPath = p.getClass().getName();
        return p.allowRanked() + // 0
                ";" +
                p.allowUnranked() + // 1
                ";" +
                p.allowJoinMinigames() + // 2
                ";" +
                p.allowChat() + // 3
                ";" +
                p.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME) + // 4
                ";" +
                p.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME) + // 5
                ";" +
                classPath + // 6
                ";" +
                p.getReason() + // 7
                ";" +
                p.hashCode(); // 8
    }

    @SuppressWarnings("unchecked")
    public static Punishment deserialize(String p) {
        String[] ss = p.split(";");
        try {
            Class<? extends Punishment> clazz = (Class<? extends Punishment>) Class.forName(ss[6]);
            return clazz.getConstructor(LocalDateTime.class, String.class).newInstance(LocalDateTime.parse(ss[5], DateTimeFormatter.ISO_DATE_TIME), ss[7]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
