package com.zurrtum.create.compat.computercraft.implementation;

import com.zurrtum.create.Create;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.events.*;
import com.zurrtum.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ComputerBehaviour extends AbstractComputerBehaviour {

    public SyncedPeripheral<?> peripheral;
    private boolean hasAttachedComputer;

    public ComputerBehaviour(SmartBlockEntity be) {
        super(be);
        hasAttachedComputer = false;
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        hasAttachedComputer = view.getBooleanOr("HasAttachedComputer", false);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putBoolean("HasAttachedComputer", hasAttachedComputer);
    }

    @Override
    public boolean isActive() {
        return peripheral != null;
    }

    @Override
    public void setHasAttachedComputer(boolean hasAttachedComputer) {
        this.hasAttachedComputer = hasAttachedComputer;
    }

    @Override
    public boolean hasAttachedComputer() {
        return hasAttachedComputer;
    }

    @Override
    public void queueKineticsChange(float speed, float capacity, float stress, boolean overStressed) {
        peripheral.prepareComputerEvent(new KineticsChangeEvent(speed, capacity, stress, overStressed));
    }

    @Override
    public void queuePackageReceived(ItemStack box) {
        peripheral.prepareComputerEvent(new PackageEvent(box, "package_received"));
    }

    @Override
    public void queuePackageCreated(ItemStack createdBox) {
        peripheral.prepareComputerEvent(new PackageEvent(createdBox, "package_created"));
    }

    @Override
    public void queueRepackage(List<BigItemStack> boxesToExport) {
        for (BigItemStack box : boxesToExport) {
            peripheral.prepareComputerEvent(new RepackageEvent(box.stack, box.count));
        }
    }

    @Override
    public void queueTrainPass(TrackObserver observer, boolean shouldBePowered) {
        TrackObserverBlockEntity be = (TrackObserverBlockEntity) blockEntity;
        if (shouldBePowered) {
            be.passingTrainUUID = observer.getCurrentTrain();
        }
        if (be.passingTrainUUID != null) {
            peripheral.prepareComputerEvent(new TrainPassEvent(Create.RAILWAYS.trains.get(be.passingTrainUUID), shouldBePowered));
            if (!shouldBePowered) {
                be.passingTrainUUID = null;
            }
        }
    }

    @Override
    public void queueSignalState(SignalState state) {
        peripheral.prepareComputerEvent(new SignalStateChangeEvent(state));
    }

    @Override
    public void queueStationTrain(Train imminentTrain, boolean newlyArrived, boolean trainPresent) {
        StationBlockEntity be = (StationBlockEntity) blockEntity;
        UUID trainId = be.imminentTrain;
        if (trainId == null && imminentTrain != null) {
            peripheral.prepareComputerEvent(new StationTrainPresenceEvent(StationTrainPresenceEvent.Type.IMMINENT, imminentTrain));
        }
        if (newlyArrived) {
            if (trainPresent) {
                peripheral.prepareComputerEvent(new StationTrainPresenceEvent(StationTrainPresenceEvent.Type.ARRIVAL, imminentTrain));
            } else if (trainId != null) {
                Train train = Create.RAILWAYS.trains.get(trainId);
                if (train != null) {
                    peripheral.prepareComputerEvent(new StationTrainPresenceEvent(StationTrainPresenceEvent.Type.DEPARTURE, train));
                }
            }
        }
    }

    @Override
    public void prepareComputerEvent(@NotNull ComputerEvent event) {
        if (peripheral != null) {
            peripheral.prepareComputerEvent(event);
        }
    }

}
