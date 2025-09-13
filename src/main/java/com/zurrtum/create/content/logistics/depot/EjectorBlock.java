package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity.State;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import com.zurrtum.create.infrastructure.packet.c2s.EjectorTriggerPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EjectorBlock extends HorizontalKineticBlock implements IBE<EjectorBlockEntity>, ProperWaterloggedBlock, SlipperinessControlBlock, ItemInventoryProvider<EjectorBlockEntity> {

    public EjectorBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(WATERLOGGED));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, EjectorBlockEntity blockEntity, Direction context) {
        return blockEntity.depotBehaviour.itemHandler;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pDirection,
        BlockPos pNeighborPos,
        BlockState pNeighborState,
        Random random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return withWater(super.getPlacementState(pContext), pContext);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState p_220053_1_, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.CASING_13PX.get(Direction.UP);
    }

    @Override
    public float getSlipperiness(WorldView world, BlockPos pos) {
        return getBlockEntityOptional(world, pos).filter(ete -> ete.state == State.LAUNCHING).isPresent() ? 1f : super.getSlipperiness();
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block p_220069_4_,
        @Nullable WireOrientation wireOrientation,
        boolean p_220069_6_
    ) {
        withBlockEntityDo(world, pos, EjectorBlockEntity::updateSignal);
    }

    @Override
    public void onLandedUpon(World p_180658_1_, BlockState p_152427_, BlockPos p_180658_2_, Entity p_180658_3_, double p_180658_4_) {
        Optional<EjectorBlockEntity> blockEntityOptional = getBlockEntityOptional(p_180658_1_, p_180658_2_);
        if (blockEntityOptional.isPresent() && !p_180658_3_.bypassesLandingEffects()) {
            p_180658_3_.handleFallDamage(p_180658_4_, 1.0F, p_180658_1_.getDamageSources().fall());
            return;
        }
        super.onLandedUpon(p_180658_1_, p_152427_, p_180658_2_, p_180658_3_, p_180658_4_);
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        BlockPos position = entityIn.getBlockPos();
        if (!worldIn.getBlockState(position).isOf(AllBlocks.WEIGHTED_EJECTOR))
            return;
        if (!entityIn.isAlive())
            return;
        if (entityIn.bypassesLandingEffects())
            return;
        if (!ItemHelper.fromItemEntity(entityIn).isEmpty()) {
            SharedDepotBlockMethods.onLanded(worldIn, entityIn);
            return;
        }

        Optional<EjectorBlockEntity> teProvider = getBlockEntityOptional(worldIn, position);
        if (!teProvider.isPresent())
            return;

        EjectorBlockEntity ejectorBlockEntity = teProvider.get();
        if (ejectorBlockEntity.getState() == State.RETRACTING)
            return;
        if (ejectorBlockEntity.powered)
            return;
        if (ejectorBlockEntity.launcher.getHorizontalDistance() == 0)
            return;

        if (entityIn.isOnGround()) {
            entityIn.setOnGround(false);
            Vec3d center = VecHelper.getCenterOf(position).add(0, 7 / 16f, 0);
            Vec3d positionVec = entityIn.getPos();
            double diff = center.distanceTo(positionVec);
            entityIn.setVelocity(0, -0.125, 0);
            Vec3d vec = center.add(positionVec).multiply(.5f);
            if (diff > 4 / 16f) {
                entityIn.setPosition(vec.x, vec.y, vec.z);
                return;
            }
        }

        ejectorBlockEntity.activate();
        ejectorBlockEntity.notifyUpdate();
        if (entityIn.getWorld().isClient)
            AllClientHandle.INSTANCE.sendPacket(new EjectorTriggerPacket(ejectorBlockEntity.getPos()));
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
        if (stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        return SharedDepotBlockMethods.onUse(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.get(HORIZONTAL_FACING).rotateYClockwise().getAxis();
    }

    @Override
    public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
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
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos) {
        return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }
}