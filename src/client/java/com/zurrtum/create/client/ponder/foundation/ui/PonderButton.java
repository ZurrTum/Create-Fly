package com.zurrtum.create.client.ponder.foundation.ui;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.BoxWidget;
import com.zurrtum.create.client.ponder.foundation.PonderTag;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class PonderButton extends BoxWidget {

    public static final Couple<Color> COLOR_IDLE = Couple.create(new Color(0x60_c0c0ff, true), new Color(0x30_c0c0ff, true)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_HOVER = Couple.create(new Color(0xf0_c0c0ff, true), new Color(0xa0_c0c0ff, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_CLICK = Couple.create(new Color(0xff_ffffff, true), new Color(0xdd_ffffff, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_DISABLED = Couple.create(new Color(0x80_909090, true), new Color(0x20_909090, true))
        .map(Color::setImmutable);

    @Nullable
    protected ItemStack item;
    @Nullable
    protected PonderTag tag;
    @Nullable
    protected KeyBinding shortcut;
    protected LerpedFloat flash = LerpedFloat.linear().startWithValue(0).chase(0, 0.1f, LerpedFloat.Chaser.EXP);

    public PonderButton(int x, int y) {
        this(x, y, 20, 20);
    }

    public PonderButton(int x, int y, int width, int height) {
        super(x, y, width, height);
        z = 420;
        paddingX = 2;
        paddingY = 2;
        colorIdle = COLOR_IDLE;
        colorHover = COLOR_HOVER;
        colorClick = COLOR_CLICK;
        colorDisabled = COLOR_DISABLED;
        updateGradientFromState();
    }

    public <T extends PonderButton> T withShortcut(KeyBinding key) {
        this.shortcut = key;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends PonderButton> T showingTag(PonderTag tag) {
        return showing(this.tag = tag);
    }

    public <T extends PonderButton> T showing(ItemStack item) {
        this.item = item;
        return super.showingElement(GuiGameElement.of(item).scale(1.5f).at(-4, -4));
    }

    public void flash() {
        flash.updateChaseTarget(1);
    }

    public void dim() {
        flash.updateChaseTarget(0);
    }

    @Override
    public void tick() {
        super.tick();
        flash.tickChaser();
    }

    @Override
    protected void beforeRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.beforeRender(graphics, mouseX, mouseY, partialTicks);

        float flashValue = flash.getValue(partialTicks);
        if (flashValue > .1f) {
            float sin = 0.5f + 0.5f * MathHelper.sin((AnimationTickHolder.getTicks(true) + partialTicks) / 10f);
            sin *= flashValue;
            Color nc1 = new Color(255, 255, 255, MathHelper.clamp(gradientColor.getFirst().getAlpha() + 150, 0, 255));
            Color nc2 = new Color(155, 155, 155, MathHelper.clamp(gradientColor.getSecond().getAlpha() + 150, 0, 255));
            Couple<Color> newColors = Couple.create(nc1, nc2);
            float finalSin = sin;
            gradientColor = gradientColor.mapWithParams((color, other) -> color.mixWith(other, finalSin), newColors);
        }
    }

    @Override
    public void doRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.doRender(graphics, mouseX, mouseY, partialTicks);

        if (!isVisible())
            return;

        if (shortcut != null) {
            graphics.drawCenteredTextWithShadow(
                graphics.client.textRenderer,
                shortcut.getBoundKeyLocalizedText().getString().toLowerCase(Locale.ROOT),
                getX() + width / 2 + 8,
                getY() + height - 6,
                UIRenderHelper.COLOR_TEXT_DARKER.getFirst().scaleAlpha(fade.getValue()).getRGB()
            );
        }
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (shortcut != null && shortcut.matchesKey(keyCode, scanCode)) {
            gradientColor = getColorClick();
            startGradientAnimation(getColorForState(), 0.15);

            runCallback(width / 2f, height / 2f);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected boolean isValidClickButton(int i) {
        return isVisible();
    }

    @Nullable
    public ItemStack getItem() {
        return item;
    }

    @Nullable
    public PonderTag getTag() {
        return tag;
    }

    public boolean isVisible() {
        return !(fade.getValue() < .1f);
    }

    public void clear() {
        if (tag != null) {
            tag.clear();
        }
        if (element != null) {
            element.clear();
        }
    }
}