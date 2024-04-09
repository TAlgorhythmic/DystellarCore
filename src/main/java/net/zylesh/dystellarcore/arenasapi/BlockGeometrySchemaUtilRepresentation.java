package net.zylesh.dystellarcore.arenasapi;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.material.*;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class BlockGeometrySchemaUtilRepresentation {

    private static final byte BED = 0;
    private static final byte DIRECTIONAL = 1;
    private static final byte ATTACHABLE = 2;
    private static final byte COLORABLE = 3;
    private static final byte REDSTONE = 4;
    private static final byte CAKE = 5;
    private static final byte CHEST = 6;
    private static final byte PRESSURE_SENSOR = 8;
    private static final byte FLOWER_POT = 9;
    private static final byte OPENABLE = 10;
    private static final byte LONG_GRASS = 11;
    private static final byte MUSHROOM = 12;
    private static final byte NETHERWARTS = 13;
    private static final byte PISTON_BASE = 14;
    private static final byte PISTON_EXTENSION = 15;
    private static final byte RAILS = 16;
    private static final byte SANDSTONE = 17;
    private static final byte SIGN = 18;
    private static final byte STEP = 19;
    private static final byte SMOOTH_BRICK = 20;
    private static final byte MONSTER_EGGS = 21;
    private static final byte STAIRS = 22;
    private static final byte TRAPDOOR = 23;
    private static final byte TREE = 24;
    private static final byte WOODEN_STEP = 25;

    private Vector posVector1;
    private Vector posVector2;
    private World world;
    private Block[][][] blockData;
    private double[] center;

    // Cache for saving safely
    private int x;
    private int y;
    private int z;

    public BlockGeometrySchemaUtilRepresentation(World world, Vector posVector1, Vector posVector2) {
        this.posVector1 = posVector1;
        this.posVector2 = posVector2;
        this.world = world;
    }

    public BlockGeometrySchemaUtilRepresentation() {}

    /**
     * Warning! Big operation.
     */
    public void loadFromVectors() {
        if (posVector1 == null || posVector2 == null || world == null) {
            Bukkit.getLogger().warning("Warning! Tried to load region from vectors but they are null.");
            return;
        }
        int x; // X amount
        int y; // Y amount
        int z; // Z amount

        int xPos; // X minimum position
        int yPos; // Y minimum position
        int zPos; // Z minimum position

        if (posVector1.getBlockX() - posVector2.getBlockX() >= 0) {
            x = posVector1.getBlockX() - posVector2.getBlockX() + 1;
            xPos = posVector2.getBlockX();
        } else {
            x = posVector2.getBlockX() - posVector1.getBlockX() + 1;
            xPos = posVector1.getBlockX();
        }

        if (posVector1.getBlockY() - posVector2.getBlockY() >= 0) {
            y = posVector1.getBlockY() - posVector2.getBlockY() + 1;
            yPos = posVector2.getBlockY();
        } else {
            y = posVector2.getBlockY() - posVector1.getBlockY() + 1;
            yPos = posVector1.getBlockY();
        }

        if (posVector1.getBlockZ() - posVector2.getBlockZ() >= 0) {
            z = posVector1.getBlockZ() - posVector2.getBlockZ() + 1;
            zPos = posVector2.getBlockZ();
        } else {
            z = posVector2.getBlockZ() - posVector1.getBlockZ() + 1;
            zPos = posVector1.getBlockZ();
        }

        this.blockData = new Block[x][y][z];
        this.center = new double[] {(double) x / 2, (double) y / 2, (double) z / 2};

        // Init cache
        this.x = x;
        this.y = y;
        this.z = z;

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    this.blockData[i][j][k] = world.getBlockAt(xPos, yPos, zPos);
                    zPos++;
                }
                yPos++;
            }
            xPos++;
        }
    }

    public void loadFromFile(DataInputStream in) throws IOException {

    }

    @SuppressWarnings("deprecation")
    public void encode(DataOutputStream out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    Block block = this.blockData[i][j][k];
                    out.writeInt(i);
                    out.writeInt(j);
                    out.writeInt(k);
                    out.writeInt(block.getTypeId());
                    out.writeUTF(block.getBiome().name());

                    byte extensions = getExtensions(block);

                    out.writeByte(extensions);

                    if (block.getState() instanceof Bed) {
                        Bed bed = (Bed) block.getState();
                        out.writeByte(BED);
                        out.writeBoolean(bed.isHeadOfBed());
                    }
                    if (block.getState() instanceof Directional) {
                        Directional direction = (Directional) block.getState();
                        out.writeByte(DIRECTIONAL);
                        out.writeUTF(direction.getFacing().name());
                    }
                    if (block.getState() instanceof Attachable) {
                        Attachable attachable = (Attachable) block.getState();
                        out.writeByte(ATTACHABLE);
                        out.writeUTF(attachable.getAttachedFace().name());
                    }
                    if (block.getState() instanceof Colorable) {
                        Colorable colorable = (Colorable) block.getState();
                        out.writeByte(COLORABLE);
                        out.writeUTF(colorable.getColor().name());
                    }
                    if (block.getState() instanceof Redstone) {
                        Redstone redstone = (Redstone) block.getState();
                        out.writeByte(COLORABLE);
                        out.writeBoolean(redstone.isPowered());
                    }
                    if (block.getState() instanceof Cake) {
                        Cake cake = (Cake) block.getState();
                        out.writeByte(CAKE);
                        out.writeInt(cake.getSlicesEaten());
                        out.writeInt(cake.getSlicesRemaining());
                    }
                    if (block.getState() instanceof Chest) {
                        Chest chest = (Chest) block.getState();
                        out.writeByte(CHEST);
                        out.writeUTF(InventorySerialization.inventoryToString(chest.getBlockInventory().getContents()));
                    }
                    if (block.getState() instanceof PressureSensor) {
                        PressureSensor sensor = (PressureSensor) block.getState();
                        out.writeByte(PRESSURE_SENSOR);
                        out.writeBoolean(sensor.isPressed());
                    }
                    if (block.getState() instanceof FlowerPot) {
                        FlowerPot flowerpot = (FlowerPot) block.getState();
                        out.writeByte(FLOWER_POT);
                        out.writeInt(flowerpot.getContents().getItemTypeId());
                    }
                    if (block.getState() instanceof Openable) {
                        Openable openable = (Openable) block.getState();
                        out.writeByte(OPENABLE);
                        out.writeBoolean(openable.isOpen());
                    }
                    if (block.getState() instanceof LongGrass) {
                        LongGrass grass = (LongGrass) block.getState();
                        out.writeByte(LONG_GRASS);
                        out.writeUTF(grass.getSpecies().name());
                    }
                    if (block.getState() instanceof Mushroom) {
                        Mushroom mushroom = (Mushroom) block.getState();
                        out.writeByte(MUSHROOM);
                        out.writeBoolean(mushroom.isStem());
                    }
                    if (block.getState() instanceof NetherWarts) {
                        NetherWarts netherWarts = (NetherWarts) block.getState();
                        out.writeByte(NETHERWARTS);
                        out.writeUTF(netherWarts.getState().name());
                    }
                    if (block.getState() instanceof PistonBaseMaterial) {
                        PistonBaseMaterial material = (PistonBaseMaterial) block.getState();
                        out.writeByte(PISTON_BASE);
                        out.writeBoolean(material.isSticky());
                    }
                    if (block.getState() instanceof PistonExtensionMaterial) {
                        PistonExtensionMaterial material = (PistonExtensionMaterial) block.getState();
                        out.writeByte(PISTON_EXTENSION);
                        out.writeUTF(material.getAttachedFace().name());
                    }
                    if (block.getState() instanceof Rails) {
                        Rails r = (Rails) block.getState();
                        out.writeByte(RAILS);
                        out.writeBoolean(r.isCurve());
                    }
                    if (block.getState() instanceof Sandstone) {
                        Sandstone sandstone = (Sandstone) block.getState();
                        out.writeByte(SANDSTONE);
                        out.writeUTF(sandstone.getType().name());
                    }
                    if (block.getState() instanceof Sign) {
                        Sign sign = (Sign) block.getState();
                        out.writeByte(SIGN);
                        out.writeInt(sign.getLines().length);
                        for (String line : sign.getLines()) out.writeUTF(line);
                    }
                    if (block.getState() instanceof Step) {
                        Step step = (Step) block.getState();
                        out.writeByte(STEP);
                        out.writeBoolean(step.isInverted());
                    }
                    if (block.getState() instanceof SmoothBrick) {
                        SmoothBrick smoothBrick = (SmoothBrick) block.getState();
                        out.writeByte(SMOOTH_BRICK);
                        out.writeUTF(smoothBrick.getMaterial().name());
                    }
                    if (block.getState() instanceof MonsterEggs) {
                        MonsterEggs eggs = (MonsterEggs) block.getState();
                        out.writeByte(MONSTER_EGGS);
                        out.writeUTF(eggs.getMaterial().name());
                    }
                    if (block.getState() instanceof Stairs) {
                        Stairs stairs = (Stairs) block.getState();
                        out.writeByte(STAIRS);
                        out.writeBoolean(stairs.isInverted());
                    }
                    if (block.getState() instanceof TrapDoor) {
                        TrapDoor trapDoor = (TrapDoor) block.getState();
                        out.writeByte(TRAPDOOR);
                        out.writeBoolean(trapDoor.isInverted());
                        out.writeBoolean(trapDoor.isOpen());
                    }
                    if (block.getState() instanceof Tree) {
                        Tree tree = (Tree) block.getState();
                        out.writeByte(TREE);
                        out.writeUTF(tree.getSpecies().name());
                    }
                    if (block.getState() instanceof WoodenStep) {
                        WoodenStep step = (WoodenStep) block.getState();
                        out.writeByte(WOODEN_STEP);
                        out.writeUTF(step.getSpecies().name());
                        out.writeBoolean(step.isInverted());
                    }
                }
            }
        }
    }

    private static byte getExtensions(Block block) {
        byte extensions = 0;
        if (block.getState() instanceof Bed) extensions++;
        if (block.getState() instanceof Directional) extensions++;
        if (block.getState() instanceof Attachable) extensions++;
        if (block.getState() instanceof Colorable) extensions++;
        if (block.getState() instanceof Redstone) extensions++;
        if (block.getState() instanceof Cake) extensions++;
        if (block.getState() instanceof Chest) extensions++;
        if (block.getState() instanceof Cauldron) extensions++;
        if (block.getState() instanceof PressureSensor) extensions++;
        if (block.getState() instanceof FlowerPot) extensions++;
        if (block.getState() instanceof Openable) extensions++;
        if (block.getState() instanceof LongGrass) extensions++;
        if (block.getState() instanceof Mushroom) extensions++;
        if (block.getState() instanceof NetherWarts) extensions++;
        if (block.getState() instanceof PistonBaseMaterial) extensions++;
        if (block.getState() instanceof PistonExtensionMaterial) extensions++;
        if (block.getState() instanceof Rails) extensions++;
        if (block.getState() instanceof Sandstone) extensions++;
        if (block.getState() instanceof Sign) extensions++;
        if (block.getState() instanceof Step) extensions++;
        if (block.getState() instanceof SmoothBrick) extensions++;
        if (block.getState() instanceof MonsterEggs) extensions++;
        if (block.getState() instanceof Stairs) extensions++;
        if (block.getState() instanceof TrapDoor) extensions++;
        if (block.getState() instanceof Tree) extensions++;
        if (block.getState() instanceof WoodenStep) extensions++;
        return extensions;
    }

    public void save(File file) throws IOException {
        BlocksSchemes.save(this, file);
    }

    @Nullable
    public Block[][][] getBlockData() {
        return blockData;
    }

    @Nullable
    public double[] getCenter() {
        return center;
    }
}
