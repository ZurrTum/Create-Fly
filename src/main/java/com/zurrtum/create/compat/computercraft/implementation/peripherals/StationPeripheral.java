package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Glob;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.compat.computercraft.events.StationTrainPresenceEvent;
import com.zurrtum.create.compat.computercraft.implementation.CreateLuaTable;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.foundation.utility.StringHelper;
import com.zurrtum.create.infrastructure.packet.s2c.TrainEditReturnPacket;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import net.minecraft.nbt.*;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public class StationPeripheral extends SyncedPeripheral<StationBlockEntity> {

    public StationPeripheral(StationBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    public final void assemble() throws LuaException {
        if (!blockEntity.isAssembling())
            throw new LuaException("station must be in assembly mode");

        blockEntity.assemble(null);

        if (blockEntity.getStation() == null || blockEntity.getStation().getPresentTrain() == null)
            throw new LuaException("failed to assemble train");

        if (!blockEntity.exitAssemblyMode())
            throw new LuaException("failed to exit assembly mode");
    }

    @LuaFunction(mainThread = true)
    public final void disassemble() throws LuaException {
        if (blockEntity.isAssembling())
            throw new LuaException("station must not be in assembly mode");

        getTrainOrThrow();

        if (!blockEntity.enterAssemblyMode(null))
            throw new LuaException("could not disassemble train");
    }

    @LuaFunction(mainThread = true)
    public final void setAssemblyMode(boolean assemblyMode) throws LuaException {
        if (assemblyMode) {
            if (!blockEntity.enterAssemblyMode(null))
                throw new LuaException("failed to enter assembly mode");
        } else {
            if (!blockEntity.exitAssemblyMode())
                throw new LuaException("failed to exit assembly mode");
        }
    }

    @LuaFunction
    public final boolean isInAssemblyMode() {
        return blockEntity.isAssembling();
    }

    @LuaFunction
    public final String getStationName() throws LuaException {
        GlobalStation station = blockEntity.getStation();
        if (station == null)
            throw new LuaException("station is not connected to a track");

        return station.name;
    }

    @LuaFunction(mainThread = true)
    public final void setStationName(String name) throws LuaException {
        if (!blockEntity.updateName(name))
            throw new LuaException("could not set station name");
    }

    @LuaFunction
    public final boolean isTrainPresent() throws LuaException {
        GlobalStation station = blockEntity.getStation();
        if (station == null)
            throw new LuaException("station is not connected to a track");

        return station.getPresentTrain() != null;
    }

    @LuaFunction
    public final boolean isTrainImminent() throws LuaException {
        GlobalStation station = blockEntity.getStation();
        if (station == null)
            throw new LuaException("station is not connected to a track");

        return station.getImminentTrain() != null;
    }

    @LuaFunction
    public final boolean isTrainEnroute() throws LuaException {
        GlobalStation station = blockEntity.getStation();
        if (station == null)
            throw new LuaException("station is not connected to a track");

        return station.getNearestTrain() != null;
    }

    @LuaFunction
    public final String getTrainName() throws LuaException {
        Train train = getTrainOrThrow();
        return train.name.getString();
    }

    @LuaFunction(mainThread = true)
    public final void setTrainName(String name) throws LuaException {
        Train train = getTrainOrThrow();
        train.name = Text.literal(name);
        blockEntity.getWorld().getServer().getPlayerManager().sendToAll((new TrainEditReturnPacket(train.id, name, train.icon.id(), train.mapColorIndex)));
    }

    @LuaFunction
    public final boolean hasSchedule() throws LuaException {
        Train train = getTrainOrThrow();
        return train.runtime.getSchedule() != null;
    }

    @LuaFunction
    public final CreateLuaTable getSchedule() throws LuaException {
        Train train = getTrainOrThrow();

        Schedule schedule = train.runtime.getSchedule();
        if (schedule == null)
            throw new LuaException("train doesn't have a schedule");
        NbtWriteView writeView;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "StationPeripheral", Create.LOGGER)) {
            writeView = NbtWriteView.create(logging,
                    blockEntity.getWorld().getRegistryManager());
            schedule.write(writeView);
        }
        return fromCompoundTag(writeView.getNbt());
    }

    @LuaFunction(mainThread = true)
    public final void setSchedule(IArguments arguments) throws LuaException {
        Train train = getTrainOrThrow();

        ReadView readView;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "StationPeripheral", Create.LOGGER)) {
            readView = NbtReadView.create(logging,
                    blockEntity.getWorld().getRegistryManager(),
                    toCompoundTag(new CreateLuaTable(arguments.getTable(0))));
        }

        Schedule schedule = Schedule.read(readView);

        // We must check the completed schedule, because `toCompoundTag` normalizes all CompoundTag keys to CamelCase
        // and so `Entries`, `entries`, `EnTrIeS`, etc. will all be converted to `Entries` in the schedule
        // https://github.com/Creators-of-Create/Create/issues/8504
        if (schedule.entries.isEmpty())
            throw new LuaException("Schedule must have at least one entry");

        boolean autoSchedule = train.runtime.getSchedule() == null || train.runtime.isAutoSchedule;
        train.runtime.setSchedule(schedule, autoSchedule);
    }

    /**
     * @return Path (if available) and boolean indicating if destination exists at all.
     */
    private Pair<@Nullable DiscoveredPath, @NotNull Boolean> findPath(String destinationFilter) throws LuaException {
        Train train = getTrainOrThrow();
        String regex = Glob.toRegexPattern(destinationFilter, "");
        boolean anyMatch = false;
        ArrayList<GlobalStation> validStations = new ArrayList<>();
        try {
            for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
                if (!globalStation.name.matches(regex))
                    continue;
                anyMatch = true;
                validStations.add(globalStation);
            }
        } catch (PatternSyntaxException ignored) {
        }

        DiscoveredPath best = train.navigation.findPathTo(validStations, Double.MAX_VALUE);
        if (best == null)
            return Pair.of(null, anyMatch);
        return Pair.of(best, true);
    }

    @LuaFunction
    public MethodResult canTrainReach(String destinationFilter) throws LuaException {
        Pair<@Nullable DiscoveredPath, @NotNull Boolean> path = findPath(destinationFilter);
        if (path.getFirst() != null)
            return MethodResult.of(true, null);
        return MethodResult.of(false, path.getSecond() ? "cannot-reach" : "no-target");
    }

    @LuaFunction
    public MethodResult distanceTo(String destinationFilter) throws LuaException {
        Pair<@Nullable DiscoveredPath, @NotNull Boolean> path = findPath(destinationFilter);
        if (path.getFirst() != null)
            return MethodResult.of(path.getFirst().distance, null);
        return MethodResult.of(null, path.getSecond() ? "cannot-reach" : "no-target");
    }

    private @NotNull Train getTrainOrThrow() throws LuaException {
        GlobalStation station = blockEntity.getStation();
        if (station == null)
            throw new LuaException("station is not connected to a track");

        Train train = station.getPresentTrain();
        if (train == null)
            throw new LuaException("there is no train present");

        return train;
    }

    private static @NotNull CreateLuaTable fromCompoundTag(NbtCompound tag) throws LuaException {
        return (CreateLuaTable) fromNBTTag(null, tag);
    }

    private static @NotNull Object fromNBTTag(@Nullable String key, NbtElement tag) throws LuaException {
        byte type = tag.getType();

        if (type == NbtElement.BYTE_TYPE && key != null && key.equals("Count"))
            return tag.asByte().get();
        else if (type == NbtElement.BYTE_TYPE)
            return tag.asByte().get() != 0;
        else if (type == NbtElement.SHORT_TYPE || type == NbtElement.INT_TYPE || type == NbtElement.LONG_TYPE)
            return tag.asLong().get();
        else if (type == NbtElement.FLOAT_TYPE || type == NbtElement.DOUBLE_TYPE)
            return tag.asDouble().get();
        else if (type == NbtElement.STRING_TYPE)
            return tag.asString().get();
        else if (type == NbtElement.LIST_TYPE || type == NbtElement.BYTE_ARRAY_TYPE || type == NbtElement.INT_ARRAY_TYPE || type == NbtElement.LONG_ARRAY_TYPE) {
            CreateLuaTable list = new CreateLuaTable();
            NbtList listTag = tag.asNbtList().get();

            for (int i = 0; i < listTag.size(); i++) {
                list.put(i + 1, fromNBTTag(null, listTag.get(i)));
            }

            return list;

        } else if (type == NbtElement.COMPOUND_TYPE) {
            CreateLuaTable table = new CreateLuaTable();
            NbtCompound compoundTag = tag.asCompound().get();

            for (String compoundKey : compoundTag.getKeys()) {
                table.put(
                        StringHelper.camelCaseToSnakeCase(compoundKey),
                        fromNBTTag(compoundKey, compoundTag.get(compoundKey))
                );
            }

            return table;
        }

        throw new LuaException("unknown tag type " + tag.getClass().getName());
    }

    private static @NotNull NbtCompound toCompoundTag(CreateLuaTable table) throws LuaException {
        return (NbtCompound) toNBTTag(null, table.getMap());
    }

    private static @NotNull NbtElement toNBTTag(@Nullable String key, Object value) throws LuaException {
        if (value instanceof Boolean v)
            return NbtByte.of(v);
        else if (value instanceof Byte || (key != null && key.equals("count")))
            return NbtByte.of(((Number) value).byteValue());
        else if (value instanceof Number v) {
            // If number is numerical integer
            if (v.intValue() == v.doubleValue())
                return NbtInt.of(v.intValue());
            else
                return NbtDouble.of(v.doubleValue());

        } else if (value instanceof String v)
            return NbtString.of(v);
        else if (value instanceof Map<?, ?> v && v.containsKey(1.0)) { // List
            NbtList list = new NbtList();
            for (double i = 1; i <= v.size(); i++) {
                if (v.get(i) != null)
                    list.add(toNBTTag(null, v.get(i)));
            }

            return list;

        } else if (value instanceof Map<?, ?> v) { // Table/Map
            NbtCompound compound = new NbtCompound();
            for (Object objectKey : v.keySet()) {
                if (!(objectKey instanceof String compoundKey))
                    throw new LuaException("table key is not of type string");

                compound.put(
                        // Items serialize their resource location as "id" and not as "Id".
                        // This check is needed to see if the 'i' should be left lowercase or not.
                        // Items store "count" in the same compound tag, so we can check for its presence to see if this is a serialized item
                        compoundKey.equals("id") && v.containsKey("count") ? "id" : StringHelper.snakeCaseToCamelCase(compoundKey),
                        toNBTTag(compoundKey, v.get(compoundKey))
                );
            }

            return compound;
        }

        throw new LuaException("unknown object type " + value.getClass().getName());
    }

    @Override
    public void prepareComputerEvent(@NotNull ComputerEvent event) {
        if (event instanceof StationTrainPresenceEvent stpe) {
            queueEvent(stpe.type.name, stpe.train.name.getString());
        }
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_Station";
    }

}
