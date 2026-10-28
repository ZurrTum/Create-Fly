package com.zurrtum.create.client.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.equipment.armor.BacktankRenderer.BacktankRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getProgress;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;

public class BacktankRenderer implements BlockEntityRenderer<BacktankBlockEntity, BacktankRenderState> {
    public BacktankRenderer(Context context) {
    }

    @Override
    public BacktankRenderState createRenderState() {
        return new BacktankRenderState();
    }

    @Override
    public void extractRenderState(
        BacktankBlockEntity be,
        BacktankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        float time = AnimationTickHolder.getRenderTime(level);
        float speed = be.getSpeed();
        state.shaft = CachedBuffers.partial(getShaftModel(state.blockState), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        float progress = getProgress(speed, time);
        float offset = rotationOffset(state.blockState, Axis.Y, state.blockPos);
        state.angle = (progress + offset) % 360;
        state.yRadians = Radians.horizontalPlus180(state.blockState.getValue(BacktankBlock.HORIZONTAL_FACING));
        state.rotate = speed / 4.0f * time % 360;
        state.cogs = CachedBuffers.partial(getCogsModel(state.blockState), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        BacktankRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != 0) {
            matrices.pushPose();
            matrices.rotateYCentered(Mth.DEG_TO_RAD * state.angle);
            state.shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.shaft.submit(matrices, queue);
        }
        Pose pose = matrices.last();
        if (state.yRadians != 0) {
            pose.rotateYCentered(state.yRadians);
        }
        if (state.rotate != 0) {
            pose.rotateXAround(Mth.DEG_TO_RAD * state.rotate, 0, 0.40625f, 0.6875f);
        }
        state.cogs.submit(matrices, queue);
    }

    public static PartialModel getCogsModel(BlockState state) {
        if (state.is(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_COGS;
        }
        return AllPartialModels.COPPER_BACKTANK_COGS;
    }

    public static PartialModel getShaftModel(BlockState state) {
        if (state.is(AllBlocks.NETHERITE_BACKTANK)) {
            return AllPartialModels.NETHERITE_BACKTANK_SHAFT;
        }
        return AllPartialModels.COPPER_BACKTANK_SHAFT;
    }

    public static class BacktankRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public float angle;
        public @UnknownNullability SuperByteBufferRenderState cogs;
        public float yRadians;
        public float rotate;
    }
}
