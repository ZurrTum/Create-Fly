package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.foundation.placement.PoleHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CopycatStepBlock extends WaterloggedCopycatBlock {

    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public CopycatStepBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(HALF, BlockHalf.BOTTOM).with(FACING, Direction.SOUTH));
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
        if (!player.isSneaking() && player.canModifyBlocks()) {
            IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
            if (helper.matchesItem(stack))
                return helper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
        }

        return super.onUseWithItem(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public boolean isIgnoredConnectivitySide(
        BlockRenderView reader,
        BlockState state,
        Direction face,
        @Nullable BlockPos fromPos,
        @Nullable BlockPos toPos
    ) {
        if (fromPos == null || toPos == null)
            return true;

        BlockState toState = reader.getBlockState(toPos);

        if (!toState.isOf(this))
            return true;

        Direction facing = state.get(FACING);
        BlockPos diff = fromPos.subtract(toPos);
        int coord = facing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ());

        BlockHalf half = state.get(HALF);
        if (half != toState.get(HALF))
            return diff.getY() == 0;

        return facing == toState.get(FACING).getOpposite() && !(coord != 0 && coord != facing.getDirection().offset());
    }

    @Override
    public boolean canConnectTexturesToward(BlockRenderView reader, BlockPos fromPos, BlockPos toPos, BlockState state) {
        Direction facing = state.get(FACING);
        BlockState toState = reader.getBlockState(toPos);
        BlockPos diff = fromPos.subtract(toPos);

        if (fromPos.equals(toPos.offset(facing)))
            return false;
        if (!toState.isOf(this))
            return false;

        if (diff.getY() != 0) {
            return isOccluded(toState, state, diff.getY() > 0 ? Direction.UP : Direction.DOWN);
        }

        if (isOccluded(state, toState, facing))
            return true;

        int coord = facing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ());
        return state.with(WATERLOGGED, false) == toState.with(WATERLOGGED, false) && coord == 0;
    }

    @Override
    public boolean canFaceBeOccluded(BlockState state, Direction face) {
        if (face.getAxis() == Axis.Y)
            return (state.get(HALF) == BlockHalf.TOP) == (face == Direction.UP);
        return state.get(FACING) == face;
    }

    @Override
    public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
        return canFaceBeOccluded(state, face.getOpposite());
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext).with(FACING, pContext.getHorizontalPlayerFacing());
        Direction direction = pContext.getSide();
        if (direction == Direction.UP)
            return stateForPlacement;
        if (direction == Direction.DOWN || (pContext.getHitPos().y - pContext.getBlockPos().getY() > 0.5D))
            return stateForPlacement.with(HALF, BlockHalf.TOP);
        return stateForPlacement;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(HALF, FACING));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        VoxelShaper voxelShaper = pState.get(HALF) == BlockHalf.BOTTOM ? AllShapes.STEP_BOTTOM : AllShapes.STEP_TOP;
        return voxelShaper.get(pState.get(FACING));
    }

    //TODO
    //    @Override
    //    public boolean supportsExternalFaceHiding(BlockState state) {
    //        return true;
    //    }

    //TODO
    //    @Override
    //    public boolean hidesNeighborFace(BlockView level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
    //        if (state.isOf(this) == neighborState.isOf(this) && getMaterial(level, pos).skipRendering(
    //            getMaterial(level, pos.offset(dir)),
    //            dir.getOpposite()
    //        ))
    //            return isOccluded(state, neighborState, dir);
    //        return false;
    //    }

    public static boolean isOccluded(BlockState state, BlockState other, Direction pDirection) {
        state = state.with(WATERLOGGED, false);
        other = other.with(WATERLOGGED, false);

        BlockHalf half = state.get(HALF);
        boolean vertical = pDirection.getAxis() == Axis.Y;
        if (half != other.get(HALF))
            return vertical && (pDirection == Direction.UP) == (half == BlockHalf.TOP);
        if (vertical)
            return false;

        Direction facing = state.get(FACING);
        if (facing.getOpposite() == other.get(FACING) && pDirection == facing)
            return true;
        if (other.get(FACING) != facing)
            return false;
        return pDirection.getAxis() != facing.getAxis();
    }

    @Override
    public BlockState rotate(BlockState pState, BlockRotation pRot) {
        return pState.with(FACING, pRot.rotate(pState.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, BlockMirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.get(FACING)));
    }

    private static class PlacementHelper extends PoleHelper<Direction> {

        public PlacementHelper() {
            super(state -> state.isOf(AllBlocks.COPYCAT_STEP), state -> state.get(FACING).rotateYClockwise().getAxis(), FACING);
        }

        @Override
        public @NotNull Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.COPYCAT_STEP);
        }

        @Override
        public @NotNull PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            PlacementOffset offset = super.getOffset(player, world, state, pos, ray);

            if (offset.isSuccessful())
                offset.withTransform(offset.getTransform().andThen(s -> s.with(HALF, state.get(HALF))));

            return offset;
        }
    }

}