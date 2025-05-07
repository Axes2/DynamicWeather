package com.dynamicweather.client;

import java.util.List;

public class CloudClusterInstance {

    private final List<Cloud> clouds;
    private final float baseX, baseY, baseZ;
    private float offsetX = 0f;
    private float offsetZ = 0f;
    private final float angleOffsetDeg;

    private int age = 0;
    private int maxLifetime = Integer.MAX_VALUE;

    public CloudClusterInstance(List<Cloud> clouds, float angleOffsetDeg, float baseX, float baseY, float baseZ) {
        this.clouds = clouds;
        this.angleOffsetDeg = angleOffsetDeg;
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;
    }

    public void fadeOutNow() {
        this.age = getLifetime() - 20; // Finish in ~1 second (20 ticks)
    }


    public List<Cloud> getClouds() {
        return clouds;
    }

    public int getAge() {
        return age;
    }


    public float getBaseX() {
        return baseX;
    }

    public float getBaseY() {
        return baseY;
    }

    public float getBaseZ() {
        return baseZ;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void update(float windSpeed, float deltaTicks, float globalWindX, float globalWindZ) {
        float angleRad = (float) Math.toRadians(angleOffsetDeg);
        float dx = globalWindX * (float) Math.cos(angleRad) - globalWindZ * (float) Math.sin(angleRad);
        float dz = globalWindX * (float) Math.sin(angleRad) + globalWindZ * (float) Math.cos(angleRad);

        offsetX += dx * windSpeed * deltaTicks;
        offsetZ += dz * windSpeed * deltaTicks;
    }

    public void setLifetime(int ticks) {
        this.maxLifetime = ticks;
    }

    public int getLifetime() {
        return this.maxLifetime;
    }


    public void tickLifetime() {
        this.age++;
    }

    public boolean isExpired() {
        return age >= maxLifetime;
    }

    public float getOpacity() {
        float fadeTime = 120f; // smoother fade in/out
        if (age < fadeTime) {
            return age / fadeTime;
        } else if (age > maxLifetime - fadeTime) {
            return Math.max(0f, (maxLifetime - age) / fadeTime);
        } else {
            return 1f;
        }
    }

    public static CloudClusterInstance generateClusterAt(float x, float y, float z) {
        List<Cloud> puff = CloudCluster.generateCumulus(0, 0, 0);
        float angleOffset = (float) (Math.random() * 10f - 5f);
        CloudClusterInstance instance = new CloudClusterInstance(puff, angleOffset, x, y, z);
        instance.setLifetime(CloudFieldManager.computeClusterLifetime());
        puff.forEach(CloudManager::addCloud);
        return instance;
    }
}
