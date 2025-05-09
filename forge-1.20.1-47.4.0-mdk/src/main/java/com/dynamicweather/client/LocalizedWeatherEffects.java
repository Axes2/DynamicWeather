package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dynamicweather", value = Dist.CLIENT)
public class LocalizedWeatherEffects {

    @SubscribeEvent
    public static void applyStormFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Vec3 playerPos = mc.player.position();
        float intensity = StormManager.getStormIntensityAt(playerPos);
        if (intensity <= 0f) return;

        // Blend current sky color toward storm gray
        float stormGray = 0.25f;
        event.setRed(lerp(event.getRed(), stormGray, intensity));
        event.setGreen(lerp(event.getGreen(), stormGray, intensity));
        event.setBlue(lerp(event.getBlue(), stormGray, intensity));
    }


    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static StormCell getNearestStorm(Vec3 playerPos) {
        StormCell nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (StormCell storm : StormManager.getStorms()) {
            double distSq = playerPos.distanceToSqr(storm.getPosition());
            if (distSq < storm.getRadius() * storm.getRadius() && distSq < nearestDistSq) {
                nearest = storm;
                nearestDistSq = distSq;
            }
        }

        return nearest;
    }
}
