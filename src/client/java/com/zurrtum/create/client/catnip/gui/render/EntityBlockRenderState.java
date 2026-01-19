package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record EntityBlockRenderState(
    int id, Matrix3x2f pose, Level world, BlockPos pos, BlockEntity entity, BlockState state, int x0, int y0, int x1, int y1, float scale, float size,
    float xRot, float yRot, float zRot, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public static EntityBlockRenderState create(
        int id,
        GuiGraphics graphics,
        Level world,
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
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
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
            Mth.DEG_TO_RAD * xRot,
            Mth.DEG_TO_RAD * yRot,
            Mth.DEG_TO_RAD * zRot,
            new ScreenRectangle(x, y, (int) size, (int) size).transformMaxBounds(pose)
        );
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
