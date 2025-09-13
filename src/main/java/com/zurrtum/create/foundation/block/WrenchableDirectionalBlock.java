package com.zurrtum.create.foundation.block;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class WrenchableDirectionalBlock extends FacingBlock implements IWrenchable {

    public static final MapCodec<WrenchableDirectionalBlock> CODEC = createCodec(WrenchableDirectionalBlock::new);

    public WrenchableDirectionalBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        Direction facing = originalState.get(FACING);

        if (facing.getAxis() == targetedFace.getAxis())
            return originalState;

        Direction newFacing = facing.rotateClockwise(targetedFace.getAxis());

        return originalState.with(FACING, newFacing);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getPlayerLookDirection());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Override
    protected @NotNull MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }

}
