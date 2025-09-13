package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChuteBlock extends Block implements IWrenchable, IBE<ChuteBlockEntity>, ItemInventoryProvider<ChuteBlockEntity>, NeighborUpdateListeningBlock {

    public AbstractChuteBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, ChuteBlockEntity blockEntity, Direction context) {
        return blockEntity.itemHandler;
    }

    public static boolean isChute(BlockState state) {
        return state.getBlock() instanceof AbstractChuteBlock;
    }

    public static boolean isOpenChute(BlockState state) {
        return isChute(state) && ((AbstractChuteBlock) state.getBlock()).isOpen(state);
    }

    public static boolean isTransparentChute(BlockState state) {
        return isChute(state) && ((AbstractChuteBlock) state.getBlock()).isTransparent(state);
    }

    @Nullable
    public static Direction getChuteFacing(BlockState state) {
        return !isChute(state) ? null : ((AbstractChuteBlock) state.getBlock()).getFacing(state);
    }

    public Direction getFacing(BlockState state) {
        return Direction.DOWN;
    }

    public boolean isOpen(BlockState state) {
        return true;
    }

    public boolean isTransparent(BlockState state) {
        return false;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        ItemStack stack = ItemHelper.fromItemEntity(entityIn);
        if (stack.isEmpty())
            return;
        if (entityIn.getWorld().isClient)
            return;
        if (!entityIn.isAlive())
            return;
        BlockPos pos = BlockPos.ofFloored(entityIn.getPos().add(0, 0.5f, 0)).down();
        DirectBeltInputBehaviour input = BlockEntityBehaviour.get(entityIn.getWorld(), pos, DirectBeltInputBehaviour.TYPE);
        if (input == null)
            return;
        if (!input.canInsertFromSide(Direction.UP))
            return;
        if (!PackageEntity.centerPackage(entityIn, Vec3d.ofBottomCenter(pos.up())))
            return;
        ItemStack remainder = input.handleInsertion(stack, Direction.UP, false);
        if (remainder.isEmpty()) {
            entityIn.discard();
            if (entityIn instanceof PackageEntity box) {
                PlayerEntity player = box.tossedBy.get();
                if (player instanceof ServerPlayerEntity serverPlayer)
                    AllAdvancements.PACKAGE_CHUTE_THROW.trigger(serverPlayer);
            }
        } else if (remainder.getCount() < stack.getCount() && entityIn instanceof ItemEntity itemEntity)
            itemEntity.setStack(remainder);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        withBlockEntityDo(world, pos, ChuteBlockEntity::onAdded);
        updateDiagonalNeighbour(state, world, pos);
    }

    protected void updateDiagonalNeighbour(BlockState state, World world, BlockPos pos) {
        if (!isChute(state))
            return;
        AbstractChuteBlock block = (AbstractChuteBlock) state.getBlock();
        Direction facing = block.getFacing(state);
        BlockPos toUpdate = pos.down();
        if (facing.getAxis().isHorizontal())
            toUpdate = toUpdate.offset(facing.getOpposite());

        BlockState stateToUpdate = world.getBlockState(toUpdate);
        if (isChute(stateToUpdate) && !world.getBlockTickScheduler().isQueued(toUpdate, stateToUpdate.getBlock()))
            world.scheduleBlockTick(toUpdate, stateToUpdate.getBlock(), 1);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean isMoving) {
        updateDiagonalNeighbour(state, world, pos);

        for (Direction direction : Iterate.horizontalDirections) {
            BlockPos toUpdate = pos.up().offset(direction);
            BlockState stateToUpdate = world.getBlockState(toUpdate);
            if (isChute(stateToUpdate) && !world.getBlockTickScheduler().isQueued(toUpdate, stateToUpdate.getBlock()))
                world.scheduleBlockTick(toUpdate, stateToUpdate.getBlock(), 1);
        }
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        BlockState updated = updateChuteState(pState, pLevel.getBlockState(pPos.up()), pLevel, pPos);
        if (pState != updated)
            pLevel.setBlockState(pPos, updated);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState above,
        Random random
    ) {
        if (direction != Direction.UP)
            return state;
        return updateChuteState(state, above, world, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos neighbourPos, boolean isMoving) {
        if (pos.down().equals(neighbourPos))
            withBlockEntityDo(world, pos, ChuteBlockEntity::blockBelowChanged);
    }

    public abstract BlockState updateChuteState(BlockState state, BlockState above, BlockView world, BlockPos pos);

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return ChuteShapes.getShape(p_220053_1_);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockView p_220071_2_, BlockPos p_220071_3_, ShapeContext p_220071_4_) {
        return ChuteShapes.getCollisionShape(p_220071_1_);
    }

    @Override
    public Class<ChuteBlockEntity> getBlockEntityClass() {
        return ChuteBlockEntity.class;
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

        return onBlockEntityUseItemOn(
            level, pos, be -> {
                if (be.item.isEmpty())
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                player.getInventory().offerOrDrop(be.item);
                be.setItem(ItemStack.EMPTY);
                return ActionResult.SUCCESS;
            }
        );
    }

}
