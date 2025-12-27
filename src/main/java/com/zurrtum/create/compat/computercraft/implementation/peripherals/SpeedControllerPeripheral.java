package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;

public class SpeedControllerPeripheral extends SyncedPeripheral<SpeedControllerBlockEntity> {

    public SpeedControllerPeripheral(SpeedControllerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    public final void setTargetSpeed(int speed) {
        blockEntity.targetSpeed.setValue(speed);
    }

    @LuaFunction
    public final float getTargetSpeed() {
        return blockEntity.targetSpeed.getValue();
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_RotationSpeedController";
    }

}
