package com.zurrtum.create.content.contraptions.minecart;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TrainCargoManager extends MountedStorageManager {

    int ticksSinceLastExchange;
    AtomicInteger version;

    public TrainCargoManager() {
        version = new AtomicInteger();
        ticksSinceLastExchange = 0;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.items = new CargoInvWrapper(this.items);
        this.allItems = this.items;
        if (this.fuelItems != null) {
            this.fuelItems = new CargoInvWrapper(this.fuelItems);
        }
        this.fluids = new CargoTankWrapper(this.fluids);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("TicksSinceLastExchange", ticksSinceLastExchange);
    }

    @Override
    public <T> void write(final DynamicOps<T> ops, final T empty, RecordBuilder<T> map, boolean clientPacket) {
        super.write(ops, empty, map, clientPacket);
        map.add("TicksSinceLastExchange", ops.createInt(ticksSinceLastExchange));
    }

    @Override
    public void read(ValueInput view, boolean clientPacket, @Nullable Contraption contraption) {
        super.read(view, clientPacket, contraption);
        ticksSinceLastExchange = view.getIntOr("TicksSinceLastExchange", 0);
    }

    @Override
    public <T> void read(final DynamicOps<T> ops, MapLike<T> map, boolean clientPacket, @Nullable Contraption contraption) {
        super.read(ops, map, clientPacket, contraption);
        ticksSinceLastExchange = ops.getNumberValue(map.get("TicksSinceLastExchange"), 0).intValue();
    }

    public void resetIdleCargoTracker() {
        ticksSinceLastExchange = 0;
    }

    public void tickIdleCargoTracker() {
        ticksSinceLastExchange++;
    }

    public int getTicksSinceLastExchange() {
        return ticksSinceLastExchange;
    }

    public int getVersion() {
        return version.get();
    }

    void changeDetected() {
        version.incrementAndGet();
        resetIdleCargoTracker();
    }

    class CargoInvWrapper extends MountedItemStorageWrapper {
        CargoInvWrapper(MountedItemStorageWrapper wrapped) {
            super(wrapped.storages);
        }

        @Override
        public void markInventoryDirty() {
            changeDetected();
        }
    }

    class CargoTankWrapper extends MountedFluidStorageWrapper {
        CargoTankWrapper(MountedFluidStorageWrapper wrapped) {
            super(wrapped.storages);
        }

        @Override
        public void markInventoryDirty() {
            changeDetected();
        }
    }

}
