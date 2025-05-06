package com.dynamicweather.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudCluster {

    private static final Random random = new Random();

    public static List<Cloud> generateCumulus(float centerX, float centerY, float centerZ, int count, float spreadX, float spreadY, float spreadZ) {
        List<Cloud> cluster = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Generate Gaussian offsets using Box-Muller transform
            float dx = (float) (random.nextGaussian() * spreadX);
            float dy = (float) (random.nextGaussian() * spreadY);
            float dz = (float) (random.nextGaussian() * spreadZ);

            // Slight size variation
            float size = 6f + random.nextFloat() * 4f;

            cluster.add(new Cloud(centerX + dx, centerY + dy, centerZ + dz, size));
        }

        return cluster;
    }
}
