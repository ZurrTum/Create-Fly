package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class CargoThresholdCondition extends LazyTickedScheduleCondition {
    public enum Ops {
        GREATER(">"),
        LESS("<"),
        EQUAL("=");

        public final String formatted;

        Ops(String formatted) {
            this.formatted = formatted;
        }

        public boolean test(int current, int target) {
            return switch (this) {
                case GREATER -> current > target;
                case EQUAL -> current == target;
                case LESS -> current < target;
            };
        }
    }

    public CargoThresholdCondition(Identifier id) {
        super(id, 20);
        data.putString("Threshold", "10");
    }

    @Override
    public boolean lazyTickCompletion(World level, Train train, NbtCompound context) {
        int lastChecked = context.contains("LastChecked") ? context.getInt("LastChecked", 0) : -1;
        int status = 0;
        for (Carriage carriage : train.carriages)
            status += carriage.storage.getVersion();
        if (status == lastChecked)
            return false;
        context.putInt("LastChecked", status);
        return test(level, train, context);
    }

    protected void requestStatusToUpdate(int amount, NbtCompound context) {
        context.putInt("CurrentDisplay", amount);
        super.requestStatusToUpdate(context);
    }

    protected int getLastDisplaySnapshot(NbtCompound context) {
        if (!context.contains("CurrentDisplay"))
            return -1;
        return context.getInt("CurrentDisplay", 0);
    }

    protected abstract boolean test(World level, Train train, NbtCompound context);

    public Ops getOperator() {
        return enumData("Operator", Ops.class);
    }

    public int getThreshold() {
        try {
            return Integer.parseInt(textData("Threshold"));
        } catch (NumberFormatException e) {
            data.putString("Threshold", "0");
        }
        return 0;
    }

    public int getMeasure() {
        return intData("Measure");
    }
}
