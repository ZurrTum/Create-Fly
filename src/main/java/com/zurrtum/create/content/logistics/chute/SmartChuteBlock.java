package com.zurrtum.create.content.logistics.chute;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class SmartChuteBlock extends AbstractChuteBlock {

    public SmartChuteBlock(Settings p_i48440_1_) {
        super(p_i48440_1_);
        setDefaultState(getDefaultState().with(POWERED, true));
    }

    public static final BooleanProperty POWERED = Properties.POWERED;

    @Override
    public void neighborUpdate(
        BlockState state,
        World level,
        BlockPos pos,
        Block block,
        @Nullable WireOrientation wireOrientation,
        boolean isMoving
    ) {
        super.neighborUpdate(state, level, pos, block, wireOrientation, isMoving);
        if (level.isClient())
            return;
        if (!level.getBlockTickScheduler().isTicking(pos, this))
            level.scheduleBlockTick(pos, this, 1);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random r) {
        boolean previouslyPowered = state.get(POWERED);
        if (previouslyPowered != worldIn.isReceivingRedstonePower(pos))
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public BlockEntityType<? extends ChuteBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.SMART_CHUTE;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(POWERED));
    }

    @Override
    public BlockState updateChuteState(BlockState state, BlockState above, BlockView world, BlockPos pos) {
        return state;
    }

}
