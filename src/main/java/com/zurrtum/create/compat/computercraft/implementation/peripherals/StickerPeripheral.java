package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

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
		BlockState state = blockEntity.getCachedState();
		if (!state.isOf(AllBlocks.STICKER) || state.get(StickerBlock.EXTENDED))
			return false;
		blockEntity.getWorld().setBlockState(
				blockEntity.getPos(), state.with(StickerBlock.EXTENDED, true), Block.NOTIFY_LISTENERS);
		return true;
	}

	@LuaFunction(mainThread = true)
	public boolean retract() {
		BlockState state = blockEntity.getCachedState();
		if (!state.isOf(AllBlocks.STICKER) || !state.get(StickerBlock.EXTENDED))
			return false;
		blockEntity.getWorld().setBlockState(
				blockEntity.getPos(), state.with(StickerBlock.EXTENDED, false), Block.NOTIFY_LISTENERS);
		return true;
	}

	@LuaFunction(mainThread = true)
	public boolean toggle() {
		BlockState state = blockEntity.getCachedState();
		if (!state.isOf(AllBlocks.STICKER))
			return false;
		boolean extended = state.get(StickerBlock.EXTENDED);
		blockEntity.getWorld().setBlockState(
				blockEntity.getPos(), state.with(StickerBlock.EXTENDED, !extended), Block.NOTIFY_LISTENERS);
		return true;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_Sticker";
	}

}
