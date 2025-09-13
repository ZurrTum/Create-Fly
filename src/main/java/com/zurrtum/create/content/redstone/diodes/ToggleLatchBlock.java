package com.zurrtum.create.content.redstone.diodes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ToggleLatchBlock extends AbstractDiodeBlock implements RedStoneConnectBlock {

    public static BooleanProperty POWERING = BooleanProperty.of("powering");

    public static final MapCodec<ToggleLatchBlock> CODEC = createCodec(ToggleLatchBlock::new);

    public ToggleLatchBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERING, false).with(POWERED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends AbstractRedstoneGateBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, POWERING, FACING);
    }

    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(FACING) == side ? getOutputLevel(blockAccess, pos, blockState) : 0;
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 1;
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
        if (!player.canModifyBlocks())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (player.isSneaking())
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (stack.isOf(AllItems.WRENCH))
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        return activated(level, pos, state);
    }

    @Override
    protected int getOutputLevel(BlockView worldIn, BlockPos pos, BlockState state) {
        return state.get(POWERING) ? 15 : 0;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        boolean poweredPreviously = state.get(POWERED);
        super.scheduledTick(state, worldIn, pos, random);
        BlockState newState = worldIn.getBlockState(pos);
        if (newState.get(POWERED) && !poweredPreviously)
            worldIn.setBlockState(pos, newState.cycle(POWERING), Block.NOTIFY_LISTENERS);
    }

    protected ActionResult activated(World worldIn, BlockPos pos, BlockState state) {
        if (!worldIn.isClient) {
            float f = !state.get(POWERING) ? 0.6F : 0.5F;
            worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
            worldIn.setBlockState(pos, state.cycle(POWERING), Block.NOTIFY_LISTENERS);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, Direction side) {
        if (side == null)
            return false;
        return side.getAxis() == state.get(FACING).getAxis();
    }

}
