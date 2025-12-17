package com.zurrtum.create.client.content.redstone.nixieTube;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class NixieTubeRenderer implements BlockEntityRenderer<NixieTubeBlockEntity, NixieTubeRenderer.NixieTubeRenderState> {
    protected final Font textRenderer;

    public NixieTubeRenderer(BlockEntityRendererProvider.Context context) {
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        Level level = be.getLevel();
        boolean inPonder = level instanceof PonderLevel;
        if (inPonder) {
            state.lightCoords = 0;
        } else {
            state.lightCoords = level != null ? LevelRenderer.getLightCoords(level, be.getBlockPos()) : 15728880;
        }
        state.breakProgress = crumblingOverlay;
        if (be.signalState != null) {
            updateSignalRenderState(be, state, cameraPos);
        } else {
            updateTextRenderState(textRenderer, be, state, inPonder);
        }
    }

    public static void updateTextRenderState(Font textRenderer, NixieTubeBlockEntity be, NixieTubeRenderState state, boolean inPonder) {
        TextRenderState data = new TextRenderState();
        DoubleAttachFace face = state.blockState.getValue(NixieTubeBlock.FACE);
        Direction facing = state.blockState.getValue(NixieTubeBlock.FACING);
        data.yRot = Mth.DEG_TO_RAD * (AngleHelper.horizontalAngle(facing) - 90 + (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0));
        data.zRot = Mth.DEG_TO_RAD * (face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0);
        if (face == DoubleAttachFace.CEILING || facing == Direction.DOWN) {
            data.zRot2 = Mth.DEG_TO_RAD * 180;
        }
        data.layer = RenderTypes.translucentMovingBlock();
        data.light = state.lightCoords;
        data.tube = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE, state.blockState);
        Couple<String> s = be.getDisplayedStrings();
        if (s != null) {
            DyeColor color = NixieTubeBlock.colorOf(state.blockState);
            float flicker = be.getLevel().getRandom().nextFloat();
            Couple<Integer> couple = DyeHelper.getDyeColors(color);
            int brightColor = couple.getFirst() | 0xFF000000;
            int darkColor = couple.getSecond() | 0xFF000000;
            int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);
            int y = face == DoubleAttachFace.CEILING ? -5 : -3;
            int light = inPonder ? 0 : LightCoordsUtil.FULL_BRIGHT;
            data.left = createTextDrawable(textRenderer, s.getFirst(), y, flickeringBrightColor, darkColor, light);
            data.right = createTextDrawable(textRenderer, s.getSecond(), y, flickeringBrightColor, darkColor, light);
        }
        state.data = data;
    }

    @Nullable
    public static TextDrawableState createTextDrawable(Font textRenderer, String text, int y, int flickeringBrightColor, int darkColor, int light) {
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
        TextRenderable mix = glyph.createGlyph(0, 0, Color.mixColors(darkColor, 0xFF000000, .35f), 0, Style.EMPTY, 0, 0);
        float x = (textRenderer.width(text) - .5f) / -2f;
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

    public static void updateSignalRenderState(NixieTubeBlockEntity be, NixieTubeRenderState state, Vec3 cameraPos) {
        SignalRenderState data = new SignalRenderState();
        state.data = data;
        DoubleAttachFace face = state.blockState.getValue(NixieTubeBlock.FACE);
        Direction facing = NixieTubeBlock.getFacing(state.blockState);
        data.yRot = Mth.DEG_TO_RAD * (AngleHelper.horizontalAngle(state.blockState.getValue(NixieTubeBlock.FACING)) - 90 + (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0));
        int zRot = face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0;
        if (facing == Direction.DOWN) {
            zRot += 180;
        }
        data.zRot = Mth.DEG_TO_RAD * zRot;
        data.light = state.lightCoords;
        data.layer = RenderTypes.solidMovingBlock();
        data.panel = CachedBuffers.partial(AllPartialModels.SIGNAL_PANEL, state.blockState);
        data.offset = facing == Direction.DOWN || state.blockState.getValue(NixieTubeBlock.FACE) == DoubleAttachFace.WALL_REVERSED ? 0.25f : -0.25f;
        SignalDrawableState left = data.left = new SignalDrawableState();
        SignalState signalState = be.signalState;
        float renderTime = AnimationTickHolder.getRenderTime(be.getLevel());
        boolean yellow = signalState.isYellowLight(renderTime);
        float longSide = yellow ? 1 : 4;
        float longSideGlow = yellow ? 2 : 5.125f;
        boolean vert = facing.getAxis().isHorizontal();
        double distance = Vec3.atCenterOf(state.blockPos).subtract(cameraPos).lengthSqr();
        left.light = state.lightCoords;
        left.layer = CreateRenderTypes.translucent();
        if (signalState.isRedLight(renderTime)) {
            left.additive = true;
            if (distance < 9216) {
                left.cubeLayer = left.layer;
                left.cube = CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, state.blockState);
                left.glow = CachedBuffers.partial(AllPartialModels.SIGNAL_RED_GLOW, state.blockState);
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
            left.layer = CreateRenderTypes.additive2();
            left.layer2 = CreateRenderTypes.additive();
            left.signal = CachedBuffers.partial(AllPartialModels.SIGNAL_RED, state.blockState);
        } else {
            left.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState);
        }
        SignalDrawableState right = data.right = new SignalDrawableState();
        right.light = state.lightCoords;
        right.layer = CreateRenderTypes.translucent();
        if (yellow || signalState.isGreenLight(renderTime)) {
            right.additive = true;
            if (distance < 9216) {
                right.cubeLayer = right.layer;
                right.cube = CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, state.blockState);
                right.glow = CachedBuffers.partial(
                    yellow ? AllPartialModels.SIGNAL_YELLOW_GLOW : AllPartialModels.SIGNAL_WHITE_GLOW,
                    state.blockState
                );
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
            right.layer = CreateRenderTypes.additive2();
            right.layer2 = CreateRenderTypes.additive();
            right.signal = CachedBuffers.partial(yellow ? AllPartialModels.SIGNAL_YELLOW : AllPartialModels.SIGNAL_WHITE, state.blockState);
        } else {
            right.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState);
        }
    }

    @Override
    public void submit(NixieTubeRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        state.data.render(matrices, queue);
    }

    public static void drawInWorldString(Font fontRenderer, PoseStack ms, MultiBufferSource buffer, String c, int color) {
        fontRenderer.drawInBatch(c, 0, 0, color, false, ms.last().pose(), buffer, DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static class NixieTubeRenderState extends BlockEntityRenderState {
        public NixieTubeRenderData data;
    }

    public interface NixieTubeRenderData {
        void render(PoseStack matrices, SubmitNodeCollector queue);
    }

    public static class TextRenderState implements NixieTubeRenderData, SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float yRot;
        public float zRot;
        public float zRot2;
        public TextDrawableState left;
        public TextDrawableState right;
        public SuperByteBuffer tube;
        public int light;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            tube.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(yRot));
            matrices.mulPose(Axis.ZP.rotation(zRot));
            if (zRot2 != 0) {
                matrices.pushPose();
                matrices.mulPose(Axis.ZP.rotation(zRot2));
                queue.order(1).submitCustomGeometry(matrices, layer, this);
                matrices.popPose();
            } else {
                queue.order(1).submitCustomGeometry(matrices, layer, this);
            }
            if (left != null) {
                matrices.pushPose();
                matrices.translate(-0.25f, 0, 0);
                matrices.scale(0.05f, -0.05f, 0.05f);
                queue.submitCustomGeometry(matrices, left.layer, left);
                matrices.popPose();
            }
            if (right != null) {
                matrices.translate(0.25f, 0, 0);
                matrices.scale(0.05f, -0.05f, 0.05f);
                queue.submitCustomGeometry(matrices, right.layer, right);
            }
            matrices.popPose();
        }
    }

    public record TextDrawableState(
        RenderType layer, float x, int y, TextRenderable bright, TextRenderable dark, TextRenderable mix, int light
    ) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
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

    public static class SignalRenderState implements NixieTubeRenderData, SubmitNodeCollector.CustomGeometryRenderer {
        public float yRot;
        public float zRot;
        public int light;
        public RenderType layer;
        public SuperByteBuffer panel;
        public float offset;
        public SignalDrawableState left;
        public SignalDrawableState right;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            panel.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(yRot));
            matrices.mulPose(Axis.ZP.rotation(zRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            queue.submitCustomGeometry(matrices, layer, this);
            matrices.translate(0.5f, 0.46875f, 0.5f);
            matrices.pushPose();
            matrices.translate(offset, 0, 0);
            left.render(matrices, queue);
            matrices.popPose();
            matrices.translate(-offset, 0, 0);
            right.render(matrices, queue);
            matrices.popPose();
        }
    }

    public static class SignalDrawableState {
        public RenderType layer;
        public RenderType layer2;
        public SuperByteBuffer signal;
        public int light;
        public RenderType cubeLayer;
        public SuperByteBuffer cube;
        public float cubeX;
        public float cubeY;
        public SuperByteBuffer glow;
        public float glowX;
        public float glowY;
        public boolean additive;

        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            if (ShadersModHelper.isShaderPackInUse()) {
                if (additive) {
                    queue.order(1).submitCustomGeometry(matrices, layer, (e, v) -> renderAdditive(e, v, 153));
                    if (cube != null) {
                        queue.order(1).submitCustomGeometry(matrices, cubeLayer, this::renderCube);
                    }
                    queue.order(2).submitCustomGeometry(matrices, layer2, (e, v) -> renderAdditive(e, v, 102));
                } else {
                    queue.order(1).submitCustomGeometry(matrices, layer, this::renderNormal);
                }
            } else {
                if (additive) {
                    queue.submitCustomGeometry(matrices, layer, (e, v) -> renderAdditive(e, v, 153));
                    if (cube != null) {
                        queue.submitCustomGeometry(matrices, cubeLayer, this::renderCube);
                    }
                    queue.submitCustomGeometry(matrices, layer2, (e, v) -> renderAdditive(e, v, 102));
                } else {
                    queue.submitCustomGeometry(matrices, layer, this::renderNormal);
                }
            }
        }

        public void renderCube(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            cube.light(LightCoordsUtil.FULL_BRIGHT).disableDiffuse().scale(cubeX, cubeY, 1).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderAdditive(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer, int color) {
            if (glow != null) {
                glow.light(LightCoordsUtil.FULL_BRIGHT).disableDiffuse().scale(glowX, glowY, 2).color(color, color, color, color)
                    .renderInto(matricesEntry, vertexConsumer);
            }
            signal.light(LightCoordsUtil.FULL_BRIGHT).disableDiffuse().scale(1.0625f).color(color, color, color, color)
                .renderInto(matricesEntry, vertexConsumer);
        }

        public void renderNormal(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            signal.light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
