package net.zylesh.dystellarcore.core.punishments;

import java.time.LocalDateTime;

public class Ban extends Punishment {

    public Ban(LocalDateTime expirationDate, String reason) {
        super(expirationDate, reason);
    }
}
