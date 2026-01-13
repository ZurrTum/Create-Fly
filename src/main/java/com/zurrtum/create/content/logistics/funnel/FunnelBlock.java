package com.zurrtum.create.content.logistics.funnel;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public abstract class FunnelBlock extends AbstractDirectionalFunnelBlock {

    public static final BooleanProperty EXTRACTING = BooleanProperty.of("extracting");

    public FunnelBlock(Settings p_i48415_1_) {
        super(p_i48415_1_);
        setDefaultState(getDefaultState().with(EXTRACTING, false));
    }

    public abstract BlockState getEquivalentBeltFunnel(BlockView world, BlockPos pos, BlockState state);

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);

        boolean sneak = context.getPlayer() != null && context.getPlayer().isSneaking();
        state = state.with(EXTRACTING, !sneak);

        for (Direction direction : context.getPlacementDirections()) {
            BlockState blockstate = state.with(FACING, direction.getOpposite());
            if (blockstate.canPlaceAt(context.getWorld(), context.getBlockPos()))
                return blockstate.with(POWERED, state.get(POWERED));
        }

        return state;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(EXTRACTING));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
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
        boolean shouldntInsertItem = stack.isOf(AllItems.MECHANICAL_ARM) || !canInsertIntoFunnel(state);

        if (stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (hitResult.getSide() == getFunnelFacing(state) && !shouldntInsertItem) {
            if (!level.isClient())
                withBlockEntityDo(
                    level, pos, be -> {
                        ItemStack toInsert = stack.copy();
                        ItemStack remainder = tryInsert(level, pos, toInsert, false);
                        if (!ItemStack.areEqual(remainder, toInsert) || remainder.getCount() != stack.getCount())
                            player.setStackInHand(hand, remainder);
                    }
                );
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient())
            world.setBlockState(context.getBlockPos(), state.cycle(EXTRACTING));
        return ActionResult.SUCCESS;
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, EntityCollisionHandler handler, boolean bl) {
        if (worldIn.isClient())
            return;
        ItemStack stack = ItemHelper.fromItemEntity(entityIn);
        if (stack.isEmpty())
            return;
        if (!canInsertIntoFunnel(state))
            return;

        Direction direction = getFunnelFacing(state);
        Vec3d openPos = VecHelper.getCenterOf(pos).add(Vec3d.of(direction.getVector()).multiply(entityIn instanceof ItemEntity ? -.25f : -.125f));
        Vec3d diff = entityIn.getEntityPos().subtract(openPos);
        double projectedDiff = direction.getAxis().choose(diff.x, diff.y, diff.z);
        if (projectedDiff < 0 == (direction.getDirection() == AxisDirection.POSITIVE))
            return;
        float yOffset = direction == Direction.UP ? 0.25f : -0.5f;
        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(worldIn, pos, ServerFilteringBehaviour.TYPE);
        if (filter.test(stack) && !PackageEntity.centerPackage(entityIn, openPos.add(0, yOffset, 0)))
            return;

        ItemStack remainder = tryInsert(worldIn, pos, stack, false);
        if (remainder.isEmpty())
            entityIn.discard();
        if (remainder.getCount() < stack.getCount() && entityIn instanceof ItemEntity)
            ((ItemEntity) entityIn).setStack(remainder);
    }

    protected boolean canInsertIntoFunnel(BlockState state) {
        return !state.get(POWERED) && !state.get(EXTRACTING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return facing == Direction.DOWN ? AllShapes.FUNNEL_CEILING : facing == Direction.UP ? AllShapes.FUNNEL_FLOOR : AllShapes.FUNNEL_WALL.get(
            facing);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext && ((EntityShapeContext) context).getEntity() instanceof ItemEntity && getFacing(state).getAxis()
            .isHorizontal())
            return AllShapes.FUNNEL_COLLISION.get(getFacing(state));
        return getOutlineShape(state, world, pos, context);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState state,
        WorldView world,
        ScheduledTickView tickView,
        BlockPos pos,
        Direction direction,
        BlockPos p_196271_6_,
        BlockState p_196271_3_,
        Random random
    ) {
        updateWater(world, tickView, state, pos);
        if (getFacing(state).getAxis().isVertical() || direction != Direction.DOWN)
            return state;
        BlockState equivalentFunnel = ProperWaterloggedBlock.withWater(world, getEquivalentBeltFunnel(null, null, state), pos);
        if (BeltFunnelBlock.isOnValidBelt(equivalentFunnel, world, pos))
            return equivalentFunnel.with(
                BeltFunnelBlock.SHAPE,
                BeltFunnelBlock.getShapeForPosition(world, pos, getFacing(state), state.get(EXTRACTING))
            );
        return state;
    }

}
