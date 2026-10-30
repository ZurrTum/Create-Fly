package com.zurrtum.create.client.content.equipment.goggles;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.infrastructure.config.CClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoggleConfigScreen extends AbstractSimiScreen {

    private int offsetX;
    private int offsetY;
    private final List<Component> tooltip;

    public GoggleConfigScreen() {
        Component componentSpacing = Component.literal("    ");
        tooltip = new ArrayList<>();
        tooltip.add(componentSpacing.plainCopy().append(CreateLang.translateDirect("gui.config.overlay1")));
        tooltip.add(componentSpacing.plainCopy()
            .append(CreateLang.translateDirect("gui.config.overlay2").withStyle(ChatFormatting.GRAY)));
        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(componentSpacing.plainCopy().append(CreateLang.translateDirect("gui.config.overlay3")));
        tooltip.add(componentSpacing.plainCopy().append(CreateLang.translateDirect("gui.config.overlay4")));
        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(componentSpacing.plainCopy()
            .append(CreateLang.translateDirect("gui.config.overlay5").withStyle(ChatFormatting.GRAY)));
        tooltip.add(componentSpacing.plainCopy()
            .append(CreateLang.translateDirect("gui.config.overlay6").withStyle(ChatFormatting.GRAY)));
        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(componentSpacing.plainCopy().append(CreateLang.translateDirect("gui.config.overlay7")));
        tooltip.add(componentSpacing.plainCopy().append(CreateLang.translateDirect("gui.config.overlay8")));
    }

    @Override
    protected void init() {
        width = minecraft.getWindow().getGuiScaledWidth();
        height = minecraft.getWindow().getGuiScaledHeight();

        CClient client = AllConfigs.client();
        offsetX = client.overlayOffsetX.get();
        offsetY = client.overlayOffsetY.get();
    }

    @Override
    public void removed() {
        CClient client = AllConfigs.client();
        client.overlayOffsetX.set(offsetX);
        client.overlayOffsetY.set(offsetY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        updateOffset(event.x(), event.y());
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        updateOffset(event.x(), event.y());
        return true;
    }

    private void updateOffset(double windowX, double windowY) {
        offsetX = (int) (windowX - width / 2);
        offsetY = (int) (windowY - height / 2);

        int titleLinesCount = 1;
        int tooltipTextWidth = 0;
        for (FormattedText textLine : tooltip) {
            int textLineWidth = minecraft.font.width(textLine);
            if (textLineWidth > tooltipTextWidth) {
                tooltipTextWidth = textLineWidth;
            }
        }
        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += (tooltip.size() - 1) * 10;
            if (tooltip.size() > titleLinesCount) {
                tooltipHeight += 2; // gap between title lines and next lines
            }
        }

        offsetX = Mth.clamp(offsetX, -(width / 2) - 5, width / 2 - tooltipTextWidth - 20);
        offsetY = Mth.clamp(offsetY, -(height / 2) + 17, height / 2 - tooltipHeight + 5);
    }

    @Override
    protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int posX = width / 2 + offsetX;
        int posY = height / 2 + offsetY;
        graphics.tooltip(
            font,
            tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create)
                .collect(Collectors.toList()),
            posX,
            posY,
            DefaultTooltipPositioner.INSTANCE,
            null,
            false
        );
        ItemStack item = AllItems.GOGGLES.getDefaultInstance();
        graphics.item(item, posX + 10, posY - 16);
    }
}
