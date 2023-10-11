package net.zylesh.dystellarcore.core.punishments;

import java.time.LocalDateTime;

public class Blacklist extends Punishment {

    public Blacklist(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }
}
