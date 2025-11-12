package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.lang.ClientFontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public class TitleTextRenderer extends PictureInPictureRenderer<TitleTextRenderState> {
    public TitleTextRenderer(MultiBufferSource.BufferSource vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void renderToTexture(TitleTextRenderState state, PoseStack matrices) {
        matrices.scale(1, 1, -1);
        matrices.translate(-90, -20, 0);
        Font font = Minecraft.getInstance().font;
        float indexDiff = state.diff();
        float absoluteIndexDiff = Math.abs(indexDiff);
        float angle = indexDiff * -90;
        matrices.translate(0, 6, 0);
        matrices.pushPose();
        matrices.mulPose(Axis.XN.rotationDegrees(angle + Math.signum(indexDiff) * 90));
        matrices.translate(0, -6, 5);
        ClientFontHelper.drawSplitString(
            bufferSource,
            matrices,
            font,
            state.otherTitle(),
            0,
            0,
            180,
            UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(absoluteIndexDiff).getRGB()
        );
        matrices.popPose();

        matrices.mulPose(Axis.XN.rotationDegrees(angle));
        matrices.translate(0, -6, 5);
        ClientFontHelper.drawSplitString(
            bufferSource,
            matrices,
            font,
            state.title(),
            0,
            0,
            180,
            UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(1 - absoluteIndexDiff).getRGB()
        );
    }

    @Override
    protected String getTextureLabel() {
        return "Title Text";
    }

    @Override
    public Class<TitleTextRenderState> getRenderStateClass() {
        return TitleTextRenderState.class;
    }
}
