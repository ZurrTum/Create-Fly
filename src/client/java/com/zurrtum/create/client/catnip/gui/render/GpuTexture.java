package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;

record GpuTexture(
    int size, com.mojang.blaze3d.textures.GpuTexture texture, GpuTextureView textureView, com.mojang.blaze3d.textures.GpuTexture depthTexture,
    GpuTextureView depthTextureView
) {
    public static GpuTexture create(int size) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        com.mojang.blaze3d.textures.GpuTexture texture = gpuDevice.createTexture(
            () -> "UI Item Transform texture",
            12,
            TextureFormat.RGBA8,
            size,
            size,
            1,
            1
        );
        texture.setTextureFilter(FilterMode.NEAREST, false);
        GpuTextureView textureView = gpuDevice.createTextureView(texture);
        com.mojang.blaze3d.textures.GpuTexture depthTexture = gpuDevice.createTexture(
            () -> "UI Item Transform depth texture",
            8,
            TextureFormat.DEPTH32,
            size,
            size,
            1,
            1
        );
        GpuTextureView depthTextureView = gpuDevice.createTextureView(depthTexture);
        return new GpuTexture(size, texture, textureView, depthTexture, depthTextureView);
    }

    public void prepare() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(texture, 0, depthTexture, 1.0F);
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
