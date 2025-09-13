package com.zurrtum.create.client.content.logistics.chute;

import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.content.logistics.chute.ChuteBlock.Shape;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.Direction;

public class ChuteRenderer extends SafeBlockEntityRenderer<ChuteBlockEntity> {

    public ChuteRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(ChuteBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (be.getItem().isEmpty())
            return;
        BlockState blockState = be.getCachedState();
        if (blockState.get(ChuteBlock.FACING) != Direction.DOWN)
            return;
        if (blockState.get(ChuteBlock.SHAPE) != Shape.WINDOW && (be.bottomPullDistance == 0 || be.itemPosition.getValue(partialTicks) > .5f))
            return;

        renderItem(be, partialTicks, ms, buffer, light, overlay);
    }

    public static void renderItem(ChuteBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        var msr = TransformStack.of(ms);
        ms.push();
        msr.center();
        float itemScale = .5f;
        float itemPosition = be.itemPosition.getValue(partialTicks);
        ms.translate(0, -.5 + itemPosition, 0);
        if (PackageItem.isPackage(be.getItem())) {
            ms.scale(1.5f, 1.5f, 1.5f);
        } else {
            ms.scale(itemScale, itemScale, itemScale);
            msr.rotateXDegrees(itemPosition * 180);
            msr.rotateYDegrees(itemPosition * 180);
        }
        itemRenderer.renderItem(be.getItem(), ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getWorld(), 0);
        ms.pop();
    }

}
