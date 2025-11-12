package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class PlayerPassengerCondition extends ScheduleWaitCondition {
    public PlayerPassengerCondition(Identifier id) {
        super(id);
    }

    public int getTarget() {
        return intData("Count");
    }

    public boolean canOvershoot() {
        return intData("Exact") != 0;
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        int prev = context.getIntOr("PrevPlayerCount", 0);
        int present = train.countPlayerPassengers();
        int target = getTarget();
        context.putInt("PrevPlayerCount", present);
        if (prev != present)
            requestStatusToUpdate(context);
        return canOvershoot() ? present >= target : present == target;
    }

    @Override
    public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag) {
        return Component.translatable("create.schedule.condition.player_count.status", train.countPlayerPassengers(), getTarget());
    }
}
