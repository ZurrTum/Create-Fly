package com.zurrtum.create.client.content.schematics.table;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.schematics.client.ClientSchematicLoader;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.schematics.table.SchematicTableBlockEntity;
import com.zurrtum.create.content.schematics.table.SchematicTableMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.foundation.utility.CreatePaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE_PROGRESS;

public class SchematicTableScreen extends AbstractSimiContainerScreen<SchematicTableMenu> {

    private final Component uploading = CreateLang.translateDirect("gui.schematicTable.uploading");
    private final Component finished = CreateLang.translateDirect("gui.schematicTable.finished");
    private final Component refresh = CreateLang.translateDirect("gui.schematicTable.refresh");
    private final Component folder = CreateLang.translateDirect("gui.schematicTable.open_folder");
    private final Component noSchematics = CreateLang.translateDirect("gui.schematicTable.noSchematics");
    private final Component availableSchematicsTitle = CreateLang.translateDirect("gui.schematicTable.availableSchematics");

    protected AllGuiTextures background;

    private @Nullable ScrollInput schematicsArea;
    private IconButton confirmButton;
    private IconButton folderButton;
    private IconButton refreshButton;
    private @Nullable Label schematicsLabel;
    private ElementWidget renderedItem;

    private float progress;
    private float chasingProgress;
    private float lastChasingProgress;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public SchematicTableScreen(SchematicTableMenu menu, Inventory playerInventory, Component title) {
        super(
            menu,
            playerInventory,
            title,
            AllGuiTextures.SCHEMATIC_TABLE.getWidth(),
            AllGuiTextures.SCHEMATIC_TABLE.getHeight() + 4 + PLAYER_INVENTORY.getHeight()
        );
        background = AllGuiTextures.SCHEMATIC_TABLE;
    }

    @Nullable
    public static SchematicTableScreen create(
        Minecraft mc,
        MenuType<SchematicTableBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        SchematicTableBlockEntity entity = getBlockEntity(mc, extraData);
        if (entity == null) {
            return null;
        }
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            ValueInput view = TagValueInput.create(logging, extraData.registryAccess(), extraData.readNbt());
            entity.readClient(view);
            return type.create(SchematicTableScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 8);
        super.init();

        Create.SCHEMATIC_SENDER.refresh();
        List<Component> availableSchematics = Create.SCHEMATIC_SENDER.getAvailableSchematics();

        int y = this.topPos + 2;

        schematicsLabel = new Label(leftPos + 51, y + 26, CommonComponents.EMPTY).withShadow();
        schematicsLabel.text = CommonComponents.EMPTY;
        if (!availableSchematics.isEmpty()) {
            schematicsArea = new SelectionScrollInput(leftPos + 45, y + 21, 139, 18).forOptions(availableSchematics)
                .titled(availableSchematicsTitle.plainCopy()).writingTo(schematicsLabel);
            addRenderableWidget(schematicsArea);
            addRenderableWidget(schematicsLabel);
        }

        confirmButton = new IconButton(leftPos + 44, y + 56, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            if (menu.canWrite() && schematicsArea != null) {
                ClientSchematicLoader schematicSender = Create.SCHEMATIC_SENDER;
                lastChasingProgress = chasingProgress = progress = 0;
                List<Component> availableSchematics1 = schematicSender.getAvailableSchematics();
                Component schematic = availableSchematics1.get(schematicsArea.getState());
                schematicSender.startNewUpload(minecraft, schematic.getString());
            }
        });

        folderButton = new IconButton(leftPos + 20, y + 21, AllIcons.I_OPEN_FOLDER);
        folderButton.withCallback(() -> {
            Util.getPlatform().openFile(CreatePaths.SCHEMATICS_DIR.toFile());
        });
        folderButton.setToolTip(folder);
        refreshButton = new IconButton(leftPos + 206, y + 21, AllIcons.I_REFRESH);
        refreshButton.withCallback(() -> {
            ClientSchematicLoader schematicSender = Create.SCHEMATIC_SENDER;
            schematicSender.refresh();
            List<Component> availableSchematics1 = schematicSender.getAvailableSchematics();
            removeWidget(schematicsArea);

            if (!availableSchematics1.isEmpty()) {
                schematicsArea = new SelectionScrollInput(leftPos + 45, this.topPos + 21, 139, 18).forOptions(availableSchematics1)
                    .titled(availableSchematicsTitle.plainCopy()).writingTo(schematicsLabel);
                schematicsArea.onChanged();
                addRenderableWidget(schematicsArea);
            } else {
                schematicsArea = null;
                schematicsLabel.text = CommonComponents.EMPTY;
            }
        });
        refreshButton.setToolTip(refresh);

        addRenderableWidget(confirmButton);
        addRenderableWidget(folderButton);
        addRenderableWidget(refreshButton);

        extraAreas = ImmutableList.of(
            new Rect2i(leftPos + imageWidth, y + background.getHeight() - 40, 48, 48),
            new Rect2i(refreshButton.getX(), refreshButton.getY(), refreshButton.getWidth(), refreshButton.getHeight())
        );

        renderedItem = new ElementWidget(
            leftPos + imageWidth,
            this.topPos + background.getHeight() - 40
        ).showingElement(GuiGameElement.of(AllItems.SCHEMATIC_TABLE.getDefaultInstance()).scale(3));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        background.render(graphics, leftPos, topPos);

        Component titleText;
        if (menu.contentHolder.isUploading)
            titleText = uploading;
        else if (menu.getSlot(1).hasItem())
            titleText = finished;
        else
            titleText = title;

        graphics.drawString(font, titleText, leftPos + (imageWidth - 8 - font.width(titleText)) / 2, topPos + 4, 0xFF505050, false);

        if (schematicsArea == null)
            graphics.drawString(font, noSchematics, leftPos + 54, topPos + 28, 0xFFD3D3D3, true);

        int width = (int) (SCHEMATIC_TABLE_PROGRESS.getWidth() * Mth.lerp(partialTicks, lastChasingProgress, chasingProgress));
        int height = SCHEMATIC_TABLE_PROGRESS.getHeight();
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            SCHEMATIC_TABLE_PROGRESS.location,
            leftPos + 70,
            topPos + 59,
            SCHEMATIC_TABLE_PROGRESS.getStartX(),
            SCHEMATIC_TABLE_PROGRESS.getStartY(),
            width,
            height,
            256,
            256
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        boolean finished = menu.getSlot(1).hasItem();

        if (menu.contentHolder.isUploading || finished) {
            if (finished) {
                chasingProgress = lastChasingProgress = progress = 1;
            } else {
                lastChasingProgress = chasingProgress;
                progress = menu.contentHolder.uploadingProgress;
                chasingProgress += (progress - chasingProgress) * .5f;
            }
            confirmButton.active = false;

            if (schematicsLabel != null) {
                schematicsLabel.colored(0xFFCCDDFF);
                String uploadingSchematic = menu.contentHolder.uploadingSchematic;
                if (uploadingSchematic == null) {
                    schematicsLabel.text = null;
                } else {
                    schematicsLabel.text = Component.literal(uploadingSchematic);
                }
            }
            if (schematicsArea != null)
                schematicsArea.visible = false;

        } else {
            progress = 0;
            chasingProgress = lastChasingProgress = 0;
            confirmButton.active = true;

            if (schematicsLabel != null)
                schematicsLabel.colored(0xFFFFFFFF);
            if (schematicsArea != null) {
                schematicsArea.writingTo(schematicsLabel);
                schematicsArea.visible = true;
            }
        }
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}