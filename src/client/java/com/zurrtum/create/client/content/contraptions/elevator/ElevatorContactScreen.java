package com.zurrtum.create.client.content.contraptions.elevator;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.TooltipArea;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import com.zurrtum.create.infrastructure.packet.c2s.ElevatorContactEditPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

public class ElevatorContactScreen extends AbstractSimiScreen {

    private final AllGuiTextures background;

    private EditBox shortNameInput;
    private EditBox longNameInput;
    private IconButton confirm;
    private ElementWidget renderedItem;

    private String shortName;
    private String longName;
    private DoorControl doorControl;

    private final BlockPos pos;

    public ElevatorContactScreen(BlockPos pos, String prevShortName, String prevLongName, DoorControl prevDoorControl) {
        super(CreateLang.translateDirect("elevator_contact.title"));
        this.pos = pos;
        this.doorControl = prevDoorControl;
        background = AllGuiTextures.ELEVATOR_CONTACT;
        this.shortName = prevShortName;
        this.longName = prevLongName;
    }

    @Override
    public void init() {
        setWindowSize(background.getWidth() + 30, background.getHeight());
        super.init();

        int x = guiLeft;
        int y = guiTop;

        confirm = new IconButton(x + 200, y + 58, AllIcons.I_CONFIRM);
        confirm.withCallback(this::confirm);
        addRenderableWidget(confirm);

        shortNameInput = editBox(33, 30, 4);
        shortNameInput.setValue(shortName);
        centerInput(x);
        shortNameInput.setResponder(s -> {
            shortName = s;
            centerInput(x);
        });
        shortNameInput.setFocused(true);
        setFocused(shortNameInput);
        shortNameInput.setHighlightPos(0);

        longNameInput = editBox(63, 140, 30);
        longNameInput.setValue(longName);
        longNameInput.setResponder(s -> longName = s);

        MutableComponent rmbToEdit = CreateLang.translate("gui.schedule.lmb_edit").style(ChatFormatting.DARK_GRAY).style(ChatFormatting.ITALIC)
            .component();

        addRenderableOnly(new TooltipArea(x + 21, y + 23, 30, 18).withTooltip(ImmutableList.of(
            CreateLang.translate(
                "elevator_contact.floor_identifier").color(0x5391E1).component(), rmbToEdit
        )));

        addRenderableOnly(new TooltipArea(x + 57, y + 23, 147, 18).withTooltip(ImmutableList.of(
            CreateLang.translate("elevator_contact.floor_description").color(0x5391E1).component(),
            CreateLang.translate("crafting_blueprint.optional").style(ChatFormatting.GRAY).component(),
            rmbToEdit
        )));
        Pair<ScrollInput, Label> doorControlWidgets = SlidingDoorRenderer.createWidget(
            minecraft,
            x + 58,
            y + 57,
            mode -> doorControl = mode,
            doorControl
        );
        addRenderableWidget(doorControlWidgets.getFirst());
        addRenderableWidget(doorControlWidgets.getSecond());

        renderedItem = new ElementWidget(
            x + background.getWidth() + 6,
            y + background.getHeight() - 56
        ).showingElement(GuiGameElement.of(AllItems.ELEVATOR_CONTACT.getDefaultInstance()).scale(5));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    private int centerInput(int x) {
        int centeredX = x + (shortName.isEmpty() ? 34 : 36 - font.width(shortName) / 2);
        shortNameInput.setX(centeredX);
        return centeredX;
    }

    private EditBox editBox(int x, int width, int chars) {
        EditBox editBox = new EditBox(font, guiLeft + x, guiTop + 30, width, 10, CommonComponents.EMPTY);
        editBox.setTextColor(-1);
        editBox.setTextColorUneditable(-1);
        editBox.setBordered(false);
        editBox.setMaxLength(chars);
        editBox.setFocused(false);
        editBox.mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
        addRenderableWidget(editBox);
        return editBox;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        FormattedCharSequence formattedcharsequence = title.getVisualOrderText();
        graphics.drawString(
            font,
            formattedcharsequence,
            x + (background.getWidth() - 8) / 2 - font.width(formattedcharsequence) / 2,
            y + 6,
            0xFF2F3738,
            false
        );

        graphics.renderItem(AllItems.TRAIN_DOOR.getDefaultInstance(), x + 37, y + 58);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean consumed = super.mouseClicked(click, doubled);

        if (!shortNameInput.isFocused()) {
            int length = shortNameInput.getValue().length();
            shortNameInput.setHighlightPos(length);
            shortNameInput.setCursorPosition(length);
        }

        if (shortNameInput.isHoveredOrFocused())
            longNameInput.mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);

        if (!consumed && click.x() > guiLeft + 22 && click.y() > guiTop + 24 && click.x() < guiLeft + 50 && click.y() < guiTop + 40) {
            setFocused(shortNameInput);
            shortNameInput.setFocused(true);
            return true;
        }

        return consumed;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (super.keyPressed(input))
            return true;
        int keyCode = input.key();
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            confirm();
            return true;
        }
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            onClose();
            return true;
        }
        return false;
    }

    private void confirm() {
        minecraft.player.connection.send(new ElevatorContactEditPacket(pos, shortName, longName, doorControl));
        onClose();
    }

}
