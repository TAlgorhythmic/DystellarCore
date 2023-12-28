package net.zylesh.practice;

import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.practice.practicecore.Main;
import net.zylesh.practice.practicecore.Practice;
import net.zylesh.practice.practicecore.core.GameFFA;
import net.zylesh.practice.practicecore.core.GameVersus;
import net.zylesh.practice.practicecore.events.PlayerKitDeselectEvent;
import net.zylesh.practice.practicecore.listeners.Scoreboards;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.zylesh.practice.practicecore.util.Msg.PARTY_JOIN_BROADCAST;
import static net.zylesh.practice.practicecore.util.Msg.PLAYER_SPECTATE_BROADCAST;

public class PUser implements Comparable<PUser>, Listener {

    protected static final Map<UUID, PUser> users = new ConcurrentHashMap<>();

    public static PUser get(Player p) {
        return users.get(p.getUniqueId());
    }

    public synchronized static PUser get(UUID uuid) {
        return users.get(uuid);
    }

    public static Map<UUID, PUser> getUsers() {
        return users;
    }

    public  ItemStack globalChatActiveItem;
    public ItemStack privateMessagesActiveItem;
    public ItemStack duelRequestsEnabledItem;
    private final Player player;
    private final String name;
    private final UUID uuid;
    private boolean inGame;
    private boolean inParty;
    private PUser forceQueued;
    private Ladder lastQueueLadder;
    private String rank = "Silver";
    private String lastDuelReceived;
    private Player lastDueledPlayer;
    private boolean partyChatActive;
    private int playerVisibility = 0;
    public ItemStack playerVisibilityItem;
    private boolean isInQueue;
    private boolean editMode;
    private boolean displayRank = true;
    private boolean duelRequestsEnabled = true;
    private PGame lastSpec;
    private PParty party;
    private PGame lastGame;
    private boolean isSpectating;
    private boolean isEditing;
    private Inventory settings;
    private final Map<Ladder, ItemStack[]> invsEdited;
    public final Map<Ladder, Integer> elo;
    public int kills = 0;
    public int deaths = 0;
    public PKillEffect killEffect;
    public final EnumSet<PKillEffect> ownedEffects;

    public PUser(UUID playeruuid) {
        this.invsEdited = new HashMap<>();
        this.isSpectating = false;
        this.player = Bukkit.getPlayer(playeruuid);
        this.name = player.getDisplayName();
        this.uuid = playeruuid;
        this.lastDuelReceived = "accept nullImbecilxdkldsj";
        this.elo = new HashMap<>();
        this.killEffect = PKillEffect.NONE;
        this.ownedEffects = EnumSet.of(PKillEffect.NONE);
        this.globalChatActiveItem = new ItemStack(Material.PAPER);
        this.privateMessagesActiveItem = new ItemStack(Material.FLINT);
        this.duelRequestsEnabledItem = new ItemStack(Material.DIAMOND_SWORD);
        this.playerVisibilityItem = new ItemStack(Material.INK_SACK, 1, (short) 8);
        ItemMeta playervisibility = this.playerVisibilityItem.getItemMeta();
        playervisibility.setDisplayName(ChatColor.AQUA + "Player Visibility: " + ChatColor.RED + "disabled");
        ItemMeta duelrequests = this.duelRequestsEnabledItem.getItemMeta();
        duelrequests.setDisplayName(ChatColor.AQUA + "Duel Requests: " + ChatColor.GREEN + "enabled");
        ItemMeta globalchatactive = globalChatActiveItem.getItemMeta();
        ItemMeta privatemessagesactive = privateMessagesActiveItem.getItemMeta();
        privatemessagesactive.setDisplayName(ChatColor.AQUA + "Private Messages: " + ChatColor.GREEN + "enabled");
        globalchatactive.setDisplayName(ChatColor.AQUA + "Global Chat: " + ChatColor.GREEN + "enabled");
        this.globalChatActiveItem.setItemMeta(globalchatactive);
        this.duelRequestsEnabledItem.setItemMeta(duelrequests);
        this.privateMessagesActiveItem.setItemMeta(privatemessagesactive);
        this.playerVisibilityItem.setItemMeta(playervisibility);
        ItemStack nullGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta nullglass = nullGlass.getItemMeta();
        nullglass.setDisplayName(ChatColor.DARK_GRAY + " ");
        nullGlass.setItemMeta(nullglass);
        this.settings = Bukkit.createInventory(player, 9, ChatColor.RED + "Settings");
        settings.setItem(0, this.globalChatActiveItem);
        settings.setItem(1, this.duelRequestsEnabledItem);
        settings.setItem(2, this.privateMessagesActiveItem);
        settings.setItem(3, this.playerVisibilityItem);
        for (String lad : Main.INSTANCE.getLaddersConfig().getStringList("ladders-list")) {
            elo.put(PApi.LADDERS.get(lad), 1000);
        }
    }

    public int getGlobalElo() {
        int elo = 0;
        for (Integer i : this.elo.values()) elo += i;
        return elo / this.elo.values().size();
    }

    public PUser(UUID uuid, String name, String rank, PKillEffect killEffect, boolean displayRank, boolean duelRequestsEnabled, int playerVisibility, Map<Ladder, ItemStack[]> invs, Map<Ladder, Integer> elo, int kills, int deaths, EnumSet<PKillEffect> ownedEffects, boolean initInvs) {
        this.name = name;
        this.uuid = uuid;
        this.player = Bukkit.getPlayer(uuid);
        this.rank = rank;
        this.killEffect = killEffect;
        this.displayRank = displayRank;
        this.duelRequestsEnabled = duelRequestsEnabled;
        this.playerVisibility = playerVisibility;
        this.invsEdited = invs;
        this.ownedEffects = ownedEffects;
        this.elo = elo;
        if (initInvs) {
            this.globalChatActiveItem = new ItemStack(Material.PAPER);
            this.privateMessagesActiveItem = new ItemStack(Material.FLINT);
            this.duelRequestsEnabledItem = new ItemStack(Material.DIAMOND_SWORD);
            this.playerVisibilityItem = new ItemStack(Material.INK_SACK);
            ItemMeta playervisibility = this.playerVisibilityItem.getItemMeta();
            switch (this.playerVisibility) {
                case 0:
                    playervisibility.setDisplayName(ChatColor.AQUA + "Player Visibility: " + ChatColor.RED + "disabled");
                    this.playerVisibilityItem.setDurability((short) 8);
                    break;
                case 1:
                    playervisibility.setDisplayName(ChatColor.AQUA + "Player Visibility: " + ChatColor.YELLOW + "ranks only");
                    this.playerVisibilityItem.setDurability((short) 9);
                    break;
                case 2:
                    playervisibility.setDisplayName(ChatColor.AQUA + "Player Visibility: " + ChatColor.GREEN + "enabled");
                    this.playerVisibilityItem.setDurability((short) 10);
                    break;
            }
            ItemMeta duelrequests = this.duelRequestsEnabledItem.getItemMeta();
            if (this.duelRequestsEnabled) {
                duelrequests.setDisplayName(ChatColor.AQUA + "Duel Requests: " + ChatColor.GREEN + "enabled");
            } else {
                duelrequests.setDisplayName(ChatColor.AQUA + "Duel Requests: " + ChatColor.RED + "disabled");
            }
            ItemMeta globalchatactive = globalChatActiveItem.getItemMeta();
            ItemMeta privatemessagesactive = privateMessagesActiveItem.getItemMeta();
            if (MariaDB.ENABLED) {
                User user = User.get(uuid);
                if (user.isPrivateMessagesActive()) {
                    privatemessagesactive.setDisplayName(ChatColor.AQUA + "Private Messages: " + ChatColor.GREEN + "enabled");
                } else {
                    privatemessagesactive.setDisplayName(ChatColor.AQUA + "Private Messages: " + ChatColor.RED + "disabled");
                }
                if (user.isGlobalChatEnabled()) {
                    globalchatactive.setDisplayName(ChatColor.AQUA + "Global Chat: " + ChatColor.GREEN + "enabled");
                } else {
                    globalchatactive.setDisplayName(ChatColor.AQUA + "Global Chat: " + ChatColor.RED + "disabled");
                }
            }
            this.globalChatActiveItem.setItemMeta(globalchatactive);
            this.duelRequestsEnabledItem.setItemMeta(duelrequests);
            this.privateMessagesActiveItem.setItemMeta(privatemessagesactive);
            this.playerVisibilityItem.setItemMeta(playervisibility);
            ItemStack nullGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
            ItemMeta nullglass = nullGlass.getItemMeta();
            nullglass.setDisplayName(ChatColor.DARK_GRAY + " ");
            nullGlass.setItemMeta(nullglass);
            this.settings = Bukkit.createInventory(this.player, 9, ChatColor.RED + "Settings");
            settings.setItem(0, this.globalChatActiveItem);
            settings.setItem(1, this.duelRequestsEnabledItem);
            settings.setItem(2, this.privateMessagesActiveItem);
            settings.setItem(3, this.playerVisibilityItem);
        }
        this.kills = kills;
        this.deaths = deaths;
        for (String lad : Main.INSTANCE.getLaddersConfig().getStringList("ladders-list")) {
            if (!this.elo.containsKey(PApi.LADDERS.get(lad))) elo.put(PApi.LADDERS.get(lad), 1000);
        }
    }

    public int getPlayerVisibility() {
        return playerVisibility;
    }

    public void forceQueue(@Nullable PUser playerUser) {
        this.forceQueued = playerUser;
    }

    public PUser getForceQueued() {
        return forceQueued;
    }

    public Map<Ladder, ItemStack[]> getInvsEdited() {
        return invsEdited;
    }

    public Inventory getSettings() {
        return settings;
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public void setInQueue(boolean inQueue) {
        isInQueue = inQueue;
    }

    public boolean isSpectating() {
        return isSpectating;
    }

    public String getLastDuelReceived() {
        return this.lastDuelReceived;
    }

    public Ladder getLastQueueLadder() {
        return lastQueueLadder;
    }

    public void setLastQueueLadder(Ladder lastQueueLadder) {
        this.lastQueueLadder = lastQueueLadder;
    }

    public Player getLastDueledPlayer() {
        return lastDueledPlayer;
    }

    public void setLastDueledPlayer(Player lastDueledPlayer) {
        this.lastDueledPlayer = lastDueledPlayer;
    }

    public void setLastDuelReceived(String lastDuelReceived) {
        this.lastDuelReceived = lastDuelReceived;
    }

    public String getName() {
        return name;
    }

    public void setEditing(boolean b) {
        this.isEditing = b;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void spectate(PGame game, boolean silent, boolean giveItems) {
        if (isSpectating)
            return;
        if (!inGame) {
            this.player.teleport(game.getSpecLocation());
        }
        game.addSpec(this);
        player.setAllowFlight(true);
        player.setFlying(true);
        this.isSpectating = true;
        if (giveItems) {
            player.getInventory().clear();
            player.getInventory().setContents(Practice.SPECTATOR_INV.getContents());
            player.updateInventory();
        }
        if (!silent) {
            this.lastSpec = game;
            game.broadcastToGame(PLAYER_SPECTATE_BROADCAST.replaceAll("-player", player.getName()));
        }
        if (game instanceof GameVersus) {
            ((GameVersus) game).getTeam1().forEach(playerUser -> {
                if (!player.canSee(playerUser.player)) player.showPlayer(playerUser.player);
            });
            ((GameVersus) game).getTeam2().forEach(playerUser -> {
                if (!player.canSee(playerUser.player)) player.showPlayer(playerUser.player);
            });
        } else {
            ((GameFFA) game).getPlayers().forEach(playerUser -> {
                if (!player.canSee(playerUser.player)) player.showPlayer(playerUser.player);
            });
        }
        game.getSpectators().forEach(playerUser -> {
            if (!this.equals(playerUser)) {
                playerUser.player.showPlayer(player);
                player.showPlayer(playerUser.player);
            }
        });
    }

    public void setSpectating(boolean b) {
        this.isSpectating = b;
    }

    public boolean isDuelRequestsEnabled() {
        return duelRequestsEnabled;
    }

    public void setDuelRequestsEnabled(boolean duelRequestsEnabled) {
        this.duelRequestsEnabled = duelRequestsEnabled;
    }

    public boolean isDisplayRank() {
        return displayRank;
    }


    public void setDisplayRank(boolean displayRank) {
        this.displayRank = displayRank;
    }

    public PGame getLastSpec() {
        return lastSpec;
    }

    public ItemStack[] getInventoryEdit(Ladder ladder) {
        if (!invsEdited.containsKey(ladder)) return null;
        return invsEdited.get(ladder);
    }

    public void inventoryPut(Ladder ladder, ItemStack[] inventory) {
        invsEdited.put(ladder, inventory);
    }

    public boolean invContainsKey(Ladder ladder) {
        return invsEdited.containsKey(ladder);
    }

    public void goBackToLobby() {
        player.setAllowFlight(false);
        player.setFlying(false);
        if (isSpectating) {
            if (lastSpec != null) {
                lastSpec.removeSpec(this);
                lastSpec.getSpectators().forEach(playerUser -> {
                    CraftPlayer entityPlayer = ((CraftPlayer) player.getPlayer());
                    if (!equals(playerUser)) {
                        entityPlayer.hidePlayer(playerUser.player);
                        switch (PUser.get(entityPlayer).getPlayerVisibility()) {
                            case 0: {
                                entityPlayer.hidePlayer(playerUser.getPlayer());
                                break;
                            }
                            case 1: {
                                if (playerUser.player.hasPermission("practice.rank") && !playerUser.player.hasPermission("practice.rankbypass"))
                                    entityPlayer.showPlayer(playerUser.getPlayer());
                                else
                                    entityPlayer.hidePlayer(playerUser.getPlayer());
                                break;
                            }
                            case 2: {
                                entityPlayer.showPlayer(playerUser.getPlayer());
                                break;
                            }
                        }
                    }
                });
            }
            if (!inGame)
                Bukkit.getPluginManager().callEvent(new PlayerKitDeselectEvent(player, this));
            this.isSpectating = false;
            player.setAllowFlight(false);
        }
        Scoreboards.INSTANCE.putLobby(uuid);
        if (inGame) {
            setInGame(false);
            this.player.getInventory().setHelmet(null);
            this.player.getInventory().setChestplate(null);
            this.player.getInventory().setLeggings(null);
            this.player.getInventory().setBoots(null);
            this.player.setFireTicks(0);
            if (lastGame instanceof GameVersus) {
                ((GameVersus) lastGame).getTeam1().forEach(playerUser -> {
                    switch (this.getPlayerVisibility()) {
                        case 0: {
                            player.hidePlayer(playerUser.getPlayer());
                            break;
                        }
                        case 1: {
                            if (playerUser.player.hasPermission("practice.rank") && !playerUser.player.hasPermission("practice.rankbypass"))
                                player.showPlayer(playerUser.getPlayer());
                            else
                                player.hidePlayer(playerUser.getPlayer());
                            break;
                        }
                        case 2: {
                            player.showPlayer(playerUser.getPlayer());
                            break;
                        }
                    }
                });
                ((GameVersus) lastGame).getTeam2().forEach(playerUser -> {
                    switch (this.getPlayerVisibility()) {
                        case 0: {
                            player.hidePlayer(playerUser.getPlayer());
                            break;
                        }
                        case 1: {
                            if (playerUser.player.hasPermission("practice.rank") && !playerUser.player.hasPermission("practice.rankbypass"))
                                player.showPlayer(playerUser.getPlayer());
                            else
                                player.hidePlayer(playerUser.getPlayer());
                            break;
                        }
                        case 2: {
                            player.showPlayer(playerUser.getPlayer());
                            break;
                        }
                    }
                });
            } else {
                ((GameFFA) lastGame).getPlayers().forEach(playerUser -> {
                    switch (this.getPlayerVisibility()) {
                        case 0: {
                            player.hidePlayer(playerUser.getPlayer());
                            break;
                        }
                        case 1: {
                            if (playerUser.player.hasPermission("practice.rank") && !playerUser.player.hasPermission("practice.rankbypass"))
                                player.showPlayer(playerUser.getPlayer());
                            else
                                player.hidePlayer(playerUser.getPlayer());
                            break;
                        }
                        case 2: {
                            player.showPlayer(playerUser.getPlayer());
                            break;
                        }
                    }
                });
            }
            player.updateInventory();
            Bukkit.getPluginManager().callEvent(new PlayerKitDeselectEvent(player, this));
        }
        if (isEditing) {
            this.isEditing = false;
            Bukkit.getPluginManager().callEvent(new PlayerKitDeselectEvent(player, this));
        }
        Location location = PApi.SPAWN_LOCATION;
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.teleport(location);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(12.0f);
    }

    public boolean isEditing() {
        return isEditing;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isInGame() {
        return inGame;
    }

    public PParty getParty() {
        return party;
    }

    public PGame getLastGame() {
        return lastGame;
    }

    public void setLastGame(PGame game) {
        this.lastGame = game;
    }

    public void setInGame(boolean b) {
        this.inGame = b;
        if (b) {
            Practice.getPlayersFighting().add(this);
        } else {
            Practice.getPlayersFighting().remove(this);
        }
    }

    public boolean isInParty() {
        return inParty;
    }

    public boolean joinParty(PParty party) {
        if (party.addPlayer(this)) {
            this.inParty = true;
            this.party = party;
            party.broadcast(PARTY_JOIN_BROADCAST.replaceAll("-player", name));
            if (!party.getLeader().equals(this)) {
                party.onPlayerJoin(this);
            } else {
                party.onPlayerJoin(this);
            }
            return true;
        }
        return false;
    }

    public void leaveCurrentParty() {
        if (inParty) {
            this.inParty = false;
            party.removePlayer(this);
            this.party = null;
        }
    }

    public void leaveCurrentParty(String message) {
        if (inParty) {
            player.sendMessage(message);
            this.inParty = false;
            party.removePlayer(this);
            this.party = null;
        }
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public boolean isPartyChatActive() {
        return partyChatActive;
    }

    public void setPartyChatActive(boolean partyChatActive) {
        this.partyChatActive = partyChatActive;
    }

    public void setPlayerVisibility(int playerVisibility) {
        this.playerVisibility = playerVisibility;
    }

    @Override
    public int compareTo(@Nonnull PUser o) {
        if (equals(o))
            return 0;
        int totalElo = 0;
        for (Ladder ladder : PApi.LADDERS.values()) {
            totalElo += elo.get(ladder);
        }
        int globalElo = totalElo / elo.size();
        int totalEloO = 0;
        for (Ladder ladder : PApi.LADDERS.values()) {
            totalEloO += o.elo.get(ladder);
        }
        int globalEloO = totalEloO / o.elo.size();
        return Integer.compare(globalElo, globalEloO);
    }



    public static class UserListener implements Listener {

        private final Map<Player, Inventory> editInv;
        private final Map<Player, Ladder> editing;

        public UserListener(Main plugin) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            editInv = new HashMap<>();
            editing = new HashMap<>();
        }

        public void onItemInteract(PlayerInteractEvent event) {
            if (event.hasItem()) {
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                    if (event.getItem().equals(Practice.KIT_EDITOR)) {
                        event.getPlayer().openInventory(Practice.EDITABLE_KITS);
                        editInv.put(event.getPlayer(), null);
                    }
                }
            }
        }

        public void onInvClose(InventoryCloseEvent event) {
            if (event.getInventory().equals(editInv.get((Player) event.getPlayer()))) {
                PUser playerUser = PUser.get(event.getPlayer().getUniqueId());
                editInv.remove((Player) event.getPlayer());
                editing.remove((Player) event.getPlayer());
                ((Player) event.getPlayer()).sendMessage(ChatColor.RED + "Cancelled.");
                playerUser.goBackToLobby();
                ((Player) event.getPlayer()).updateInventory();
            }
        }

        @EventHandler
        public void onItemStackClick(InventoryClickEvent event) {
            if (event.getInventory().equals(Practice.EDITABLE_KITS)) {
                event.setCancelled(true);
                if (editInv.containsKey((Player) event.getWhoClicked())) {
                    ItemStack currentItem = event.getCurrentItem();
                    if (currentItem == null || currentItem.getItemMeta() == null) {
                        return;
                    }
                    PUser.get(event.getWhoClicked().getUniqueId()).setEditing(true);
                    Ladder currentLadder = PApi.LADDERS_BY_DISPLAYNAME.get(currentItem.getItemMeta().getDisplayName());
                    editing.put((Player) event.getWhoClicked(), currentLadder);
                    if (currentLadder != null) {
                        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.RED + "Edit your kit");
                        inventory.setContents(editing.get((Player) event.getWhoClicked()).getChestInventory().getContents());
                        PUser.get(event.getWhoClicked().getUniqueId()).getPlayer().getInventory().setContents(currentLadder.getInventory().getContents());
                        editInv.put((Player) event.getWhoClicked(), inventory);
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().openInventory(editInv.get((Player) event.getWhoClicked()));
                    }
                }
            } else if (editInv.containsKey((Player) event.getWhoClicked()) && event.getInventory().equals(editInv.get((Player) event.getWhoClicked()))) {
                PUser playerUser = PUser.get(event.getWhoClicked().getUniqueId());
                if (event.getCurrentItem() != null) {
                    if (event.getCurrentItem().equals(Practice.SAVE_KIT)) {
                        event.setCancelled(true);
                        playerUser.inventoryPut(editing.get((Player) event.getWhoClicked()), event.getWhoClicked().getInventory().getContents());
                        editInv.remove((Player) event.getWhoClicked());
                        editing.remove((Player) event.getWhoClicked());
                        event.getWhoClicked().closeInventory();
                        playerUser.goBackToLobby();
                        ((Player) event.getWhoClicked()).sendMessage(ChatColor.GREEN + "Kit saved.");
                    } else if (event.getCurrentItem().equals(Practice.CANCEL_KIT)) {
                        event.setCancelled(true);
                        editInv.remove((Player) event.getWhoClicked());
                        editing.remove((Player) event.getWhoClicked());
                        ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "Cancelled.");
                        event.getWhoClicked().closeInventory();
                        playerUser.goBackToLobby();
                    }
                }
            }
        }

        @EventHandler
        public void dragInv(InventoryDragEvent event) {
            if (event.getInventory().equals(Practice.ALL_LADERS_SELECT)) {
                event.setCancelled(true);
            }
        }


    }
}