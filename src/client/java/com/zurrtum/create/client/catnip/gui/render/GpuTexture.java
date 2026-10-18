package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;

public record GpuTexture(int width, int height, com.mojang.renderpearl.api.textures.GpuTexture texture,
                         GpuTextureView textureView, com.mojang.renderpearl.api.textures.GpuTexture depthTexture,
                         GpuTextureView depthTextureView) {
    public static GpuTexture create(int size) {
        return create(size, size, 1);
    }

    public static GpuTexture create(int width, int height) {
        return create(width, height, 1);
    }

    public static GpuTexture create(int width, int height, int factor) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        com.mojang.renderpearl.api.textures.GpuTexture texture = gpuDevice.createTexture(
            () -> "UI Item Transform texture",
            13,
            GpuFormat.RGBA8_UNORM,
            width * factor,
            height * factor,
            1,
            1
        );
        GpuTextureView textureView = gpuDevice.createTextureView(texture);
        com.mojang.renderpearl.api.textures.GpuTexture depthTexture = gpuDevice.createTexture(
            () -> "UI Item Transform depth texture",
            9,
            GpuFormat.D32_FLOAT,
            texture.getWidth(0),
            texture.getHeight(0),
            1,
            1
        );
        GpuTextureView depthTextureView = gpuDevice.createTextureView(depthTexture);
        return new GpuTexture(width, height, texture, textureView, depthTexture, depthTextureView);
    }

    public void prepare(Projection projection, ProjectionMatrixBuffer projectionMatrixBuffer) {
        RenderSystem.getDevice().createCommandEncoder()
            .clearColorAndDepthTextures(texture, GuiRenderer.CLEAR_COLOR, depthTexture, 0);
        projection.setupOrtho(-1000.0F, 1000.0F, width, height, true);
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
        RenderSystem.outputColorTextureOverride = textureView;
        RenderSystem.outputDepthTextureOverride = depthTextureView;
    }

    public void clear() {
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
    }

    public void close() {
        texture.close();
        textureView.close();
        depthTexture.close();
        depthTextureView.close();
    }
}
