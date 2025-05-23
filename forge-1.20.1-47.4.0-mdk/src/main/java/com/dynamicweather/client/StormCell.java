package com.dynamicweather.client;

import com.dynamicweather.client.CloudClusterInstance;
import com.dynamicweather.client.CloudFieldManager;
import com.dynamicweather.client.CloudType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StormCell {
    private Vec3 position;
    private final Vec3 motion;
    private final float radius;
    private int lifetime;
    private final List<CloudClusterInstance> clouds = new ArrayList<>();

    private static final Random random = new Random();
    private final float intensity; // 0.0 to 1.0

    public Vec3 getMotion() {
        return this.motion;
    }



    public StormCell(Vec3 position, Vec3 motion, float radius, int lifetime, float intensity) {
        this.position = position;
        this.motion = motion;
        this.radius = radius;
        this.lifetime = lifetime;
        this.intensity = intensity;
        generateRadialStorm();
    }

    public Vec3 getWindVectorAt(Vec3 pos) {
        Vec3 center = this.position;

        double dx = pos.x - center.x;
        double dz = pos.z - center.z;
        double distSq = dx * dx + dz * dz;

        if (distSq > radius * radius) return Vec3.ZERO;

        double dist = Math.sqrt(distSq);
        double t = dist / radius;

        // Handle degenerate center
        Vec3 radialOutward;
        if (dist < 0.001) {
            radialOutward = new Vec3(0, 0, 0); // no horizontal component
        } else {
            radialOutward = new Vec3(dx, 0, dz).normalize();
        }

        Vec3 vertical = new Vec3(0, -1, 0);
        Vec3 blended = vertical.scale(1 - t).add(radialOutward.scale(t));

        double speed = (0.05 + (1.0 - t) * 0.15) * intensity;
        return blended.normalize().scale(speed);
    }






    public Vec3 getPosition() {
        return this.position;
    }

    public float getRadius() {
        return this.radius;
    }
    public float getIntensity() {
        return intensity;
    }




    private void generateCloudLayer(float y, float radius, int count, float alpha, float tint) {
        generateCloudLayer(y, radius, count, alpha, tint, 0f, 0f);
    }

    private void generateCloudLayer(float y, float radius, int count, float alpha, float tint, float offsetX, float offsetZ) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double r = Math.random() * radius;
            double x = position.x + Math.cos(angle) * r + offsetX;
            double z = position.z + Math.sin(angle) * r + offsetZ;

            CloudClusterInstance cluster = CloudClusterInstance.generateClusterAt(
                    CloudType.CUMULONIMBUS,
                    (float) x, y, (float) z
            );

            cluster.setLifetime(this.lifetime);
            cluster.setColorMultiplier(tint);
            cluster.setOpacity(alpha);

            // 💥 Add this to control visual chunkiness:
            cluster.setSize(10f + random.nextFloat() * 8f); // 10–18 block cubes

            clouds.add(cluster);
            CloudFieldManager.queueCluster(cluster);
        }
    }



    public void tick() {
        // Move storm position
        this.position = this.position.add(motion);
        this.lifetime--;

        for (CloudClusterInstance cloud : clouds) {
            cloud.tickLifetime(); // just age them
        }

        CloudFieldManager.flushPendingClusters();

    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public void generateRadialStorm() {
        int layers = 3;
        float baseY = (float) position.y;

        float maxCloudSpread = radius * 1.03f; // tiny buffer for fade-out edge
        int totalArea = (int)(Math.PI * radius * radius);

        for (int i = 0; i < layers; i++) {
            float y = baseY + i * 4 + random.nextFloat() * 2f;

            int cloudCount = (int)(totalArea / 300f); // about 1 cloud per 300 blocks — tweak as needed

            for (int j = 0; j < cloudCount; j++) {
                double angle = random.nextDouble() * Math.PI * 2;

                // EVEN distribution all the way to radius
                double r = random.nextDouble() * maxCloudSpread;

                float x = (float)(position.x + Math.cos(angle) * r + (random.nextFloat() - 0.5f) * 4f);
                float z = (float)(position.z + Math.sin(angle) * r + (random.nextFloat() - 0.5f) * 4f);

                float distNorm = (float)(r / radius); // 0 to ~1
                float tint = 0.4f + 0.3f * (1.0f - distNorm);
                float alpha = 0.4f + 0.3f * (1.0f - distNorm);

                CloudClusterInstance cluster = CloudClusterInstance.generateClusterAt(
                        CloudType.CUMULONIMBUS, x, y, z
                );

                cluster.setLifetime(this.lifetime);
                cluster.setColorMultiplier(tint);
                cluster.setOpacity(alpha);
                cluster.setSize(8f + random.nextFloat() * 10f);

                clouds.add(cluster);
                CloudFieldManager.queueCluster(cluster);
            }
        }

        // Optional outer perimeter enhancement
        int edgeCount = (int)(radius * 0.5f);
        float edgeY = baseY + 6f + random.nextFloat() * 2f;

        for (int i = 0; i < edgeCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = radius * (0.95 + random.nextDouble() * 0.08); // close to max radius

            float x = (float)(position.x + Math.cos(angle) * r);
            float z = (float)(position.z + Math.sin(angle) * r);

            CloudClusterInstance edge = CloudClusterInstance.generateClusterAt(
                    CloudType.CUMULONIMBUS, x, edgeY, z
            );
            edge.setLifetime(this.lifetime);
            edge.setOpacity(0.3f + random.nextFloat() * 0.1f);
            edge.setColorMultiplier(0.35f + random.nextFloat() * 0.1f);
            edge.setSize(10f + random.nextFloat() * 8f);

            clouds.add(edge);
            CloudFieldManager.queueCluster(edge);
        }

    }







}




