package com.zurrtum.create.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

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
    private @Nullable Consumer<Direction> removeListener;

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

    @Nullable
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
    public void write(ValueOutput view, boolean clientPacket) {
        ValueOutput.ValueOutputList list = view.childrenList("Filters");
        sidedFilters.forEach((side, filter) -> {
            ValueOutput item = list.addChild();
            item.store("Side", Direction.CODEC, side);
            filter.write(item, clientPacket);
        });
        super.write(view, clientPacket);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        view.childrenListOrEmpty("Filters").forEach(item -> {
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
