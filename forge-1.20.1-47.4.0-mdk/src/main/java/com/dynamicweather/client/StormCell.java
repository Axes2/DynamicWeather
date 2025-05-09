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



    public StormCell(Vec3 position, Vec3 motion, float radius, int lifetime, float intensity) {
        this.position = position;
        this.motion = motion;
        this.radius = radius;
        this.lifetime = lifetime;
        this.intensity = intensity;
        generateRadialStorm();
    }

    public Vec3 getWindVectorAt(Vec3 pos) {
        Vec3 stormCenter = this.position;
        Vec3 toTarget = pos.subtract(stormCenter);
        double dist = toTarget.length();

        if (dist > this.radius) return Vec3.ZERO;

        // Normalize distance
        double t = dist / radius;

        // Motion-aligned offset — pulls inflow toward the front (storm leading edge)
        Vec3 frontBias = this.motion.normalize().scale(radius * 0.4);
        Vec3 skewedCenter = stormCenter.add(frontBias); // inflow focus ahead of actual center
        Vec3 toSkewed = pos.subtract(skewedCenter);
        Vec3 inflowDir = toSkewed.normalize().scale(-1); // inward to skewed center

        // Outflow lags behind — apply offset in reverse
        Vec3 rearBias = this.motion.normalize().scale(radius * 0.4);
        Vec3 outflowDir = pos.subtract(stormCenter.subtract(rearBias)).normalize();

        // Intensity-based scaling
        double inflowSpeed = 0.08 * intensity;
        double updraftSpeed = 0.12 * intensity;
        double outflowSpeed = 0.06 * intensity;

        if (t > 0.6) {
            // Inflow zone
            return inflowDir.add(0, -0.1, 0).normalize().scale(inflowSpeed);
        } else if (t > 0.3) {
            // Updraft
            return new Vec3(0, updraftSpeed, 0);
        } else {
            // Outflow, biased behind storm
            return outflowDir.add(0, -0.1, 0).normalize().scale(outflowSpeed);
        }

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
            cloud.applyMotion((float) motion.x, (float) motion.z);
            cloud.tickLifetime(); // ensure fade-out works
        }
        CloudFieldManager.flushPendingClusters();

    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public void generateRadialStorm() {
        int layers = 3;
        float baseY = (float) position.y;

        for (int i = 0; i < layers; i++) {
            float y = baseY + i * 4 + random.nextFloat() * 2f;

            float layerRadius = radius * (0.9f + random.nextFloat() * 0.2f); // varied radius
            int cloudCount = (int) (radius * 0.5f) + 5 + random.nextInt(6); // fewer, less dense

            for (int j = 0; j < cloudCount; j++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = random.nextDouble() * layerRadius;

                // Jittered center
                float x = (float) (position.x + Math.cos(angle) * r + (random.nextFloat() - 0.5f) * 8f);
                float z = (float) (position.z + Math.sin(angle) * r + (random.nextFloat() - 0.5f) * 8f);

                CloudClusterInstance cluster = CloudClusterInstance.generateClusterAt(
                        CloudType.CUMULONIMBUS, x, y, z
                );

                cluster.setLifetime(this.lifetime);

                // Fade edges and randomize tint
                float dist = (float) (r / layerRadius);
                float tint = 0.45f + 0.3f * (1.0f - dist) + (random.nextFloat() * 0.1f);
                float alpha = 0.45f + 0.3f * (1.0f - dist);

                // 🎲 Randomize size more
                cluster.setSize(6f + random.nextFloat() * 12f); // 6–18 block cubes

                cluster.setColorMultiplier(tint);
                cluster.setOpacity(alpha);

                clouds.add(cluster);
                CloudFieldManager.queueCluster(cluster);
            }
        }
    }





}




