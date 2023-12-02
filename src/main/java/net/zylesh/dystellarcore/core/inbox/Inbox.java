package net.zylesh.dystellarcore.core.inbox;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.zylesh.dystellarcore.DystellarCore.NULL_GLASS;

public class Inbox {

    private static final ItemStack[] LAYOUT = new ItemStack[54];
    static {
        for (int i = 0; i < 9; i++) {
            LAYOUT[i] = NULL_GLASS;
        }
        for (int i = 45; i < 54; i++) {
            LAYOUT[i] = NULL_GLASS;
        }
    }

    private final User user;
    private final Inventory inbox;
    protected final TreeSet<InboxSender> senders = new TreeSet<>();

    public Inbox(User user) {
        this.user = user;
        this.inbox = Bukkit.createInventory(Bukkit.getPlayer(user.getUUID()), 54, ChatColor.DARK_AQUA + "Inbox");
        this.inbox.setContents(LAYOUT);
    }

    public User getUser() {
        return user;
    }

    public void addSender(InboxSender inboxSender) {
        senders.add(inboxSender);
        update();
    }

    public void deleteSender(InboxSender inboxSender) {
        if (senders.remove(inboxSender)) update();
    }

    public void update() {
        int position = 9;
        for (InboxSender inboxSender : senders) {
            if (position > 44) return;
            if (inboxSender.isDeleted()) continue;
            inbox.setItem(position, inboxSender instanceof Claimable && ((Claimable) inboxSender).isClaimed() ? inboxSender.getReadIcon() : inboxSender.getUnreadIcon());
            position++;
        }
    }

    public static class SenderListener implements Listener {

        private static final ConcurrentMap<UUID, Inventory> inboxes = new ConcurrentHashMap<>();

        public SenderListener() {
            Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
        }

        public static void registerInbox(User user) {
            inboxes.put(user.getUUID(), user.getInbox().inbox);
        }

        public static void unregisterInbox(UUID user) {
            inboxes.remove(user);
        }

        @EventHandler
        public void onInvClick(InventoryClickEvent event) {
            if (event.getClickedInventory().equals(inboxes.get(event.getWhoClicked().getUniqueId()))) {
                event.setCancelled(true);
                ItemStack i = event.getCurrentItem();
                if (i == null || i.getType().equals(Material.AIR) || i.equals(NULL_GLASS)) return;
                User user = User.get(event.getWhoClicked().getUniqueId());
                if (user == null) return;
                int pos = 9;
                for (InboxSender sender : user.getInbox().senders) {
                    if (pos > 44) break;
                    if (sender.getReadIcon().equals(i) || sender.getReadIcon().equals(i)) {
                        if (event.isLeftClick()) sender.onLeftClick();
                        else if (event.isRightClick()) sender.onRightClick();
                        break;
                    }
                    pos++;
                }
            }
        }

        @EventHandler
        public void onDrag(InventoryDragEvent event) {
            if (event.getInventory().equals(inboxes.get(event.getWhoClicked().getUniqueId()))) event.setCancelled(true);
        }
    }
}
