package com.zurrtum.create.content.kinetics.transmission;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public class GearshiftBlock extends AbstractEncasedShaftBlock implements IBE<SplitShaftBlockEntity> {

    public static final BooleanProperty POWERED = Properties.POWERED;

    public GearshiftBlock(Settings properties) {
        super(properties);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.appendProperties(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return super.getPlacementState(context).with(POWERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos()));
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
        if (previouslyPowered != worldIn.isReceivingRedstonePower(pos)) {
            detachKinetics(worldIn, pos, true);
            worldIn.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public Class<SplitShaftBlockEntity> getBlockEntityClass() {
        return SplitShaftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SplitShaftBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.GEARSHIFT;
    }

    public void detachKinetics(World worldIn, BlockPos pos, boolean reAttachNextTick) {
        BlockEntity be = worldIn.getBlockEntity(pos);
        if (be == null || !(be instanceof KineticBlockEntity))
            return;
        RotationPropagator.handleRemoved(worldIn, pos, (KineticBlockEntity) be);

        // Re-attach next tick
        if (reAttachNextTick)
            worldIn.scheduleBlockTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        BlockEntity be = worldIn.getBlockEntity(pos);
        if (!(be instanceof KineticBlockEntity kte))
            return;
        RotationPropagator.handleAdded(worldIn, pos, kte);
    }
}
