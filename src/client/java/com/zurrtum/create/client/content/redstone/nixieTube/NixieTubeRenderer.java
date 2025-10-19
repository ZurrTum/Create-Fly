package com.zurrtum.create.client.content.redstone.nixieTube;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class NixieTubeRenderer implements BlockEntityRenderer<NixieTubeBlockEntity, NixieTubeRenderer.NixieTubeRenderState> {
    protected final TextRenderer textRenderer;

    public NixieTubeRenderer(BlockEntityRendererFactory.Context context) {
        textRenderer = context.textRenderer();
    }

    @Override
    public NixieTubeRenderState createRenderState() {
        return new NixieTubeRenderState();
    }

    @Override
    public void updateRenderState(
        NixieTubeBlockEntity be,
        NixieTubeRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        if (be.signalState != null) {
            updateSignalRenderState(be, state, cameraPos);
        } else {
            updateTextRenderState(textRenderer, be, state);
        }
    }

    public static void updateTextRenderState(TextRenderer textRenderer, NixieTubeBlockEntity be, NixieTubeRenderState state) {
        TextRenderState data = new TextRenderState();
        DoubleAttachFace face = state.blockState.get(NixieTubeBlock.FACE);
        Direction facing = state.blockState.get(NixieTubeBlock.FACING);
        data.yRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.horizontalAngle(facing) - 90 + (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0));
        data.zRot = MathHelper.RADIANS_PER_DEGREE * (face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0);
        if (face == DoubleAttachFace.CEILING || facing == Direction.DOWN) {
            data.zRot2 = MathHelper.RADIANS_PER_DEGREE * 180;
        }
        data.layer = PonderRenderTypes.translucent();
        data.light = state.lightmapCoordinates;
        data.tube = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE, state.blockState);
        Couple<String> s = be.getDisplayedStrings();
        if (s != null) {
            DyeColor color = NixieTubeBlock.colorOf(state.blockState);
            float flicker = be.getWorld().random.nextFloat();
            Couple<Integer> couple = DyeHelper.getDyeColors(color);
            int brightColor = couple.getFirst() | 0xFF000000;
            int darkColor = couple.getSecond() | 0xFF000000;
            int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);
            int y = face == DoubleAttachFace.CEILING ? -5 : -3;
            data.left = createTextDrawable(textRenderer, s.getFirst(), y, flickeringBrightColor, darkColor);
            data.right = createTextDrawable(textRenderer, s.getSecond(), y, flickeringBrightColor, darkColor);
        }
        state.data = data;
    }

    @Nullable
    public static TextDrawableState createTextDrawable(TextRenderer textRenderer, String text, int y, int flickeringBrightColor, int darkColor) {
        int code = visit(text);
        if (code == ' ') {
            return null;
        }
        BakedGlyph glyph = textRenderer.getGlyphs(Style.EMPTY.getFont()).get(code);
        TextDrawable bright = glyph.create(0, 0, flickeringBrightColor, 0, Style.EMPTY, 0, 0);
        if (bright == null) {
            return null;
        }
        TextDrawable dark = glyph.create(0, 0, darkColor, 0, Style.EMPTY, 0, 0);
        TextDrawable mix = glyph.create(0, 0, Color.mixColors(darkColor, 0xFF000000, .35f), 0, Style.EMPTY, 0, 0);
        float x = (textRenderer.getWidth(text) - .5f) / -2f;
        return new TextDrawableState(bright.getRenderLayer(TextLayerType.NORMAL), x, y, bright, dark, mix);
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

    public static void updateSignalRenderState(NixieTubeBlockEntity be, NixieTubeRenderState state, Vec3d cameraPos) {
        SignalRenderState data = new SignalRenderState();
        state.data = data;
        DoubleAttachFace face = state.blockState.get(NixieTubeBlock.FACE);
        Direction facing = NixieTubeBlock.getFacing(state.blockState);
        data.yRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.horizontalAngle(state.blockState.get(NixieTubeBlock.FACING)) - 90 + (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0));
        int zRot = face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0;
        if (facing == Direction.DOWN) {
            zRot += 180;
        }
        data.zRot = MathHelper.RADIANS_PER_DEGREE * zRot;
        data.light = state.lightmapCoordinates;
        data.layer = RenderLayer.getSolid();
        data.panel = CachedBuffers.partial(AllPartialModels.SIGNAL_PANEL, state.blockState);
        data.offset = facing == Direction.DOWN || state.blockState.get(NixieTubeBlock.FACE) == DoubleAttachFace.WALL_REVERSED ? 0.25f : -0.25f;
        SignalDrawableState left = data.left = new SignalDrawableState();
        SignalState signalState = be.signalState;
        float renderTime = AnimationTickHolder.getRenderTime(be.getWorld());
        boolean yellow = signalState.isYellowLight(renderTime);
        float longSide = yellow ? 1 : 4;
        float longSideGlow = yellow ? 2 : 5.125f;
        boolean vert = facing.getAxis().isHorizontal();
        double distance = Vec3d.ofCenter(state.pos).subtract(cameraPos).lengthSquared();
        left.light = state.lightmapCoordinates;
        left.layer = RenderTypes.translucent();
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
            left.layer = RenderTypes.additive2();
            left.layer2 = RenderTypes.additive();
            left.signal = CachedBuffers.partial(AllPartialModels.SIGNAL_RED, state.blockState);
        } else {
            left.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState);
        }
        SignalDrawableState right = data.right = new SignalDrawableState();
        right.light = state.lightmapCoordinates;
        right.layer = RenderTypes.translucent();
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
            right.layer = RenderTypes.additive2();
            right.layer2 = RenderTypes.additive();
            right.signal = CachedBuffers.partial(yellow ? AllPartialModels.SIGNAL_YELLOW : AllPartialModels.SIGNAL_WHITE, state.blockState);
        } else {
            right.signal = CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, state.blockState);
        }
    }

    @Override
    public void render(NixieTubeRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        state.data.render(matrices, queue);
    }

    public static void drawInWorldString(TextRenderer fontRenderer, MatrixStack ms, VertexConsumerProvider buffer, String c, int color) {
        fontRenderer.draw(
            c,
            0,
            0,
            color,
            false,
            ms.peek().getPositionMatrix(),
            buffer,
            TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }

    public static class NixieTubeRenderState extends BlockEntityRenderState {
        public NixieTubeRenderData data;
    }

    public interface NixieTubeRenderData {
        void render(MatrixStack matrices, OrderedRenderCommandQueue queue);
    }

    public static class TextRenderState implements NixieTubeRenderData, OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float yRot;
        public float zRot;
        public float zRot2;
        public TextDrawableState left;
        public TextDrawableState right;
        public SuperByteBuffer tube;
        public int light;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            tube.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            matrices.push();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(zRot));
            if (zRot2 != 0) {
                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(zRot2));
                queue.submitCustom(matrices, layer, this);
                matrices.pop();
            } else {
                queue.submitCustom(matrices, layer, this);
            }
            if (left != null) {
                matrices.push();
                matrices.translate(-0.25f, 0, 0);
                matrices.scale(0.05f, -0.05f, 0.05f);
                queue.submitCustom(matrices, left.layer, left);
                matrices.pop();
            }
            if (right != null) {
                matrices.translate(0.25f, 0, 0);
                matrices.scale(0.05f, -0.05f, 0.05f);
                queue.submitCustom(matrices, right.layer, right);
            }
            matrices.pop();
        }
    }

    public record TextDrawableState(
        RenderLayer layer, float x, int y, TextDrawable bright, TextDrawable dark, TextDrawable mix
    ) implements OrderedRenderCommandQueue.Custom {
        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            Matrix4f pose = matricesEntry.getPositionMatrix();
            pose.translate(x, y, 0);
            bright.render(pose, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
            pose.translate(0.5f, 0.5f, -0.0625f);
            dark.render(pose, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
            pose.scale(-1, 1, 1);
            pose.translate(0.5f + x + x, -0.5f, 0.0625f);
            dark.render(pose, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
            pose.translate(-0.5f, 0.5f, -0.0625f);
            mix.render(pose, vertexConsumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, false);
        }
    }

    public static class SignalRenderState implements NixieTubeRenderData, OrderedRenderCommandQueue.Custom {
        public float yRot;
        public float zRot;
        public int light;
        public RenderLayer layer;
        public SuperByteBuffer panel;
        public float offset;
        public SignalDrawableState left;
        public SignalDrawableState right;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            panel.light(light).renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            matrices.push();
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(zRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            queue.submitCustom(matrices, layer, this);
            matrices.translate(0.5f, 0.46875f, 0.5f);
            matrices.push();
            matrices.translate(offset, 0, 0);
            left.render(matrices, queue);
            matrices.pop();
            matrices.translate(-offset, 0, 0);
            right.render(matrices, queue);
            matrices.pop();
        }
    }

    public static class SignalDrawableState {
        public RenderLayer layer;
        public RenderLayer layer2;
        public SuperByteBuffer signal;
        public int light;
        public RenderLayer cubeLayer;
        public SuperByteBuffer cube;
        public float cubeX;
        public float cubeY;
        public SuperByteBuffer glow;
        public float glowX;
        public float glowY;
        public boolean additive;

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            if (additive) {
                queue.submitCustom(matrices, layer, (e, v) -> renderAdditive(e, v, 153));
                if (cube != null) {
                    queue.submitCustom(matrices, cubeLayer, this::renderCube);
                }
                queue.submitCustom(matrices, layer2, (e, v) -> renderAdditive(e, v, 102));
            } else {
                queue.submitCustom(matrices, layer, this::renderNormal);
            }
        }

        public void renderCube(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            cube.light(LightmapTextureManager.MAX_LIGHT_COORDINATE).disableDiffuse().scale(cubeX, cubeY, 1).renderInto(matricesEntry, vertexConsumer);
        }

        public void renderAdditive(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer, int color) {
            if (glow != null) {
                glow.light(LightmapTextureManager.MAX_LIGHT_COORDINATE).disableDiffuse().scale(glowX, glowY, 2).color(color, color, color, color)
                    .renderInto(matricesEntry, vertexConsumer);
            }
            signal.light(LightmapTextureManager.MAX_LIGHT_COORDINATE).disableDiffuse().scale(1.0625f).color(color, color, color, color)
                .renderInto(matricesEntry, vertexConsumer);
        }

        public void renderNormal(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            signal.light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
