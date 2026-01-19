package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.compat.computercraft.events.KineticsChangeEvent;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;

public class StressGaugePeripheral extends SyncedPeripheral<StressGaugeBlockEntity> {

    public StressGaugePeripheral(StressGaugeBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction
    public final float getStress() {
        return this.blockEntity.getNetworkStress();
    }

    @LuaFunction
    public final float getStressCapacity() {
        return this.blockEntity.getNetworkCapacity();
    }

    @Override
    public void prepareComputerEvent(ComputerEvent event) {
        if (event instanceof KineticsChangeEvent kce) {
            if (kce.overStressed)
                queueEvent("overstressed");
            else
                queueEvent("stress_change", kce.stress, kce.capacity);
        }
    }

    @Override
    public String getType() {
        return "Create_Stressometer";
    }

}
