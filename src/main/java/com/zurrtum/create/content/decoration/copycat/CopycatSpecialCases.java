package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.content.decoration.palettes.GlassPaneBlock;
import net.minecraft.block.*;

public class CopycatSpecialCases {

    public static boolean isBarsMaterial(BlockState material) {
        return material.getBlock() instanceof PaneBlock && !(material.getBlock() instanceof GlassPaneBlock) && !(material.getBlock() instanceof StainedGlassPaneBlock) && material.getBlock() != Blocks.GLASS_PANE;
    }

    public static boolean isTrapdoorMaterial(BlockState material) {
        return material.getBlock() instanceof TrapdoorBlock && material.contains(TrapdoorBlock.HALF) && material.contains(TrapdoorBlock.OPEN) && material.contains(
            TrapdoorBlock.FACING);
    }

}
