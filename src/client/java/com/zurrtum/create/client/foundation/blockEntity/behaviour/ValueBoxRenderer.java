package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import org.joml.Matrix3f;

public class ValueBoxRenderer {
    public static void renderItemIntoValueBox(ItemStackRenderState state, SubmitNodeCollector queue, PoseStack ms, int light, float offset) {
        boolean blockItem = state.usesBlockLight();
        float scale = (!blockItem ? .5f : 1f) + 1 / 64f;
        float zOffset = (!blockItem ? -.15f : 0) + offset;
        ms.scale(scale, scale, scale);
        ms.translate(0, 0, zOffset);
        state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
    }

    public static void renderFlatItemIntoValueBox(ItemStackRenderState state, SubmitNodeCollector queue, PoseStack ms, int light) {
        int bl = light >> 4 & 0xf;
        int sl = light >> 20 & 0xf;
        int itemLight = Mth.floor(sl + .5) << 20 | (Mth.floor(bl + .5) & 0xf) << 4;

        ms.pushPose();
        TransformStack.of(ms).rotateXDegrees(230);
        Matrix3f copy = new Matrix3f(ms.last().normal());
        ms.popPose();

        ms.pushPose();
        TransformStack.of(ms).translate(0, 0, -1 / 4f).translate(0, 0, 1 / 32f + .001).rotateYDegrees(180);

        PoseStack squashedMS = new PoseStack();
        squashedMS.last().pose().mul(ms.last().pose());
        squashedMS.scale(.5f, .5f, 1 / 1024f);
        squashedMS.last().normal().set(copy);
        state.submit(squashedMS, queue, itemLight, OverlayTexture.NO_OVERLAY, 0);

        ms.popPose();
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
            if (block.builtInRegistryHolder().is(BlockTags.BUTTONS))
                return nudge;
            if (block == Blocks.END_ROD)
                return nudge;
        }
        return 0;
    }
}