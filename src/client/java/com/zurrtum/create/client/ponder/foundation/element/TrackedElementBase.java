package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.client.ponder.api.element.TrackedElement;
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

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

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
        BlockEntityRenderManager blockEntityRenderDispatcher,
        BlockRenderManager blockRenderManager,
        PonderLevel world,
        VertexConsumerProvider buffer,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        MatrixStack ms,
        float pt
    ) {
    }

    @Override
    public void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack ms, float pt) {
    }

    @Override
    public void renderLast(
        EntityRenderManager entityRenderManager,
        ItemModelManager itemModelManager,
        PonderLevel world,
        VertexConsumerProvider buffer,
        OrderedRenderCommandQueue queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        MatrixStack ms,
        float pt
    ) {
    }

}