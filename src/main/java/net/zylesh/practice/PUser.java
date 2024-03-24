package net.zylesh.practice;

import net.zylesh.dystellarcore.utils.Utils;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.zylesh.practice.practicecore.util.Msg.PARTY_JOIN_BROADCAST;
import static net.zylesh.practice.practicecore.util.Msg.PLAYER_SPECTATE_BROADCAST;

public class PUser implements Comparable<PUser>, Listener {

    // TODO Do not disturb item configs

    /**
     * Do Not Disturb Modes
     */
    public static final byte DISABLED = 0;
    public static final byte ENABLED_CHAT_ONLY = 1;
    public static final byte ENABLED_PMS_ONLY = 2;
    public static final byte ENABLED = 3;

    protected static final Map<UUID, PUser> users = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static PUser get(Player p) {
        return users.get(p.getUniqueId());
    }

    public synchronized static PUser get(UUID uuid) {
        return users.get(uuid);
    }

    public static Map<UUID, PUser> getUsers() {
        return users;
    }

    public ItemStack duelRequestsEnabledItem;
    private Player player;
    private String name;
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
    private byte doNotDisturbMode = DISABLED;

    public PUser(UUID playeruuid) {
        this.invsEdited = new HashMap<>();
        this.isSpectating = false;
        this.uuid = playeruuid;
        this.lastDuelReceived = "accept nullImbecilxdkldsj";
        this.elo = new HashMap<>();
        this.killEffect = PKillEffect.NONE;
        this.ownedEffects = EnumSet.of(PKillEffect.NONE);
        initItems();
        for (String lad : Main.INSTANCE.getLaddersConfig().getStringList("ladders-list")) elo.put(PApi.LADDERS.get(lad), 1000);
    }

    public int getGlobalElo() {
        int elo = 0;
        for (Integer i : this.elo.values()) elo += i;
        return elo / this.elo.values().size();
    }

    public PUser(UUID uuid, String name, String rank, PKillEffect killEffect, boolean displayRank, boolean duelRequestsEnabled, int playerVisibility, Map<Ladder, ItemStack[]> invs, Map<Ladder, Integer> elo, int kills, int deaths, EnumSet<PKillEffect> ownedEffects, boolean initInvs, byte doNotDisturb) {
        this.name = name;
        this.uuid = uuid;
        this.rank = rank;
        this.killEffect = killEffect;
        this.displayRank = displayRank;
        this.duelRequestsEnabled = duelRequestsEnabled;
        this.playerVisibility = playerVisibility;
        this.invsEdited = invs;
        this.ownedEffects = ownedEffects;
        this.elo = elo;
        this.doNotDisturbMode = doNotDisturb;
        if (initInvs) initItems();
        this.kills = kills;
        this.deaths = deaths;
        for (String lad : Main.INSTANCE.getLaddersConfig().getStringList("ladders-list")) if (!this.elo.containsKey(PApi.LADDERS.get(lad))) elo.put(PApi.LADDERS.get(lad), 1000);
    }

    private void initItems() {
        this.duelRequestsEnabledItem = new ItemStack(Material.DIAMOND_SWORD);
        this.playerVisibilityItem = new ItemStack(Material.INK_SACK);
        ItemMeta playervisibility = this.playerVisibilityItem.getItemMeta();
        playervisibility.setDisplayName(ChatColor.DARK_AQUA + "Player Visibility:");
        switch (this.playerVisibility) {
            case 0: {
                List<String> lore = List.of(
                        " ",
                        ChatColor.RED + "➢ Off",
                        ChatColor.GRAY + " Ranks Only",
                        ChatColor.GRAY + " On",
                        " ",
                        ChatColor.YELLOW + "Click to toggle."
                );
                playervisibility.setLore(lore);
                this.playerVisibilityItem.setDurability((short) 8);
                break;
            }
            case 1: {
                List<String> lore = List.of(
                        " ",
                        ChatColor.GRAY + " Off",
                        ChatColor.YELLOW + "➢ Ranks Only",
                        ChatColor.GRAY + " On",
                        " ",
                        ChatColor.YELLOW + "Click to toggle."
                );
                playervisibility.setLore(lore);
                this.playerVisibilityItem.setDurability((short) 9);
                break;
            }
            case 2: {
                List<String> lore = List.of(
                        " ",
                        ChatColor.GRAY + " Off",
                        ChatColor.GRAY + " Ranks Only",
                        ChatColor.GREEN + "➢ On",
                        " ",
                        ChatColor.YELLOW + "Click to toggle."
                );
                playervisibility.setLore(lore);
                this.playerVisibilityItem.setDurability((short) 10);
                break;
            }
        }

        ItemMeta duelrequests = this.duelRequestsEnabledItem.getItemMeta();
        duelrequests.setDisplayName(ChatColor.AQUA + "Duel Requests:");
        List<String> requestsLore = List.of(
                " ",
                this.duelRequestsEnabled ? ChatColor.GRAY + " Off" : ChatColor.RED + "➢ Off",
                this.duelRequestsEnabled ? ChatColor.GREEN + "➢ On" : ChatColor.GRAY + " On",
                " ",
                ChatColor.YELLOW + "Click to toggle"
        );
        duelrequests.setLore(requestsLore);

        this.duelRequestsEnabledItem.setItemMeta(duelrequests);
        this.playerVisibilityItem.setItemMeta(playervisibility);
        ItemStack nullGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta nullglass = nullGlass.getItemMeta();
        nullglass.setDisplayName(ChatColor.DARK_GRAY + " ");
        nullGlass.setItemMeta(nullglass);
    }

    public void postInitialize(Player p) {
        this.player = p;
        this.name = p.getName();
        this.settings = Bukkit.createInventory(this.player, 9, ChatColor.RED + "Settings");
        settings.setItem(0, this.duelRequestsEnabledItem);
        settings.setItem(1, this.playerVisibilityItem);
    }

    public int getPlayerVisibility() {
        return playerVisibility;
    }

    public void forceQueue(@Nullable PUser playerUser) {
        this.forceQueued = playerUser;
    }

    @Nullable
    public PUser getForceQueued() {
        return forceQueued;
    }

    public Map<Ladder, ItemStack[]> getInvsEdited() {
        return invsEdited;
    }

    public byte getDoNotDisturbMode() {
        return doNotDisturbMode;
    }

    public void setDoNotDisturbMode(byte doNotDisturbMode) {
        this.doNotDisturbMode = doNotDisturbMode;
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
            for (PUser p : ((GameVersus) game).getTeam1()) if (p != null && !player.canSee(p.player)) player.showPlayer(p.player);
            for (PUser p : ((GameVersus) game).getTeam2()) if (p != null && !player.canSee(p.player)) player.showPlayer(p.player);
        } else if (game instanceof GameFFA) {
            for (PUser p : ((GameFFA) game).getPlayers()) if (p != null && !player.canSee(p.player)) player.showPlayer(p.player);
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
            if (!inGame) Bukkit.getPluginManager().callEvent(new PlayerKitDeselectEvent(player, this));
            this.isSpectating = false;
        }
        Scoreboards.INSTANCE.putLobby(uuid);
        Utils.resetEffects(this.player);
        Utils.removeArmor(this.player);
        if (lastGame instanceof GameVersus) {
            for (PUser p : ((GameVersus) lastGame).getTeam1()) {
                if (p != null) {
                    switch (this.getPlayerVisibility()) {
                        case 0: {
                            player.hidePlayer(p.getPlayer());
                            break;
                        }
                        case 1: {
                            if (p.player.hasPermission("practice.rank") && !p.player.hasPermission("practice.rankbypass"))
                                player.showPlayer(p.getPlayer());
                            else
                                player.hidePlayer(p.getPlayer());
                            break;
                        }
                        case 2: {
                            player.showPlayer(p.getPlayer());
                            break;
                        }
                    }
                }
            }
            for (PUser p : ((GameVersus) lastGame).getTeam2()) {
                if (p != null) {
                    switch (this.getPlayerVisibility()) {
                        case 0: {
                            player.hidePlayer(p.getPlayer());
                            break;
                        }
                        case 1: {
                            if (p.player.hasPermission("practice.rank") && !p.player.hasPermission("practice.rankbypass"))
                                player.showPlayer(p.getPlayer());
                            else
                                player.hidePlayer(p.getPlayer());
                            break;
                        }
                        case 2: {
                            player.showPlayer(p.getPlayer());
                            break;
                        }
                    }
                }
            }
        } else {
            for (PUser p : ((GameFFA) lastGame).getPlayers()) {
                if (p != null) {
                    switch (this.getPlayerVisibility()) {
                        case 0: {
                            player.hidePlayer(p.getPlayer());
                            break;
                        }
                        case 1: {
                            if (p.player.hasPermission("practice.rank") && !p.player.hasPermission("practice.rankbypass"))
                                player.showPlayer(p.getPlayer());
                            else
                                player.hidePlayer(p.getPlayer());
                            break;
                        }
                        case 2: {
                            player.showPlayer(p.getPlayer());
                            break;
                        }
                    }
                }
            }
        }
        player.updateInventory();
        Bukkit.getPluginManager().callEvent(new PlayerKitDeselectEvent(player, this));
        if (isEditing) {
            this.isEditing = false;
            Bukkit.getPluginManager().callEvent(new PlayerKitDeselectEvent(player, this));
        }
        Location location = PApi.SPAWN_LOCATION;
        player.teleport(location);

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
        return lastGame != null && !lastGame.isEnded;
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