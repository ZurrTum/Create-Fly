package com.zurrtum.create.client.content.redstone.nixieTube;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.redstone.nixieTube.NixieTubeRenderer.NixieTubeRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity.ComputerSignal.TubeDisplay;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class NixieTubeRenderer implements BlockEntityRenderer<NixieTubeBlockEntity, NixieTubeRenderState> {
    protected final Font textRenderer;

    public NixieTubeRenderer(Context context) {
        textRenderer = context.font();
    }

    @Override
    public NixieTubeRenderState createRenderState() {
        return new NixieTubeRenderState();
    }

    @Override
    public void extractRenderState(
        NixieTubeBlockEntity be,
        NixieTubeRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        if (be.signalState != null || be.computerSignal != null) {
            updateSignalRenderState(be, state, cameraPos, crumblingOverlay);
        } else {
            updateTextRenderState(textRenderer, be, state, crumblingOverlay);
        }
    }

    public static void updateTextRenderState(
        Font textRenderer,
        NixieTubeBlockEntity be,
        NixieTubeRenderState state,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        TextRenderState data = new TextRenderState();
        DoubleAttachFace face = state.blockState.getValue(NixieTubeBlock.FACE);
        Direction facing = state.blockState.getValue(NixieTubeBlock.FACING);
        if (face == DoubleAttachFace.WALL_REVERSED) {
            data.yRadians = Radians.horizontalPlus90(facing);
            data.zRadians = Radians.DEG_90;
        } else {
            data.yRadians = Radians.horizontalPlus270(facing);
            data.zRadians = face == DoubleAttachFace.WALL ? Radians.DEG_270 : 0;
        }
        data.zRot2 = face == DoubleAttachFace.CEILING || facing == Direction.DOWN;
        data.tube = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE, state.blockState).cardinalLighting(level)
            .light(state.lightCoords).extractRenderState();
        Couple<String> s = be.getDisplayedStrings();
        if (s != null) {
            DyeColor color = NixieTubeBlock.colorOf(state.blockState);
            float flicker = level.getRandom().nextFloat();
            Couple<Integer> couple = DyeHelper.getDyeColors(color);
            int brightColor = couple.getFirst() | 0xFF000000;
            int darkColor = couple.getSecond() | 0xFF000000;
            int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);
            int y = face == DoubleAttachFace.CEILING ? -5 : -3;
            data.left = createTextDrawable(
                textRenderer,
                s.getFirst(),
                y,
                flickeringBrightColor,
                darkColor,
                LightCoordsUtil.FULL_BRIGHT
            );
            data.right = createTextDrawable(
                textRenderer,
                s.getSecond(),
                y,
                flickeringBrightColor,
                darkColor,
                LightCoordsUtil.FULL_BRIGHT
            );
        }
        state.data = data;
    }

    @Nullable
    public static TextDrawableState createTextDrawable(
        Font textRenderer,
        String text,
        int y,
        int flickeringBrightColor,
        int darkColor,
        int light
    ) {
        int code = visit(text);
        if (code == ' ') {
            return null;
        }
        BakedGlyph glyph = textRenderer.getGlyphSource(Style.EMPTY.getFont()).getGlyph(code);
        TextRenderable bright = glyph.createGlyph(0, 0, flickeringBrightColor, 0, Style.EMPTY, 0, 0);
        if (bright == null) {
            return null;
        }
        TextRenderable dark = glyph.createGlyph(0, 0, darkColor, 0, Style.EMPTY, 0, 0);
        TextRenderable mix = glyph.createGlyph(
            0,
            0,
            Color.mixColors(darkColor, 0xFF000000, 0.35f),
            0,
            Style.EMPTY,
            0,
            0
        );
        float x = (textRenderer.width(text) - 0.5f) / -2.0f;
        return new TextDrawableState(bright.renderType(DisplayMode.NORMAL), x, y, bright, dark, mix, light);
    }

    public static int visit(String text) {
        int length = text.length();
        if (length == 0) {
            return ' ';
        }
        char c = text.charAt(0);
        if (Character.isHighSurrogate(c)) {
            if (length == 1) {
                return 65533;
            }
            char d = text.charAt(1);
            if (Character.isLowSurrogate(d)) {
                return Character.toCodePoint(c, d);
            }
            return 65533;
        }
        if (Character.isSurrogate(c)) {
            return 65533;
        }
        return c;
    }

    public static void updateSignalRenderState(
        NixieTubeBlockEntity be,
        NixieTubeRenderState state,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        SignalRenderState data = new SignalRenderState();
        state.data = data;
        DoubleAttachFace face = state.blockState.getValue(NixieTubeBlock.FACE);
        Direction facing = NixieTubeBlock.getFacing(state.blockState);
        Direction direction = state.blockState.getValue(NixieTubeBlock.FACING);
        if (face == DoubleAttachFace.WALL_REVERSED) {
            data.yRadians = Radians.horizontalPlus90(direction);
            data.zRadians = facing == Direction.DOWN ? Radians.DEG_270 : Radians.DEG_90;
        } else if (face == DoubleAttachFace.WALL) {
            data.yRadians = Radians.horizontalPlus270(direction);
            data.zRadians = facing == Direction.DOWN ? Radians.DEG_90 : Radians.DEG_270;
        } else {
            data.yRadians = Radians.horizontalPlus270(direction);
            data.zRadians = facing == Direction.DOWN ? Radians.DEG_180 : 0;
        }
        data.panel = CachedBuffers.partial(AllPartialModels.SIGNAL_PANEL, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        data.offset =
            facing == Direction.DOWN || state.blockState.getValue(NixieTubeBlock.FACE) == DoubleAttachFace.WALL_REVERSED ?
                0.25f : -0.25f;

        float renderTime = AnimationTickHolder.getRenderTime(level);
        double distance = Vec3.atCenterOf(state.blockPos).subtract(cameraPos).lengthSqr();
        boolean vert = facing.getAxis().isHorizontal();

        if (be.signalState != null) {
            SignalDrawableState left = data.left = new SignalDrawableState();
            SignalState signalState = be.signalState;
            boolean yellow = signalState.isYellowLight(renderTime);
            float longSide = yellow ? 1 : 4;
            float longSideGlow = yellow ? 2 : 5.125f;
            if (signalState.isRedLight(renderTime)) {
                left.additive = true;
                if (distance < 9216) {
                    left.cube = CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, state.blockState)
                        .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                        .extractRenderState();
                    left.glow = CachedBuffers.partial(AllPartialModels.SIGNAL_RED_GLOW, state.blockState)
                        .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                        .extractRenderState();
                    if (vert) {
                        left.cubeX = 1;
                        left.cubeY = longSide;
                        left.glowX = 2;
                        left.glowY = longSideGlow;
                    } else {
                        left.cubeX = longSide;
                        left.cubeY = 1;
                        left.glowX = longSideGlow;
                        left.glowY = 2;
                    }
                }
                left.signal = CachedBuffers.partial(AllPartialModels.SIGNAL_RED, state.blockState)
                    .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                    .extractRenderState();
            } else {
                left.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState)
                    .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            }
            SignalDrawableState right = data.right = new SignalDrawableState();
            if (yellow || signalState.isGreenLight(renderTime)) {
                right.additive = true;
                if (distance < 9216) {
                    right.cube = CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, state.blockState)
                        .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                        .extractRenderState();
                    right.glow = CachedBuffers.partial(
                            yellow ? AllPartialModels.SIGNAL_YELLOW_GLOW : AllPartialModels.SIGNAL_WHITE_GLOW,
                            state.blockState
                        ).cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                        .extractRenderState();
                    if (vert) {
                        right.cubeX = longSide;
                        right.cubeY = 1;
                        right.glowX = longSideGlow;
                        right.glowY = 2;
                    } else {
                        right.cubeX = 1;
                        right.cubeY = longSide;
                        right.glowX = 2;
                        right.glowY = longSideGlow;
                    }
                }
                right.signal = CachedBuffers.partial(
                        yellow ? AllPartialModels.SIGNAL_YELLOW : AllPartialModels.SIGNAL_WHITE,
                        state.blockState
                    ).cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                    .extractRenderState();
            } else {
                right.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState)
                    .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            }
        } else if (be.computerSignal != null) {
            for (boolean first : Iterate.trueAndFalse) {
                SignalDrawableState cState = new SignalDrawableState();
                TubeDisplay tubeDisplay;
                if (first) {
                    tubeDisplay = be.computerSignal.first;
                    data.left = cState;
                } else {
                    tubeDisplay = be.computerSignal.second;
                    data.right = cState;
                }
                if (tubeDisplay.blinkPeriod == 0 || tubeDisplay.blinkPeriod > 1 && renderTime % tubeDisplay.blinkPeriod < tubeDisplay.blinkOffTime) {
                    cState.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState)
                        .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
                    continue;
                }
                cState.additive = true;
                if (distance < 9216) {
                    cState.cube = CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_CUBE, state.blockState)
                        .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                        .extractRenderState();
                    cState.glow = CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_GLOW, state.blockState)
                        .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT).color(
                            Math.min((tubeDisplay.r & 0xFF) * 6 + 256 >> 3, 255) >> 1,
                            Math.min((tubeDisplay.g & 0xFF) * 6 + 256 >> 3, 255) >> 1,
                            Math.min((tubeDisplay.b & 0xFF) * 6 + 256 >> 3, 255) >> 1,
                            127
                        ).extractRenderState();
                    float width = vert ? tubeDisplay.glowHeight : tubeDisplay.glowWidth;
                    float height = vert ? tubeDisplay.glowWidth : tubeDisplay.glowHeight;
                    cState.cubeX = width;
                    cState.cubeY = height;
                    cState.glowX = width + 1.125f;
                    cState.glowY = height + 1.125f;
                }
                cState.signal = CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE, state.blockState)
                    .cardinalLighting(cardinalLighting).disableDiffuse().light(LightCoordsUtil.FULL_BRIGHT)
                    .color(tubeDisplay.r >> 1, tubeDisplay.g >> 1, tubeDisplay.b >> 1, 127).extractRenderState();
            }
        }
    }

    @Override
    public void submit(
        NixieTubeRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        state.data.submit(matrices, queue);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class NixieTubeRenderState extends BlockEntityRenderState {
        public @UnknownNullability NixieTubeRenderData data;
    }

    public interface NixieTubeRenderData {
        void submit(PoseStack matrices, SubmitNodeCollector queue);
    }

    public static class TextRenderState implements NixieTubeRenderData {
        public float yRadians;
        public float zRadians;
        public boolean zRot2;
        public @Nullable TextDrawableState left;
        public @Nullable TextDrawableState right;
        public @UnknownNullability SuperByteBufferRenderState tube;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            pose.translate(0.5f, 0.5f, 0.5f);
            if (yRadians != 0) {
                pose.rotate(Axis.YP, yRadians);
            }
            if (zRadians != 0) {
                pose.rotate(Axis.ZP, zRadians);
            }
            if (zRot2) {
                matrices.pushPose();
                matrices.rotate(Axis.ZP, Radians.DEG_180);
                tube.submit(matrices, queue.order(1));
                matrices.popPose();
            } else {
                tube.submit(matrices, queue.order(1));
            }
            if (left != null) {
                matrices.pushPose();
                pose = matrices.last();
                pose.translate(-0.25f, 0, 0);
                pose.scale(0.05f, -0.05f, 0.05f);
                queue.submitCustomGeometry(matrices, left.layer, left);
                matrices.popPose();
            }
            if (right != null) {
                pose = matrices.last();
                pose.translate(0.25f, 0, 0);
                pose.scale(0.05f, -0.05f, 0.05f);
                queue.submitCustomGeometry(matrices, right.layer, right);
            }
            matrices.popPose();
        }
    }

    public record TextDrawableState(RenderType layer, float x, int y, TextRenderable bright, TextRenderable dark,
                                    TextRenderable mix, int light) implements CustomGeometryRenderer {
        @Override
        public void render(Pose matricesEntry, VertexConsumer vertexConsumer) {
            Matrix4f pose = matricesEntry.pose();
            pose.translate(x, y, 0);
            bright.render(pose, vertexConsumer, light, false);
            pose.translate(0.5f, 0.5f, -0.0625f);
            dark.render(pose, vertexConsumer, light, false);
            pose.scale(-1, 1, 1);
            pose.translate(0.5f + x + x, -0.5f, 0.0625f);
            dark.render(pose, vertexConsumer, light, false);
            pose.translate(-0.5f, 0.5f, -0.0625f);
            mix.render(pose, vertexConsumer, light, false);
        }
    }

    public static class SignalRenderState implements NixieTubeRenderData {
        public float yRadians;
        public float zRadians;
        public @UnknownNullability SuperByteBufferRenderState panel;
        public float offset;
        public @UnknownNullability SignalDrawableState left;
        public @UnknownNullability SignalDrawableState right;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            if (yRadians != 0 || zRadians != 0) {
                pose.translate(0.5f, 0.5f, 0.5f);
                if (yRadians != 0) {
                    pose.rotate(Axis.YP, yRadians);
                }
                if (zRadians != 0) {
                    pose.rotate(Axis.ZP, zRadians);
                }
                pose.translate(-0.5f, -0.5f, -0.5f);
            }
            panel.submit(matrices, queue);
            pose.translate(0.5f, 0.46875f, 0.5f);
            matrices.pushPose();
            matrices.translate(offset, 0, 0);
            left.render(matrices, queue);
            matrices.popPose();
            pose.translate(-offset, 0, 0);
            right.render(matrices, queue);
            matrices.popPose();
        }
    }

    public static class SignalDrawableState {
        public @UnknownNullability SuperByteBufferRenderState signal;
        public @Nullable SuperByteBufferRenderState cube;
        public @Nullable SuperByteBufferRenderState glow;
        public float cubeX;
        public float cubeY;
        public float glowX;
        public float glowY;
        public boolean additive;

        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            if (additive) {
                if (cube != null) {
                    matrices.pushPose();
                    matrices.scale(cubeX, cubeY, 1);
                    cube.submit(matrices, queue);
                    matrices.popPose();
                }
                matrices.pushPose();
                matrices.scale(1.0625f, 1.0625f, 1.0625f);
                signal.submit(CreateRenderTypes.additive(), matrices, queue);
                matrices.popPose();
                if (glow != null) {
                    matrices.pushPose();
                    matrices.scale(glowX, glowY, 2);
                    glow.submit(CreateRenderTypes.additive(), matrices, queue);
                    matrices.popPose();
                }
            } else {
                signal.submit(matrices, queue);
            }
        }
    }
}
