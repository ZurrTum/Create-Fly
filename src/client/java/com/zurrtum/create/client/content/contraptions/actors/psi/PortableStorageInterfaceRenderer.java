package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
        float progress = be.getExtensionDistance(tickProgress);
        state.layer = RenderLayer.getSolid();
        state.middle = CachedBuffers.partial(getMiddleForState(state.blockState, be.isConnected()), state.blockState);
        state.top = CachedBuffers.partial(getTopForState(state.blockState), state.blockState);
        Direction facing = state.blockState.get(PortableStorageInterfaceBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        state.middleOffset = progress * 0.5f + 0.375f;
        state.topOffset = progress;
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

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        BlockState blockState = context.state;
        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());
        float renderPartialTicks = AnimationTickHolder.getPartialTicks();

        LerpedFloat animation = PortableStorageInterfaceMovement.getAnimation(context);
        float progress = animation.getValue(renderPartialTicks);
        boolean lit = animation.settled();
        render(
            blockState,
            lit,
            progress,
            matrices.getModel(),
            sbb -> sbb.light(WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos)).useLevelLight(context.world, matrices.getWorld())
                .renderInto(matrices.getViewProjection().peek(), vb)
        );
    }

    private static void render(BlockState blockState, boolean lit, float progress, MatrixStack local, Consumer<SuperByteBuffer> drawCallback) {
        SuperByteBuffer middle = CachedBuffers.partial(getMiddleForState(blockState, lit), blockState);
        SuperByteBuffer top = CachedBuffers.partial(getTopForState(blockState), blockState);

        if (local != null) {
            middle.transform(local);
            top.transform(local);
        }
        Direction facing = blockState.get(PortableStorageInterfaceBlock.FACING);
        rotateToFacing(middle, facing);
        rotateToFacing(top, facing);
        middle.translate(0, progress * 0.5f + 0.375f, 0);
        top.translate(0, progress, 0);

        drawCallback.accept(middle);
        drawCallback.accept(top);
    }

    private static void rotateToFacing(SuperByteBuffer buffer, Direction facing) {
        buffer.center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90).uncenter();
    }

    static PortableStorageInterfaceBlockEntity getTargetPSI(MovementContext context) {
        String _workingPos_ = PortableStorageInterfaceMovement._workingPos_;
        if (!context.data.contains(_workingPos_))
            return null;

        BlockPos pos = context.data.get(_workingPos_, BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        BlockEntity blockEntity = context.world.getBlockEntity(pos);
        if (!(blockEntity instanceof PortableStorageInterfaceBlockEntity psi))
            return null;

        if (!psi.isTransferring())
            return null;
        return psi;
    }

    static PartialModel getMiddleForState(BlockState state, boolean lit) {
        if (state.isOf(AllBlocks.PORTABLE_FLUID_INTERFACE))
            return lit ? AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED : AllPartialModels.PORTABLE_FLUID_INTERFACE_MIDDLE;
        return lit ? AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED : AllPartialModels.PORTABLE_STORAGE_INTERFACE_MIDDLE;
    }

    static PartialModel getTopForState(BlockState state) {
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
