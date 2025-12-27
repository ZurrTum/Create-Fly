package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChuteBlock extends Block implements IWrenchable, IBE<ChuteBlockEntity>, ItemInventoryProvider<ChuteBlockEntity>, NeighborUpdateListeningBlock {

    public AbstractChuteBlock(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, ChuteBlockEntity blockEntity, Direction context) {
        return blockEntity.itemHandler;
    }

    public static boolean isChute(BlockState state) {
        return state.getBlock() instanceof AbstractChuteBlock;
    }

    public static boolean isOpenChute(BlockState state) {
        return isChute(state) && ((AbstractChuteBlock) state.getBlock()).isOpen(state);
    }

    public static boolean isTransparentChute(BlockState state) {
        return isChute(state) && ((AbstractChuteBlock) state.getBlock()).propagatesSkylightDown(state);
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

    public boolean propagatesSkylightDown(BlockState state) {
        return false;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);
        ItemStack stack = ItemHelper.fromItemEntity(entityIn);
        if (stack.isEmpty())
            return;
        if (entityIn.level().isClientSide())
            return;
        if (!entityIn.isAlive())
            return;
        BlockPos pos = BlockPos.containing(entityIn.position().add(0, 0.5f, 0)).below();
        DirectBeltInputBehaviour input = BlockEntityBehaviour.get(entityIn.level(), pos, DirectBeltInputBehaviour.TYPE);
        if (input == null)
            return;
        if (!input.canInsertFromSide(Direction.UP))
            return;
        if (!PackageEntity.centerPackage(entityIn, Vec3.atBottomCenterOf(pos.above())))
            return;
        ItemStack remainder = input.handleInsertion(stack, Direction.UP, false);
        if (remainder.isEmpty()) {
            entityIn.discard();
            if (entityIn instanceof PackageEntity box) {
                Player player = box.tossedBy.get();
                if (player instanceof ServerPlayer serverPlayer)
                    AllAdvancements.PACKAGE_CHUTE_THROW.trigger(serverPlayer);
            }
        } else if (remainder.getCount() < stack.getCount() && entityIn instanceof ItemEntity itemEntity)
            itemEntity.setItem(remainder);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        withBlockEntityDo(world, pos, ChuteBlockEntity::onAdded);
        updateDiagonalNeighbour(state, world, pos);
    }

    protected void updateDiagonalNeighbour(BlockState state, Level world, BlockPos pos) {
        if (!isChute(state))
            return;
        AbstractChuteBlock block = (AbstractChuteBlock) state.getBlock();
        Direction facing = block.getFacing(state);
        BlockPos toUpdate = pos.below();
        if (facing.getAxis().isHorizontal())
            toUpdate = toUpdate.relative(facing.getOpposite());

        BlockState stateToUpdate = world.getBlockState(toUpdate);
        if (isChute(stateToUpdate) && !world.getBlockTicks().hasScheduledTick(toUpdate, stateToUpdate.getBlock()))
            world.scheduleTick(toUpdate, stateToUpdate.getBlock(), 1);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        updateDiagonalNeighbour(state, world, pos);

        for (Direction direction : Iterate.horizontalDirections) {
            BlockPos toUpdate = pos.above().relative(direction);
            BlockState stateToUpdate = world.getBlockState(toUpdate);
            if (isChute(stateToUpdate) && !world.getBlockTicks().hasScheduledTick(toUpdate, stateToUpdate.getBlock()))
                world.scheduleTick(toUpdate, stateToUpdate.getBlock(), 1);
        }
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockState updated = updateChuteState(pState, pLevel.getBlockState(pPos.above()), pLevel, pPos);
        if (pState != updated)
            pLevel.setBlockAndUpdate(pPos, updated);
    }

    @Override
    public BlockState updateShape(
        BlockState state,
        LevelReader world,
        ScheduledTickAccess tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState above,
        RandomSource random
    ) {
        if (direction != Direction.UP)
            return state;
        return updateChuteState(state, above, world, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos neighbourPos, boolean isMoving) {
        if (pos.below().equals(neighbourPos))
            withBlockEntityDo(world, pos, ChuteBlockEntity::blockBelowChanged);
    }

    public abstract BlockState updateChuteState(BlockState state, BlockState above, BlockGetter world, BlockPos pos);

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return ChuteShapes.getShape(p_220053_1_);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_, CollisionContext p_220071_4_) {
        return ChuteShapes.getCollisionShape(p_220071_1_);
    }

    @Override
    public Class<ChuteBlockEntity> getBlockEntityClass() {
        return ChuteBlockEntity.class;
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

        return onBlockEntityUseItemOn(
            level, pos, be -> {
                if (be.item.isEmpty())
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                player.getInventory().placeItemBackInInventory(be.item);
                be.setItem(ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        );
    }

}
