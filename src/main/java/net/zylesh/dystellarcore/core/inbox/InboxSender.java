package net.zylesh.dystellarcore.core.inbox;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;

public interface InboxSender extends Comparable<InboxSender> {

    void initializeIcons();

    ItemStack getUnreadIcon();

    ItemStack getReadIcon();

    void onLeftClick();

    void onRightClick();

    LocalDateTime getSubmissionDate();

    boolean isDeleted();

    byte getSerialID();

    String getFrom();

    int getId();

    InboxSender clone(Inbox inbox);
}
