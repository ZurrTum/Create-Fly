package com.zurrtum.create.client.content.fluids.pipes;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.fluid.FluidRenderer;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class TransparentStraightPipeRenderer extends SafeBlockEntityRenderer<StraightPipeBlockEntity> {

    public TransparentStraightPipeRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(StraightPipeBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        FluidTransportBehaviour pipe = be.getBehaviour(FluidTransportBehaviour.TYPE);
        if (pipe == null)
            return;

        for (Direction side : Iterate.directions) {

            Flow flow = pipe.getFlow(side);
            if (flow == null)
                continue;
            FluidStack fluidStack = flow.fluid;
            if (fluidStack.isEmpty())
                continue;
            LerpedFloat progress = flow.progress;
            if (progress == null)
                continue;

            float value = progress.getValue(partialTicks);
            boolean inbound = flow.inbound;
            if (value == 1) {
                if (inbound) {
                    Flow opposite = pipe.getFlow(side.getOpposite());
                    if (opposite == null)
                        value -= 1e-6f;
                } else {
                    FluidTransportBehaviour adjacent = BlockEntityBehaviour.get(
                        be.getWorld(),
                        be.getPos().offset(side),
                        FluidTransportBehaviour.TYPE
                    );
                    if (adjacent == null)
                        value -= 1e-6f;
                    else {
                        Flow other = adjacent.getFlow(side.getOpposite());
                        if (other == null || !other.inbound && !other.complete)
                            value -= 1e-6f;
                    }
                }
            }

            FluidRenderer.renderFluidStream(fluidStack, side, 3 / 16f, value, inbound, buffer, ms, light);
        }

    }

}
