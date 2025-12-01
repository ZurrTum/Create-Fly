package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.Create;
import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.compat.computercraft.events.TrainPassEvent;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrackObserverPeripheral extends SyncedPeripheral<TrackObserverBlockEntity> {

	public TrackObserverPeripheral(TrackObserverBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction
	public boolean isTrainPassing() {
		return Create.RAILWAYS.trains.containsKey(blockEntity.passingTrainUUID);
	}

	@LuaFunction
	public @Nullable String getPassingTrainName() {
		Train train = Create.RAILWAYS.trains.get(blockEntity.passingTrainUUID);
		return train == null ? null : train.name.getString();
	}

	@Override
	public void prepareComputerEvent(@NotNull ComputerEvent event) {
		if (event instanceof TrainPassEvent tpe) {
			queueEvent(tpe.passing ? "train_passing" : "train_passed", tpe.train.name.getString());
		}
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_TrainObserver";
	}

}
