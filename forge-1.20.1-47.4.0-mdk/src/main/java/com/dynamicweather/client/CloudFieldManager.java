package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
    public static float globalWindSpeed = 0.02f;

    private static final List<CloudClusterInstance> clusters = new ArrayList<>();

    public static int MAX_CLOUD_CUBES = 3000;
    public static int CUBES_PER_CLUSTER = 40;

    private static boolean hasBootstrappedSky = false;
    private static int tickCounter = 0;
    private static final int SPAWN_INTERVAL_TICKS = 40; // Every 2 seconds at 20 TPS

    public static void clear() {
        clusters.clear();
    }

    public static void setWindSpeed(float speed) {
        globalWindSpeed = speed;
    }

    private static float getRenderDistanceSq() {
        int chunks = Minecraft.getInstance().options.renderDistance().get();
        float blocks = chunks * 16f;
        return blocks * blocks;
    }

    private static void bootstrapSkyPopulation(LocalPlayer player, int numClusters) {
        int renderDistBlocks = Minecraft.getInstance().options.renderDistance().get() * 16;

        for (int i = 0; i < numClusters; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * renderDistBlocks * 0.9;

            float x = (float) (player.getX() + Math.cos(angle) * radius);
            float z = (float) (player.getZ() + Math.sin(angle) * radius);
            float y = (float) player.getY() + 60f + random.nextFloat() * 20f;

            CloudClusterInstance cluster = CloudClusterInstance.generateClusterAt(x, y, z);
            clusters.add(cluster);
        }
    }

    public static void updateCloudsIfNeeded() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        tickCounter++;

        if (!hasBootstrappedSky) {
            bootstrapSkyPopulation(player, 55);
            hasBootstrappedSky = true;
        }

        updateClusterOffsets();
        removeExpiredClusters();

        if (tickCounter % SPAWN_INTERVAL_TICKS == 0 && countTotalClouds() < MAX_CLOUD_CUBES) {
            trySpawnClusterNearPlayer();
        }
    }

    public static int computeClusterLifetime() {
        int chunks = Minecraft.getInstance().options.renderDistance().get();
        float renderDistance = chunks * 16f;

        // Slow wind → longer lifetime
        float speedPerTick = globalWindSpeed + 0.01f; // prevent divide by zero
        return (int)((renderDistance * 1.5f) / speedPerTick);
    }


    private static void trySpawnClusterNearPlayer() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Vec3 playerPos = player.position();
        float renderDistance = mc.options.renderDistance().get() * 16f;

        // 🎯 Doughnut zone: avoid clutter near player
        float minDistance = 80f; // avoid directly overhead
        float maxDistance = renderDistance * 0.9f;

        float windAngle = (float) Math.atan2(globalWindDirection.y, globalWindDirection.x);

        // 🔀 Bias 70% of clouds upwind, but with a wide 180° cone
        float r = random.nextFloat();
        float angle;
        if (r < 0.7f) {
            float spread = (float) Math.toRadians(180); // ±90°
            angle = windAngle + (float) Math.PI + (random.nextFloat() - 0.5f) * spread;
        } else {
            angle = (float) (random.nextFloat() * 2 * Math.PI);
        }

        // 📏 Position in wide ring
        float distance = minDistance + random.nextFloat() * (maxDistance - minDistance);
        float baseX = (float) (playerPos.x + Math.cos(angle) * distance);
        float baseZ = (float) (playerPos.z + Math.sin(angle) * distance);
        float baseY = 130f + random.nextFloat() * 40f;

        List<Cloud> puff = CloudCluster.generateCumulus(0, 0, 0);
        float angleOffset = random.nextFloat() * 10f - 5f;

        CloudClusterInstance instance = new CloudClusterInstance(puff, angleOffset, baseX, baseY, baseZ);
        instance.setLifetime(computeClusterLifetime());

        clusters.add(instance);
        puff.forEach(CloudManager::addCloud);
    }



    private static void removeExpiredClusters() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Iterator<CloudClusterInstance> iterator = clusters.iterator();
        while (iterator.hasNext()) {
            CloudClusterInstance cluster = iterator.next();
            cluster.tickLifetime();

            if (cluster.isExpired() || isOutOfRenderDistance(cluster, player)) {
                iterator.remove(); // safe removal
            }
        }

        // If near or over max capacity, prune oldest off-screen clusters
        if (countTotalClouds() >= MAX_CLOUD_CUBES * 0.9) {
            iterator = clusters.iterator();
            while (iterator.hasNext()) {
                CloudClusterInstance cluster = iterator.next();
                if (cluster.getAge() > 1000 && isOutOfRenderDistance(cluster, player)) {
                    iterator.remove();
                }
            }
        }
    }


    private static boolean isOutOfRenderDistance(CloudClusterInstance cluster, LocalPlayer player) {
        float dx = (float) (player.getX() - (cluster.getBaseX() + cluster.getOffsetX()));
        float dz = (float) (player.getZ() - (cluster.getBaseZ() + cluster.getOffsetZ()));
        return (dx * dx + dz * dz) > getRenderDistanceSq();
    }

    private static void updateClusterOffsets() {
        for (CloudClusterInstance cluster : clusters) {
            cluster.update(globalWindSpeed * 2.5f, 1f, globalWindDirection.x, globalWindDirection.y);

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
