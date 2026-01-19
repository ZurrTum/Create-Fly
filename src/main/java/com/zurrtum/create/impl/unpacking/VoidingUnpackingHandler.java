package com.zurrtum.create.impl.unpacking;

import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An {@link UnpackingHandler} that voids inserted items.
 */
public class VoidingUnpackingHandler implements UnpackingHandler {
    @Override
    public boolean unpack(
        Level level,
        BlockPos pos,
        BlockState state,
        Direction side,
        List<ItemStack> items,
        @Nullable PackageOrderWithCrafts orderContext,
        boolean simulate
    ) {
        return true;
    }
}