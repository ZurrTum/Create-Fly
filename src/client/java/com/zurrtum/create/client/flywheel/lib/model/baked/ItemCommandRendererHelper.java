package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class ItemCommandRendererHelper {
    public static void render(
        MatrixStack matrices,
        BatchingRenderCommandQueue queue,
        VertexConsumerProvider vertexConsumers,
        VertexConsumerProvider outlineVertexConsumers
    ) {
        for (OrderedRenderCommandQueueImpl.ItemCommand itemCommand : queue.getItemCommands()) {
            matrices.push();
            matrices.peek().copy(itemCommand.positionMatrix());
            ItemRenderer.renderItem(
                itemCommand.displayContext(),
                matrices,
                vertexConsumers,
                itemCommand.lightCoords(),
                itemCommand.overlayCoords(),
                itemCommand.tintLayers(),
                itemCommand.quads(),
                itemCommand.renderLayer(),
                itemCommand.glintType()
            );
            if (itemCommand.outlineColor() != 0 && outlineVertexConsumers instanceof OutlineVertexConsumerProvider outline) {
                outline.setColor(itemCommand.outlineColor());
                ItemRenderer.renderItem(
                    itemCommand.displayContext(),
                    matrices,
                    outlineVertexConsumers,
                    itemCommand.lightCoords(),
                    itemCommand.overlayCoords(),
                    itemCommand.tintLayers(),
                    itemCommand.quads(),
                    itemCommand.renderLayer(),
                    ItemRenderState.Glint.NONE
                );
            }

            matrices.pop();
        }
    }
}
