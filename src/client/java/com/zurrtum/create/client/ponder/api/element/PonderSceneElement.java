package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public interface PonderSceneElement extends PonderElement {

    void renderFirst(PonderLevel world, VertexConsumerProvider buffer, MatrixStack ms, float pt);

    void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack ms, float pt);

    void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack ms, float pt);

}
