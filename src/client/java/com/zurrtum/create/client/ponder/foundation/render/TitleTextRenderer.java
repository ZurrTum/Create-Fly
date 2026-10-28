package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.lang.ClientFontHelper;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

public class TitleTextRenderer extends PictureInPictureRenderer<TitleTextRenderState> {
    @Override
    protected void renderToTexture(TitleTextRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        Pose pose = matrices.last();
        pose.scale(1, 1, -1);
        pose.translate(-90, -20, 0);
        Font font = Minecraft.getInstance().font;
        float indexDiff = state.diff();
        float absoluteIndexDiff = Math.abs(indexDiff);
        float xRadians = indexDiff * Radians.DEG_90;
        pose.translate(0, 6, 0);
        matrices.pushPose();
        pose = matrices.last();
        pose.rotate(Axis.XP, xRadians - Math.signum(indexDiff) * Radians.DEG_90);
        pose.translate(0, -6, 5);
        ClientFontHelper.submitSplitString(
            queue,
            matrices,
            font,
            state.otherTitle(),
            0,
            0,
            180,
            UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(absoluteIndexDiff).getRGB()
        );
        matrices.popPose();

        pose = matrices.last();
        pose.rotate(Axis.XP, xRadians);
        pose.translate(0, -6, 5);
        ClientFontHelper.submitSplitString(
            queue,
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
