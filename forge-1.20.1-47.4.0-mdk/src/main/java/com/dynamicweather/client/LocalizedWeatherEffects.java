package com.dynamicweather.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;


import java.util.*;

@Mod.EventBusSubscriber(modid = "dynamicweather", value = Dist.CLIENT)
public class LocalizedWeatherEffects {

    private static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("dynamicweather", "textures/particles/rain_drop.png");

    private static final List<RainDrop> activeRainDrops = new ArrayList<>();
    private static final int MAX_DROPS = 9000;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void applyStormFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Vec3 playerPos = mc.player.position();
        float intensity = StormManager.getStormIntensityAt(playerPos);
        if (intensity <= 0f) return;

        float stormGray = 0.25f;
        event.setRed(lerp(event.getRed(), stormGray, intensity));
        event.setGreen(lerp(event.getGreen(), stormGray, intensity));
        event.setBlue(lerp(event.getBlue(), stormGray, intensity));
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static int spawnCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.isPaused() || player == null) return;

        Vec3 playerPos = player.position();

        if (!StormManager.isPlayerInStorm(player)) {
            activeRainDrops.clear();
            return;
        }

        // Step 1: Determine target count based on storm strength
        float intensity = StormManager.getStormIntensityAt(playerPos);
        int targetDrops = (int)(MAX_DROPS * intensity);

        // Step 2: Update existing raindrops
        activeRainDrops.removeIf(RainDrop::isDead);
        for (RainDrop drop : activeRainDrops) {
            drop.tick();
        }

        // Step 3: Spawn throttling to prevent player-follow illusion
        spawnCooldown++;
        if (spawnCooldown < 1) return; // every ~3 ticks
        spawnCooldown = 0;

        // Step 4: Spawn new drops around player (but drops are world-tied)
        while (activeRainDrops.size() < targetDrops) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 6 + random.nextDouble() * 6; // wider spread (8–18 blocks)
            double x = playerPos.x + Math.cos(angle) * radius;
            double z = playerPos.z + Math.sin(angle) * radius;
            double y = playerPos.y + 10 + random.nextDouble() * 5;

            Vec3 pos = new Vec3(x, y, z);

            // Optional: skip if under tree, cave, or roof
            if (!mc.level.canSeeSkyFromBelowWater(BlockPos.containing(pos))) continue;

            // Wind-aware velocity with slight randomness
            Vec3 wind = StormManager.getWindAt(pos);
            Vec3 vel = wind.normalize().scale(0.4) // Stronger wind tilt
                    .add(0, -1.45 - random.nextDouble() * 0.2, 0) // Faster vertical fall
                    .add((random.nextDouble() - 0.5) * 0.025, 0, (random.nextDouble() - 0.5) * 0.025); // Slight spray

            float maxAge = 50 + random.nextFloat() * 30; // raindrops live ~2.5–4 seconds
            activeRainDrops.add(new RainDrop(pos, vel, maxAge));
        }
    }




    @SubscribeEvent
    public static void renderRain(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (!StormManager.isPlayerInStorm(player)) return;

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RAIN_TEXTURE);
        RenderSystem.defaultBlendFunc();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (RainDrop drop : activeRainDrops) {
            float alpha = drop.getAlpha();
            Vec3 relPos = drop.position.subtract(camPos);

            float width = 0.06f + random.nextFloat() * 0.06f;  // 0.06–0.12 wide
            float height = 0.6f + random.nextFloat() * 0.4f;   // 0.6–1.0 tall

            buffer.vertex(relPos.x - width, relPos.y, relPos.z).uv(0, 0).color(1f, 1f, 1f, alpha).endVertex();
            buffer.vertex(relPos.x + width, relPos.y, relPos.z).uv(1, 0).color(1f, 1f, 1f, alpha).endVertex();
            buffer.vertex(relPos.x + width, relPos.y + height, relPos.z).uv(1, 1).color(1f, 1f, 1f, alpha).endVertex();
            buffer.vertex(relPos.x - width, relPos.y + height, relPos.z).uv(0, 1).color(1f, 1f, 1f, alpha).endVertex();
        }


        tessellator.end();
        RenderSystem.disableBlend();
    }



}
