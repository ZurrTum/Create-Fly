package com.zurrtum.create.client.catnip.gui;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.Collection;
import java.util.List;

public abstract class AbstractSimiScreen extends Screen {

    protected static final Color BACKGROUND_COLOR = new Color(0x50_101010, true);

    protected int windowWidth, windowHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;

    protected AbstractSimiScreen(Text title) {
        super(title);
    }

    protected AbstractSimiScreen() {
        this(ScreenTexts.EMPTY);
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    protected void init() {
        guiLeft = (width - windowWidth) / 2;
        guiTop = (height - windowHeight) / 2;
        guiLeft += windowXOffset;
        guiTop += windowYOffset;
    }

    @Override
    public void tick() {
        for (Element listener : children()) {
            if (listener instanceof TickableGuiEventListener tickable) {
                tickable.tick();
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @SuppressWarnings("unchecked")
    protected <W extends Element & Drawable & Selectable> void addRenderableWidgets(W... widgets) {
        for (W widget : widgets) {
            addDrawableChild(widget);
        }
    }

    protected <W extends Element & Drawable & Selectable> void addRenderableWidgets(Collection<W> widgets) {
        for (W widget : widgets) {
            addDrawableChild(widget);
        }
    }

    protected void removeWidgets(Element... widgets) {
        for (Element widget : widgets) {
            remove(widget);
        }
    }

    protected void removeWidgets(Collection<? extends Element> widgets) {
        for (Element widget : widgets) {
            remove(widget);
        }
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = NavigatableSimiScreen.currentlyRenderingPreviousScreen ? 0 : AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());
        Matrix3x2fStack poseStack = graphics.getMatrices();

        poseStack.pushMatrix();

        prepareFrame();

        renderWindowBackground(graphics, mouseX, mouseY, partialTicks);
        renderWindow(graphics, mouseX, mouseY, partialTicks);

        for (Drawable renderable : getRenderables())
            renderable.render(graphics, mouseX, mouseY, partialTicks);

        renderWindowForeground(graphics, mouseX, mouseY, partialTicks);

        endFrame();

        poseStack.popMatrix();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean keyPressed = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyPressed || getFocused() != null)
            return keyPressed;

        if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }

        boolean consumed = false;

        for (Element widget : children()) {
            if (widget instanceof AbstractSimiWidget simiWidget) {
                if (simiWidget.keyPressed(keyCode, scanCode, modifiers))
                    consumed = true;
            }
        }

        return consumed;
    }

    protected void prepareFrame() {
    }

    protected void renderWindowBackground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        renderDarkening(graphics);
    }

    protected abstract void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks);

    protected void renderWindowForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        for (Drawable widget : getRenderables()) {
            if (widget instanceof AbstractSimiWidget simiWidget && simiWidget.isHovered() && simiWidget.visible) {
                List<Text> tooltip = simiWidget.getToolTip();
                if (tooltip.isEmpty())
                    continue;
                int ttx = simiWidget.lockedTooltipX == -1 ? mouseX : simiWidget.lockedTooltipX + simiWidget.getX();
                int tty = simiWidget.lockedTooltipY == -1 ? mouseY : simiWidget.lockedTooltipY + simiWidget.getY();
                graphics.drawTooltip(textRenderer, tooltip, ttx, tty);
            }
        }
    }

    protected void endFrame() {
    }

    @Deprecated
    protected void debugWindowArea(DrawContext graphics) {
        graphics.fill(guiLeft + windowWidth, guiTop + windowHeight, guiLeft, guiTop, 0xD3D3D3D3);
    }

    protected List<Drawable> getRenderables() {
        return this.drawables;
    }

    @Override
    public Element getFocused() {
        Element focused = super.getFocused();
        if (focused instanceof ClickableWidget && !focused.isFocused())
            focused = null;
        setFocused(focused);
        return focused;
    }

}
