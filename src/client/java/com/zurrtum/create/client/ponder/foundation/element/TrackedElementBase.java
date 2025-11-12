package com.zurrtum.create.client.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.ponder.api.element.TrackedElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;

public abstract class TrackedElementBase<T> extends PonderElementBase implements TrackedElement<T> {

    private final WeakReference<T> reference;

    public TrackedElementBase(T wrapped) {
        this.reference = new WeakReference<>(wrapped);
    }

    @Override
    public void ifPresent(Consumer<T> func) {
        T resolved = reference.get();
        if (resolved == null)
            return;
        func.accept(resolved);
    }

    @Override
    public void renderFirst(
        BlockEntityRenderDispatcher blockEntityRenderDispatcher,
        BlockRenderDispatcher blockRenderManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float pt
    ) {
    }

    @Override
    public void renderLayer(PonderLevel world, MultiBufferSource buffer, ChunkSectionLayer type, PoseStack ms, float pt) {
    }

    @Override
    public void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack ms,
        float pt
    ) {
    }

}