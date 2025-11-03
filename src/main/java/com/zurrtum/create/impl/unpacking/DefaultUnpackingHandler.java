package com.zurrtum.create.impl.unpacking;

import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DefaultUnpackingHandler implements UnpackingHandler {
    @Override
    public boolean unpack(
        World level,
        BlockPos pos,
        BlockState state,
        Direction side,
        List<ItemStack> items,
        @Nullable PackageOrderWithCrafts orderContext,
        boolean simulate
    ) {
        BlockEntity targetBE = level.getBlockEntity(pos);
        if (targetBE == null)
            return false;

        Inventory targetInv = ItemHelper.getInventory(level, pos, state, targetBE, side);
        if (targetInv == null)
            return false;

        if (!simulate) {
            targetInv.insert(items);
            return true;
        }
        return targetInv.countSpace(items);
    }
}