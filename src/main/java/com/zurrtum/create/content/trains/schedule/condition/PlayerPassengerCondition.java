package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        int prev = context.getInt("PrevPlayerCount", 0);
        int present = train.countPlayerPassengers();
        int target = getTarget();
        context.putInt("PrevPlayerCount", present);
        if (prev != present)
            requestStatusToUpdate(context);
        return canOvershoot() ? present >= target : present == target;
    }

    @Override
    public MutableText getWaitingStatus(World level, Train train, NbtCompound tag) {
        return Text.translatable("create.schedule.condition.player_count.status", train.countPlayerPassengers(), getTarget());
    }
}
