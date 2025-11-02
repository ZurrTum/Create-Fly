package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.SharedConstants;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;

import java.util.*;

public class ModelCommandRendererHelper {
    public static void render(
        MatrixStack matrices,
        BatchingRenderCommandQueue queue,
        VertexConsumerProvider vertexConsumers,
        VertexConsumerProvider outlineVertexConsumers,
        VertexConsumerProvider crumblingOverlayVertexConsumers
    ) {
        net.minecraft.client.render.command.ModelCommandRenderer.Commands commands = queue.getModelCommands();
        renderAll(matrices, vertexConsumers, outlineVertexConsumers, commands.opaqueModelCommands, crumblingOverlayVertexConsumers);
        commands.blendedModelCommands.sort(Comparator.comparingDouble(modelCommand -> -modelCommand.position().lengthSquared()));
        renderAllBlended(matrices, vertexConsumers, outlineVertexConsumers, commands.blendedModelCommands, crumblingOverlayVertexConsumers);
    }

    private static void renderAllBlended(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        VertexConsumerProvider outlineVertexConsumers,
        List<OrderedRenderCommandQueueImpl.BlendedModelCommand<?>> blendedModelCommands,
        VertexConsumerProvider crumblingOverlayVertexConsumers
    ) {
        for (OrderedRenderCommandQueueImpl.BlendedModelCommand<?> blendedModelCommand : blendedModelCommands) {
            render(
                matrices,
                blendedModelCommand.model(),
                blendedModelCommand.renderType(),
                vertexConsumers.getBuffer(blendedModelCommand.renderType()),
                outlineVertexConsumers,
                crumblingOverlayVertexConsumers
            );
        }
    }

    private static void renderAll(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        VertexConsumerProvider outlineVertexConsumers,
        Map<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>> modelCommands,
        VertexConsumerProvider crumblingOverlayVertexConsumers
    ) {
        Iterable<Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>>> iterable;
        if (SharedConstants.SHUFFLE_MODELS) {
            List<Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>>> list = new ArrayList<>(modelCommands.entrySet());
            Collections.shuffle(list);
            iterable = list;
        } else {
            iterable = modelCommands.entrySet();
        }

        for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.ModelCommand<?>>> entry : iterable) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(entry.getKey());

            for (OrderedRenderCommandQueueImpl.ModelCommand<?> modelCommand : entry.getValue()) {
                render(matrices, modelCommand, entry.getKey(), vertexConsumer, outlineVertexConsumers, crumblingOverlayVertexConsumers);
            }
        }
    }

    private static <S> void render(
        MatrixStack matrices,
        OrderedRenderCommandQueueImpl.ModelCommand<S> model,
        RenderLayer renderLayer,
        VertexConsumer vertexConsumer,
        VertexConsumerProvider outlineVertexConsumers,
        VertexConsumerProvider crumblingOverlayVertexConsumers
    ) {
        matrices.push();
        matrices.peek().copy(model.matricesEntry());
        Model<? super S> model2 = model.model();
        VertexConsumer vertexConsumer2 = model.sprite() == null ? vertexConsumer : model.sprite().getTextureSpecificVertexConsumer(vertexConsumer);
        model2.setAngles(model.state());
        model2.render(matrices, vertexConsumer2, model.lightCoords(), model.overlayCoords(), model.tintedColor());
        if (model.outlineColor() != 0 && (renderLayer.getAffectedOutline().isPresent() || renderLayer.isOutline())) {
            VertexConsumer vertexConsumer3 = CustomCommandRendererHelper.getOutlineBuffer(outlineVertexConsumers, renderLayer, model.outlineColor());
            if (vertexConsumer3 != null) {
                if (model.sprite() != null) {
                    vertexConsumer3 = model.sprite().getTextureSpecificVertexConsumer(vertexConsumer3);
                }
                model2.render(matrices, vertexConsumer3, model.lightCoords(), model.overlayCoords(), model.tintedColor());
            }
        }

        if (model.crumblingOverlay() != null && renderLayer.hasCrumbling()) {
            VertexConsumer vertexConsumer3 = new OverlayVertexConsumer(
                crumblingOverlayVertexConsumers.getBuffer(ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.get(
                    model.crumblingOverlay().progress())),
                model.crumblingOverlay().cameraMatricesEntry(),
                1.0F
            );
            model2.render(
                matrices,
                model.sprite() == null ? vertexConsumer3 : model.sprite().getTextureSpecificVertexConsumer(vertexConsumer3),
                model.lightCoords(),
                model.overlayCoords(),
                model.tintedColor()
            );
        }

        matrices.pop();
    }
}
