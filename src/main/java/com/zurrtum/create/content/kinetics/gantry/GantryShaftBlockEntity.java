package com.zurrtum.create.content.kinetics.gantry;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

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
            if (d.getAxis() == getCachedState().get(GantryShaftBlock.FACING).getAxis())
                continue;
            BlockPos offset = pos.offset(d);
            BlockState pinionState = world.getBlockState(offset);
            if (!pinionState.isOf(AllBlocks.GANTRY_CARRIAGE))
                continue;
            if (pinionState.get(GantryCarriageBlock.FACING) != d)
                continue;
            BlockEntity blockEntity = world.getBlockEntity(offset);
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
        if (!stateFrom.get(GantryShaftBlock.POWERED))
            return defaultModifier;
        if (!stateTo.isOf(AllBlocks.GANTRY_CARRIAGE))
            return defaultModifier;

        Direction direction = Direction.getFacing(diff.getX(), diff.getY(), diff.getZ());
        if (stateTo.get(GantryCarriageBlock.FACING) != direction)
            return defaultModifier;
        return GantryCarriageBlockEntity.getGantryPinionModifier(stateFrom.get(GantryShaftBlock.FACING), stateTo.get(GantryCarriageBlock.FACING));
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        if (!otherState.isOf(AllBlocks.GANTRY_CARRIAGE))
            return false;
        final BlockPos diff = other.getPos().subtract(pos);
        Direction direction = Direction.getFacing(diff.getX(), diff.getY(), diff.getZ());
        return otherState.get(GantryCarriageBlock.FACING) == direction;
    }

    public boolean canAssembleOn() {
        BlockState blockState = getCachedState();
        if (blockState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return false;
        if (blockState.get(GantryShaftBlock.POWERED))
            return false;
        float speed = getPinionMovementSpeed();

        switch (blockState.get(GantryShaftBlock.PART)) {
            case END:
                return speed < 0;
            case MIDDLE:
                return speed != 0;
            case START:
                return speed > 0;
            case SINGLE:
            default:
                return false;
        }
    }

    public float getPinionMovementSpeed() {
        BlockState blockState = getCachedState();
        if (blockState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return 0;
        return MathHelper.clamp(convertToLinear(-getSpeed()), -.49f, .49f);
    }

    @Override
    public boolean isNoisy() {
        return false;
    }

}
