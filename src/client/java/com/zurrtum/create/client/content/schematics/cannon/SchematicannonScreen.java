package com.zurrtum.create.client.content.schematics.cannon;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.zurrtum.create.Create.LOGGER;

public class SchematicannonScreen extends AbstractSimiContainerScreen<SchematicannonMenu> {

    private static final AllGuiTextures BG_BOTTOM = AllGuiTextures.SCHEMATICANNON_BOTTOM;
    private static final AllGuiTextures BG_TOP = AllGuiTextures.SCHEMATICANNON_TOP;

    private final Component listPrinter = CreateLang.translateDirect("gui.schematicannon.listPrinter");
    private final String _gunpowderLevel = "gui.schematicannon.gunpowderLevel";
    private final String _shotsRemaining = "gui.schematicannon.shotsRemaining";
    private final String _showSettings = "gui.schematicannon.showOptions";
    private final String _shotsRemainingWithBackup = "gui.schematicannon.shotsRemainingWithBackup";

    private final String _slotGunpowder = "gui.schematicannon.slot.gunpowder";
    private final String _slotListPrinter = "gui.schematicannon.slot.listPrinter";
    private final String _slotSchematic = "gui.schematicannon.slot.schematic";

    private final Component optionEnabled = CreateLang.translateDirect("gui.schematicannon.optionEnabled");
    private final Component optionDisabled = CreateLang.translateDirect("gui.schematicannon.optionDisabled");

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

    protected List<AbstractWidget> placementSettingWidgets;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public SchematicannonScreen(SchematicannonMenu menu, Inventory inventory, Component title) {
        super(
            menu,
            inventory,
            title,
            BG_TOP.getWidth(),
            BG_TOP.getHeight() + BG_BOTTOM.getHeight() + 2 + AllGuiTextures.PLAYER_INVENTORY.getHeight()
        );
        placementSettingWidgets = new ArrayList<>();
    }

    public static SchematicannonScreen create(
        Minecraft mc,
        MenuType<SchematicannonBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        SchematicannonBlockEntity entity = getBlockEntity(mc, extraData);
        if (entity == null) {
            return null;
        }
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            ValueInput view = TagValueInput.create(logging, extraData.registryAccess(), extraData.readNbt());
            entity.readClient(view);
            return type.create(SchematicannonScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 0);
        super.init();

        // Play Pause Stop
        playButton = new IconButton(leftPos + 75, topPos + 85, AllIcons.I_PLAY);
        playButton.withCallback(() -> {
            sendOptionUpdate(Option.PLAY, true);
        });
        playIndicator = new Indicator(leftPos + 75, topPos + 79, CommonComponents.EMPTY);
        pauseButton = new IconButton(leftPos + 93, topPos + 85, AllIcons.I_PAUSE);
        pauseButton.withCallback(() -> {
            sendOptionUpdate(Option.PAUSE, true);
        });
        pauseIndicator = new Indicator(leftPos + 93, topPos + 79, CommonComponents.EMPTY);
        resetButton = new IconButton(leftPos + 111, topPos + 85, AllIcons.I_STOP);
        resetButton.withCallback(() -> {
            sendOptionUpdate(Option.STOP, true);
        });
        resetIndicator = new Indicator(leftPos + 111, topPos + 79, CommonComponents.EMPTY);
        resetIndicator.state = State.RED;
        addRenderableWidgets(playButton, playIndicator, pauseButton, pauseIndicator, resetButton, resetIndicator);

        confirmButton = new IconButton(leftPos + 180, topPos + 111, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            minecraft.player.closeContainer();
        });
        addRenderableWidget(confirmButton);
        showSettingsButton = new IconButton(leftPos + 8, topPos + 111, AllIcons.I_PLACEMENT_SETTINGS);
        showSettingsButton.withCallback(() -> {
            showSettingsIndicator.state = placementSettingsHidden() ? State.GREEN : State.OFF;
            initPlacementSettings();
        });
        showSettingsButton.setToolTip(CreateLang.translateDirect(_showSettings));
        addRenderableWidget(showSettingsButton);
        showSettingsIndicator = new Indicator(leftPos + 9, topPos + 111, CommonComponents.EMPTY);
        //		addRenderableWidget(showSettingsIndicator);

        extraAreas = ImmutableList.of(new Rect2i(leftPos + imageWidth, topPos + BG_TOP.getHeight() + BG_BOTTOM.getHeight() - 62, 84, 92));

        renderedItem = new ElementWidget(leftPos + imageWidth - 14, topPos + BG_TOP.getHeight() + BG_BOTTOM.getHeight() - 62).showingElement(
            GuiGameElement.of(AllItems.SCHEMATICANNON.getDefaultInstance()).scale(5).padding(28));
        addRenderableWidget(renderedItem);

        tick();
    }

    @Override
    public void onClose() {
        super.onClose();
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
        List<Component> toolTips = ImmutableList.of(
            CreateLang.translateDirect("gui.schematicannon.option.dontReplaceSolid"),
            CreateLang.translateDirect("gui.schematicannon.option.replaceWithSolid"),
            CreateLang.translateDirect("gui.schematicannon.option.replaceWithAny"),
            CreateLang.translateDirect("gui.schematicannon.option.replaceWithEmpty")
        );

        for (int i = 0; i < 4; i++) {
            replaceLevelIndicators.add(new Indicator(leftPos + 33 + i * 18, topPos + 111, CommonComponents.EMPTY));
            IconButton replaceLevelButton = new IconButton(leftPos + 33 + i * 18, topPos + 111, icons.get(i));
            int replaceMode = i;
            replaceLevelButton.withCallback(() -> {
                if (menu.contentHolder.replaceMode != replaceMode)
                    sendOptionUpdate(Option.values()[replaceMode], true);
            });
            replaceLevelButton.setToolTip(toolTips.get(i));
            replaceLevelButtons.add(replaceLevelButton);
        }
        placementSettingWidgets.addAll(replaceLevelButtons);
        //		placementSettingWidgets.addAll(replaceLevelIndicators);

        // Other Settings
        skipMissingButton = new IconButton(leftPos + 111, topPos + 111, AllIcons.I_SKIP_MISSING);
        skipMissingButton.withCallback(() -> {
            sendOptionUpdate(Option.SKIP_MISSING, !menu.contentHolder.skipMissing);
        });
        skipMissingButton.setToolTip(CreateLang.translateDirect("gui.schematicannon.option.skipMissing"));
        skipMissingIndicator = new Indicator(leftPos + 111, topPos + 111, CommonComponents.EMPTY);
        Collections.addAll(placementSettingWidgets, skipMissingButton);

        skipBlockEntitiesButton = new IconButton(leftPos + 135, topPos + 111, AllIcons.I_SKIP_BLOCK_ENTITIES);
        skipBlockEntitiesButton.withCallback(() -> {
            sendOptionUpdate(Option.SKIP_BLOCK_ENTITIES, !menu.contentHolder.replaceBlockEntities);
        });
        skipBlockEntitiesButton.setToolTip(CreateLang.translateDirect("gui.schematicannon.option.skipBlockEntities"));
        skipBlockEntitiesIndicator = new Indicator(leftPos + 129, topPos + 111, CommonComponents.EMPTY);
        Collections.addAll(placementSettingWidgets, skipBlockEntitiesButton);

        addRenderableWidgets(placementSettingWidgets);
    }

    protected boolean placementSettingsHidden() {
        return showSettingsIndicator.state == State.OFF;
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        SchematicannonBlockEntity be = menu.contentHolder;

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

        boolean hasShiftDown = AllKeys.hasShiftDown();
        for (AbstractWidget w : placementSettingWidgets)
            if (w instanceof IconButton button) {
                if (!button.getToolTip().isEmpty()) {
                    button.setToolTip(button.getToolTip().getFirst());
                    button.getToolTip().add(TooltipHelper.holdShift(Palette.BLUE, hasShiftDown));
                }
            }

        if (hasShiftDown) {
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
        List<Component> tip = button.getToolTip();
        tip.add((enabled ? optionEnabled : optionDisabled).plainCopy().withStyle(enabled ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
        tip.addAll(TooltipHelper.cutTextComponent(
            CreateLang.translateDirect("gui.schematicannon.option." + tooltipKey + ".description"),
            Palette.ALL_GRAY
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.getWidth());
        int invY = topPos + BG_TOP.getHeight() + BG_BOTTOM.getHeight() + 2;
        renderPlayerInventory(graphics, invX, invY);

        BG_TOP.render(graphics, leftPos, topPos);
        BG_BOTTOM.render(graphics, leftPos, topPos + BG_TOP.getHeight());
        AllGuiTextures.SCHEMATIC_TITLE.render(graphics, leftPos, topPos - 2);

        SchematicannonBlockEntity be = menu.contentHolder;
        renderPrintingProgress(graphics, leftPos, topPos, be.schematicProgress);
        float amount = be.remainingFuel / (float) be.getShotsPerGunpowder();
        renderFuelBar(graphics, leftPos, topPos, amount);
        renderChecklistPrinterProgress(graphics, leftPos, topPos, be.bookPrintingProgress);

        if (!be.inventory.getItem(0).isEmpty())
            renderBlueprintHighlight(graphics, leftPos, topPos);

        graphics.drawString(font, title, leftPos + (imageWidth - 8 - font.width(title)) / 2, topPos + 2, 0xFF505050, false);

        Component msg = CreateLang.translateDirect("schematicannon.status." + be.statusMsg);
        int stringWidth = font.width(msg);

        if (be.missingItem != null) {
            stringWidth += 16;
            graphics.renderItem(be.missingItem, leftPos + 128, topPos + 49);
        }

        graphics.drawString(font, msg, leftPos + 103 - stringWidth / 2, topPos + 53, 0xFFDDEEFF, true);

        if ("schematicErrored".equals(be.statusMsg))
            graphics.drawString(
                font,
                CreateLang.translateDirect("schematicannon.status.schematicErroredCheckLogs"),
                leftPos + 103 - stringWidth / 2,
                topPos + 65,
                0xFFDDEEFF,
                true
            );
    }

    protected void renderBlueprintHighlight(GuiGraphics graphics, int x, int y) {
        AllGuiTextures.SCHEMATICANNON_HIGHLIGHT.render(graphics, x + 10, y + 60);
    }

    protected void renderPrintingProgress(GuiGraphics graphics, int x, int y, float progress) {
        progress = Math.min(progress, 1);
        AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_PROGRESS;
        graphics.blit(
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

    protected void renderChecklistPrinterProgress(GuiGraphics graphics, int x, int y, float progress) {
        AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_CHECKLIST_PROGRESS;
        graphics.blit(
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

    protected void renderFuelBar(GuiGraphics graphics, int x, int y, float amount) {
        AllGuiTextures sprite = AllGuiTextures.SCHEMATICANNON_FUEL;
        if (menu.contentHolder.hasCreativeCrate) {
            AllGuiTextures.SCHEMATICANNON_FUEL_CREATIVE.render(graphics, x + 36, y + 19);
            return;
        }
        graphics.blit(
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
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        SchematicannonBlockEntity be = menu.contentHolder;

        int fuelX = leftPos + 36, fuelY = topPos + 19;
        if (mouseX >= fuelX && mouseY >= fuelY && mouseX <= fuelX + AllGuiTextures.SCHEMATICANNON_FUEL.getWidth() && mouseY <= fuelY + AllGuiTextures.SCHEMATICANNON_FUEL.getHeight()) {
            List<Component> tooltip = getFuelLevelTooltip(be);
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }

        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            String tooltipKey = switch (hoveredSlot.index) {
                case 0 -> _slotSchematic;
                case 2 -> _slotListPrinter;
                case 4 -> _slotGunpowder;
                default -> null;
            };
            if (tooltipKey != null) {
                graphics.setComponentTooltipForNextFrame(
                    font,
                    TooltipHelper.cutTextComponent(CreateLang.translateDirect(tooltipKey), Palette.GRAY_AND_BLUE),
                    mouseX,
                    mouseY
                );
            }
        }

        if (be.missingItem != null) {
            int missingBlockX = leftPos + 128, missingBlockY = topPos + 49;
            if (mouseX >= missingBlockX && mouseY >= missingBlockY && mouseX <= missingBlockX + 16 && mouseY <= missingBlockY + 16) {
                graphics.setTooltipForNextFrame(font, be.missingItem, mouseX, mouseY);
            }
        }

        int paperX = leftPos + 112, paperY = topPos + 19;
        if (mouseX >= paperX && mouseY >= paperY && mouseX <= paperX + 16 && mouseY <= paperY + 16)
            graphics.setTooltipForNextFrame(font, listPrinter, mouseX, mouseY);

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    protected List<Component> getFuelLevelTooltip(SchematicannonBlockEntity be) {
        int shotsLeft = be.remainingFuel;
        int shotsLeftWithItems = shotsLeft + be.inventory.getItem(4).getCount() * be.getShotsPerGunpowder();
        List<Component> tooltip = new ArrayList<>();

        if (be.hasCreativeCrate) {
            tooltip.add(CreateLang.translateDirect(_gunpowderLevel, "" + 100));
            tooltip.add(Component.literal("(").append(AllItems.CREATIVE_CRATE.getName()).append(")").withStyle(ChatFormatting.DARK_PURPLE));
            return tooltip;
        }

        int fillPercent = (int) ((be.remainingFuel / (float) be.getShotsPerGunpowder()) * 100);
        tooltip.add(CreateLang.translateDirect(_gunpowderLevel, fillPercent));
        tooltip.add(CreateLang.translateDirect(_shotsRemaining, Component.literal(Integer.toString(shotsLeft)).withStyle(ChatFormatting.BLUE))
            .withStyle(ChatFormatting.GRAY));
        if (shotsLeftWithItems != shotsLeft) {
            tooltip.add(CreateLang.translateDirect(
                _shotsRemainingWithBackup,
                Component.literal(Integer.toString(shotsLeftWithItems)).withStyle(ChatFormatting.BLUE)
            ).withStyle(ChatFormatting.GRAY));
        }

        return tooltip;
    }

    protected void sendOptionUpdate(Option option, boolean set) {
        minecraft.player.connection.send(new ConfigureSchematicannonPacket(option, set));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
