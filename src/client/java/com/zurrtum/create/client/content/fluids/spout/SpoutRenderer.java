package com.zurrtum.create.client.content.fluids.spout;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class SpoutRenderer extends SafeBlockEntityRenderer<SpoutBlockEntity> {

    public SpoutRenderer(BlockEntityRendererFactory.Context context) {
    }

    static final PartialModel[] BITS = {AllPartialModels.SPOUT_TOP, AllPartialModels.SPOUT_MIDDLE, AllPartialModels.SPOUT_BOTTOM};

    @Override
    protected void renderSafe(SpoutBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        SmartFluidTankBehaviour tank = be.tank;
        if (tank == null)
            return;

        TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        float level = primaryTank.getFluidLevel().getValue(partialTicks);

        if (!fluidStack.isEmpty() && level != 0) {
            boolean top = false;//TODO fluidStack.getFluid().getFluidType().isLighterThanAir();

            level = Math.max(level, 0.175f);
            float min = 2.5f / 16f;
            float max = min + (11 / 16f);
            float yOffset = (11 / 16f) * level;

            ms.push();
            if (!top)
                ms.translate(0, yOffset, 0);
            else
                ms.translate(0, max - min, 0);

            FluidRenderHelper.renderFluidBox(fluidStack, min, min - yOffset, min, max, min, max, buffer, ms, light, false, true);

            ms.pop();
        }

        int processingTicks = be.processingTicks;
        float processingPT = processingTicks - partialTicks;
        float processingProgress = 1 - (processingPT - 5) / 10;
        processingProgress = MathHelper.clamp(processingProgress, 0, 1);
        float radius = 0;

        if (!fluidStack.isEmpty() && processingTicks != -1) {
            radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
            Box bb = new Box(0.5, 0.0, 0.5, 0.5, -1.2, 0.5).expand(radius / 32f);
            FluidRenderHelper.renderFluidBox(
                fluidStack,
                (float) bb.minX,
                (float) bb.minY,
                (float) bb.minZ,
                (float) bb.maxX,
                (float) bb.maxY,
                (float) bb.maxZ,
                buffer,
                ms,
                light,
                true,
                true
            );
        }

        float squeeze = radius;
        if (processingPT < 0)
            squeeze = 0;
        else if (processingPT < 2)
            squeeze = MathHelper.lerp(processingPT / 2f, 0, -1);
        else if (processingPT < 10)
            squeeze = -1;

        ms.push();
        for (PartialModel bit : BITS) {
            CachedBuffers.partial(bit, be.getCachedState()).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
            ms.translate(0, -3 * squeeze / 32f, 0);
        }
        ms.pop();

    }

}
