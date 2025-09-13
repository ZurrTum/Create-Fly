package com.zurrtum.create.client.content.trains.display;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.content.trains.display.FlapDisplayBlock;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.List;

public class FlapDisplayRenderer extends KineticBlockEntityRenderer<FlapDisplayBlockEntity> {

    public FlapDisplayRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FlapDisplayBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        TextRenderer fontRenderer = MinecraftClient.getInstance().textRenderer;
        FontStorage fontSet = fontRenderer.getFontStorage(Style.DEFAULT_FONT_ID);

        float scale = 1 / 32f;

        if (!be.isController)
            return;

        List<FlapDisplayLayout> lines = be.getLines();

        ms.push();
        TransformStack.of(ms).center().rotateYDegrees(AngleHelper.horizontalAngle(be.getCachedState().get(FlapDisplayBlock.HORIZONTAL_FACING)))
            .uncenter().translate(0, 0, -3 / 16f);

        ms.translate(0, 1, 1);
        ms.scale(scale, scale, scale);
        ms.scale(1, -1, 1);
        ms.translate(0, 0, 1 / 2f);

        for (int j = 0; j < lines.size(); j++) {
            List<FlapDisplaySection> line = lines.get(j).getSections();
            int color = getLineColor(be, j);
            ms.push();

            float w = 0;
            for (FlapDisplaySection section : line)
                w += section.getSize() + (section.hasGap ? 8 : 1);
            ms.translate(be.xSize * 16 - w / 2 + 1, 4.5f, 0);

            Entry transform = ms.peek();
            FlapDisplayRenderOutput renderOutput = new FlapDisplayRenderOutput(
                fontSet,
                buffer,
                color,
                transform.getPositionMatrix(),
                light,
                j,
                !be.isSpeedRequirementFulfilled(),
                be.getWorld(),
                be.isLineGlowing(j)
            );

            for (int i = 0; i < line.size(); i++) {
                FlapDisplaySection section = line.get(i);
                renderOutput.nextSection(section);
                int ticks = AnimationTickHolder.getTicks(be.getWorld());
                String text = section.renderCharsIndividually() || !section.spinning[0] ? section.text : section.cyclingOptions[((ticks / 3) + i * 13) % section.cyclingOptions.length];
                TextVisitFactory.visitFormatted(text, Style.EMPTY, renderOutput);
                ms.translate(section.getSize() + (section.hasGap ? 8 : 1), 0, 0);
            }

            if (buffer instanceof Immediate bs) {
                BakedGlyph texturedglyph = fontSet.getRectangleBakedGlyph();
                bs.draw(texturedglyph.getLayer(TextRenderer.TextLayerType.NORMAL));
            }

            ms.pop();
            ms.translate(0, 16, 0);
        }

        ms.pop();
    }

    public static int getLineColor(FlapDisplayBlockEntity be, int line) {
        DyeColor color = be.colour[line];
        return color == null ? 0xFF_D3C6BA : DyeHelper.getDyeColors(color).getFirst() | 0xFF_000000;
    }

    static class FlapDisplayRenderOutput implements CharacterVisitor {
        final FontStorage fontSet;
        final VertexConsumerProvider bufferSource;
        final int color;
        final int r, g, b, a;
        final Matrix4f pose;
        final int light;
        final boolean paused;

        FlapDisplaySection section;
        float x;
        private int lineIndex;
        private World level;

        public FlapDisplayRenderOutput(
            FontStorage fontSet,
            VertexConsumerProvider buffer,
            int color,
            Matrix4f pose,
            int light,
            int lineIndex,
            boolean paused,
            World level,
            boolean glowing
        ) {
            this.fontSet = fontSet;
            this.bufferSource = buffer;
            this.lineIndex = lineIndex;
            this.level = level;
            this.a = glowing ? 0xF8000000 : 0xD8000000;
            this.r = color >> 16 & 255;
            this.g = color >> 8 & 255;
            this.b = color & 255;
            this.color = color;
            this.pose = pose;
            this.light = glowing ? 0xf000f0 : light;
            this.paused = paused;
        }

        public void nextSection(FlapDisplaySection section) {
            this.section = section;
            x = 0;
        }

        public boolean accept(int charIndex, Style style, int glyph) {
            int ticks = paused ? 0 : AnimationTickHolder.getTicks(level);
            float time = paused ? 0 : AnimationTickHolder.getRenderTime(level);
            TextColor textcolor = style.getColor();
            boolean canDim = textcolor == null;
            boolean dim = false;

            if (section.renderCharsIndividually() && section.spinning[Math.min(charIndex, section.spinning.length)]) {
                float speed = section.spinningTicks > 5 && section.spinningTicks < 20 ? 1.75f : 2.5f;
                float cycle = (time / speed) + charIndex * 16.83f + lineIndex * 0.75f;
                float partial = cycle % 1;
                char cyclingGlyph = section.cyclingOptions[((int) cycle) % section.cyclingOptions.length].charAt(0);
                glyph = paused ? cyclingGlyph : partial > 1 / 2f ? partial > 3 / 4f ? '_' : '-' : cyclingGlyph;
                if (canDim)
                    dim = true;
            }

            Glyph glyphinfo = fontSet.getGlyph(glyph, false);
            float glyphWidth = glyphinfo.getAdvance(false);

            if (!section.renderCharsIndividually() && section.spinning[0]) {
                glyph = ticks % 3 == 0 ? glyphWidth == 6 ? '-' : glyphWidth == 1 ? '\'' : glyph : glyph;
                glyph = ticks % 3 == 2 ? glyphWidth == 6 ? '_' : glyphWidth == 1 ? '.' : glyph : glyph;
                if (canDim && ticks % 3 != 1)
                    dim = true;
            }

            BakedGlyph bakedglyph = style.isObfuscated() && glyph != 32 ? fontSet.getObfuscatedBakedGlyph(glyphinfo) : fontSet.getBaked(glyph);

            int drawColor = a;
            if (textcolor != null) {
                drawColor |= textcolor.getRgb();
            } else if (dim) {
                drawColor |= (r * 0xC0 >> 8 << 16) | (g * 0xC0 >> 8 << 8) | (b * 0xC0 >> 8);
            } else {
                drawColor |= color;
            }

            float standardWidth = section.wideFlaps ? FlapDisplaySection.WIDE_MONOSPACE : FlapDisplaySection.MONOSPACE;

            if (section.renderCharsIndividually())
                x += (standardWidth - glyphWidth) / 2f;

            if (isNotEmpty(bakedglyph)) {
                VertexConsumer vertexconsumer = bufferSource.getBuffer(renderTypeOf(bakedglyph));
                bakedglyph.draw(style.isItalic(), x, 0, 0, pose, vertexconsumer, drawColor, false, light);
            }

            if (section.renderCharsIndividually())
                x += standardWidth - (standardWidth - glyphWidth) / 2f;
            else
                x += glyphWidth;

            return true;
        }

        public float finish(int bgColor) {
            if (bgColor == 0)
                return x;

            BakedGlyph bakedglyph = fontSet.getRectangleBakedGlyph();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(renderTypeOf(bakedglyph));
            bakedglyph.drawRectangle(
                new BakedGlyph.Rectangle(-1f, -2f, section.getSize(), 9f, 0.01f, bgColor, 0, 0),
                pose,
                vertexconsumer,
                light,
                true
            );

            return x;
        }

        private RenderLayer renderTypeOf(BakedGlyph bakedglyph) {
            return bakedglyph.getLayer(TextRenderer.TextLayerType.NORMAL);
        }

        private boolean isNotEmpty(BakedGlyph bakedglyph) {
            return !(bakedglyph instanceof EmptyBakedGlyph);
        }

    }

    @Override
    protected SuperByteBuffer getRotatedModel(FlapDisplayBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_COGWHEEL, state, state.get(FlapDisplayBlock.HORIZONTAL_FACING));
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        //        return be.isController;
        return true;
    }

}
