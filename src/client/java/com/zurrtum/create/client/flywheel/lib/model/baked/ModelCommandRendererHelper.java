package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.SharedConstants;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.resources.model.ModelBakery;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.*;

public class ModelCommandRendererHelper {
    public static void render(
        PoseStack matrices,
        SubmitNodeCollection queue,
        MultiBufferSource vertexConsumers,
        MultiBufferSource outlineVertexConsumers,
        MultiBufferSource crumblingOverlayVertexConsumers
    ) {
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.Storage commands = queue.getModelSubmits();
        renderAll(matrices, vertexConsumers, outlineVertexConsumers, commands.opaqueModelSubmits, crumblingOverlayVertexConsumers);
        commands.translucentModelSubmits.sort(Comparator.comparingDouble(modelCommand -> -modelCommand.position().lengthSquared()));
        renderAllBlended(matrices, vertexConsumers, outlineVertexConsumers, commands.translucentModelSubmits, crumblingOverlayVertexConsumers);
    }

    private static void renderAllBlended(
        PoseStack matrices,
        MultiBufferSource vertexConsumers,
        MultiBufferSource outlineVertexConsumers,
        List<SubmitNodeStorage.TranslucentModelSubmit<?>> blendedModelCommands,
        MultiBufferSource crumblingOverlayVertexConsumers
    ) {
        for (SubmitNodeStorage.TranslucentModelSubmit<?> blendedModelCommand : blendedModelCommands) {
            render(
                matrices,
                blendedModelCommand.modelSubmit(),
                blendedModelCommand.renderType(),
                vertexConsumers.getBuffer(blendedModelCommand.renderType()),
                outlineVertexConsumers,
                crumblingOverlayVertexConsumers
            );
        }
    }

    private static void renderAll(
        PoseStack matrices,
        MultiBufferSource vertexConsumers,
        MultiBufferSource outlineVertexConsumers,
        Map<RenderType, List<SubmitNodeStorage.ModelSubmit<?>>> modelCommands,
        MultiBufferSource crumblingOverlayVertexConsumers
    ) {
        Iterable<Map.Entry<RenderType, List<SubmitNodeStorage.ModelSubmit<?>>>> iterable;
        if (SharedConstants.DEBUG_SHUFFLE_MODELS) {
            List<Map.Entry<RenderType, List<SubmitNodeStorage.ModelSubmit<?>>>> list = new ArrayList<>(modelCommands.entrySet());
            Collections.shuffle(list);
            iterable = list;
        } else {
            iterable = modelCommands.entrySet();
        }

        for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelSubmit<?>>> entry : iterable) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(entry.getKey());

            for (SubmitNodeStorage.ModelSubmit<?> modelCommand : entry.getValue()) {
                render(matrices, modelCommand, entry.getKey(), vertexConsumer, outlineVertexConsumers, crumblingOverlayVertexConsumers);
            }
        }
    }

    private static <S> void render(
        PoseStack matrices,
        SubmitNodeStorage.ModelSubmit<S> model,
        RenderType renderLayer,
        VertexConsumer vertexConsumer,
        MultiBufferSource outlineVertexConsumers,
        MultiBufferSource crumblingOverlayVertexConsumers
    ) {
        matrices.pushPose();
        matrices.last().set(model.pose());
        Model<? super S> model2 = model.model();
        VertexConsumer vertexConsumer2 = model.sprite() == null ? vertexConsumer : model.sprite().wrap(vertexConsumer);
        model2.setupAnim(model.state());
        model2.renderToBuffer(matrices, vertexConsumer2, model.lightCoords(), model.overlayCoords(), model.tintedColor());
        if (model.outlineColor() != 0 && (renderLayer.outline().isPresent() || renderLayer.isOutline())) {
            VertexConsumer vertexConsumer3 = CustomCommandRendererHelper.getOutlineBuffer(outlineVertexConsumers, renderLayer, model.outlineColor());
            if (vertexConsumer3 != null) {
                if (model.sprite() != null) {
                    vertexConsumer3 = model.sprite().wrap(vertexConsumer3);
                }
                model2.renderToBuffer(matrices, vertexConsumer3, model.lightCoords(), model.overlayCoords(), model.tintedColor());
            }
        }

        if (model.crumblingOverlay() != null && renderLayer.affectsCrumbling()) {
            VertexConsumer vertexConsumer3 = new SheetedDecalTextureGenerator(
                crumblingOverlayVertexConsumers.getBuffer(ModelBakery.DESTROY_TYPES.get(model.crumblingOverlay().progress())),
                model.crumblingOverlay().cameraPose(),
                1.0F
            );
            model2.renderToBuffer(
                matrices,
                model.sprite() == null ? vertexConsumer3 : model.sprite().wrap(vertexConsumer3),
                model.lightCoords(),
                model.overlayCoords(),
                model.tintedColor()
            );
        }

        matrices.popPose();
    }
}
