package com.zurrtum.create.catnip.codecs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import java.util.Set;

public interface CatnipCodecs {
    PrimitiveCodec<Character> CHAR = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Character> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(n -> (char) n.intValue());
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Character value) {
            return ops.createInt(value);
        }

        @Override
        public String toString() {
            return "Char";
        }
    };

    static <E> Codec<Set<E>> set(Codec<E> codec) {
        return codec.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    }
}
