package com.zurrtum.create.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ServerSidedFilteringBehaviour extends ServerFilteringBehaviour {

    Map<Direction, ServerFilteringBehaviour> sidedFilters;
    private final BiFunction<Direction, ServerFilteringBehaviour, ServerFilteringBehaviour> filterFactory;
    private final Predicate<Direction> validDirections;
    private Consumer<Direction> removeListener;

    public ServerSidedFilteringBehaviour(
        SmartBlockEntity be,
        BiFunction<Direction, ServerFilteringBehaviour, ServerFilteringBehaviour> filterFactory,
        Predicate<Direction> validDirections
    ) {
        super(be);
        this.filterFactory = filterFactory;
        this.validDirections = validDirections;
        sidedFilters = new IdentityHashMap<>();
        updateFilterPresence();
    }

    public ServerFilteringBehaviour get(Direction side) {
        return sidedFilters.get(side);
    }

    public void updateFilterPresence() {
        Set<Direction> valid = new HashSet<>();
        for (Direction d : Iterate.directions)
            if (validDirections.test(d))
                valid.add(d);
        for (Direction d : Iterate.directions)
            if (valid.contains(d)) {
                if (!sidedFilters.containsKey(d))
                    sidedFilters.put(d, filterFactory.apply(d, new ServerFilteringBehaviour(blockEntity)));
            } else if (sidedFilters.containsKey(d))
                removeFilter(d);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        WriteView.ListView list = view.getList("Filters");
        sidedFilters.forEach((side, filter) -> {
            WriteView item = list.add();
            item.put("Side", Direction.CODEC, side);
            filter.write(item, clientPacket);
        });
        super.write(view, clientPacket);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        view.getListReadView("Filters").forEach(item -> {
            Direction side = item.read("Side", Direction.CODEC).orElseThrow();
            ServerFilteringBehaviour filter = sidedFilters.get(side);
            if (filter != null) {
                filter.read(item, clientPacket);
            }
        });
        super.read(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();
        sidedFilters.values().forEach(ServerFilteringBehaviour::tick);
    }

    @Override
    public boolean setFilter(Direction side, ItemStack stack) {
        if (!sidedFilters.containsKey(side))
            return true;
        sidedFilters.get(side).setFilter(stack);
        return true;
    }

    @Override
    public ItemStack getFilter(Direction side) {
        if (!sidedFilters.containsKey(side))
            return ItemStack.EMPTY;
        return sidedFilters.get(side).getFilter();
    }

    public boolean test(Direction side, ItemStack stack) {
        if (!sidedFilters.containsKey(side))
            return true;
        return sidedFilters.get(side).test(stack);
    }

    @Override
    public void destroy() {
        sidedFilters.values().forEach(ServerFilteringBehaviour::destroy);
        super.destroy();
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return sidedFilters.values().stream().reduce(ItemRequirement.NONE, (a, b) -> a.union(b.getRequiredItems()), ItemRequirement::union);
    }

    public void removeFilter(Direction side) {
        if (!sidedFilters.containsKey(side))
            return;
        sidedFilters.remove(side).destroy();
        if (removeListener != null) {
            removeListener.accept(side);
        }
    }

    public void setRemoveListener(Consumer<Direction> removeListener) {
        this.removeListener = removeListener;
    }
}
