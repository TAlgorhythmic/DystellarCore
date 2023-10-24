package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class Blacklist extends Punishment {

    public Blacklist(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }

    @Override
    public void onPunishment(User user) {
        if (user == null) return;
        Player p = Bukkit.getPlayer(user.getUUID());
        if (p != null) {
            p.kickPlayer(getMessage());
        }
    }

    @Override
    public boolean allowChat() {
        return false;
    }

    @Override
    public boolean allowRanked() {
        return false;
    }

    @Override
    public boolean allowUnranked() {
        return false;
    }

    @Override
    public boolean allowJoinMinigames() {
        return false;
    }

    @Override
    public String getMessage() {
        return DystellarCore.BLACKLIST_MESSAGE;
    }

    @Override
    public int getPriorityScale() {
        return 0;
    }
}
