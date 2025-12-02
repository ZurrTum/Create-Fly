package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;

public class SpeedControllerPeripheral extends SyncedPeripheral<SpeedControllerBlockEntity> {

    private final ServerScrollValueBehaviour targetSpeed;

    public SpeedControllerPeripheral(SpeedControllerBlockEntity blockEntity, ServerScrollValueBehaviour targetSpeed) {
        super(blockEntity);
        this.targetSpeed = targetSpeed;
    }

    @LuaFunction(mainThread = true)
    public final void setTargetSpeed(int speed) {
        this.targetSpeed.setValue(speed);
    }

    @LuaFunction
    public final float getTargetSpeed() {
        return this.targetSpeed.getValue();
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_RotationSpeedController";
    }

}
