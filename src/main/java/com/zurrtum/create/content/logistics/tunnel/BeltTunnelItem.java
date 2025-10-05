package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BeltTunnelItem extends BlockItem {

    public BeltTunnelItem(Block p_i48527_1_, Settings p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext ctx, BlockState state) {
        PlayerEntity playerentity = ctx.getPlayer();
        ShapeContext iselectioncontext = playerentity == null ? ShapeContext.absent() : ShapeContext.of(playerentity);
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return (!checkStatePlacement() || AllBlocks.ANDESITE_TUNNEL.isValidPositionForPlacement(state, world, pos)) && world.canPlace(
            state,
            pos,
            iselectioncontext
        );
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, PlayerEntity p_195943_3_, ItemStack p_195943_4_, BlockState state) {
        boolean flag = super.postPlacement(pos, world, p_195943_3_, p_195943_4_, state);
        if (!world.isClient()) {
            BeltBlockEntity belt = BeltHelper.getSegmentBE(world, pos.down());
            if (belt != null && belt.casing == CasingType.NONE)
                belt.setCasingType(state.isOf(AllBlocks.ANDESITE_TUNNEL) ? CasingType.ANDESITE : CasingType.BRASS);
        }
        return flag;
    }

}
