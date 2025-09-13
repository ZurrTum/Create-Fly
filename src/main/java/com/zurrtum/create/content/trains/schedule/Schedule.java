package com.zurrtum.create.content.trains.schedule;

import com.mojang.serialization.*;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Schedule {
    public static final PacketCodec<RegistryByteBuf, Schedule> STREAM_CODEC = PacketCodec.tuple(
        CatnipStreamCodecBuilders.list(ScheduleEntry.STREAM_CODEC),
        schedule -> schedule.entries,
        PacketCodecs.BOOLEAN,
        schedule -> schedule.cyclic,
        PacketCodecs.VAR_INT,
        schedule -> schedule.savedProgress,
        Schedule::new
    );

    public List<ScheduleEntry> entries;
    public boolean cyclic;
    public int savedProgress;

    public Schedule() {
        this(new ArrayList<>(), true, 0);
    }

    public Schedule(List<ScheduleEntry> entries, boolean cyclic, int savedProgress) {
        this.entries = entries;
        this.cyclic = cyclic;
        this.savedProgress = savedProgress;
    }

    public void write(WriteView view) {
        WriteView.ListView list = view.getList("Entries");
        entries.forEach(entry -> entry.write(list.add()));
        view.putBoolean("Cyclic", cyclic);
        if (savedProgress > 0)
            view.putInt("Progress", savedProgress);
    }

    public static <T> DataResult<T> encode(final Schedule input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        ListBuilder<T> list = ops.listBuilder();
        input.entries.forEach(entry -> list.add(ScheduleEntry.encode(entry, ops, empty)));
        map.add("Entries", list.build(empty));
        map.add("Cyclic", ops.createBoolean(input.cyclic));
        if (input.savedProgress > 0)
            map.add("Progress", ops.createInt(input.savedProgress));
        return map.build(empty);
    }

    public static Schedule read(ReadView view) {
        Schedule schedule = new Schedule();
        schedule.entries = view.getListReadView("Entries").stream().map(ScheduleEntry::read).collect(Collectors.toList());
        schedule.cyclic = view.getBoolean("Cyclic", false);
        view.getOptionalInt("Progress").ifPresent(value -> schedule.savedProgress = value);
        return schedule;
    }

    public static <T> Schedule decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        Schedule schedule = new Schedule();
        schedule.entries = ops.getStream(map.get("Entries"))
            .mapOrElse(stream -> stream.map(entry -> ScheduleEntry.decode(ops, entry)).collect(Collectors.toList()), e -> new ArrayList<>());
        schedule.cyclic = ops.getBooleanValue(map.get("Cyclic")).result().orElse(false);
        Optional.ofNullable(map.get("Progress")).ifPresent(value -> schedule.savedProgress = ops.getNumberValue(value).getOrThrow().intValue());
        return schedule;
    }

}
