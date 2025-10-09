package com.zurrtum.create.client.foundation.fluid;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class FluidRenderer {
    public static void renderFluidStream(
        Fluid fluid,
        ComponentChanges changes,
        Direction direction,
        float radius,
        float progress,
        boolean inbound,
        VertexConsumer builder,
        MatrixStack.Entry entry,
        int light
    ) {
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        Sprite flowTexture = config.flowing().get();
        Sprite stillTexture = config.still().get();

        int color = config.tint().apply(changes) | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.getDefaultState().getBlockState().getLuminance());
        light = (light & 0xF00000) | luminosity << 4;

        if (inbound)
            direction = direction.getOpposite();

        entry = entry.copy();
        entry.translate(0.5f, 0.5f, 0.5f);
        entry.rotate(RotationAxis.POSITIVE_Y.rotation(MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(direction)));
        entry.rotate(RotationAxis.POSITIVE_X.rotation(MathHelper.RADIANS_PER_DEGREE * (direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)));
        entry.translate(0, -0.5f, 0);

        float hMin = -radius;
        float y = inbound ? 1 : .5f;
        float yMin = y - MathHelper.clamp(progress * .5f, 0, 1);

        for (int i = 0; i < 4; i++) {
            renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, radius, y, radius, builder, entry, light, color, flowTexture);
            entry.rotate(RotationAxis.POSITIVE_Y.rotation(MathHelper.RADIANS_PER_DEGREE * 90));
        }

        if (progress != 1) {
            FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, radius, radius, yMin, builder, entry, light, color, stillTexture);
        }
    }

    public static void renderFlowingTiledFace(
        Direction dir,
        float left,
        float down,
        float right,
        float up,
        float depth,
        VertexConsumer builder,
        MatrixStack.Entry entry,
        int light,
        int color,
        Sprite texture
    ) {
        FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, entry, light, color, texture, 0.5f);
    }
}
