package com.zurrtum.create.client.api.behaviour.movement;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;

public abstract class MovementRenderState {
    public final BlockPos pos;

    public MovementRenderState(BlockPos pos) {
        this.pos = pos;
    }

    public void transform(PoseStack matrices) {
        matrices.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    public void render(PoseStack matrices, SubmitNodeCollector queue) {
    }
}
