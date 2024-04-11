package net.zylesh.dystellarcore.arenasapi;

import net.zylesh.dystellarcore.DystellarCore;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

import static net.zylesh.dystellarcore.arenasapi.BlockGeometrySchemaUtilRepresentation.*;

public class BlocksSchemes {

    public static void save(BlockGeometrySchemaUtilRepresentation schema, File file) throws IOException {
        if (file.createNewFile()) {
            DystellarCore.getAsyncManager().submit(() -> {
                try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
                    schema.encode(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            throw new FileAlreadyExistsException("This file already exists.");
        }
    }



    private static OfflineRegion load(DataInputStream in) throws IOException {
        BlockGeometrySchemaUtilRepresentation schema = new BlockGeometrySchemaUtilRepresentation();
        return schema.loadFromFile(in, true);
    }

    public static DataArray decodeExtensions(DataInputStream in) throws IOException {
        byte extensions = in.readByte();
        DataArray array = new DataArray();
        for (byte e = 0; e < extensions; e++) {
            byte type = in.readByte();
            ext.add(type);
            switch (type) {
                case BED: {
                    break;
                }
                case DIRECTIONAL: {
                    break;
                }
                case ATTACHABLE: {
                    break;
                }
                case COLORABLE: {
                    break;
                }
                case REDSTONE: {
                    break;
                }
                case CAKE: {
                    break;
                }
                case CHEST: {
                    break;
                }
                case PRESSURE_SENSOR: {
                    break;
                }
                case FLOWER_POT: {
                    break;
                }
                case OPENABLE: {
                    break;
                }
                case LONG_GRASS: {
                    break;
                }
                case MUSHROOM: {
                    break;
                }
                case NETHERWARTS: {
                    break;
                }
                case PISTON_BASE: {
                    break;
                }
                case PISTON_EXTENSION: {
                    break;
                }
                case RAILS: {
                    break;
                }
                case SANDSTONE: {
                    break;
                }
                case SIGN: {
                    break;
                }
                case STEP: {
                    break;
                }
                case SMOOTH_BRICK: {
                    break;
                }
                case MONSTER_EGGS: {
                    break;
                }
                case STAIRS: {
                    break;
                }
                case TRAPDOOR: {
                    break;
                }
                case TREE: {
                    break;
                }
                case WOODEN_STEP: {
                    break;
                }
            }
        }
        return array;
    }
}
