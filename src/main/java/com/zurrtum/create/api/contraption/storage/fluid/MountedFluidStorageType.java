package com.zurrtum.create.api.contraption.storage.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class MountedFluidStorageType<T extends MountedFluidStorage> {
    public static final Codec<MountedFluidStorageType<?>> CODEC = CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE.getCodec();
    public static final SimpleRegistry<Block, MountedFluidStorageType<?>> REGISTRY = SimpleRegistry.create();

    public final MapCodec<? extends T> codec;

    protected MountedFluidStorageType(MapCodec<? extends T> codec) {
        this.codec = codec;
    }

    @Nullable
    public abstract T mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
