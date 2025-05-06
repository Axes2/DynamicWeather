package com.dynamicweather.client;

import com.dynamicweather.client.Cloud;

import java.util.List;



public class CloudClusterInstance {

    private final List<Cloud> clouds;
    private final float offsetAngle;
    private float accumulatedTime = 0f;

    public CloudClusterInstance(List<Cloud> baseClouds, float offsetAngleDegrees) {
        this.clouds = baseClouds;
        this.offsetAngle = (float) Math.toRadians(offsetAngleDegrees);  // degrees → radians
    }

    public void update(float windSpeed, float deltaTime, float globalDirX, float globalDirZ) {
        accumulatedTime += deltaTime;

        float angle = (float) Math.atan2(globalDirZ, globalDirX) + offsetAngle;

        float dx = (float) Math.cos(angle) * windSpeed * deltaTime;
        float dz = (float) Math.sin(angle) * windSpeed * deltaTime;

        for (Cloud cloud : clouds) {
            cloud.x += dx;
            cloud.z += dz;
        }
    }

    public List<Cloud> getClouds() {
        return clouds;
    }
}
