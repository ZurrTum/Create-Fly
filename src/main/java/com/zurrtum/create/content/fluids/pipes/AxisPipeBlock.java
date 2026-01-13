package com.zurrtum.create.content.fluids.pipes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.equipment.wrench.IWrenchableWithBracket;
import com.zurrtum.create.content.fluids.FluidPropagator;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.NeighborUpdateListeningBlock;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class AxisPipeBlock extends PillarBlock implements IWrenchableWithBracket, IAxisPipe, NeighborUpdateListeningBlock {

    public AxisPipeBlock(Settings p_i48339_1_) {
        super(p_i48339_1_);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean isMoving) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
        if (!isMoving)
            removeBracket(world, pos, true).ifPresent(stack -> Block.dropStack(world, pos, stack));
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
        if (!stack.isOf(AllItems.COPPER_CASING))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient())
            return ActionResult.SUCCESS;
        BlockState newState = AllBlocks.ENCASED_FLUID_PIPE.getDefaultState();
        for (Direction d : Iterate.directionsInAxis(getAxis(state)))
            newState = newState.with(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(d), true);
        FluidTransportBehaviour.cacheFlows(level, pos);
        level.setBlockState(pos, newState);
        FluidTransportBehaviour.loadFlows(level, pos);
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isClient())
            return;
        if (state != oldState)
            world.scheduleBlockTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.FLUID_PIPE.getDefaultStack();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos, boolean isMoving) {
        Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!isOpenAt(state, d))
            return;
        world.scheduleBlockTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block otherBlock,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
    }

    public static boolean isOpenAt(BlockState state, Direction d) {
        return d.getAxis() == state.get(AXIS);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_, ShapeContext p_220053_4_) {
        return AllShapes.EIGHT_VOXEL_POLE.get(state.get(AXIS));
    }

    public BlockState toRegularPipe(WorldAccess world, BlockPos pos, BlockState state) {
        Direction side = Direction.get(AxisDirection.POSITIVE, state.get(AXIS));
        Map<Direction, BooleanProperty> facingToPropertyMap = FluidPipeBlock.FACING_PROPERTIES;
        return AllBlocks.FLUID_PIPE.updateBlockState(
            AllBlocks.FLUID_PIPE.getDefaultState().with(facingToPropertyMap.get(side), true).with(facingToPropertyMap.get(side.getOpposite()), true),
            side,
            null,
            world,
            pos
        );
    }

    @Override
    public Axis getAxis(BlockState state) {
        return state.get(AXIS);
    }

    @Override
    public Optional<ItemStack> removeBracket(BlockView world, BlockPos pos, boolean inOnReplacedContext) {
        BracketedBlockEntityBehaviour behaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (behaviour == null)
            return Optional.empty();
        BlockState bracket = behaviour.removeBracket(inOnReplacedContext);
        if (bracket == null)
            return Optional.empty();
        return Optional.of(new ItemStack(bracket.getBlock()));
    }

}
