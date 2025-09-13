package com.zurrtum.create.client.flywheel.lib.backend;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.backend.Engine;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;

public final class SimpleBackend implements Backend {
    private final Function<WorldAccess, Engine> engineFactory;
    private final IntSupplier priority;
    private final BooleanSupplier isSupported;

    public SimpleBackend(Function<WorldAccess, Engine> engineFactory, IntSupplier priority, BooleanSupplier isSupported) {
        this.engineFactory = engineFactory;
        this.priority = priority;
        this.isSupported = isSupported;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Engine createEngine(WorldAccess level) {
        return this.engineFactory.apply(level);
    }

    public int priority() {
        return this.priority.getAsInt();
    }

    public boolean isSupported() {
        return this.isSupported.getAsBoolean();
    }

    public static final class Builder {
        private @Nullable Function<WorldAccess, Engine> engineFactory;
        private IntSupplier priority = () -> 0;
        private @Nullable BooleanSupplier isSupported;

        public Builder() {
        }

        public Builder engineFactory(Function<WorldAccess, Engine> engineFactory) {
            this.engineFactory = engineFactory;
            return this;
        }

        public Builder priority(int priority) {
            return this.priority(() -> priority);
        }

        public Builder priority(IntSupplier priority) {
            this.priority = priority;
            return this;
        }

        public Builder supported(BooleanSupplier isSupported) {
            this.isSupported = isSupported;
            return this;
        }

        public Backend register(Identifier id) {
            Objects.requireNonNull(this.engineFactory);
            Objects.requireNonNull(this.isSupported);
            return Backend.REGISTRY.registerAndGet(id, new SimpleBackend(this.engineFactory, this.priority, this.isSupported));
        }
    }
}
