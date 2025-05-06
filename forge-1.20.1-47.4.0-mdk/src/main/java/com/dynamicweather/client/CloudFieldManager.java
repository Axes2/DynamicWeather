package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class CloudFieldManager {

    private static final Random random = new Random();
    private static final float CLUSTER_RADIUS = 25f;
    private static final int CUBES_PER_CLUSTER = 40;

    public static int MAX_CLOUD_CUBES = 300;
    private static Vec3 lastPlayerPos = Vec3.ZERO;

    private static boolean initialized = false;

    public static void updateCloudsIfNeeded() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 currentPos = mc.player.position();

        // Only update if player has moved significantly
        if (!initialized || currentPos.distanceToSqr(lastPlayerPos) > 64 * 64) {
            lastPlayerPos = currentPos;
            initialized = true;
            regenerateCloudField(currentPos);
        }
    }

    private static void regenerateCloudField(Vec3 center) {
        CloudManager.clear();

        int cloudCount = 0;
        int attempts = 0;

        while (cloudCount < MAX_CLOUD_CUBES && attempts < MAX_CLOUD_CUBES * 2) {
            attempts++;

            // Random cluster position in a circular range
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;

            float x = (float) (center.x + Math.cos(angle) * distance);
            float z = (float) (center.z + Math.sin(angle) * distance);
            float y = 150f + (random.nextFloat() - 0.5f) * 10f; // mild vertical jitter

            List<Cloud> cluster = CloudCluster.generateCumulus(x, y, z, CUBES_PER_CLUSTER, CLUSTER_RADIUS, 5f, CLUSTER_RADIUS);

            for (Cloud c : cluster) {
                if (cloudCount >= MAX_CLOUD_CUBES) break;
                CloudManager.addCloud(c);
                cloudCount++;
            }
        }
    }
}
