package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity.SequenceContext;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerValveScrollValueBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ValveHandleBlockEntity extends HandCrankBlockEntity {

    public ServerScrollValueBehaviour angleInput;
    public int cooldown;

    public int startAngle;
    public int targetAngle;
    public int totalUseTicks;
    private boolean keepAlive;

    public ValveHandleBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.VALVE_HANDLE, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        angleInput = new ServerValveScrollValueBehaviour(this);
        angleInput.between(-180, 180);
        angleInput.setValue(45);
        behaviours.add(angleInput);
    }

    @Override
    protected boolean clockwise() {
        return angleInput.getValue() < 0 ^ backwards;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("TotalUseTicks", totalUseTicks);
        view.putInt("StartAngle", startAngle);
        view.putInt("TargetAngle", targetAngle);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        totalUseTicks = view.getIntOr("TotalUseTicks", 0);
        startAngle = view.getIntOr("StartAngle", 0);
        targetAngle = view.getIntOr("TargetAngle", 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (inUse == 0 && cooldown > 0)
            cooldown--;
        independentAngle = 0;
    }

    public boolean showValue() {
        return inUse == 0;
    }

    public boolean activate(boolean sneak) {
        if (getTheoreticalSpeed() != 0)
            return false;
        if (inUse > 0 || cooldown > 0)
            return false;
        if (level.isClientSide())
            return true;

        // Always overshoot, target will stop early
        int value = angleInput.getValue();
        int target = Math.abs(value);
        int rotationSpeed = AllBlocks.COPPER_VALVE_HANDLE.getRotationSpeed();
        double degreesPerTick = KineticBlockEntity.convertToAngular(rotationSpeed);
        inUse = (int) Math.ceil(target / degreesPerTick) + 2;

        startAngle = 0;
        targetAngle = Math.round((startAngle + (target > 135 ? 180 : 90) * Mth.sign(value)) / 90f) * 90;
        totalUseTicks = inUse;
        backwards = sneak;

        sequenceContext = SequenceContext.fromGearshift(SequencerInstructions.TURN_ANGLE, rotationSpeed, target);
        updateGeneratedRotation();
        cooldown = 4;

        return true;
    }

    @Override
    protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        BlockState state = level.getBlockState(pos);
        if (getType().isValid(state)) {
            keepAlive = true;
            setBlockState(state);
        } else {
            super.preRemoveSideEffects(pos, oldState);
        }
    }

    @Override
    public void setRemoved() {
        if (keepAlive) {
            keepAlive = false;
            level.getChunk(worldPosition).setBlockEntity(this);
        } else {
            super.setRemoved();
        }
    }
}
