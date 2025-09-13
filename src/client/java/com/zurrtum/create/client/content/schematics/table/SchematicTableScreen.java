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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.SCHEMATIC_TABLE_PROGRESS;

public class SchematicTableScreen extends AbstractSimiContainerScreen<SchematicTableMenu> {

    private final Text uploading = CreateLang.translateDirect("gui.schematicTable.uploading");
    private final Text finished = CreateLang.translateDirect("gui.schematicTable.finished");
    private final Text refresh = CreateLang.translateDirect("gui.schematicTable.refresh");
    private final Text folder = CreateLang.translateDirect("gui.schematicTable.open_folder");
    private final Text noSchematics = CreateLang.translateDirect("gui.schematicTable.noSchematics");
    private final Text availableSchematicsTitle = CreateLang.translateDirect("gui.schematicTable.availableSchematics");

    protected AllGuiTextures background;

    private ScrollInput schematicsArea;
    private IconButton confirmButton;
    private IconButton folderButton;
    private IconButton refreshButton;
    private Label schematicsLabel;
    private ElementWidget renderedItem;

    private float progress;
    private float chasingProgress;
    private float lastChasingProgress;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public SchematicTableScreen(SchematicTableMenu menu, PlayerInventory playerInventory, Text title) {
        super(menu, playerInventory, title);
        background = AllGuiTextures.SCHEMATIC_TABLE;
    }

    public static SchematicTableScreen create(
        MinecraftClient mc,
        MenuType<SchematicTableBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        SchematicTableBlockEntity entity = getBlockEntity(mc, extraData);
        if (entity == null) {
            return null;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getReporterContext(), LOGGER)) {
            ReadView view = NbtReadView.create(logging, extraData.getRegistryManager(), extraData.readNbt());
            entity.readClient(view);
            return type.create(SchematicTableScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + 4 + PLAYER_INVENTORY.getHeight());
        setWindowOffset(-11, 8);
        super.init();

        Create.SCHEMATIC_SENDER.refresh();
        List<Text> availableSchematics = Create.SCHEMATIC_SENDER.getAvailableSchematics();

        int y = this.y + 2;

        schematicsLabel = new Label(x + 51, y + 26, ScreenTexts.EMPTY).withShadow();
        schematicsLabel.text = ScreenTexts.EMPTY;
        if (!availableSchematics.isEmpty()) {
            schematicsArea = new SelectionScrollInput(x + 45, y + 21, 139, 18).forOptions(availableSchematics)
                .titled(availableSchematicsTitle.copyContentOnly()).writingTo(schematicsLabel);
            addDrawableChild(schematicsArea);
            addDrawableChild(schematicsLabel);
        }

        confirmButton = new IconButton(x + 44, y + 56, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            if (handler.canWrite() && schematicsArea != null) {
                ClientSchematicLoader schematicSender = Create.SCHEMATIC_SENDER;
                lastChasingProgress = chasingProgress = progress = 0;
                List<Text> availableSchematics1 = schematicSender.getAvailableSchematics();
                Text schematic = availableSchematics1.get(schematicsArea.getState());
                schematicSender.startNewUpload(client, schematic.getString());
            }
        });

        folderButton = new IconButton(x + 20, y + 21, AllIcons.I_OPEN_FOLDER);
        folderButton.withCallback(() -> {
            Util.getOperatingSystem().open(CreatePaths.SCHEMATICS_DIR.toFile());
        });
        folderButton.setToolTip(folder);
        refreshButton = new IconButton(x + 206, y + 21, AllIcons.I_REFRESH);
        refreshButton.withCallback(() -> {
            ClientSchematicLoader schematicSender = Create.SCHEMATIC_SENDER;
            schematicSender.refresh();
            List<Text> availableSchematics1 = schematicSender.getAvailableSchematics();
            remove(schematicsArea);

            if (!availableSchematics1.isEmpty()) {
                schematicsArea = new SelectionScrollInput(x + 45, this.y + 21, 139, 18).forOptions(availableSchematics1)
                    .titled(availableSchematicsTitle.copyContentOnly()).writingTo(schematicsLabel);
                schematicsArea.onChanged();
                addDrawableChild(schematicsArea);
            } else {
                schematicsArea = null;
                schematicsLabel.text = ScreenTexts.EMPTY;
            }
        });
        refreshButton.setToolTip(refresh);

        addDrawableChild(confirmButton);
        addDrawableChild(folderButton);
        addDrawableChild(refreshButton);

        extraAreas = ImmutableList.of(
            new Rect2i(x + background.getWidth(), y + background.getHeight() - 40, 48, 48),
            new Rect2i(refreshButton.getX(), refreshButton.getY(), refreshButton.getWidth(), refreshButton.getHeight())
        );

        renderedItem = new ElementWidget(
            x + background.getWidth(),
            this.y + background.getHeight() - 40
        ).showingElement(GuiGameElement.of(AllItems.SCHEMATIC_TABLE.getDefaultStack()).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = y + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        background.render(graphics, x, y);

        Text titleText;
        if (handler.contentHolder.isUploading)
            titleText = uploading;
        else if (handler.getSlot(1).hasStack())
            titleText = finished;
        else
            titleText = title;

        graphics.drawText(textRenderer, titleText, x + (background.getWidth() - 8 - textRenderer.getWidth(titleText)) / 2, y + 4, 0xFF505050, false);

        if (schematicsArea == null)
            graphics.drawText(textRenderer, noSchematics, x + 54, y + 28, 0xFFD3D3D3, true);

        int width = (int) (SCHEMATIC_TABLE_PROGRESS.getWidth() * MathHelper.lerp(partialTicks, lastChasingProgress, chasingProgress));
        int height = SCHEMATIC_TABLE_PROGRESS.getHeight();
        graphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            SCHEMATIC_TABLE_PROGRESS.location,
            x + 70,
            y + 59,
            SCHEMATIC_TABLE_PROGRESS.getStartX(),
            SCHEMATIC_TABLE_PROGRESS.getStartY(),
            width,
            height,
            256,
            256
        );
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        boolean finished = handler.getSlot(1).hasStack();

        if (handler.contentHolder.isUploading || finished) {
            if (finished) {
                chasingProgress = lastChasingProgress = progress = 1;
            } else {
                lastChasingProgress = chasingProgress;
                progress = handler.contentHolder.uploadingProgress;
                chasingProgress += (progress - chasingProgress) * .5f;
            }
            confirmButton.active = false;

            if (schematicsLabel != null) {
                schematicsLabel.colored(0xFFCCDDFF);
                String uploadingSchematic = handler.contentHolder.uploadingSchematic;
                if (uploadingSchematic == null) {
                    schematicsLabel.text = null;
                } else {
                    schematicsLabel.text = Text.literal(uploadingSchematic);
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