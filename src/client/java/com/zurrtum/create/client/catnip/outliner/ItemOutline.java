package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemOutline extends Outline {
    protected Vec3 pos;
    protected ItemStack stack;
    protected ItemStackRenderState itemRenderState;

    public ItemOutline(Vec3 pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
        itemRenderState = new ItemStackRenderState();
    }

    @Override
    public void submit(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera, float pt) {
        ms.pushPose();
        Pose pose = ms.last();
        pose.translate((float) (pos.x - camera.x), (float) (pos.y - camera.y), (float) (pos.z - camera.z));
        pose.scale(params.alpha, params.alpha, params.alpha);
        mc.getItemModelResolver().updateForTopItem(itemRenderState, stack, ItemDisplayContext.FIXED, null, null, 0);
        itemRenderState.submit(ms, queue, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }
}
