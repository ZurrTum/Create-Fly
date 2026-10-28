package com.zurrtum.create.client.content.equipment.bell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.equipment.bell.BellRenderer.BellRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class BellRenderer<BE extends AbstractBellBlockEntity> implements BlockEntityRenderer<BE, BellRenderState> {
    private final PartialModel model;

    public BellRenderer(Context context, PartialModel model) {
        this.model = model;
    }

    public static <BE extends AbstractBellBlockEntity> BlockEntityRendererProvider<BE, BellRenderState> of(PartialModel model) {
        return context -> new BellRenderer<>(context, model);
    }

    @Override
    public BellRenderState createRenderState() {
        return new BellRenderState();
    }

    @Override
    public void extractRenderState(
        BE be,
        BellRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.model = CachedBuffers.partial(model, state.blockState).cardinalLighting(level).light(state.lightCoords)
            .extractRenderState();
        if (be.isRinging) {
            state.direction = be.ringDirection.getCounterClockWise();
            state.swingRadians = getSwingAngle(be.ringingTicks + tickProgress);
        }
        Direction facing = state.blockState.getValue(BellBlock.FACING);
        BellAttachType attachment = state.blockState.getValue(BellBlock.ATTACHMENT);
        if (attachment == BellAttachType.SINGLE_WALL || attachment == BellAttachType.DOUBLE_WALL) {
            state.yRadians = Radians.horizontalPlus90(facing);
        } else {
            state.yRadians = Radians.horizontal(facing);
        }
    }

    @Override
    public void submit(
        BellRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        if (state.swingRadians != 0) {
            pose.rotateCentered(state.direction, state.swingRadians);
        }
        if (state.yRadians != 0) {
            pose.rotateYCentered(state.yRadians);
        }
        state.model.submit(matrices, queue);
    }

    public static float getSwingAngle(float time) {
        float t = time / 1.5f;
        return 1.2f * Mth.sin(t / (float) Math.PI) / (2.5f + t / 3.0f);
    }

    public static class BellRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @UnknownNullability Direction direction;
        public float swingRadians;
        public float yRadians;
    }
}
