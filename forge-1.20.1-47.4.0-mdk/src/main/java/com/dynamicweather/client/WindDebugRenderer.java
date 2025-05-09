package com.dynamicweather.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = "dynamicweather", value = Dist.CLIENT)
public class WindDebugRenderer {

    private static final List<WindTracer> tracers = new ArrayList<>();
    private static boolean enabled = false;

    public static void toggle() {
        enabled = !enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 pos = mc.player.position().add(0, 1.5, 0);
        float intensity = StormManager.getStormIntensityAt(pos);
        if (intensity <= 0f) return; // skip spawning tracer

        Vec3 wind = StormManager.getWindAt(pos);

        tracers.add(new WindTracer(pos, wind.scale(0.5), 20)); // Short-lived tracer

        Iterator<WindTracer> it = tracers.iterator();
        while (it.hasNext()) {
            WindTracer tracer = it.next();
            tracer.tick();
            if (tracer.isDead()) it.remove();
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (!enabled || event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 camPos = cam.getPosition();

        PoseStack stack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = stack.last().pose();

        for (WindTracer tracer : tracers) {
            float alpha = tracer.getAlpha();

            Vec3 from = tracer.position.subtract(camPos);
            Vec3 dir = tracer.velocity.normalize();
            Vec3 to = from.add(dir.scale(0.8));

            // Compute arrowhead "wings"
            Vec3 sideways = dir.cross(new Vec3(0, 1, 0)).normalize().scale(0.15);
            Vec3 tail = from.subtract(dir.scale(0.6));
            Vec3 leftWing = tail.add(sideways);
            Vec3 rightWing = tail.subtract(sideways);

            // Wind speed-based brightness (clamped for readability)
            float speed = (float) tracer.velocity.length();
            float brightness = Mth.clamp(speed * 4f, 0.3f, 1.0f);
            float r = 0.2f;
            float g = brightness;
            float b = 1f;

            // Main shaft
            builder.vertex(matrix, (float) tail.x, (float) tail.y, (float) tail.z)
                    .color(r, g, b, alpha)
                    .normal(0, 1, 0).uv(0, 0).endVertex();
            builder.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                    .color(r, g, b, alpha)
                    .normal(0, 1, 0).uv(1, 1).endVertex();

            // Left wing
            builder.vertex(matrix, (float) leftWing.x, (float) leftWing.y, (float) leftWing.z)
                    .color(r, g, b, alpha)
                    .normal(0, 1, 0).uv(0, 0).endVertex();
            builder.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                    .color(r, g, b, alpha)
                    .normal(0, 1, 0).uv(1, 1).endVertex();

            // Right wing
            builder.vertex(matrix, (float) rightWing.x, (float) rightWing.y, (float) rightWing.z)
                    .color(r, g, b, alpha)
                    .normal(0, 1, 0).uv(0, 0).endVertex();
            builder.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                    .color(r, g, b, alpha)
                    .normal(0, 1, 0).uv(1, 1).endVertex();
        }

        buffer.endBatch();
    }

}
