package com.dynamicweather.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudCluster {

    private static final Random random = new Random();

    // ====== 🎛 Tweakable Settings ======

    // Overall cluster puff count
    public static int CUBES_PER_CLUSTER = 60;

    // Legacy default cluster shape (used if type missing)
    public static float SPREAD_X = 8f;
    public static float SPREAD_Y = 3f;
    public static float SPREAD_Z = 8f;

    // Core/edge size scaling
    public static float MIN_CUBE_SIZE = 2.5f;   // Edge cubes
    public static float MAX_CUBE_SIZE = 9f;     // Center cubes

    // How fast size drops off from center (higher = tighter core)
    public static float SIZE_FALLOFF_STRENGTH = 2f;

    // ====================================

    public static List<Cloud> generateCluster(CloudType type, float centerX, float centerY, float centerZ) {
        List<Cloud> cluster = new ArrayList<>();

        float spreadX, spreadY, spreadZ;
        float minSize, maxSize, falloff;

        // Choose cloud shape parameters by type
        switch (type) {
            case STRATUS:
                spreadX = 32f;
                spreadY = 1.0f;
                spreadZ = 32f;
                minSize = 6f;
                maxSize = 10f;
                falloff = 0.8f;
                break;
            case CUMULONIMBUS:
                spreadX = 9f;
                spreadY = 5f;
                spreadZ = 9f;
                minSize = 3.5f;
                maxSize = 12f;
                falloff = 1.8f;
                break;
            case CUMULUS:
            default:
                spreadX = SPREAD_X;
                spreadY = SPREAD_Y;
                spreadZ = SPREAD_Z;
                minSize = MIN_CUBE_SIZE;
                maxSize = MAX_CUBE_SIZE;
                falloff = SIZE_FALLOFF_STRENGTH;
                break;
        }


        int numCubes;
        switch (type) {
            case STRATUS:
                numCubes = 100;
                break;
            case CUMULONIMBUS:
                numCubes = 12;
                break;
            case CUMULUS:
            default:
                numCubes = CUBES_PER_CLUSTER;
                break;
        }


        for (int i = 0; i < numCubes; i++) {

            // Gaussian offset
            float dx = (float) (random.nextGaussian() * spreadX);
            float dy = (float) (random.nextGaussian() * spreadY);
            float dz = (float) (random.nextGaussian() * spreadZ);

            // Normalized offset for distance measure
            float normX = dx / spreadX;
            float normY = dy / spreadY;
            float normZ = dz / spreadZ;

            float dist = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);
            float clamped = Math.max(0f, 1f - dist / falloff);  // closer to 1 = center

            // Cube size based on distance from center
            float size = minSize + clamped * (maxSize - minSize) + random.nextFloat();

            cluster.add(new Cloud(centerX + dx, centerY + dy, centerZ + dz, size));
        }

        return cluster;
    }
}
