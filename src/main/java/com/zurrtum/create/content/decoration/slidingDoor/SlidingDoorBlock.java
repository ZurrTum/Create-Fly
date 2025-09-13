package com.zurrtum.create.content.decoration.slidingDoor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.contraptions.ContraptionWorld;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SlidingDoorBlock extends DoorBlock implements IWrenchable, IBE<SlidingDoorBlockEntity> {

    public static final Supplier<BlockSetType> TRAIN_SET_TYPE = () -> new BlockSetType(
        "create:train",
        true,
        true,
        true,
        BlockSetType.ActivationRule.EVERYTHING,
        BlockSoundGroup.NETHERITE,
        SoundEvents.BLOCK_IRON_DOOR_CLOSE,
        SoundEvents.BLOCK_IRON_DOOR_OPEN,
        SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE,
        SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON
    );

    public static final Supplier<BlockSetType> GLASS_SET_TYPE = () -> new BlockSetType(
        "create:glass",
        true,
        true,
        true,
        BlockSetType.ActivationRule.EVERYTHING,
        BlockSoundGroup.GLASS,
        SoundEvents.BLOCK_IRON_DOOR_CLOSE,
        SoundEvents.BLOCK_IRON_DOOR_OPEN,
        SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE,
        SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON
    );
    public static final Supplier<BlockSetType> STONE_SET_TYPE = () -> new BlockSetType(
        "create:stone",
        true,
        true,
        true,
        BlockSetType.ActivationRule.EVERYTHING,
        BlockSoundGroup.STONE,
        SoundEvents.BLOCK_IRON_DOOR_CLOSE,
        SoundEvents.BLOCK_IRON_DOOR_OPEN,
        SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE,
        SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON
    );

    public static final BooleanProperty VISIBLE = BooleanProperty.of("visible");
    private final boolean folds;

    public static SlidingDoorBlock metal_fold(Settings settings) {
        return new SlidingDoorBlock(settings, TRAIN_SET_TYPE.get(), true);
    }

    public static SlidingDoorBlock metal_slide(Settings settings) {
        return new SlidingDoorBlock(settings, TRAIN_SET_TYPE.get(), false);
    }

    public static SlidingDoorBlock glass_slide(Settings settings) {
        return new SlidingDoorBlock(settings, GLASS_SET_TYPE.get(), false);
    }

    public static SlidingDoorBlock stone_fold(Settings settings) {
        return new SlidingDoorBlock(settings, STONE_SET_TYPE.get(), true);
    }

    public static SlidingDoorBlock stone_slide(Settings settings) {
        return new SlidingDoorBlock(settings, STONE_SET_TYPE.get(), false);
    }

    public SlidingDoorBlock(Settings properties, BlockSetType type, boolean folds) {
        super(type, properties);
        this.folds = folds;
    }

    public boolean isFoldingDoor() {
        return folds;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(VISIBLE));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        if (!pState.get(OPEN) && (pState.get(VISIBLE) || pLevel instanceof ContraptionWorld))
            return super.getOutlineShape(pState, pLevel, pPos, pContext);

        Direction direction = pState.get(FACING);
        boolean hinge = pState.get(HINGE) == DoorHinge.RIGHT;
        return SlidingDoorShapes.get(direction, hinge, isFoldingDoor());
    }

    @Override
    public boolean canPlaceAt(BlockState pState, WorldView pLevel, BlockPos pPos) {
        return pState.get(HALF) == DoubleBlockHalf.LOWER || pLevel.getBlockState(pPos.down()).isOf(this);
    }

    @Override
    public VoxelShape getRaycastShape(BlockState pState, BlockView pLevel, BlockPos pPos) {
        return getOutlineShape(pState, pLevel, pPos, ShapeContext.absent());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        BlockState stateForPlacement = super.getPlacementState(pContext);
        if (stateForPlacement != null && stateForPlacement.get(OPEN))
            return stateForPlacement.with(OPEN, false).with(POWERED, false);
        return stateForPlacement;
    }

    @Override
    public void onBlockAdded(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.isOf(this))
            deferUpdate(pLevel, pPos);
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
        BlockState blockState = super.getStateForNeighborUpdate(pState, pLevel, tickView, pCurrentPos, pFacing, pFacingPos, pFacingState, random);
        if (blockState.isAir())
            return blockState;
        DoubleBlockHalf doubleblockhalf = blockState.get(HALF);
        if (pFacing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
            return pFacingState.isOf(this) && pFacingState.get(HALF) != doubleblockhalf ? blockState.with(
                VISIBLE,
                pFacingState.get(VISIBLE)
            ) : Blocks.AIR.getDefaultState();
        }
        return blockState;
    }

    @Override
    public void setOpen(@Nullable Entity entity, World level, BlockState state, BlockPos pos, boolean open) {
        if (!state.isOf(this))
            return;
        if (state.get(OPEN) == open)
            return;
        BlockState changedState = state.with(OPEN, open);
        if (open)
            changedState = changedState.with(VISIBLE, false);
        level.setBlockState(pos, changedState, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);

        DoorHinge hinge = changedState.get(HINGE);
        Direction facing = changedState.get(FACING);
        BlockPos otherPos = pos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
        BlockState otherDoor = level.getBlockState(otherPos);
        if (isDoubleDoor(changedState, hinge, facing, otherDoor))
            setOpen(entity, level, otherDoor, otherPos, open);

        this.playSound(entity, level, pos, open);
        level.emitGameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    @Override
    public void neighborUpdate(
        BlockState pState,
        World pLevel,
        BlockPos pPos,
        Block pBlock,
        @Nullable WireOrientation wireOrientation,
        boolean pIsMoving
    ) {
        boolean lower = pState.get(HALF) == DoubleBlockHalf.LOWER;
        boolean isPowered = isDoorPowered(pLevel, pPos, pState);
        if (getDefaultState().isOf(pBlock))
            return;
        if (isPowered == pState.get(POWERED))
            return;

        SlidingDoorBlockEntity be = getBlockEntity(pLevel, lower ? pPos : pPos.down());
        if (be != null && be.deferUpdate)
            return;

        BlockState changedState = pState.with(POWERED, isPowered).with(OPEN, isPowered);
        if (isPowered)
            changedState = changedState.with(VISIBLE, false);

        if (isPowered != pState.get(OPEN)) {
            this.playSound(null, pLevel, pPos, isPowered);
            pLevel.emitGameEvent(null, isPowered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);

            DoorHinge hinge = changedState.get(HINGE);
            Direction facing = changedState.get(FACING);
            BlockPos otherPos = pPos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
            BlockState otherDoor = pLevel.getBlockState(otherPos);

            if (isDoubleDoor(changedState, hinge, facing, otherDoor)) {
                otherDoor = otherDoor.with(POWERED, isPowered).with(OPEN, isPowered);
                if (isPowered)
                    otherDoor = otherDoor.with(VISIBLE, false);
                pLevel.setBlockState(otherPos, otherDoor, Block.NOTIFY_LISTENERS);
            }
        }

        pLevel.setBlockState(pPos, changedState, Block.NOTIFY_LISTENERS);
    }

    public static boolean isDoorPowered(World pLevel, BlockPos pPos, BlockState state) {
        boolean lower = state.get(HALF) == DoubleBlockHalf.LOWER;
        DoorHinge hinge = state.get(HINGE);
        Direction facing = state.get(FACING);
        BlockPos otherPos = pPos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
        BlockState otherDoor = pLevel.getBlockState(otherPos);

        if (isDoubleDoor(
            state.cycle(OPEN),
            hinge,
            facing,
            otherDoor
        ) && (pLevel.isReceivingRedstonePower(otherPos) || pLevel.isReceivingRedstonePower(otherPos.offset(lower ? Direction.UP : Direction.DOWN))))
            return true;

        return pLevel.isReceivingRedstonePower(pPos) || pLevel.isReceivingRedstonePower(pPos.offset(lower ? Direction.UP : Direction.DOWN));
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        state = state.cycle(OPEN);
        boolean isOpen = state.get(OPEN);
        if (isOpen)
            state = state.with(VISIBLE, false);
        level.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
        level.emitGameEvent(player, isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        DoorHinge hinge = state.get(HINGE);
        Direction facing = state.get(FACING);
        BlockPos otherPos = pos.offset(hinge == DoorHinge.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
        BlockState otherDoor = level.getBlockState(otherPos);
        if (isDoubleDoor(state, hinge, facing, otherDoor))
            onUse(otherDoor, level, otherPos, player, hitResult);
        else if (isOpen) {
            this.playSound(player, level, pos, true);
            level.emitGameEvent(player, GameEvent.BLOCK_OPEN, pos);
        }

        return ActionResult.SUCCESS;
    }

    public void deferUpdate(WorldAccess level, BlockPos pos) {
        withBlockEntityDo(level, pos, sdte -> sdte.deferUpdate = true);
    }

    public static boolean isDoubleDoor(BlockState pState, DoorHinge hinge, Direction facing, BlockState otherDoor) {
        return otherDoor.getBlock() == pState.getBlock() && otherDoor.get(HINGE) != hinge && otherDoor.get(FACING) == facing && otherDoor.get(OPEN) != pState.get(
            OPEN) && otherDoor.get(HALF) == pState.get(HALF);
    }

    @Override
    public BlockRenderType getRenderType(BlockState pState) {
        return pState.get(VISIBLE) ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
    }

    private void playSound(@Nullable Entity pSource, World pLevel, BlockPos pPos, boolean pIsOpening) {
        pLevel.playSound(
            pSource,
            pPos,
            pIsOpening ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE,
            SoundCategory.BLOCKS,
            1.0F,
            pLevel.getRandom().nextFloat() * 0.1F + 0.9F
        );
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER)
            return null;
        return IBE.super.createBlockEntity(pos, state);
    }

    @Override
    public Class<SlidingDoorBlockEntity> getBlockEntityClass() {
        return SlidingDoorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SlidingDoorBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SLIDING_DOOR;
    }

}
