package com.zurrtum.create.content.logistics.crate;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class CreativeCrateBlock extends CrateBlock implements IBE<CreativeCrateBlockEntity>, ItemInventoryProvider<CreativeCrateBlockEntity> {

    public CreativeCrateBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, CreativeCrateBlockEntity blockEntity, Direction context) {
        return blockEntity.inv;
    }

    @Override
    public Class<CreativeCrateBlockEntity> getBlockEntityClass() {
        return CreativeCrateBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeCrateBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CREATIVE_CRATE;
    }
}
