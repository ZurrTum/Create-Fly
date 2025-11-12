package com.zurrtum.create.api.packager.unpacking;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Interface for custom handling of box unpacking into storage.
 * <p>
 * This interface is <strong>experimental</strong> as it is for a new feature. It may be revised or relocated,
 * but will likely not change very much.
 */
@ApiStatus.Experimental
public interface UnpackingHandler {
    SimpleRegistry<Block, UnpackingHandler> REGISTRY = SimpleRegistry.create();

    /**
     * Unpack the given items into storage.
     *
     * @param items        the list of non-empty item stacks to unpack. May be freely modified
     * @param orderContext the order context, if present
     * @param simulate     true if the unpacking should only be simulated
     * @return true if all items have been unpacked successfully
     */
    boolean unpack(
        Level level,
        BlockPos pos,
        BlockState state,
        Direction side,
        List<ItemStack> items,
        @Nullable PackageOrderWithCrafts orderContext,
        boolean simulate
    );
}
