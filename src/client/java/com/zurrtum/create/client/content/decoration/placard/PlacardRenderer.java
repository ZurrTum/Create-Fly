package com.zurrtum.create.client.content.decoration.placard;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.decoration.placard.PlacardBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class PlacardRenderer extends SafeBlockEntityRenderer<PlacardBlockEntity> {

    public PlacardRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(PlacardBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        ItemStack heldItem = be.getHeldItem();
        if (heldItem.isEmpty())
            return;

        BlockState blockState = be.getCachedState();
        Direction facing = blockState.get(PlacardBlock.FACING);
        BlockFace face = blockState.get(PlacardBlock.FACE);

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, heldItem, ItemDisplayContext.FIXED, null, null, 0);
        boolean blockItem = itemRenderer.itemRenderState.isSideLit();

        ms.push();
        boolean isCeiling = face == BlockFace.CEILING;
        TransformStack.of(ms).center()
            .rotate((isCeiling ? MathHelper.PI : 0) + AngleHelper.rad(180 + AngleHelper.horizontalAngle(facing)), Direction.UP)
            .rotate(isCeiling ? -MathHelper.PI / 2 : face == BlockFace.FLOOR ? MathHelper.PI / 2 : 0, Direction.EAST).translate(0, 0, 4.5 / 16f)
            .scale(blockItem ? .5f : .375f);

        itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
        ms.pop();
    }

}
