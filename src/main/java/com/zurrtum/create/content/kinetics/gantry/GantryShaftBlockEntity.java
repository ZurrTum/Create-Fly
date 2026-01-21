package com.zurrtum.create.content.kinetics.gantry;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GantryShaftBlockEntity extends KineticBlockEntity {

    public GantryShaftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.GANTRY_SHAFT, pos, state);
    }

    @Override
    protected boolean syncSequenceContext() {
        return true;
    }

    public void checkAttachedCarriageBlocks() {
        if (!canAssembleOn())
            return;
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == getBlockState().getValue(GantryShaftBlock.FACING).getAxis())
                continue;
            BlockPos offset = worldPosition.relative(d);
            BlockState pinionState = level.getBlockState(offset);
            if (!pinionState.is(AllBlocks.GANTRY_CARRIAGE))
                continue;
            if (pinionState.getValue(GantryCarriageBlock.FACING) != d)
                continue;
            BlockEntity blockEntity = level.getBlockEntity(offset);
            if (blockEntity instanceof GantryCarriageBlockEntity)
                ((GantryCarriageBlockEntity) blockEntity).queueAssembly();
        }
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        checkAttachedCarriageBlocks();
    }

    @Override
    public float propagateRotationTo(
        KineticBlockEntity target,
        BlockState stateFrom,
        BlockState stateTo,
        BlockPos diff,
        boolean connectedViaAxes,
        boolean connectedViaCogs
    ) {
        float defaultModifier = super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);

        if (connectedViaAxes)
            return defaultModifier;
        if (!stateFrom.getValue(GantryShaftBlock.POWERED))
            return defaultModifier;
        if (!stateTo.is(AllBlocks.GANTRY_CARRIAGE))
            return defaultModifier;

        Direction direction = Direction.getApproximateNearest(diff.getX(), diff.getY(), diff.getZ());
        if (stateTo.getValue(GantryCarriageBlock.FACING) != direction)
            return defaultModifier;
        return GantryCarriageBlockEntity.getGantryPinionModifier(
            stateFrom.getValue(GantryShaftBlock.FACING),
            stateTo.getValue(GantryCarriageBlock.FACING)
        );
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        if (!otherState.is(AllBlocks.GANTRY_CARRIAGE))
            return false;
        final BlockPos diff = other.getBlockPos().subtract(worldPosition);
        Direction direction = Direction.getApproximateNearest(diff.getX(), diff.getY(), diff.getZ());
        return otherState.getValue(GantryCarriageBlock.FACING) == direction;
    }

    public boolean canAssembleOn() {
        BlockState blockState = getBlockState();
        if (blockState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return false;
        if (blockState.getValue(GantryShaftBlock.POWERED))
            return false;
        float speed = getPinionMovementSpeed();

        return switch (blockState.getValue(GantryShaftBlock.PART)) {
            case END -> speed < 0;
            case MIDDLE -> speed != 0;
            case START -> speed > 0;
            default -> false;
        };
    }

    public float getPinionMovementSpeed() {
        BlockState blockState = getBlockState();
        if (blockState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return 0;
        return Mth.clamp(convertToLinear(-getSpeed()), -.49f, .49f);
    }

    @Override
    public boolean isNoisy() {
        return false;
    }

}
