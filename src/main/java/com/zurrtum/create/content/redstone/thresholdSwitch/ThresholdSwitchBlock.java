package com.zurrtum.create.content.redstone.thresholdSwitch;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ThresholdSwitchBlock extends DirectedDirectionalBlock implements IBE<ThresholdSwitchBlockEntity>, RedStoneConnectBlock {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 5);

    public ThresholdSwitchBlock(Properties p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        updateObservedInventory(state, worldIn, pos);
    }

    private void updateObservedInventory(BlockState state, LevelReader world, BlockPos pos) {
        withBlockEntityDo(world, pos, ThresholdSwitchBlockEntity::updateCurrentLevel);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        return side != null && side.getOpposite() != getTargetDirection(state);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (side == getTargetDirection(blockState).getOpposite())
            return 0;
        return getBlockEntityOptional(blockAccess, pos).filter(ThresholdSwitchBlockEntity::isPowered).map($ -> 15).orElse(0);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel world, BlockPos pos, RandomSource random) {
        getBlockEntityOptional(world, pos).ifPresent(ThresholdSwitchBlockEntity::updatePowerAfterDelay);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(LEVEL));
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (player != null && stack.is(AllItems.WRENCH))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) {
            withBlockEntityDo(
                level, pos, be -> {
                    AllClientHandle.INSTANCE.openThresholdSwitchScreen(be, player);
                }
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();

        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos().relative(face));
            if (be != null && (ItemHelper.getInventory(
                be.getLevel(),
                be.getBlockPos(),
                null,
                be,
                null
            ) != null || FluidHelper.hasFluidInventory(be.getLevel(), be.getBlockPos(), null, be, null))) {
                preferredFacing = face;
                break;
            }
        }

        if (preferredFacing == null) {
            Direction facing = context.getNearestLookingDirection();
            preferredFacing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? facing : facing.getOpposite();
        }

        if (preferredFacing.getAxis() == Axis.Y) {
            state = state.setValue(TARGET, preferredFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
            preferredFacing = context.getHorizontalDirection();
        }

        return state.setValue(FACING, preferredFacing);
    }

    @Override
    public Class<ThresholdSwitchBlockEntity> getBlockEntityClass() {
        return ThresholdSwitchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThresholdSwitchBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.THRESHOLD_SWITCH;
    }

}
