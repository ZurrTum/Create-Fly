package com.zurrtum.create.content.decoration.steamWhistle;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
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
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class WhistleBlock extends Block implements IBE<WhistleBlockEntity>, IWrenchable {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WALL = BooleanProperty.of("wall");
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final EnumProperty<WhistleSize> SIZE = EnumProperty.of("size", WhistleSize.class);

    public enum WhistleSize implements StringIdentifiable {

        SMALL,
        MEDIUM,
        LARGE;

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }

    }

    public WhistleBlock(Settings p_49795_) {
        super(p_49795_);
        setDefaultState(getDefaultState().with(POWERED, false).with(WALL, false).with(SIZE, WhistleSize.MEDIUM));
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return FluidTankBlock.isTank(pLevel.getBlockState(pPos.offset(getAttachedDirection(pState))));
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.cycle(SIZE);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(FACING, POWERED, SIZE, WALL));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        World level = pContext.getWorld();
        BlockPos clickedPos = pContext.getBlockPos();
        Direction face = pContext.getSide();
        boolean wall = true;
        if (face.getAxis() == Axis.Y) {
            face = pContext.getHorizontalPlayerFacing().getOpposite();
            wall = false;
        }

        BlockState state = super.getPlacementState(pContext).with(FACING, face.getOpposite())
            .with(POWERED, level.isReceivingRedstonePower(clickedPos)).with(WALL, wall);
        if (!canPlaceAt(state, level, clickedPos))
            return null;
        return state;
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
        if (player == null)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        if (stack.isOf(AllItems.STEAM_WHISTLE)) {
            incrementSize(level, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    public static void incrementSize(WorldAccess pLevel, BlockPos pPos) {
        BlockState base = pLevel.getBlockState(pPos);
        if (!base.contains(SIZE))
            return;
        WhistleSize size = base.get(SIZE);
        BlockSoundGroup soundtype = base.getSoundGroup();
        BlockPos currentPos = pPos.up();

        for (int i = 1; i <= 6; i++) {
            BlockState blockState = pLevel.getBlockState(currentPos);
            float pVolume = (soundtype.getVolume() + 1.0F) / 2.0F;
            SoundEvent growSound = SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value();
            SoundEvent hitSound = soundtype.getHitSound();

            if (blockState.isOf(AllBlocks.STEAM_WHISTLE_EXTENSION)) {
                if (blockState.get(WhistleExtenderBlock.SHAPE) == WhistleExtenderShape.SINGLE) {
                    pLevel.setBlockState(currentPos, blockState.with(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE), 3);
                    float pPitch = (float) Math.pow(2, -(i * 2) / 12.0);
                    pLevel.playSound(null, currentPos, growSound, SoundCategory.BLOCKS, pVolume / 4f, pPitch);
                    pLevel.playSound(null, currentPos, hitSound, SoundCategory.BLOCKS, pVolume, pPitch);
                    return;
                }
                currentPos = currentPos.up();
                continue;
            }

            if (!blockState.isReplaceable())
                return;

            pLevel.setBlockState(currentPos, AllBlocks.STEAM_WHISTLE_EXTENSION.getDefaultState().with(SIZE, size), 3);
            float pPitch = (float) Math.pow(2, -(i * 2 - 1) / 12.0);
            pLevel.playSound(null, currentPos, growSound, SoundCategory.BLOCKS, pVolume / 4f, pPitch);
            pLevel.playSound(null, currentPos, hitSound, SoundCategory.BLOCKS, pVolume, pPitch);
            return;
        }
    }

    public static void queuePitchUpdate(WorldAccess level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof WhistleBlock whistle && !level.getBlockTickScheduler().isQueued(pos, whistle))
            level.scheduleBlockTick(pos, whistle, 1);
    }

    @Override
    public void scheduledTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        withBlockEntityDo(pLevel, pPos, WhistleBlockEntity::updatePitch);
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        FluidTankBlock.updateBoilerState(pState, pLevel, pPos.offset(getAttachedDirection(pState)));
        if (pOldState.getBlock() != this || pOldState.get(SIZE) != pState.get(SIZE))
            queuePitchUpdate(pLevel, pPos);
    }

    @Override
    public void onStateReplaced(BlockState pState, ServerWorld pLevel, BlockPos pPos, boolean pIsMoving) {
        FluidTankBlock.updateBoilerState(pState, pLevel, pPos.offset(getAttachedDirection(pState)));
    }

    @Override
    public void neighborUpdate(
        BlockState state,
        World worldIn,
        BlockPos pos,
        Block blockIn,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        if (worldIn.isClient)
            return;
        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != worldIn.isReceivingRedstonePower(pos))
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
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
        return getAttachedDirection(pState) == pFacing && !pState.canPlaceAt(pLevel, pCurrentPos) ? Blocks.AIR.getDefaultState() : pState;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        WhistleSize size = pState.get(SIZE);
        if (!pState.get(WALL))
            return size == WhistleSize.SMALL ? AllShapes.WHISTLE_SMALL_FLOOR : size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_MEDIUM_FLOOR : AllShapes.WHISTLE_LARGE_FLOOR;
        Direction direction = pState.get(FACING);
        return (size == WhistleSize.SMALL ? AllShapes.WHISTLE_SMALL_WALL : size == WhistleSize.MEDIUM ? AllShapes.WHISTLE_MEDIUM_WALL : AllShapes.WHISTLE_LARGE_WALL).get(
            direction);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

    public static Direction getAttachedDirection(BlockState state) {
        return state.get(WALL) ? state.get(FACING) : Direction.DOWN;
    }

    @Override
    public Class<WhistleBlockEntity> getBlockEntityClass() {
        return WhistleBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WhistleBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.STEAM_WHISTLE;
    }

    @Override
    public BlockState rotate(BlockState pState, BlockRotation pRotation) {
        return pState.with(FACING, pRotation.rotate(pState.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, BlockMirror pMirror) {
        return pMirror == BlockMirror.NONE ? pState : pState.rotate(pMirror.getRotation(pState.get(FACING)));
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return direction == Direction.UP && stateFrom.isOf(AllBlocks.STEAM_WHISTLE_EXTENSION);
    }
}
