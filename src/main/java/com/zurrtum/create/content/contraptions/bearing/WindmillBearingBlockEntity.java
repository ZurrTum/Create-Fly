package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class WindmillBearingBlockEntity extends MechanicalBearingBlockEntity {

    protected ServerScrollOptionBehaviour<RotationDirection> movementDirection;
    protected float lastGeneratedSpeed;

    protected boolean queuedReassembly;

    public WindmillBearingBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.WINDMILL_BEARING, pos, state);
    }

    @Override
    public void updateGeneratedRotation() {
        super.updateGeneratedRotation();
        lastGeneratedSpeed = getGeneratedSpeed();
        queuedReassembly = false;
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        boolean cancelAssembly = assembleNextTick;
        super.onSpeedChanged(prevSpeed);
        assembleNextTick = cancelAssembly;
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient())
            return;
        if (!queuedReassembly)
            return;
        queuedReassembly = false;
        if (!running)
            assembleNextTick = true;
    }

    public void disassembleForMovement() {
        if (!running)
            return;
        disassemble();
        queuedReassembly = true;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!running)
            return 0;
        if (movedContraption == null)
            return lastGeneratedSpeed;
        int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks() / AllConfigs.server().kinetics.windmillSailsPerRPM.get();
        return MathHelper.clamp(sails, 1, 16) * getAngleSpeedDirection();
    }

    @Override
    public boolean isWindmill() {
        return true;
    }

    protected float getAngleSpeedDirection() {
        RotationDirection rotationDirection = RotationDirection.values()[movementDirection.getValue()];
        return (rotationDirection == RotationDirection.CLOCKWISE ? 1 : -1);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putFloat("LastGenerated", lastGeneratedSpeed);
        view.putBoolean("QueueAssembly", queuedReassembly);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        if (!wasMoved)
            lastGeneratedSpeed = view.getFloat("LastGenerated", 0);
        queuedReassembly = view.getBoolean("QueueAssembly", false);
        super.read(view, clientPacket);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        movementDirection = new ServerScrollOptionBehaviour<>(RotationDirection.class, this);
        movementDirection.withCallback($ -> onDirectionChanged());
        behaviours.add(movementDirection);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.WINDMILL, AllAdvancements.WINDMILL_MAXED);
    }

    private void onDirectionChanged() {
        if (!running)
            return;
        if (!world.isClient)
            updateGeneratedRotation();
    }

    @Override
    public boolean isWoodenTop() {
        return true;
    }

    public enum RotationDirection {
        CLOCKWISE,
        COUNTER_CLOCKWISE;
    }

}
