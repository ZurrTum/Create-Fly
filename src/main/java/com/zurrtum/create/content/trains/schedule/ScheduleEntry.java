package com.zurrtum.create.content.trains.schedule;

import com.mojang.serialization.*;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.zurrtum.create.content.trains.schedule.destination.ScheduleInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ScheduleEntry {
    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleEntry> STREAM_CODEC = StreamCodec.composite(
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

    public ScheduleEntry clone(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleEntry", Create.LOGGER)) {
            TagValueOutput writeView = TagValueOutput.createWithContext(logging, registries);
            write(writeView);
            ValueInput readView = TagValueInput.create(logging, registries, writeView.buildResult());
            return read(readView);
        }
    }

    public void write(ValueOutput view) {
        instruction.write(view.child("Instruction"));
        if (!instruction.supportsConditions())
            return;
        ValueOutput.ValueOutputList outer = view.childrenList("Conditions");
        conditions.forEach(column -> {
            ValueOutput.ValueOutputList list = outer.addChild().childrenList("Column");
            column.forEach(condition -> {
                condition.write(list.addChild());
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

    public static ScheduleEntry read(ValueInput view) {
        ScheduleEntry entry = new ScheduleEntry();
        entry.instruction = ScheduleInstruction.read(view.childOrEmpty("Instruction"));
        entry.conditions = new ArrayList<>();
        if (entry.instruction.supportsConditions()) {
            view.childrenListOrEmpty("Conditions")
                .forEach(column -> entry.conditions.add(column.childrenListOrEmpty("Column").stream().map(ScheduleWaitCondition::read)
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
