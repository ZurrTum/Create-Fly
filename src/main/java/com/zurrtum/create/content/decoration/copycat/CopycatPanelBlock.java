package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TrapdoorBlock;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class CopycatPanelBlock extends WaterloggedCopycatBlock {

    public static final EnumProperty<Direction> FACING = Properties.FACING;

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public CopycatPanelBlock(Settings pProperties) {
        super(pProperties);
        setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    public boolean isAcceptedRegardless(BlockState material) {
        return CopycatSpecialCases.isBarsMaterial(material) || CopycatSpecialCases.isTrapdoorMaterial(material);
    }

    @Override
    public BlockState prepareMaterial(
        World pLevel,
        BlockPos pPos,
        BlockState pState,
        PlayerEntity pPlayer,
        Hand pHand,
        BlockHitResult pHit,
        BlockState material
    ) {
        if (!CopycatSpecialCases.isTrapdoorMaterial(material))
            return super.prepareMaterial(pLevel, pPos, pState, pPlayer, pHand, pHit, material);

        Direction panelFacing = pState.get(FACING);
        if (panelFacing == Direction.DOWN)
            material = material.with(TrapdoorBlock.HALF, BlockHalf.TOP);
        if (panelFacing.getAxis() == Axis.Y)
            return material.with(TrapdoorBlock.FACING, pPlayer.getHorizontalFacing()).with(TrapdoorBlock.OPEN, false);

        boolean clickedNearTop = pHit.getPos().y - .5 > pPos.getY();
        return material.with(TrapdoorBlock.OPEN, true).with(TrapdoorBlock.HALF, clickedNearTop ? BlockHalf.TOP : BlockHalf.BOTTOM)
            .with(TrapdoorBlock.FACING, panelFacing);
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
            IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult).placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
                return ActionResult.SUCCESS;
            }
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

        Direction facing = state.get(FACING);
        BlockState toState = reader.getBlockState(toPos);

        if (!toState.isOf(this))
            return facing != face.getOpposite();

        BlockPos diff = fromPos.subtract(toPos);
        int coord = facing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ());
        return facing == toState.get(FACING).getOpposite() && !(coord != 0 && coord == facing.getDirection().offset());
    }

    @Override
    public boolean canConnectTexturesToward(BlockRenderView reader, BlockPos fromPos, BlockPos toPos, BlockState state) {
        Direction facing = state.get(FACING);
        BlockState toState = reader.getBlockState(toPos);

        if (toPos.equals(fromPos.offset(facing)))
            return false;

        BlockPos diff = fromPos.subtract(toPos);
        int coord = facing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ());

        if (!toState.isOf(this))
            return coord != -facing.getDirection().offset();

        if (isOccluded(state, toState, facing))
            return true;
        return toState.with(WATERLOGGED, false) == state.with(WATERLOGGED, false) && coord == 0;
    }

    @Override
    public boolean canFaceBeOccluded(BlockState state, Direction face) {
        return state.get(FACING).getOpposite() == face;
    }

    @Override
    public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
        return canFaceBeOccluded(state, face.getOpposite());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        return stateForPlacement.with(FACING, pContext.getPlayerLookDirection().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACING));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.CASING_3PX.get(pState.get(FACING));
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    //TODO
    //    @Override
    //    public boolean supportsExternalFaceHiding(BlockState state) {
    //        return true;
    //    }

    //TODO
    //    @Override
    //    public boolean hidesNeighborFace(BlockView level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
    //        if (state.isOf(this) == neighborState.isOf(this)) {
    //            if (CopycatSpecialCases.isBarsMaterial(getMaterial(level, pos)) && CopycatSpecialCases.isBarsMaterial(getMaterial(
    //                level,
    //                pos.offset(dir)
    //            )))
    //                return state.get(FACING) == neighborState.get(FACING);
    //            if (getMaterial(level, pos).skipRendering(getMaterial(level, pos.offset(dir)), dir.getOpposite()))
    //                return isOccluded(state, neighborState, dir.getOpposite());
    //        }
    //
    //        return state.get(FACING) == dir.getOpposite() && getMaterial(level, pos).skipRendering(neighborState, dir.getOpposite());
    //    }

    public static boolean isOccluded(BlockState state, BlockState other, Direction pDirection) {
        state = state.with(WATERLOGGED, false);
        other = other.with(WATERLOGGED, false);
        Direction facing = state.get(FACING);
        if (facing.getOpposite() == other.get(FACING) && pDirection == facing)
            return true;
        if (other.get(FACING) != facing)
            return false;
        return pDirection.getAxis() != facing.getAxis();
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.isOf(AllItems.COPYCAT_PANEL);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.isOf(AllBlocks.COPYCAT_PANEL);
        }

        @Override
        public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(
                pos,
                ray.getPos(),
                state.get(FACING).getAxis(),
                dir -> world.getBlockState(pos.offset(dir)).isReplaceable()
            );

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(pos.offset(directions.getFirst()), s -> s.with(FACING, state.get(FACING)));
            }
        }
    }

}
