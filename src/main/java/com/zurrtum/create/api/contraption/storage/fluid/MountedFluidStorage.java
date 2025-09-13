package com.zurrtum.create.api.contraption.storage.fluid;

import com.mojang.serialization.Codec;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class MountedFluidStorage implements FluidInventory {
    public static final Codec<MountedFluidStorage> CODEC = MountedFluidStorageType.CODEC.dispatch(storage -> storage.type, type -> type.codec);

    @SuppressWarnings("deprecation")
    public static final PacketCodec<RegistryByteBuf, MountedFluidStorage> STREAM_CODEC = PacketCodec.ofStatic(
        (b, t) -> b.encode(RegistryOps.of(NbtOps.INSTANCE, b.getRegistryManager()), CODEC, t),
        b -> b.decode(RegistryOps.of(NbtOps.INSTANCE, b.getRegistryManager()), CODEC)
    );

    public final MountedFluidStorageType<? extends MountedFluidStorage> type;

    protected MountedFluidStorage(MountedFluidStorageType<?> type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Un-mount this storage back into the world. The expected storage type of the target
     * block has already been checked to make sure it matches this storage's type.
     */
    public abstract void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be);
}
