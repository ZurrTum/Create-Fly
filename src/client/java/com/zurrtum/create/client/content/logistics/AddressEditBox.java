package com.zurrtum.create.client.content.logistics;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class AddressEditBox extends EditBox {

    private final DestinationSuggestions destinationSuggestions;
    private final Consumer<String> mainResponder;
    private String prevValue = "=)";

    public AddressEditBox(Screen screen, Font pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom) {
        this(screen, pFont, pX, pY, pWidth, pHeight, anchorToBottom, null);
    }

    public AddressEditBox(Screen screen, Font pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom, @Nullable String localAddress) {
        super(pFont, pX, pY, pWidth, pHeight, Component.empty());
        destinationSuggestions = AddressEditBoxHelper.createSuggestions(screen, this, anchorToBottom, localAddress);
        destinationSuggestions.setAllowSuggestions(true);
        destinationSuggestions.updateCommandInfo();
        mainResponder = t -> {
            if (!t.equals(prevValue)) {
                int length = t.length();
                if (highlightPos > length) {
                    highlightPos = length;
                }
                destinationSuggestions.updateCommandInfo();
            }
            prevValue = t;
        };
        setResponder(mainResponder);
        setBordered(false);
        setFocused(false);
        mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
        setMaxLength(25);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (destinationSuggestions.keyPressed(input))
            return true;
        if (isFocused() && input.key() == GLFW.GLFW_KEY_ENTER) {
            setFocused(false);
            moveCursorToEnd(false);
            mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (destinationSuggestions.mouseScrolled(Mth.clamp(scrollY, -1.0D, 1.0D)))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (isMouseOver(click.x(), click.y())) {
                setValue("");
                return true;
            }
        }

        boolean wasFocused = isFocused();
        if (super.mouseClicked(click, doubled)) {
            if (!wasFocused) {
                setHighlightPos(0);
                setCursorPosition(getValue().length());
            }
            return true;
        }
        return destinationSuggestions.mouseClicked(click);
    }

    @Override
    public void setValue(String text) {
        setHighlightPos(0);
        super.setValue(text);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        destinationSuggestions.render(pGuiGraphics, pMouseX, pMouseY);

        if (!destinationSuggestions.isEmpty())
            return;

        int itemX = getX() + width + 4;
        int itemY = getY() - 4;
        pGuiGraphics.renderItem(AllItems.CLIPBOARD.getDefaultInstance(), itemX, itemY);
        if (pMouseX >= itemX && pMouseX < itemX + 16 && pMouseY >= itemY && pMouseY < itemY + 16) {
            List<Component> promiseTip = List.of(
                CreateLang.translate("gui.address_box.clipboard_tip").color(ScrollInput.HEADER_RGB).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_1").style(ChatFormatting.GRAY).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_2").style(ChatFormatting.GRAY).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_3").style(ChatFormatting.GRAY).component(),
                CreateLang.translate("gui.address_box.clipboard_tip_4").style(ChatFormatting.DARK_GRAY).component()
            );
            pGuiGraphics.setComponentTooltipForNextFrame(font, promiseTip, pMouseX, pMouseY);
        }
    }

    @Override
    public void setResponder(Consumer<String> pResponder) {
        super.setResponder(pResponder == mainResponder ? mainResponder : mainResponder.andThen(pResponder));
    }

    public void tick() {
        if (!isFocused())
            destinationSuggestions.hide();
        if (isFocused() && destinationSuggestions.suggestions == null)
            destinationSuggestions.updateCommandInfo();
        destinationSuggestions.tick();
    }
}