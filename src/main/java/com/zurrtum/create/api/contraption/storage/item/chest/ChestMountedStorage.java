package com.zurrtum.create.api.contraption.storage.item.chest;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorage;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Mounted storage that handles opening a combined GUI for double chests.
 */
public class ChestMountedStorage extends SimpleMountedStorage {
    public static final MapCodec<ChestMountedStorage> CODEC = SimpleMountedStorage.codec(ChestMountedStorage::new);

    protected ChestMountedStorage(MountedItemStorageType<?> type, Inventory handler) {
        super(type, handler);
    }

    public ChestMountedStorage(Inventory handler) {
        this(AllMountedStorageTypes.CHEST, handler);
    }

    @Override
    public void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        // the capability will include both sides of chests, but mounted storage is 1:1
        if (be instanceof Inventory container) {
            int size = size();
            if (size == container.size()) {
                for (int i = 0; i < size; i++) {
                    container.setStack(i, getStack(i));
                }
            }
        }
    }

    @Override
    protected Inventory getHandlerForMenu(StructureBlockInfo info, Contraption contraption) {
        BlockState state = info.state();
        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        if (type == ChestType.SINGLE)
            return this;

        Direction facing = state.get(ChestBlock.FACING);
        Direction connectedDirection = ChestBlock.getFacing(state);
        BlockPos otherHalfPos = info.pos().offset(connectedDirection);

        MountedItemStorage otherHalf = this.getOtherHalf(contraption, otherHalfPos, state.getBlock(), facing, type);
        if (otherHalf == null)
            return this;

        if (type == ChestType.RIGHT) {
            return new DoubleInventory(this, otherHalf);
        } else {
            return new DoubleInventory(otherHalf, this);
        }
    }

    @Nullable
    protected MountedItemStorage getOtherHalf(Contraption contraption, BlockPos localPos, Block block, Direction thisFacing, ChestType thisType) {
        StructureBlockInfo info = contraption.getBlocks().get(localPos);
        if (info == null)
            return null;
        BlockState state = info.state();
        if (!state.isOf(block))
            return null;

        Direction facing = state.get(ChestBlock.FACING);
        ChestType type = state.get(ChestBlock.CHEST_TYPE);

        return facing == thisFacing && type == thisType.getOpposite() ? contraption.getStorage().getMountedItems().storages.get(localPos) : null;
    }

    @Override
    protected void playOpeningSound(ServerWorld level, Vec3d pos) {
        level.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.75f, 1f);
    }

    @Override
    protected void playClosingSound(ServerWorld level, Vec3d pos) {
        level.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.75f, 1f);
    }
}
