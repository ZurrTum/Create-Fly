package com.zurrtum.create.client.catnip.gui.render;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.function.BiConsumer;

public class PartialRenderState implements SpecialGuiElementRenderState {
    public GeometryBakedModel model;
    public boolean dirty;
    public Matrix3x2f pose;
    public ScreenRect bounds;
    public int x1, y1, x2, y2, padding;
    public float size;
    private BiConsumer<MatrixStack, Float> transform;
    private float partialTicks;
    public @Nullable ScreenRect scissor;

    public void transform(MatrixStack matrices) {
        if (transform != null) {
            transform.accept(matrices, partialTicks);
        }
    }

    public void update(
        DrawContext graphics,
        PartialModel partial,
        float x,
        float y,
        float xLocal,
        float yLocal,
        float scale,
        int padding,
        float partialTicks,
        BiConsumer<MatrixStack, Float> transform
    ) {
        float size = scale * 16 + padding;
        if (model != partial.get()) {
            dirty = model != null;
            model = partial.get();
        } else if (size != this.size || partialTicks != this.partialTicks) {
            dirty = true;
        }
        pose = new Matrix3x2f(graphics.getMatrices());
        pose.translate(xLocal, yLocal);
        x1 = (int) x;
        y1 = (int) y;
        x2 = (int) (x + size);
        y2 = (int) (y + size);
        bounds = new ScreenRect(x1, y1, (int) size, (int) size).transformEachVertex(pose);
        scissor = graphics.scissorStack.peekLast();
        if (scissor != null) {
            bounds = bounds.intersection(scissor);
        }
        this.size = size;
        this.padding = padding;
        this.transform = transform;
        this.partialTicks = partialTicks;
    }

    public void clearDirty() {
        dirty = false;
    }

    @Override
    public int x1() {
        return x1;
    }

    @Override
    public int x2() {
        return x2;
    }

    @Override
    public int y1() {
        return y1;
    }

    @Override
    public int y2() {
        return y2;
    }

    @Override
    public Matrix3x2f pose() {
        return pose;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return bounds;
    }

    @Override
    public float scale() {
        return size;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return scissor;
    }
}
