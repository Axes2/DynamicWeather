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
    public static final int CELL_SIZE = 50;
    private static final int SCAN_RADIUS = 4; // Number of cells in each direction from player

    public static CloudType selectCloudTypeForCurrentCover(Random rand) {
        return CloudFieldManager.selectCloudTypeForCurrentCover(random);

    }
    private static final int SPAWN_INTERVAL = 200; // ticks (about 10 seconds)
    private static int spawnTimer = 0;
    private static final int SPAWN_ATTEMPTS_PER_TICK = 3; // try a few cells per interval






    public static void update() {
        spawnTimer++;
        if (spawnTimer < SPAWN_INTERVAL) return;
        spawnTimer = 0;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int px = (int) player.getX();
        int pz = (int) player.getZ();

        for (int i = 0; i < SPAWN_ATTEMPTS_PER_TICK; i++) {
            // Random cell offset from player's current position
            int dx = random.nextInt(SCAN_RADIUS * 2 + 1) - SCAN_RADIUS;
            int dz = random.nextInt(SCAN_RADIUS * 2 + 1) - SCAN_RADIUS;

            int cx = Math.floorDiv(px, CELL_SIZE) + dx;
            int cz = Math.floorDiv(pz, CELL_SIZE) + dz;
            long key = cellKey(cx, cz);

            if (visitedCells.contains(key)) continue;
            if (random.nextFloat() > getSpawnChance()) continue;

            visitedCells.add(key);

            CloudType type = CloudFieldManager.selectCloudTypeForCurrentCover(random);
            float worldX = cx * CELL_SIZE + randomOffset();
            float worldZ = cz * CELL_SIZE + randomOffset();
            float worldY = determineCloudHeight(type);

            CloudClusterInstance cluster = CloudClusterInstance.generateClusterAt(type, worldX, worldY, worldZ);
            cluster.setLifetime(CloudFieldManager.computeClusterLifetime());
            CloudFieldManager.addCluster(cluster);
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
    public static void unvisitCell(int cx, int cz) {
        long key = (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
        visitedCells.remove(key);
    }
    private static float determineCloudHeight(CloudType type) {
        float baseY;
        switch (type) {
            case STRATUS:
                baseY = 145f + random.nextFloat() * 20f;
                break;
            case CUMULONIMBUS:
                baseY = 140f + random.nextFloat() * 40f;
                break;
            case CUMULUS:
            default:
                baseY = 130f + random.nextFloat() * 40f;
                break;
        }
        return baseY;
    }



}
