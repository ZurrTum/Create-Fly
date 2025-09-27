package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.client.ponder.api.element.TrackedElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
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
    public void renderFirst(PonderLevel world, VertexConsumerProvider buffer, MatrixStack ms, float pt) {
    }

    @Override
    public void renderLayer(PonderLevel world, VertexConsumerProvider buffer, BlockRenderLayer type, MatrixStack ms, float pt) {
    }

    @Override
    public void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack ms, float pt) {
    }

}