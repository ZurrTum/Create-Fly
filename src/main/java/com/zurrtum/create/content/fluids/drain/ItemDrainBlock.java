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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ItemDrainBlock extends Block implements IWrenchable, IBE<ItemDrainBlockEntity>, ItemInventoryProvider<ItemDrainBlockEntity>, FluidInventoryProvider<ItemDrainBlockEntity> {

    public ItemDrainBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, ItemDrainBlockEntity blockEntity, Direction context) {
        if (context != null && context.getAxis().isHorizontal())
            return blockEntity.itemHandlers.get(context);
        return null;
    }

    @Override
    public FluidInventory getFluidInventory(WorldAccess world, BlockPos pos, BlockState state, ItemDrainBlockEntity blockEntity, Direction context) {
        return blockEntity.internalTank.getCapability();
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (stack.getItem() instanceof BlockItem && !FluidHelper.hasFluidInventory(stack))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        return onBlockEntityUseItemOn(
            level, pos, be -> {
                if (!stack.isEmpty()) {
                    be.internalTank.allowInsertion();
                    ActionResult tryExchange = tryExchange(level, player, hand, stack, be);
                    be.internalTank.forbidInsertion();
                    if (tryExchange.isAccepted())
                        return tryExchange;
                }

                ItemStack heldItemStack = be.getHeldItemStack();
                if (!level.isClient() && !heldItemStack.isEmpty()) {
                    player.getInventory().offerOrDrop(heldItemStack);
                    be.heldItem = null;
                    be.notifyUpdate();
                }
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;
        if (entityIn.getWorld().isClient())
            return;

        DirectBeltInputBehaviour inputBehaviour = BlockEntityBehaviour.get(worldIn, entityIn.getBlockPos(), DirectBeltInputBehaviour.TYPE);
        if (inputBehaviour == null)
            return;
        Vec3d deltaMovement = entityIn.getVelocity().multiply(1, 0, 1).normalize();
        Direction nearest = Direction.getFacing(deltaMovement.x, deltaMovement.y, deltaMovement.z);
        ItemStack remainder = inputBehaviour.handleInsertion(itemEntity.getStack(), nearest, false);
        itemEntity.setStack(remainder);
        if (remainder.isEmpty())
            itemEntity.discard();
    }

    protected ActionResult tryExchange(World worldIn, PlayerEntity player, Hand handIn, ItemStack heldItem, ItemDrainBlockEntity be) {
        if (FluidHelper.tryEmptyItemIntoBE(worldIn, player, handIn, heldItem, be))
            return ActionResult.SUCCESS;
        if (GenericItemEmptying.canItemBeEmptied(worldIn, heldItem))
            return ActionResult.SUCCESS;
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @Override
    public Class<ItemDrainBlockEntity> getBlockEntityClass() {
        return ItemDrainBlockEntity.class;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public BlockEntityType<? extends ItemDrainBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ITEM_DRAIN;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos) {
        return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}
