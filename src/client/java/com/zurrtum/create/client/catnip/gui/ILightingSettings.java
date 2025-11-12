package com.zurrtum.create.client.catnip.gui;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.Minecraft;

public interface ILightingSettings {

    void applyLighting();

    static final ILightingSettings DEFAULT_3D = () -> Minecraft.getInstance().gameRenderer.getLighting()
        .setupFor(Lighting.Entry.ITEMS_3D);
    static final ILightingSettings DEFAULT_FLAT = () -> Minecraft.getInstance().gameRenderer.getLighting()
        .setupFor(Lighting.Entry.ITEMS_FLAT);

}
