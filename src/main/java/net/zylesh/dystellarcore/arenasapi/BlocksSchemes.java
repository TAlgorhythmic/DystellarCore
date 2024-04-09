package net.zylesh.dystellarcore.arenasapi;

import net.zylesh.dystellarcore.DystellarCore;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

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



    private static BlockGeometrySchemaUtilRepresentation decode(DataInputStream in) throws IOException {
        BlockGeometrySchemaUtilRepresentation schema = new BlockGeometrySchemaUtilRepresentation();
        schema.loadFromFile(in);
        return schema;
    }
}
