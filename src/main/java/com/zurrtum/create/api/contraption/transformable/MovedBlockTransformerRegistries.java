package com.zurrtum.create.api.contraption.transformable;

import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

/**
 * Registry for custom transformations to apply to blocks after they've been moved by a contraption.
 * These interfaces are alternatives to the {@link TransformableBlock} and {@link TransformableBlockEntity} interfaces.
 */
public class MovedBlockTransformerRegistries {
    public static final SimpleRegistry<Block, BlockTransformer> BLOCK_TRANSFORMERS = SimpleRegistry.create();
    public static final SimpleRegistry<BlockEntityType<?>, BlockEntityTransformer> BLOCK_ENTITY_TRANSFORMERS = SimpleRegistry.create();

    @FunctionalInterface
    public interface BlockTransformer {
        BlockState transform(BlockState state, StructureTransform transform);
    }

    @FunctionalInterface
    public interface BlockEntityTransformer {
        void transform(BlockEntity be, StructureTransform transform);
    }

    private MovedBlockTransformerRegistries() {
        throw new AssertionError("This class should not be instantiated");
    }
}
