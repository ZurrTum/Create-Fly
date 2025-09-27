package com.zurrtum.create.client.ponder.foundation.ui;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

public class PonderProgressBar extends AbstractSimiWidget {

    public static final Couple<Color> BAR_COLORS = Couple.create(new Color(0x80_aaaadd, true), new Color(0x50_aaaadd, true)).map(Color::setImmutable);

    LerpedFloat progress;

    PonderUI ponder;

    public PonderProgressBar(PonderUI ponder, int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn);

        this.ponder = ponder;
        progress = LerpedFloat.linear().startWithValue(0);
    }

    public void tick() {
        progress.chase(ponder.getActiveScene().getSceneProgress(), .5f, LerpedFloat.Chaser.EXP);
        progress.tickChaser();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && ponder.getActiveScene()
            .getKeyframeCount() > 0 && mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width + 4) && mouseY >= (double) this.getY() - 3 && mouseY < (double) (this.getY() + this.height + 20);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(false);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        PonderScene activeScene = ponder.getActiveScene();

        int keyframeIndex = getHoveredKeyframeIndex(activeScene, mouseX);

        if (keyframeIndex == -1)
            ponder.seekToTime(0);
        else if (keyframeIndex == activeScene.getKeyframeCount())
            ponder.seekToTime(activeScene.getTotalTime());
        else
            ponder.seekToTime(activeScene.getKeyframeTime(keyframeIndex));
    }

    public int getHoveredKeyframeIndex(PonderScene activeScene, double mouseX) {
        int totalTime = activeScene.getTotalTime();
        int clickedAtTime = (int) ((mouseX - getX()) / ((double) width + 4) * totalTime);

        {
            int lastKeyframeTime = activeScene.getKeyframeTime(activeScene.getKeyframeCount() - 1);

            int diffToEnd = totalTime - clickedAtTime;
            int diffToLast = clickedAtTime - lastKeyframeTime;

            if (diffToEnd > 0 && diffToEnd < diffToLast / 2) {
                return activeScene.getKeyframeCount();
            }
        }

        int index = -1;

        for (int i = 0; i < activeScene.getKeyframeCount(); i++) {
            int keyframeTime = activeScene.getKeyframeTime(i);

            if (keyframeTime > clickedAtTime)
                break;

            index = i;
        }

        return index;
    }

    @Override
    public void doRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack poseStack = graphics.getMatrices();

        hovered = isMouseOver(mouseX, mouseY);

        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(PonderUI.COLOR_IDLE).at(getX(), getY(), 400)
            .withBounds(width, height).render(graphics);

        poseStack.pushMatrix();
        poseStack.translate(getX() - 2, getY() - 2);

        poseStack.pushMatrix();
        poseStack.scale((width + 4) * progress.getValue(partialTicks), 1);
        Color c1 = BAR_COLORS.getFirst();
        Color c2 = BAR_COLORS.getSecond();
        UIRenderHelper.drawGradientRect(graphics, 0f, 1f, 1f, 3f, c1, c1);
        UIRenderHelper.drawGradientRect(graphics, 0f, 3f, 1f, 4f, c2, c2);
        poseStack.popMatrix();

        renderKeyframes(graphics, mouseX, partialTicks);

        poseStack.popMatrix();
    }

    private void renderKeyframes(DrawContext graphics, int mouseX, float partialTicks) {
        PonderScene activeScene = ponder.getActiveScene();

        Couple<Color> hover = PonderUI.COLOR_HOVER.map(c -> c.setAlpha(0xe0));
        Couple<Color> idle = PonderUI.COLOR_HOVER.map(c -> c.setAlpha(0x70));
        int hoverIndex;

        if (hovered) {
            hoverIndex = getHoveredKeyframeIndex(activeScene, mouseX);
        } else {
            hoverIndex = -2;
        }

        if (hoverIndex == -1)
            drawKeyframe(graphics, activeScene, true, 0, 0, hover.getFirst(), hover.getSecond(), 8);
        else if (hoverIndex == activeScene.getKeyframeCount())
            drawKeyframe(graphics, activeScene, true, activeScene.getTotalTime(), width + 4, hover.getFirst(), hover.getSecond(), 8);

        for (int i = 0; i < activeScene.getKeyframeCount(); i++) {
            int keyframeTime = activeScene.getKeyframeTime(i);
            int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.getTotalTime()) * (width + 2));

            boolean selected = i == hoverIndex;
            Couple<Color> colors = selected ? hover : idle;
            int height = selected ? 8 : 4;

            drawKeyframe(graphics, activeScene, selected, keyframeTime, keyframePos, colors.getFirst(), colors.getSecond(), height);

        }
    }

    private void drawKeyframe(
        DrawContext graphics,
        PonderScene activeScene,
        boolean selected,
        int keyframeTime,
        int keyframePos,
        Color startColor,
        Color endColor,
        int height
    ) {
        if (selected) {
            TextRenderer font = graphics.client.textRenderer;
            UIRenderHelper.drawGradientRect(graphics, ((float) keyframePos), 9f, keyframePos + 2f, 9f + height, endColor, startColor);
            String text;
            int offset;
            if (activeScene.getCurrentTime() < keyframeTime) {
                text = ">";
                offset = -2 - font.getWidth(text);
            } else {
                text = "<";
                offset = 4;
            }
            graphics.drawText(font, Text.literal(text).formatted(Formatting.BOLD), keyframePos + offset, 10, endColor.getRGB(), false);
        }

        UIRenderHelper.drawGradientRect(graphics, ((float) keyframePos), 0f, keyframePos + 2f, 1f + height, startColor, endColor);
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }
}