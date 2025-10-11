package com.zurrtum.create.client.content.equipment.bell;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlockEntity;
import com.zurrtum.create.content.equipment.bell.PeculiarBellBlockEntity;
import net.minecraft.block.BellBlock;
import net.minecraft.block.enums.Attachment;
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

public class BellRenderer<BE extends AbstractBellBlockEntity> implements BlockEntityRenderer<BE, BellRenderer.BellRenderState> {
    public BellRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public BellRenderState createRenderState() {
        return new BellRenderState();
    }

    @Override
    public void updateRenderState(
        BE be,
        BellRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getCutout();
        state.model = CachedBuffers.partial(
            be instanceof PeculiarBellBlockEntity ? AllPartialModels.PECULIAR_BELL : AllPartialModels.HAUNTED_BELL,
            state.blockState
        );
        if (be.isRinging) {
            state.direction = be.ringDirection.rotateYCounterclockwise();
            state.angle = getSwingAngle(be.ringingTicks + tickProgress);
        }
        Direction facing = state.blockState.get(BellBlock.FACING);
        Attachment attachment = state.blockState.get(BellBlock.ATTACHMENT);
        float rY = AngleHelper.horizontalAngle(facing);
        if (attachment == Attachment.SINGLE_WALL || attachment == Attachment.DOUBLE_WALL)
            rY += 90;
        state.upAngle = AngleHelper.rad(rY);
    }

    @Override
    public void render(BellRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static float getSwingAngle(float time) {
        float t = time / 1.5f;
        return 1.2f * MathHelper.sin(t / (float) Math.PI) / (2.5f + t / 3.0f);
    }

    public static class BellRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float upAngle;
        public Direction direction;
        public float angle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            if (direction != null) {
                model.rotateCentered(angle, direction);
            }
            model.rotateCentered(upAngle, Direction.UP);
            model.light(lightmapCoordinates);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
