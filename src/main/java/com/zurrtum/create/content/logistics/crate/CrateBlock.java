package com.zurrtum.create.content.logistics.crate;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;

public class CrateBlock extends WrenchableDirectionalBlock implements IWrenchable {

    public static final MapCodec<CrateBlock> CODEC = createCodec(CrateBlock::new);

    public CrateBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.CRATE_BLOCK_SHAPE;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    protected @NotNull MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }
}
