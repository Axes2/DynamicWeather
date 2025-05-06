package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CloudFieldManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();

    public static Vector2f globalWindDirection = new Vector2f(1f, 0f); // Eastward
    public static float windSpeed = 0.02f;

    private static final List<CloudClusterInstance> clusters = new ArrayList<>();

    public static int MAX_CLOUD_CUBES = 3000;
    public static int CUBES_PER_CLUSTER = 40;
    private static int tickCounter = 0;
    private static final int SPAWN_INTERVAL_TICKS = 40; // Every 10 seconds at 20 TPS

    public static void updateCloudsIfNeeded() {
        if (mc.player == null) return;
        tickCounter++;

        updateClusterOffsets();
        removeExpiredClusters();

        if (tickCounter % SPAWN_INTERVAL_TICKS == 0 && countTotalClouds() < MAX_CLOUD_CUBES) {
            trySpawnClusterNearPlayer();
        }
    }

    private static void trySpawnClusterNearPlayer() {
        Vec3 playerPos = mc.player.position();

        float windAngle = (float) Math.atan2(globalWindDirection.y, globalWindDirection.x);
        float spawnSpread = (float) Math.toRadians(90); // 90° arc upwind
        // 70% of clusters spawn upwind, 30% random direction
        boolean spawnUpwind = random.nextFloat() < 0.7f;

        float angle;
        if (spawnUpwind) {
            angle = windAngle + (float) Math.PI + (random.nextFloat() - 0.5f) * (float) Math.toRadians(90);
        } else {
            angle = (float) (random.nextFloat() * 2 * Math.PI);  // full circle
        }


        float distance = 100 + random.nextFloat() * mc.options.getEffectiveRenderDistance() * 16;
        float baseX = (float) (playerPos.x + Math.cos(angle) * distance);
        float baseZ = (float) (playerPos.z + Math.sin(angle) * distance);

        float baseY = 150f + (random.nextFloat() - 0.5f) * 10f;

        List<Cloud> puff = CloudCluster.generateCumulus(0, 0, 0);
        float angleOffset = random.nextFloat() * 10f - 5f;

        CloudClusterInstance instance = new CloudClusterInstance(puff, angleOffset, baseX, baseY, baseZ);
        instance.setLifetime(5000 + random.nextInt(800)); // 20–30 seconds

        clusters.add(instance);
        puff.forEach(CloudManager::addCloud);
    }

    private static void removeExpiredClusters() {
        Iterator<CloudClusterInstance> iterator = clusters.iterator();
        while (iterator.hasNext()) {
            CloudClusterInstance cluster = iterator.next();
            cluster.tickLifetime();
            if (cluster.isExpired()) {
                iterator.remove(); // This safely removes the entire cluster
            }
        }

    }

    private static void updateClusterOffsets() {
        for (CloudClusterInstance cluster : clusters) {
            cluster.update(windSpeed, 1f, globalWindDirection.x, globalWindDirection.y);
        }
    }

    private static int countTotalClouds() {
        int count = 0;
        for (CloudClusterInstance cluster : clusters) {
            count += cluster.getClouds().size();
        }
        return count;
    }

    public static List<CloudClusterInstance> getClusters() {
        return clusters;
    }
}
