package com.zurrtum.create.client.content.schematics.client;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SchematicPromptScreen extends AbstractSimiScreen {

    private final AllGuiTextures background;

    private final Text convertLabel = CreateLang.translateDirect("schematicAndQuill.convert");
    private final Text abortLabel = CreateLang.translateDirect("action.discard");
    private final Text confirmLabel = CreateLang.translateDirect("action.saveToFile");

    private TextFieldWidget nameField;
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

        nameField = new TextFieldWidget(textRenderer, x + 49, y + 26, 131, 10, ScreenTexts.EMPTY);
        nameField.setEditableColor(-1);
        nameField.setUneditableColor(-1);
        nameField.setDrawsBackground(false);
        nameField.setMaxLength(35);
        nameField.setFocused(true);
        setFocused(nameField);
        addDrawableChild(nameField);

        abort = new IconButton(x + 7, y + 53, AllIcons.I_TRASH);
        abort.withCallback(() -> {
            Create.SCHEMATIC_AND_QUILL_HANDLER.discard(client);
            close();
        });
        abort.setToolTip(abortLabel);
        addDrawableChild(abort);

        confirm = new IconButton(x + 158, y + 53, AllIcons.I_CONFIRM);
        confirm.withCallback(() -> {
            confirm(false);
        });
        confirm.setToolTip(confirmLabel);
        addDrawableChild(confirm);

        convert = new IconButton(x + 180, y + 53, AllIcons.I_SCHEMATIC);
        convert.withCallback(() -> {
            confirm(true);
        });
        convert.setToolTip(convertLabel);
        addDrawableChild(convert);

        renderedItem = new ElementWidget(x + background.getWidth() + 6, guiTop + background.getHeight() - 38).showingElement(GuiGameElement.of(
            AllItems.SCHEMATIC_AND_QUILL.getDefaultStack()).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.drawText(textRenderer, title, x + (background.getWidth() - 8 - textRenderer.getWidth(title)) / 2, y + 4, 0xFF505050, false);

        graphics.drawItem(AllItems.SCHEMATIC.getDefaultStack(), x + 22, y + 24);
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            confirm(false);
            return true;
        }
        if (keyCode == 256 && this.shouldCloseOnEsc()) {
            close();
            return true;
        }
        return nameField.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    private void confirm(boolean convertImmediately) {
        Create.SCHEMATIC_AND_QUILL_HANDLER.saveSchematic(client, nameField.getText(), convertImmediately);
        close();
    }
}
