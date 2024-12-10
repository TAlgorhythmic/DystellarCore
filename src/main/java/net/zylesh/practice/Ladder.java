package net.zylesh.practice;

import eu.vortexdev.api.KnockbackAPI;
import gg.zylesh.practice.practicecore.Main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Ladder {

    final List<String> arenas = new ArrayList<>();
    private final String name;
    private String fancyName;
    private boolean stickSpawn;
    private boolean isRegen;
    private Inventory inventory;
    private Inventory chestInv;
    private String profile;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    boolean canPlaceBlocks;
    private ItemStack icon;
    private boolean boxing;
    private boolean active;
    private boolean healthBar;
    private boolean isSumo;
    private boolean pvp;
    private boolean itemDrop = true;
    private int slot;
    private boolean editable;
    private boolean bedwarsEnabled = false;
    private int respawnDelay = 0;
    private boolean chestEnabled;
    private int deathType = 0;
    private final int id;
    private boolean limitCps;
    private int rounds = 1;
    private boolean disapearingBlocks = false;
    final EnumSet<GameType> gameTypes = EnumSet.noneOf(GameType.class);

    public Ladder(String name) {
        this.id = new Random().nextInt();
        this.name = name;
    }

    public Ladder(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public List<String> getArenas() {
        return arenas;
    }

    public void addArena(String arena) {
        arenas.add(arena);
    }

    public boolean isCpsLimitEnabled() {
        return limitCps;
    }

    public void enableCpsLimit(boolean b) {
        this.limitCps = b;
    }

    public String getKnockbackProfile() {
        return profile;
    }

    public void setKnockbackProfile(String profile) {
        this.profile = profile;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isRegen() {
        return this.isRegen;
    }

    public boolean isSumo() {
        return isSumo;
    }

    public void setSumo(boolean sumo) {
        isSumo = sumo;
    }

    public void setRegen(boolean b) {
        this.isRegen = b;
    }

    public boolean removeArena(String arena) {
        return arenas.remove(arena);
    }

    public Set<GameType> getGameTypes() {
        return gameTypes;
    }

    public void addGameType(GameType gameType) {
        gameTypes.add(gameType);
    }

    public void removeGameType(GameType gameType) {
        gameTypes.remove(gameType);
    }

    public void clearGameTypes() {
        gameTypes.clear();
    }

    public boolean isStickSpawn() {
        return stickSpawn;
    }

    public void setStickSpawn(boolean stickSpawn) {
        this.stickSpawn = stickSpawn;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public void setInventory(PlayerInventory inv) {
        this.inventory = inv;
        this.helmet = inv.getHelmet();
        this.chestplate = inv.getChestplate();
        this.leggings = inv.getLeggings();
        this.boots = inv.getBoots();
    }

    public boolean canPlaceBreakBlocks() {
        return canPlaceBlocks;
    }

    public void setCanPlaceBreakBlocks(boolean b) {
        this.canPlaceBlocks = b;
    }

    public boolean isBlocksDisapearing() {
        return disapearingBlocks;
    }

    public void setBlocksDisapearing(boolean b) {
        this.disapearingBlocks = b;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
        if (fancyName != null) {
            ItemMeta itemMeta = this.icon.getItemMeta();
            itemMeta.setDisplayName(this.fancyName);
            this.icon.setItemMeta(itemMeta);
        }
    }

    public String getName() {
        return name;
    }

    public String getFancyName() {
        return fancyName;
    }

    public void setFancyName(String s) {
        this.fancyName = s;
        if (this.icon != null) {
            ItemMeta itemMeta = this.icon.getItemMeta();
            itemMeta.setDisplayName(this.fancyName);
            this.icon.setItemMeta(itemMeta);
        }
    }

    public boolean healthBarActivated() {
        return healthBar;
    }

    public void setHealthBarActivated(boolean b) {
        this.healthBar = b;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getDeathType() {
        return deathType;
    }

    public void setDeathType(int deathType) {
        this.deathType = deathType;
    }

    public int getRespawnDelay() {
        return respawnDelay;
    }

    public void setRespawnDelay(int delay) {
        this.respawnDelay = delay;
    }

    public boolean isItemDrop() {
        return itemDrop;
    }

    public void setItemDrop(boolean b) {
        this.itemDrop = b;
    }

    public boolean isBedwarsEnabled() {
        return bedwarsEnabled;
    }

    public void setBedwarsEnabled(boolean b) {
        this.bedwarsEnabled = b;
    }

    public boolean isPvpEnabled() {
        return pvp;
    }

    public void setPvpEnabled(boolean b) {
        this.pvp = b;
    }

    public boolean getChestEnabled() {
        return chestEnabled;
    }

    public void setChestEnabled(boolean b) {
        this.chestEnabled = b;
    }

    public Inventory getChestInventory() {
        return chestInv;
    }

    public void setChestInventory(Inventory inv) {
        this.chestInv = inv;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean b) {
        this.editable = b;
    }

    public boolean isBoxing() {
        return boxing;
    }

    public void setBoxing(boolean b) {
        this.boxing = b;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean b) {
        this.active = b;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Ladder)
            return obj.hashCode() == id;
        return false;
    }

    public int getSlot() {
        return slot;
    }

    public void serialize() {
        Main plugin = Main.INSTANCE;
        if (!plugin.getLaddersConfig().contains(name)) {
            plugin.getLaddersConfig().createSection(name).set("name", name);
            plugin.save("ladder");
        }
        ConfigurationSection section = plugin.getLaddersConfig().getConfigurationSection(name);
        section.set("id", this.id);
        List<String> arenasNames = new ArrayList<>();
        if (!arenas.isEmpty()) {
            arenasNames.addAll(arenas);
            section.set("arenas", arenasNames);
        } else {
            section.set("arenas", arenasNames);
        }
        List<String> gameTypesNames = new ArrayList<>();
        if (!gameTypes.isEmpty()) {
            gameTypes.forEach(gameType -> gameTypesNames.add(gameType.toString()));
            section.set("gametypes", gameTypesNames);
        } else {
            section.set("gametypes", gameTypesNames);
        }
        section.set("regen", isRegen);
        section.set("slot", slot);
        section.set("stick-spawn", stickSpawn);
        if (inventory != null) {
            section.set("inventory", Arrays.asList(inventory.getContents()));
        } else {
            section.set("inventory", new ArrayList<ItemStack>());
        }
        section.set("helmet", helmet);
        section.set("chestplate", chestplate);
        section.set("leggings", leggings);
        section.set("sumo", isSumo);
        section.set("boots", boots);
        section.set("limit-cps", limitCps);
        section.set("icon", icon);
        section.set("build", canPlaceBlocks);
        section.set("name", name);
        section.set("fancy-name", fancyName);
        section.set("death-type", deathType);
        section.set("respawn-delay", respawnDelay);
        section.set("item-drop", itemDrop);
        section.set("bedwars-enabled", bedwarsEnabled);
        if (profile == null) {
            section.set("knockback-profile", KnockbackAPI.getDefault().getName());
        } else {
            section.set("knockback-profile", profile);
        }
        section.set("health-bar", healthBar);
        section.set("pvp-enabled", pvp);
        section.set("chest-enabled", chestEnabled);
        if (chestInv != null) {
            section.set("chest-inventory", Arrays.asList(chestInv.getContents()));
        } else {
            section.set("chest-inventory", new ArrayList<ItemStack>());
        }
        section.set("editable", editable);
        section.set("rounds", rounds);
        section.set("boxing", boxing);
        section.set("disapearing-blocks", disapearingBlocks);
        section.set("active", active);

        plugin.save("ladder");
    }

    @SuppressWarnings("unchecked")
    public static Ladder deserialize(String name) {
        Main plugin = Main.INSTANCE;
        ConfigurationSection section = plugin.getLaddersConfig().getConfigurationSection(name);
        Ladder result = new Ladder(name, section.getInt("id"));

        if (section.contains("arenas")) {
            List<String> arenass = section.getStringList("arenas");
            arenass.forEach(result::addArena);
        }
        if (section.contains("gametypes")) {
            List<String> gametypes1 = section.getStringList("gametypes");
            gametypes1.forEach(s -> result.addGameType(GameType.valueOf(s)));
        }
        if (section.contains("knockback-profile")) {
            result.profile = section.getString("knockback-profile");
        } else {
            result.profile = "Default";
        }

        boolean regen = section.getBoolean("regen");
        boolean stickspawn = section.getBoolean("stick-spawn");
        if (section.contains("inventory")) {
            Inventory inv = Bukkit.createInventory(null, InventoryType.PLAYER);
            List<ItemStack> contentsInv = ((List<ItemStack>) section.getList("inventory"));
            ItemStack[] contentsinsdx = contentsInv.toArray(new ItemStack[0]);
            inv.setContents(contentsinsdx);
            result.inventory = inv;
            result.helmet = section.getItemStack("helmet");
            result.chestplate = section.getItemStack("chestplate");
            result.leggings = section.getItemStack("leggings");
            result.boots = section.getItemStack("boots");
        }
        if (section.contains("icon")) {
            result.icon = section.getItemStack("icon");
        }
        boolean build = section.getBoolean("build");
        String fancyname = section.getString("fancy-name");
        boolean healthbar = section.getBoolean("health-bar");
        boolean pvpenabled = section.getBoolean("pvp-enabled");
        boolean chestenabled = section.getBoolean("chest-enabled");
        if (section.contains("chest-inventory")) {
            Inventory chestInv = Bukkit.createInventory(null, 54, ChatColor.RED + "Choose items");
            List<ItemStack> contentsChest = (List<ItemStack>) section.getList("chest-inventory");
            ItemStack[] itemStacks = contentsChest.toArray(new ItemStack[0]);
            chestInv.setContents(itemStacks);
            result.chestInv = chestInv;
        }
        boolean editable = section.getBoolean("editable");
        boolean boxing = section.getBoolean("boxing");
        boolean active = section.getBoolean("active");

        result.isSumo = section.getBoolean("sumo");
        result.isRegen = regen;
        result.stickSpawn = stickspawn;
        result.canPlaceBlocks = build;
        result.fancyName = fancyname;
        result.healthBar = healthbar;
        result.pvp = pvpenabled;
        result.chestEnabled = chestenabled;
        result.editable = editable;
        result.boxing = boxing;
        result.active = active;
        result.slot = section.getInt("slot");
        if (!section.contains("limit-cps")) {
            result.limitCps = true;
        } else {
            result.limitCps = section.getBoolean("limit-cps");
        }
        if (section.contains("rounds")) result.rounds = section.getInt("rounds");
        if (section.contains("death-type")) result.deathType = section.getInt("death-type");
        if (section.contains("respawn-delay")) result.respawnDelay = section.getInt("respawn-delay");
        if (section.contains("item-drop")) result.itemDrop = section.getBoolean("item-drop");
        if (section.contains("bedwars-enabled")) result.bedwarsEnabled = section.getBoolean("bedwars-enabled");
        if (section.contains("disapearing-blocks")) result.disapearingBlocks = section.getBoolean("disapearing-blocks");
        return result;
    }
}
