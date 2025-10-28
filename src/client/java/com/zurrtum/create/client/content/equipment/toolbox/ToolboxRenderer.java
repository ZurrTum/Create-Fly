package com.zurrtum.create.client.content.equipment.toolbox;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
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

public class ToolboxRenderer implements BlockEntityRenderer<ToolboxBlockEntity, ToolboxRenderer.ToolboxRenderState> {
    public ToolboxRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public ToolboxRenderState createRenderState() {
        return new ToolboxRenderState();
    }

    @Override
    public void updateRenderState(
        ToolboxBlockEntity be,
        ToolboxRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getCutoutMipped();
        state.lid = CachedBuffers.partial(AllPartialModels.TOOLBOX_LIDS.get(be.getColor()), state.blockState);
        state.drawer = CachedBuffers.partial(AllPartialModels.TOOLBOX_DRAWER, state.blockState);
        Direction facing = state.blockState.get(ToolboxBlock.FACING).getOpposite();
        state.yRot = MathHelper.RADIANS_PER_DEGREE * -facing.getPositiveHorizontalDegrees();
        state.xRot = MathHelper.RADIANS_PER_DEGREE * 135 * be.lid.getValue(tickProgress);
        float drawerOffset = be.drawers.getValue(tickProgress);
        state.offset1 = -drawerOffset * .175f;
        state.offset2 = state.offset1 * 2;
    }

    @Override
    public void render(ToolboxRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static class ToolboxRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer lid;
        public SuperByteBuffer drawer;
        public float yRot;
        public float xRot;
        public float offset1;
        public float offset2;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            lid.center().rotateY(yRot).uncenter().translate(0, 0.375f, 0.75f).rotateX(xRot).translate(0, -0.375f, -0.75f).light(lightmapCoordinates)
                .renderInto(matricesEntry, vertexConsumer);
            drawer.center().rotateY(yRot).uncenter().translate(0, 0.125f, offset1).light(lightmapCoordinates)
                .renderInto(matricesEntry, vertexConsumer);
            drawer.center().rotateY(yRot).uncenter().translate(0, 0, offset2).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
