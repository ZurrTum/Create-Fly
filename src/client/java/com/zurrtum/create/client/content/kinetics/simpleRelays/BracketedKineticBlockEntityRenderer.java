package com.zurrtum.create.client.content.kinetics.simpleRelays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BracketedKineticBlockEntityRenderer extends KineticBlockEntityRenderer<BracketedKineticBlockEntity, BracketedKineticBlockEntityRenderer.BracketedKineticRenderState> {
    public BracketedKineticBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BracketedKineticRenderState createRenderState() {
        return new BracketedKineticRenderState();
    }

    @Override
    public void extractRenderState(
        BracketedKineticBlockEntity be,
        BracketedKineticRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.large = be.getBlockState().is(AllBlocks.LARGE_COGWHEEL);
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.large) {
            state.shaft = CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, state.blockState, state.direction);
            state.shaftAngle = getAngleForLargeCogShaft(be, state.axis);
        }
    }

    @Override
    protected RenderType getRenderType(BracketedKineticBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(BracketedKineticBlockEntity be, BracketedKineticRenderState state) {
        if (state.large) {
            return CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, state.blockState, state.direction);
        }
        return super.getRotatedModel(be, state);
    }

    public static float getAngleForLargeCogShaft(SimpleKineticBlockEntity be, Axis axis) {
        BlockPos pos = be.getBlockPos();
        float offset = getShaftAngleOffset(axis, pos);
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
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
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            if (shaft != null) {
                shaft.light(lightCoords);
                shaft.rotateCentered(shaftAngle, direction);
                shaft.color(color);
                shaft.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
