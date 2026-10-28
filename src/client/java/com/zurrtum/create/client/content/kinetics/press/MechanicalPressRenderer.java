package com.zurrtum.create.client.content.kinetics.press;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.press.MechanicalPressRenderer.MechanicalPressRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class MechanicalPressRenderer implements BlockEntityRenderer<MechanicalPressBlockEntity, MechanicalPressRenderState> {
    public MechanicalPressRenderer(Context context) {
    }

    @Override
    public MechanicalPressRenderState createRenderState() {
        return new MechanicalPressRenderState();
    }

    @Override
    public void extractRenderState(
        MechanicalPressBlockEntity be,
        MechanicalPressRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        int color = getTintColor(be);
        state.model = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(color).extractRenderState();
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
        PressingBehaviour pressingBehaviour = be.getPressingBehaviour();
        state.offset = -(pressingBehaviour.getRenderedHeadOffset(tickProgress) * pressingBehaviour.mode.headOffset);
        state.head = CachedBuffers.partialFacing(
            AllPartialModels.MECHANICAL_PRESS_HEAD,
            state.blockState,
            state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        MechanicalPressRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
            state.model.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model.submit(matrices, queue);
        }
        matrices.translate(0, state.offset, 0);
        state.head.submit(matrices, queue);
    }

    public static class MechanicalPressRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @UnknownNullability SuperByteBufferRenderState head;
        public float offset;
        public @UnknownNullability Axis axis;
        public float angle;
    }
}
