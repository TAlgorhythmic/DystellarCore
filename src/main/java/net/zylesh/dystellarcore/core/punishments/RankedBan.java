package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;

import java.time.LocalDateTime;

public class RankedBan extends Punishment {

    public RankedBan(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }

    @Override
    public void onPunishment(User user) {
        super.onPunishment(user);
    }

    @Override
    public boolean allowChat() {
        return true;
    }

    @Override
    public boolean allowRanked() {
        return false;
    }

    @Override
    public boolean allowUnranked() {
        return true;
    }

    @Override
    public boolean allowJoinMinigames() {
        return true;
    }

    @Override
    public String getMessage() {
        return DystellarCore.RANKED_BAN_MESSAGE;
    }

    @Override
    public int getPriorityScale() {
        return 4;
    }
}
