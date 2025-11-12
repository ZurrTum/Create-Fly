package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class IdleCargoCondition extends TimedWaitCondition {
    public IdleCargoCondition(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        int idleTime = Integer.MAX_VALUE;
        for (Carriage carriage : train.carriages)
            idleTime = Math.min(idleTime, carriage.storage.getTicksSinceLastExchange());
        context.putInt("Time", idleTime);
        requestDisplayIfNecessary(context, idleTime);
        return idleTime > totalWaitTicks();
    }
}
