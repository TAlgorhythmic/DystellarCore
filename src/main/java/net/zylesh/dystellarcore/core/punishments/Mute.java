package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.core.User;

import java.time.LocalDateTime;

public class Mute extends Punishment {

    public Mute(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }

    @Override
    public void onPunishment(User user) {

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
        return null;
    }


}
