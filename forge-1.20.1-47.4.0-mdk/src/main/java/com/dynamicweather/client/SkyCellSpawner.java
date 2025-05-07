package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SkyCellSpawner {
    private static final Set<Long> visitedCells = new HashSet<>();
    private static final Random random = new Random();
    private static final int CELL_SIZE = 50;
    private static final int SCAN_RADIUS = 4; // Number of cells in each direction from player

    public static void update() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int px = (int) player.getX();
        int pz = (int) player.getZ();

        int cellX = Math.floorDiv(px, CELL_SIZE);
        int cellZ = Math.floorDiv(pz, CELL_SIZE);

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                int cx = cellX + dx;
                int cz = cellZ + dz;
                long key = cellKey(cx, cz);

                if (!visitedCells.contains(key)) {
                    if (random.nextFloat() < getSpawnChance()) {
                        visitedCells.add(key);  // ✅ Only mark it if spawn happens

                        float worldX = cx * CELL_SIZE + randomOffset();
                        float worldZ = cz * CELL_SIZE + randomOffset();
                        float worldY = 130f + random.nextFloat() * 40f;

                        CloudClusterInstance cluster = CloudClusterInstance.generateClusterAt(worldX, worldY, worldZ);
                        cluster.setLifetime(CloudFieldManager.computeClusterLifetime());
                        CloudFieldManager.addCluster(cluster);
                    }
                }


            }
        }
    }

    private static long cellKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    private static float getSpawnChance() {
        return switch (CloudFieldManager.getCloudCover()) {
            case CLEAR -> 0.0f;
            case PARTLY_CLOUDY -> 0.4f;
            case MOSTLY_CLOUDY -> 0.7f;
            case OVERCAST, STORM -> 1.0f;
        };
    }


    private static float randomOffset() {
        return random.nextFloat() * CELL_SIZE;
    }

    public static void clear() {
        visitedCells.clear();
    }
}
