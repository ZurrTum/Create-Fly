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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.Collections;
import java.util.List;

public class DisplayLinkScreen extends AbstractSimiScreen {

    private static final ItemStack FALLBACK = new ItemStack(Items.BARRIER);

    private AllGuiTextures background;
    private DisplayLinkBlockEntity blockEntity;
    private IconButton confirmButton;
    private ElementWidget renderedItem;

    BlockState sourceState;
    BlockState targetState;
    List<DisplaySource> sources;
    DisplayTarget target;

    ScrollInput sourceTypeSelector;
    Label sourceTypeLabel;
    ScrollInput targetLineSelector;
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
        clearChildren();

        int x = guiLeft;
        int y = guiTop;


        initGathererOptions();

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);

        renderedItem = new ElementWidget(
            x + background.getWidth() - 11,
            y + background.getHeight() - 55
        ).showingElement(GuiGameElement.of(AllItems.DISPLAY_LINK.getDefaultStack()).scale(4).rotate(50, 207, -14).padding(17));
        addDrawableChild(renderedItem);
    }

    @Override
    public void tick() {
        super.tick();
        if (sourceState != null && sourceState.getBlock() != client.world.getBlockState(blockEntity.getSourcePosition())
            .getBlock() || targetState != null && targetState.getBlock() != client.world.getBlockState(blockEntity.getTargetPosition()).getBlock())
            initGathererOptions();
    }

    private void initGathererOptions() {
        ClientWorld level = client.world;
        sourceState = level.getBlockState(blockEntity.getSourcePosition());
        targetState = level.getBlockState(blockEntity.getTargetPosition());

        ItemStack asItem;
        int x = guiLeft;
        int y = guiTop;

        Block sourceBlock = sourceState.getBlock();
        Block targetBlock = targetState.getBlock();

        asItem = sourceState.getPickStack(level, blockEntity.getSourcePosition(), true);
        ItemStack sourceIcon = asItem == null || asItem.isEmpty() ? FALLBACK : asItem;
        asItem = targetState.getPickStack(level, blockEntity.getTargetPosition(), true);
        ItemStack targetIcon = asItem == null || asItem.isEmpty() ? FALLBACK : asItem;

        sources = DisplaySource.getAll(level, blockEntity.getSourcePosition());
        target = DisplayTarget.get(level, blockEntity.getTargetPosition());

        remove(targetLineSelector);
        remove(targetLineLabel);
        remove(sourceTypeSelector);
        remove(sourceTypeLabel);
        remove(sourceWidget);
        remove(targetWidget);

        configWidgets.forEach(s -> s.forEach(this::remove));

        targetLineSelector = null;
        sourceTypeSelector = null;

        if (target != null) {
            DisplayTargetStats stats = target.provideStats(new DisplayLinkContext(level, blockEntity));
            int rows = stats.maxRows();
            int startIndex = Math.min(blockEntity.targetLine, rows);

            targetLineLabel = new Label(x + 65, y + 109, ScreenTexts.EMPTY).withShadow();
            targetLineLabel.text = target.getLineOptionText(startIndex);

            if (rows > 1) {
                targetLineSelector = new ScrollInput(x + 61, y + 105, 135, 16).withRange(0, rows)
                    .titled(CreateLang.translateDirect("display_link.display_on")).inverted()
                    .calling(i -> targetLineLabel.text = target.getLineOptionText(i)).setState(startIndex);
                addDrawableChild(targetLineSelector);
            }

            addDrawableChild(targetLineLabel);
        }

        sourceWidget = new ElementWidget(x + 37, y + 26).showingElement(GuiGameElement.of(sourceIcon)).withCallback((mX, mY) -> {
            ScreenOpener.open(new PonderTagScreen(AllCreatePonderTags.DISPLAY_SOURCES));
        });

        sourceWidget.getToolTip().addAll(List.of(
            CreateLang.translateDirect("display_link.reading_from"),
            sourceState.getBlock().getName().styled(s -> s.withColor(sources.isEmpty() ? 0xF68989 : 0xF2C16D)),
            CreateLang.translateDirect("display_link.attached_side"),
            CreateLang.translateDirect("display_link.view_compatible").formatted(Formatting.GRAY)
        ));

        addDrawableChild(sourceWidget);

        targetWidget = new ElementWidget(x + 37, y + 105).showingElement(GuiGameElement.of(targetIcon)).withCallback((mX, mY) -> {
            ScreenOpener.open(new PonderTagScreen(AllCreatePonderTags.DISPLAY_TARGETS));
        });

        targetWidget.getToolTip().addAll(List.of(
            CreateLang.translateDirect("display_link.writing_to"),
            targetState.getBlock().getName().styled(s -> s.withColor(target == null ? 0xF68989 : 0xF2C16D)),
            CreateLang.translateDirect("display_link.targeted_location"),
            CreateLang.translateDirect("display_link.view_compatible").formatted(Formatting.GRAY)
        ));

        addDrawableChild(targetWidget);

        if (!sources.isEmpty()) {
            int startIndex = Math.max(sources.indexOf(blockEntity.activeSource), 0);

            sourceTypeLabel = new Label(x + 65, y + 30, ScreenTexts.EMPTY).withShadow();
            sourceTypeLabel.text = sources.get(startIndex).getName();

            if (sources.size() > 1) {
                List<Text> options = sources.stream().map(DisplaySource::getName).toList();
                sourceTypeSelector = new SelectionScrollInput(x + 61, y + 26, 135, 16).forOptions(options).writingTo(sourceTypeLabel)
                    .titled(CreateLang.translateDirect("display_link.information_type")).calling(this::initGathererSourceSubOptions)
                    .setState(startIndex);
                sourceTypeSelector.onChanged();
                addDrawableChild(sourceTypeSelector);
            } else
                initGathererSourceSubOptions(0);

            addDrawableChild(sourceTypeLabel);
        }
    }

    private void initGathererSourceSubOptions(int i) {
        DisplaySource source = sources.get(i);
        source.populateData(new DisplayLinkContext(blockEntity.getWorld(), blockEntity));

        if (targetLineSelector != null)
            targetLineSelector.titled(source instanceof SingleLineDisplaySource ? CreateLang.translateDirect("display_link.display_on") : CreateLang.translateDirect(
                "display_link.display_on_multiline"));

        configWidgets.forEach(s -> {
            s.forEach(this::remove);
            s.clear();
        });

        DisplaySourceRender render = source.getAttachRender();
        if (render != null) {
            DisplayLinkContext context = new DisplayLinkContext(client.world, blockEntity);
            configWidgets.forEachWithContext((s, first) -> {
                render.initConfigurationWidgets(
                    source,
                    context,
                    new ModularGuiLineBuilder(textRenderer, s, guiLeft + 60, guiTop + (first ? 51 : 72)),
                    first
                );
            });
        }
        configWidgets.forEach(s -> s.loadValues(blockEntity.getSourceConfig(), this::addDrawableChild, this::addDrawable));
    }

    @Override
    public void close() {
        super.close();
        NbtCompound sourceData = new NbtCompound();

        if (!sources.isEmpty()) {
            DisplaySource source = sources.get(sourceTypeSelector == null ? 0 : sourceTypeSelector.getState());
            Identifier id = CreateRegistries.DISPLAY_SOURCE.getId(source);
            if (id != null) {
                sourceData.putString("Id", id.toString());
            }
            configWidgets.forEach(s -> s.saveValues(sourceData));
        }

        client.player.networkHandler.sendPacket(new DisplayLinkConfigurationPacket(
            blockEntity.getPos(),
            sourceData,
            targetLineSelector == null ? 0 : targetLineSelector.getState()
        ));

        renderedItem.getRenderElement().clear();
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        MutableText header = CreateLang.translateDirect("display_link.title");
        graphics.drawText(textRenderer, header, x + background.getWidth() / 2 - textRenderer.getWidth(header) / 2, y + 4, 0xFF592424, false);

        if (sources.isEmpty())
            graphics.drawText(textRenderer, CreateLang.translateDirect("display_link.no_source"), x + 65, y + 30, 0xFFD3D3D3, true);
        if (target == null)
            graphics.drawText(textRenderer, CreateLang.translateDirect("display_link.no_target"), x + 65, y + 109, 0xFFD3D3D3, true);

        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();
        ms.translate(0, guiTop + 46);
        configWidgets.getFirst().renderWidgetBG(guiLeft, graphics);
        ms.translate(0, 21);
        configWidgets.getSecond().renderWidgetBG(guiLeft, graphics);
        ms.popMatrix();
    }

    @Override
    protected void remove(Element p_169412_) {
        if (p_169412_ != null)
            super.remove(p_169412_);
    }
}
