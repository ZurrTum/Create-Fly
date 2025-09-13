package com.zurrtum.create.client.content.equipment.zapper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.zapper.ConfigureZapperPacket;
import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public abstract class ZapperScreen extends AbstractSimiScreen {

    protected final Text patternSection = CreateLang.translateDirect("gui.terrainzapper.patternSection");

    protected AllGuiTextures background;
    protected ItemStack zapper;
    protected Hand hand;

    protected float animationProgress;

    protected ElementWidget renderedItem;
    protected ElementWidget renderedBlock;
    protected Text title;
    protected List<IconButton> patternButtons = new ArrayList<>(6);
    private IconButton confirmButton;
    protected int brightColor;
    protected int fontColor;

    protected PlacementPatterns currentPattern;

    public ZapperScreen(AllGuiTextures background, ItemStack zapper, Hand hand) {
        this.background = background;
        this.zapper = zapper;
        this.hand = hand;
        title = ScreenTexts.EMPTY;
        brightColor = 0xFEFEFE;
        fontColor = AllGuiTextures.FONT_COLOR;

        currentPattern = zapper.getOrDefault(AllDataComponents.PLACEMENT_PATTERN, PlacementPatterns.Solid);
    }

    public AllIcons getIcon(PlacementPatterns pattern) {
        return switch (pattern) {
            case Solid -> AllIcons.I_PATTERN_SOLID;
            case Checkered -> AllIcons.I_PATTERN_CHECKERED;
            case InverseCheckered -> AllIcons.I_PATTERN_CHECKERED_INVERSED;
            case Chance25 -> AllIcons.I_PATTERN_CHANCE_25;
            case Chance50 -> AllIcons.I_PATTERN_CHANCE_50;
            case Chance75 -> AllIcons.I_PATTERN_CHANCE_75;
        };
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        setWindowOffset(-10, 0);
        super.init();

        animationProgress = 0;

        int x = guiLeft;
        int y = guiTop;

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);

        patternButtons.clear();
        for (int row = 0; row <= 1; row++) {
            for (int col = 0; col <= 2; col++) {
                int id = patternButtons.size();
                PlacementPatterns pattern = PlacementPatterns.values()[id];
                IconButton patternButton = new IconButton(x + background.getWidth() - 76 + col * 18, y + 21 + row * 18, getIcon(pattern));
                patternButton.withCallback(() -> {
                    patternButtons.forEach(b -> b.green = false);
                    patternButton.green = true;
                    currentPattern = pattern;
                });
                patternButton.setToolTip(CreateLang.translateDirect("gui.terrainzapper.pattern." + pattern.translationKey));
                patternButtons.add(patternButton);
            }
        }

        patternButtons.get(currentPattern.ordinal()).green = true;

        addRenderableWidgets(patternButtons);

        renderedItem = new ElementWidget(x + background.getWidth(), y + background.getHeight() - 48).showingElement(GuiGameElement.of(zapper)
            .scale(4));
        addDrawableChild(renderedItem);
        renderedBlock = new ElementWidget(x + 17, y + 24).showingElement(GuiGameElement.of(zapper.getOrDefault(
            AllDataComponents.SHAPER_BLOCK_USED,
            Blocks.AIR.getDefaultState()
        )).scale(1.25f).rotate(-25f, -45f, 0).padding(10));
        addDrawableChild(renderedBlock);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
        renderedBlock.getRenderElement().clear();
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        drawOnBackground(graphics, x, y);
    }

    protected void drawOnBackground(DrawContext graphics, int x, int y) {
        graphics.drawText(textRenderer, title, x + (background.getWidth() - textRenderer.getWidth(title)) / 2, y + 4, 0xFF54214F, false);
    }

    @Override
    public void tick() {
        super.tick();
        animationProgress += 5;
    }

    @Override
    public void removed() {
        ConfigureZapperPacket packet = getConfigurationPacket();
        packet.configureZapper(zapper);
        client.player.networkHandler.sendPacket(packet);
    }

    protected abstract ConfigureZapperPacket getConfigurationPacket();

}
