package net.zylesh.dystellarcore.core.inbox.senders;

import net.zylesh.dystellarcore.core.inbox.Claimable;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import org.bukkit.inventory.ItemStack;

public abstract class Reward extends Message implements Claimable {

    protected ItemStack readIcon;
    protected final String title;
    protected boolean isClaimed = false;

    protected Reward(Inbox inbox, String title, String from, String... messageLines) {
        super(inbox, from, messageLines);
        this.title = title;
    }

    @Override
    public abstract void initializeIcons();

    public String getTitle() {
        return title;
    }

    @Override
    public ItemStack getReadIcon() {
        return readIcon;
    }

    @Override
    public abstract void onLeftClick();

    @Override
    public abstract void onRightClick();

    @Override
    public abstract boolean claim();

    @Override
    public boolean isClaimed() {
        return isClaimed;
    }
}
