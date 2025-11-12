package com.zurrtum.create.client.catnip.gui.element;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.render.BoxRenderState;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2f;

public class BoxElement extends AbstractRenderElement {

    public static final Couple<Color> COLOR_VANILLA_BORDER = Couple.create(new Color(0x50_5000ff, true), new Color(0x50_28007f, true))
        .map(Color::setImmutable);
    public static final Color COLOR_VANILLA_BACKGROUND = new Color(0xf0_100010, true).setImmutable();
    public static final Color COLOR_BACKGROUND_FLAT = new Color(0xff_000000, true).setImmutable();
    public static final Color COLOR_BACKGROUND_TRANSPARENT = new Color(0xdd_000000, true).setImmutable();

    protected Color background = COLOR_VANILLA_BACKGROUND;
    protected Color borderTop = COLOR_VANILLA_BORDER.getFirst();
    protected Color borderBot = COLOR_VANILLA_BORDER.getSecond();
    protected int borderOffset = 2;

    public <T extends BoxElement> T withBackground(Color color) {
        this.background = color;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends BoxElement> T withBackground(int color) {
        return withBackground(new Color(color, true));
    }

    public <T extends BoxElement> T flatBorder(Color color) {
        this.borderTop = color;
        this.borderBot = color;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends BoxElement> T flatBorder(int color) {
        return flatBorder(new Color(color, true));
    }

    public <T extends BoxElement> T gradientBorder(Couple<Color> colors) {
        this.borderTop = colors.getFirst();
        this.borderBot = colors.getSecond();
        //noinspection unchecked
        return (T) this;
    }

    public <T extends BoxElement> T gradientBorder(Color top, Color bot) {
        this.borderTop = top;
        this.borderBot = bot;
        //noinspection unchecked
        return (T) this;
    }

    public <T extends BoxElement> T gradientBorder(int top, int bot) {
        return gradientBorder(new Color(top, true), new Color(bot, true));
    }

    public <T extends BoxElement> T withBorderOffset(int offset) {
        this.borderOffset = offset;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public void render(GuiGraphics graphics) {
        renderBox(graphics);
    }

    //total box width = 1 * 2 (outer border) + 1 * 2 (inner color border) + 2 * borderOffset + width
    //defaults to 2 + 2 + 4 + 16 = 24px
    //batch everything together to save a bunch of gl calls over ScreenUtils
    protected void renderBox(GuiGraphics graphics) {
        /*
         *          _____________
         *        _|_____________|_
         *       | | ___________ | |
         *       | | |  |      | | |
         *       | | |  |      | | |
         *       | | |--*   |  | | |
         *       | | |      h  | | |
         *       | | |  --w-+  | | |
         *       | | |         | | |
         *       | | |_________| | |
         *       |_|_____________|_|
         *         |_____________|
         *
         * */
        Color c1 = background.copy().scaleAlpha(alpha);
        Color c2 = borderTop.copy().scaleAlpha(alpha);
        Color c3 = borderBot.copy().scaleAlpha(alpha);
        Matrix3x2f model = new Matrix3x2f(graphics.pose());
        graphics.guiRenderState.submitGuiElement(new BoxRenderState(model, x, y, width, height, borderOffset, c1, c2, c3));
    }
}
