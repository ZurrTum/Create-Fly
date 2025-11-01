package com.zurrtum.create.client.catnip.outliner;

import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class ItemOutline extends Outline {
    protected Vec3d pos;
    protected ItemStack stack;
    protected ItemRenderState itemRenderState;
    protected OrderedRenderCommandQueueImpl queue;
    protected MatrixStack matrices;

    public ItemOutline(Vec3d pos, ItemStack stack) {
        this.pos = pos;
        this.stack = stack;
        this.itemRenderState = new ItemRenderState();
        this.matrices = new MatrixStack();
        this.queue = new OrderedRenderCommandQueueImpl();
    }

    @Override
    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, float pt) {
        ms.push();

        ms.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
        ms.scale(params.alpha, params.alpha, params.alpha);

        mc.getItemModelManager().clearAndUpdate(this.itemRenderState, stack, ItemDisplayContext.FIXED, null, null, 0);
        itemRenderState.render(ms, queue, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0);
        for (BatchingRenderCommandQueue batchingRenderCommandQueue : queue.getBatchingQueues().values()) {
            for (OrderedRenderCommandQueueImpl.ItemCommand itemCommand : batchingRenderCommandQueue.getItemCommands()) {
                matrices.push();
                matrices.peek().copy(itemCommand.positionMatrix());
                ItemRenderer.renderItem(
                    itemCommand.displayContext(),
                    matrices,
                    buffer,
                    itemCommand.lightCoords(),
                    itemCommand.overlayCoords(),
                    itemCommand.tintLayers(),
                    itemCommand.quads(),
                    itemCommand.renderLayer(),
                    itemCommand.glintType()
                );
                matrices.pop();
            }
        }

        ms.pop();
    }
}
