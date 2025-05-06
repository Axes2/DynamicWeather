package com.dynamicweather.client;

import java.util.Random;

public class CloudGenerator {

    private static final Random random = new Random();

//    public static void generateCloudField(float centerX, float centerZ, float radius, float spacing, float coverage) {
//        CloudManager.clear();
//
//        float cloudY = 150f;
//
//        for (float x = centerX - radius; x <= centerX + radius; x += spacing) {
//            for (float z = centerZ - radius; z <= centerZ + radius; z += spacing) {
//
//                float dx = x - centerX;
//                float dz = z - centerZ;
//                if (dx * dx + dz * dz > radius * radius) continue;
//
//                if (random.nextFloat() < coverage) {
//                    // Offset for anti-grid effect
//                    float offsetX = (random.nextFloat() - 0.5f) * spacing * 0.9f;
//                    float offsetZ = (random.nextFloat() - 0.5f) * spacing * 0.9f;
//
//                    // Bias offsets toward cluster centers
//                    float clumpFactor = (float) Math.pow(random.nextFloat(), 2);
//                    offsetX *= clumpFactor;
//                    offsetZ *= clumpFactor;
//
//                    // Small vertical variance
//                    float offsetY = (random.nextFloat() - 0.5f) * 3f;
//
//                    // Random size for puffiness
//                    float cloudSize = 6f + random.nextFloat() * 6f;
//
//                    CloudManager.addCloud(new Cloud(x + offsetX, cloudY + offsetY, z + offsetZ, cloudSize));
//                }
//            }
//        }
//    }
}
