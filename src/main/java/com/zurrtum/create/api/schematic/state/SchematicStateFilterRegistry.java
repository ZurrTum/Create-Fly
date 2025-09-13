package com.zurrtum.create.api.schematic.state;

import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for schematic state filters, used for filtering states out of schematics.
 * <p>
 * This is used to exclude correct tags for blocks that have their NBT tags stripped, like chiseled bookshelves.
 * <p>
 * This is provided as an alternative to {@link SchematicStateFilter}.
 */
public class SchematicStateFilterRegistry {
    public static final SimpleRegistry<Block, StateFilter> REGISTRY = SimpleRegistry.create();

    @FunctionalInterface
    public interface StateFilter {
        /**
         * Write filtered, state info to the given block. This is always called on the logical server.
         */
        BlockState filterStates(@Nullable BlockEntity be, BlockState state);
    }

    private SchematicStateFilterRegistry() {
        throw new AssertionError("This class should not be instantiated");
    }
}
