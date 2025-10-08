package com.zurrtum.create.client.catnip.gui.widget;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.TickableGuiEventListener;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractSimiWidget extends ClickableWidget implements TickableGuiEventListener {

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
    protected List<Text> toolTip = new LinkedList<>();
    protected BiConsumer<Integer, Integer> onClick = (_$, _$$) -> {
    };

    public int lockedTooltipX = -1;
    public int lockedTooltipY = -1;

    protected AbstractSimiWidget(int x, int y) {
        this(x, y, 16, 16);
    }

    protected AbstractSimiWidget(int x, int y, int width, int height) {
        this(x, y, width, height, ScreenTexts.EMPTY);
    }

    protected AbstractSimiWidget(int x, int y, int width, int height, Text message) {
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
        return (T) this;
    }

    public List<Text> getToolTip() {
        return toolTip;
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            hovered = isMouseOver(mouseX, mouseY);
            renderWidget(graphics, mouseX, mouseY, partialTicks);
            wasHovered = isSelected();
        }
    }

    @Override
    protected void renderWidget(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        beforeRender(graphics, mouseX, mouseY, partialTicks);
        doRender(graphics, mouseX, mouseY, partialTicks);
        afterRender(graphics, mouseX, mouseY, partialTicks);
    }

    protected void beforeRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.getMatrices().pushMatrix();
    }

    protected void doRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
    }

    protected void afterRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.getMatrices().popMatrix();
    }

    public void runCallback(double mouseX, double mouseY) {
        onClick.accept((int) mouseX, (int) mouseY);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        runCallback(click.x(), click.y());
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder pNarrationElementOutput) {
        appendDefaultNarrations(pNarrationElementOutput);
    }

    public void setHeight(int value) {
        this.height = value;
    }
}
