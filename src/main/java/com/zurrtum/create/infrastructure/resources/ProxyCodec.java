package com.zurrtum.create.infrastructure.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public record ProxyCodec<A>(Codec<A> codec, InputModify<A> proxy) implements Codec<A> {
    public static <T> Codec<T> create(Codec<T> codec, InputModify<T> modify) {
        return new ProxyCodec<>(codec, modify);
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        return proxy.decode(codec, ops, input);
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        return codec.encode(input, ops, prefix);
    }
}
