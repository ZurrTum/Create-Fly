package com.zurrtum.create.client.content.redstone.analogLever;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
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

public class AnalogLeverRenderer implements BlockEntityRenderer<AnalogLeverBlockEntity, AnalogLeverRenderer.AnalogLeverRenderState> {
    public AnalogLeverRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public AnalogLeverRenderState createRenderState() {
        return new AnalogLeverRenderState();
    }

    @Override
    public void updateRenderState(
        AnalogLeverBlockEntity be,
        AnalogLeverRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getSolid();
        float level = be.clientState.getValue(tickProgress);
        state.angle = (float) ((level / 15) * 90 / 180 * Math.PI);
        state.handle = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_HANDLE, state.blockState);
        BlockFace face = state.blockState.get(AnalogLeverBlock.FACE);
        float rX = face == BlockFace.FLOOR ? 0 : face == BlockFace.WALL ? 90 : 180;
        float rY = AngleHelper.horizontalAngle(state.blockState.get(AnalogLeverBlock.FACING));
        state.xRot = MathHelper.RADIANS_PER_DEGREE * rX;
        state.yRot = MathHelper.RADIANS_PER_DEGREE * rY;
        state.indicator = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_INDICATOR, state.blockState);
        state.color = Color.mixColors(0x2C0300, 0xCD0000, level / 15f);
    }

    @Override
    public void render(AnalogLeverRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    private SuperByteBuffer transform(SuperByteBuffer buffer, BlockState leverState) {
        BlockFace face = leverState.get(AnalogLeverBlock.FACE);
        float rX = face == BlockFace.FLOOR ? 0 : face == BlockFace.WALL ? 90 : 180;
        float rY = AngleHelper.horizontalAngle(leverState.get(AnalogLeverBlock.FACING));
        buffer.rotateCentered((float) (rY / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) (rX / 180 * Math.PI), Direction.EAST);
        return buffer;
    }

    public static class AnalogLeverRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float angle;
        public SuperByteBuffer handle;
        public float xRot;
        public float yRot;
        public SuperByteBuffer indicator;
        public int color;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            handle.rotateCentered(yRot, Direction.UP);
            handle.rotateCentered(xRot, Direction.EAST);
            handle.translate(0.5f, 0.0625f, 0.5f).rotate(angle, Direction.EAST).translate(-0.5f, -0.0625f, -0.5f);
            handle.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
            indicator.rotateCentered(yRot, Direction.UP);
            indicator.rotateCentered(xRot, Direction.EAST);
            indicator.light(lightmapCoordinates).color(color).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
