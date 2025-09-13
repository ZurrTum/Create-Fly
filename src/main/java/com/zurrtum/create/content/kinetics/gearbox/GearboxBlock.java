package com.zurrtum.create.content.kinetics.gearbox;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootWorldContext.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.WorldView;

import java.util.List;

public class GearboxBlock extends RotatedPillarKineticBlock implements IBE<GearboxBlockEntity> {

    public GearboxBlock(Settings properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, Builder builder) {
        if (state.get(AXIS).isVertical())
            return super.getDroppedStacks(state, builder);
        return List.of(new ItemStack(AllItems.VERTICAL_GEARBOX));
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        if (state.get(AXIS).isVertical())
            return super.getPickStack(world, pos, state, includeData);
        return AllItems.VERTICAL_GEARBOX.getDefaultStack();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(AXIS, Axis.Y);
    }

    // IRotate:

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() != state.get(AXIS);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public Class<GearboxBlockEntity> getBlockEntityClass() {
        return GearboxBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GearboxBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.GEARBOX;
    }
}