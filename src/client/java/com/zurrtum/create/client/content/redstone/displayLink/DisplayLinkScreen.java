package com.zurrtum.create.client.content.redstone.displayLink;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.api.behaviour.display.DisplaySourceRender;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.ModularGuiLine;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import com.zurrtum.create.client.ponder.foundation.ui.PonderTagScreen;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.infrastructure.packet.c2s.DisplayLinkConfigurationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DisplayLinkScreen extends AbstractSimiScreen {

    private static final ItemStack FALLBACK = new ItemStack(Items.BARRIER);

    private AllGuiTextures background;
    private DisplayLinkBlockEntity blockEntity;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    @Nullable BlockState sourceState;
    @Nullable BlockState targetState;
    List<DisplaySource> sources;
    @Nullable DisplayTarget target;

    @Nullable ScrollInput sourceTypeSelector;
    Label sourceTypeLabel;
    @Nullable ScrollInput targetLineSelector;
    Label targetLineLabel;
    AbstractSimiWidget sourceWidget;
    AbstractSimiWidget targetWidget;

    Couple<ModularGuiLine> configWidgets;

    public DisplayLinkScreen(DisplayLinkBlockEntity be) {
        this.background = AllGuiTextures.DATA_GATHERER;
        this.blockEntity = be;
        sources = Collections.emptyList();
        configWidgets = Couple.create(ModularGuiLine::new);
        target = null;
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        super.init();
        clearWidgets();

        int x = guiLeft;
        int y = guiTop;


        initGathererOptions();

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        renderedItem = new ElementWidget(
            x + background.getWidth() - 11,
            y + background.getHeight() - 55
        ).showingElement(GuiGameElement.of(AllItems.DISPLAY_LINK.getDefaultInstance()).scale(4).rotate(50, 207, -14).padding(17));
        addRenderableWidget(renderedItem);
    }

    @Override
    public void tick() {
        super.tick();
        if (sourceState != null && sourceState.getBlock() != minecraft.level.getBlockState(blockEntity.getSourcePosition())
            .getBlock() || targetState != null && targetState.getBlock() != minecraft.level.getBlockState(blockEntity.getTargetPosition()).getBlock())
            initGathererOptions();
    }

    private void initGathererOptions() {
        ClientLevel level = minecraft.level;
        sourceState = level.getBlockState(blockEntity.getSourcePosition());
        targetState = level.getBlockState(blockEntity.getTargetPosition());

        ItemStack asItem;
        int x = guiLeft;
        int y = guiTop;

        Block sourceBlock = sourceState.getBlock();
        Block targetBlock = targetState.getBlock();

        asItem = sourceState.getCloneItemStack(level, blockEntity.getSourcePosition(), true);
        ItemStack sourceIcon = asItem == null || asItem.isEmpty() ? FALLBACK : asItem;
        asItem = targetState.getCloneItemStack(level, blockEntity.getTargetPosition(), true);
        ItemStack targetIcon = asItem == null || asItem.isEmpty() ? FALLBACK : asItem;

        sources = DisplaySource.getAll(level, blockEntity.getSourcePosition());
        target = DisplayTarget.get(level, blockEntity.getTargetPosition());

        removeWidget(targetLineSelector);
        removeWidget(targetLineLabel);
        removeWidget(sourceTypeSelector);
        removeWidget(sourceTypeLabel);
        removeWidget(sourceWidget);
        removeWidget(targetWidget);

        configWidgets.forEach(s -> s.forEach(this::removeWidget));

        targetLineSelector = null;
        sourceTypeSelector = null;

        if (target != null) {
            DisplayTargetStats stats = target.provideStats(new DisplayLinkContext(level, blockEntity));
            int rows = stats.maxRows();
            int startIndex = Math.min(blockEntity.targetLine, rows);

            targetLineLabel = new Label(x + 65, y + 109, CommonComponents.EMPTY).withShadow();
            targetLineLabel.text = target.getLineOptionText(startIndex);

            if (rows > 1) {
                targetLineSelector = new ScrollInput(x + 61, y + 105, 135, 16).withRange(0, rows)
                    .titled(CreateLang.translateDirect("display_link.display_on")).inverted()
                    .calling(i -> targetLineLabel.text = target.getLineOptionText(i)).setState(startIndex);
                addRenderableWidget(targetLineSelector);
            }

            addRenderableWidget(targetLineLabel);
        }

        sourceWidget = new ElementWidget(x + 37, y + 26).showingElement(GuiGameElement.of(sourceIcon)).withCallback((mX, mY) -> {
            ScreenOpener.open(new PonderTagScreen(AllCreatePonderTags.DISPLAY_SOURCES));
        });

        sourceWidget.getToolTip().addAll(List.of(
            CreateLang.translateDirect("display_link.reading_from"),
            sourceState.getBlock().getName().withStyle(s -> s.withColor(sources.isEmpty() ? 0xF68989 : 0xF2C16D)),
            CreateLang.translateDirect("display_link.attached_side"),
            CreateLang.translateDirect("display_link.view_compatible").withStyle(ChatFormatting.GRAY)
        ));

        addRenderableWidget(sourceWidget);

        targetWidget = new ElementWidget(x + 37, y + 105).showingElement(GuiGameElement.of(targetIcon)).withCallback((mX, mY) -> {
            ScreenOpener.open(new PonderTagScreen(AllCreatePonderTags.DISPLAY_TARGETS));
        });

        targetWidget.getToolTip().addAll(List.of(
            CreateLang.translateDirect("display_link.writing_to"),
            targetState.getBlock().getName().withStyle(s -> s.withColor(target == null ? 0xF68989 : 0xF2C16D)),
            CreateLang.translateDirect("display_link.targeted_location"),
            CreateLang.translateDirect("display_link.view_compatible").withStyle(ChatFormatting.GRAY)
        ));

        addRenderableWidget(targetWidget);

        if (!sources.isEmpty()) {
            int startIndex = Math.max(sources.indexOf(blockEntity.activeSource), 0);

            sourceTypeLabel = new Label(x + 65, y + 30, CommonComponents.EMPTY).withShadow();
            sourceTypeLabel.text = sources.get(startIndex).getName();

            if (sources.size() > 1) {
                List<Component> options = sources.stream().map(DisplaySource::getName).toList();
                sourceTypeSelector = new SelectionScrollInput(x + 61, y + 26, 135, 16).forOptions(options).writingTo(sourceTypeLabel)
                    .titled(CreateLang.translateDirect("display_link.information_type")).calling(this::initGathererSourceSubOptions)
                    .setState(startIndex);
                sourceTypeSelector.onChanged();
                addRenderableWidget(sourceTypeSelector);
            } else
                initGathererSourceSubOptions(0);

            addRenderableWidget(sourceTypeLabel);
        }
    }

    private void initGathererSourceSubOptions(int i) {
        DisplaySource source = sources.get(i);
        source.populateData(new DisplayLinkContext(blockEntity.getLevel(), blockEntity));

        if (targetLineSelector != null)
            targetLineSelector.titled(source instanceof SingleLineDisplaySource ? CreateLang.translateDirect("display_link.display_on") : CreateLang.translateDirect(
                "display_link.display_on_multiline"));

        configWidgets.forEach(s -> {
            s.forEach(this::removeWidget);
            s.clear();
        });

        DisplaySourceRender render = source.getAttachRender();
        if (render != null) {
            DisplayLinkContext context = new DisplayLinkContext(minecraft.level, blockEntity);
            configWidgets.forEachWithContext((s, first) -> {
                render.initConfigurationWidgets(source, context, new ModularGuiLineBuilder(font, s, guiLeft + 60, guiTop + (first ? 51 : 72)), first);
            });
        }
        configWidgets.forEach(s -> s.loadValues(blockEntity.getSourceConfig(), this::addRenderableWidget, this::addRenderableOnly));
    }

    @Override
    public void onClose() {
        super.onClose();
        CompoundTag sourceData = new CompoundTag();

        if (!sources.isEmpty()) {
            DisplaySource source = sources.get(sourceTypeSelector == null ? 0 : sourceTypeSelector.getState());
            Identifier id = CreateRegistries.DISPLAY_SOURCE.getKey(source);
            if (id != null) {
                sourceData.putString("Id", id.toString());
            }
            configWidgets.forEach(s -> s.saveValues(sourceData));
        }

        minecraft.player.connection.send(new DisplayLinkConfigurationPacket(
            blockEntity.getBlockPos(),
            sourceData,
            targetLineSelector == null ? 0 : targetLineSelector.getState()
        ));

        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        MutableComponent header = CreateLang.translateDirect("display_link.title");
        graphics.drawString(font, header, x + background.getWidth() / 2 - font.width(header) / 2, y + 4, 0xFF592424, false);

        if (sources.isEmpty())
            graphics.drawString(font, CreateLang.translateDirect("display_link.no_source"), x + 65, y + 30, 0xFFD3D3D3, true);
        if (target == null)
            graphics.drawString(font, CreateLang.translateDirect("display_link.no_target"), x + 65, y + 109, 0xFFD3D3D3, true);

        Matrix3x2fStack ms = graphics.pose();
        ms.pushMatrix();
        ms.translate(0, guiTop + 46);
        configWidgets.getFirst().renderWidgetBG(guiLeft, graphics);
        ms.translate(0, 21);
        configWidgets.getSecond().renderWidgetBG(guiLeft, graphics);
        ms.popMatrix();
    }

    @Override
    protected void removeWidget(@Nullable GuiEventListener p_169412_) {
        if (p_169412_ != null)
            super.removeWidget(p_169412_);
    }
}
