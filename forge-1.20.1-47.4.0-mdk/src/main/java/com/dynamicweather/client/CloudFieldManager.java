package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudFieldManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();

    // Drift and wind
    public static Vector2f globalWindDirection = new Vector2f(1f, 0f); // Eastward
    public static float windSpeed = 0.05f; // blocks per tick

    private static final List<CloudClusterInstance> clusters = new ArrayList<>();
    private static Vec3 lastPlayerPos = Vec3.ZERO;
    private static boolean initialized = false;

    public static int MAX_CLOUD_CUBES = 300;
    public static int CUBES_PER_CLUSTER = 40;

    public static void updateCloudsIfNeeded() {
        if (mc.player == null) return;
        Vec3 currentPos = mc.player.position();

        if (!initialized || currentPos.distanceToSqr(lastPlayerPos) > 64 * 64) {
            lastPlayerPos = currentPos;
            initialized = true;
            regenerateCloudField(currentPos);
        }

        updateClusterOffsets();
    }

    private static void regenerateCloudField(Vec3 center) {
        CloudManager.clear();
        clusters.clear();

        int cloudCount = 0;
        int attempts = 0;

        while (cloudCount < MAX_CLOUD_CUBES && attempts < MAX_CLOUD_CUBES * 2) {
            attempts++;

            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 100 + random.nextDouble() * mc.options.getEffectiveRenderDistance() * 16;

            float baseX = (float) (center.x + Math.cos(angle) * distance);
            float baseZ = (float) (center.z + Math.sin(angle) * distance);
            float baseY = 150f + (random.nextFloat() - 0.5f) * 10f;

            List<Cloud> puff = CloudCluster.generateCumulus(0, 0, 0); // Localized to (0,0,0)
            float offsetAngle = random.nextFloat() * 10f - 5f; // +/-5 degrees variation

            CloudClusterInstance instance = new CloudClusterInstance(puff, offsetAngle, baseX, baseY, baseZ);
            clusters.add(instance);

            instance.getClouds().forEach(CloudManager::addCloud);

            cloudCount += puff.size();
        }
    }

    private static void updateClusterOffsets() {
        for (CloudClusterInstance cluster : clusters) {
            cluster.update(windSpeed, 1f, globalWindDirection.x, globalWindDirection.y); // 1 tick
        }
    }

    public static List<CloudClusterInstance> getClusters() {
        return clusters;
    }
}
