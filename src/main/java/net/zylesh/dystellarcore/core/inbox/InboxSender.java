package net.zylesh.dystellarcore.core.inbox;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;

public interface InboxSender extends Comparable<InboxSender> {

    void initializeIcons();

    ItemStack getUnreadIcon();

    void setUnreadIcon(ItemStack itemStack);

    ItemStack getReadIcon();

    void setReadIcon(ItemStack itemStack);

    void onLeftClick();

    void onRightClick();

    LocalDateTime getSubmissionDate();

    boolean isDeleted();

    byte getSerialID();

    String getFrom();

    int getId();
}
