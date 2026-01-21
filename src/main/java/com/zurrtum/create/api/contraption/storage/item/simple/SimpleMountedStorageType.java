package com.zurrtum.create.api.contraption.storage.item.simple;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public abstract class SimpleMountedStorageType<T extends SimpleMountedStorage> extends MountedItemStorageType<T> {
    protected SimpleMountedStorageType(MapCodec<T> codec) {
        super(codec);
    }

    @Override
    @Nullable
    public T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        return Optional.ofNullable(be).map(b -> getHandler(level, b)).map(this::createStorage).orElse(null);
    }

    @Nullable
    protected Container getHandler(Level level, BlockEntity be) {
        return ItemHelper.getInventory(level, be.getBlockPos(), null, be, null);
    }

    protected abstract T createStorage(Container handler);

    public static final class Impl extends SimpleMountedStorageType<SimpleMountedStorage> {
        public Impl() {
            super(SimpleMountedStorage.CODEC);
        }

        @Override
        protected SimpleMountedStorage createStorage(Container handler) {
            return new SimpleMountedStorage(this, handler);
        }
    }
}
