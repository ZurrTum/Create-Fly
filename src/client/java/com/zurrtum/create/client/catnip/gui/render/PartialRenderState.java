package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.function.BiConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;

public class PartialRenderState implements PictureInPictureRenderState {
    public SimpleModelWrapper model;
    public boolean dirty;
    public Matrix3x2f IDENTITY_POSE;
    public ScreenRectangle bounds;
    public int x1, y1, x2, y2, padding;
    public float size;
    private BiConsumer<PoseStack, Float> transform;
    private float partialTicks;
    public @Nullable ScreenRectangle scissor;

    public void transform(PoseStack matrices) {
        if (transform != null) {
            transform.accept(matrices, partialTicks);
        }
    }

    public void update(
        GuiGraphics graphics,
        PartialModel partial,
        float x,
        float y,
        float xLocal,
        float yLocal,
        float scale,
        int padding,
        float partialTicks,
        BiConsumer<PoseStack, Float> transform
    ) {
        float size = scale * 16 + padding;
        if (model != partial.get()) {
            dirty = model != null;
            model = partial.get();
        } else if (size != this.size || partialTicks != this.partialTicks) {
            dirty = true;
        }
        IDENTITY_POSE = new Matrix3x2f(graphics.pose());
        IDENTITY_POSE.translate(xLocal, yLocal);
        x1 = (int) x;
        y1 = (int) y;
        x2 = (int) (x + size);
        y2 = (int) (y + size);
        bounds = new ScreenRectangle(x1, y1, (int) size, (int) size).transformMaxBounds(IDENTITY_POSE);
        scissor = graphics.scissorStack.peek();
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
    public int x0() {
        return x1;
    }

    @Override
    public int x1() {
        return x2;
    }

    @Override
    public int y0() {
        return y1;
    }

    @Override
    public int y1() {
        return y2;
    }

    @Override
    public Matrix3x2f pose() {
        return IDENTITY_POSE;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return bounds;
    }

    @Override
    public float scale() {
        return size;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return scissor;
    }
}
