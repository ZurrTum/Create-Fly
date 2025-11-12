package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HandCrankBlockEntity extends GeneratingKineticBlockEntity {

    public int inUse;
    public boolean backwards;
    public float independentAngle;
    public float chasingVelocity;

    public HandCrankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public HandCrankBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.HAND_CRANK, pos, state);
    }

    public void turn(boolean back) {
        boolean update = false;

        if (getGeneratedSpeed() == 0 || back != backwards)
            update = true;

        inUse = 10;
        this.backwards = back;
        if (update && !level.isClientSide())
            updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        Block block = getBlockState().getBlock();
        if (!(block instanceof HandCrankBlock crank))
            return 0;
        int speed = (inUse == 0 ? 0 : clockwise() ? -1 : 1) * crank.getRotationSpeed();
        return convertToDirection(speed, getBlockState().getValue(HandCrankBlock.FACING));
    }

    protected boolean clockwise() {
        return backwards;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("InUse", inUse);
        view.putBoolean("Backwards", backwards);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        inUse = view.getIntOr("InUse", 0);
        backwards = view.getBooleanOr("Backwards", false);
        super.read(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        float actualSpeed = getSpeed();
        chasingVelocity += ((actualSpeed * 10 / 3f) - chasingVelocity) * .25f;
        independentAngle += chasingVelocity;

        if (inUse > 0) {
            inUse--;

            if (inUse == 0 && !level.isClientSide()) {
                sequenceContext = null;
                updateGeneratedRotation();
            }
        }
    }

    @Override
    protected Block getStressConfigKey() {
        return getBlockState().is(AllBlocks.HAND_CRANK) ? AllBlocks.HAND_CRANK : AllBlocks.COPPER_VALVE_HANDLE;
    }

}
