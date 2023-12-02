package net.zylesh.dystellarcore.core.inbox;

public interface Claimable extends InboxSender {

    boolean isClaimed();

    boolean claim();
}
