package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.bearing.ClockworkContraption.HandType;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ClockworkBearingBlockEntity extends KineticBlockEntity implements IBearingBlockEntity {

    protected ControlledContraptionEntity hourHand;
    protected ControlledContraptionEntity minuteHand;
    protected float hourAngle;
    protected float minuteAngle;
    protected float clientHourAngleDiff;
    protected float clientMinuteAngleDiff;

    protected boolean running;
    protected boolean assembleNextTick;
    protected AssemblyException lastException;
    protected ServerScrollOptionBehaviour<ClockHands> operationMode;

    private float prevForcedAngle;

    public ClockworkBearingBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CLOCKWORK_BEARING, pos, state);
        setLazyTickRate(3);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        operationMode = new ServerScrollOptionBehaviour<>(ClockHands.class, this);
        behaviours.add(operationMode);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CLOCKWORK_BEARING);
    }

    @Override
    public boolean isWoodenTop() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient) {
            prevForcedAngle = hourAngle;
            clientMinuteAngleDiff /= 2;
            clientHourAngleDiff /= 2;
        }

        if (!world.isClient && assembleNextTick) {
            assembleNextTick = false;
            if (running) {
                boolean canDisassemble = true;
                if (speed == 0 && (canDisassemble || hourHand == null || hourHand.getContraption().getBlocks().isEmpty())) {
                    if (hourHand != null)
                        hourHand.getContraption().stop(world);
                    if (minuteHand != null)
                        minuteHand.getContraption().stop(world);
                    disassemble();
                }
                return;
            } else
                assemble();
            return;
        }

        if (!running)
            return;

        if (!(hourHand != null && hourHand.isStalled())) {
            float newAngle = hourAngle + getHourArmSpeed();
            hourAngle = newAngle % 360;
        }

        if (!(minuteHand != null && minuteHand.isStalled())) {
            float newAngle = minuteAngle + getMinuteArmSpeed();
            minuteAngle = newAngle % 360;
        }

        applyRotations();
    }

    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    protected void applyRotations() {
        BlockState blockState = getCachedState();
        Axis axis = Axis.X;

        if (blockState.contains(Properties.FACING))
            axis = blockState.get(Properties.FACING).getAxis();

        if (hourHand != null) {
            hourHand.setAngle(hourAngle);
            hourHand.setRotationAxis(axis);
        }
        if (minuteHand != null) {
            minuteHand.setAngle(minuteAngle);
            minuteHand.setRotationAxis(axis);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (hourHand != null && !world.isClient)
            sendData();
    }

    public float getHourArmSpeed() {
        float speed = getAngularSpeed() / 2f;

        if (speed != 0) {
            ClockHands mode = ClockHands.values()[operationMode.getValue()];
            float hourTarget = mode == ClockHands.HOUR_FIRST ? getHourTarget(false) : mode == ClockHands.MINUTE_FIRST ? getMinuteTarget() : getHourTarget(
                true);
            float shortestAngleDiff = AngleHelper.getShortestAngleDiff(hourAngle, hourTarget);
            if (shortestAngleDiff < 0) {
                speed = Math.max(speed, shortestAngleDiff);
            } else {
                speed = Math.min(-speed, shortestAngleDiff);
            }
        }

        return speed + clientHourAngleDiff / 3f;
    }

    public float getMinuteArmSpeed() {
        float speed = getAngularSpeed();

        if (speed != 0) {
            ClockHands mode = ClockHands.values()[operationMode.getValue()];
            float minuteTarget = mode == ClockHands.MINUTE_FIRST ? getHourTarget(false) : getMinuteTarget();
            float shortestAngleDiff = AngleHelper.getShortestAngleDiff(minuteAngle, minuteTarget);
            if (shortestAngleDiff < 0) {
                speed = Math.max(speed, shortestAngleDiff);
            } else {
                speed = Math.min(-speed, shortestAngleDiff);
            }
        }

        return speed + clientMinuteAngleDiff / 3f;
    }

    protected float getHourTarget(boolean cycle24) {
        boolean isNatural = world.getDimension().natural();
        int dayTime = (int) ((world.getTimeOfDay() * (isNatural ? 1 : 24)) % 24000);
        int hours = (dayTime / 1000 + 6) % 24;
        int offset = getCachedState().get(ClockworkBearingBlock.FACING).getDirection().offset();
        return offset * -360 / (cycle24 ? 24f : 12f) * (hours % (cycle24 ? 24 : 12));
    }

    protected float getMinuteTarget() {
        boolean isNatural = world.getDimension().natural();
        int dayTime = (int) ((world.getTimeOfDay() * (isNatural ? 1 : 24)) % 24000);
        int minutes = (dayTime % 1000) * 60 / 1000;
        int offset = getCachedState().get(ClockworkBearingBlock.FACING).getDirection().offset();
        return offset * -360 / 60f * (minutes);
    }

    public float getAngularSpeed() {
        float speed = -Math.abs(getSpeed() * 3 / 10f);
        if (world.isClient)
            speed *= AllClientHandle.INSTANCE.getServerSpeed();
        return speed;
    }

    public void assemble() {
        if (!(world.getBlockState(pos).getBlock() instanceof ClockworkBearingBlock))
            return;

        Direction direction = getCachedState().get(Properties.FACING);

        // Collect Construct
        Pair<ClockworkContraption, ClockworkContraption> contraption;
        try {
            contraption = ClockworkContraption.assembleClockworkAt(world, pos, direction);
            lastException = null;
        } catch (AssemblyException e) {
            lastException = e;
            sendData();
            return;
        }
        if (contraption == null)
            return;
        if (contraption.getLeft() == null)
            return;
        if (contraption.getLeft().getBlocks().isEmpty())
            return;
        BlockPos anchor = pos.offset(direction);

        contraption.getLeft().removeBlocksFromWorld(world, BlockPos.ORIGIN);
        hourHand = ControlledContraptionEntity.create(world, this, contraption.getLeft());
        hourHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        hourHand.setRotationAxis(direction.getAxis());
        world.spawnEntity(hourHand);

        if (contraption.getLeft().containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);

        if (contraption.getRight() != null) {
            anchor = pos.offset(direction, contraption.getRight().offset + 1);
            contraption.getRight().removeBlocksFromWorld(world, BlockPos.ORIGIN);
            minuteHand = ControlledContraptionEntity.create(world, this, contraption.getRight());
            minuteHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
            minuteHand.setRotationAxis(direction.getAxis());
            world.spawnEntity(minuteHand);

            if (contraption.getRight().containsBlockBreakers())
                award(AllAdvancements.CONTRAPTION_ACTORS);
        }

        award(AllAdvancements.CLOCKWORK_BEARING);

        // Run
        running = true;
        hourAngle = 0;
        minuteAngle = 0;
        sendData();
    }

    public void disassemble() {
        if (!running && hourHand == null && minuteHand == null)
            return;

        hourAngle = 0;
        minuteAngle = 0;
        applyRotations();

        if (hourHand != null) {
            hourHand.disassemble();
        }
        if (minuteHand != null)
            minuteHand.disassemble();

        hourHand = null;
        minuteHand = null;
        running = false;
        sendData();
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        if (!(contraption.getContraption() instanceof ClockworkContraption cc))
            return;

        markDirty();
        Direction facing = getCachedState().get(Properties.FACING);
        BlockPos anchor = pos.offset(facing, cc.offset + 1);
        if (cc.handType == HandType.HOUR) {
            this.hourHand = contraption;
            hourHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        } else {
            this.minuteHand = contraption;
            minuteHand.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        }
        if (!world.isClient) {
            this.running = true;
            sendData();
        }
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putBoolean("Running", running);
        view.putFloat("HourAngle", hourAngle);
        view.putFloat("MinuteAngle", minuteAngle);
        AssemblyException.write(view, lastException);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        float hourAngleBefore = hourAngle;
        float minuteAngleBefore = minuteAngle;

        running = view.getBoolean("Running", false);
        hourAngle = view.getFloat("HourAngle", 0);
        minuteAngle = view.getFloat("MinuteAngle", 0);
        lastException = AssemblyException.read(view);
        super.read(view, clientPacket);

        if (!clientPacket)
            return;

        if (running) {
            clientHourAngleDiff = AngleHelper.getShortestAngleDiff(hourAngleBefore, hourAngle);
            clientMinuteAngleDiff = AngleHelper.getShortestAngleDiff(minuteAngleBefore, minuteAngle);
            hourAngle = hourAngleBefore;
            minuteAngle = minuteAngleBefore;
        } else {
            hourHand = null;
            minuteHand = null;
        }
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        assembleNextTick = true;
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public float getInterpolatedAngle(float partialTicks) {
        if (isVirtual())
            return MathHelper.lerp(partialTicks, prevForcedAngle, hourAngle);
        if (hourHand == null || hourHand.isStalled())
            partialTicks = 0;
        return MathHelper.lerp(partialTicks, hourAngle, hourAngle + getHourArmSpeed());
    }

    @Override
    public void onStall() {
        if (!world.isClient)
            sendData();
    }

    @Override
    public void remove() {
        if (!world.isClient)
            disassemble();
        super.remove();
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        if (!(contraption.getContraption() instanceof ClockworkContraption cc))
            return false;
        if (cc.handType == HandType.HOUR)
            return this.hourHand == contraption;
        else
            return this.minuteHand == contraption;
    }

    public boolean isRunning() {
        return running;
    }

    public enum ClockHands {
        HOUR_FIRST,
        MINUTE_FIRST,
        HOUR_FIRST_24;
    }

    public void setAngle(float forcedAngle) {
        hourAngle = forcedAngle;
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }
}