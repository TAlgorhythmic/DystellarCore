package net.zylesh.dystellarcore.arenasapi;

public class OfflineRegion {

    private final OfflineBlock[][][] blockData;
    private final double[] center;

    public OfflineRegion(OfflineBlock[][][] data, int xLenght, int yLenght, int zLenght) {
        this.blockData = data;
        this.center = new double[] {(double) xLenght / 2, (double) yLenght / 2, (double) zLenght / 2};
    }

    public void paste() {

    }

    public OfflineBlock[][][] getBlockData() {
        return blockData;
    }

    public double[] getCenter() {
        return center;
    }
}
