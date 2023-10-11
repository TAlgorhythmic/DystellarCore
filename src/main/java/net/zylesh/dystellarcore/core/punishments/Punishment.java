package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.core.User;

import java.time.LocalDateTime;

public abstract class Punishment {

    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;
    private final String reason;

    public Punishment(LocalDateTime expirationDate, String reason) {
        this.creationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
        this.reason = reason;
    }

    public abstract void onPunishment(User user);

    public abstract boolean allowChat();

    public abstract boolean allowRanked();

    public abstract boolean allowUnranked();

    public abstract boolean allowJoinMinigames();

    public abstract String getMessage();

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
}
