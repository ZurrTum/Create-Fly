package com.zurrtum.create.impl.unpacking;

import com.zurrtum.create.AllUnpackingHandlers;
import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasinUnpackingHandler implements UnpackingHandler {
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
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BasinBlockEntity basin))
            return false;

        basin.itemCapability.disableCheck();

        try {
            return AllUnpackingHandlers.DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);
        } finally {
            basin.itemCapability.enableCheck();
        }
    }
}
