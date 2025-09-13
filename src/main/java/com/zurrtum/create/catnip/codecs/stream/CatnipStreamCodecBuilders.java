package com.zurrtum.create.catnip.codecs.stream;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;

public interface CatnipStreamCodecBuilders {
    static <T extends ByteBuf, S extends Enum<S>> PacketCodec<T, S> ofEnum(Class<S> clazz) {
        return new PacketCodec<>() {
            public @NotNull S decode(@NotNull T buffer) {
                return clazz.getEnumConstants()[VarInts.read(buffer)];
            }

            public void encode(@NotNull T buffer, @NotNull S value) {
                VarInts.write(buffer, value.ordinal());
            }
        };
    }

    static <B extends ByteBuf, L, R> PacketCodec<B, Pair<L, R>> pair(PacketCodec<B, L> codecL, PacketCodec<B, R> codecR) {
        return new PacketCodec<>() {
            @Override
            public @NotNull Pair<L, R> decode(B buffer) {
                L l = codecL.decode(buffer);
                R r = codecR.decode(buffer);
                return Pair.of(l, r);
            }

            @Override
            public void encode(B buffer, Pair<L, R> value) {
                codecL.encode(buffer, value.getFirst());
                codecR.encode(buffer, value.getSecond());
            }
        };
    }

    static <B extends PacketByteBuf, V> PacketCodec.ResultFunction<B, V, Optional<V>> optional() {
        return PacketCodecs::optional;
    }

    static <B extends ByteBuf, V> PacketCodec<B, @Nullable V> nullable(PacketCodec<B, V> base) {
        return new PacketCodec<>() {
            @Override
            public @Nullable V decode(@NotNull B buffer) {
                return PacketByteBuf.readNullable(buffer, base);
            }

            @Override
            public void encode(@NotNull B buffer, @Nullable V value) {
                PacketByteBuf.writeNullable(buffer, value, base);
            }
        };
    }

    static <B extends PacketByteBuf, V> PacketCodec.ResultFunction<B, V, @Nullable V> nullable() {
        return CatnipStreamCodecBuilders::nullable;
    }

    static <B extends ByteBuf, V> PacketCodec<B, List<V>> list(PacketCodec<B, V> base) {
        return base.collect(PacketCodecs.toList());
    }

    static <B extends PacketByteBuf, V> PacketCodec<B, List<V>> list(PacketCodec<B, V> base, int maxSize) {
        return base.collect(PacketCodecs.toList(maxSize));
    }

    static <B extends PacketByteBuf, V> PacketCodec.ResultFunction<B, V, DefaultedList<V>> nonNullList() {
        return streamCodec -> PacketCodecs.collection(DefaultedList::ofSize, streamCodec);
    }

    static <B extends PacketByteBuf, V> PacketCodec.ResultFunction<B, V, DefaultedList<V>> nonNullList(int maxSize) {
        return streamCodec -> PacketCodecs.collection(DefaultedList::ofSize, streamCodec, maxSize);
    }

    static <B extends PacketByteBuf, V> PacketCodec<B, DefaultedList<V>> nonNullList(PacketCodec<B, V> base) {
        return base.collect(nonNullList());
    }

    static <B extends PacketByteBuf, V> PacketCodec<B, DefaultedList<V>> nonNullList(PacketCodec<B, V> base, int maxSize) {
        return base.collect(nonNullList(maxSize));
    }

    static <B extends PacketByteBuf, V> PacketCodec<B, V[]> array(PacketCodec<? super B, V> base, Class<?> clazz) {
        return new PacketCodec<>() {
            @Override
            public V @NotNull [] decode(@NotNull B buffer) {
                int size = buffer.readVarInt();
                @SuppressWarnings("unchecked") V[] array = (V[]) Array.newInstance(clazz, size);
                for (int i = 0; i < size; i++) {
                    array[i] = base.decode(buffer);
                }
                return array;
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull V[] value) {
                buffer.writeVarInt(value.length);
                for (V v : value) {
                    base.encode(buffer, v);
                }
            }
        };
    }

    static <B extends PacketByteBuf, V> PacketCodec.ResultFunction<B, V, V[]> array(Class<?> clazz) {
        return streamCodec -> array(streamCodec, clazz);
    }

    static <T> PacketCodec<ByteBuf, TagKey<T>> tagKey(RegistryKey<? extends Registry<T>> registry) {
        return Identifier.PACKET_CODEC.xmap(id -> TagKey.of(registry, id), TagKey::id);
    }
}
