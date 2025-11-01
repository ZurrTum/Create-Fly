package com.zurrtum.create.client.api.behaviour.movement;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

public abstract class MovementRenderState {
    public final BlockPos pos;

    public MovementRenderState(BlockPos pos) {
        this.pos = pos;
    }

    public void transform(MatrixStack matrices) {
        matrices.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
    }
}
