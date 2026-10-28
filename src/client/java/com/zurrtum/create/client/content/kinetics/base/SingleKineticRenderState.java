package com.zurrtum.create.client.content.kinetics.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.UnknownNullability;

public class SingleKineticRenderState extends BlockEntityRenderState {
    public @UnknownNullability SuperByteBufferRenderState model;
    public @UnknownNullability Axis axis;
    public float angle;

    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        if (angle != 0) {
            matrices.rotateCentered(axis, Mth.DEG_TO_RAD * angle);
        }
        model.submit(matrices, queue);
    }
}
