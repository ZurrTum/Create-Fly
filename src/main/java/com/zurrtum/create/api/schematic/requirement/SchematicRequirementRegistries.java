package com.zurrtum.create.api.schematic.requirement;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Registries for custom schematic requirements for blocks, block entities, and entities. These requirements determine
 * the items that are needed for placement into the world through schematics.
 * <p>
 * This is provided as an alternative to the following interfaces:
 * <ul>
 *     <li>{@link SpecialBlockItemRequirement}</li>
 *     <li>{@link SpecialBlockEntityItemRequirement}</li>
 *     <li>{@link SpecialEntityItemRequirement}</li>
 * </ul>
 */
public class SchematicRequirementRegistries {
    public static final SimpleRegistry<Block, BlockRequirement> BLOCKS = SimpleRegistry.create();
    public static final SimpleRegistry<BlockEntityType<?>, BlockEntityRequirement> BLOCK_ENTITIES = SimpleRegistry.create();
    public static final SimpleRegistry<EntityType<?>, EntityRequirement> ENTITIES = SimpleRegistry.create();

    @FunctionalInterface
    public interface BlockRequirement {
        ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity);
    }

    @FunctionalInterface
    public interface BlockEntityRequirement {
        ItemRequirement getRequiredItems(BlockEntity blockEntity, BlockState state);
    }

    @FunctionalInterface
    public interface EntityRequirement {
        ItemRequirement getRequiredItems(Entity entity);
    }

    private SchematicRequirementRegistries() {
        throw new AssertionError("This class should not be instantiated");
    }
}