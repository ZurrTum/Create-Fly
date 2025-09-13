package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.zurrtum.create.client.catnip.outliner.ChasingAABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

import java.lang.ref.WeakReference;

public class ValueBox extends ChasingAABBOutline {
    protected Text label;

    public int overrideColor = -1;
    public boolean isPassive;

    protected ValueBoxTransform transform;

    protected WeakReference<WorldAccess> level;
    protected BlockPos pos;
    protected BlockState blockState;

    protected AllIcons outline = AllIcons.VALUE_BOX_HOVER_4PX;

    public ValueBox(Text label, Box bb, BlockPos pos) {
        this(label, bb, pos, MinecraftClient.getInstance().world.getBlockState(pos));
    }

    public ValueBox(Text label, Box bb, BlockPos pos, BlockState state) {
        super(bb);
        this.label = label;
        this.pos = pos;
        this.blockState = state;
        this.level = new WeakReference<>(MinecraftClient.getInstance().world);
    }

    public ValueBox transform(ValueBoxTransform transform) {
        this.transform = transform;
        return this;
    }

    public ValueBox wideOutline() {
        this.outline = AllIcons.VALUE_BOX_HOVER_6PX;
        return this;
    }

    public ValueBox passive(boolean passive) {
        this.isPassive = passive;
        return this;
    }

    public ValueBox withColor(int color) {
        this.overrideColor = color;
        return this;
    }

    @Override
    public void render(MinecraftClient mc, MatrixStack ms, SuperRenderTypeBuffer buffer, Vec3d camera, float pt) {
        boolean hasTransform = transform != null;
        if (transform instanceof Sided && params.getHighlightedFace() != null)
            ((Sided) transform).fromSide(params.getHighlightedFace());

        WorldAccess levelAccessor = level.get();
        if (hasTransform && !transform.shouldRender(levelAccessor, pos, blockState))
            return;

        ms.push();
        ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        if (hasTransform)
            transform.transform(levelAccessor, pos, blockState, ms);

        if (!isPassive) {
            ms.push();
            ms.scale(-2.01f, -2.01f, 2.01f);
            ms.translate(-8 / 16.0, -8 / 16.0, -.5 / 16.0);
            getOutline().render(ms, buffer, 0xffffff);
            ms.pop();
        }

        float fontScale = hasTransform ? -transform.getFontScale() : -1 / 64f;
        ms.scale(fontScale, fontScale, fontScale);
        renderContents(mc, ms, buffer);

        ms.pop();
    }

    public AllIcons getOutline() {
        return outline;
    }

    public void renderContents(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer) {
    }

    public static class ItemValueBox extends ValueBox {
        ItemStack stack;
        MutableText count;

        public ItemValueBox(Text label, Box bb, BlockPos pos, ItemStack stack, MutableText count) {
            super(label, bb, pos);
            this.stack = stack;
            this.count = count;
        }

        @Override
        public AllIcons getOutline() {
            if (!stack.isEmpty())
                return AllIcons.VALUE_BOX_HOVER_6PX;
            return super.getOutline();
        }

        @Override
        public void renderContents(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer) {
            if (count == null)
                return;

            TextRenderer font = mc.textRenderer;
            ms.translate(17.5, -5, 7);

            boolean isFilter = stack.getItem() instanceof FilterItem;
            boolean isEmpty = stack.isEmpty();

            ItemRenderer renderer = mc.getItemRenderer();
            renderer.itemModelManager.clearAndUpdate(renderer.itemRenderState, stack, ItemDisplayContext.GUI, mc.world, mc.player, 0);
            boolean blockItem = renderer.itemRenderState.isSideLit();

            float scale = 1.5f;
            ms.translate(-font.getWidth(count), 0, 0);

            if (isFilter) {
                ms.translate(-5, 8, 0);
            } else if (isEmpty) {
                ms.translate(-15, -1, -2.75);
                scale = 1.65f;
            } else {
                ms.translate(-7, 10, blockItem ? 10 + 1 / 4f : 0);
            }

            if (count.getString().equals("*"))
                ms.translate(-1, 3, 0);

            ms.scale(scale, scale, scale);
            drawString8x(ms, buffer, count, 0, 0, isFilter ? 0xFFFFFFFF : 0xFFEDEDED);
        }

    }

    public static class TextValueBox extends ValueBox {
        Text text;

        public TextValueBox(Text label, Box bb, BlockPos pos, Text text) {
            super(label, bb, pos);
            this.text = text;
        }

        public TextValueBox(Text label, Box bb, BlockPos pos, BlockState state, Text text) {
            super(label, bb, pos, state);
            this.text = text;
        }

        @Override
        public void renderContents(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer) {
            TextRenderer font = mc.textRenderer;
            float scale = 3;
            ms.scale(scale, scale, 1);
            ms.translate(-4, -3.75, 5);

            int stringWidth = font.getWidth(text);
            float numberScale = (float) font.fontHeight / stringWidth;
            boolean singleDigit = stringWidth < 10;
            if (singleDigit)
                numberScale = numberScale / 2;
            float verticalMargin = (stringWidth - font.fontHeight) / 2f;

            ms.scale(numberScale, numberScale, numberScale);
            ms.translate(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin, 0);

            int overrideColor = transform.getOverrideColor();
            if (overrideColor == -1)
                drawString8x(ms, buffer, text, 0, 0, 0xFFEDEDED);
            else
                drawString(ms, buffer, text, 0, 0, overrideColor);
        }

    }

    public static class IconValueBox extends ValueBox {
        AllIcons icon;

        public IconValueBox(Text label, INamedIconOptions iconValue, Box bb, BlockPos pos) {
            super(label, bb, pos);
            icon = iconValue.getIcon();
        }

        @Override
        public void renderContents(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer) {
            float scale = 2 * 16;
            ms.scale(scale, scale, scale);
            ms.translate(-.5f, -.5f, 5 / 32f);

            int overrideColor = transform.getOverrideColor();
            icon.render(ms, buffer, overrideColor != -1 ? overrideColor : 0xFFFFFF);
        }

    }

    private static void drawString(MatrixStack ms, VertexConsumerProvider buffer, Text text, float x, float y, int color) {
        MinecraftClient.getInstance().textRenderer.draw(
            text,
            x,
            y,
            color,
            false,
            ms.peek().getPositionMatrix(),
            buffer,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
    }

    private static void drawString8x(MatrixStack ms, VertexConsumerProvider buffer, Text text, float x, float y, int color) {
        MinecraftClient.getInstance().textRenderer.drawWithOutline(
            text.asOrderedText(),
            x,
            y,
            color,
            0xff333333,
            ms.peek().getPositionMatrix(),
            buffer,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
    }

}
