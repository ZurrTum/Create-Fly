package com.zurrtum.create.client.foundation.blockEntity;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter.ScrollOptionSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.infrastructure.packet.c2s.ValueSettingsPacket;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ValueSettingsScreen extends AbstractSimiScreen {

    private int ticksOpen;
    private ValueSettingsBoard board;
    private int maxLabelWidth;
    private int valueBarWidth;
    private BlockPos pos;
    private ValueSettings initialSettings;
    private ValueSettings lastHovered = new ValueSettings(-1, -1);
    private Consumer<ValueSettings> onHover;
    private boolean iconMode;
    private int milestoneSize;
    private int soundCoolDown;
    private int netId;

    public ValueSettingsScreen(BlockPos pos, ValueSettingsBoard board, ValueSettings valueSettings, Consumer<ValueSettings> onHover, int netId) {
        this.pos = pos;
        this.board = board;
        this.initialSettings = valueSettings;
        this.onHover = onHover;
        this.netId = netId;
        this.iconMode = board.formatter() instanceof ScrollOptionSettingsFormatter;
        this.milestoneSize = iconMode ? 8 : 4;
    }

    @Override
    protected void init() {
        int maxValue = board.maxValue();
        maxLabelWidth = 0;
        int milestoneCount = maxValue / board.milestoneInterval() + 1;
        int scale = maxValue > 128 ? 1 : 2;

        for (Text component : board.rows())
            maxLabelWidth = Math.max(maxLabelWidth, textRenderer.getWidth(component));
        if (iconMode)
            maxLabelWidth = -18;

        valueBarWidth = (maxValue + 1) * scale + 1 + milestoneCount * milestoneSize;
        int width = (maxLabelWidth + 14) + (valueBarWidth + 10);
        int height = (board.rows().size() * 11);

        setWindowSize(width, height);
        super.init();

        Vec2f coordinateOfValue = getCoordinateOfValue(initialSettings.row(), initialSettings.value());
        setCursor(coordinateOfValue);
    }

    private void setCursor(Vec2f coordinateOfValue) {
        double guiScale = client.getWindow().getScaleFactor();
        GLFW.glfwSetCursorPos(client.getWindow().getHandle(), coordinateOfValue.x * guiScale, coordinateOfValue.y * guiScale);
    }

    public ValueSettings getClosestCoordinate(int mouseX, int mouseY) {
        int row = 0;
        int column = 0;
        boolean milestonesOnly = hasShiftDown();

        double bestDiff = Double.MAX_VALUE;
        for (; row < board.rows().size(); row++) {
            Vec2f coord = getCoordinateOfValue(row, 0);
            double diff = Math.abs(coord.y - mouseY);
            if (bestDiff < diff)
                break;
            bestDiff = diff;
        }
        row -= 1;

        bestDiff = Double.MAX_VALUE;
        for (; column <= board.maxValue(); column++) {
            Vec2f coord = getCoordinateOfValue(row, milestonesOnly ? column * board.milestoneInterval() : column);
            double diff = Math.abs(coord.x - mouseX);
            if (bestDiff < diff)
                break;
            bestDiff = diff;
        }
        column -= 1;

        return new ValueSettings(row, milestonesOnly ? Math.min(column * board.milestoneInterval(), board.maxValue()) : column);
    }

    public Vec2f getCoordinateOfValue(int row, int column) {
        int scale = board.maxValue() > 128 ? 1 : 2;
        float xOut = guiLeft + ((Math.max(1, column) - 1) / board.milestoneInterval()) * milestoneSize + column * scale + 1.5f;
        xOut += maxLabelWidth + 14 + 4;

        if (column % board.milestoneInterval() == 0)
            xOut += milestoneSize / 2;
        if (column > 0)
            xOut += milestoneSize;

        float yOut = guiTop + (row + .5f) * 11 - .5f;
        return new Vec2f(xOut, yOut);
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;
        int milestoneCount = board.maxValue() / board.milestoneInterval() + 1;
        int scale = board.maxValue() > 128 ? 1 : 2;

        Text title = board.title();
        Text tip = CreateLang.translateDirect("gui.value_settings.release_to_confirm", Text.keybind("key.use"));
        double fadeIn = Math.pow(MathHelper.clamp((ticksOpen + partialTicks) / 4.0, 0, 1), 1);

        int fattestLabel = Math.max(textRenderer.getWidth(tip), textRenderer.getWidth(title));
        if (iconMode)
            for (int i = 0; i <= board.maxValue(); i++)
                fattestLabel = Math.max(fattestLabel, textRenderer.getWidth(board.formatter().format(new ValueSettings(0, i))));

        int fatTipOffset = Math.max(0, fattestLabel + 10 - (windowWidth + 13)) / 2;
        int bgWidth = Math.max((windowWidth + 13), fattestLabel + 10);
        int fadeInWidth = (int) (bgWidth * fadeIn);
        int fadeInStart = (bgWidth - fadeInWidth) / 2 - fatTipOffset;
        int additionalHeight = iconMode ? 46 : 33;

        UIRenderHelper.drawStretched(
            graphics,
            x - 11 + fadeInStart,
            y - 17,
            fadeInWidth,
            windowHeight + additionalHeight,
            AllGuiTextures.VALUE_SETTINGS_OUTER_BG
        );
        UIRenderHelper.drawStretched(graphics, x - 10 + fadeInStart, y - 18, fadeInWidth - 2, 1, AllGuiTextures.VALUE_SETTINGS_OUTER_BG);
        UIRenderHelper.drawStretched(
            graphics,
            x - 10 + fadeInStart,
            y - 17 + windowHeight + additionalHeight,
            fadeInWidth - 2,
            1,
            AllGuiTextures.VALUE_SETTINGS_OUTER_BG
        );

        if (fadeInWidth > fattestLabel) {
            int textX = x - 11 - fatTipOffset + bgWidth / 2;
            graphics.drawText(textRenderer, title, textX - textRenderer.getWidth(title) / 2, y - 14, 0xffdddddd, false);
            graphics.drawText(textRenderer, tip, textX - textRenderer.getWidth(tip) / 2, y + windowHeight + additionalHeight - 27, 0xffdddddd, false);
        }

        renderBrassFrame(graphics, x + maxLabelWidth + 14, y - 3, valueBarWidth + 8, board.rows().size() * 11 + 5);
        UIRenderHelper.drawStretched(
            graphics,
            x + maxLabelWidth + 17,
            y,
            valueBarWidth + 2,
            board.rows().size() * 11 - 1,
            AllGuiTextures.VALUE_SETTINGS_BAR_BG
        );

        int originalY = y;
        for (Text component : board.rows()) {
            int valueBarX = x + maxLabelWidth + 14 + 4;

            if (!iconMode) {
                UIRenderHelper.drawCropped(graphics, x - 4, y, maxLabelWidth + 8, 11, AllGuiTextures.VALUE_SETTINGS_LABEL_BG);
                for (int w = 0; w < valueBarWidth; w += AllGuiTextures.VALUE_SETTINGS_BAR.getWidth() - 1)
                    UIRenderHelper.drawCropped(
                        graphics,
                        valueBarX + w,
                        y + 1,
                        Math.min(AllGuiTextures.VALUE_SETTINGS_BAR.getWidth() - 1, valueBarWidth - w),
                        8,
                        AllGuiTextures.VALUE_SETTINGS_BAR
                    );
                graphics.drawText(textRenderer, component, x, y + 1, 0xFF442000, false);
            }

            int milestoneX = valueBarX;
            for (int milestone = 0; milestone < milestoneCount; milestone++) {
                if (iconMode)
                    AllGuiTextures.VALUE_SETTINGS_WIDE_MILESTONE.render(graphics, milestoneX, y + 1);
                else
                    AllGuiTextures.VALUE_SETTINGS_MILESTONE.render(graphics, milestoneX, y + 1);
                milestoneX += milestoneSize + board.milestoneInterval() * scale;
            }

            y += 11;
        }

        if (!iconMode)
            renderBrassFrame(graphics, x - 7, originalY - 3, maxLabelWidth + 14, board.rows().size() * 11 + 5);

        if (ticksOpen < 1)
            return;

        ValueSettings closest = getClosestCoordinate(mouseX, mouseY);

        if (!closest.equals(lastHovered)) {
            onHover.accept(closest);
            if (soundCoolDown == 0) {
                float pitch = (closest.value()) / (float) (board.maxValue());
                pitch = MathHelper.lerp(pitch, 1.15f, 1.5f);
                client.getSoundManager().play(PositionedSoundInstance.master(AllSoundEvents.SCROLL_VALUE.getMainEvent(), pitch, 0.25F));
                ScrollValueHandler.wrenchCog.bump(3, -(closest.value() - lastHovered.value()) * 10);
                soundCoolDown = 1;
            }
        }
        lastHovered = closest;

        Vec2f coordinate = getCoordinateOfValue(closest.row(), closest.value());
        Text cursorText = board.formatter().format(closest);

        AllIcons cursorIcon = null;
        if (board.formatter() instanceof ScrollOptionSettingsFormatter sosf)
            cursorIcon = sosf.getIcon(closest);

        int cursorWidth = ((cursorIcon != null ? 16 : textRenderer.getWidth(cursorText)) / 2) * 2 + 3;
        int cursorX = ((int) (coordinate.x)) - cursorWidth / 2;
        int cursorY = ((int) (coordinate.y)) - 7;

        if (cursorIcon != null) {
            AllGuiTextures.VALUE_SETTINGS_CURSOR_ICON.render(graphics, cursorX - 2, cursorY - 3);
            cursorIcon.render(graphics, cursorX + 1, cursorY - 1, 0xFF431F00);
            if (fadeInWidth > fattestLabel)
                graphics.drawText(
                    textRenderer,
                    cursorText,
                    x - 11 - fatTipOffset + (bgWidth - textRenderer.getWidth(cursorText)) / 2,
                    originalY + windowHeight + additionalHeight - 40,
                    0xFFFBDC7D,
                    false
                );
            return;
        }

        AllGuiTextures.VALUE_SETTINGS_CURSOR_LEFT.render(graphics, cursorX - 3, cursorY);
        UIRenderHelper.drawCropped(graphics, cursorX, cursorY, cursorWidth, 14, AllGuiTextures.VALUE_SETTINGS_CURSOR);
        AllGuiTextures.VALUE_SETTINGS_CURSOR_RIGHT.render(graphics, cursorX + cursorWidth, cursorY);

        graphics.drawText(textRenderer, cursorText, cursorX + 2, cursorY + 3, 0xFF442000, false);
    }

    protected void renderBrassFrame(DrawContext graphics, int x, int y, int w, int h) {
        AllGuiTextures.BRASS_FRAME_TL.render(graphics, x, y);
        AllGuiTextures.BRASS_FRAME_TR.render(graphics, x + w - 4, y);
        AllGuiTextures.BRASS_FRAME_BL.render(graphics, x, y + h - 4);
        AllGuiTextures.BRASS_FRAME_BR.render(graphics, x + w - 4, y + h - 4);

        if (h > 8) {
            UIRenderHelper.drawStretched(graphics, x, y + 4, 3, h - 8, AllGuiTextures.BRASS_FRAME_LEFT);
            UIRenderHelper.drawStretched(graphics, x + w - 3, y + 4, 3, h - 8, AllGuiTextures.BRASS_FRAME_RIGHT);
        }

        if (w > 8) {
            UIRenderHelper.drawCropped(graphics, x + 4, y, w - 8, 3, AllGuiTextures.BRASS_FRAME_TOP);
            UIRenderHelper.drawCropped(graphics, x + 4, y + h - 3, w - 8, 3, AllGuiTextures.BRASS_FRAME_BOTTOM);
        }

    }

    @Override
    public void renderBackground(@NotNull DrawContext graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int a = ((int) (0x50 * Math.min(1, (ticksOpen + pPartialTick) / 20f))) << 24;
        graphics.fillGradient(0, 0, this.width, this.height, 0x101010 | a, 0x101010 | a);
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (soundCoolDown > 0)
            soundCoolDown--;
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        ValueSettings closest = getClosestCoordinate((int) pMouseX, (int) pMouseY);
        int column = closest.value() + ((int) Math.signum(pScrollY)) * (hasShiftDown() ? board.milestoneInterval() : 1);
        column = MathHelper.clamp(column, 0, board.maxValue());
        if (column == closest.value())
            return false;
        setCursor(getCoordinateOfValue(closest.row(), column));
        return true;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (client.options.useKey.matchesKey(pKeyCode, pScanCode)) {
            Window window = client.getWindow();
            double x = client.mouse.getX() * window.getScaledWidth() / window.getWidth();
            double y = client.mouse.getY() * window.getScaledHeight() / window.getHeight();
            saveAndClose(x, y);
            return true;
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (client.options.useKey.matchesMouse(click)) {
            saveAndClose(click.x(), click.y());
            return true;
        }
        return super.mouseReleased(click);
    }

    protected void saveAndClose(double pMouseX, double pMouseY) {
        ValueSettings closest = getClosestCoordinate((int) pMouseX, (int) pMouseY);
        client.player.networkHandler.sendPacket(new ValueSettingsPacket(
            pos,
            closest.row(),
            closest.value(),
            null,
            null,
            Direction.UP,
            hasControlDown(),
            netId
        ));
        close();
    }

    @Override
    public void close() {
        super.close();
    }

}
