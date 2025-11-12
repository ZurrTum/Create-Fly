package com.zurrtum.create.content.decoration.encasing;

import com.zurrtum.create.AllBlocks;
import java.util.*;
import net.minecraft.world.level.block.Block;

public class EncasingRegistry {
    private static final Map<Block, List<Block>> ENCASED_VARIANTS = new HashMap<>();

    /**
     * <strong>This method must not be called before block registration is finished.</strong>
     */
    public static <B extends Block & EncasableBlock, E extends Block & EncasedBlock, P> void addVariant(B encasable, E encased) {
        ENCASED_VARIANTS.computeIfAbsent(encasable, b -> new ArrayList<>()).add(encased);
    }

    public static List<Block> getVariants(Block block) {
        return ENCASED_VARIANTS.getOrDefault(block, Collections.emptyList());
    }

    public static void register() {
        EncasingRegistry.addVariant(AllBlocks.SHAFT, AllBlocks.ANDESITE_ENCASED_SHAFT);
        EncasingRegistry.addVariant(AllBlocks.SHAFT, AllBlocks.BRASS_ENCASED_SHAFT);
        EncasingRegistry.addVariant(AllBlocks.COGWHEEL, AllBlocks.ANDESITE_ENCASED_COGWHEEL);
        EncasingRegistry.addVariant(AllBlocks.COGWHEEL, AllBlocks.BRASS_ENCASED_COGWHEEL);
        EncasingRegistry.addVariant(AllBlocks.LARGE_COGWHEEL, AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL);
        EncasingRegistry.addVariant(AllBlocks.LARGE_COGWHEEL, AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL);
        EncasingRegistry.addVariant(AllBlocks.FLUID_PIPE, AllBlocks.ENCASED_FLUID_PIPE);
    }
}
