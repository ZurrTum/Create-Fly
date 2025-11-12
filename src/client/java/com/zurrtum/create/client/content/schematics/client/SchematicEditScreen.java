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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.List;

public class SchematicEditScreen extends AbstractSimiScreen {

    private final List<Component> rotationOptions = CreateLang.translatedOptions("schematic.rotation", "none", "cw90", "cw180", "cw270");
    private final List<Component> mirrorOptions = CreateLang.translatedOptions("schematic.mirror", "none", "leftRight", "frontBack");
    private final Component rotationLabel = CreateLang.translateDirect("schematic.rotation");
    private final Component mirrorLabel = CreateLang.translateDirect("schematic.mirror");

    private AllGuiTextures background;

    private EditBox xInput;
    private EditBox yInput;
    private EditBox zInput;
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

        xInput = new EditBox(font, x + 50, y + 26, 34, 10, CommonComponents.EMPTY);
        yInput = new EditBox(font, x + 90, y + 26, 34, 10, CommonComponents.EMPTY);
        zInput = new EditBox(font, x + 130, y + 26, 34, 10, CommonComponents.EMPTY);

        BlockPos anchor = handler.getTransformation().getAnchor();
        if (handler.isDeployed()) {
            xInput.setValue("" + anchor.getX());
            yInput.setValue("" + anchor.getY());
            zInput.setValue("" + anchor.getZ());
        } else {
            BlockPos alt = minecraft.player.blockPosition();
            xInput.setValue("" + alt.getX());
            yInput.setValue("" + alt.getY());
            zInput.setValue("" + alt.getZ());
        }

        for (EditBox widget : new EditBox[]{xInput, yInput, zInput}) {
            widget.setMaxLength(6);
            widget.setBordered(false);
            widget.setTextColor(0xFFFFFFFF);
            widget.setFocused(false);
            widget.mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
            widget.setFilter(s -> {
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

        StructurePlaceSettings settings = handler.getTransformation().toSettings();
        Label labelR = new Label(x + 50, y + 48, CommonComponents.EMPTY).withShadow();
        rotationArea = new SelectionScrollInput(x + 45, y + 43, 118, 18).forOptions(rotationOptions).titled(rotationLabel.plainCopy())
            .setState(settings.getRotation().ordinal()).writingTo(labelR);

        Label labelM = new Label(x + 50, y + 70, CommonComponents.EMPTY).withShadow();
        mirrorArea = new SelectionScrollInput(x + 45, y + 65, 118, 18).forOptions(mirrorOptions).titled(mirrorLabel.plainCopy())
            .setState(settings.getMirror().ordinal()).writingTo(labelM);

        addRenderableWidgets(xInput, yInput, zInput);
        addRenderableWidgets(labelR, labelM, rotationArea, mirrorArea);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 26, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        renderedItem = new ElementWidget(
            x + background.getWidth() + 6,
            y + background.getHeight() - 40
        ).showingElement(GuiGameElement.of(AllItems.SCHEMATIC.getDefaultInstance()).scale(3));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.isPaste()) {
            String coords = minecraft.keyboardHandler.getClipboard();
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
                        xInput.setValue(split[0]);
                        yInput.setValue(split[1]);
                        zInput.setValue(split[2]);
                        return true;
                    }
                }
            }
        }

        return super.keyPressed(input);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        String title = handler.getCurrentSchematicName();
        graphics.drawString(font, title, x + (background.getWidth() - 8 - font.width(title)) / 2, y + 4, 0xFF505050, false);
    }

    @Override
    public void removed() {
        boolean validCoords = true;
        BlockPos newLocation = null;
        try {
            newLocation = new BlockPos(Integer.parseInt(xInput.getValue()), Integer.parseInt(yInput.getValue()), Integer.parseInt(zInput.getValue()));
        } catch (NumberFormatException e) {
            validCoords = false;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setRotation(Rotation.values()[rotationArea.getState()]);
        settings.setMirror(Mirror.values()[mirrorArea.getState()]);

        if (validCoords && newLocation != null) {
            ItemStack item = handler.getActiveSchematicItem();
            if (item != null) {
                item.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
                item.set(AllDataComponents.SCHEMATIC_ANCHOR, newLocation);
            }

            handler.getTransformation().init(newLocation, settings, handler.getBounds());
            handler.markDirty();
            handler.deploy(minecraft);
        }
    }

}