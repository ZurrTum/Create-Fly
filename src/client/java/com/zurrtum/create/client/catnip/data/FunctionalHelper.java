package com.zurrtum.create.client.catnip.data;

import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class FunctionalHelper {

    public static <U> Function<Object, @Nullable U> filterAndCast(Class<? extends U> clazz) {
        return t -> clazz.isInstance(t) ? clazz.cast(t) : null;
    }

}