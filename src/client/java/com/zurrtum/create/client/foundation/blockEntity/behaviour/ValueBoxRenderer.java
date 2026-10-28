package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.content.kinetics.base.Radians;
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
import org.joml.Matrix4f;

public class ValueBoxRenderer {
    public static void renderItemIntoValueBox(
        ItemStackRenderState state,
        SubmitNodeCollector queue,
        PoseStack ms,
        int light,
        float offset
    ) {
        Pose pose = ms.last();
        boolean blockItem = state.usesBlockLight();
        float scale = (!blockItem ? 0.5f : 1.0f) + 1 / 64.0f;
        float zOffset = (!blockItem ? -0.15f : 0) + offset;
        pose.scale(scale, scale, scale);
        pose.translate(0, 0, zOffset);
        state.submit(ms, queue, light, OverlayTexture.NO_OVERLAY, 0);
    }

    public static void renderFlatItemIntoValueBox(
        ItemStackRenderState state,
        SubmitNodeCollector queue,
        PoseStack ms,
        int light
    ) {
        ms.pushPose();
        Pose pose = ms.last();
        Matrix4f matrix4f = pose.pose();
        matrix4f.translate(0, 0, -0.21775f);
        matrix4f.rotateY(Radians.DEG_180);
        matrix4f.scale(0.5f, 0.5f, 1 / 1024.0f);
        pose.normal().rotateX(Radians.DEG_230);
        int bl = light >> 4 & 0xf;
        int sl = light >> 20 & 0xf;
        int itemLight = Mth.floor(sl + 0.5) << 20 | (Mth.floor(bl + 0.5) & 0xf) << 4;
        state.submit(ms, queue, itemLight, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @SuppressWarnings("deprecation")
    public static float customZOffset(Item item) {
        float nudge = -0.1f;
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof AbstractSimpleShaftBlock) {
                return nudge;
            }
            if (block instanceof FenceBlock) {
                return nudge;
            }
            if (block.builtInRegistryHolder().is(BlockTags.BUTTONS)) {
                return nudge;
            }
            if (block == Blocks.END_ROD) {
                return nudge;
            }
        }
        return 0;
    }
}