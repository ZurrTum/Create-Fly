package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class IdleCargoCondition extends TimedWaitCondition {
    public IdleCargoCondition(Identifier id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        int idleTime = Integer.MAX_VALUE;
        for (Carriage carriage : train.carriages)
            idleTime = Math.min(idleTime, carriage.storage.getTicksSinceLastExchange());
        context.putInt("Time", idleTime);
        requestDisplayIfNecessary(context, idleTime);
        return idleTime > totalWaitTicks();
    }
}
