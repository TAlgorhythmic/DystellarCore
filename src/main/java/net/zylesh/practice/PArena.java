package net.zylesh.practice;

import net.zylesh.dystellarcore.serialization.LocationSerialization;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import gg.zylesh.practice.practicecore.Main;

import java.util.ArrayList;
import java.util.List;

@SerializableAs("Arena")
public class PArena {

    private String fancyName;
    private final String name;
    private ItemStack icon;
    private double[] spawn1;
    private double[] spawn2;
    private Location center;
    private String schematic;
    private short spawn1Color = 0;
    private short spawn2Color = 0;
    private boolean portalEnabled = false;
    private List<Location> portalLocations1 = new ArrayList<>();
    private List<Location> portalLocations2 = new ArrayList<>();
    private boolean colorsEnabled = false;
    private String armorColor1 = "MAROON";
    private String armorColor2 = "MAROON";
    private boolean isBedwars = false;
    private List<Location> breakpoints1 = new ArrayList<>();
    private List<Location> breakpoints2 = new ArrayList<>();


    public PArena(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSchematic(String s) {
        this.schematic = s;
    }

    public String getFancyName() {
        return fancyName;
    }

    public void setFancyName(String s) {
        if (this.icon != null) {
            ItemMeta itemMeta = this.icon.getItemMeta();
            itemMeta.setDisplayName(s);
            this.icon.setItemMeta(itemMeta);
        }
        this.fancyName = s;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        if (this.fancyName != null) {
            ItemMeta itemMeta = icon.getItemMeta();
            itemMeta.setDisplayName(this.fancyName);
            icon.setItemMeta(itemMeta);
        }
        this.icon = icon;
    }

    public String getSchematic() {
        return schematic;
    }

    public double[] getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(double[] loc) {
        this.spawn1 = loc;
    }

    public double[] getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(double[] loc) {
        this.spawn2 = loc;
    }

    public boolean areColorsEnabled() {
        return colorsEnabled;
    }

    public void setColorsEnabled(boolean b) {
        this.colorsEnabled = b;
    }

    public Location getCenter() {
        return center;
    }

    public short getSpawn1Color() {
        return spawn1Color;
    }

    public void setSpawn1Color(short spawn1Color) {
        this.spawn1Color = spawn1Color;
    }

    public short getSpawn2Color() {
        return spawn2Color;
    }

    public void setSpawn2Color(short c) {
        this.spawn2Color = c;
    }

    public String getArmorColor1() {
        return armorColor1;
    }

    public void setArmorColor1(String s) {
        this.armorColor1 = s;
    }

    public String getArmorColor2() {
        return armorColor2;
    }

    public void setArmorColor2(String s) {
        this.armorColor2 = s;
    }

    public boolean isBedwars() {
        return isBedwars;
    }

    public void setBedwars(boolean b) {
        this.isBedwars = b;
    }

    public List<Location> getBreakpoints1() {
        return breakpoints1;
    }

    public void setBreakpoints1(List<Location> loc) {
        this.breakpoints1 = loc;
    }

    public List<Location> getBreakpoints2() {
        return breakpoints2;
    }

    public void setBreakpoints2(List<Location> loc) {
        this.breakpoints2 = loc;
    }

    public boolean isPortalEnabled() {
        return portalEnabled;
    }

    public void setPortalEnabled(boolean b) {
        portalEnabled = b;
    }

    public List<Location> getPortalLocation1() {
        return portalLocations1;
    }

    public List<Location> getPortalLocation2() {
        return portalLocations2;
    }

    public void setPortalLocation1(List<Location> loc) {
        this.portalLocations1 = loc;
    }

    public void setPortalLocation2(List<Location> loc) {
        this.portalLocations2 = loc;
    }

    public void setCenter(Location loc) {
        this.center = loc;
    }

    public void serialize() {
        Main plugin = Main.INSTANCE;
        if (!plugin.getArenasConfig().contains(name)) {
            plugin.getArenasConfig().createSection(name).set("name", name);
            plugin.save("arena");
        }
        ConfigurationSection section = plugin.getArenasConfig().getConfigurationSection(name);

        if (spawn1 != null) {
            section.set("x-first", spawn1[0]);
            section.set("y-first", spawn1[1]);
            section.set("z-first", spawn1[2]);
            section.set("yaw-first", spawn1[3]);
            section.set("pitch-first", spawn1[4]);
        }
        if (spawn2 != null) {
            section.set("x-second", spawn2[0]);
            section.set("y-second", spawn2[1]);
            section.set("z-second", spawn2[2]);
            section.set("yaw-second", spawn2[3]);
            section.set("pitch-second", spawn2[4]);
        }
        section.set("fancy-name", fancyName);
        section.set("schematic", schematic);
        section.set("icon", icon);
        if (center != null) {
            section.set("center-x", center.getX());
            section.set("center-y", center.getY());
            section.set("center-z", center.getZ());
            section.set("center-yaw", center.getYaw());
            section.set("center-pitch", center.getPitch());
        }
        section.set("color1", spawn1Color);
        section.set("color2", spawn2Color);
        section.set("portal-enabled", portalEnabled);
        if (portalEnabled) {
            List<String> portalLocs1 = new ArrayList<>();
            List<String> portalLocs2 = new ArrayList<>();
            for (Location location : portalLocations1)
                portalLocs1.add(LocationSerialization.locationToString(location));
            for (Location location : portalLocations2)
                portalLocs2.add(LocationSerialization.locationToString(location));
            section.set("portal-locations1", portalLocs1);
            section.set("portal-locations2", portalLocs2);
        }
        section.set("colors-enabled", colorsEnabled);
        section.set("armor-color1", armorColor1);
        section.set("armor-color2", armorColor2);
        section.set("bedwars", isBedwars);
        List<String> bp1 = new ArrayList<>();
        List<String> bp2 = new ArrayList<>();
        for (Location location : breakpoints1) bp1.add(LocationSerialization.locationToString(location));
        for (Location location : breakpoints2) bp2.add(LocationSerialization.locationToString(location));
        section.set("bp1", bp1);
        section.set("bp2", bp2);
        plugin.save("arena");
    }

    public static PArena deserialize(String name) {
        PArena arena = new PArena(name);
        Main plugin = Main.INSTANCE;
        ConfigurationSection section = plugin.getArenasConfig().getConfigurationSection(name);
        if (section.contains("fancy-name")) arena.setFancyName(section.getString("fancy-name"));
        if (section.contains("schematic")) arena.setSchematic(section.getString("schematic"));
        if (section.contains("icon")) arena.setIcon(section.getItemStack("icon"));
        double[] spawn1 = new double[]{section.getDouble("x-first"), section.getDouble("y-first"), section.getDouble("z-first"), section.getDouble("yaw-first"), section.getDouble("pitch-first")};
        double[] spawn2 = new double[]{section.getDouble("x-second"), section.getDouble("y-second"), section.getDouble("z-second"), section.getDouble("yaw-second"), section.getDouble("pitch-second")};
        Location location = new Location(PApi.GAMES_WORLD, section.getDouble("center-x"), section.getDouble("center-y"), section.getDouble("center-z"), (float) section.getDouble("center-yaw"), (float) section.getDouble("center-pitch"));
        arena.setSpawn1(spawn1);
        arena.setSpawn2(spawn2);
        arena.setCenter(location);
        if (section.contains("portal-enabled")) arena.setPortalEnabled(section.getBoolean("portal-enabled"));
        if (arena.portalEnabled) {
            List<Location> portalLocs1 = new ArrayList<>();
            List<Location> portalLocs2 = new ArrayList<>();
            for (String s : section.getStringList("portal-locations1"))
                portalLocs1.add(LocationSerialization.stringToLocation(s));
            for (String s : section.getStringList("portal-locations2"))
                portalLocs2.add(LocationSerialization.stringToLocation(s));
            arena.setPortalLocation1(portalLocs1);
            arena.setPortalLocation2(portalLocs2);
        }
        if (section.contains("colors-enabled")) arena.setColorsEnabled(section.getBoolean("colors-enabled"));
        if (arena.colorsEnabled) {
            arena.setSpawn1Color((short) section.getInt("color1"));
            arena.setSpawn2Color((short) section.getInt("color2"));
            arena.setArmorColor1(section.getString("armor-color1"));
            arena.setArmorColor2(section.getString("armor-color2"));
        }
        if (section.contains("bedwars")) arena.setBedwars(section.getBoolean("bedwars"));
        if (arena.isBedwars()) {
            if (section.contains("bp1")) {
                List<Location> bp1 = new ArrayList<>();
                for (String s : section.getStringList("break-points1"))
                    bp1.add(LocationSerialization.stringToLocation(s));
                arena.setBreakpoints1(bp1);
            }
            if (section.contains("bp2")) {
                List<Location> bp2 = new ArrayList<>();
                for (String s : section.getStringList("break-points2"))
                    bp2.add(LocationSerialization.stringToLocation(s));
                arena.setBreakpoints2(bp2);
            }
        }
        return arena;
    }
}
