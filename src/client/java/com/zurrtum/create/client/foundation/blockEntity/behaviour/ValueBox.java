package com.zurrtum.create.client.foundation.blockEntity.behaviour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.outliner.ChasingAABBOutline;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ValueBox extends ChasingAABBOutline {
    protected Component label;

    public int overrideColor = -1;
    public boolean isPassive;

    protected @Nullable ValueBoxTransform transform;

    protected BlockPos pos;
    protected BlockState blockState;
    protected ItemStackRenderState state;

    protected AllIcons outline = AllIcons.VALUE_BOX_HOVER_4PX;

    public ValueBox(Component label, AABB bb, BlockPos pos) {
        this(label, bb, pos, Minecraft.getInstance().level.getBlockState(pos));
    }

    public ValueBox(Component label, AABB bb, BlockPos pos, BlockState state) {
        super(bb);
        this.label = label;
        this.pos = pos;
        blockState = state;
        this.state = new ItemStackRenderState();
    }

    public ValueBox transform(ValueBoxTransform transform) {
        this.transform = transform;
        return this;
    }

    public ValueBox wideOutline() {
        outline = AllIcons.VALUE_BOX_HOVER_6PX;
        return this;
    }

    public ValueBox passive(boolean passive) {
        isPassive = passive;
        return this;
    }

    public ValueBox withColor(int color) {
        overrideColor = color;
        return this;
    }

    @Override
    public void submit(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera, float pt) {
        boolean hasTransform = transform != null;
        if (transform instanceof Sided && params.getHighlightedFace() != null) {
            ((Sided) transform).fromSide(params.getHighlightedFace());
        }
        if (hasTransform && !transform.shouldRender(blockState)) {
            return;
        }
        ms.pushPose();
        ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        float fontScale;
        int color;
        if (hasTransform) {
            transform.transform(blockState, ms);
            fontScale = -transform.getFontScale();
            color = transform.getOverrideColor();
        } else {
            fontScale = -1 / 64.0f;
            color = overrideColor;
        }
        if (!isPassive) {
            ms.pushPose();
            Pose pose = ms.last();
            pose.scale(-2.01f, -2.01f, 2.01f);
            pose.translate(-0.5f, -0.5f, -0.03125f);
            getOutline().submit(ms, queue, 0xffffffff);
            ms.popPose();
        }
        ms.scale(fontScale, fontScale, fontScale);
        submitContents(mc, ms, queue, color);
        ms.popPose();
    }

    public AllIcons getOutline() {
        return outline;
    }

    public void submitContents(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, int color) {
    }

    public static class ItemValueBox extends ValueBox {
        ItemStack stack;
        @Nullable MutableComponent count;

        public ItemValueBox(Component label, AABB bb, BlockPos pos, ItemStack stack, MutableComponent count) {
            super(label, bb, pos);
            this.stack = stack;
            this.count = count;
        }

        @Override
        public AllIcons getOutline() {
            if (!stack.isEmpty()) {
                return AllIcons.VALUE_BOX_HOVER_6PX;
            }
            return super.getOutline();
        }

        @Override
        public void submitContents(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, int color) {
            if (count == null) {
                return;
            }

            Font font = mc.font;
            Pose pose = ms.last();
            pose.translate(17.5f, -5, 7);

            ItemModelResolver itemModelManager = mc.getItemModelResolver();
            itemModelManager.updateForTopItem(state, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
            boolean blockItem = state.usesBlockLight();

            float scale = 1.5f;
            pose.translate(-font.width(count), 0, 0);

            if (stack.getItem() instanceof FilterItem) {
                pose.translate(-5, 8, 0);
                color = 0xFFFFFFFF;
            } else if (stack.isEmpty()) {
                pose.translate(-15, -1, -2.75f);
                scale = 1.65f;
                color = 0xFFEDEDED;
            } else {
                pose.translate(-7, 10, blockItem ? 10 + 1 / 4.0f : 0);
                color = 0xFFEDEDED;
            }

            if (count.getString().equals("*")) {
                pose.translate(-1, 3, 0);
            }

            pose.scale(scale, scale, scale);
            submitText(ms, queue, count, color, 0xff333333);
        }

    }

    public static class TextValueBox extends ValueBox {
        Component text;

        public TextValueBox(Component label, AABB bb, BlockPos pos, Component text) {
            super(label, bb, pos);
            this.text = text;
        }

        public TextValueBox(Component label, AABB bb, BlockPos pos, BlockState state, Component text) {
            super(label, bb, pos, state);
            this.text = text;
        }

        @Override
        public void submitContents(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, int color) {
            Pose pose = ms.last();
            Font font = mc.font;
            float scale = 3;
            pose.scale(scale, scale, 1);
            pose.translate(-4, -3.75f, 5);

            int stringWidth = font.width(text);
            float numberScale = (float) font.lineHeight / stringWidth;
            boolean singleDigit = stringWidth < 10;
            if (singleDigit) {
                numberScale = numberScale / 2;
            }
            float verticalMargin = (stringWidth - font.lineHeight) / 2.0f;

            pose.scale(numberScale, numberScale, numberScale);
            pose.translate(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin, 0);

            if (color == -1) {
                submitText(ms, queue, text, 0xFFEDEDED, 0xff333333);
            } else {
                submitText(ms, queue, text, color, 0);
            }
        }

    }

    public static class IconValueBox extends ValueBox {
        AllIcons icon;

        public IconValueBox(Component label, INamedIconOptions iconValue, AABB bb, BlockPos pos) {
            super(label, bb, pos);
            icon = iconValue.getIcon();
        }

        @Override
        public void submitContents(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, int color) {
            Pose pose = ms.last();
            float scale = 2 * 16;
            pose.scale(scale, scale, scale);
            pose.translate(-0.5f, -0.5f, 5 / 32.0f);
            icon.submit(ms, queue, color);
        }

    }

    private static void submitText(
        PoseStack ms,
        SubmitNodeCollector queue,
        Component text,
        int color,
        int outlineColor
    ) {
        queue.submitText(
            ms,
            0,
            0,
            text.getVisualOrderText(),
            false,
            DisplayMode.NORMAL,
            LightCoordsUtil.FULL_BRIGHT,
            color,
            0,
            outlineColor
        );
    }
}
