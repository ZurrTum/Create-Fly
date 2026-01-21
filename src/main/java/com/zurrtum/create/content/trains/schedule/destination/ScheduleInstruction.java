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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class ScheduleInstruction extends ScheduleDataEntry {
    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleInstruction> STREAM_CODEC = StreamCodec.of(
        ScheduleInstruction::encode,
        ScheduleInstruction::decode
    );

    public ScheduleInstruction(Identifier id) {
        super(id);
    }

    public abstract boolean supportsConditions();

    @Nullable
    public abstract DiscoveredPath start(ScheduleRuntime runtime, Level level);

    public final void write(ValueOutput view) {
        view.store("Id", Identifier.CODEC, id);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleInstruction", Create.LOGGER)) {
            TagValueOutput writeView = new TagValueOutput(logging, ((TagValueOutput) view).ops, data);
            writeAdditional(writeView);
            view.store("Data", CompoundTag.CODEC, writeView.buildResult());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DataResult<T> encode(final ScheduleInstruction input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Id", input.id, Identifier.CODEC);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleInstruction", Create.LOGGER)) {
            TagValueOutput view = new TagValueOutput(logging, (DynamicOps<Tag>) ops, input.data);
            input.writeAdditional(view);
            map.add("Data", view.buildResult(), CompoundTag.CODEC);
        }
        return map.build(empty);
    }

    public static ScheduleInstruction read(ValueInput view) {
        Identifier location = view.read("Id", Identifier.CODEC).orElse(null);
        ScheduleInstruction scheduleDestination = AllSchedules.createScheduleInstruction(location);
        if (scheduleDestination == null) {
            return fallback(location);
        }
        ValueInput data = view.childOrEmpty("Data");
        scheduleDestination.readAdditional(data);
        scheduleDestination.data = view.read("Data", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        return scheduleDestination;
    }

    public static <T> ScheduleInstruction decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        Identifier location = Identifier.CODEC.parse(ops, map.get("Id")).getOrThrow();
        ScheduleInstruction scheduleDestination = AllSchedules.createScheduleInstruction(location);
        if (scheduleDestination == null) {
            return fallback(location);
        }
        scheduleDestination.data = CompoundTag.CODEC.parse(ops, map.get("Data")).result().orElseGet(CompoundTag::new);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleInstruction", Create.LOGGER)) {
            TagValueInput view = new TagValueInput(logging, new NbtReadContext(ops), scheduleDestination.data);
            scheduleDestination.readAdditional(view);
        }
        return scheduleDestination;
    }

    private static ScheduleInstruction fallback(Identifier location) {
        Create.LOGGER.warn("Could not parse schedule instruction type: {}", location);
        return new DestinationInstruction(AllSchedules.DESTINATION);
    }

    private static void encode(RegistryFriendlyByteBuf buf, ScheduleInstruction value) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleInstruction", Create.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, buf.registryAccess());
            value.write(view);
            buf.writeNbt(view.buildResult());
        }
    }

    private static ScheduleInstruction decode(RegistryFriendlyByteBuf buf) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleInstruction", Create.LOGGER)) {
            ValueInput view = TagValueInput.create(logging, buf.registryAccess(), buf.readNbt());
            return ScheduleInstruction.read(view);
        }
    }
}
