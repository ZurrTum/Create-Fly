package com.zurrtum.create.client.ponder.api.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;

public interface PonderSceneElement extends PonderElement {

    void renderFirst(
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        BlockRenderDispatcher blockRenderManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float pt
    );

    void renderLayer(PonderLevel world, MultiBufferSource buffer, ChunkSectionLayer type, PoseStack ms, float pt);

    void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float pt
    );

}
