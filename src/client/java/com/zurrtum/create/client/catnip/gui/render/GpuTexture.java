package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;

public record GpuTexture(int width, int height, com.mojang.blaze3d.textures.GpuTexture texture,
                         GpuTextureView textureView, com.mojang.blaze3d.textures.GpuTexture depthTexture,
                         GpuTextureView depthTextureView) {
    public static GpuTexture create(int size) {
        return create(size, size, 1);
    }

    public static GpuTexture create(int width, int height) {
        return create(width, height, 1);
    }

    public static GpuTexture create(int width, int height, int factor) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        com.mojang.blaze3d.textures.GpuTexture texture = gpuDevice.createTexture(
            () -> "UI Item Transform texture",
            12,
            TextureFormat.RGBA8,
            width * factor,
            height * factor,
            1,
            1
        );
        GpuTextureView textureView = gpuDevice.createTextureView(texture);
        com.mojang.blaze3d.textures.GpuTexture depthTexture = gpuDevice.createTexture(
            () -> "UI Item Transform depth texture",
            8,
            TextureFormat.DEPTH32,
            texture.getWidth(0),
            texture.getHeight(0),
            1,
            1
        );
        GpuTextureView depthTextureView = gpuDevice.createTextureView(depthTexture);
        return new GpuTexture(width, height, texture, textureView, depthTexture, depthTextureView);
    }

    public void prepare(CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(texture, 0, depthTexture, 1.0F);
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(width, height), ProjectionType.ORTHOGRAPHIC);
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
