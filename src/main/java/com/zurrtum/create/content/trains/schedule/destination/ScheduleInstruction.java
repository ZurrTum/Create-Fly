package com.zurrtum.create.content.trains.schedule.destination;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.AllSchedules;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.schedule.ScheduleDataEntry;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class ScheduleInstruction extends ScheduleDataEntry {
    public static final PacketCodec<RegistryByteBuf, ScheduleInstruction> STREAM_CODEC = PacketCodec.ofStatic(
        ScheduleInstruction::encode,
        ScheduleInstruction::decode
    );

    public ScheduleInstruction(Identifier id) {
        super(id);
    }

    public abstract boolean supportsConditions();

    @Nullable
    public abstract DiscoveredPath start(ScheduleRuntime runtime, World level);

    public final void write(WriteView view) {
        view.put("Id", Identifier.CODEC, id);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleInstruction", Create.LOGGER)) {
            NbtWriteView writeView = new NbtWriteView(logging, ((NbtWriteView) view).ops, data);
            writeAdditional(writeView);
            view.put("Data", NbtCompound.CODEC, writeView.getNbt());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DataResult<T> encode(final ScheduleInstruction input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Id", input.id, Identifier.CODEC);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleInstruction", Create.LOGGER)) {
            NbtWriteView view = new NbtWriteView(logging, (DynamicOps<NbtElement>) ops, input.data);
            input.writeAdditional(view);
            map.add("Data", view.getNbt(), NbtCompound.CODEC);
        }
        return map.build(empty);
    }

    public static ScheduleInstruction read(ReadView view) {
        Identifier location = view.read("Id", Identifier.CODEC).orElse(null);
        ScheduleInstruction scheduleDestination = AllSchedules.createScheduleInstruction(location);
        if (scheduleDestination == null) {
            return fallback(location);
        }
        ReadView data = view.getReadView("Data");
        scheduleDestination.readAdditional(data);
        scheduleDestination.data = view.read("Data", NbtCompound.CODEC).orElseGet(NbtCompound::new);
        return scheduleDestination;
    }

    public static <T> ScheduleInstruction decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        Identifier location = Identifier.CODEC.parse(ops, map.get("Id")).getOrThrow();
        ScheduleInstruction scheduleDestination = AllSchedules.createScheduleInstruction(location);
        if (scheduleDestination == null) {
            return fallback(location);
        }
        scheduleDestination.data = NbtCompound.CODEC.parse(ops, map.get("Data")).result().orElseGet(NbtCompound::new);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleInstruction", Create.LOGGER)) {
            NbtReadView view = new NbtReadView(logging, new NbtReadContext(ops), scheduleDestination.data);
            scheduleDestination.readAdditional(view);
        }
        return scheduleDestination;
    }

    private static ScheduleInstruction fallback(Identifier location) {
        Create.LOGGER.warn("Could not parse schedule instruction type: {}", location);
        return AllSchedules.createScheduleInstruction(AllSchedules.DESTINATION);
    }

    private static void encode(RegistryByteBuf buf, ScheduleInstruction value) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleInstruction", Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, buf.getRegistryManager());
            value.write(view);
            buf.writeNbt(view.getNbt());
        }
    }

    private static ScheduleInstruction decode(RegistryByteBuf buf) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleInstruction", Create.LOGGER)) {
            ReadView view = NbtReadView.create(logging, buf.getRegistryManager(), buf.readNbt());
            return ScheduleInstruction.read(view);
        }
    }
}
