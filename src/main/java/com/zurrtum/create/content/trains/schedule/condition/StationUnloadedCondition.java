package com.zurrtum.create.content.trains.schedule.condition;

import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StationUnloadedCondition extends ScheduleWaitCondition {
    public StationUnloadedCondition(Identifier id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        GlobalStation currentStation = train.getCurrentStation();
        if (currentStation == null)
            return false;
        RegistryKey<World> stationDim = currentStation.getBlockEntityDimension();
        MinecraftServer server = level.getServer();
        if (server == null)
            return false;
        ServerWorld stationLevel = server.getWorld(stationDim);
        if (stationLevel == null) {
            return false;
        }
        return !stationLevel.shouldTickEntityAt(currentStation.getBlockEntityPos());
    }

    @Override
    public MutableText getWaitingStatus(World level, Train train, NbtCompound tag) {
        return Text.translatable("create.schedule.condition.unloaded.status");
    }
}
