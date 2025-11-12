package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class ColoredLitOverlayInstance extends ColoredLitInstance {
    public int overlay = OverlayTexture.NO_OVERLAY;

    public ColoredLitOverlayInstance(InstanceType<? extends ColoredLitOverlayInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public ColoredLitOverlayInstance overlay(int overlay) {
        this.overlay = overlay;
        return this;
    }
}
