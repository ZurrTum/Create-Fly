package com.zurrtum.create.client.foundation.fluid;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class FluidRenderer {

    public static void renderFluidStream(
        FluidStack fluidStack,
        Direction direction,
        float radius,
        float progress,
        boolean inbound,
        VertexConsumerProvider buffer,
        MatrixStack ms,
        int light
    ) {
        renderFluidStream(fluidStack, direction, radius, progress, inbound, FluidRenderHelper.getFluidBuilder(buffer), ms, light);
    }

    public static void renderFluidStream(
        FluidStack fluidStack,
        Direction direction,
        float radius,
        float progress,
        boolean inbound,
        VertexConsumer builder,
        MatrixStack ms,
        int light
    ) {
        Fluid fluid = fluidStack.getFluid();
        FluidConfig config = AllFluidConfigs.get(fluid);
        if (config == null) {
            return;
        }
        Sprite flowTexture = config.flowing().get();
        Sprite stillTexture = config.still().get();

        int color = config.tint().get() | 0xff000000;
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluid.getDefaultState().getBlockState().getLuminance());
        light = (light & 0xF00000) | luminosity << 4;

        if (inbound)
            direction = direction.getOpposite();

        var msr = TransformStack.of(ms);
        ms.push();
        msr.center().rotateYDegrees(AngleHelper.horizontalAngle(direction))
            .rotateXDegrees(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270).uncenter();
        ms.translate(.5, 0, .5);

        float hMin = -radius;
        float y = inbound ? 1 : .5f;
        float yMin = y - MathHelper.clamp(progress * .5f, 0, 1);

        for (int i = 0; i < 4; i++) {
            ms.push();
            renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, radius, y, radius, builder, ms, light, color, flowTexture);
            ms.pop();
            msr.rotateYDegrees(90);
        }

        if (progress != 1)
            FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, radius, radius, yMin, builder, ms, light, color, stillTexture);

        ms.pop();
    }

    public static void renderFlowingTiledFace(
        Direction dir,
        float left,
        float down,
        float right,
        float up,
        float depth,
        VertexConsumer builder,
        MatrixStack ms,
        int light,
        int color,
        Sprite texture
    ) {
        FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 0.5f);
    }

}
