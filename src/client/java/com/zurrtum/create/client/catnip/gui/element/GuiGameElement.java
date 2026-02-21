package com.zurrtum.create.client.catnip.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2fStack;

import java.util.function.BiConsumer;

public class GuiGameElement {
    public static GuiItemRenderBuilder of(ItemStack stack) {
        return new GuiItemRenderBuilder(stack);
    }

    public static GuiItemRenderBuilder of(Item item) {
        return new GuiItemRenderBuilder(item.getDefaultInstance());
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
            xRot = Mth.DEG_TO_RAD * x;
            yRot = Mth.DEG_TO_RAD * y;
            zRot = Mth.DEG_TO_RAD * z;
            return self();
        }

        public T scale(float scale) {
            this.scale = scale;
            return self();
        }
    }

    public static class GuiItemRenderBuilder extends GuiRenderBuilder<GuiItemRenderBuilder> {
        private final ItemTransformRenderKey key;

        public GuiItemRenderBuilder(ItemStack stack) {
            key = new ItemTransformRenderKey(stack);
        }

        @Override
        GuiItemRenderBuilder self() {
            return this;
        }

        @Override
        public void render(GuiGraphics graphics) {
            if (scale <= 1 && xRot == 0 && yRot == 0 && zRot == 0) {
                if (scale == 1) {
                    graphics.renderItem(key.stack, (int) x, (int) y);
                } else {
                    Matrix3x2fStack matrices = graphics.pose();
                    matrices.pushMatrix();
                    matrices.scale(scale);
                    graphics.renderItem(key.stack, (int) x, (int) y);
                    matrices.popMatrix();
                }
                return;
            }
            key.update(scale, padding, xRot, yRot, zRot);
            graphics.guiRenderState.submitPicturesInPictureState(ItemTransformRenderState.create(graphics, key, x, y));
        }

        @Override
        public void clear() {
            ItemTransformElementRenderer.clear(key);
        }

        public GuiItemRenderBuilder copy() {
            GuiItemRenderBuilder builder = new GuiItemRenderBuilder(key.stack);
            builder.scale = scale;
            builder.padding = padding;
            builder.xRot = xRot;
            builder.yRot = yRot;
            builder.zRot = zRot;
            builder.x = x;
            builder.y = y;
            return builder;
        }
    }

    public static class GuiBlockStateRenderBuilder extends GuiRenderBuilder<GuiBlockStateRenderBuilder> {
        private final BlockTransformRenderKey key;

        public GuiBlockStateRenderBuilder(BlockState block) {
            key = new BlockTransformRenderKey(block);
        }

        @Override
        GuiBlockStateRenderBuilder self() {
            return this;
        }

        @Override
        public void render(GuiGraphics graphics) {
            key.update(scale, padding, xRot, yRot, zRot);
            graphics.guiRenderState.submitPicturesInPictureState(BlockTransformRenderState.create(graphics, key, x, y));
        }

        @Override
        public void clear() {
            BlockTransformElementRenderer.clear(key);
        }
    }

    public static class GuiPartialRenderBuilder extends AbstractRenderElement {
        private final PartialRenderState state = new PartialRenderState();
        private PartialModel model;
        private float scale = 1;
        private BiConsumer<PoseStack, Float> transform;
        private float partialTicks;
        private int padding;
        private float xLocal, yLocal;

        public GuiPartialRenderBuilder() {
        }

        public GuiPartialRenderBuilder(PartialModel model) {
            this.model = model;
        }

        @Override
        public void render(GuiGraphics graphics) {
            if (model == null) {
                return;
            }
            state.update(graphics, model, x, y, xLocal, yLocal, scale, padding, partialTicks, transform);
            graphics.guiRenderState.submitPicturesInPictureState(state);
        }

        public GuiPartialRenderBuilder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public GuiPartialRenderBuilder transform(BiConsumer<PoseStack, Float> transform) {
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
