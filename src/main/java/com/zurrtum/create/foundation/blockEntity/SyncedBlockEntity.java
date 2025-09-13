package com.zurrtum.create.foundation.blockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;

import static com.zurrtum.create.Create.LOGGER;

public abstract class SyncedBlockEntity extends BlockEntity {
    public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getReporterContext(), LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, registries);
            writeClient(view);
            return view.getNbt();
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void handleUpdateTag(ReadView view) {
        readClient(view);
    }

    public void onDataPacket(ReadView view) {
        readClient(view);
    }

    // Special handling for client update packets
    public void readClient(ReadView view) {
        readData(view);
    }

    // Special handling for client update packets
    public void writeClient(WriteView view) {
        writeData(view);
    }

    public void sendData() {
        if (world instanceof ServerWorld serverLevel)
            serverLevel.getChunkManager().markForUpdate(getPos());
    }

    public void notifyUpdate() {
        markDirty();
        sendData();
    }

    public RegistryEntryLookup<Block> blockHolderGetter() {
        return world != null ? world.createCommandRegistryWrapper(RegistryKeys.BLOCK) : Registries.BLOCK;
    }

}
