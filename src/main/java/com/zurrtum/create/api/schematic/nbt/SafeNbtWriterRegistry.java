package com.zurrtum.create.api.schematic.nbt;

import com.zurrtum.create.api.registry.SimpleRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

/**
 * Registry for safe NBT writers, used for filtering unsafe BlockEntity data out of schematics.
 * <p>
 * This is used to exclude specific tags that would result in exploits, ex. signs that execute commands when clicked.
 * <p>
 * This is provided as an alternative to {@link PartialSafeNBT}.
 */
public class SafeNbtWriterRegistry {
    public static final SimpleRegistry<BlockEntityType<?>, SafeNbtWriter> REGISTRY = SimpleRegistry.create();

    @FunctionalInterface
    public interface SafeNbtWriter {
        /**
         * Write filtered, safe NBT to the given tag. This is always called on the logical server.
         *
         * @param tag the NBT tag to write to
         */
        void writeSafe(BlockEntity be, NbtCompound tag, RegistryWrapper.WrapperLookup registries);
    }

    private SafeNbtWriterRegistry() {
        throw new AssertionError("This class should not be instantiated");
    }
}
