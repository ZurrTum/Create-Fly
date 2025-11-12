package com.zurrtum.create.client.catnip.gui;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import org.joml.Matrix3x2fStack;

import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public abstract class AbstractSimiScreen extends Screen {

    protected static final Color BACKGROUND_COLOR = new Color(0x50_101010, true);

    protected int windowWidth, windowHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;

    protected AbstractSimiScreen(Component title) {
        super(title);
    }

    protected AbstractSimiScreen() {
        this(CommonComponents.EMPTY);
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
        for (GuiEventListener listener : children()) {
            if (listener instanceof TickableGuiEventListener tickable) {
                tickable.tick();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @SuppressWarnings("unchecked")
    protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(W... widgets) {
        for (W widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(Collection<W> widgets) {
        for (W widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    protected void removeWidgets(GuiEventListener... widgets) {
        for (GuiEventListener widget : widgets) {
            removeWidget(widget);
        }
    }

    protected void removeWidgets(Collection<? extends GuiEventListener> widgets) {
        for (GuiEventListener widget : widgets) {
            removeWidget(widget);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = NavigatableSimiScreen.currentlyRenderingPreviousScreen ? 0 : AnimationTickHolder.getPartialTicksUI(minecraft.getDeltaTracker());
        Matrix3x2fStack poseStack = graphics.pose();

        poseStack.pushMatrix();

        prepareFrame();

        renderWindowBackground(graphics, mouseX, mouseY, partialTicks);
        renderWindow(graphics, mouseX, mouseY, partialTicks);

        for (Renderable renderable : getRenderables())
            renderable.render(graphics, mouseX, mouseY, partialTicks);

        renderWindowForeground(graphics, mouseX, mouseY, partialTicks);

        endFrame();

        poseStack.popMatrix();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean keyPressed = super.keyPressed(input);
        if (keyPressed || getFocused() != null)
            return keyPressed;

        if (this.minecraft.options.keyInventory.matches(input)) {
            this.onClose();
            return true;
        }

        boolean consumed = false;

        for (GuiEventListener widget : children()) {
            if (widget instanceof AbstractSimiWidget simiWidget) {
                if (simiWidget.keyPressed(input))
                    consumed = true;
            }
        }

        return consumed;
    }

    protected void prepareFrame() {
    }

    protected void renderWindowBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderMenuBackground(graphics);
    }

    protected abstract void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    protected void renderWindowForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }

    protected void endFrame() {
    }

    @Deprecated
    protected void debugWindowArea(GuiGraphics graphics) {
        graphics.fill(guiLeft + windowWidth, guiTop + windowHeight, guiLeft, guiTop, 0xD3D3D3D3);
    }

    protected List<Renderable> getRenderables() {
        return this.renderables;
    }

    @Override
    public GuiEventListener getFocused() {
        GuiEventListener focused = super.getFocused();
        if (focused instanceof AbstractWidget && !focused.isFocused())
            focused = null;
        setFocused(focused);
        return focused;
    }

}
