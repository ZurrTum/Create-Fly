package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

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
        if (update && !world.isClient)
            updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        Block block = getCachedState().getBlock();
        if (!(block instanceof HandCrankBlock crank))
            return 0;
        int speed = (inUse == 0 ? 0 : clockwise() ? -1 : 1) * crank.getRotationSpeed();
        return convertToDirection(speed, getCachedState().get(HandCrankBlock.FACING));
    }

    protected boolean clockwise() {
        return backwards;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("InUse", inUse);
        view.putBoolean("Backwards", backwards);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        inUse = view.getInt("InUse", 0);
        backwards = view.getBoolean("Backwards", false);
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

            if (inUse == 0 && !world.isClient) {
                sequenceContext = null;
                updateGeneratedRotation();
            }
        }
    }

    @Override
    protected Block getStressConfigKey() {
        return getCachedState().isOf(AllBlocks.HAND_CRANK) ? AllBlocks.HAND_CRANK : AllBlocks.COPPER_VALVE_HANDLE;
    }

}
