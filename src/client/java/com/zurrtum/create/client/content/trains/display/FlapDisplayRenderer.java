package com.zurrtum.create.client.content.trains.display;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.content.trains.display.FlapDisplayBlock;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.GlyphProvider;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FlapDisplayRenderer extends KineticBlockEntityRenderer<FlapDisplayBlockEntity, FlapDisplayRenderer.FlapDisplayRenderState> {
    protected final TextRenderer textRenderer;

    public FlapDisplayRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        textRenderer = context.textRenderer();
    }

    @Override
    public FlapDisplayRenderState createRenderState() {
        return new FlapDisplayRenderState();
    }

    @Override
    public void updateRenderState(
        FlapDisplayBlockEntity be,
        FlapDisplayRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (!be.isController) {
            return;
        }
        if (state.support) {
            BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        }
        FlapDisplayData display = new FlapDisplayData();
        List<FlapDisplayLayout> lines = be.getLines();
        boolean paused = !be.isSpeedRequirementFulfilled();
        World world = be.getWorld();
        int levelTicks = AnimationTickHolder.getTicks(world);
        int ticks = paused ? 0 : levelTicks;
        float time = paused ? 0 : AnimationTickHolder.getRenderTime(world);
        int size = lines.size();
        float y = 4.5f;
        int light = state.lightmapCoordinates;
        for (int j = 0; j < size; j++) {
            List<FlapDisplaySection> line = lines.get(j).getSections();
            int color = getLineColor(be, j);
            float w = 0;
            int count = line.size();
            float[] offsets = new float[count];
            for (int i = 0; i < count; i++) {
                FlapDisplaySection section = line.get(i);
                offsets[i] = w;
                w += section.getSize() + (section.hasGap ? 8 : 1);
            }
            float margin = be.xSize * 16 - w / 2 + 1;
            boolean glowing = be.isLineGlowing(j);
            FlapDisplayRenderOutput renderOutput = new FlapDisplayRenderOutput(
                y,
                textRenderer,
                color,
                j,
                paused,
                ticks,
                time,
                glowing,
                drawable -> display.add(light, glowing, drawable)
            );
            for (int i = 0; i < count; i++) {
                FlapDisplaySection section = line.get(i);
                renderOutput.nextSection(section, margin + offsets[i]);
                String text = section.renderCharsIndividually() || !section.spinning[0] ? section.text : section.cyclingOptions[((levelTicks / 3) + i * 13) % section.cyclingOptions.length];
                TextVisitFactory.visitFormatted(text, Style.EMPTY, renderOutput);
            }
            y += 16;
        }
        if (display.isEmpty()) {
            return;
        }
        display.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(state.blockState.get(FlapDisplayBlock.HORIZONTAL_FACING));
        state.display = display;
    }

    @Override
    public void render(FlapDisplayRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.display != null) {
            state.display.render(matrices, queue);
        }
    }

    public static int getLineColor(FlapDisplayBlockEntity be, int line) {
        DyeColor color = be.colour[line];
        return color == null ? 0xFF_D3C6BA : DyeHelper.getDyeColors(color).getFirst() | 0xFF_000000;
    }

    public static class FlapDisplayRenderOutput implements CharacterVisitor {
        final TextRenderer textRenderer;
        final int color;
        final int r, g, b, a;
        final boolean paused;
        final int ticks;
        final float time;
        final int lineIndex;
        final float y;
        final Consumer<TextDrawable> consumer;

        FlapDisplaySection section;
        float x;

        public FlapDisplayRenderOutput(
            float y,
            TextRenderer textRenderer,
            int color,
            int lineIndex,
            boolean paused,
            int ticks,
            float time,
            boolean glowing,
            Consumer<TextDrawable> consumer
        ) {
            this.y = y;
            this.textRenderer = textRenderer;
            this.lineIndex = lineIndex;
            this.paused = paused;
            this.ticks = ticks;
            this.time = time;
            this.a = glowing ? 0xF8000000 : 0xD8000000;
            this.r = color >> 16 & 255;
            this.g = color >> 8 & 255;
            this.b = color & 255;
            this.color = color;
            this.consumer = consumer;
        }

        public void nextSection(FlapDisplaySection section, float offset) {
            this.section = section;
            x = offset;
        }

        @Override
        public boolean accept(int charIndex, Style style, int glyph) {
            TextColor textcolor = style.getColor();
            boolean canDim = textcolor == null;
            boolean dim = false;

            if (section.renderCharsIndividually() && section.spinning[Math.min(charIndex, section.spinning.length)]) {
                float speed = section.spinningTicks > 5 && section.spinningTicks < 20 ? 1.75f : 2.5f;
                float cycle = (time / speed) + charIndex * 16.83f + lineIndex * 0.75f;
                float partial = cycle % 1;
                char cyclingGlyph = section.cyclingOptions[((int) cycle) % section.cyclingOptions.length].charAt(0);
                glyph = paused ? cyclingGlyph : partial > 1 / 2f ? partial > 3 / 4f ? '_' : '-' : cyclingGlyph;
                if (canDim) {
                    dim = true;
                }
            }

            GlyphProvider glyphProvider = textRenderer.getGlyphs(style.getFont());
            BakedGlyph bakedglyph = glyphProvider.get(glyph);
            float glyphWidth = bakedglyph.getMetrics().getAdvance(false);

            boolean obfuscated = style.isObfuscated();
            if (!section.renderCharsIndividually() && section.spinning[0]) {
                if (!obfuscated) {
                    int oldGlyph = glyph;
                    int i = ticks % 3;
                    if (i == 0) {
                        if (glyphWidth == 6) {
                            glyph = '-';
                        } else if (glyphWidth == 1) {
                            glyph = '\'';
                        }
                    } else if (i == 2) {
                        if (glyphWidth == 6) {
                            glyph = '_';
                        } else if (glyphWidth == 1) {
                            glyph = '.';
                        }
                    }
                    if (oldGlyph != glyph) {
                        bakedglyph = glyphProvider.get(glyph);
                    }
                }
                if (canDim && ticks % 3 != 1) {
                    dim = true;
                }
            }

            if (obfuscated && glyph != 32) {
                bakedglyph = glyphProvider.getObfuscated(textRenderer.random, MathHelper.ceil(glyphWidth));
            }

            int drawColor = a;
            if (textcolor != null) {
                drawColor |= textcolor.getRgb();
            } else if (dim) {
                drawColor |= (r * 0xC0 >> 8 << 16) | (g * 0xC0 >> 8 << 8) | (b * 0xC0 >> 8);
            } else {
                drawColor |= color;
            }

            float standardWidth = section.wideFlaps ? FlapDisplaySection.WIDE_MONOSPACE : FlapDisplaySection.MONOSPACE;

            if (section.renderCharsIndividually()) {
                x += (standardWidth - glyphWidth) / 2f;
            }
            TextDrawable textDrawable = bakedglyph.create(x, y, drawColor, 0, style, 0, 0);
            if (textDrawable != null) {
                consumer.accept(textDrawable);
            }
            if (section.renderCharsIndividually()) {
                x += standardWidth - (standardWidth - glyphWidth) / 2f;
            } else {
                x += glyphWidth;
            }
            return true;
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(FlapDisplayBlockEntity be, FlapDisplayRenderState state) {
        return CachedBuffers.partialFacingVertical(
            AllPartialModels.SHAFTLESS_COGWHEEL,
            state.blockState,
            state.blockState.get(FlapDisplayBlock.HORIZONTAL_FACING)
        );
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        //        return be.isController;
        return true;
    }

    public static class FlapDisplayRenderState extends KineticRenderState {
        public FlapDisplayData display;
    }

    public static class FlapDisplayData {
        public Map<RenderLayer, TextRenderState> map = new IdentityHashMap<>();
        public float yRot;

        public void add(int light, boolean glowing, TextDrawable textDrawable) {
            map.computeIfAbsent(textDrawable.getRenderLayer(TextLayerType.NORMAL), layer -> new TextRenderState(light)).add(glowing, textDrawable);
        }

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            matrices.push();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            matrices.translate(-0.5f, 0.5f, 0.3125f);
            matrices.scale(0.03125f, -0.03125f, 0.03125f);
            matrices.translate(0, 0, 0.5f);
            map.forEach((layer, state) -> queue.submitCustom(matrices, layer, state));
            matrices.pop();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }
    }

    public static class TextRenderState implements OrderedRenderCommandQueue.Custom {
        public List<TextDrawable> glowingText = new ArrayList<>();
        public List<TextDrawable> normalText = new ArrayList<>();
        public int light;

        public TextRenderState(int light) {
            this.light = light;
        }

        public void add(boolean glowing, TextDrawable textDrawable) {
            if (glowing) {
                glowingText.add(textDrawable);
            } else {
                normalText.add(textDrawable);
            }
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            Matrix4f pose = matricesEntry.getPositionMatrix();
            for (TextDrawable glyph : glowingText) {
                glyph.render(pose, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, true);
            }
            for (TextDrawable glyph : normalText) {
                glyph.render(pose, vertexConsumer, light, true);
            }
        }
    }
}
