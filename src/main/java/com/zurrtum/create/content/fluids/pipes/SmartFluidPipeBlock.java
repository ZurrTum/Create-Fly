package com.zurrtum.create.content.fluids.pipes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.Nullable;

public class SmartFluidPipeBlock extends FaceAttachedHorizontalDirectionalBlock implements IBE<SmartFluidPipeBlockEntity>, IAxisPipe, IWrenchable, ProperWaterloggedBlock, NeighborUpdateListeningBlock {

    public static final MapCodec<SmartFluidPipeBlock> CODEC = simpleCodec(SmartFluidPipeBlock::new);

    public SmartFluidPipeBlock(Properties p_i48339_1_) {
        super(p_i48339_1_);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState stateForPlacement = super.getStateForPlacement(ctx);
        Axis prefferedAxis = null;
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        for (Direction side : Iterate.directions) {
            if (!prefersConnectionTo(world, pos, side))
                continue;
            if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                prefferedAxis = null;
                break;
            }
            prefferedAxis = side.getAxis();
        }

        if (prefferedAxis == Axis.Y)
            stateForPlacement = stateForPlacement.setValue(FACE, AttachFace.WALL).setValue(FACING, stateForPlacement.getValue(FACING).getOpposite());
        else if (prefferedAxis != null) {
            if (stateForPlacement.getValue(FACE) == AttachFace.WALL)
                stateForPlacement = stateForPlacement.setValue(FACE, AttachFace.FLOOR);
            for (Direction direction : ctx.getNearestLookingDirections()) {
                if (direction.getAxis() != prefferedAxis)
                    continue;
                stateForPlacement = stateForPlacement.setValue(FACING, direction.getOpposite());
            }
        }

        return withWater(stateForPlacement, ctx);
    }

    protected boolean prefersConnectionTo(LevelReader reader, BlockPos pos, Direction facing) {
        BlockPos offset = pos.relative(facing);
        BlockState blockState = reader.getBlockState(offset);
        return FluidPipeBlock.canConnectTo(reader, offset, blockState, facing);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        if (!world.isClientSide())
            FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    @Override
    public boolean canSurvive(BlockState p_196260_1_, LevelReader p_196260_2_, BlockPos p_196260_3_) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isClientSide())
            return;
        if (state != oldState)
            world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborUpdate(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos, boolean isMoving) {
        Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!isOpenAt(state, d))
            return;
        world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block otherBlock,
        @Nullable Orientation wireOrientation,
        boolean isMoving
    ) {
    }

    public static boolean isOpenAt(BlockState state, Direction d) {
        return d.getAxis() == getPipeAxis(state);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    protected static Axis getPipeAxis(BlockState state) {
        return state.getValue(FACE) == AttachFace.WALL ? Axis.Y : state.getValue(FACING).getAxis();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        AttachFace face = state.getValue(FACE);
        VoxelShaper shape = face == AttachFace.FLOOR ? AllShapes.SMART_FLUID_PIPE_FLOOR : face == AttachFace.CEILING ? AllShapes.SMART_FLUID_PIPE_CEILING : AllShapes.SMART_FLUID_PIPE_WALL;
        return shape.get(state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public Axis getAxis(BlockState state) {
        return getPipeAxis(state);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public BlockState updateShape(
        BlockState pState,
        LevelReader pLevel,
        ScheduledTickAccess tickView,
        BlockPos pCurrentPos,
        Direction pFacing,
        BlockPos pFacingPos,
        BlockState pFacingState,
        RandomSource random
    ) {
        updateWater(pLevel, tickView, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public Class<SmartFluidPipeBlockEntity> getBlockEntityClass() {
        return SmartFluidPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmartFluidPipeBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SMART_FLUID_PIPE;
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }

}
