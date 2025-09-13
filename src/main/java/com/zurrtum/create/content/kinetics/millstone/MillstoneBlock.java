package com.zurrtum.create.content.kinetics.millstone;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class MillstoneBlock extends KineticBlock implements IBE<MillstoneBlockEntity>, ICogWheel, ItemInventoryProvider<MillstoneBlockEntity> {

    public MillstoneBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, MillstoneBlockEntity blockEntity, Direction context) {
        return blockEntity.capability;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.MILLSTONE;
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
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
        if (!stack.isEmpty())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient)
            return ActionResult.SUCCESS;

        withBlockEntityDo(
            level, pos, millstone -> {
                boolean emptyOutput = true;
                Inventory inv = millstone.capability;
                for (int slot = 1, size = inv.size(); slot < size; slot++) {
                    ItemStack stackInSlot = inv.getStack(slot);
                    if (stackInSlot.isEmpty()) {
                        continue;
                    }
                    emptyOutput = false;
                    player.getInventory().offerOrDrop(stackInSlot);
                    inv.setStack(slot, ItemStack.EMPTY);
                }

                if (emptyOutput) {
                    player.getInventory().offerOrDrop(inv.getStack(0));
                    inv.setStack(0, ItemStack.EMPTY);
                }

                millstone.markDirty();
                millstone.sendData();
            }
        );

        return ActionResult.SUCCESS;
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);

        if (entityIn.getWorld().isClient)
            return;
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;

        MillstoneBlockEntity millstone = null;
        for (BlockPos pos : Iterate.hereAndBelow(entityIn.getBlockPos()))
            if (millstone == null)
                millstone = getBlockEntity(worldIn, pos);

        if (millstone == null)
            return;

        Inventory capability = ItemHelper.getInventory(millstone.getWorld(), millstone.getPos(), millstone.getCachedState(), millstone, null);
        if (capability == null)
            return;

        ItemStack stack = itemEntity.getStack();
        int insert = capability.insert(stack);
        if (insert == stack.getCount()) {
            itemEntity.discard();
        } else if (insert != 0) {
            stack.decrement(insert);
            itemEntity.setStack(stack);
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
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}