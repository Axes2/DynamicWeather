package com.dynamicweather.client;

import java.util.ArrayList;
import java.util.List;

public class CloudManager {

    public static final List<Cloud> clouds = new ArrayList<>();

    public static void addCloud(Cloud cloud) {
        clouds.add(cloud);
    }

    public static void clear() {
        clouds.clear();
    }

    private static boolean generated = false;

    public static boolean isGenerated() {
        return generated;
    }

    public static void setGenerated(boolean value) {
        generated = value;
    }


    public static void initializeStaticGrid() {
        if (!clouds.isEmpty()) return; // Don't duplicate if already initialized

        int gridSize = 5;
        float spacing = 20f;
        float cloudY = 150f;
        float cloudSize = 10f;

        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                float worldX = 100 + x * spacing;
                float worldZ = 100 + z * spacing;
                clouds.add(new Cloud(worldX, cloudY, worldZ, cloudSize));
            }
        }
    }
}
