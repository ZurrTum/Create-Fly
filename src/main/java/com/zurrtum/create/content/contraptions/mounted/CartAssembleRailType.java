package com.zurrtum.create.content.contraptions.mounted;

import com.zurrtum.create.AllBlocks;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum CartAssembleRailType implements StringRepresentable {

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
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

}
