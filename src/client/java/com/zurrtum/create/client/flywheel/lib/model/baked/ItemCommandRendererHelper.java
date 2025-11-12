package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ItemCommandRendererHelper {
    public static void render(
        PoseStack matrices,
        SubmitNodeCollection queue,
        MultiBufferSource vertexConsumers,
        MultiBufferSource outlineVertexConsumers
    ) {
        for (SubmitNodeStorage.ItemSubmit itemCommand : queue.getItemSubmits()) {
            matrices.pushPose();
            matrices.last().set(itemCommand.pose());
            ItemRenderer.renderItem(
                itemCommand.displayContext(),
                matrices,
                vertexConsumers,
                itemCommand.lightCoords(),
                itemCommand.overlayCoords(),
                itemCommand.tintLayers(),
                itemCommand.quads(),
                itemCommand.renderType(),
                itemCommand.foilType()
            );
            if (itemCommand.outlineColor() != 0 && outlineVertexConsumers instanceof OutlineBufferSource outline) {
                outline.setColor(itemCommand.outlineColor());
                ItemRenderer.renderItem(
                    itemCommand.displayContext(),
                    matrices,
                    outlineVertexConsumers,
                    itemCommand.lightCoords(),
                    itemCommand.overlayCoords(),
                    itemCommand.tintLayers(),
                    itemCommand.quads(),
                    itemCommand.renderType(),
                    ItemStackRenderState.FoilType.NONE
                );
            }

            matrices.popPose();
        }
    }
}
