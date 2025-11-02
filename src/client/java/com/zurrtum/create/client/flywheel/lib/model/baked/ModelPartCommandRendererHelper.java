package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.ModelPartCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.Map;

public class ModelPartCommandRendererHelper {
    public static void render(
        MatrixStack matrices,
        BatchingRenderCommandQueue queue,
        VertexConsumerProvider vertexConsumers,
        VertexConsumerProvider outlineVertexConsumerProvider,
        VertexConsumerProvider immediate
    ) {
        ModelPartCommandRenderer.Commands commands = queue.getModelPartCommands();

        for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelPartCommand>> entry : commands.modelPartCommands.entrySet()) {
            RenderLayer renderLayer = entry.getKey();
            List<OrderedRenderCommandQueueImpl.ModelPartCommand> list = entry.getValue();
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

            for (OrderedRenderCommandQueueImpl.ModelPartCommand modelPartCommand : list) {
                VertexConsumer vertexConsumer2;
                if (modelPartCommand.sprite() != null) {
                    if (modelPartCommand.hasGlint()) {
                        vertexConsumer2 = modelPartCommand.sprite()
                            .getTextureSpecificVertexConsumer(ItemRenderer.getItemGlintConsumer(
                                vertexConsumers,
                                renderLayer,
                                modelPartCommand.sheeted(),
                                true
                            ));
                    } else {
                        vertexConsumer2 = modelPartCommand.sprite().getTextureSpecificVertexConsumer(vertexConsumer);
                    }
                } else if (modelPartCommand.hasGlint()) {
                    vertexConsumer2 = ItemRenderer.getItemGlintConsumer(vertexConsumers, renderLayer, modelPartCommand.sheeted(), true);
                } else {
                    vertexConsumer2 = vertexConsumer;
                }

                matrices.peek().copy(modelPartCommand.matricesEntry());
                modelPartCommand.modelPart().render(
                    matrices,
                    vertexConsumer2,
                    modelPartCommand.lightCoords(),
                    modelPartCommand.overlayCoords(),
                    modelPartCommand.tintedColor()
                );
                if (modelPartCommand.outlineColor() != 0 && (renderLayer.getAffectedOutline().isPresent() || renderLayer.isOutline())) {
                    VertexConsumer vertexConsumer3 = CustomCommandRendererHelper.getOutlineBuffer(
                        outlineVertexConsumerProvider,
                        renderLayer,
                        modelPartCommand.outlineColor()
                    );
                    if (vertexConsumer3 != null) {
                        if (modelPartCommand.sprite() != null) {
                            vertexConsumer3 = modelPartCommand.sprite().getTextureSpecificVertexConsumer(vertexConsumer3);
                        }
                        modelPartCommand.modelPart().render(
                            matrices,
                            vertexConsumer3,
                            modelPartCommand.lightCoords(),
                            modelPartCommand.overlayCoords(),
                            modelPartCommand.tintedColor()
                        );
                    }
                }

                if (modelPartCommand.crumblingOverlay() != null) {
                    VertexConsumer vertexConsumer3 = new OverlayVertexConsumer(
                        immediate.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(modelPartCommand.crumblingOverlay().progress())),
                        modelPartCommand.crumblingOverlay().cameraMatricesEntry(),
                        1.0F
                    );
                    modelPartCommand.modelPart().render(
                        matrices,
                        vertexConsumer3,
                        modelPartCommand.lightCoords(),
                        modelPartCommand.overlayCoords(),
                        modelPartCommand.tintedColor()
                    );
                }
            }
        }
    }
}
