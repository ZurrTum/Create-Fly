package com.zurrtum.create.api.contraption.storage.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class MountedItemStorageType<T extends MountedItemStorage> {
    public static final Codec<MountedItemStorageType<?>> CODEC = CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE.getCodec();
    public static final SimpleRegistry<Block, MountedItemStorageType<?>> REGISTRY = SimpleRegistry.create();

    public final MapCodec<? extends T> codec;
    public final RegistryEntry.Reference<MountedItemStorageType<?>> holder = CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE.createEntry(this);

    protected MountedItemStorageType(MapCodec<? extends T> codec) {
        this.codec = codec;
    }

    public final boolean is(TagKey<MountedItemStorageType<?>> tag) {
        return this.holder.isIn(tag);
    }

    @Nullable
    public abstract T mount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
