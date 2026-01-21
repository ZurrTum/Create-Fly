package com.zurrtum.create.content.trains.schedule.condition;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.zurrtum.create.AllSchedules;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.ScheduleDataEntry;
import com.zurrtum.create.content.trains.schedule.destination.NbtReadContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class ScheduleWaitCondition extends ScheduleDataEntry {
    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleWaitCondition> STREAM_CODEC = StreamCodec.of(
        ScheduleWaitCondition::encode,
        ScheduleWaitCondition::decode
    );

    public ScheduleWaitCondition(Identifier id) {
        super(id);
    }

    public abstract boolean tickCompletion(Level level, Train train, CompoundTag context);

    protected void requestStatusToUpdate(CompoundTag context) {
        context.putInt("StatusVersion", context.getIntOr("StatusVersion", 0) + 1);
    }

    public final void write(ValueOutput view) {
        view.store("Id", Identifier.CODEC, id);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            TagValueOutput writeView = new TagValueOutput(logging, ((TagValueOutput) view).ops, data);
            writeAdditional(writeView);
            view.store("Data", CompoundTag.CODEC, writeView.buildResult());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DataResult<T> encode(final ScheduleWaitCondition input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Id", input.id, Identifier.CODEC);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            TagValueOutput view = new TagValueOutput(logging, (DynamicOps<Tag>) ops, input.data);
            input.writeAdditional(view);
            map.add("Data", view.buildResult(), CompoundTag.CODEC);
        }
        return map.build(empty);
    }

    @Nullable
    public static ScheduleWaitCondition read(ValueInput view) {
        Identifier location = view.read("Id", Identifier.CODEC).orElse(null);
        ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(location);
        if (condition == null) {
            return fallback(location);
        }
        ValueInput data = view.childOrEmpty("Data");
        condition.readAdditional(data);
        condition.data = view.read("Data", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        return condition;
    }

    @Nullable
    public static <T> ScheduleWaitCondition decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        Identifier location = Identifier.CODEC.parse(ops, map.get("Id")).result().orElse(null);
        ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(location);
        if (condition == null) {
            return fallback(location);
        }
        condition.data = CompoundTag.CODEC.parse(ops, map.get("Data")).result().orElseGet(CompoundTag::new);
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            TagValueInput view = new TagValueInput(logging, new NbtReadContext(ops), condition.data);
            condition.readAdditional(view);
        }
        return condition;
    }

    @Nullable
    private static ScheduleWaitCondition fallback(Identifier location) {
        Create.LOGGER.warn("Could not parse waiting condition type: {}", location);
        return null;
    }

    private static void encode(RegistryFriendlyByteBuf buf, ScheduleWaitCondition value) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, buf.registryAccess());
            value.write(view);
            buf.writeNbt(view.buildResult());
        }
    }

    @Nullable
    private static ScheduleWaitCondition decode(RegistryFriendlyByteBuf buf) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            ValueInput view = TagValueInput.create(logging, buf.registryAccess(), buf.readNbt());
            return ScheduleWaitCondition.read(view);
        }
    }

    public abstract MutableComponent getWaitingStatus(Level level, Train train, CompoundTag tag);

}
