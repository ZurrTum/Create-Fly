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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public abstract class ScheduleWaitCondition extends ScheduleDataEntry {
    public static final PacketCodec<RegistryByteBuf, ScheduleWaitCondition> STREAM_CODEC = PacketCodec.ofStatic(
        ScheduleWaitCondition::encode,
        ScheduleWaitCondition::decode
    );

    public ScheduleWaitCondition(Identifier id) {
        super(id);
    }

    public abstract boolean tickCompletion(World level, Train train, NbtCompound context);

    protected void requestStatusToUpdate(NbtCompound context) {
        context.putInt("StatusVersion", context.getInt("StatusVersion", 0) + 1);
    }

    public final void write(WriteView view) {
        view.put("Id", Identifier.CODEC, id);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            NbtWriteView writeView = new NbtWriteView(logging, ((NbtWriteView) view).ops, data);
            writeAdditional(writeView);
            view.put("Data", NbtCompound.CODEC, writeView.getNbt());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DataResult<T> encode(final ScheduleWaitCondition input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Id", input.id, Identifier.CODEC);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            NbtWriteView view = new NbtWriteView(logging, (DynamicOps<NbtElement>) ops, input.data);
            input.writeAdditional(view);
            map.add("Data", view.getNbt(), NbtCompound.CODEC);
        }
        return map.build(empty);
    }

    public static ScheduleWaitCondition read(ReadView view) {
        Identifier location = view.read("Id", Identifier.CODEC).orElse(null);
        ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(location);
        if (condition == null) {
            return fallback(location);
        }
        ReadView data = view.getReadView("Data");
        condition.readAdditional(data);
        condition.data = view.read("Data", NbtCompound.CODEC).orElseGet(NbtCompound::new);
        return condition;
    }

    public static <T> ScheduleWaitCondition decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        Identifier location = Identifier.CODEC.parse(ops, map.get("Id")).result().orElse(null);
        ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(location);
        if (condition == null) {
            return fallback(location);
        }
        condition.data = NbtCompound.CODEC.parse(ops, map.get("Data")).result().orElseGet(NbtCompound::new);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            NbtReadView view = new NbtReadView(logging, new NbtReadContext(ops), condition.data);
            condition.readAdditional(view);
        }
        return condition;
    }

    private static ScheduleWaitCondition fallback(Identifier location) {
        Create.LOGGER.warn("Could not parse waiting condition type: {}", location);
        return null;
    }

    private static void encode(RegistryByteBuf buf, ScheduleWaitCondition value) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, buf.getRegistryManager());
            value.write(view);
            buf.writeNbt(view.getNbt());
        }
    }

    private static ScheduleWaitCondition decode(RegistryByteBuf buf) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleWaitCondition", Create.LOGGER)) {
            ReadView view = NbtReadView.create(logging, buf.getRegistryManager(), buf.readNbt());
            return ScheduleWaitCondition.read(view);
        }
    }

    public abstract MutableText getWaitingStatus(World level, Train train, NbtCompound tag);

}
