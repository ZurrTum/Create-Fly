package com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering;

import com.zurrtum.create.client.content.logistics.tunnel.BrassTunnelFilterSlot;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerSidedFilteringBehaviour;
import net.minecraft.util.math.Direction;

import java.util.EnumMap;
import java.util.Map;

public class SidedFilteringBehaviour extends FilteringBehaviour<ServerSidedFilteringBehaviour> {
    Map<Direction, FilteringBehaviour<ServerFilteringBehaviour>> sidedFilters = new EnumMap<>(Direction.class);

    public SidedFilteringBehaviour(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
    }

    @Override
    public void initialize() {
        super.initialize();
        behaviour.setRemoveListener(this::removeFilter);
    }

    private void removeFilter(Direction side) {
        FilteringBehaviour<ServerFilteringBehaviour> filter = sidedFilters.get(side);
        if (filter != null) {
            filter.behaviour = null;
        }
    }

    public FilteringBehaviour<ServerFilteringBehaviour> get(Direction side) {
        FilteringBehaviour<ServerFilteringBehaviour> sidedFilter = sidedFilters.get(side);
        if (sidedFilter != null && sidedFilter.behaviour != null) {
            return sidedFilter;
        }
        ServerFilteringBehaviour filter = behaviour.get(side);
        if (filter == null) {
            return null;
        }
        if (sidedFilter == null) {
            sidedFilter = new FilteringBehaviour<>(blockEntity, slotPositioning);
        }
        sidedFilter.behaviour = filter;
        sidedFilters.put(side, sidedFilter);
        return sidedFilter;
    }

    public static SidedFilteringBehaviour tunnel(BrassTunnelBlockEntity be) {
        return new SidedFilteringBehaviour(be, new BrassTunnelFilterSlot());
    }
}
