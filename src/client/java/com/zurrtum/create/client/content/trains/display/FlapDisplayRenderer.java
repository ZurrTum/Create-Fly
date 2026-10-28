package com.zurrtum.create.client.content.trains.display;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.trains.display.FlapDisplayRenderer.FlapDisplayRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.content.trains.display.FlapDisplayBlock;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getProgress;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;

public class FlapDisplayRenderer implements BlockEntityRenderer<FlapDisplayBlockEntity, FlapDisplayRenderState> {
    protected final Font textRenderer;

    public FlapDisplayRenderer(Context context) {
        textRenderer = context.font();
    }

    @Override
    public boolean shouldRender(FlapDisplayBlockEntity blockEntity, Vec3 cameraPosition) {
        if (BlockEntityRenderer.super.shouldRender(blockEntity, cameraPosition)) {
            if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) {
                return blockEntity.isController;
            }
            return true;
        }
        return false;
    }

    @Override
    public FlapDisplayRenderState createRenderState() {
        return new FlapDisplayRenderState();
    }

    @Override
    public void extractRenderState(
        FlapDisplayBlockEntity be,
        FlapDisplayRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        Direction facing = state.blockState.getValue(FlapDisplayBlock.HORIZONTAL_FACING);
        float time = AnimationTickHolder.getRenderTime(level);
        if (!VisualizationManager.supportsVisualization(level)) {
            state.model = CachedBuffers.partialFacingVertical(
                AllPartialModels.SHAFTLESS_COGWHEEL,
                state.blockState,
                facing
            ).cardinalLighting(level).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            Direction.Axis axis = facing.getAxis();
            float progress = getProgress(time, be.getSpeed());
            float offset = rotationOffset(state.blockState, axis, state.blockPos);
            state.axis = axis;
            state.angle = (progress + offset) % 360;
            if (!be.isController) {
                return;
            }
        }
        FlapDisplayData display = new FlapDisplayData();
        List<FlapDisplayLayout> lines = be.getLines();
        int levelTicks = AnimationTickHolder.getTicks(level);
        boolean paused = !be.isSpeedRequirementFulfilled();
        int ticks;
        if (paused) {
            ticks = 0;
            time = 0;
        } else {
            ticks = levelTicks;
        }
        int size = lines.size();
        float y = 4.5f;
        int light = state.lightCoords;
        int xSize = be.xSize;
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
            float margin = xSize * 16 - w / 2 + 1;
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
                String text = section.renderCharsIndividually() || !section.spinning[0] ? section.text :
                    section.cyclingOptions[(levelTicks / 3 + i * 13) % section.cyclingOptions.length];
                StringDecomposer.iterateFormatted(text, Style.EMPTY, renderOutput);
            }
            y += 16;
        }
        if (display.isEmpty()) {
            return;
        }
        display.yRadians = Radians.horizontal(facing);
        state.display = display;
    }

    @Override
    public void submit(
        FlapDisplayRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.model != null) {
            if (state.angle != 0) {
                matrices.pushPose();
                matrices.rotateCentered(state.axis, Mth.DEG_TO_RAD * state.angle);
                state.model.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.model.submit(matrices, queue);
            }
        }
        if (state.display != null) {
            state.display.submit(matrices, queue);
        }
    }

    public static int getLineColor(FlapDisplayBlockEntity be, int line) {
        DyeColor color = be.colour[line];
        return color == null ? 0xFF_D3C6BA : DyeHelper.getDyeColors(color).getFirst() | 0xFF_000000;
    }

    public static class FlapDisplayRenderOutput implements FormattedCharSink {
        final Font textRenderer;
        final int color;
        final int r, g, b, a;
        final boolean paused;
        final int ticks;
        final float time;
        final int lineIndex;
        final float y;
        final Consumer<TextRenderable> consumer;

        FlapDisplaySection section;
        float x;

        public FlapDisplayRenderOutput(
            float y,
            Font textRenderer,
            int color,
            int lineIndex,
            boolean paused,
            int ticks,
            float time,
            boolean glowing,
            Consumer<TextRenderable> consumer
        ) {
            this.y = y;
            this.textRenderer = textRenderer;
            this.lineIndex = lineIndex;
            this.paused = paused;
            this.ticks = ticks;
            this.time = time;
            a = glowing ? 0xF8000000 : 0xD8000000;
            r = color >> 16 & 255;
            g = color >> 8 & 255;
            b = color & 255;
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
                float cycle = time / speed + charIndex * 16.83f + lineIndex * 0.75f;
                float partial = cycle % 1;
                char cyclingGlyph = section.cyclingOptions[(int) cycle % section.cyclingOptions.length].charAt(0);
                glyph = paused ? cyclingGlyph : partial > 0.5f ? partial > 0.75f ? '_' : '-' : cyclingGlyph;
                if (canDim) {
                    dim = true;
                }
            }

            GlyphSource glyphProvider = textRenderer.getGlyphSource(style.getFont());
            BakedGlyph bakedglyph = glyphProvider.getGlyph(glyph);
            float glyphWidth = bakedglyph.info().getAdvance(false);

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
                        bakedglyph = glyphProvider.getGlyph(glyph);
                    }
                }
                if (canDim && ticks % 3 != 1) {
                    dim = true;
                }
            }

            if (obfuscated && glyph != 32) {
                bakedglyph = glyphProvider.getRandomGlyph(textRenderer.random, Mth.ceil(glyphWidth));
            }

            int drawColor = a;
            if (textcolor != null) {
                drawColor |= textcolor.getValue();
            } else if (dim) {
                drawColor |= r * 0xC0 >> 8 << 16 | g * 0xC0 >> 8 << 8 | b * 0xC0 >> 8;
            } else {
                drawColor |= color;
            }

            float standardWidth = section.wideFlaps ? FlapDisplaySection.WIDE_MONOSPACE : FlapDisplaySection.MONOSPACE;

            if (section.renderCharsIndividually()) {
                x += (standardWidth - glyphWidth) / 2.0f;
            }
            TextRenderable textDrawable = bakedglyph.createGlyph(x, y, drawColor, 0, style, 0, 0);
            if (textDrawable != null) {
                consumer.accept(textDrawable);
            }
            if (section.renderCharsIndividually()) {
                x += standardWidth - (standardWidth - glyphWidth) / 2.0f;
            } else {
                x += glyphWidth;
            }
            return true;
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        //        return be.isController;
        return true;
    }

    public static class FlapDisplayRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState model;
        public Direction.@UnknownNullability Axis axis;
        public float angle;
        public @Nullable FlapDisplayData display;
    }

    public static class FlapDisplayData {
        public Map<RenderType, TextRenderState> map = new IdentityHashMap<>();
        public float yRadians;

        public void add(int light, boolean glowing, TextRenderable textDrawable) {
            map.computeIfAbsent(textDrawable.renderType(DisplayMode.NORMAL), layer -> new TextRenderState(light))
                .add(glowing, textDrawable);
        }

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.rotateYCentered(yRadians);
            pose.translate(0, 1.0f, 0.8125f);
            pose.scale(0.03125f, -0.03125f, 0.03125f);
            pose.translate(0, 0, 0.5f);
            map.forEach((layer, state) -> queue.submitCustomGeometry(matrices, layer, state));
            matrices.popPose();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }
    }

    public static class TextRenderState implements CustomGeometryRenderer {
        public List<TextRenderable> glowingText = new ArrayList<>();
        public List<TextRenderable> normalText = new ArrayList<>();
        public int light;

        public TextRenderState(int light) {
            this.light = light;
        }

        public void add(boolean glowing, TextRenderable textDrawable) {
            if (glowing) {
                glowingText.add(textDrawable);
            } else {
                normalText.add(textDrawable);
            }
        }

        @Override
        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            Matrix4f pose = matricesEntry.pose();
            for (TextRenderable glyph : glowingText) {
                glyph.render(pose, vertexConsumer, LightCoordsUtil.FULL_BRIGHT, true);
            }
            for (TextRenderable glyph : normalText) {
                glyph.render(pose, vertexConsumer, light, true);
            }
        }
    }
}
