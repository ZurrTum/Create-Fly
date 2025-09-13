package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;
import java.util.function.Supplier;

public enum CartAssembleRailType implements StringIdentifiable {

    REGULAR(Blocks.RAIL),
    POWERED_RAIL(Blocks.POWERED_RAIL),
    DETECTOR_RAIL(Blocks.DETECTOR_RAIL),
    ACTIVATOR_RAIL(Blocks.ACTIVATOR_RAIL),
    CONTROLLER_RAIL(AllBlocks.CONTROLLER_RAIL);

    private final Supplier<Block> railBlockSupplier;
    private final Supplier<Item> railItemSupplier;

    CartAssembleRailType(Block block) {
        this.railBlockSupplier = () -> block;
        this.railItemSupplier = block::asItem;
    }

    public Block getBlock() {
        return railBlockSupplier.get();
    }

    public Item getItem() {
        return railItemSupplier.get();
    }

    public boolean matches(BlockState rail) {
        return rail.getBlock() == railBlockSupplier.get();
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

}
