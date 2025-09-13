package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.infrastructure.packet.s2c.PackagePortPlacementRequestPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PackagePortItem extends BlockItem {

    public PackagePortItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack p_195943_4_, BlockState p_195943_5_) {
        if (!world.isClient && player instanceof ServerPlayerEntity sp)
            sp.networkHandler.sendPacket(new PackagePortPlacementRequestPacket(pos));
        return super.postPlacement(pos, world, player, p_195943_4_, p_195943_5_);
    }

}
