package com.zurrtum.create.client.content.trains.track;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;

public abstract class TrackBlockRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
    public RenderType layer;

    public abstract void transform(PoseStack matrices);

    public void render(PoseStack matrices, SubmitNodeCollector queue) {
        matrices.pushPose();
        transform(matrices);
        queue.submitCustomGeometry(matrices, layer, this);
        matrices.popPose();
    }

    public void render(PoseStack matrices, MultiBufferSource buffer) {
        matrices.pushPose();
        transform(matrices);
        render(matrices.last(), buffer.getBuffer(layer));
        matrices.popPose();
    }
}
