package com.zurrtum.create.api.contraption.storage.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class MountedFluidStorageType<T extends MountedFluidStorage> {
    public static final Codec<MountedFluidStorageType<?>> CODEC = CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE.byNameCodec();
    public static final SimpleRegistry<Block, MountedFluidStorageType<?>> REGISTRY = SimpleRegistry.create();

    public final MapCodec<? extends T> codec;

    protected MountedFluidStorageType(MapCodec<? extends T> codec) {
        this.codec = codec;
    }

    @Nullable
    public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
