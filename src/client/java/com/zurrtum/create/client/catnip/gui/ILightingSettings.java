package com.zurrtum.create.client.catnip.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;

public interface ILightingSettings {

    void applyLighting();

    static final ILightingSettings DEFAULT_3D = () -> MinecraftClient.getInstance().gameRenderer.getDiffuseLighting()
        .setShaderLights(DiffuseLighting.Type.ITEMS_3D);
    static final ILightingSettings DEFAULT_FLAT = () -> MinecraftClient.getInstance().gameRenderer.getDiffuseLighting()
        .setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);

}
