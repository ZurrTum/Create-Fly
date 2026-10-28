package com.zurrtum.create.client.content.equipment.toolbox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxRenderer.ToolboxRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class ToolboxRenderer implements BlockEntityRenderer<ToolboxBlockEntity, ToolboxRenderState> {
    public ToolboxRenderer(Context context) {
    }

    @Override
    public ToolboxRenderState createRenderState() {
        return new ToolboxRenderState();
    }

    @Override
    public void extractRenderState(
        ToolboxBlockEntity be,
        ToolboxRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.lid = CachedBuffers.partial(AllPartialModels.TOOLBOX_LIDS.get(be.getColor()), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.drawer = CachedBuffers.partial(AllPartialModels.TOOLBOX_DRAWER, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        Direction facing = state.blockState.getValue(ToolboxBlock.FACING).getOpposite();
        state.yRot = -facing.toYRot();
        state.xRadians = be.lid.getValue(tickProgress) * Radians.DEG_135;
        float drawerOffset = be.drawers.getValue(tickProgress);
        state.offset1 = -drawerOffset * 0.175f;
        state.offset2 = state.offset1 * 2;
    }

    @Override
    public void submit(
        ToolboxRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.yRot != 0) {
            matrices.rotateYCentered(Mth.DEG_TO_RAD * state.yRot);
        }
        matrices.pushPose();
        matrices.translate(0, 0.125f, state.offset1);
        state.drawer.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(0, 0, state.offset2);
        state.drawer.submit(matrices, queue);
        matrices.popPose();
        if (state.xRadians != 0) {
            matrices.rotateXAround(state.xRadians, 0, 0.375f, 0.75f);
        }
        state.lid.submit(matrices, queue);
    }

    public static class ToolboxRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState lid;
        public @UnknownNullability SuperByteBufferRenderState drawer;
        public float yRot;
        public float xRadians;
        public float offset1;
        public float offset2;
    }
}
