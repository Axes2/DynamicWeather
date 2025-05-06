package com.dynamicweather.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudCluster {

    private static final Random random = new Random();

    // ====== 🎛 Tweakable Settings ======

    // Overall cluster puff count
    public static int CUBES_PER_CLUSTER = 60;

    // Cluster shape (horizontal/vertical spread)
    public static float SPREAD_X = 8f;
    public static float SPREAD_Y = 3f;
    public static float SPREAD_Z = 8f;

    // Core/edge size scaling
    public static float MIN_CUBE_SIZE = 2.5f;   // Edge cubes
    public static float MAX_CUBE_SIZE = 9f;   // Center cubes

    // How fast size drops off from center (higher = tighter core)
    public static float SIZE_FALLOFF_STRENGTH = 2f;

    // ====================================

    public static List<Cloud> generateCumulus(float centerX, float centerY, float centerZ) {
        List<Cloud> cluster = new ArrayList<>();

        for (int i = 0; i < CUBES_PER_CLUSTER; i++) {
            // Gaussian offset
            float dx = (float) (random.nextGaussian() * SPREAD_X);
            float dy = (float) (random.nextGaussian() * SPREAD_Y);
            float dz = (float) (random.nextGaussian() * SPREAD_Z);

            // Normalized offset for distance measure
            float normX = dx / SPREAD_X;
            float normY = dy / SPREAD_Y;
            float normZ = dz / SPREAD_Z;

            float dist = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);
            float clamped = Math.max(0f, 1f - dist / SIZE_FALLOFF_STRENGTH);  // closer to 1 = center

            // Cube size based on distance from center
            float size = MIN_CUBE_SIZE + clamped * (MAX_CUBE_SIZE - MIN_CUBE_SIZE) + random.nextFloat();

            cluster.add(new Cloud(centerX + dx, centerY + dy, centerZ + dz, size));
        }

        return cluster;
    }
}
