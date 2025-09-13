package com.zurrtum.create.content.decoration;

import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.function.Predicate;

public class MetalLadderBlock extends LadderBlock implements IWrenchable {

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public MetalLadderBlock(Settings p_54345_) {
        super(p_54345_);
    }

    //TODO
    //    public boolean supportsExternalFaceHiding(BlockState state) {
    //        return false;
    //    }

    @Override
    public boolean isSideInvisible(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
        if (pDirection != null && pDirection.getAxis().isHorizontal())
            return pAdjacentBlockState.isAir() || !pAdjacentBlockState.blocksMovement();
        return pDirection == Direction.UP && pAdjacentBlockState.getBlock() instanceof LadderBlock;
    }

    @Override
    public VoxelShape getCullingShape(BlockState pState) {
        return AllShapes.SIX_VOXEL_POLE.get(Axis.Y);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        WorldView pLevel,
        ScheduledTickView tickView,
        BlockPos pCurrentPos,
        Direction pFacing,
        BlockPos pFacingPos,
        BlockState pFacingState,
        Random random
    ) {
        if (!pState.canPlaceAt(pLevel, pCurrentPos))
            return Blocks.AIR.getDefaultState();
        return super.getStateForNeighborUpdate(pState, pLevel, tickView, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        BlockState otherState = pLevel.getBlockState(pPos.offset(Direction.UP));
        return super.canPlaceAt(pState, pLevel, pPos) || (otherState.isOf(this) && pState.get(FACING).equals(otherState.get(FACING)));
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
        if (player.isSneaking() || !player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    private static class PlacementHelper implements IPlacementHelper {

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i.getItem() instanceof BlockItem item && item.getBlock() instanceof MetalLadderBlock;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof LadderBlock;
        }

        public int attachedLadders(World world, BlockPos pos, Direction direction) {
            BlockPos checkPos = pos.offset(direction);
            BlockState state = world.getBlockState(checkPos);
            int count = 0;
            while (getStatePredicate().test(state)) {
                count++;
                checkPos = checkPos.offset(direction);
                state = world.getBlockState(checkPos);
            }
            return count;
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            Direction dir = player.getPitch() < 0 ? Direction.UP : Direction.DOWN;

            int range = AllConfigs.server().equipment.placementAssistRange.get();
            if (player != null) {
                EntityAttributeInstance reach = player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
                if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier.id()))
                    range += 4;
            }

            int ladders = attachedLadders(world, pos, dir);
            if (ladders >= range)
                return PlacementOffset.fail();

            BlockPos newPos = pos.offset(dir, ladders + 1);
            BlockState newState = world.getBlockState(newPos);

            if (!state.canPlaceAt(world, newPos))
                return PlacementOffset.fail();

            if (newState.isReplaceable())
                return PlacementOffset.success(newPos, bState -> bState.with(FACING, state.get(FACING)));
            return PlacementOffset.fail();
        }

    }

}
