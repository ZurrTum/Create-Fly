package com.zurrtum.create.foundation.blockEntity;

import static com.zurrtum.create.Create.LOGGER;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class SyncedBlockEntity extends BlockEntity {
    public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, registries);
            writeClient(view);
            return view.buildResult();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void handleUpdateTag(ValueInput view) {
        readClient(view);
    }

    public void onDataPacket(ValueInput view) {
        readClient(view);
    }

    // Special handling for client update packets
    public void readClient(ValueInput view) {
        loadAdditional(view);
    }

    // Special handling for client update packets
    public void writeClient(ValueOutput view) {
        saveAdditional(view);
    }

    public void sendData() {
        if (level instanceof ServerLevel serverLevel)
            serverLevel.getChunkSource().blockChanged(getBlockPos());
    }

    public void notifyUpdate() {
        setChanged();
        sendData();
    }

    public HolderGetter<Block> blockHolderGetter() {
        return level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK;
    }

}
