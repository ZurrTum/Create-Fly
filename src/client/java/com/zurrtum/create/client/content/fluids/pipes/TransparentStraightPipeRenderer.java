package com.zurrtum.create.client.content.fluids.pipes;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.foundation.fluid.FluidRenderer;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TransparentStraightPipeRenderer implements BlockEntityRenderer<StraightPipeBlockEntity, TransparentStraightPipeRenderer.TransparentStraightPipeRenderState> {
    public TransparentStraightPipeRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public TransparentStraightPipeRenderState createRenderState() {
        return new TransparentStraightPipeRenderState();
    }

    @Override
    public void updateRenderState(
        StraightPipeBlockEntity be,
        TransparentStraightPipeRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        FluidTransportBehaviour pipe = be.getBehaviour(FluidTransportBehaviour.TYPE);
        if (pipe == null)
            return;
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = PonderRenderTypes.fluid();
        Direction[] directions = Iterate.directions;
        int size = directions.length;
        state.radius = 3 / 16f;
        state.data = new FluidRenderData[size];
        World world = be.getWorld();
        for (int i = 0; i < size; i++) {
            Direction side = directions[i];
            Flow flow = pipe.getFlow(side);
            if (flow == null) {
                continue;
            }
            FluidStack fluidStack = flow.fluid;
            if (fluidStack.isEmpty()) {
                continue;
            }
            LerpedFloat progress = flow.progress;
            if (progress == null) {
                continue;
            }
            float value = progress.getValue(tickProgress);
            boolean inbound = flow.inbound;
            if (value == 1) {
                if (inbound) {
                    Flow opposite = pipe.getFlow(side.getOpposite());
                    if (opposite == null)
                        value -= 1e-6f;
                } else {
                    FluidTransportBehaviour adjacent = BlockEntityBehaviour.get(world, state.pos.offset(side), FluidTransportBehaviour.TYPE);
                    if (adjacent == null)
                        value -= 1e-6f;
                    else {
                        Flow other = adjacent.getFlow(side.getOpposite());
                        if (other == null || !other.inbound && !other.complete)
                            value -= 1e-6f;
                    }
                }
            }
            state.data[i] = new FluidRenderData(fluidStack.getFluid(), fluidStack.getComponentChanges(), side, value, inbound);
        }
    }

    @Override
    public void render(
        TransparentStraightPipeRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState
    ) {
        if (state.data != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
    }

    public static class TransparentStraightPipeRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float radius;
        public FluidRenderData[] data;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            for (FluidRenderData renderData : data) {
                if (renderData == null) {
                    continue;
                }
                FluidRenderer.renderFluidStream(
                    renderData.fluid,
                    renderData.changes,
                    renderData.side,
                    radius,
                    renderData.value,
                    renderData.inbound,
                    vertexConsumer,
                    matricesEntry,
                    lightmapCoordinates
                );
            }
        }
    }

    public record FluidRenderData(
        Fluid fluid, ComponentChanges changes, Direction side, float value, boolean inbound
    ) {
    }
}
