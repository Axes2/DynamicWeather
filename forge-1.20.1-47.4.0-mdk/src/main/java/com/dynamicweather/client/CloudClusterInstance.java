package com.dynamicweather.client;

import java.util.List;

public class CloudClusterInstance {

    private final List<Cloud> clouds;
    private final float baseX, baseY, baseZ;
    private float offsetX = 0f;
    private float offsetZ = 0f;
    private float opacity = 1.0f; // default fully opaque
    private float colorMultiplier = 1.0f; // default: fully white
    private final float angleOffsetDeg;

    private final CloudType type;

    public CloudType getType() {
        return type;
    }

    public void setColorMultiplier(float value) {
        this.colorMultiplier = value;
    }


    public float getColorMultiplier() {
        return this.colorMultiplier;
    }

    public void setOpacity(float value) {
        this.opacity = value;
    }





    private int age = 0;
    private int maxLifetime = Integer.MAX_VALUE;

    public CloudClusterInstance(CloudType type, List<Cloud> clouds, float angleOffsetDeg, float baseX, float baseY, float baseZ)
    {

        this.clouds = clouds;
        this.angleOffsetDeg = angleOffsetDeg;
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;
        this.type = type;

    }

    public void fadeOutNow() {
        this.age = getLifetime() - 20; // Finish in ~1 second (20 ticks)
    }


    public List<Cloud> getClouds() {
        return clouds;
    }

    private float cloudSize = 6.0f;

    public void setSize(float size) {
        this.cloudSize = size;
    }

    public float getSize() {
        return this.cloudSize;
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
        // Apply angle offset to wind direction (for slight cloud orientation variation)
        float angleRad = (float) Math.toRadians(angleOffsetDeg);
        float windX = globalWindX * (float) Math.cos(angleRad) - globalWindZ * (float) Math.sin(angleRad);
        float windZ = globalWindX * (float) Math.sin(angleRad) + globalWindZ * (float) Math.cos(angleRad);

        // Apply consistent per-tick drift (no accumulation bug)
        float velocityX = windX * windSpeed;
        float velocityZ = windZ * windSpeed;

        offsetX += velocityX * deltaTicks;
        offsetZ += velocityZ * deltaTicks;
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

    public void applyMotion(float dx, float dz) {
        this.offsetX += dx;
        this.offsetZ += dz;
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

    public static CloudClusterInstance generateClusterAt(CloudType type, float x, float y, float z) {
        List<Cloud> puff = CloudCluster.generateCluster(type, 0, 0, 0);
        float angleOffset = (float) (Math.random() * 10f - 5f);
        CloudClusterInstance instance = new CloudClusterInstance(type, puff, angleOffset, x, y, z);
        instance.setLifetime(CloudFieldManager.computeClusterLifetime());

        return instance;
    }

}
