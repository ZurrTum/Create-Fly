package com.zurrtum.create.content.decoration.steamWhistle;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Locale;

public class WhistleExtenderBlock extends Block implements IWrenchable {

    public static final EnumProperty<WhistleExtenderShape> SHAPE = EnumProperty.of("shape", WhistleExtenderShape.class);
    public static final EnumProperty<WhistleSize> SIZE = WhistleBlock.SIZE;

    public enum WhistleExtenderShape implements StringIdentifiable {
        SINGLE,
        DOUBLE,
        DOUBLE_CONNECTED;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public WhistleExtenderBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(SHAPE, WhistleExtenderShape.SINGLE).with(SIZE, WhistleSize.MEDIUM));
    }

    @Override
    public ActionResult onSneakWrenched(BlockState state, ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (context.getHitPos().y < context.getBlockPos().getY() + .5f || state.get(SHAPE) == WhistleExtenderShape.SINGLE)
            return IWrenchable.super.onSneakWrenched(state, context);
        if (!(world instanceof ServerWorld))
            return ActionResult.SUCCESS;
        world.setBlockState(pos, state.with(SHAPE, WhistleExtenderShape.SINGLE), Block.NOTIFY_ALL);
        IWrenchable.playRemoveSound(world, pos);
        return ActionResult.SUCCESS;
    }

    protected ItemUsageContext relocateContext(ItemUsageContext context, BlockPos target) {
        return new ItemUsageContext(
            context.getPlayer(),
            context.getHand(),
            new BlockHitResult(context.getHitPos(), context.getSide(), target, context.hitsInsideBlock())
        );
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
        if (player == null || !stack.isOf(AllItems.STEAM_WHISTLE))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        BlockPos findRoot = findRoot(level, pos);
        BlockState blockState = level.getBlockState(findRoot);
        if (blockState.getBlock() instanceof WhistleBlock whistle)
            return whistle.onUseWithItem(
                stack,
                blockState,
                level,
                findRoot,
                player,
                hand,
                new BlockHitResult(hitResult.getPos(), hitResult.getSide(), findRoot, hitResult.isInsideBlock())
            );
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        World level = context.getWorld();
        BlockPos findRoot = findRoot(level, context.getBlockPos());
        BlockState blockState = level.getBlockState(findRoot);
        if (blockState.getBlock() instanceof WhistleBlock whistle)
            return whistle.onWrenched(blockState, relocateContext(context, findRoot));
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.STEAM_WHISTLE.getDefaultStack();
    }

    public static BlockPos findRoot(WorldAccess pLevel, BlockPos pPos) {
        BlockPos currentPos = pPos.down();
        while (true) {
            BlockState blockState = pLevel.getBlockState(currentPos);
            if (blockState.isOf(AllBlocks.STEAM_WHISTLE_EXTENSION)) {
                currentPos = currentPos.down();
                continue;
            }
            return currentPos;
        }
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        BlockState below = pLevel.getBlockState(pPos.down());
        return below.isOf(this) && below.get(SHAPE) != WhistleExtenderShape.SINGLE || below.isOf(AllBlocks.STEAM_WHISTLE);
    }

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
        if (pFacing.getAxis() != Axis.Y)
            return pState;

        if (pFacing == Direction.UP) {
            boolean connected = pState.get(SHAPE) == WhistleExtenderShape.DOUBLE_CONNECTED;
            boolean shouldConnect = pLevel.getBlockState(pCurrentPos.up()).isOf(this);
            if (!connected && shouldConnect)
                return pState.with(SHAPE, WhistleExtenderShape.DOUBLE_CONNECTED);
            if (connected && !shouldConnect)
                return pState.with(SHAPE, WhistleExtenderShape.DOUBLE);
            return pState;
        }

        return !pState.canPlaceAt(pLevel, pCurrentPos) ? Blocks.AIR.getDefaultState() : pState.with(
            SIZE,
            pLevel.getBlockState(pCurrentPos.down()).get(SIZE)
        );
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.getBlock() != this || pOldState.get(SHAPE) != pState.get(SHAPE))
            WhistleBlock.queuePitchUpdate(pLevel, findRoot(pLevel, pPos));
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        WhistleBlock.queuePitchUpdate(pLevel, findRoot(pLevel, pPos));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(SHAPE, SIZE));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        WhistleSize size = pState.get(SIZE);
        return switch (pState.get(SHAPE)) {
            case DOUBLE ->
                size == WhistleSize.LARGE ? AllShapes.WHISTLE_EXTENDER_LARGE_DOUBLE : size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_EXTENDER_MEDIUM_DOUBLE : AllShapes.WHISTLE_EXTENDER_SMALL_DOUBLE;
            case DOUBLE_CONNECTED ->
                size == WhistleSize.LARGE ? AllShapes.WHISTLE_EXTENDER_LARGE_DOUBLE_CONNECTED : size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_EXTENDER_MEDIUM_DOUBLE_CONNECTED : AllShapes.WHISTLE_EXTENDER_SMALL_DOUBLE_CONNECTED;
            default ->
                size == WhistleSize.LARGE ? AllShapes.WHISTLE_EXTENDER_LARGE : size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_EXTENDER_MEDIUM : AllShapes.WHISTLE_EXTENDER_SMALL;
        };
    }

    //TODO
    //    @Override
    //    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState,
    //        Direction dir) {
    //        return AllBlocks.STEAM_WHISTLE.has(neighborState) && dir == Direction.DOWN;
    //    }
}
