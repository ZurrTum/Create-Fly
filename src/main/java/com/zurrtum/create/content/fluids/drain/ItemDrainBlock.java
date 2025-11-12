package com.zurrtum.create.content.fluids.drain;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ItemDrainBlock extends Block implements IWrenchable, IBE<ItemDrainBlockEntity>, ItemInventoryProvider<ItemDrainBlockEntity>, FluidInventoryProvider<ItemDrainBlockEntity> {

    public ItemDrainBlock(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, ItemDrainBlockEntity blockEntity, Direction context) {
        if (context != null && context.getAxis().isHorizontal())
            return blockEntity.itemHandlers.get(context);
        return null;
    }

    @Override
    public FluidInventory getFluidInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        ItemDrainBlockEntity blockEntity,
        Direction context
    ) {
        return blockEntity.internalTank.getCapability();
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
        if (stack.getItem() instanceof BlockItem && !FluidHelper.hasFluidInventory(stack))
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        return onBlockEntityUseItemOn(
            level, pos, be -> {
                if (!stack.isEmpty()) {
                    be.internalTank.allowInsertion();
                    InteractionResult tryExchange = tryExchange(level, player, hand, stack, be);
                    be.internalTank.forbidInsertion();
                    if (tryExchange.consumesAction())
                        return tryExchange;
                }

                ItemStack heldItemStack = be.getHeldItemStack();
                if (!level.isClientSide() && !heldItemStack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(heldItemStack);
                    be.heldItem = null;
                    be.notifyUpdate();
                }
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;
        if (entityIn.level().isClientSide())
            return;

        DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(worldIn, entityIn.blockPosition(), DirectBeltInputBehaviour.TYPE);
        if (inputBehaviour == null)
            return;
        Vec3 deltaMovement = entityIn.getDeltaMovement().multiply(1, 0, 1).normalize();
        Direction nearest = Direction.getApproximateNearest(deltaMovement.x, deltaMovement.y, deltaMovement.z);
        ItemStack remainder = inputBehaviour.handleInsertion(itemEntity.getItem(), nearest, false);
        itemEntity.setItem(remainder);
        if (remainder.isEmpty())
            itemEntity.discard();
    }

    protected InteractionResult tryExchange(Level worldIn, Player player, InteractionHand handIn, ItemStack heldItem, ItemDrainBlockEntity be) {
        if (FluidHelper.tryEmptyItemIntoBE(worldIn, player, handIn, heldItem, be))
            return InteractionResult.SUCCESS;
        if (GenericItemEmptying.canItemBeEmptied(worldIn, heldItem))
            return InteractionResult.SUCCESS;
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @Override
    public Class<ItemDrainBlockEntity> getBlockEntityClass() {
        return ItemDrainBlockEntity.class;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public BlockEntityType<? extends ItemDrainBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ITEM_DRAIN;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
        return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}
