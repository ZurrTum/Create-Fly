package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import net.minecraft.client.render.OverlayTexture;

public abstract class ColoredLitOverlayInstance extends ColoredLitInstance {
    public int overlay = OverlayTexture.DEFAULT_UV;

    public ColoredLitOverlayInstance(InstanceType<? extends ColoredLitOverlayInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public ColoredLitOverlayInstance overlay(int overlay) {
        this.overlay = overlay;
        return this;
    }
}
