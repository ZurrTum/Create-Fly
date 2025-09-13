package com.zurrtum.create.catnip.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Comparator;
import java.util.function.Function;

public class IntAttached<V> extends Pair<Integer, V> {

    protected IntAttached(Integer first, V second) {
        super(first, second);
    }

    public static <V> IntAttached<V> with(int number, V value) {
        return new IntAttached<>(number, value);
    }

    public static <V> IntAttached<V> withZero(V value) {
        return new IntAttached<>(0, value);
    }

    public boolean isZero() {
        return first == 0;
    }

    public boolean exceeds(int value) {
        return first > value;
    }

    public boolean isOrBelowZero() {
        return first <= 0;
    }

    public void increment() {
        first++;
    }

    public void decrement() {
        first--;
    }

    public V getValue() {
        return getSecond();
    }

    public NbtCompound serializeNBT(Function<V, NbtCompound> serializer) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("Item", serializer.apply(getValue()));
        nbt.putInt("Location", getFirst());
        return nbt;
    }

    public static Comparator<? super IntAttached<?>> comparator() {
        return (i1, i2) -> Integer.compare(i2.getFirst(), i1.getFirst());
    }

    public static <T> IntAttached<T> read(NbtCompound nbt, Function<NbtCompound, T> deserializer) {
        return IntAttached.with(nbt.getInt("Location", 0), deserializer.apply(nbt.getCompoundOrEmpty("Item")));
    }

    public static <T> Codec<IntAttached<T>> codec(Codec<T> codec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("first").forGetter(IntAttached::getFirst),
            codec.fieldOf("second").forGetter(IntAttached::getSecond)
        ).apply(instance, IntAttached::new));
    }

    public static <B extends PacketByteBuf, T> PacketCodec<B, IntAttached<T>> streamCodec(PacketCodec<? super B, T> codec) {
        return PacketCodec.tuple(PacketCodecs.INTEGER, Pair::getFirst, codec, Pair::getSecond, IntAttached::new);
    }
}
