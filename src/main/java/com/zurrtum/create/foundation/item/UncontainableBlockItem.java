package com.zurrtum.create.foundation.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class UncontainableBlockItem extends BlockItem {
    public UncontainableBlockItem(Block block, Settings properties) {
        super(block, properties);
    }

    @Override
    public boolean canBeNested() {
        return false;
    }
}
