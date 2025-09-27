package com.zurrtum.create.client.ponder.foundation.render;

import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.lang.ClientFontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class TitleTextRenderer extends SpecialGuiElementRenderer<TitleTextRenderState> {
    public TitleTextRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
    }

    @Override
    protected void render(TitleTextRenderState state, MatrixStack matrices) {
        matrices.scale(1, 1, -1);
        matrices.translate(-90, -20, 0);
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        float indexDiff = state.diff();
        float absoluteIndexDiff = Math.abs(indexDiff);
        float angle = indexDiff * -90;
        matrices.translate(0, 6, 0);
        matrices.push();
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(angle + Math.signum(indexDiff) * 90));
        matrices.translate(0, -6, 5);
        ClientFontHelper.drawSplitString(
            vertexConsumers,
            matrices,
            font,
            state.otherTitle(),
            0,
            0,
            180,
            UIRenderHelper.COLOR_TEXT.getFirst().scaleAlphaForText(absoluteIndexDiff).getRGB()
        );
        matrices.pop();

        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(angle));
        matrices.translate(0, -6, 5);
        ClientFontHelper.drawSplitString(
            vertexConsumers,
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
    protected String getName() {
        return "Title Text";
    }

    @Override
    public Class<TitleTextRenderState> getElementClass() {
        return TitleTextRenderState.class;
    }
}
