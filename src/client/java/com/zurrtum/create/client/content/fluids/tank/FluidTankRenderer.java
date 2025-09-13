package com.zurrtum.create.client.content.fluids.tank;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class FluidTankRenderer extends SafeBlockEntityRenderer<FluidTankBlockEntity> {

    public FluidTankRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(FluidTankBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (!be.isController())
            return;
        if (!be.window) {
            if (be.boiler.isActive())
                renderAsBoiler(be, partialTicks, ms, buffer, light, overlay);
            return;
        }

        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null)
            return;

        float capHeight = 1 / 4f;
        float tankHullWidth = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(partialTicks);
        if (level < 1 / (512f * totalHeight))
            return;
        float clampedLevel = MathHelper.clamp(level * totalHeight, 0, totalHeight);

        FluidTank tank = be.getTankInventory();
        FluidStack fluidStack = tank.getFluid();

        if (fluidStack.isEmpty())
            return;

        //TODO
        boolean top = false;//fluidStack.getFluid()
        //			.getFluidType()
        //            .isLighterThanAir();

        float xMin = tankHullWidth;
        float xMax = xMin + be.getWidth() - 2 * tankHullWidth;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;

        if (top) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }

        float zMin = tankHullWidth;
        float zMax = zMin + be.getWidth() - 2 * tankHullWidth;

        ms.push();
        ms.translate(0, clampedLevel - totalHeight, 0);
        FluidRenderHelper.renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, false, true);
        ms.pop();
    }

    protected void renderAsBoiler(
        FluidTankBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        BlockState blockState = be.getCachedState();
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getCutout());
        ms.push();
        var msr = TransformStack.of(ms);
        msr.translate(be.getWidth() / 2f, 0.5, be.getWidth() / 2f);

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        float progress = be.boiler.gauge.getValue(partialTicks);

        for (Direction d : Iterate.horizontalDirections) {
            if (be.boiler.occludedDirections[d.getHorizontalQuarterTurns()])
                continue;
            ms.push();
            float yRot = -d.getPositiveHorizontalDegrees() - 90;
            CachedBuffers.partial(AllPartialModels.BOILER_GAUGE, blockState).rotateYDegrees(yRot).uncenter()
                .translate(be.getWidth() / 2f - 6 / 16f, 0, 0).light(light).renderInto(ms, vb);
            CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState).rotateYDegrees(yRot).uncenter()
                .translate(be.getWidth() / 2f - 6 / 16f, 0, 0).translate(0, dialPivotY, dialPivotZ).rotateXDegrees(-145 * progress + 90)
                .translate(0, -dialPivotY, -dialPivotZ).light(light).renderInto(ms, vb);
            ms.pop();
        }

        ms.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(/*FluidTankBlockEntity be*/) {
        //TODO
        //        return be.isController();
        return true;
    }

}
