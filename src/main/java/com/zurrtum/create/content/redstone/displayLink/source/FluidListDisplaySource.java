package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.zurrtum.create.foundation.utility.FluidFormatter;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.MutableText;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FluidListDisplaySource extends ValueListDisplaySource {
    @Override
    protected Stream<IntAttached<MutableText>> provideEntries(DisplayLinkContext context, int maxRows) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
            return Stream.empty();

        TankManipulationBehaviour tankManipulationBehaviour = cobe.getBehaviour(TankManipulationBehaviour.OBSERVE);
        ServerFilteringBehaviour filteringBehaviour = cobe.getBehaviour(ServerFilteringBehaviour.TYPE);
        FluidInventory handler = tankManipulationBehaviour.getInventory();

        if (handler == null)
            return Stream.empty();


        Map<Fluid, Integer> fluids = new HashMap<>();
        Map<Fluid, FluidStack> fluidNames = new HashMap<>();

        for (int i = 0, size = handler.size(); i < size; i++) {
            FluidStack stack = handler.getStack(i);
            if (stack.isEmpty())
                continue;
            if (filteringBehaviour.test(stack)) {
                fluids.merge(stack.getFluid(), stack.getAmount(), Integer::sum);
                fluidNames.putIfAbsent(stack.getFluid(), stack);
            }
        }

        return fluids.entrySet().stream().sorted(Comparator.<Map.Entry<Fluid, Integer>>comparingInt(Map.Entry::getValue).reversed()).limit(maxRows)
            .map(entry -> IntAttached.with(entry.getValue(), fluidNames.get(entry.getKey()).getName().copy()));
    }

    @Override
    protected List<MutableText> createComponentsFromEntry(DisplayLinkContext context, IntAttached<MutableText> entry) {
        int amount = entry.getFirst();
        MutableText name = entry.getSecond().append(WHITESPACE);

        Couple<MutableText> formatted = FluidFormatter.asComponents(amount, shortenNumbers(context));

        return List.of(formatted.getFirst(), formatted.getSecond(), name);
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
        Integer max = ((MutableInt) context.flapDisplayContext).getValue();
        boolean shorten = shortenNumbers(context);
        int length = FluidFormatter.asString(max, shorten).length();
        String layoutKey = "FluidList_" + length;

        if (layout.isLayout(layoutKey))
            return;

        int maxCharCount = flapDisplay.getMaxCharCount(1);
        int numberLength = Math.min(maxCharCount, Math.max(3, length - 2));
        int nameLength = Math.max(maxCharCount - numberLength - 2, 0);

        FlapDisplaySection value = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * numberLength, "number", false, false).rightAligned();
        FlapDisplaySection unit = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * 2, "fluid_units", true, true);
        FlapDisplaySection name = new FlapDisplaySection(FlapDisplaySection.MONOSPACE * nameLength, "alphabet", false, false);

        layout.configure(layoutKey, List.of(value, unit, name));
    }

    @Override
    protected String getTranslationKey() {
        return "list_fluids";
    }

    @Override
    protected boolean valueFirst() {
        return false;
    }
}
