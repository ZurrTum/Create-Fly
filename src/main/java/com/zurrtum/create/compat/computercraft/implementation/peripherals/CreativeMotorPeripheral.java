package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;

public class CreativeMotorPeripheral extends SyncedPeripheral<CreativeMotorBlockEntity> {

    private final ServerScrollValueBehaviour generatedSpeed;

    public CreativeMotorPeripheral(CreativeMotorBlockEntity blockEntity, ServerScrollValueBehaviour generatedSpeed) {
        super(blockEntity);
        this.generatedSpeed = generatedSpeed;
    }

    @LuaFunction(mainThread = true)
    public final void setGeneratedSpeed(int speed) {
        this.generatedSpeed.setValue(speed);
    }

    @LuaFunction
    public final float getGeneratedSpeed() {
        return this.generatedSpeed.getValue();
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_CreativeMotor";
    }

}
