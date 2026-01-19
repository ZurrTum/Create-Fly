package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StickerPeripheral extends SyncedPeripheral<StickerBlockEntity> {

    public StickerPeripheral(StickerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction
    public boolean isExtended() {
        return blockEntity.isBlockStateExtended();
    }

    @LuaFunction
    public boolean isAttachedToBlock() {
        return blockEntity.isBlockStateExtended() && blockEntity.isAttachedToBlock();
    }

    @LuaFunction(mainThread = true)
    public boolean extend() {
        BlockState state = blockEntity.getBlockState();
        if (!state.is(AllBlocks.STICKER) || state.getValue(StickerBlock.EXTENDED))
            return false;
        blockEntity.getLevel().setBlock(blockEntity.getBlockPos(), state.setValue(StickerBlock.EXTENDED, true), Block.UPDATE_CLIENTS);
        return true;
    }

    @LuaFunction(mainThread = true)
    public boolean retract() {
        BlockState state = blockEntity.getBlockState();
        if (!state.is(AllBlocks.STICKER) || !state.getValue(StickerBlock.EXTENDED))
            return false;
        blockEntity.getLevel().setBlock(blockEntity.getBlockPos(), state.setValue(StickerBlock.EXTENDED, false), Block.UPDATE_CLIENTS);
        return true;
    }

    @LuaFunction(mainThread = true)
    public boolean toggle() {
        BlockState state = blockEntity.getBlockState();
        if (!state.is(AllBlocks.STICKER))
            return false;
        boolean extended = state.getValue(StickerBlock.EXTENDED);
        blockEntity.getLevel().setBlock(blockEntity.getBlockPos(), state.setValue(StickerBlock.EXTENDED, !extended), Block.UPDATE_CLIENTS);
        return true;
    }

    @Override
    public String getType() {
        return "Create_Sticker";
    }

}
