package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource.EntityOutlineGenerator;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;

public class CustomCommandRendererHelper {
    public static void render(SubmitNodeCollection queue, MultiBufferSource vertexConsumers) {
        CustomFeatureRenderer.Storage commands = queue.getCustomGeometrySubmits();

        for (Map.Entry<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>> entry : commands.customGeometrySubmits.entrySet()) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(entry.getKey());

            for (SubmitNodeStorage.CustomGeometrySubmit customCommand : entry.getValue()) {
                customCommand.customGeometryRenderer().render(customCommand.pose(), vertexConsumer);
            }
        }

    }

    public static VertexConsumer getOutlineBuffer(MultiBufferSource vertexConsumers, RenderType layer, int color) {
        if (layer.isOutline()) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(layer);
            return new EntityOutlineGenerator(vertexConsumer, color);
        }
        Optional<RenderType> optional = layer.outline();
        if (optional.isPresent()) {
            VertexConsumer vertexConsumer2 = vertexConsumers.getBuffer(optional.get());
            return new EntityOutlineGenerator(vertexConsumer2, color);
        }
        return null;
    }
}
