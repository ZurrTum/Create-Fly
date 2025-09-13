package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FactoryGaugeDisplaySource extends ValueListDisplaySource {

    @Override
    protected Stream<IntAttached<MutableText>> provideEntries(DisplayLinkContext context, int maxRows) {
        List<FactoryPanelPosition> panels = context.blockEntity().factoryPanelSupport.getLinkedPanels();
        if (panels.isEmpty())
            return Stream.empty();
        return panels.stream().map(fpp -> createEntry(context.level(), fpp))
            //			.sorted(IntAttached.comparator())
            .filter(Objects::nonNull).limit(maxRows);
    }

    @Nullable
    public IntAttached<MutableText> createEntry(World level, FactoryPanelPosition pos) {
        ServerFactoryPanelBehaviour panel = ServerFactoryPanelBehaviour.at(level, pos);
        if (panel == null)
            return null;

        ItemStack filter = panel.getFilter();

        int demand = panel.getAmount() * (panel.upTo ? 1 : filter.getMaxCount());
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
            Text.literal(s + " ").withColor(panel.getIngredientStatusColor()).append(filter.getName().copyContentOnly().formatted(Formatting.RESET))
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
