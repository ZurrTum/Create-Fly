package com.zurrtum.create.content.logistics.stockTicker;

import com.zurrtum.create.content.logistics.packager.IdentifiedInventory;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.packagerLink.LogisticsManager;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class StockCheckingBlockEntity extends SmartBlockEntity {

    public LogisticallyLinkedBehaviour behaviour;

    public StockCheckingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(10);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, false));
    }

    public InventorySummary getRecentSummary() {
        return LogisticsManager.getSummaryOfNetwork(behaviour.freqId, false);
    }

    public InventorySummary getAccurateSummary() {
        return LogisticsManager.getSummaryOfNetwork(behaviour.freqId, true);
    }

    public boolean broadcastPackageRequest(RequestType type, PackageOrder order, @Nullable IdentifiedInventory ignoredHandler, String address) {
        return broadcastPackageRequest(type, PackageOrderWithCrafts.simple(order.stacks()), ignoredHandler, address);
    }

    public boolean broadcastPackageRequest(
        RequestType type,
        PackageOrderWithCrafts order,
        @Nullable IdentifiedInventory ignoredHandler,
        String address
    ) {
        return LogisticsManager.broadcastPackageRequest(behaviour.freqId, type, order, ignoredHandler, address);
    }

}
