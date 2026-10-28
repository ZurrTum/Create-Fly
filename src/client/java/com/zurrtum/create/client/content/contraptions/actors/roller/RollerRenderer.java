package com.zurrtum.create.client.content.contraptions.actors.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.actors.roller.RollerRenderer.RollerRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlock;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class RollerRenderer implements BlockEntityRenderer<RollerBlockEntity, RollerRenderState> {
    protected final ItemModelResolver itemModelManager;

    public RollerRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public RollerRenderState createRenderState() {
        return new RollerRenderState();
    }

    @Override
    public void extractRenderState(
        RollerBlockEntity be,
        RollerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level)) {
            BlockPos blockPos = be.getBlockPos();
            BlockState blockState = be.getBlockState();
            state.filter = FilteringRenderer.getFilterRenderState(
                be,
                blockState,
                itemModelManager,
                be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos))
            );
            if (state.filter != null) {
                state.blockPos = blockPos;
                state.blockState = blockState;
                state.blockEntityType = be.getType();
                state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, blockPos);
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            state.blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
        );
        state.wheel = CachedBuffers.partial(AllPartialModels.ROLLER_WHEEL, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        Direction facing = state.blockState.getValue(RollerBlock.FACING);
        state.offset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(1.0625f).add(0, -0.25f, 0);
        state.wheelRadians = Radians.horizontal(facing);
        float animatedSpeed = be.getAnimatedSpeed();
        if (animatedSpeed != 0) {
            state.xRadians = Mth.DEG_TO_RAD * -(AnimationTickHolder.getRenderTime(level) / 20 * animatedSpeed % 360);
        }
        state.frame = CachedBuffers.partial(AllPartialModels.ROLLER_FRAME, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.frameRadians = Radians.horizontalPlus180(facing);
    }

    @Override
    public void submit(
        RollerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.wheel != null) {
            matrices.pushPose();
            Pose pose = matrices.last();
            Vec3 offset = state.offset;
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (state.wheelRadians != 0) {
                pose.rotateYCentered(state.wheelRadians);
            }
            if (state.xRadians != 0) {
                pose.rotate(Axis.XP, state.xRadians);
            }
            pose.translate(0, -0.5f, 0.5f);
            pose.rotate(Axis.YP, Radians.DEG_90);
            state.wheel.submit(matrices, queue);
            matrices.popPose();
            if (state.frameRadians != 0) {
                matrices.rotateYCentered(state.frameRadians);
            }
            state.frame.submit(matrices, queue);
        }
    }

    public static class RollerRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable SuperByteBufferRenderState wheel;
        public @UnknownNullability SuperByteBufferRenderState frame;
        public @UnknownNullability Vec3 offset;
        public float wheelRadians;
        public float xRadians;
        public float frameRadians;
    }
}
