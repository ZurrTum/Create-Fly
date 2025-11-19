package com.zurrtum.create.catnip.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.*;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public class Couple<T> extends Pair<T, T> implements Iterable<T> {

    private static final Couple<Boolean> TRUE_AND_FALSE = Couple.create(true, false);

    protected Couple(T first, T second) {
        super(first, second);
    }

    public static <T> Couple<T> create(T first, T second) {
        return new Couple<>(first, second);
    }

    public static <T> Couple<T> create(Supplier<T> factory) {
        return new Couple<>(factory.get(), factory.get());
    }

    public static <T> Couple<T> createWithContext(Function<Boolean, T> factory) {
        return new Couple<>(factory.apply(true), factory.apply(false));
    }

    public static <S> Couple<S> deserializeEach(NbtList list, Function<NbtCompound, S> deserializer) {
        List<S> readCompoundList = NBTHelper.readCompoundList(list, deserializer);
        return new Couple<>(readCompoundList.get(0), readCompoundList.get(1));
    }

    public static <T> Codec<Couple<T>> codec(Codec<T> codec) {
        return new Codec<>() {
            @Override
            public <V> DataResult<com.mojang.datafixers.util.Pair<Couple<T>, V>> decode(DynamicOps<V> ops, V input) {
                return ops.getStream(input).mapOrElse(
                    stream -> {
                        Iterator<V> iterator = stream.iterator();
                        if (!iterator.hasNext()) {
                            return DataResult.error(() -> "size error");
                        }
                        DataResult<T> first = codec.parse(ops, iterator.next());
                        if (first.isError()) {
                            return first.map(i -> null);
                        }
                        if (!iterator.hasNext()) {
                            return DataResult.error(() -> "size error");
                        }
                        DataResult<T> second = codec.parse(ops, iterator.next());
                        if (second.isError()) {
                            return second.map(i -> null);
                        }
                        return DataResult.success(com.mojang.datafixers.util.Pair.of(
                            new Couple<>(first.getOrThrow(), second.getOrThrow()),
                            ops.empty()
                        ));
                    }, e -> e.map(i -> null)
                );
            }

            @Override
            public <V> DataResult<V> encode(Couple<T> input, DynamicOps<V> ops, V prefix) {
                ListBuilder<V> list = ops.listBuilder();
                list.add(input.first, codec);
                list.add(input.second, codec);
                return list.build(prefix);
            }
        };
    }

    public static <T> Codec<Couple<Optional<T>>> optionalCodec(Codec<T> codec) {
        return new Codec<>() {
            @Override
            public <V> DataResult<com.mojang.datafixers.util.Pair<Couple<Optional<T>>, V>> decode(DynamicOps<V> ops, V input) {
                return ops.getMap(input).mapOrElse(
                    map -> {
                        Optional<T> first = Optional.ofNullable(map.get("first")).flatMap(i -> codec.parse(ops, i).result());
                        Optional<T> second = Optional.ofNullable(map.get("second")).flatMap(i -> codec.parse(ops, i).result());
                        return DataResult.success(com.mojang.datafixers.util.Pair.of(new Couple<>(first, second), ops.empty()));
                    }, e -> DataResult.success(com.mojang.datafixers.util.Pair.of(Couple.create(Optional::empty), ops.empty()))
                );
            }

            @Override
            public <V> DataResult<V> encode(Couple<Optional<T>> input, DynamicOps<V> ops, V prefix) {
                RecordBuilder<V> map = ops.mapBuilder();
                input.getFirst().ifPresent(first -> map.add("first", first, codec));
                input.getSecond().ifPresent(second -> map.add("second", second, codec));
                return map.build(prefix);
            }
        };
    }

    public static <B, T> PacketCodec<B, Couple<T>> streamCodec(PacketCodec<? super B, T> codec) {
        return PacketCodec.tuple(codec, Couple::getFirst, codec, Couple::getSecond, Couple::new);
    }

    public T get(boolean first) {
        return first ? getFirst() : getSecond();
    }

    public void set(boolean first, T value) {
        if (first)
            setFirst(value);
        else
            setSecond(value);
    }

    @Override
    public Couple<T> copy() {
        return create(first, second);
    }

    public <S> Couple<S> map(Function<T, S> function) {
        return Couple.create(function.apply(first), function.apply(second));
    }

    public <S> Couple<S> mapNotNull(Function<T, S> function) {
        return Couple.create(first != null ? function.apply(first) : null, second != null ? function.apply(second) : null);
    }

    public <S> Couple<S> mapWithContext(BiFunction<T, Boolean, S> function) {
        return Couple.create(function.apply(first, true), function.apply(second, false));
    }

    public <S, R> Couple<S> mapWithParams(BiFunction<T, R, S> function, Couple<R> values) {
        return Couple.create(function.apply(first, values.first), function.apply(second, values.second));
    }

    public <S, R> Couple<S> mapNotNullWithParam(BiFunction<T, R, S> function, R value) {
        return Couple.create(first != null ? function.apply(first, value) : null, second != null ? function.apply(second, value) : null);
    }

    public boolean both(Predicate<T> test) {
        return test.test(getFirst()) && test.test(getSecond());
    }

    public boolean either(Predicate<T> test) {
        return test.test(getFirst()) || test.test(getSecond());
    }

    public void replace(Function<T, T> function) {
        setFirst(function.apply(getFirst()));
        setSecond(function.apply(getSecond()));
    }

    public void replaceWithContext(BiFunction<T, Boolean, T> function) {
        replaceWithParams(function, TRUE_AND_FALSE);
    }

    public <S> void replaceWithParams(BiFunction<T, S, T> function, Couple<S> values) {
        setFirst(function.apply(getFirst(), values.getFirst()));
        setSecond(function.apply(getSecond(), values.getSecond()));
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
        consumer.accept(getFirst());
        consumer.accept(getSecond());
    }

    public void forEachWithContext(BiConsumer<T, Boolean> consumer) {
        forEachWithParams(consumer, TRUE_AND_FALSE);
    }

    public <S> void forEachWithParams(BiConsumer<T, S> function, Couple<S> values) {
        function.accept(getFirst(), values.getFirst());
        function.accept(getSecond(), values.getSecond());
    }

    public Couple<T> swap() {
        return Couple.create(second, first);
    }

    public NbtList serializeEach(Function<T, NbtCompound> serializer) {
        return NBTHelper.writeCompoundList(ImmutableList.of(first, second), serializer);
    }

    @Override
    @NotNull
    public Iterator<T> iterator() {
        return new Couplerator<>(this);
    }

    public Stream<T> stream() {
        return Stream.of(first, second);
    }

    private static class Couplerator<T> implements Iterator<T> {

        private final Couple<T> couple;
        int state;

        public Couplerator(Couple<T> couple) {
            this.couple = couple;
            state = 0;
        }

        @Override
        public boolean hasNext() {
            return state != 2;
        }

        @Override
        public T next() {
            state++;
            if (state == 1)
                return couple.first;
            if (state == 2)
                return couple.second;
            return null;
        }

    }

}