package com.zurrtum.create.client.content.redstone.analogLever;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AnalogLeverRenderer implements BlockEntityRenderer<AnalogLeverBlockEntity, AnalogLeverRenderer.AnalogLeverRenderState> {
    public AnalogLeverRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.layer = RenderType.solid();
        float level = be.clientState.getValue(tickProgress);
        state.angle = (float) ((level / 15) * 90 / 180 * Math.PI);
        state.handle = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_HANDLE, state.blockState);
        AttachFace face = state.blockState.getValue(AnalogLeverBlock.FACE);
        float rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        float rY = AngleHelper.horizontalAngle(state.blockState.getValue(AnalogLeverBlock.FACING));
        state.xRot = Mth.DEG_TO_RAD * rX;
        state.yRot = Mth.DEG_TO_RAD * rY;
        state.indicator = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_INDICATOR, state.blockState);
        state.color = Color.mixColors(0x2C0300, 0xCD0000, level / 15f);
    }

    @Override
    public void submit(AnalogLeverRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    private SuperByteBuffer transform(SuperByteBuffer buffer, BlockState leverState) {
        AttachFace face = leverState.getValue(AnalogLeverBlock.FACE);
        float rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        float rY = AngleHelper.horizontalAngle(leverState.getValue(AnalogLeverBlock.FACING));
        buffer.rotateCentered((float) (rY / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) (rX / 180 * Math.PI), Direction.EAST);
        return buffer;
    }

    public static class AnalogLeverRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float angle;
        public SuperByteBuffer handle;
        public float xRot;
        public float yRot;
        public SuperByteBuffer indicator;
        public int color;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            handle.rotateCentered(yRot, Direction.UP);
            handle.rotateCentered(xRot, Direction.EAST);
            handle.translate(0.5f, 0.0625f, 0.5f).rotate(angle, Direction.EAST).translate(-0.5f, -0.0625f, -0.5f);
            handle.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            indicator.rotateCentered(yRot, Direction.UP);
            indicator.rotateCentered(xRot, Direction.EAST);
            indicator.light(lightCoords).color(color).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
