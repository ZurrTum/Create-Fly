package com.zurrtum.create.client.content.logistics;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class AddressEditBox extends TextFieldWidget {

    private final DestinationSuggestions destinationSuggestions;
    private final Consumer<String> mainResponder;
    private String prevValue = "=)";

    public AddressEditBox(Screen screen, TextRenderer pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom) {
        this(screen, pFont, pX, pY, pWidth, pHeight, anchorToBottom, null);
    }

    public AddressEditBox(Screen screen, TextRenderer pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom, String localAddress) {
        super(pFont, pX, pY, pWidth, pHeight, Text.empty());
        destinationSuggestions = AddressEditBoxHelper.createSuggestions(screen, this, anchorToBottom, localAddress);
        destinationSuggestions.setWindowActive(true);
        destinationSuggestions.refresh();
        mainResponder = t -> {
            if (!t.equals(prevValue))
                destinationSuggestions.refresh();
            prevValue = t;
        };
        setChangedListener(mainResponder);
        setDrawsBackground(false);
        setFocused(false);
        mouseClicked(new Click(0, 0, new MouseInput(0, 0)), false);
        setMaxLength(25);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (destinationSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers))
            return true;
        if (isFocused() && pKeyCode == GLFW.GLFW_KEY_ENTER) {
            setFocused(false);
            setCursorToEnd(false);
            mouseClicked(new Click(0, 0, new MouseInput(0, 0)), false);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (destinationSuggestions.mouseScrolled(MathHelper.clamp(scrollY, -1.0D, 1.0D)))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (isMouseOver(click.x(), click.y())) {
                setText("");
                return true;
            }
        }

        boolean wasFocused = isFocused();
        if (super.mouseClicked(click, doubled)) {
            if (!wasFocused) {
                setSelectionEnd(0);
                setSelectionStart(getText().length());
            }
            return true;
        }
        return destinationSuggestions.mouseClicked(click);
    }

    @Override
    public void setText(String text) {
        setSelectionEnd(0);
        super.setText(text);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public void renderWidget(DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        destinationSuggestions.render(pGuiGraphics, pMouseX, pMouseY);

        if (!destinationSuggestions.isEmpty())
            return;

        int itemX = getX() + width + 4;
        int itemY = getY() - 4;
        pGuiGraphics.drawItem(AllItems.CLIPBOARD.getDefaultStack(), itemX, itemY);
        if (pMouseX >= itemX && pMouseX < itemX + 16 && pMouseY >= itemY && pMouseY < itemY + 16) {
            List<Text> promiseTip = List.of(
                CreateLang.translate("gui.address_box.clipboard_tip").color(ScrollInput.HEADER_RGB).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_1").style(Formatting.GRAY).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_2").style(Formatting.GRAY).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_3").style(Formatting.GRAY).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_4").style(Formatting.DARK_GRAY).component()
            );
            pGuiGraphics.drawTooltip(textRenderer, promiseTip, pMouseX, pMouseY);
        }
    }

    @Override
    public void setChangedListener(Consumer<String> pResponder) {
        super.setChangedListener(pResponder == mainResponder ? mainResponder : mainResponder.andThen(pResponder));
    }

    public void tick() {
        if (!isFocused())
            destinationSuggestions.clearWindow();
        if (isFocused() && destinationSuggestions.window == null)
            destinationSuggestions.refresh();
        destinationSuggestions.tick();
    }
}