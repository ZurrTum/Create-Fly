package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PortableStorageInterfaceRenderer implements BlockEntityRenderer<PortableStorageInterfaceBlockEntity, PortableStorageInterfaceRenderer.PortableStorageInterfaceRenderState> {
    public PortableStorageInterfaceRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public PortableStorageInterfaceRenderState createRenderState() {
        return new PortableStorageInterfaceRenderState();
    }

    @Override
    public void updateRenderState(
        PortableStorageInterfaceBlockEntity be,
        PortableStorageInterfaceRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        if (VisualizationManager.supportsVisualization(be.getWorld())) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getSolid();
        state.middle = CachedBuffers.partial(getMiddleForState(state.blockState, be.isConnected()), state.blockState);
        state.top = CachedBuffers.partial(getTopForState(state.blockState), state.blockState);
        Direction facing = state.blockState.get(PortableStorageInterfaceBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        state.topOffset = be.getExtensionDistance(tickProgress);
        state.middleOffset = state.topOffset * 0.5f + 0.375f;
    }

    @Override
    public void render(
        PortableStorageInterfaceRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState
    ) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static PartialModel getMiddleForState(BlockState state, boolean lit) {
        if (state.isOf(AllBlocks.PORTABLE_FLUID_INTERFACE))
            return lit ? AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED : AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE;
        return lit ? AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED : AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE;
    }

    public static PartialModel getTopForState(BlockState state) {
        if (state.isOf(AllBlocks.PORTABLE_FLUID_INTERFACE))
            return AllPartialModels.PORTABLE_FLUID_INTERFACE_TOP;
        return AllPartialModels.PORTABLE_STORAGE_INTERFACE_TOP;
    }

    public static class PortableStorageInterfaceRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer middle;
        public SuperByteBuffer top;
        public float yRot;
        public float xRot;
        public float middleOffset;
        public float topOffset;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            middle.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, middleOffset, 0).light(lightmapCoordinates)
                .renderInto(matricesEntry, vertexConsumer);
            top.center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, topOffset, 0).light(lightmapCoordinates)
                .renderInto(matricesEntry, vertexConsumer);
        }
    }
}
