package com.zurrtum.create.api.packager;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.logistics.packager.AllInventoryIdentifiers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Interface identifying an inventory spanning multiple block faces.
 * This is used to avoid multiple packagers on the same inventory requesting items from each other.
 */
@FunctionalInterface
public interface InventoryIdentifier {
    SimpleRegistry<Block, Finder> REGISTRY = SimpleRegistry.create();

    /**
     * @return true if the given face is part of the inventory this identifier represents
     */
    boolean contains(BlockFace face);

    /**
     * Get the InventoryIdentifier for the given BlockFace, if present.
     */
    @Nullable
    static InventoryIdentifier get(World level, BlockFace face) {
        BlockState state = level.getBlockState(face.getPos());
        Finder finder = REGISTRY.get(state);
        Finder toQuery = finder != null ? finder : AllInventoryIdentifiers::fallback;
        return toQuery.find(level, state, face);
    }

    /**
     * Interface for finding an InventoryIdentifier for a block.
     */
    @FunctionalInterface
    interface Finder {
        /**
         * Attempt to find the InventoryIdentifier that contains the given BlockFace.
         *
         * @return the found identifier, or null if one isn't present
         */
        @Nullable InventoryIdentifier find(World level, BlockState state, BlockFace face);
    }

    // common identifier implementations.

    record Single(BlockPos pos) implements InventoryIdentifier {
        @Override
        public boolean contains(BlockFace face) {
            return this.pos.equals(face.getPos());
        }
    }

    record Pair(BlockPos first, BlockPos second) implements InventoryIdentifier {
        public Pair(BlockPos first, BlockPos second) {
            if (first.compareTo(second) < 0) {
                this.first = first;
                this.second = second;
            } else {
                this.first = second;
                this.second = first;
            }
        }

        @Override
        public boolean contains(BlockFace face) {
            BlockPos pos = face.getPos();
            return this.first.equals(pos) || this.second.equals(pos);
        }
    }

    record Bounds(BlockBox bounds) implements InventoryIdentifier {
        @Override
        public boolean contains(BlockFace face) {
            return this.bounds.contains(face.getPos());
        }
    }

    record MultiFace(BlockPos pos, Set<Direction> sides) implements InventoryIdentifier {
        @Override
        public boolean contains(BlockFace face) {
            return this.pos.equals(face.getPos()) && this.sides.contains(face.getFace());
        }
    }
}
