package com.zurrtum.create.client.content.redstone.deskBell;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class DeskBellRenderer extends SmartBlockEntityRenderer<DeskBellBlockEntity> {

    public DeskBellRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        DeskBellBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        BlockState blockState = blockEntity.getCachedState();
        float p = blockEntity.animation.getValue(partialTicks);
        if (p < 0.004 && !blockState.get(DeskBellBlock.POWERED, false))
            return;

        float f = (float) (1 - 4 * Math.pow((Math.max(p - 0.5, 0)) - 0.5, 2));
        float f2 = (float) (Math.pow(p, 1.25f));

        Direction facing = blockState.get(DeskBellBlock.FACING);

        CachedBuffers.partial(AllPartialModels.DESK_BELL_PLUNGER, blockState).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90).uncenter().translate(0, f * -.75f / 16f, 0).light(light).overlay(overlay)
            .renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

        CachedBuffers.partial(AllPartialModels.DESK_BELL_BELL, blockState).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90).translate(0, -1 / 16, 0)
            .rotateXDegrees(f2 * 8 * MathHelper.sin(p * MathHelper.PI * 4 + blockEntity.animationOffset))
            .rotateZDegrees(f2 * 8 * MathHelper.cos(p * MathHelper.PI * 4 + blockEntity.animationOffset)).translate(0, 1 / 16, 0).scale(0.995f)
            .uncenter().light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
    }

}
