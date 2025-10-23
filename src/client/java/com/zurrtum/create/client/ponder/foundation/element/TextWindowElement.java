package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.TextElementBuilder;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.PonderScene.SceneTransform;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.function.Supplier;

public class TextWindowElement extends AnimatedOverlayElementBase {

    public static final Couple<Color> COLOR_WINDOW_BORDER = Couple.create(new Color(0x607a6000, true), new Color(0x207a6000, true))
        .map(Color::setImmutable);

    Supplier<String> textGetter = () -> "(?) No text was provided";
    @Nullable String bakedText;

    // from 0 to 200
    int y;

    @Nullable Vec3d vec;

    boolean nearScene = false;
    PonderPalette palette = PonderPalette.WHITE;

    public TextElementBuilder builder(PonderScene scene) {
        return new Builder(scene);
    }

    private class Builder implements TextElementBuilder {

        private final PonderScene scene;

        public Builder(PonderScene scene) {
            this.scene = scene;
        }

        @Override
        public Builder colored(PonderPalette color) {
            TextWindowElement.this.palette = color;
            return this;
        }

        @Override
        public Builder pointAt(Vec3d vec) {
            TextWindowElement.this.vec = vec;
            return this;
        }

        @Override
        public Builder independent(int y) {
            TextWindowElement.this.y = y;
            return this;
        }

        @Override
        public Builder text(String defaultText) {
            textGetter = scene.registerText(defaultText);
            return this;
        }

        @Override
        public TextElementBuilder text(String defaultText, Object... params) {
            textGetter = scene.registerText(defaultText, params);
            return this;
        }

        @Override
        public Builder sharedText(Identifier key) {
            textGetter = () -> PonderIndex.getLangAccess().getShared(key);
            return this;
        }

        @Override
        public TextElementBuilder sharedText(Identifier key, Object... params) {
            textGetter = () -> PonderIndex.getLangAccess().getShared(key, params);
            return this;
        }

        @Override
        public Builder sharedText(String key) {
            return sharedText(Identifier.of(scene.getNamespace(), key));
        }

        @Override
        public TextElementBuilder sharedText(String key, Object... params) {
            return sharedText(Identifier.of(scene.getNamespace(), key), params);
        }

        @Override
        public Builder placeNearTarget() {
            TextWindowElement.this.nearScene = true;
            return this;
        }

        @Override
        public Builder attachKeyFrame() {
            scene.builder().addLazyKeyframe();
            return this;
        }
    }

    @Override
    public void render(PonderScene scene, PonderUI screen, DrawContext graphics, float partialTicks, float fade) {
        if (bakedText == null)
            bakedText = textGetter.get();

        if (fade < 1 / 16f)
            return;
        SceneTransform transform = scene.getTransform();
        Vec2f sceneToScreen = vec != null ? transform.sceneToScreen(vec, partialTicks) : new Vec2f(
            screen.width / 2f,
            (screen.height - 200) / 2f + y - 8
        );

        boolean settled = transform.xRotation.settled() && transform.yRotation.settled();
        float pY = settled ? (int) sceneToScreen.y : sceneToScreen.y;

        float yDiff = (screen.height / 2f - sceneToScreen.y - 10) / 100f;
        float targetX = (screen.width * MathHelper.lerp(yDiff * yDiff, 6f / 8, 5f / 8));

        if (nearScene)
            targetX = Math.min(targetX, sceneToScreen.x + 50);

        if (settled)
            targetX = (int) targetX;

        int textWidth = (int) Math.min(screen.width - targetX, 180);

        TextRenderer fontRenderer = screen.getFontRenderer();
        List<StringVisitable> lines = fontRenderer.getTextHandler().wrapLines(bakedText, textWidth, Style.EMPTY);

        int boxWidth = 0;
        for (StringVisitable line : lines)
            boxWidth = Math.max(boxWidth, fontRenderer.getWidth(line));

        int boxHeight = fontRenderer.getWrappedLinesHeight(bakedText, boxWidth);

        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(0, pY);

        new BoxElement().withBackground(PonderUI.BACKGROUND_FLAT).gradientBorder(COLOR_WINDOW_BORDER).at(targetX - 10, 3, -101)
            .withBounds(boxWidth, boxHeight - 1).render(graphics);

        Color brighter = palette.getColorObject().mixWith(new Color(0xff_ffffdd), 0.5f).setImmutable();
        Color c1 = new Color(0xff_494949);
        Color c2 = new Color(0xff_393939);
        if (vec != null) {
            poseStack.pushMatrix();
            poseStack.translate(sceneToScreen.x, 0);
            double lineTarget = (targetX - sceneToScreen.x) * fade;
            poseStack.scale((float) lineTarget, 1);
            graphics.fillGradient(0, 0, 1, 1, brighter.getRGB(), brighter.getRGB());
            graphics.fillGradient(0, 1, 1, 2, c1.getRGB(), c2.getRGB());
            poseStack.popMatrix();
        }

        for (int i = 0; i < lines.size(); i++) {
            graphics.drawText(
                fontRenderer,
                lines.get(i).getString(),
                (int) (targetX - 10),
                3 + 9 * i,
                brighter.scaleAlphaForText(fade).getRGB(),
                false
            );
        }
        poseStack.popMatrix();
    }

    public PonderPalette getPalette() {
        return palette;
    }

}