package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class MechanicalBearingBlockEntity extends GeneratingKineticBlockEntity implements IBearingBlockEntity {

    protected ServerScrollOptionBehaviour<RotationMode> movementMode;
    protected ControlledContraptionEntity movedContraption;
    protected float angle;
    protected boolean running;
    protected boolean assembleNextTick;
    protected float clientAngleDiff;
    public AssemblyException lastException;
    protected double sequencedAngleLimit;

    private float prevAngle;

    public MechanicalBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(3);
        sequencedAngleLimit = -1;
    }

    public MechanicalBearingBlockEntity(BlockPos pos, BlockState state) {
        this(AllBlockEntityTypes.MECHANICAL_BEARING, pos, state);
    }

    @Override
    public boolean isWoodenTop() {
        return false;
    }

    @Override
    protected boolean syncSequenceContext() {
        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        movementMode = new ServerScrollOptionBehaviour<>(RotationMode.class, this);
        behaviours.add(movementMode);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CONTRAPTION_ACTORS);
    }

    @Override
    public void remove() {
        if (!world.isClient())
            disassemble();
        super.remove();
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putBoolean("Running", running);
        view.putFloat("Angle", angle);
        if (sequencedAngleLimit >= 0)
            view.putDouble("SequencedAngleLimit", sequencedAngleLimit);
        if (lastException != null) {
            view.put("LastException", AssemblyException.CODEC, lastException);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        if (wasMoved) {
            super.read(view, clientPacket);
            return;
        }

        float angleBefore = angle;
        running = view.getBoolean("Running", false);
        angle = view.getFloat("Angle", 0);
        sequencedAngleLimit = view.getDouble("SequencedAngleLimit", -1);
        lastException = view.read("LastException", AssemblyException.CODEC).orElse(null);
        super.read(view, clientPacket);
        if (!clientPacket)
            return;
        if (running) {
            if (movedContraption == null || !movedContraption.isStalled()) {
                clientAngleDiff = AngleHelper.getShortestAngleDiff(angleBefore, angle);
                angle = angleBefore;
            }
        } else
            movedContraption = null;
    }

    @Override
    public float getInterpolatedAngle(float partialTicks) {
        if (isVirtual())
            return MathHelper.lerp(partialTicks + .5f, prevAngle, angle);
        if (movedContraption == null || movedContraption.isStalled() || !running)
            partialTicks = 0;
        float angularSpeed = getAngularSpeed();
        if (sequencedAngleLimit >= 0)
            angularSpeed = (float) MathHelper.clamp(angularSpeed, -sequencedAngleLimit, sequencedAngleLimit);
        return MathHelper.lerp(partialTicks, angle, angle + angularSpeed);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        assembleNextTick = true;
        sequencedAngleLimit = -1;

        if (movedContraption != null && Math.signum(prevSpeed) != Math.signum(getSpeed()) && prevSpeed != 0) {
            if (!movedContraption.isStalled()) {
                angle = Math.round(angle);
                applyRotation();
            }
            movedContraption.getContraption().stop(world);
        }

        if (!isWindmill() && sequenceContext != null && sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE)
            sequencedAngleLimit = sequenceContext.getEffectiveValue(getTheoreticalSpeed());
    }

    public float getAngularSpeed() {
        float speed = convertToAngular(isWindmill() ? getGeneratedSpeed() : getSpeed());
        if (getSpeed() == 0)
            speed = 0;
        if (world.isClient()) {
            speed *= AllClientHandle.INSTANCE.getServerSpeed();
            speed += clientAngleDiff / 3f;
        }
        return speed;
    }

    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    public boolean isWindmill() {
        return false;
    }

    public void assemble() {
        if (!(world.getBlockState(pos).getBlock() instanceof BearingBlock))
            return;

        Direction direction = getCachedState().get(BearingBlock.FACING);
        BearingContraption contraption = new BearingContraption(isWindmill(), direction);
        try {
            if (!contraption.assemble(world, pos))
                return;

            lastException = null;
        } catch (AssemblyException e) {
            lastException = e;
            sendData();
            return;
        }

        if (isWindmill())
            award(AllAdvancements.WINDMILL);
        if (contraption.getSailBlocks() >= 16 * 8)
            award(AllAdvancements.WINDMILL_MAXED);

        contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
        movedContraption = ControlledContraptionEntity.create(world, this, contraption);
        BlockPos anchor = pos.offset(direction);
        movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
        movedContraption.setRotationAxis(direction.getAxis());
        world.spawnEntity(movedContraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(world, pos);

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);

        running = true;
        angle = 0;
        sendData();
        updateGeneratedRotation();
    }

    public void disassemble() {
        if (!running && movedContraption == null)
            return;
        angle = 0;
        sequencedAngleLimit = -1;
        if (isWindmill())
            applyRotation();
        if (movedContraption != null) {
            movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(world, pos);
        }

        movedContraption = null;
        running = false;
        updateGeneratedRotation();
        assembleNextTick = false;
        sendData();
    }

    @Override
    public void tick() {
        super.tick();

        prevAngle = angle;
        if (world.isClient())
            clientAngleDiff /= 2;

        if (!world.isClient() && assembleNextTick) {
            assembleNextTick = false;
            if (running) {
                boolean canDisassemble = movementMode.get() == RotationMode.ROTATE_PLACE || (isNearInitialAngle() && movementMode.get() == RotationMode.ROTATE_PLACE_RETURNED);
                if (speed == 0 && (canDisassemble || movedContraption == null || movedContraption.getContraption().getBlocks().isEmpty())) {
                    if (movedContraption != null)
                        movedContraption.getContraption().stop(world);
                    disassemble();
                    return;
                }
            } else {
                if (speed == 0 && !isWindmill())
                    return;
                assemble();
            }
        }

        if (!running)
            return;

        if (!(movedContraption != null && movedContraption.isStalled())) {
            float angularSpeed = getAngularSpeed();
            if (sequencedAngleLimit >= 0) {
                angularSpeed = (float) MathHelper.clamp(angularSpeed, -sequencedAngleLimit, sequencedAngleLimit);
                sequencedAngleLimit = Math.max(0, sequencedAngleLimit - Math.abs(angularSpeed));
            }
            angle = (angle + angularSpeed) % 360;
        }

        applyRotation();
    }

    public boolean isNearInitialAngle() {
        return Math.abs(angle) < 22.5 || Math.abs(angle) > 360 - 22.5;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (movedContraption != null && !world.isClient())
            sendData();
    }

    protected void applyRotation() {
        if (movedContraption == null)
            return;
        movedContraption.setAngle(angle);
        BlockState blockState = getCachedState();
        if (blockState.contains(Properties.FACING))
            movedContraption.setRotationAxis(blockState.get(Properties.FACING).getAxis());
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        BlockState blockState = getCachedState();
        if (!(contraption.getContraption() instanceof BearingContraption))
            return;
        if (!blockState.contains(BearingBlock.FACING))
            return;

        this.movedContraption = contraption;
        markDirty();
        BlockPos anchor = pos.offset(blockState.get(BearingBlock.FACING));
        movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
        if (!world.isClient()) {
            this.running = true;
            sendData();
        }
    }

    @Override
    public void onStall() {
        if (!world.isClient())
            sendData();
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return movedContraption == contraption;
    }

    public boolean isRunning() {
        return running;
    }

    public void setAngle(float forcedAngle) {
        angle = forcedAngle;
    }

    public ControlledContraptionEntity getMovedContraption() {
        return movedContraption;
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }
}
