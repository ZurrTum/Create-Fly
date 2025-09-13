package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3f;

public class ValueBoxRenderer {

    public static void renderItemIntoValueBox(ItemStack filter, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        itemRenderer.itemModelManager.clearAndUpdate(itemRenderer.itemRenderState, filter, ItemDisplayContext.FIXED, null, null, 0);
        boolean blockItem = itemRenderer.itemRenderState.isSideLit();
        float scale = (!blockItem ? .5f : 1f) + 1 / 64f;
        float zOffset = (!blockItem ? -.15f : 0) + customZOffset(filter.getItem());
        ms.scale(scale, scale, scale);
        ms.translate(0, 0, zOffset);
        itemRenderer.itemRenderState.render(ms, buffer, light, overlay);
    }

    public static void renderFlatItemIntoValueBox(ItemStack filter, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (filter.isEmpty())
            return;

        int bl = light >> 4 & 0xf;
        int sl = light >> 20 & 0xf;
        int itemLight = MathHelper.floor(sl + .5) << 20 | (MathHelper.floor(bl + .5) & 0xf) << 4;

        ms.push();
        TransformStack.of(ms).rotateXDegrees(230);
        Matrix3f copy = new Matrix3f(ms.peek().getNormalMatrix());
        ms.pop();

        ms.push();
        TransformStack.of(ms).translate(0, 0, -1 / 4f).translate(0, 0, 1 / 32f + .001).rotateYDegrees(180);

        MatrixStack squashedMS = new MatrixStack();
        squashedMS.peek().getPositionMatrix().mul(ms.peek().getPositionMatrix());
        squashedMS.scale(.5f, .5f, 1 / 1024f);
        squashedMS.peek().getNormalMatrix().set(copy);
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getItemRenderer().renderItem(filter, ItemDisplayContext.GUI, itemLight, OverlayTexture.DEFAULT_UV, squashedMS, buffer, mc.world, 0);

        ms.pop();
    }

    @SuppressWarnings("deprecation")
    private static float customZOffset(Item item) {
        float nudge = -.1f;
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof AbstractSimpleShaftBlock)
                return nudge;
            if (block instanceof FenceBlock)
                return nudge;
            if (block.getRegistryEntry().isIn(BlockTags.BUTTONS))
                return nudge;
            if (block == Blocks.END_ROD)
                return nudge;
        }
        return 0;
    }

}