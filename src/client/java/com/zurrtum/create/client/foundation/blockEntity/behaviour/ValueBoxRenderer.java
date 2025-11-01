package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3f;

public class ValueBoxRenderer {
    public static void renderItemIntoValueBox(ItemRenderState state, OrderedRenderCommandQueue queue, MatrixStack ms, int light, float offset) {
        boolean blockItem = state.isSideLit();
        float scale = (!blockItem ? .5f : 1f) + 1 / 64f;
        float zOffset = (!blockItem ? -.15f : 0) + offset;
        ms.scale(scale, scale, scale);
        ms.translate(0, 0, zOffset);
        state.render(ms, queue, light, OverlayTexture.DEFAULT_UV, 0);
    }

    public static void renderFlatItemIntoValueBox(ItemRenderState state, OrderedRenderCommandQueue queue, MatrixStack ms, int light) {
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
        state.render(squashedMS, queue, itemLight, OverlayTexture.DEFAULT_UV, 0);

        ms.pop();
    }

    @SuppressWarnings("deprecation")
    public static float customZOffset(Item item) {
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