package com.zurrtum.create.client.content.redstone.analogLever;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverRenderer.AnalogLeverRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class AnalogLeverRenderer implements BlockEntityRenderer<AnalogLeverBlockEntity, AnalogLeverRenderState> {
    public AnalogLeverRenderer(Context context) {
    }

    @Override
    public AnalogLeverRenderState createRenderState() {
        return new AnalogLeverRenderState();
    }

    @Override
    public void extractRenderState(
        AnalogLeverBlockEntity be,
        AnalogLeverRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level world = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(world);
        float level = be.clientState.getValue(tickProgress) / 15;
        state.level = level;
        int lightCoords = state.lightCoords;
        BlockState blockState = state.blockState;
        state.handle = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_HANDLE, blockState)
            .cardinalLighting(cardinalLighting).light(lightCoords).extractRenderState();
        AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
        if (face != AttachFace.FLOOR) {
            state.xRadians = face == AttachFace.WALL ? Radians.DEG_90 : Radians.DEG_180;
        }
        state.yRadians = Radians.horizontal(blockState.getValue(AnalogLeverBlock.FACING));
        int color = Color.mixColors(0xFF2C0300, 0xFFCD0000, level);
        state.indicator = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_INDICATOR, blockState)
            .cardinalLighting(cardinalLighting).light(lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
        AnalogLeverRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        if (state.yRadians != 0) {
            pose.rotateYCentered(state.yRadians);
        }
        if (state.xRadians != 0) {
            pose.rotateXCentered(state.xRadians);
        }
        state.indicator.submit(matrices, queue);
        if (state.level != 0) {
            pose.rotateXAround(state.level * Radians.DEG_90, 0.5f, 0.0625f, 0.5f);
        }
        state.handle.submit(matrices, queue);
    }

    public static class AnalogLeverRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState handle;
        public @UnknownNullability SuperByteBufferRenderState indicator;
        public float level;
        public float xRadians;
        public float yRadians;
    }
}
