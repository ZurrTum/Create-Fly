package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyBlockEntityRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BogeyBlockEntityRenderer<T extends AbstractBogeyBlockEntity> implements BlockEntityRenderer<T, BogeyBlockEntityRenderState> {
    public BogeyBlockEntityRenderer(Context context) {
    }

    @Override
    public BogeyBlockEntityRenderState createRenderState() {
        return new BogeyBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        BogeyBlockEntityRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.rotate = state.blockState.getValue(AbstractBogeyBlock.AXIS) == Direction.Axis.X;
        BogeySize size = ((AbstractBogeyBlock<?>) state.blockState.getBlock()).getSize();
        state.data = AllBogeyStyleRenders.getRenderData(
            be.getStyle(),
            size,
            tickProgress,
            state.lightCoords,
            cardinalLighting,
            be.getVirtualAngle(tickProgress),
            be.getBogeyData(),
            false
        );
    }

    @Override
    public void submit(
        BogeyBlockEntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.data == null) {
            return;
        }
        matrices.pushPose();
        Pose pose = matrices.last();
        pose.translate(0.5f, 0.5f, 0.5f);
        if (state.rotate) {
            pose.rotate(Axis.YP, Radians.DEG_90);
        }
        state.data.submit(matrices, queue);
        matrices.popPose();
    }

    public static class BogeyBlockEntityRenderState extends BlockEntityRenderState {
        public boolean rotate;
        public @Nullable BogeyRenderState data;
    }

    public interface BogeyRenderState {
        void submit(PoseStack matrices, SubmitNodeCollector queue);
    }
}
