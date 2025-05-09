package com.dynamicweather.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StormManager {
    private static final List<StormCell> storms = new ArrayList<>();

    public static void tickStorms() {
        Iterator<StormCell> it = storms.iterator();
        while (it.hasNext()) {
            StormCell storm = it.next();
            storm.tick();
            if (storm.isExpired()) {
                it.remove();
            }
        }
    }

    public static void spawnStorm(StormCell storm) {
        storms.add(storm);
    }

    public static void clear() {
        storms.clear();
    }

    public static List<StormCell> getStorms() {
        return storms;
    }


    public static boolean isPlayerInStorm(LocalPlayer player) {
        if (player == null) return false;

        Vec3 playerPos = player.position();

        for (StormCell storm : storms) {
            Vec3 stormPos = storm.getPosition();
            double dx = playerPos.x - stormPos.x;
            double dz = playerPos.z - stormPos.z;
            double horizontalDistSq = dx * dx + dz * dz;

            if (horizontalDistSq <= storm.getRadius() * storm.getRadius()) {
                return true;
            }
        }

        return false;
    }

    public static float getStormIntensityAt(Vec3 pos) {
        StormCell nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (StormCell storm : storms) {
            Vec3 stormPos = storm.getPosition();
            double dx = pos.x - stormPos.x;
            double dz = pos.z - stormPos.z;
            double distSq = dx * dx + dz * dz;
            double radiusSq = storm.getRadius() * storm.getRadius();

            if (distSq < radiusSq && distSq < nearestDistSq) {
                nearest = storm;
                nearestDistSq = distSq;
            }
        }

        if (nearest != null) {
            double dist = Math.sqrt(nearestDistSq);
            double t = dist / nearest.getRadius(); // normalized 0–1
            float fade = 1.0f - (float) t;
            return fade * nearest.getIntensity();
        }

        return 0f;
    }


    public static Vec3 getWindAt(Vec3 pos) {
        StormCell nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (StormCell storm : storms) {
            double distSq = pos.distanceToSqr(storm.getPosition());
            if (distSq < storm.getRadius() * storm.getRadius() && distSq < nearestDistSq) {
                nearest = storm;
                nearestDistSq = distSq;
            }
        }

        if (nearest != null) {
            return nearest.getWindVectorAt(pos);
        }

        // Fall back to global wind
        Vector2f globalDir = CloudFieldManager.globalWindDirection;
        return new Vec3(globalDir.x, -0.8f, globalDir.y).normalize().scale(CloudFieldManager.globalWindSpeed);
    }


}
