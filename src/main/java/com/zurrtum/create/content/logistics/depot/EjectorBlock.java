package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity.State;
import com.zurrtum.create.foundation.block.EntityControlBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorTriggerPacket;
import com.zurrtum.create.infrastructure.packet.s2c.EjectorPlacementRequestPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class EjectorBlock extends HorizontalKineticBlock implements IBE<EjectorBlockEntity>, ProperWaterloggedBlock, SlipperinessControlBlock, EntityControlBlock, ItemInventoryProvider<EjectorBlockEntity> {

    public EjectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    public void setPlacedBy(
        Level level,
        BlockPos pos,
        BlockState state,
        @Nullable LivingEntity by,
        ItemStack itemStack
    ) {
        if (!level.isClientSide() && by instanceof ServerPlayer sp) {
            sp.connection.send(new EjectorPlacementRequestPacket(pos));
        }
        super.setPlacedBy(level, pos, state, by, itemStack);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(WATERLOGGED));
    }

    @Override
    public Container getInventory(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        EjectorBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return blockEntity.depotBehaviour.itemHandler;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return withWater(super.getStateForPlacement(pContext), pContext);
    }

    @Override
    public VoxelShape getShape(
        BlockState p_220053_1_,
        BlockGetter p_220053_2_,
        BlockPos p_220053_3_,
        CollisionContext p_220053_4_
    ) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @Override
    public float getSlipperiness(LevelReader world, BlockPos pos) {
        return getBlockEntityOptional(world, pos).filter(ete -> ete.state == State.LAUNCHING).isPresent() ? 1.0f :
            getFriction();
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable Orientation wireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, EjectorBlockEntity::updateSignal);
    }

    @Override
    public void fallOn(
        Level p_180658_1_,
        BlockState p_152427_,
        BlockPos p_180658_2_,
        Entity p_180658_3_,
        double p_180658_4_
    ) {
        Optional<EjectorBlockEntity> blockEntityOptional = getBlockEntityOptional(p_180658_1_, p_180658_2_);
        if (blockEntityOptional.isPresent() && !p_180658_3_.isSuppressingBounce()) {
            p_180658_3_.causeFallDamage(p_180658_4_, 1.0F, p_180658_1_.damageSources().fall());
            return;
        }
        super.fallOn(p_180658_1_, p_152427_, p_180658_2_, p_180658_3_, p_180658_4_);
    }

    @Override
    public void onEntityMovement(Level level, Entity entity) {
        if (!entity.isAlive() || entity.isSuppressingBounce()) {
            return;
        }
        BlockPos position = entity.blockPosition();
        if (!level.getBlockState(position).is(AllBlocks.WEIGHTED_EJECTOR)) {
            return;
        }
        if (!ItemHelper.fromItemEntity(entity).isEmpty()) {
            SharedDepotBlockMethods.onLanded(level, entity);
            return;
        }
        EjectorBlockEntity blockEntity = getBlockEntity(level, position);
        if (blockEntity == null) {
            return;
        }
        if (blockEntity.getState() == State.RETRACTING || blockEntity.powered || blockEntity.launcher.getHorizontalDistance() == 0) {
            return;
        }
        if (entity.onGround()) {
            entity.setOnGround(false);
            Vec3 center = VecHelper.getCenterOf(position).add(0, 7 / 16.0f, 0);
            Vec3 positionVec = entity.position();
            double diff = center.distanceTo(positionVec);
            entity.setDeltaMovement(0, -0.125, 0);
            Vec3 vec = center.add(positionVec).scale(0.5f);
            if (diff > 4 / 16.0f) {
                entity.setPos(vec.x, vec.y, vec.z);
                return;
            }
        }
        blockEntity.activate();
        blockEntity.notifyUpdate();
        if (level.isClientSide()) {
            AllClientHandle.INSTANCE.sendPacket(new EjectorTriggerPacket(blockEntity.getBlockPos()));
        }
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
        if (stack.is(AllItems.WRENCH)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return SharedDepotBlockMethods.onUse(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getClockWise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return getRotationAxis(state) == face.getAxis();
    }

    @Override
    public Class<EjectorBlockEntity> getBlockEntityClass() {
        return EjectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EjectorBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.WEIGHTED_EJECTOR;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
        return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}