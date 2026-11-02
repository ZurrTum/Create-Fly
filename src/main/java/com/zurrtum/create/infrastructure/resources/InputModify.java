package com.zurrtum.create.infrastructure.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

@FunctionalInterface
public interface InputModify<A> {
    <T> DataResult<Pair<A, T>> decode(Codec<A> codec, DynamicOps<T> ops, T input);
}
