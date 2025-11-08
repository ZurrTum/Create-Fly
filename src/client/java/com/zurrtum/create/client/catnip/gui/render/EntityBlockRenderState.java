package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record EntityBlockRenderState(
    int id, Matrix3x2f pose, World world, BlockPos pos, BlockEntity entity, BlockState state, int x1, int y1, int x2, int y2, float scale, float size, float xRot,
    float yRot, float zRot, ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public static EntityBlockRenderState create(
        int id,
        DrawContext graphics,
        World world,
        BlockPos pos,
        BlockEntity entity,
        BlockState state,
        int x,
        int y,
        float scale,
        int padding,
        float xRot,
        float yRot,
        float zRot
    ) {
        Matrix3x2f pose = new Matrix3x2f(graphics.getMatrices());
        scale = scale * 16;
        float size = scale + padding;
        return new EntityBlockRenderState(
            id,
            pose,
            world,
            pos,
            entity,
            state,
            x,
            y,
            (int) (x + size),
            (int) (y + size),
            scale,
            size,
            MathHelper.RADIANS_PER_DEGREE * xRot,
            MathHelper.RADIANS_PER_DEGREE * yRot,
            MathHelper.RADIANS_PER_DEGREE * zRot,
            new ScreenRect(x, y, (int) size, (int) size).transformEachVertex(pose)
        );
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return null;
    }
}
