package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiItemRenderBuilder;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.InputElementBuilder;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class InputWindowElement extends AnimatedOverlayElementBase {

    private final Vec3d sceneSpace;
    private final Pointing direction;
    @Nullable Identifier key;
    @Nullable ScreenElement icon;
    @Nullable GuiItemRenderBuilder item;

    public InputWindowElement(Vec3d sceneSpace, Pointing direction) {
        this.sceneSpace = sceneSpace;
        this.direction = direction;
    }

    public InputElementBuilder builder() {
        return new Builder();
    }

    private class Builder implements InputElementBuilder {

        @Override
        public InputElementBuilder withItem(ItemStack stack) {
            item = GuiGameElement.of(stack).scale(1.5f);
            return this;
        }

        @Override
        public InputElementBuilder leftClick() {
            icon = PonderGuiTextures.ICON_LMB;
            return this;
        }

        @Override
        public InputElementBuilder scroll() {
            icon = PonderGuiTextures.ICON_SCROLL;
            return this;
        }

        @Override
        public InputElementBuilder rightClick() {
            icon = PonderGuiTextures.ICON_RMB;
            return this;
        }

        @Override
        public InputElementBuilder showing(ScreenElement icon) {
            InputWindowElement.this.icon = icon;
            return this;
        }

        @Override
        public InputElementBuilder whileSneaking() {
            key = Ponder.asResource("sneak_and");
            return this;
        }

        @Override
        public InputElementBuilder whileCTRL() {
            key = Ponder.asResource("ctrl_and");
            return this;
        }

    }

    @Override
    public void render(PonderScene scene, PonderUI screen, DrawContext graphics, float partialTicks, float fade) {
        TextRenderer font = screen.getFontRenderer();
        int width = 0;
        int height = 0;

        float xFade = direction == Pointing.RIGHT ? -1 : direction == Pointing.LEFT ? 1 : 0;
        float yFade = direction == Pointing.DOWN ? -1 : direction == Pointing.UP ? 1 : 0;
        xFade *= 10 * (1 - fade);
        yFade *= 10 * (1 - fade);

        boolean hasText = key != null;
        boolean hasIcon = icon != null;
        int keyWidth = 0;
        String text = hasText ? PonderIndex.getLangAccess().getShared(key) : "";

        if (fade < 1 / 16f)
            return;
        Vec2f sceneToScreen = scene.getTransform().sceneToScreen(sceneSpace, partialTicks);

        if (hasIcon) {
            width += 24;
            height = 24;
        }

        if (hasText) {
            keyWidth = font.getWidth(text);
            width += keyWidth;
        }

        if (item != null) {
            width += 24;
            height = 24;
        }

        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade);

        PonderUI.renderSpeechBox(graphics, 0, 0, width, height, false, direction, true);

        if (hasText)
            graphics.drawText(
                font,
                text,
                2,
                (int) ((height - font.fontHeight) / 2f + 2),
                PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(),
                false
            );

        if (hasIcon) {
            poseStack.pushMatrix();
            poseStack.translate(keyWidth, 0);
            poseStack.scale(1.5f, 1.5f);
            icon.render(graphics, 0, 0);
            poseStack.popMatrix();
        }

        if (item != null) {
            item.at(keyWidth + (hasIcon ? 24 : 0), 0).render(graphics);
        }

        poseStack.popMatrix();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (item != null && !visible) {
            item.clear();
        }
    }
}