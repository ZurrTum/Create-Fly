package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.infrastructure.packet.s2c.EjectorPlacementRequestPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EjectorItem extends BlockItem {

    public EjectorItem(Block p_i48527_1_, Settings p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player != null && player.isSneaking())
            return ActionResult.SUCCESS;
        return super.useOnBlock(ctx);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack p_195943_4_, BlockState p_195943_5_) {
        if (!world.isClient() && player instanceof ServerPlayerEntity sp)
            sp.networkHandler.sendPacket(new EjectorPlacementRequestPacket(pos));
        return super.postPlacement(pos, world, player, p_195943_4_, p_195943_5_);
    }

    @Override
    public boolean canMine(ItemStack stack, BlockState state, World world, BlockPos pos, LivingEntity p_195938_4_) {
        return !p_195938_4_.isSneaking();
    }

}
