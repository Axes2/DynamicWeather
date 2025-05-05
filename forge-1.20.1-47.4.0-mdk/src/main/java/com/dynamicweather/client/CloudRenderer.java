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

@Mod.EventBusSubscriber(modid = "dynamicweather", value = Dist.CLIENT)
public class CloudRenderer {

    private static final ResourceLocation CLOUD_TEXTURE = new ResourceLocation("dynamicweather", "textures/particles/cloud.png");

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(CLOUD_TEXTURE));

        // Get matrix stack


        // Align to world space
        poseStack.pushPose();
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        // Draw cube at a fixed location

        renderCube(builder, matrix, normal, 100f, 150f, 100f, 10f);

        poseStack.popPose();
        buffer.endBatch();
    }

    private static void renderCube(VertexConsumer builder, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float size) {
        float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;
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
