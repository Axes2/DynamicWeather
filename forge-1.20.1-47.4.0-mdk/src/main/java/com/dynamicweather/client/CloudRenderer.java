package com.dynamicweather.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;


import java.util.List;

@Mod.EventBusSubscriber(modid = "dynamicweather", value = Dist.CLIENT)
public class CloudRenderer {


    private static final ResourceLocation CLOUD_TEXTURE = new ResourceLocation("dynamicweather", "textures/particles/cloud.png");

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        // Adjusted cloud management call with spawn and count control
        StormManager.tickStorms();

        CloudFieldManager.updateCloudsIfNeeded(); // Adjust spawn count and lifetime here

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(CLOUD_TEXTURE));

        poseStack.pushPose();
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        Vec3 cameraPos = camera.getPosition();
        Vec3 lookVec = new Vec3(camera.getLookVector());


        for (CloudClusterInstance cluster : List.copyOf(CloudFieldManager.getClusters())) {
            float baseX = cluster.getBaseX() + cluster.getOffsetX();
            float baseY = cluster.getBaseY();
            float baseZ = cluster.getBaseZ() + cluster.getOffsetZ();
            float alpha = cluster.getOpacity();

            // Skip clusters that are too far or out of view
            Vec3 toCluster = new Vec3(baseX, baseY, baseZ).subtract(cameraPos);
            double distance = toCluster.length();

            // Distance check (add 20% buffer)
            if (distance > mc.options.renderDistance().get() * 16f * 1.2f) continue;

            // Angle check (70 degrees cone)
            double angleCos = lookVec.dot(toCluster.normalize());
            if (angleCos < Math.cos(Math.toRadians(90))) continue;

            // Draw this cluster
            for (Cloud cloud : cluster.getClouds()) {
                float worldX = baseX + cloud.x;
                float worldY = baseY + cloud.y;
                float worldZ = baseZ + cloud.z;

                renderCube(builder, matrix, normal, worldX, worldY, worldZ, cluster.getSize(), alpha, cluster.getColorMultiplier());

            }
        }


        poseStack.popPose();
        buffer.endBatch();
    }

    private static void renderCube(VertexConsumer builder, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float size, float alpha, float tint) {
        float r = tint, g = tint, b = tint, a = alpha;
        float x2 = x + size;
        float y2 = y + size;
        float z2 = z + size;

        // Front (Z+)
        builder.vertex(matrix, x, y, z2).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y, z2).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x, y2, z2).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, 1).endVertex();

        // Back (Z-)
        builder.vertex(matrix, x2, y, z).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x, y, z).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x, y2, z).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();

        // Left (X-)
        builder.vertex(matrix, x, y, z).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x, y, z2).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x, y2, z2).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x, y2, z).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, -1, 0, 0).endVertex();

        // Right (X+)
        builder.vertex(matrix, x2, y, z2).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y, z).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 1, 0, 0).endVertex();

        // Top (Y+)
        builder.vertex(matrix, x, y2, z2).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x, y2, z).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 1, 0).endVertex();

        // Bottom (Y-)
        builder.vertex(matrix, x, y, z).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y, z).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y, z2).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x, y, z2).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, -1, 0).endVertex();
    }

}
