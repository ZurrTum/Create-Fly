package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemPlacementContext;

public class LayeredBlock extends PillarBlock {

    public LayeredBlock(Settings p_55926_) {
        super(p_55926_);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        BlockState placedOn = pContext.getWorld().getBlockState(pContext.getBlockPos().offset(pContext.getSide().getOpposite()));
        if (placedOn.getBlock() == this && (pContext.getPlayer() == null || !pContext.getPlayer().isSneaking()))
            stateForPlacement = stateForPlacement.with(AXIS, placedOn.get(AXIS));
        return stateForPlacement;
    }

}
