package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemOutline extends Outline {
    protected Vec3 pos;
    protected ItemStack stack;
    protected ItemStackRenderState itemRenderState;
    protected SubmitNodeStorage queue;
    protected PoseStack matrices;

    public ItemOutline(Vec3 pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
        this.itemRenderState = new ItemStackRenderState();
        this.matrices = new PoseStack();
        this.queue = new SubmitNodeStorage();
    }

    @Override
    public void render(Minecraft mc, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
        ms.pushPose();

        ms.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        ms.scale(params.alpha, params.alpha, params.alpha);

        mc.getItemModelResolver().updateForTopItem(this.itemRenderState, stack, ItemDisplayContext.FIXED, null, null, 0);
        itemRenderState.submit(ms, queue, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        for (SubmitNodeCollection batchingRenderCommandQueue : queue.getSubmitsPerOrder().values()) {
            for (SubmitNodeStorage.ItemSubmit itemCommand : batchingRenderCommandQueue.getItemSubmits()) {
                matrices.pushPose();
                matrices.last().set(itemCommand.pose());
                ItemRenderer.renderItem(
                    itemCommand.displayContext(),
                    matrices,
                    buffer,
                    itemCommand.lightCoords(),
                    itemCommand.overlayCoords(),
                    itemCommand.tintLayers(),
                    itemCommand.quads(),
                    itemCommand.renderType(),
                    itemCommand.foilType()
                );
                matrices.popPose();
            }
        }

        ms.popPose();
    }
}
