package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FactoryGaugeDisplaySource extends ValueListDisplaySource {

    @Override
    protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
        List<FactoryPanelPosition> panels = context.blockEntity().factoryPanelSupport.getLinkedPanels();
        if (panels.isEmpty())
            return Stream.empty();
        return panels.stream().map(fpp -> createEntry(context.level(), fpp))
            //			.sorted(IntAttached.comparator())
            .filter(Objects::nonNull).limit(maxRows);
    }

    @Nullable
    public IntAttached<MutableComponent> createEntry(Level level, FactoryPanelPosition pos) {
        ServerFactoryPanelBehaviour panel = ServerFactoryPanelBehaviour.at(level, pos);
        if (panel == null)
            return null;

        ItemStack filter = panel.getFilter();

        int demand = panel.getAmount() * (panel.upTo ? 1 : filter.getMaxStackSize());
        String s = " ";

        if (demand != 0) {
            int promised = panel.getPromised();
            if (panel.satisfied)
                s = "✔";
            else if (promised != 0)
                s = "↑";
            else
                s = "▪";
        }

        return IntAttached.with(
            panel.getLevelInStorage(),
            Component.literal(s + " ").withColor(panel.getIngredientStatusColor())
                .append(filter.getHoverName().plainCopy().withStyle(ChatFormatting.RESET))
        );
    }

    @Override
    protected String getTranslationKey() {
        return "gauge_status";
    }

    @Override
    protected boolean valueFirst() {
        return true;
    }

}
