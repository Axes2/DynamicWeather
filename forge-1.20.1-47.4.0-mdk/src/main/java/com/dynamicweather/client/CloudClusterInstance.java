package com.dynamicweather.client;

import java.util.List;

public class CloudClusterInstance {
    private final List<Cloud> clouds;
    private final float baseX, baseY, baseZ;
    private float offsetX = 0f;
    private float offsetZ = 0f;
    private final float angleOffsetDeg;

    public CloudClusterInstance(List<Cloud> clouds, float angleOffsetDeg, float baseX, float baseY, float baseZ) {
        this.clouds = clouds;
        this.angleOffsetDeg = angleOffsetDeg;
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;
    }

    public List<Cloud> getClouds() {
        return clouds;
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
}
