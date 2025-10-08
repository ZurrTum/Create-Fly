package com.zurrtum.create.client.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BracketedKineticBlockEntityRenderer extends KineticBlockEntityRenderer<BracketedKineticBlockEntity, BracketedKineticBlockEntityRenderer.BracketedKineticRenderState> {
    public BracketedKineticBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BracketedKineticRenderState createRenderState() {
        return new BracketedKineticRenderState();
    }

    @Override
    public void updateRenderState(
        BracketedKineticBlockEntity be,
        BracketedKineticRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.large = be.getCachedState().isOf(AllBlocks.LARGE_COGWHEEL);
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.large) {
            state.shaft = CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, state.blockState, state.direction);
            state.shaftAngle = getAngleForLargeCogShaft(be, state.axis);
        }
    }

    @Override
    protected RenderLayer getRenderType(BracketedKineticBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BracketedKineticBlockEntity be, BracketedKineticRenderState state) {
        if (state.large) {
            return CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, state.blockState, state.direction);
        }
        return super.getRotatedModel(be, state);
    }

    public static float getAngleForLargeCogShaft(SimpleKineticBlockEntity be, Axis axis) {
        BlockPos pos = be.getPos();
        float offset = getShaftAngleOffset(axis, pos);
        float time = AnimationTickHolder.getRenderTime(be.getWorld());
        return ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
    }

    public static float getShaftAngleOffset(Axis axis, BlockPos pos) {
        if (KineticBlockEntityVisual.shouldOffset(axis, pos)) {
            return 22.5f;
        } else {
            return 0;
        }
    }

    public static class BracketedKineticRenderState extends KineticRenderState {
        public boolean large;
        public SuperByteBuffer shaft;
        public float shaftAngle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            if (shaft != null) {
                shaft.light(lightmapCoordinates);
                shaft.rotateCentered(shaftAngle, direction);
                shaft.color(color);
                shaft.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
