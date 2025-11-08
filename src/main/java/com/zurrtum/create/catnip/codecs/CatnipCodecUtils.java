package com.zurrtum.create.catnip.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface CatnipCodecUtils {
    // Decode and return Optional<T>
    static <T> Optional<T> decode(Codec<T> codec, NbtElement tag) {
        return decode(codec, NbtOps.INSTANCE, tag);
    }

    static <T> Optional<T> decode(Codec<T> codec, RegistryWrapper.WrapperLookup registries, NbtElement tag) {
        return decode(codec, RegistryOps.of(NbtOps.INSTANCE, registries), tag);
    }

    static <T, S> Optional<T> decode(Codec<T> codec, DynamicOps<S> ops, S s) {
        return codec.decode(ops, s).result().map(Pair::getFirst);
    }

    // Decode and return @Nullable T
    static <T> @Nullable T decodeOrNull(Codec<T> codec, NbtElement tag) {
        return decodeOrNull(codec, NbtOps.INSTANCE, tag);
    }

    static <T> @Nullable T decodeOrNull(Codec<T> codec, RegistryWrapper.WrapperLookup registries, NbtElement tag) {
        return decodeOrNull(codec, RegistryOps.of(NbtOps.INSTANCE, registries), tag);
    }

    static <T, S> @Nullable T decodeOrNull(Codec<T> codec, DynamicOps<S> ops, S s) {
        return codec.decode(ops, s).mapOrElse(Pair::getFirst, e -> null);
    }

    // Encode and return Optional<Tag>
    static <T> Optional<NbtElement> encode(Codec<T> codec, T t) {
        return encode(codec, NbtOps.INSTANCE, t);
    }

    static <T> Optional<NbtElement> encode(Codec<T> codec, RegistryWrapper.WrapperLookup registries, T t) {
        return encode(codec, RegistryOps.of(NbtOps.INSTANCE, registries), t);
    }

    static <T, S> Optional<S> encode(Codec<T> codec, DynamicOps<S> ops, T t) {
        return codec.encodeStart(ops, t).result();
    }
}