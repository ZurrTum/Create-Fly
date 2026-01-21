package com.zurrtum.create.client.catnip.gui.widget;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.gui.element.AbstractRenderElement;
import com.zurrtum.create.client.catnip.gui.element.RenderElement;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.joml.Vector4i;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ElementWidget extends AbstractSimiWidget {

    protected RenderElement element = AbstractRenderElement.EMPTY;

    protected boolean usesFade = false;
    protected int fadeModX;
    protected int fadeModY;
    protected LerpedFloat fade = LerpedFloat.linear().startWithValue(1);

    protected boolean rescaleElement = false;
    protected float rescaleSizeX;
    protected float rescaleSizeY;

    protected float paddingX = 0;
    protected float paddingY = 0;
    protected @Nullable Vector4i scissor;

    public ElementWidget(int x, int y) {
        super(x, y);
    }

    public ElementWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public <T extends ElementWidget> T showingElement(RenderElement element) {
        this.element = element;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T showing(ScreenElement renderable) {
        return this.showingElement(RenderElement.of(renderable));
    }

    public <T extends ElementWidget> T modifyElement(Consumer<RenderElement> consumer) {
        consumer.accept(element);
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T mapElement(UnaryOperator<RenderElement> function) {
        element = function.apply(element);
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T withScissor(int x1, int y1, int width, int height) {
        this.scissor = new Vector4i(x1, y1, x1 + width, y1 + height);
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T withPadding(float paddingX, float paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T enableFade(int fadeModifierX, int fadeModifierY) {
        this.fade.startWithValue(0);
        this.usesFade = true;
        this.fadeModX = fadeModifierX;
        this.fadeModY = fadeModifierY;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T disableFade() {
        this.fade.startWithValue(1);
        this.usesFade = false;
        //noinspection unchecked
        return (T) this;
    }

    public LerpedFloat fade() {
        return fade;
    }

    public <T extends ElementWidget> T fade(float target) {
        fade.chase(target, 0.1, LerpedFloat.Chaser.EXP);
        //noinspection unchecked
        return (T) this;
    }

    /**
     * Rescaling and its effects aren't properly tested with most elements.
     * Thought it should work fine when using a TextStencilElement.
     * Check BaseConfigScreen's title for such an example.
     */
    @Deprecated
    public <T extends ElementWidget> T rescaleElement(float rescaleSizeX, float rescaleSizeY) {
        this.rescaleElement = true;
        this.rescaleSizeX = rescaleSizeX;
        this.rescaleSizeY = rescaleSizeY;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends ElementWidget> T disableRescale() {
        this.rescaleElement = false;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public void tick() {
        super.tick();
        fade.tickChaser();
    }

    @Override
    protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.beforeRender(graphics, mouseX, mouseY, partialTicks);
        isHovered = isMouseOver(mouseX, mouseY);

        float fadeValue = fade.getValue(partialTicks);
        element.withAlpha(fadeValue);
        if (fadeValue < 1) {
            graphics.pose().translate((1 - fadeValue) * fadeModX, (1 - fadeValue) * fadeModY);
        }
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(getX() + paddingX, getY() + paddingY);
        float innerWidth = width - 2 * paddingX;
        float innerHeight = height - 2 * paddingY;
        float eX = element.getX(), eY = element.getY();
        if (rescaleElement) {
            float xScale = innerWidth / rescaleSizeX;
            float yScale = innerHeight / rescaleSizeY;
            poseStack.scale(xScale, yScale);
            element.at(eX / xScale, eY / yScale);
            innerWidth /= xScale;
            innerHeight /= yScale;
        }
        if (scissor != null) {
            graphics.enableScissor(scissor.x, scissor.y, scissor.z, scissor.w);
        }
        element.withBounds((int) innerWidth, (int) innerHeight).render(graphics);
        if (scissor != null) {
            graphics.disableScissor();
        }
        poseStack.popMatrix();
        if (rescaleElement) {
            element.at(eX, eY);
        }
    }

    public RenderElement getRenderElement() {
        return element;
    }
}
