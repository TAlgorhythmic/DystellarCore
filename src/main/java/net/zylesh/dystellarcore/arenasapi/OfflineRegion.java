package net.zylesh.dystellarcore.arenasapi;

import org.bukkit.World;
import org.bukkit.block.Block;
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
        int x = (int) (vector.getBlockX() - center[0]);
        int y = (int) (vector.getBlockY() - center[1]);
        int z = (int) (vector.getBlockZ() - center[2]);
        for (int i = 0; i < blockData.length; i++) {
            for (int j = 0; j < blockData[i].length; j++) {
                for (int k = 0; k < blockData[i][j].length; k++) {
                    blockData[i][j][k].paste(world, x, y, z);

                }
            }
        }
    }

    public OfflineBlock[][][] getBlockData() {
        return blockData;
    }

    public double[] getCenter() {
        return center;
    }
}
