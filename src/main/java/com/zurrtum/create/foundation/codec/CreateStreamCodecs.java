package com.zurrtum.create.foundation.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;

import java.util.Vector;
import java.util.function.BiFunction;

public interface CreateStreamCodecs {
    /**
     * @deprecated Vector should be replaced with list
     */
    @Deprecated(forRemoval = true)
    static <B extends ByteBuf, V> PacketCodec.ResultFunction<B, V, Vector<V>> vector() {
        return codec -> PacketCodecs.collection(Vector::new, codec);
    }

    static <C> PacketCodec<RegistryByteBuf, C> ofLegacyNbtWithRegistries(
        BiFunction<C, RegistryWrapper.WrapperLookup, NbtCompound> writer,
        BiFunction<RegistryWrapper.WrapperLookup, NbtCompound, C> reader
    ) {
        return new PacketCodec<>() {
            @Override
            public C decode(RegistryByteBuf buffer) {
                return reader.apply(buffer.getRegistryManager(), buffer.readNbt());
            }

            @Override
            public void encode(RegistryByteBuf buffer, C value) {
                buffer.writeNbt(writer.apply(value, buffer.getRegistryManager()));
            }
        };
    }

    PacketCodec<PacketByteBuf, byte[]> UNBOUNDED_BYTE_ARRAY = new PacketCodec<>() {
        public byte[] decode(PacketByteBuf buf) {
            return buf.readByteArray();
        }

        public void encode(PacketByteBuf buf, byte[] data) {
            buf.writeByteArray(data);
        }
    };
}
