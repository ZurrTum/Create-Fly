package com.zurrtum.create.api.contraption.storage.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class MountedItemStorageType<T extends MountedItemStorage> {
    public static final Codec<MountedItemStorageType<?>> CODEC = CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE.byNameCodec();
    public static final SimpleRegistry<Block, MountedItemStorageType<?>> REGISTRY = SimpleRegistry.create();

    public final MapCodec<? extends T> codec;
    public final Holder.Reference<MountedItemStorageType<?>> holder = CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE.createIntrusiveHolder(this);

    protected MountedItemStorageType(MapCodec<? extends T> codec) {
        this.codec = codec;
    }

    public final boolean is(TagKey<MountedItemStorageType<?>> tag) {
        return this.holder.is(tag);
    }

    @Nullable
    public abstract T mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
