package com.zurrtum.create.client.catnip.gui;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.catnip.gui.widget.BoxWidget;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class NavigatableSimiScreen extends AbstractSimiScreen {

    public static final Couple<Color> COLOR_NAV_ARROW = Couple.create(new Color(0x80_aa9999, true), new Color(0x30_aa9999)).map(Color::setImmutable);

    protected static boolean currentlyRenderingPreviousScreen = false;

    protected int depthPointX, depthPointY;
    public final LerpedFloat transition = LerpedFloat.linear().startWithValue(0).chase(0, .1f, LerpedFloat.Chaser.LINEAR);
    protected final LerpedFloat arrowAnimation = LerpedFloat.linear().startWithValue(0).chase(0, 0.075f, LerpedFloat.Chaser.LINEAR);
    @Nullable
    protected BoxWidget backTrack;

    public NavigatableSimiScreen() {
        Window window = MinecraftClient.getInstance().getWindow();
        depthPointX = window.getScaledWidth() / 2;
        depthPointY = window.getScaledHeight() / 2;
    }

    @Override
    public void close() {
        ScreenOpener.clearStack();
        super.close();
    }

    @Override
    public void tick() {
        super.tick();
        transition.tickChaser();
        arrowAnimation.tickChaser();
    }

    @Override
    protected void init() {
        super.init();

        backTrack = null;
        List<Screen> screenHistory = ScreenOpener.getScreenHistory();
        if (screenHistory.isEmpty())
            return;

        addDrawableChild(backTrack = new BoxWidget(31, height - 31 - 20).withBounds(20, 20).withCustomBackground(BoxElement.COLOR_BACKGROUND_FLAT)
            .enableFade(0, 5).withPadding(2, 2).fade(1).withCallback(() -> ScreenOpener.openPreviousScreen(this, null)));

        Screen previousScreen = screenHistory.getFirst();
        if (previousScreen instanceof NavigatableSimiScreen screen) {
            screen.initBackTrackIcon(backTrack);
        } else {
            backTrack.showing(PonderGuiTextures.ICON_DISABLE);
        }

    }

    /**
     * Called when {@code this} represents the previous screen to
     * initialize the {@code backTrack} icon of the current screen.
     *
     * @param backTrack The backTrack button of the current screen.
     */
    protected abstract void initBackTrackIcon(BoxWidget backTrack);

    protected Text backTrackingComponent() {
        if (ScreenOpener.getBackStepScreen() instanceof NavigatableSimiScreen) {
            return Lang.builder("catnip").translate("gui.step_back").component();
        }

        return Lang.builder("catnip").translate("gui.exit").component();
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        //		renderZeloBreadcrumbs(ms, mouseX, mouseY, partialTicks);
        if (backTrack == null)
            return;

        Matrix3x2fStack poseStack = graphics.getMatrices();

        int x = (int) MathHelper.lerp(arrowAnimation.getValue(partialTicks), -9, 21);
        int maxX = backTrack.getX() + backTrack.getWidth();
        Couple<Color> colors = COLOR_NAV_ARROW;

        poseStack.pushMatrix();
        poseStack.translate(0, 0);
        if (x + 30 < backTrack.getX())
            UIRenderHelper.breadcrumbArrow(graphics, x + 30, height - 51, maxX - (x + 30), 20, 5, colors);

        UIRenderHelper.breadcrumbArrow(graphics, x, height - 51, 30, 20, 5, colors);
        UIRenderHelper.breadcrumbArrow(graphics, x - 30, height - 51, 30, 20, 5, colors);
        poseStack.popMatrix();

        poseStack.pushMatrix();
        poseStack.translate(0, 0);
        if (backTrack.isSelected()) {
            Text component = backTrackingComponent();
            graphics.drawText(
                textRenderer,
                component,
                41 - textRenderer.getWidth(component) / 2,
                height - 16,
                UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB(),
                false
            );
            if (MathHelper.approximatelyEquals(arrowAnimation.getValue(), arrowAnimation.getChaseTarget())) {
                arrowAnimation.setValue(1);
                arrowAnimation.setValue(1);// called twice to also set the previous value to 1
            }
        }
        poseStack.popMatrix();
    }

    @Override
    public void renderBackground(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isCurrentlyRenderingPreviousScreen())
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWindowBackground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (transition.getChaseTarget() == 0 || transition.settled()) {
            renderBackground(graphics, mouseX, mouseY, partialTicks);
            return;
        }

        renderBackground(graphics, mouseX, mouseY, partialTicks);

        Matrix3x2fStack ms = graphics.getMatrices();

        Window window = client.getWindow();
        float guiScaledWidth = window.getScaledWidth();
        float guiScaledHeight = window.getScaledHeight();

        Screen lastScreen = ScreenOpener.getPreviouslyRenderedScreen();
        float tValue = transition.getValue(partialTicks);
        float tValueAbsolute = Math.abs(tValue);

        // draw last screen into buffer
        if (lastScreen != null && lastScreen != this && !transition.settled()) {
            currentlyRenderingPreviousScreen = true;
            ms.pushMatrix();
            lastScreen.render(graphics, 0, 0, partialTicks);
            ms.popMatrix();

            ms.pushMatrix();
            int dpx = (int) (guiScaledWidth / 2);
            int dpy = (int) (guiScaledHeight / 2);
            if (lastScreen instanceof NavigatableSimiScreen navigableScreen && tValue > 0) {
                dpx = navigableScreen.depthPointX;
                dpy = navigableScreen.depthPointY;
            }

            float scale = 1 + (0.2f * tValue);

            Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, guiScaledWidth, guiScaledHeight, 0.0F, 1000.0F, 3000.0F);
            MatrixStack poseStack2 = new MatrixStack();
            poseStack2.peek().getPositionMatrix().set(matrix4f);
            poseStack2.translate(dpx, dpy, 0);
            poseStack2.scale(scale, scale, 1);
            poseStack2.translate(-dpx, -dpy, 0);


            UIRenderHelper.drawFramebuffer(poseStack2, 1f - tValueAbsolute);
            ms.popMatrix();
            currentlyRenderingPreviousScreen = false;
        }

        // modify current screen as well
        float scale = tValue > 0 ? 1 - 0.5f * (1 - tValueAbsolute) : 1 + .5f * (1 - tValueAbsolute);
        int dpx = (int) (guiScaledWidth / 2);
        //dpx = depthPointX;
        int dpy = (int) (guiScaledHeight / 2);
        //dpy = depthPointY;
        ms.translate(dpx, dpy);
        ms.scale(scale, scale);
        ms.translate(-dpx, -dpy);
    }

    @Override
    public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (code == GLFW.GLFW_KEY_BACKSPACE) {
            ScreenOpener.openPreviousScreen(this, null);
            return true;
        }
        return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
    }

    public void centerScalingOn(int x, int y) {
        depthPointX = x;
        depthPointY = y;
    }

    public void centerScalingOnMouse() {
        Window w = client.getWindow();
        double mouseX = client.mouse.getX() * w.getScaledWidth() / w.getWidth();
        double mouseY = client.mouse.getY() * w.getScaledHeight() / w.getHeight();
        centerScalingOn((int) mouseX, (int) mouseY);
    }

    public boolean isEquivalentTo(NavigatableSimiScreen other) {
        return false;
    }

    public void shareContextWith(NavigatableSimiScreen other) {
    }

    protected void renderZeloBreadcrumbs(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        List<Screen> history = ScreenOpener.getScreenHistory();
        if (history.isEmpty())
            return;

        history.add(0, client.currentScreen);
        int spacing = 20;

        List<String> names = new ArrayList<>();
        for (Screen screen : history)
            names.add(NavigatableSimiScreen.screenTitle(screen));

        int bWidth = 0;
        for (String name : names) {
            bWidth += textRenderer.getWidth(name) + spacing;
        }

        MutableInt x = new MutableInt(width - bWidth);
        MutableInt y = new MutableInt(height - 18);
        MutableBoolean first = new MutableBoolean(true);

        if (x.getValue() < 25)
            x.setValue(25);

        Matrix3x2fStack poseStack = graphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(0, 0);
        names.forEach(s -> {
            int sWidth = textRenderer.getWidth(s);
            UIRenderHelper.breadcrumbArrow(
                graphics,
                x.getValue(),
                y.getValue(),
                sWidth + spacing,
                14,
                spacing / 2,
                new Color(0xdd101010),
                new Color(0x44101010)
            );
            graphics.drawText(textRenderer, s, x.getValue() + 5, y.getValue() + 3, first.getValue() ? 0xffeeffee : 0xffddeeff, true);
            first.setFalse();

            x.add(sWidth + spacing);
        });
        poseStack.popMatrix();
    }

    public static boolean isCurrentlyRenderingPreviousScreen() {
        return currentlyRenderingPreviousScreen;
    }

    private static String screenTitle(Screen screen) {
        if (screen instanceof NavigatableSimiScreen)
            return ((NavigatableSimiScreen) screen).getBreadcrumbTitle();
        return "<";
    }

    protected String getBreadcrumbTitle() {
        return this.getClass().getSimpleName();
    }
}
