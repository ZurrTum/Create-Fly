package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.actors.psi.PortableStorageInterfaceRenderer.PortableStorageInterfaceRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class PortableStorageInterfaceRenderer implements BlockEntityRenderer<PortableStorageInterfaceBlockEntity, PortableStorageInterfaceRenderState> {
    public PortableStorageInterfaceRenderer(Context context) {
    }

    @Override
    public PortableStorageInterfaceRenderState createRenderState() {
        return new PortableStorageInterfaceRenderState();
    }

    @Override
    public void extractRenderState(
        PortableStorageInterfaceBlockEntity be,
        PortableStorageInterfaceRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.middle = CachedBuffers.partial(getMiddleForState(state.blockState, be.isConnected()), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.top = CachedBuffers.partial(getTopForState(state.blockState), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        Direction facing = state.blockState.getValue(PortableStorageInterfaceBlock.FACING);
        state.yRadians = Radians.horizontal(facing);
        if (facing != Direction.UP) {
            state.xRadians = facing == Direction.DOWN ? Radians.DEG_180 : Radians.DEG_90;
        }
        float offset = be.getExtensionDistance(tickProgress) * 0.5f;
        state.middleOffset = offset + 0.375f;
        state.topOffset = offset - 0.375f;
    }

    @Override
    public void submit(
        PortableStorageInterfaceRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        if (state.yRadians != 0 || state.xRadians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            if (state.yRadians != 0) {
                pose.rotate(Axis.YP, state.yRadians);
            }
            if (state.xRadians != 0) {
                pose.rotate(Axis.XP, state.xRadians);
            }
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        pose.translate(0, state.middleOffset, 0);
        state.middle.submit(matrices, queue);
        pose.translate(0, state.topOffset, 0);
        state.top.submit(matrices, queue);
    }

    public static PartialModel getMiddleForState(BlockState state, boolean lit) {
        if (state.is(AllBlocks.PORTABLE_FLUID_INTERFACE)) {
            return lit ? AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED :
                AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE;
        }
        return lit ? AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED :
            AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE;
    }

    public static PartialModel getTopForState(BlockState state) {
        if (state.is(AllBlocks.PORTABLE_FLUID_INTERFACE)) {
            return AllPartialModels.PORTABLE_FLUID_INTERFACE_TOP;
        }
        return AllPartialModels.PORTABLE_STORAGE_INTERFACE_TOP;
    }

    public static class PortableStorageInterfaceRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState middle;
        public @UnknownNullability SuperByteBufferRenderState top;
        public float yRadians;
        public float xRadians;
        public float middleOffset;
        public float topOffset;
    }
}
