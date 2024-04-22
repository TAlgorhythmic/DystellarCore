package net.zylesh.dystellarcore.arenasapi;

import org.bukkit.World;
import org.bukkit.util.Vector;

public class OfflineRegion {

    private final OfflineBlock[][][] blockData;
    private final double[] center;

    public OfflineRegion(OfflineBlock[][][] data, int xLenght, int yLenght, int zLenght) {
        this.blockData = data;
        this.center = new double[] {(double) xLenght / 2.0, (double) yLenght / 2.0, (double) zLenght / 2.0};
    }

    public void paste(World world, Vector vector) {
        // TODO
    }

    public OfflineBlock[][][] getBlockData() {
        return blockData;
    }

    public double[] getCenter() {
        return center;
    }
}
