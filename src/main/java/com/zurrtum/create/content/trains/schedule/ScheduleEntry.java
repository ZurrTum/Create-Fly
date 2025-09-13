package com.zurrtum.create.content.trains.schedule;

import com.mojang.serialization.*;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.zurrtum.create.content.trains.schedule.destination.ScheduleInstruction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleEntry {
    public static final PacketCodec<RegistryByteBuf, ScheduleEntry> STREAM_CODEC = PacketCodec.tuple(
        ScheduleInstruction.STREAM_CODEC,
        entry -> entry.instruction,
        CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.list(ScheduleWaitCondition.STREAM_CODEC)),
        entry -> entry.conditions,
        ScheduleEntry::new
    );

    public ScheduleInstruction instruction;
    public List<List<ScheduleWaitCondition>> conditions;

    public ScheduleEntry() {
        conditions = new ArrayList<>();
    }

    public ScheduleEntry(ScheduleInstruction instruction, List<List<ScheduleWaitCondition>> conditions) {
        this.instruction = instruction;
        this.conditions = conditions;
    }

    public ScheduleEntry clone(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleEntry", Create.LOGGER)) {
            NbtWriteView writeView = NbtWriteView.create(logging, registries);
            write(writeView);
            ReadView readView = NbtReadView.create(logging, registries, writeView.getNbt());
            return read(readView);
        }
    }

    public void write(WriteView view) {
        instruction.write(view.get("Instruction"));
        if (!instruction.supportsConditions())
            return;
        WriteView.ListView outer = view.getList("Conditions");
        conditions.forEach(column -> {
            WriteView.ListView list = outer.add().getList("Column");
            column.forEach(condition -> {
                condition.write(list.add());
            });
        });
    }

    public static <T> DataResult<T> encode(final ScheduleEntry input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Instruction", ScheduleInstruction.encode(input.instruction, ops, empty));
        if (!input.instruction.supportsConditions())
            return map.build(empty);
        ListBuilder<T> outer = ops.listBuilder();
        input.conditions.forEach(column -> {
            ListBuilder<T> list = ops.listBuilder();
            column.forEach(condition -> list.add(ScheduleWaitCondition.encode(condition, ops, empty)));
            outer.add(list.build(empty));
        });
        map.add("Conditions", outer.build(empty));
        return map.build(empty);
    }

    public static ScheduleEntry read(ReadView view) {
        ScheduleEntry entry = new ScheduleEntry();
        entry.instruction = ScheduleInstruction.read(view.getReadView("Instruction"));
        entry.conditions = new ArrayList<>();
        if (entry.instruction.supportsConditions()) {
            view.getListReadView("Conditions")
                .forEach(column -> entry.conditions.add(column.getListReadView("Column").stream().map(ScheduleWaitCondition::read)
                    .collect(Collectors.toList())));
        }
        return entry;
    }

    public static <T> ScheduleEntry decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        ScheduleEntry entry = new ScheduleEntry();
        entry.instruction = ScheduleInstruction.decode(ops, map.get("Instruction"));
        entry.conditions = new ArrayList<>();
        if (entry.instruction.supportsConditions()) {
            ops.getList(map.get("Conditions")).getOrThrow()
                .accept(column -> entry.conditions.add(ops.getStream(column).getOrThrow().map(item -> ScheduleWaitCondition.decode(ops, item))
                    .collect(Collectors.toList())));
        }
        return entry;
    }

}
