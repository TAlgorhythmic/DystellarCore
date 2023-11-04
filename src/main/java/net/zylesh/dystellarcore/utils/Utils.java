package net.zylesh.dystellarcore.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class Utils {

    public static String getTimeFormat(LocalDateTime expirationDate) {
        if (expirationDate == null) return "Never";
        LocalDateTime now = LocalDateTime.now();
        long between = Duration.between(now, expirationDate).toDays();
        long betweenHours = Duration.between(now, expirationDate).toHours() - (between * 24);
        long betweenMinutes = Duration.between(now, expirationDate).toMinutes() - (betweenHours * 60);
        return (between > 0 ? between + " days, " : "") + (betweenHours > 0 ? betweenHours + " hours and " : "") + (betweenMinutes > 0 ? betweenMinutes + " minutes." : "");
    }
}
