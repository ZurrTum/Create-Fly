package com.zurrtum.create.api.contraption.storage.item.simple;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SimpleMountedStorageType<T extends SimpleMountedStorage> extends MountedItemStorageType<T> {
    protected SimpleMountedStorageType(MapCodec<T> codec) {
        super(codec);
    }

    @Override
    @Nullable
    public T mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        return Optional.ofNullable(be).map(b -> getHandler(level, b)).map(this::createStorage).orElse(null);
    }

    protected Inventory getHandler(World level, BlockEntity be) {
        return ItemHelper.getInventory(level, be.getPos(), null, be, null);
    }

    protected abstract T createStorage(Inventory handler);

    public static final class Impl extends SimpleMountedStorageType<SimpleMountedStorage> {
        public Impl() {
            super(SimpleMountedStorage.CODEC);
        }

        @Override
        protected SimpleMountedStorage createStorage(Inventory handler) {
            return new SimpleMountedStorage(this, handler);
        }
    }
}
