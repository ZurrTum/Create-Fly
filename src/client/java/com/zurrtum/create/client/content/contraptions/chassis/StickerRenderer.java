package com.zurrtum.create.client.content.contraptions.chassis;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class StickerRenderer extends SafeBlockEntityRenderer<StickerBlockEntity> {

    public StickerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(StickerBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        BlockState state = be.getCachedState();
        SuperByteBuffer head = CachedBuffers.partial(AllPartialModels.STICKER_HEAD, state);
        float offset = be.piston.getValue(AnimationTickHolder.getPartialTicks(be.getWorld()));

        if (be.getWorld() != MinecraftClient.getInstance().world && !be.isVirtual())
            offset = state.get(StickerBlock.EXTENDED) ? 1 : 0;

        Direction facing = state.get(StickerBlock.FACING);
        head.nudge(be.hashCode()).center().rotateYDegrees(AngleHelper.horizontalAngle(facing)).rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
            .uncenter().translate(0, (offset * offset) * 4 / 16f, 0);

        head.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

}
