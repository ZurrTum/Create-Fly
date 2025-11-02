package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

public interface PonderSceneElement extends PonderElement {

    void renderFirst(
        BlockEntityRenderManager blockEntityRenderDispatcher,
        BlockRenderManager blockRenderManager,
        PonderLevel world,
        VertexConsumerProvider buffer,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        MatrixStack ms,
        float pt
    );

    void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack ms, float pt);

    void renderLast(
        EntityRenderManager entityRenderManager,
        ItemModelManager itemModelManager,
        PonderLevel world,
        VertexConsumerProvider buffer,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        MatrixStack ms,
        float pt
    );

}
