package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.punishments.SenderContainer;
import net.zylesh.dystellarcore.serialization.MariaDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InboxCommand implements CommandExecutor, Listener {

    private static final ItemStack SEND = new ItemStack(Material.WOOL, 1, (short) 5);
    private static final ItemStack DELETE = new ItemStack(Material.WOOL, 1, (short) 14);
    private static final ItemStack BACK = new ItemStack(Material.WOOL, 1, (short) 15);
    static {
        ItemMeta metaSend = SEND.getItemMeta();
        metaSend.setDisplayName(ChatColor.GREEN + "Send");
        SEND.setItemMeta(metaSend);
        ItemMeta metaDelete = DELETE.getItemMeta();
        metaDelete.setDisplayName(ChatColor.RED + "Delete");
        DELETE.setItemMeta(metaDelete);
        ItemMeta metaBack = BACK.getItemMeta();
        metaBack.setDisplayName(ChatColor.RED + "Go Back");
        BACK.setItemMeta(metaBack);
    }

    private static InboxCommand INSTANCE;

    public static InboxCommand g() {
        return INSTANCE;
    }

    private SenderContainer selected = null;

    private boolean creating = false;

    private final Inventory menu = Bukkit.createInventory(null, 9, ChatColor.DARK_AQUA + "Menu");
    private final Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "Inbox Senders");
    private volatile SenderContainer[] containers;

    public InboxCommand() {
        Bukkit.getPluginCommand("inbox").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
        INSTANCE = this;
        inv.setContents(Inbox.LAYOUT);
        for (int i = 0; i < menu.getSize(); i++) {
            switch (i) {
                case 3: menu.setItem(i, SEND); break;
                case 5: menu.setItem(i, DELETE); break;
                case 8: menu.setItem(i, BACK); break;
                default: menu.setItem(i, DystellarCore.NULL_GLASS); break;
            }
        }
        init();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (strings.length < 1 || !commandSender.hasPermission("dystellar.admin.inbox")) {
            User user = User.get(p);
            user.getInbox().open();
            return true;
        }
        if (strings[0].equalsIgnoreCase("manage")) {
            p.openInventory(inv);
        }
        return true;
    }

    @EventHandler
    public void onInv(InventoryClickEvent e) {
        if (!e.getClickedInventory().equals(inv) && !e.getClickedInventory().equals(menu)) return;
        if (selected != null) {
            ((Player) e.getWhoClicked()).sendMessage(ChatColor.RED + "Someone else is performing actions. Please wait.");
            return;
        }
        e.setCancelled(true);
        ItemStack i = e.getCurrentItem();
        if (i == null || i.getType().equals(Material.AIR) || i.equals(DystellarCore.NULL_GLASS)) return;
        selected = containers[e.getSlot() - 9];
        // TODO
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) e.setCancelled(true);
    }

    public void init() {
        DystellarCore.getAsyncManager().submit(() -> {
            containers = MariaDB.loadSenderContainers();
            synchronized (inv) {
                int i = 0;
                for (; i < containers.length; i++) {
                    if (i + 9 >= inv.getSize() - 9) break;
                    inv.setItem(i + 9, containers[i].getIcon());
                }
                if ((i + 9) < (inv.getSize() - 9)) {
                    for (; (i + 9) < (inv.getSize() - 9); i++) {
                        inv.setItem(i + 9, null);
                    }
                }
            }
        });
    }
}
