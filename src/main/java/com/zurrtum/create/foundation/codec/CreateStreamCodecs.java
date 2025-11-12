package com.zurrtum.create.foundation.codec;

import io.netty.buffer.ByteBuf;

import java.util.Vector;
import java.util.function.BiFunction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface CreateStreamCodecs {
    /**
     * @deprecated Vector should be replaced with list
     */
    @Deprecated(forRemoval = true)
    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, Vector<V>> vector() {
        return codec -> ByteBufCodecs.collection(Vector::new, codec);
    }

    static <C> StreamCodec<RegistryFriendlyByteBuf, C> ofLegacyNbtWithRegistries(
        BiFunction<C, HolderLookup.Provider, CompoundTag> writer,
        BiFunction<HolderLookup.Provider, CompoundTag, C> reader
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(RegistryFriendlyByteBuf buffer) {
                return reader.apply(buffer.registryAccess(), buffer.readNbt());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, C value) {
                buffer.writeNbt(writer.apply(value, buffer.registryAccess()));
            }
        };
    }

    StreamCodec<FriendlyByteBuf, byte[]> UNBOUNDED_BYTE_ARRAY = new StreamCodec<>() {
        public byte[] decode(FriendlyByteBuf buf) {
            return buf.readByteArray();
        }

        public void encode(FriendlyByteBuf buf, byte[] data) {
            buf.writeByteArray(data);
        }
    };
}
