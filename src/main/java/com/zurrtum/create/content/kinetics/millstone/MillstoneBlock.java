package com.zurrtum.create.content.kinetics.millstone;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MillstoneBlock extends KineticBlock implements IBE<MillstoneBlockEntity>, ICogWheel, ItemInventoryProvider<MillstoneBlockEntity> {

    public MillstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Container getInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        MillstoneBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return blockEntity.capability;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.MILLSTONE;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (!stack.isEmpty())
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        withBlockEntityDo(
            level, pos, millstone -> {
                boolean emptyOutput = true;
                Container inv = millstone.capability;
                for (int slot = 1, size = inv.getContainerSize(); slot < size; slot++) {
                    ItemStack stackInSlot = inv.getItem(slot);
                    if (stackInSlot.isEmpty()) {
                        continue;
                    }
                    emptyOutput = false;
                    player.getInventory().placeItemBackInInventory(stackInSlot);
                    inv.setItem(slot, ItemStack.EMPTY);
                }

                if (emptyOutput) {
                    player.getInventory().placeItemBackInInventory(inv.getItem(0));
                    inv.setItem(0, ItemStack.EMPTY);
                }

                millstone.setChanged();
                millstone.sendData();
            }
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);

        if (entityIn.level().isClientSide())
            return;
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;

        MillstoneBlockEntity millstone = null;
        for (BlockPos pos : Iterate.hereAndBelow(entityIn.blockPosition()))
            if (millstone == null)
                millstone = getBlockEntity(worldIn, pos);

        if (millstone == null)
            return;

        Container capability = ItemHelper.getInventory(millstone.getLevel(), millstone.getBlockPos(), millstone.getBlockState(), millstone, null);
        if (capability == null)
            return;

        ItemStack stack = itemEntity.getItem();
        int insert = capability.insert(stack);
        if (insert == stack.getCount()) {
            itemEntity.discard();
        } else if (insert != 0) {
            stack.shrink(insert);
            itemEntity.setItem(stack);
        }
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    @Override
    public Class<MillstoneBlockEntity> getBlockEntityClass() {
        return MillstoneBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MillstoneBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MILLSTONE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}