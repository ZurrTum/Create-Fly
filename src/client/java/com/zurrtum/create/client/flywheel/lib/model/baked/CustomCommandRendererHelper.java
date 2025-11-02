package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.OutlineVertexConsumerProvider.OutlineVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.CustomCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomCommandRendererHelper {
    public static void render(BatchingRenderCommandQueue queue, VertexConsumerProvider vertexConsumers) {
        CustomCommandRenderer.Commands commands = queue.getCustomCommands();

        for (Map.Entry<RenderLayer, List<OrderedRenderCommandQueueImpl.CustomCommand>> entry : commands.customCommands.entrySet()) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(entry.getKey());

            for (OrderedRenderCommandQueueImpl.CustomCommand customCommand : entry.getValue()) {
                customCommand.customRenderer().render(customCommand.matricesEntry(), vertexConsumer);
            }
        }

    }

    public static VertexConsumer getOutlineBuffer(VertexConsumerProvider vertexConsumers, RenderLayer layer, int color) {
        if (layer.isOutline()) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(layer);
            return new OutlineVertexConsumer(vertexConsumer, color);
        }
        Optional<RenderLayer> optional = layer.getAffectedOutline();
        if (optional.isPresent()) {
            VertexConsumer vertexConsumer2 = vertexConsumers.getBuffer(optional.get());
            return new OutlineVertexConsumer(vertexConsumer2, color);
        }
        return null;
    }
}
