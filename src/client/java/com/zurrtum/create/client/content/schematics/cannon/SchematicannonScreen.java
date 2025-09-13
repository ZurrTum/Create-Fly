package com.zurrtum.create.client.content.schematics.cannon;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Indicator;
import com.zurrtum.create.client.foundation.gui.widget.Indicator.State;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureSchematicannonPacket;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureSchematicannonPacket.Option;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.Create.LOGGER;

public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonMenu> {

    private static final AllGuiTextures BG_BOTTOM = AllGuiTextures.SCHEMATICANNON_BOTTOM;
    private static final AllGuiTextures BG_TOP = AllGuiTextures.SCHEMATICANNON_TOP;

    private final Text listPrinter = CreateLang.translateDirect("gui.schematicannon.listPrinter");
    private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
    private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
    private final String _showSettings = "gui.schematicannon.showOptions";
    private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

    private final String _slotGunpowder = "gui.schematicannon.slot.gunpowder";
    private final String _slotListPrinter = "gui.schematicannon.slot.listPrinter";
    private final String _slotSchematic = "gui.schematicannon.slot.schematic";

    private final Text optionEnabled = CreateLang.translateDirect("gui.schematicannon.optionEnabled");
    private final Text optionDisabled = CreateLang.translateDirect("gui.schematicannon.optionDisabled");

    protected List<Indicator> replaceLevelIndicators;
    protected List<IconButton> replaceLevelButtons;

    protected IconButton skipMissingButton;
    protected Indicator skipMissingIndicator;
    protected IconButton skipBlockEntitiesButton;
    protected Indicator skipBlockEntitiesIndicator;

    protected IconButton playButton;
    protected Indicator playIndicator;
    protected IconButton pauseButton;
    protected Indicator pauseIndicator;
    protected IconButton resetButton;
    protected Indicator resetIndicator;

    private IconButton confirmButton;
    private IconButton showSettingsButton;
    private Indicator showSettingsIndicator;
    private ElementWidget renderedItem;

    protected List<ClickableWidget> placementSettingWidgets;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public SchematicannonScreen(SchematicannonMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
        placementSettingWidgets = new ArrayList<>();
    }

    public static SchematicannonScreen create(
        MinecraftClient mc,
        MenuType<SchematicannonBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        SchematicannonBlockEntity entity = getBlockEntity(mc, extraData);
        if (entity == null) {
            return null;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getReporterContext(), LOGGER)) {
            ReadView view = NbtReadView.create(logging, extraData.getRegistryManager(), extraData.readNbt());
            entity.readClient(view);
            return type.create(SchematicannonScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    protected void init() {
        setWindowSize(BG_TOP.getWidth(), BG_TOP.getHeight() + BG_BOTTOM.getHeight() + 2 + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        setWindowOffset(-11, 0);
        super.init();

        // Play Pause Stop
        playButton = new IconButton(x + 75, y + 85, AllIcons.I_PLAY);
        playButton.withCallback(() -> {
            sendOptionUpdate(Option.PLAY, true);
        });
        playIndicator = new Indicator(x + 75, y + 79, ScreenTexts.EMPTY);
        pauseButton = new IconButton(x + 93, y + 85, AllIcons.I_PAUSE);
        pauseButton.withCallback(() -> {
            sendOptionUpdate(Option.PAUSE, true);
        });
        pauseIndicator = new Indicator(x + 93, y + 79, ScreenTexts.EMPTY);
        resetButton = new IconButton(x + 111, y + 85, AllIcons.I_STOP);
        resetButton.withCallback(() -> {
            sendOptionUpdate(Option.STOP, true);
        });
        resetIndicator = new Indicator(x + 111, y + 79, ScreenTexts.EMPTY);
        resetIndicator.state = State.RED;
        addRenderableWidgets(playButton, playIndicator, pauseButton, pauseIndicator, resetButton, resetIndicator);

        confirmButton = new IconButton(x + 180, y + 111, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            client.player.closeHandledScreen();
        });
        addDrawableChild(confirmButton);
        showSettingsButton = new IconButton(x + 8, y + 111, AllIcons.I_PLACEMENT_SETTINGS);
        showSettingsButton.withCallback(() -> {
            showSettingsIndicator.state = placementSettingsHidden() ? State.GREEN : State.OFF;
            initPlacementSettings();
        });
        showSettingsButton.setToolTip(CreateLang.translateDirect(_showSettings));
        addDrawableChild(showSettingsButton);
        showSettingsIndicator = new Indicator(x + 9, y + 111, ScreenTexts.EMPTY);
        //		addRenderableWidget(showSettingsIndicator);

        extraAreas = ImmutableList.of(new Rect2i(x + BG_TOP.getWidth(), y + BG_TOP.getHeight() + BG_BOTTOM.getHeight() - 62, 84, 92));

        renderedItem = new ElementWidget(x + BG_TOP.getWidth() - 14, y + BG_TOP.getHeight() + BG_BOTTOM.getHeight() - 62).showingElement(
            GuiGameElement.of(AllItems.SCHEMATICANNON.getDefaultStack()).scale(5).padding(28));
        addDrawableChild(renderedItem);

        tick();
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    private void initPlacementSettings() {
        removeWidgets(placementSettingWidgets);
        placementSettingWidgets.clear();

        if (placementSettingsHidden())
            return;

        // Replace settings
        replaceLevelButtons = new ArrayList<>(4);
        replaceLevelIndicators = new ArrayList<>(4);
        List<AllIcons> icons = ImmutableList.of(AllIcons.I_DONT_REPLACE, AllIcons.I_REPLACE_SOLID, AllIcons.I_REPLACE_ANY, AllIcons.I_REPLACE_EMPTY);
        List<Text> toolTips = ImmutableList.of(
            CreateLang.translateDirect("gui.schematicannon.option.dontReplaceSolid"),
            CreateLang.translateDirect("gui.schematicannon.option.replaceWithSolid"),
            CreateLang.translateDirect("gui.schematicannon.option.replaceWithAny"),
            CreateLang.translateDirect("gui.schematicannon.option.replaceWithEmpty")
        );

        for (int i = 0; i < 4; i++) {
            replaceLevelIndicators.add(new Indicator(x + 33 + i * 18, y + 111, ScreenTexts.EMPTY));
            IconButton replaceLevelButton = new IconButton(x + 33 + i * 18, y + 111, icons.get(i));
            int replaceMode = i;
            replaceLevelButton.withCallback(() -> {
                if (handler.contentHolder.replaceMode != replaceMode)
                    sendOptionUpdate(Option.values()[replaceMode], true);
            });
            replaceLevelButton.setToolTip(toolTips.get(i));
            replaceLevelButtons.add(replaceLevelButton);
        }
        placementSettingWidgets.addAll(replaceLevelButtons);
        //		placementSettingWidgets.addAll(replaceLevelIndicators);

        // Other Settings
        skipMissingButton = new IconButton(x + 111, y + 111, AllIcons.I_SKIP_MISSING);
        skipMissingButton.withCallback(() -> {
            sendOptionUpdate(Option.SKIP_MISSING, !handler.contentHolder.skipMissing);
        });
        skipMissingButton.setToolTip(CreateLang.translateDirect("gui.schematicannon.option.skipMissing"));
        skipMissingIndicator = new Indicator(x + 111, y + 111, ScreenTexts.EMPTY);
        Collections.addAll(placementSettingWidgets, skipMissingButton);

        skipBlockEntitiesButton = new IconButton(x + 135, y + 111, AllIcons.I_SKIP_BLOCK_ENTITIES);
        skipBlockEntitiesButton.withCallback(() -> {
            sendOptionUpdate(Option.SKIP_BLOCK_ENTITIES, !handler.contentHolder.replaceBlockEntities);
        });
        skipBlockEntitiesButton.setToolTip(CreateLang.translateDirect("gui.schematicannon.option.skipBlockEntities"));
        skipBlockEntitiesIndicator = new Indicator(x + 129, y + 111, ScreenTexts.EMPTY);
        Collections.addAll(placementSettingWidgets, skipBlockEntitiesButton);

        addRenderableWidgets(placementSettingWidgets);
    }

    protected boolean placementSettingsHidden() {
        return showSettingsIndicator.state == State.OFF;
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        SchematicannonBlockEntity be = handler.contentHolder;

        if (!placementSettingsHidden()) {
            for (int replaceMode = 0; replaceMode < replaceLevelButtons.size(); replaceMode++) {
                replaceLevelButtons.get(replaceMode).green = replaceMode == be.replaceMode;
                replaceLevelIndicators.get(replaceMode).state = replaceMode == be.replaceMode ? State.ON : State.OFF;
            }
            skipMissingButton.green = be.skipMissing;
            skipBlockEntitiesButton.green = !be.replaceBlockEntities;
        }

        playIndicator.state = State.OFF;
        pauseIndicator.state = State.OFF;
        resetIndicator.state = State.OFF;

        switch (be.state) {
            case PAUSED:
                pauseIndicator.state = State.YELLOW;
                playButton.active = true;
                pauseButton.active = false;
                resetButton.active = true;
                break;
            case RUNNING:
                playIndicator.state = State.GREEN;
                playButton.active = false;
                pauseButton.active = true;
                resetButton.active = true;
                break;
            case STOPPED:
                resetIndicator.state = State.RED;
                playButton.active = true;
                pauseButton.active = false;
                resetButton.active = false;
                break;
            default:
                break;
        }

        handleTooltips();
    }

    protected void handleTooltips() {
        if (placementSettingsHidden())
            return;

        for (ClickableWidget w : placementSettingWidgets)
            if (w instanceof IconButton button) {
                if (!button.getToolTip().isEmpty()) {
                    button.setToolTip(button.getToolTip().getFirst());
                    button.getToolTip().add(TooltipHelper.holdShift(Palette.BLUE, hasShiftDown()));
                }
            }

        if (hasShiftDown()) {
            fillToolTip(skipMissingButton, skipMissingIndicator, "skipMissing");
            fillToolTip(skipBlockEntitiesButton, skipBlockEntitiesIndicator, "skipBlockEntities");
            fillToolTip(replaceLevelButtons.get(0), replaceLevelIndicators.get(0), "dontReplaceSolid");
            fillToolTip(replaceLevelButtons.get(1), replaceLevelIndicators.get(1), "replaceWithSolid");
            fillToolTip(replaceLevelButtons.get(2), replaceLevelIndicators.get(2), "replaceWithAny");
            fillToolTip(replaceLevelButtons.get(3), replaceLevelIndicators.get(3), "replaceWithEmpty");
        }
    }

    private void fillToolTip(IconButton button, Indicator indicator, String tooltipKey) {
        if (!button.isHovered())
            return;
        boolean enabled = button.green;
        List<Text> tip = button.getToolTip();
        tip.add((enabled ? optionEnabled : optionDisabled).copyContentOnly().formatted(enabled ? Formatting.DARK_GREEN : Formatting.RED));
        tip.addAll(TooltipHelper.cutTextComponent(
            CreateLang.translateDirect("gui.schematicannon.option." + tooltipKey + ".description"),
            Palette.ALL_GRAY
        ));
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.getWidth());
        int invY = y + BG_TOP.getHeight() + BG_BOTTOM.getHeight() + 2;
        renderPlayerInventory(graphics, invX, invY);

        BG_TOP.render(graphics, x, y);
        BG_BOTTOM.render(graphics, x, y + BG_TOP.getHeight());
        AllGuiTextures.SCHEMATIC_TITLE.render(graphics, x, y - 2);

        SchematicannonBlockEntity be = handler.contentHolder;
        renderPrintingProgress(graphics, x, y, be.schematicProgress);
        float amount = be.remainingFuel / (float) be.getShotsPerGunpowder();
        renderFuelBar(graphics, x, y, amount);
        renderChecklistPrinterProgress(graphics, x, y, be.bookPrintingProgress);

        if (!be.inventory.getStack(0).isEmpty())
            renderBlueprintHighlight(graphics, x, y);

        graphics.drawText(textRenderer, title, x + (BG_TOP.getWidth() - 8 - textRenderer.getWidth(title)) / 2, y + 2, 0xFF505050, false);

        Text msg = CreateLang.translateDirect("schematicannon.status." + be.statusMsg);
        int stringWidth = textRenderer.getWidth(msg);

        if (be.missingItem != null) {
            stringWidth += 16;
            graphics.drawItem(be.missingItem, x + 128, y + 49);
        }

        graphics.drawText(textRenderer, msg, x + 103 - stringWidth / 2, y + 53, 0xFFDDEEFF, true);

        if ("schematicErrored".equals(be.statusMsg))
            graphics.drawText(
                textRenderer,
                CreateLang.translateDirect("schematicannon.status.schematicErroredCheckLogs"),
                x + 103 - stringWidth / 2,
                y + 65,
                0xFFDDEEFF,
                true
            );
    }

    protected void renderBlueprintHighlight(DrawContext graphics, int x, int y) {
        AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.render(graphics, x + 10, y + 60);
    }

    protected void renderPrintingProgress(DrawContext graphics, int x, int y, float progress) {
        progress = Math.min(progress, 1);
        AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
        graphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            sprite.location,
            x + 44,
            y + 64,
            sprite.getStartX(),
            sprite.getStartY(),
            (int) (sprite.getWidth() * progress),
            sprite.getHeight(),
            256,
            256
        );
    }

    protected void renderChecklistPrinterProgress(DrawContext graphics, int x, int y, float progress) {
        AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_CHECKLIST_PROGRESS;
        graphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            sprite.location,
            x + 154,
            y + 20,
            sprite.getStartX(),
            sprite.getStartY(),
            (int) (sprite.getWidth() * progress),
            sprite.getHeight(),
            256,
            256
        );
    }

    protected void renderFuelBar(DrawContext graphics, int x, int y, float amount) {
        AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
        if (handler.contentHolder.hasCreativeCrate) {
            AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.render(graphics, x + 36, y + 19);
            return;
        }
        graphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            sprite.location,
            x + 36,
            y + 19,
            sprite.getStartX(),
            sprite.getStartY(),
            (int) (sprite.getWidth() * amount),
            sprite.getHeight(),
            256,
            256
        );
    }

    @Override
    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        SchematicannonBlockEntity be = handler.contentHolder;

        int fuelX = x + 36, fuelY = y + 19;
        if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.getWidth() && mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.getHeight()) {
            List<Text> tooltip = getFuelLevelTooltip(be);
            graphics.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }

        if (focusedSlot != null && !focusedSlot.hasStack()) {
            String tooltipKey = switch (focusedSlot.id) {
                case 0 -> _slotSchematic;
                case 2 -> _slotListPrinter;
                case 4 -> _slotGunpowder;
                default -> null;
            };
            if (tooltipKey != null) {
                graphics.drawTooltip(
                    textRenderer,
                    TooltipHelper.cutTextComponent(CreateLang.translateDirect(tooltipKey), Palette.GRAY_AND_BLUE),
                    mouseX,
                    mouseY
                );
            }
        }

        if (be.missingItem != null) {
            int missingBlockX = x + 128, missingBlockY = y + 49;
            if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16 && mouseY <= missingBlockY + 16) {
                graphics.drawItemTooltip(textRenderer, be.missingItem, mouseX, mouseY);
            }
        }

        int paperX = x + 112, paperY = y + 19;
        if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16)
            graphics.drawTooltip(textRenderer, listPrinter, mouseX, mouseY);

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    protected List<Text> getFuelLevelTooltip(SchematicannonBlockEntity be) {
        int shotsLeft = be.remainingFuel;
        int shotsLeftWithItems = shotsLeft + be.inventory.getStack(4).getCount() * be.getShotsPerGunpowder();
        List<Text> tooltip = new ArrayList<>();

        if (be.hasCreativeCrate) {
            tooltip.add(CreateLang.translateDirect(_gunpowderLevel, "" + 100));
            tooltip.add(Text.literal("(").append(AllItems.CREATIVE_CRATE.getName()).append(")").formatted(Formatting.DARK_PURPLE));
            return tooltip;
        }

        int fillPercent = (int) ((be.remainingFuel / (float) be.getShotsPerGunpowder()) * 100);
        tooltip.add(CreateLang.translateDirect(_gunpowderLevel, fillPercent));
        tooltip.add(CreateLang.translateDirect(_shotsRemaining, Text.literal(Integer.toString(shotsLeft)).formatted(Formatting.BLUE))
            .formatted(Formatting.GRAY));
        if (shotsLeftWithItems != shotsLeft) {
            tooltip.add(CreateLang.translateDirect(
                _shotsRemainingWithBackup,
                Text.literal(Integer.toString(shotsLeftWithItems)).formatted(Formatting.BLUE)
            ).formatted(Formatting.GRAY));
        }

        return tooltip;
    }

    protected void sendOptionUpdate(Option option, boolean set) {
        client.player.networkHandler.sendPacket(new ConfigureSchematicannonPacket(option, set));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
