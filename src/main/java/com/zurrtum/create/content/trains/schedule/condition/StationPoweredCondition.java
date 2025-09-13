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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StationPoweredCondition extends ScheduleWaitCondition {
    public StationPoweredCondition(Identifier id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(World level, Train train, NbtCompound context) {
        GlobalStation currentStation = train.getCurrentStation();
        if (currentStation == null)
            return false;
        BlockPos stationPos = currentStation.getBlockEntityPos();
        RegistryKey<World> stationDim = currentStation.getBlockEntityDimension();
        MinecraftServer server = level.getServer();
        if (server == null)
            return false;
        ServerWorld stationLevel = server.getWorld(stationDim);
        if (stationLevel == null || !stationLevel.isPosLoaded(stationPos))
            return false;
        return stationLevel.isReceivingRedstonePower(stationPos);
    }

    @Override
    public MutableText getWaitingStatus(World level, Train train, NbtCompound tag) {
        return Text.translatable("create.schedule.condition.powered.status");
    }
}
