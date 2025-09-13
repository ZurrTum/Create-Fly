package com.zurrtum.create.client.catnip.gui.element;

import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

import java.util.function.BiConsumer;

public class GuiGameElement {
    public static GuiItemRenderBuilder of(ItemStack stack) {
        return new GuiItemRenderBuilder(stack);
    }

    public static GuiBlockStateRenderBuilder of(BlockState block) {
        return new GuiBlockStateRenderBuilder(block);
    }

    public static GuiPartialRenderBuilder partial() {
        return new GuiPartialRenderBuilder();
    }

    public static GuiPartialRenderBuilder of(PartialModel model) {
        return new GuiPartialRenderBuilder(model);
    }

    public static abstract class GuiRenderBuilder<T extends GuiRenderBuilder<T>> extends AbstractRenderElement {
        protected float xRot, yRot, zRot;
        protected float scale = 1;
        protected int padding;

        abstract T self();

        public T padding(int padding) {
            this.padding = padding;
            return self();
        }

        public T rotate(float x, float y, float z) {
            xRot = MathHelper.RADIANS_PER_DEGREE * x;
            yRot = MathHelper.RADIANS_PER_DEGREE * y;
            zRot = MathHelper.RADIANS_PER_DEGREE * z;
            return self();
        }

        public T scale(float scale) {
            this.scale = scale;
            return self();
        }
    }

    public static class GuiItemRenderBuilder extends GuiRenderBuilder<GuiItemRenderBuilder> {
        private final ItemStack stack;
        private ItemTransformRenderState state;

        public GuiItemRenderBuilder(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        GuiItemRenderBuilder self() {
            return this;
        }

        @Override
        public void render(DrawContext graphics) {
            if (scale <= 1 && xRot == 0 && yRot == 0 && zRot == 0) {
                if (scale == 1) {
                    graphics.drawItem(stack, (int) x, (int) y);
                } else {
                    Matrix3x2fStack matrices = graphics.getMatrices();
                    matrices.pushMatrix();
                    matrices.scale(scale);
                    graphics.drawItem(stack, (int) x, (int) y);
                    matrices.popMatrix();
                }
                return;
            }
            if (state == null) {
                state = new ItemTransformRenderState();
            }
            state.update(graphics, stack, x, y, scale, padding, xRot, yRot, zRot);
            graphics.state.addSpecialElement(state);
        }

        @Override
        public void clear() {
            if (state != null) {
                ItemTransformElementRenderer.clear(state);
            }
        }
    }

    public static class GuiBlockStateRenderBuilder extends GuiRenderBuilder<GuiBlockStateRenderBuilder> {
        private final BlockTransformRenderState state = new BlockTransformRenderState();
        private final BlockState block;

        public GuiBlockStateRenderBuilder(BlockState block) {
            this.block = block;
        }

        @Override
        GuiBlockStateRenderBuilder self() {
            return this;
        }

        @Override
        public void render(DrawContext graphics) {
            state.update(graphics, block, x, y, scale, padding, xRot, yRot, zRot);
            graphics.state.addSpecialElement(state);
        }

        @Override
        public void clear() {
            BlockTransformElementRenderer.clear(state);
        }
    }

    public static class GuiPartialRenderBuilder extends AbstractRenderElement {
        private final PartialRenderState state = new PartialRenderState();
        private PartialModel model;
        private float scale = 1;
        private BiConsumer<MatrixStack, Float> transform;
        private float partialTicks;
        private int padding;
        private float xLocal, yLocal;

        public GuiPartialRenderBuilder() {
        }

        public GuiPartialRenderBuilder(PartialModel model) {
            this.model = model;
        }

        @Override
        public void render(DrawContext graphics) {
            if (model == null) {
                return;
            }
            state.update(graphics, model, x, y, xLocal, yLocal, scale, padding, partialTicks, transform);
            graphics.state.addSpecialElement(state);
        }

        public GuiPartialRenderBuilder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public GuiPartialRenderBuilder transform(BiConsumer<MatrixStack, Float> transform) {
            this.transform = transform;
            return this;
        }

        public GuiPartialRenderBuilder partial(PartialModel model) {
            this.model = model;
            return this;
        }

        public GuiPartialRenderBuilder padding(int padding) {
            this.padding = padding;
            return this;
        }

        public GuiPartialRenderBuilder atLocal(float x, float y) {
            xLocal = x;
            yLocal = y;
            return this;
        }

        public void markDirty() {
            state.dirty = true;
        }

        public void tick(float partialTicks) {
            this.partialTicks = partialTicks;
        }

        @Override
        public void clear() {
            PartialElementRenderer.clear(state);
        }
    }
}
