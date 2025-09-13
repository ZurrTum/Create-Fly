package com.zurrtum.create.content.kinetics.speedController;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class SpeedControllerBlock extends HorizontalAxisKineticBlock implements IBE<SpeedControllerBlockEntity>, NeighborUpdateListeningBlock {

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public SpeedControllerBlock(Settings properties) {
        super(properties);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState above = context.getWorld().getBlockState(context.getBlockPos().up());
        if (ICogWheel.isLargeCog(above) && above.get(CogWheelBlock.AXIS).isHorizontal())
            return getDefaultState().with(HORIZONTAL_AXIS, above.get(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X);
        return super.getPlacementState(context);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos neighborPos, boolean isMoving) {
        if (neighborPos.equals(pos.up()))
            withBlockEntityDo(world, pos, SpeedControllerBlockEntity::updateBracket);
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
        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return AllShapes.SPEED_CONTROLLER;
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.isOf(AllBlocks.ROTATION_SPEED_CONTROLLER);
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            BlockPos newPos = pos.up();
            if (!world.getBlockState(newPos).isReplaceable())
                return PlacementOffset.fail();

            Axis newAxis = state.get(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

            if (!CogWheelBlock.isValidCogwheelPosition(true, world, newPos, newAxis))
                return PlacementOffset.fail();

            return PlacementOffset.success(newPos, s -> s.with(CogWheelBlock.AXIS, newAxis));
        }
    }

    @Override
    public Class<SpeedControllerBlockEntity> getBlockEntityClass() {
        return SpeedControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SpeedControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER;
    }
}
