package com.zurrtum.create.client.content.schematics.client;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SchematicEditScreen extends AbstractSimiScreen {

    private final List<Text> rotationOptions = CreateLang.translatedOptions("schematic.rotation", "none", "cw90", "cw180", "cw270");
    private final List<Text> mirrorOptions = CreateLang.translatedOptions("schematic.mirror", "none", "leftRight", "frontBack");
    private final Text rotationLabel = CreateLang.translateDirect("schematic.rotation");
    private final Text mirrorLabel = CreateLang.translateDirect("schematic.mirror");

    private AllGuiTextures background;

    private TextFieldWidget xInput;
    private TextFieldWidget yInput;
    private TextFieldWidget zInput;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    private ScrollInput rotationArea;
    private ScrollInput mirrorArea;
    private SchematicHandler handler;

    public SchematicEditScreen() {
        background = AllGuiTextures.SCHEMATIC;
        handler = Create.SCHEMATIC_HANDLER;
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        setWindowOffset(-6, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop + 2;

        xInput = new TextFieldWidget(textRenderer, x + 50, y + 26, 34, 10, ScreenTexts.EMPTY);
        yInput = new TextFieldWidget(textRenderer, x + 90, y + 26, 34, 10, ScreenTexts.EMPTY);
        zInput = new TextFieldWidget(textRenderer, x + 130, y + 26, 34, 10, ScreenTexts.EMPTY);

        BlockPos anchor = handler.getTransformation().getAnchor();
        if (handler.isDeployed()) {
            xInput.setText("" + anchor.getX());
            yInput.setText("" + anchor.getY());
            zInput.setText("" + anchor.getZ());
        } else {
            BlockPos alt = client.player.getBlockPos();
            xInput.setText("" + alt.getX());
            yInput.setText("" + alt.getY());
            zInput.setText("" + alt.getZ());
        }

        for (TextFieldWidget widget : new TextFieldWidget[]{xInput, yInput, zInput}) {
            widget.setMaxLength(6);
            widget.setDrawsBackground(false);
            widget.setEditableColor(0xFFFFFFFF);
            widget.setFocused(false);
            widget.mouseClicked(new Click(0, 0, new MouseInput(0, 0)), false);
            widget.setTextPredicate(s -> {
                if (s.isEmpty() || s.equals("-"))
                    return true;
                try {
                    Integer.parseInt(s);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
        }

        StructurePlacementData settings = handler.getTransformation().toSettings();
        Label labelR = new Label(x + 50, y + 48, ScreenTexts.EMPTY).withShadow();
        rotationArea = new SelectionScrollInput(x + 45, y + 43, 118, 18).forOptions(rotationOptions).titled(rotationLabel.copyContentOnly())
            .setState(settings.getRotation().ordinal()).writingTo(labelR);

        Label labelM = new Label(x + 50, y + 70, ScreenTexts.EMPTY).withShadow();
        mirrorArea = new SelectionScrollInput(x + 45, y + 65, 118, 18).forOptions(mirrorOptions).titled(mirrorLabel.copyContentOnly())
            .setState(settings.getMirror().ordinal()).writingTo(labelM);

        addRenderableWidgets(xInput, yInput, zInput);
        addRenderableWidgets(labelR, labelM, rotationArea, mirrorArea);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 26, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);

        renderedItem = new ElementWidget(
            x + background.getWidth() + 6,
            y + background.getHeight() - 40
        ).showingElement(GuiGameElement.of(AllItems.SCHEMATIC.getDefaultStack()).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    @Override
    public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (isPaste(code)) {
            String coords = client.keyboard.getClipboard();
            if (coords != null && !coords.isEmpty()) {
                coords.replaceAll(" ", "");
                String[] split = coords.split(",");
                if (split.length == 3) {
                    boolean valid = true;
                    for (String s : split) {
                        try {
                            Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            valid = false;
                        }
                    }
                    if (valid) {
                        xInput.setText(split[0]);
                        yInput.setText(split[1]);
                        zInput.setText(split[2]);
                        return true;
                    }
                }
            }
        }

        return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        String title = handler.getCurrentSchematicName();
        graphics.drawText(textRenderer, title, x + (background.getWidth() - 8 - textRenderer.getWidth(title)) / 2, y + 4, 0xFF505050, false);
    }

    @Override
    public void removed() {
        boolean validCoords = true;
        BlockPos newLocation = null;
        try {
            newLocation = new BlockPos(Integer.parseInt(xInput.getText()), Integer.parseInt(yInput.getText()), Integer.parseInt(zInput.getText()));
        } catch (NumberFormatException e) {
            validCoords = false;
        }

        StructurePlacementData settings = new StructurePlacementData();
        settings.setRotation(BlockRotation.values()[rotationArea.getState()]);
        settings.setMirror(BlockMirror.values()[mirrorArea.getState()]);

        if (validCoords && newLocation != null) {
            ItemStack item = handler.getActiveSchematicItem();
            if (item != null) {
                item.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
                item.set(AllDataComponents.SCHEMATIC_ANCHOR, newLocation);
            }

            handler.getTransformation().init(newLocation, settings, handler.getBounds());
            handler.markDirty();
            handler.deploy(client);
        }
    }

}