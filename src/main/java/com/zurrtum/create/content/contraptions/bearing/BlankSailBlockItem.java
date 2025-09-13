package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import java.util.Map;

public class BlankSailBlockItem extends BlockItem {
    public BlankSailBlockItem(Block block, Settings properties) {
        super(block, properties);
    }

    @Override
    public Block getBlock() {
        return super.getBlock();
    }

    @Override
    public void appendBlocks(Map<Block, Item> map, Item item) {
        map.put(AllBlocks.SAIL, item);
        map.put(AllBlocks.ORANGE_SAIL, item);
        map.put(AllBlocks.MAGENTA_SAIL, item);
        map.put(AllBlocks.LIGHT_BLUE_SAIL, item);
        map.put(AllBlocks.YELLOW_SAIL, item);
        map.put(AllBlocks.LIME_SAIL, item);
        map.put(AllBlocks.PINK_SAIL, item);
        map.put(AllBlocks.GRAY_SAIL, item);
        map.put(AllBlocks.LIGHT_GRAY_SAIL, item);
        map.put(AllBlocks.CYAN_SAIL, item);
        map.put(AllBlocks.PURPLE_SAIL, item);
        map.put(AllBlocks.BLUE_SAIL, item);
        map.put(AllBlocks.BROWN_SAIL, item);
        map.put(AllBlocks.GREEN_SAIL, item);
        map.put(AllBlocks.RED_SAIL, item);
        map.put(AllBlocks.BLACK_SAIL, item);
    }
}
