package com.zurrtum.create.client.content.redstone.analogLever;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class AnalogLeverRenderer extends SafeBlockEntityRenderer<AnalogLeverBlockEntity> {

    public AnalogLeverRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(AnalogLeverBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState leverState = be.getCachedState();
        float state = be.clientState.getValue(partialTicks);

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        // Handle
        SuperByteBuffer handle = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_HANDLE, leverState);
        float angle = (float) ((state / 15) * 90 / 180 * Math.PI);
        transform(handle, leverState).translate(1 / 2f, 1 / 16f, 1 / 2f).rotate(angle, Direction.EAST).translate(-1 / 2f, -1 / 16f, -1 / 2f);
        handle.light(light).renderInto(ms, vb);

        // Indicator
        int color = Color.mixColors(0x2C0300, 0xCD0000, state / 15f);
        SuperByteBuffer indicator = transform(CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_INDICATOR, leverState), leverState);
        indicator.light(light).color(color).renderInto(ms, vb);
    }

    private SuperByteBuffer transform(SuperByteBuffer buffer, BlockState leverState) {
        BlockFace face = leverState.get(AnalogLeverBlock.FACE);
        float rX = face == BlockFace.FLOOR ? 0 : face == BlockFace.WALL ? 90 : 180;
        float rY = AngleHelper.horizontalAngle(leverState.get(AnalogLeverBlock.FACING));
        buffer.rotateCentered((float) (rY / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) (rX / 180 * Math.PI), Direction.EAST);
        return buffer;
    }

}
