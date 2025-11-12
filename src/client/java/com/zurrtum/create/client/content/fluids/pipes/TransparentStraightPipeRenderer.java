package com.zurrtum.create.client.content.fluids.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.foundation.fluid.FluidRenderer;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TransparentStraightPipeRenderer implements BlockEntityRenderer<StraightPipeBlockEntity, TransparentStraightPipeRenderer.TransparentStraightPipeRenderState> {
    public TransparentStraightPipeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TransparentStraightPipeRenderState createRenderState() {
        return new TransparentStraightPipeRenderState();
    }

    @Override
    public void extractRenderState(
        StraightPipeBlockEntity be,
        TransparentStraightPipeRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        FluidTransportBehaviour pipe = be.getBehaviour(FluidTransportBehaviour.TYPE);
        if (pipe == null)
            return;
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = PonderRenderTypes.fluid();
        Direction[] directions = Iterate.directions;
        int size = directions.length;
        state.radius = 3 / 16f;
        state.data = new FluidRenderData[size];
        Level world = be.getLevel();
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
                    FluidTransportBehaviour adjacent = BlockEntityBehaviour.get(world, state.blockPos.relative(side), FluidTransportBehaviour.TYPE);
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
    public void submit(TransparentStraightPipeRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.data != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
    }

    public static class TransparentStraightPipeRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float radius;
        public FluidRenderData[] data;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
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
                    lightCoords
                );
            }
        }
    }

    public record FluidRenderData(
        Fluid fluid, DataComponentPatch changes, Direction side, float value, boolean inbound
    ) {
    }
}
