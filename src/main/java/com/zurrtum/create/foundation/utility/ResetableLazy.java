package com.zurrtum.create.foundation.utility;


import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class ResetableLazy<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private @Nullable T value;

    public ResetableLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

    public void reset() {
        value = null;
    }

    public static <T> ResetableLazy<T> of(Supplier<T> supplier) {
        return new ResetableLazy<>(supplier);
    }

}
