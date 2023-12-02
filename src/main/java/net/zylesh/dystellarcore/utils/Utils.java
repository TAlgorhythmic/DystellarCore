package net.zylesh.dystellarcore.utils;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.nethandler.server.CBPacketTitle;
import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTitle;
import com.lunarclient.bukkitapi.title.LCTitleBuilder;
import com.lunarclient.bukkitapi.title.TitleType;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

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
}
