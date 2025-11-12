package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.resources.model.ModelBakery;

public class ModelPartCommandRendererHelper {
    public static void render(
        PoseStack matrices,
        SubmitNodeCollection queue,
        MultiBufferSource vertexConsumers,
        MultiBufferSource outlineVertexConsumerProvider,
        MultiBufferSource immediate
    ) {
        ModelPartFeatureRenderer.Storage commands = queue.getModelPartSubmits();

        for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> entry : commands.modelPartSubmits.entrySet()) {
            RenderType renderLayer = entry.getKey();
            List<SubmitNodeStorage.ModelPartSubmit> list = entry.getValue();
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

            for (SubmitNodeStorage.ModelPartSubmit modelPartCommand : list) {
                VertexConsumer vertexConsumer2;
                if (modelPartCommand.sprite() != null) {
                    if (modelPartCommand.hasFoil()) {
                        vertexConsumer2 = modelPartCommand.sprite()
                            .wrap(ItemRenderer.getFoilBuffer(vertexConsumers, renderLayer, modelPartCommand.sheeted(), true));
                    } else {
                        vertexConsumer2 = modelPartCommand.sprite().wrap(vertexConsumer);
                    }
                } else if (modelPartCommand.hasFoil()) {
                    vertexConsumer2 = ItemRenderer.getFoilBuffer(vertexConsumers, renderLayer, modelPartCommand.sheeted(), true);
                } else {
                    vertexConsumer2 = vertexConsumer;
                }

                matrices.last().set(modelPartCommand.pose());
                modelPartCommand.modelPart().render(
                    matrices,
                    vertexConsumer2,
                    modelPartCommand.lightCoords(),
                    modelPartCommand.overlayCoords(),
                    modelPartCommand.tintedColor()
                );
                if (modelPartCommand.outlineColor() != 0 && (renderLayer.outline().isPresent() || renderLayer.isOutline())) {
                    VertexConsumer vertexConsumer3 = CustomCommandRendererHelper.getOutlineBuffer(
                        outlineVertexConsumerProvider,
                        renderLayer,
                        modelPartCommand.outlineColor()
                    );
                    if (vertexConsumer3 != null) {
                        if (modelPartCommand.sprite() != null) {
                            vertexConsumer3 = modelPartCommand.sprite().wrap(vertexConsumer3);
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
                    VertexConsumer vertexConsumer3 = new SheetedDecalTextureGenerator(
                        immediate.getBuffer(ModelBakery.DESTROY_TYPES.get(modelPartCommand.crumblingOverlay().progress())),
                        modelPartCommand.crumblingOverlay().cameraPose(),
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
