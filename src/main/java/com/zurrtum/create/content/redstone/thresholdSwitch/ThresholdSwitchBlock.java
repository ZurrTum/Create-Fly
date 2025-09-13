package com.zurrtum.create.content.redstone.thresholdSwitch;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class ThresholdSwitchBlock extends DirectedDirectionalBlock implements IBE<ThresholdSwitchBlockEntity>, RedStoneConnectBlock {

    public static final IntProperty LEVEL = IntProperty.of("level", 0, 5);

    public ThresholdSwitchBlock(Settings p_i48377_1_) {
        super(p_i48377_1_);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        updateObservedInventory(state, worldIn, pos);
    }

    private void updateObservedInventory(BlockState state, WorldView world, BlockPos pos) {
        withBlockEntityDo(world, pos, ThresholdSwitchBlockEntity::updateCurrentLevel);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        return side != null && side.getOpposite() != getTargetDirection(state);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        if (side == getTargetDirection(blockState).getOpposite())
            return 0;
        return getBlockEntityOptional(blockAccess, pos).filter(ThresholdSwitchBlockEntity::isPowered).map($ -> 15).orElse(0);
    }

    @Override
    public void scheduledTick(BlockState blockState, ServerWorld world, BlockPos pos, Random random) {
        getBlockEntityOptional(world, pos).ifPresent(ThresholdSwitchBlockEntity::updatePowerAfterDelay);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(LEVEL));
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
        if (player != null && stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (level.isClient) {
            withBlockEntityDo(
                level, pos, be -> {
                    AllClientHandle.INSTANCE.openThresholdSwitchScreen(be, player);
                }
            );
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState();

        Direction preferredFacing = null;
        for (Direction face : context.getPlacementDirections()) {
            BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos().offset(face));
            if (be != null && (ItemHelper.getInventory(
                be.getWorld(),
                be.getPos(),
                null,
                be,
                null
            ) != null || FluidHelper.hasFluidInventory(be.getWorld(), be.getPos(), null, be, null))) {
                preferredFacing = face;
                break;
            }
        }

        if (preferredFacing == null) {
            Direction facing = context.getPlayerLookDirection();
            preferredFacing = context.getPlayer() != null && context.getPlayer().isSneaking() ? facing : facing.getOpposite();
        }

        if (preferredFacing.getAxis() == Axis.Y) {
            state = state.with(TARGET, preferredFacing == Direction.UP ? BlockFace.CEILING : BlockFace.FLOOR);
            preferredFacing = context.getHorizontalPlayerFacing();
        }

        return state.with(FACING, preferredFacing);
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
