package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.compat.computercraft.events.KineticsChangeEvent;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;

public class SpeedGaugePeripheral extends SyncedPeripheral<SpeedGaugeBlockEntity> {

	public SpeedGaugePeripheral(SpeedGaugeBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction
	public final float getSpeed() {
		return this.blockEntity.getSpeed();
	}

	@Override
	public void prepareComputerEvent(@NotNull ComputerEvent event) {
		if (event instanceof KineticsChangeEvent kce) {
			queueEvent("speed_change", kce.overStressed ? 0 : kce.speed);
		}
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Speedometer";
	}

}
