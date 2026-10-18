package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SchematicPromptScreen extends AbstractSimiScreen {

    private final AllGuiTextures background;

    private final Component convertLabel = CreateLang.translateDirect("schematicAndQuill.convert");
    private final Component abortLabel = CreateLang.translateDirect("action.discard");
    private final Component confirmLabel = CreateLang.translateDirect("action.saveToFile");

    private EditBox nameField;
    private IconButton confirm;
    private IconButton abort;
    private IconButton convert;
    private ElementWidget renderedItem;

    public SchematicPromptScreen() {
        super(CreateLang.translateDirect("schematicAndQuill.title"));
        background = AllGuiTextures.SCHEMATIC_PROMPT;
    }

    @Override
    public void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        super.init();

        int x = guiLeft;
        int y = guiTop + 2;

        nameField = new EditBox(font, x + 49, y + 26, 131, 10, CommonComponents.EMPTY);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setMaxLength(35);
        nameField.setFocused(true);
        setFocused(nameField);
        addRenderableWidget(nameField);

        abort = new IconButton(x + 7, y + 53, AllIcons.I_TRASH);
        abort.withCallback(() -> {
            Create.SCHEMATIC_AND_QUILL_HANDLER.discard(minecraft);
            onClose();
        });
        abort.setToolTip(abortLabel);
        addRenderableWidget(abort);

        confirm = new IconButton(x + 158, y + 53, AllIcons.I_CONFIRM);
        confirm.withCallback(() -> {
            confirm(false);
        });
        confirm.setToolTip(confirmLabel);
        addRenderableWidget(confirm);

        convert = new IconButton(x + 180, y + 53, AllIcons.I_SCHEMATIC);
        convert.withCallback(() -> {
            confirm(true);
        });
        convert.setToolTip(convertLabel);
        addRenderableWidget(convert);

        renderedItem = new ElementWidget(
            x + background.getWidth() + 6,
            guiTop + background.getHeight() - 38
        ).showingElement(GuiGameElement.of(AllItems.SCHEMATIC_AND_QUILL.getDefaultInstance()).scale(3));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void removed() {
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.text(font, title, x + (background.getWidth() - 8 - font.width(title)) / 2, y + 4, 0xFF505050, false);

        graphics.item(AllItems.SCHEMATIC.getDefaultInstance(), x + 22, y + 24);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.key();
        if (keyCode == InputConstants.KEY_RETURN) {
            confirm(false);
            return true;
        }
        if (keyCode == InputConstants.KEY_ESCAPE && shouldCloseOnEsc()) {
            onClose();
            return true;
        }
        return nameField.keyPressed(input);
    }

    private void confirm(boolean convertImmediately) {
        Create.SCHEMATIC_AND_QUILL_HANDLER.saveSchematic(minecraft, nameField.getValue(), convertImmediately);
        onClose();
    }
}
