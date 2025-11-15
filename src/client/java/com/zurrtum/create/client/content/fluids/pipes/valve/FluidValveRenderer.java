package com.zurrtum.create.client.content.fluids.pipes.valve;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FluidValveRenderer extends KineticBlockEntityRenderer<FluidValveBlockEntity, FluidValveRenderer.FluidValveRenderState> {
    public FluidValveRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FluidValveRenderState createRenderState() {
        return new FluidValveRenderState();
    }

    @Override
    public void extractRenderState(
        FluidValveBlockEntity be,
        FluidValveRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            return;
        }
        BlockState blockState = be.getBlockState();
        state.pointer = CachedBuffers.partial(AllPartialModels.FLUID_VALVE_POINTER, blockState);
        Direction facing = blockState.getValue(FluidValveBlock.FACING);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        Axis pipeAxis = FluidValveBlock.getPipeAxis(blockState);
        float pointerRotation = Mth.lerpInt(be.pointer.getValue(tickProgress), 0, -90);
        if (pipeAxis.isHorizontal() && getRotationAxisOf(be) == Axis.X || pipeAxis.isVertical()) {
            state.yRot2 = Mth.DEG_TO_RAD * (90 + pointerRotation);
        } else {
            state.yRot2 = Mth.DEG_TO_RAD * pointerRotation;
        }
    }

    @Override
    protected RenderType getRenderType(FluidValveBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    @Override
    protected BlockState getRenderedBlockState(FluidValveBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public static class FluidValveRenderState extends KineticRenderState {
        public SuperByteBuffer pointer;
        public float yRot;
        public float xRot;
        public float yRot2;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            pointer.center().rotateY(yRot).rotateX(xRot).rotateY(yRot2).uncenter();
            pointer.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
