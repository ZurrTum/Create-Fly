package com.zurrtum.create.client.content.redstone.nixieTube;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.RenderTypes;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class NixieTubeRenderer extends SafeBlockEntityRenderer<NixieTubeBlockEntity> {
    public NixieTubeRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(NixieTubeBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        ms.push();
        BlockState blockState = be.getCachedState();
        DoubleAttachFace face = blockState.get(NixieTubeBlock.FACE);
        Direction facing = blockState.get(NixieTubeBlock.FACING);
        float yRot = AngleHelper.horizontalAngle(facing) - 90 + (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0);
        float xRot = face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0;

        var msr = TransformStack.of(ms);
        msr.center().rotateYDegrees(yRot).rotateZDegrees(xRot).uncenter();
        MinecraftClient mc = MinecraftClient.getInstance();

        if (be.signalState != null || be.computerSignal != null) {
            renderAsSignal(mc, be, partialTicks, ms, buffer, light, overlay);
            ms.pop();
            return;
        }

        msr.center();
        if (face == DoubleAttachFace.CEILING || facing == Direction.DOWN) {
            ms.push();
            msr.rotateZDegrees(180);
            CachedBuffers.partial(AllPartialModels.NIXIE_TUBE, blockState).light(light)
                .renderInto(ms, buffer.getBuffer(PonderRenderTypes.translucent()));
            ms.pop();
        } else {
            CachedBuffers.partial(AllPartialModels.NIXIE_TUBE, blockState).light(light)
                .renderInto(ms, buffer.getBuffer(PonderRenderTypes.translucent()));
        }

        float height = face == DoubleAttachFace.CEILING ? 5 : 3;
        float scale = 1 / 20f;

        Couple<String> s = be.getDisplayedStrings();
        DyeColor color = NixieTubeBlock.colorOf(be.getCachedState());

        ms.push();
        ms.translate(-4 / 16f, 0, 0);
        ms.scale(scale, -scale, scale);
        drawTube(mc, ms, buffer, s.getFirst(), height, color);
        ms.pop();

        ms.push();
        ms.translate(4 / 16f, 0, 0);
        ms.scale(scale, -scale, scale);
        drawTube(mc, ms, buffer, s.getSecond(), height, color);
        ms.pop();

        ms.pop();
    }

    public static void drawTube(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider buffer, String c, float height, DyeColor color) {
        TextRenderer fontRenderer = mc.textRenderer;
        float charWidth = fontRenderer.getWidth(c);
        float shadowOffset = .5f;
        float flicker = mc.world.random.nextFloat();
        Couple<Integer> couple = DyeHelper.getDyeColors(color);
        int brightColor = couple.getFirst() | 0xFF000000;
        int darkColor = couple.getSecond() | 0xFF000000;
        int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);

        ms.push();
        ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
        drawInWorldString(fontRenderer, ms, buffer, c, flickeringBrightColor);
        ms.push();
        ms.translate(shadowOffset, shadowOffset, -1 / 16f);
        drawInWorldString(fontRenderer, ms, buffer, c, darkColor);
        ms.pop();
        ms.pop();

        ms.push();
        ms.scale(-1, 1, 1);
        ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
        drawInWorldString(fontRenderer, ms, buffer, c, darkColor);
        ms.push();
        ms.translate(-shadowOffset, shadowOffset, -1 / 16f);
        drawInWorldString(fontRenderer, ms, buffer, c, Color.mixColors(darkColor, 0, .35f));
        ms.pop();
        ms.pop();
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
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
        if (buffer instanceof Immediate immediate) {
            BakedGlyph texturedglyph = fontRenderer.getFontStorage(Style.DEFAULT_FONT_ID).getRectangleBakedGlyph();
            immediate.draw(texturedglyph.getLayer(TextRenderer.TextLayerType.NORMAL));
        }
    }

    private void renderAsSignal(
        MinecraftClient mc,
        NixieTubeBlockEntity be,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        BlockState blockState = be.getCachedState();
        Direction facing = NixieTubeBlock.getFacing(blockState);
        Vec3d observerVec = mc.cameraEntity.getCameraPosVec(partialTicks);
        var msr = TransformStack.of(ms);

        if (facing == Direction.DOWN)
            msr.center().rotateZDegrees(180).uncenter();

        boolean invertTubes = facing == Direction.DOWN || blockState.get(NixieTubeBlock.FACE) == DoubleAttachFace.WALL_REVERSED;

        CachedBuffers.partial(AllPartialModels.SIGNAL_PANEL, blockState).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

        ms.push();
        ms.translate(1 / 2f, 7.5f / 16f, 1 / 2f);
        float renderTime = AnimationTickHolder.getRenderTime(be.getWorld());

        Vec3d lampVec = Vec3d.ofCenter(be.getPos());
        Vec3d diff = lampVec.subtract(observerVec);
        if (be.signalState != null) {
            for (boolean first : Iterate.trueAndFalse) {
                boolean flip = first == invertTubes;
                VertexConsumer translucent = buffer.getBuffer(RenderTypes.translucent());
                if (first && !be.signalState.isRedLight(renderTime)) {
                    ms.push();
                    ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);
                    CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, blockState).light(light).renderInto(ms, translucent);
                    ms.pop();
                    continue;
                }
                if (!first && !be.signalState.isGreenLight(renderTime) && !be.signalState.isYellowLight(renderTime)) {
                    ms.push();
                    ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);
                    CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, blockState).light(light).renderInto(ms, translucent);
                    ms.pop();
                    continue;
                }

                boolean yellow = be.signalState.isYellowLight(renderTime);

                ms.push();
                ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);

                VertexConsumer additive2 = buffer.getBuffer(RenderTypes.additive2());
                VertexConsumer additive = buffer.getBuffer(RenderTypes.additive());
                if (diff.lengthSquared() < 9216) {
                    boolean vert = first ^ facing.getAxis().isHorizontal();
                    float longSide = yellow ? 1 : 4;
                    float longSideGlow = yellow ? 2 : 5.125f;

                    CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, blockState).light(0xf000f0).disableDiffuse()
                        .scale(vert ? longSide : 1, vert ? 1 : longSide, 1).renderInto(ms, translucent);

                    float factorX = vert ? longSideGlow : 2;
                    float factorY = vert ? 2 : longSideGlow;
                    SuperByteBuffer glow = CachedBuffers.partial(
                        first ? AllPartialModels.SIGNAL_RED_GLOW : yellow ? AllPartialModels.SIGNAL_YELLOW_GLOW : AllPartialModels.SIGNAL_WHITE_GLOW,
                        blockState
                    );
                    glow.light(0xf000f0).disableDiffuse().scale(factorX, factorY, 2).color(153, 153, 153, 153).renderInto(ms, additive2);
                    glow.light(0xf000f0).disableDiffuse().scale(factorX, factorY, 2).color(102, 102, 102, 102).renderInto(ms, additive);
                }

                float scale = 1 + 1 / 16f;
                SuperByteBuffer signal = CachedBuffers.partial(
                    first ? AllPartialModels.SIGNAL_RED : yellow ? AllPartialModels.SIGNAL_YELLOW : AllPartialModels.SIGNAL_WHITE,
                    blockState
                );
                signal.light(0xF000F0).disableDiffuse().scale(scale).color(153, 153, 153, 153).renderInto(ms, additive2);
                signal.light(0xF000F0).disableDiffuse().scale(scale).color(102, 102, 102, 102).renderInto(ms, additive);

                ms.pop();
            }
        } else if (be.computerSignal != null) {
            VertexConsumer translucent = buffer.getBuffer(RenderTypes.translucent());
            for (boolean first : Iterate.trueAndFalse) {
                boolean flip = first == invertTubes;
                NixieTubeBlockEntity.ComputerSignal.TubeDisplay tubeDisplay = first ? be.computerSignal.first : be.computerSignal.second;
                if (tubeDisplay.blinkPeriod == 0 || tubeDisplay.blinkPeriod > 1 && renderTime % tubeDisplay.blinkPeriod < tubeDisplay.blinkOffTime) {
                    ms.push();
                    ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);
                    CachedBuffers.partial(AllPartialModels.NIXIE_TUBE_SINGLE, blockState).light(light).renderInto(ms, translucent);
                    ms.pop();
                    continue;
                }

                ms.push();
                ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);

                VertexConsumer additive2 = buffer.getBuffer(RenderTypes.additive2());
                VertexConsumer additive = buffer.getBuffer(RenderTypes.additive());
                if (diff.lengthSquared() < 9216) {
                    boolean horiz = facing.getAxis().isHorizontal();
                    float width = horiz ? tubeDisplay.glowWidth : tubeDisplay.glowHeight;
                    float height = horiz ? tubeDisplay.glowHeight : tubeDisplay.glowWidth;

                    CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_CUBE, blockState).light(0xf000f0).disableDiffuse()
                        .scale(width, height, 1).renderInto(ms, translucent);

                    SuperByteBuffer glow = CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_GLOW, blockState);
                    int r = Math.min(((tubeDisplay.r & 0xFF) * 6 + 256) >> 3, 255);
                    int g = Math.min(((tubeDisplay.g & 0xFF) * 6 + 256) >> 3, 255);
                    int b = Math.min(((tubeDisplay.b & 0xFF) * 6 + 256) >> 3, 255);
                    float factorX = width + 1.125f;
                    float factorY = height + 1.125f;
                    int r1 = (int) (r * 0.6);
                    int g1 = (int) (g * 0.6);
                    int b1 = (int) (b * 0.6);
                    glow.light(0xf000f0).color(r1, g1, b1, 153).disableDiffuse().scale(factorX, factorY, 2).renderInto(ms, additive2);
                    glow.light(0xf000f0).color(r - r1, g - g1, b - b1, 102).disableDiffuse().scale(factorX, factorY, 2).renderInto(ms, additive);
                }

                CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_BASE, blockState).light(0xF000F0).color(12, 12, 12, 255).disableDiffuse()
                    .scale(1 + 1.25f / 16f).renderInto(ms, additive);

                SuperByteBuffer signal = CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE, blockState);
                int r1 = (int) (tubeDisplay.r * 0.6);
                int g1 = (int) (tubeDisplay.g * 0.6);
                int b1 = (int) (tubeDisplay.b * 0.6);
                float scale = 1 + 1 / 16f;
                signal.light(0xF000F0).color(r1, g1, b1, 153).disableDiffuse().scale(scale).renderInto(ms, additive2);
                signal.light(0xF000F0).color(tubeDisplay.r - r1, tubeDisplay.g - g1, tubeDisplay.b - b1, 102).disableDiffuse().scale(scale)
                    .renderInto(ms, additive);

                ms.pop();
            }
        }

        ms.pop();

    }

    @Override
    public int getRenderDistance() {
        return 128;
    }

}
