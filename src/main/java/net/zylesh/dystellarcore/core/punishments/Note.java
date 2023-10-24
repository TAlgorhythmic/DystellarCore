package net.zylesh.dystellarcore.core.punishments;

import net.zylesh.dystellarcore.core.User;

public class Note extends Punishment {

    private String message;

    public Note(String reason, String message) {
        super(null, reason);
        this.message = message;
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
        return this.message;
    }

    @Override
    public int getPriorityScale() {
        return 5;
    }
}
