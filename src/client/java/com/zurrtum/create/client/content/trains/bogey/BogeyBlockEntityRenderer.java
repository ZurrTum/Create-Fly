package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BogeyBlockEntityRenderer<T extends AbstractBogeyBlockEntity> implements BlockEntityRenderer<T, BogeyBlockEntityRenderer.BogeyBlockEntityRenderState> {
    public BogeyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.blockState = be.getBlockState();
        if (!(state.blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey)) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        Level world = be.getLevel();
        state.lightCoords = world != null ? LevelRenderer.getLightCoords(world, state.blockPos) : LightCoordsUtil.FULL_BRIGHT;
        if (state.blockState.getValue(AbstractBogeyBlock.AXIS) == Direction.Axis.X) {
            state.yRot = Mth.DEG_TO_RAD * 90;
        }
        state.bogeyData = be.getBogeyData();
        if (state.bogeyData == null) {
            state.bogeyData = new CompoundTag();
        }
        state.data = AllBogeyStyleRenders.getRenderData(
            be.getStyle(),
            bogey.getSize(),
            tickProgress,
            state.lightCoords,
            be.getVirtualAngle(tickProgress),
            be.getBogeyData(),
            false
        );
    }

    @Override
    public void submit(BogeyBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.data == null) {
            return;
        }
        matrices.pushPose();
        matrices.translate(.5f, .5f, .5f);
        if (state.yRot != 0) {
            matrices.mulPose(Axis.YP.rotation(state.yRot));
        }
        state.data.render(matrices, queue);
        matrices.popPose();
    }

    public static class BogeyBlockEntityRenderState extends BlockEntityRenderState {
        public float yRot;
        public CompoundTag bogeyData;
        public BogeyRenderState data;
    }

    public interface BogeyRenderState {
        void render(PoseStack matrices, SubmitNodeCollector queue);
    }
}
