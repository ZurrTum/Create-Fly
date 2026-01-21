package com.zurrtum.create.client.catnip.gui.widget;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.TickableGuiEventListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractSimiWidget extends AbstractWidget implements TickableGuiEventListener {

    public static final Color HEADER_RGB = new Color(0x5391e1, false);
    public static final Color HINT_RGB = new Color(0x96b7e0, false);

    public static final Couple<Color> COLOR_IDLE = Couple.create(new Color(0xdd_8ab6d6, true), new Color(0x90_8ab6d6, true)).map(Color::setImmutable);
    public static final Couple<Color> COLOR_HOVER = Couple.create(new Color(0xff_9abbd3, true), new Color(0xd0_9abbd3, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_CLICK = Couple.create(new Color(0xff_ffffff, true), new Color(0xee_ffffff, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_DISABLED = Couple.create(new Color(0x80_909090, true), new Color(0x60_909090, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_SUCCESS = Couple.create(new Color(0xcc_88f788, true), new Color(0xcc_20cc20, true))
        .map(Color::setImmutable);
    public static final Couple<Color> COLOR_FAIL = Couple.create(new Color(0xcc_f78888, true), new Color(0xcc_cc2020, true)).map(Color::setImmutable);

    protected float z;
    protected boolean wasHovered = false;
    protected List<Component> toolTip = new LinkedList<>();
    protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {
    };

    public int lockedTooltipX = -1;
    public int lockedTooltipY = -1;

    protected AbstractSimiWidget(int x, int y) {
        this(x, y, 16, 16);
    }

    protected AbstractSimiWidget(int x, int y, int width, int height) {
        this(x, y, width, height, CommonComponents.EMPTY);
    }

    protected AbstractSimiWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public <T extends AbstractSimiWidget> T withCallback(BiConsumer<Integer, Integer> cb) {
        this.onClick = cb;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends AbstractSimiWidget> T withCallback(Runnable cb) {
        return withCallback((_$, _$$) -> cb.run());
    }

    public <T extends AbstractSimiWidget> T atZLevel(float z) {
        this.z = z;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends AbstractSimiWidget> T setActive(boolean active) {
        this.active = active;
        //noinspection unchecked
        return (T) this;
    }

    public List<Component> getToolTip() {
        return toolTip;
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            isHovered = isMouseOver(mouseX, mouseY);
            renderWidget(graphics, mouseX, mouseY, partialTicks);
            renderTooltip(graphics, mouseX, mouseY, partialTicks);
            wasHovered = isHoveredOrFocused();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        beforeRender(graphics, mouseX, mouseY, partialTicks);
        doRender(graphics, mouseX, mouseY, partialTicks);
        afterRender(graphics, mouseX, mouseY, partialTicks);
    }

    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.isHovered()) {
            List<Component> tooltip = this.getToolTip();
            if (tooltip.isEmpty())
                return;
            int ttx = this.lockedTooltipX == -1 ? mouseX : this.lockedTooltipX + this.getX();
            int tty = this.lockedTooltipY == -1 ? mouseY : this.lockedTooltipY + this.getY();

            graphics.setComponentTooltipForNextFrame(graphics.minecraft.font, tooltip, ttx, tty);
        }
    }

    protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().pushMatrix();
    }

    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }

    protected void afterRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.pose().popMatrix();
    }

    public void runCallback(double mouseX, double mouseY) {
        onClick.accept((int) mouseX, (int) mouseY);
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        runCallback(click.x(), click.y());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        defaultButtonNarrationText(pNarrationElementOutput);
    }
}
