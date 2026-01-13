package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;

public class CreativeMotorPeripheral extends SyncedPeripheral<CreativeMotorBlockEntity> {
    public CreativeMotorPeripheral(CreativeMotorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    public final void setGeneratedSpeed(int speed) {
        blockEntity.generatedSpeed.setValue(speed);
    }

    @LuaFunction
    public final float getGeneratedSpeed() {
        return blockEntity.generatedSpeed.getValue();
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_CreativeMotor";
    }

}
