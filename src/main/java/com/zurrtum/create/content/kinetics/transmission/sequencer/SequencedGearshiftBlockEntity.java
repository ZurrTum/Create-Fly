package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class SequencedGearshiftBlockEntity extends SplitShaftBlockEntity {

    public Vector<Instruction> instructions;
    int currentInstruction;
    int currentInstructionDuration;
    float currentInstructionProgress;
    int timer;
    boolean poweredPreviously;

    public AbstractComputerBehaviour computerBehaviour;

    public record SequenceContext(SequencerInstructions instruction, double relativeValue) {
        public static Codec<SequenceContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SequencerInstructions.CODEC.fieldOf("Mode").forGetter(SequenceContext::instruction),
                Codec.DOUBLE.fieldOf("Value").forGetter(SequenceContext::relativeValue)
        ).apply(instance, SequenceContext::new));

        public static SequenceContext fromGearshift(SequencerInstructions instruction, double kineticSpeed, int absoluteValue) {
            return instruction.needsPropagation() ? new SequenceContext(instruction, kineticSpeed == 0 ? 0 : absoluteValue / kineticSpeed) : null;
        }

        public double getEffectiveValue(double speedAtTarget) {
            return Math.abs(relativeValue * speedAtTarget);
        }

        public NbtCompound serializeNBT() {
            NbtCompound nbt = new NbtCompound();
            NBTHelper.writeEnum(nbt, "Mode", instruction);
            nbt.putDouble("Value", relativeValue);
            return nbt;
        }

        public static SequenceContext fromNBT(NbtCompound nbt) {
            if (nbt.isEmpty())
                return null;
            return new SequenceContext(NBTHelper.readEnum(nbt, "Mode", SequencerInstructions.class), nbt.getDouble("Value", 0));
        }

    }

    public SequencedGearshiftBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SEQUENCED_GEARSHIFT, pos, state);
        instructions = Instruction.createDefault();
        currentInstruction = -1;
        currentInstructionDuration = -1;
        currentInstructionProgress = 0;
        timer = 0;
        poweredPreviously = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (isIdle())
            return;
        if (world.isClient())
            return;
        if (currentInstructionDuration < 0)
            return;
        if (timer < currentInstructionDuration) {
            timer++;
            currentInstructionProgress += getInstruction(currentInstruction).getTickProgress(speed);
            return;
        }
        run(currentInstruction + 1);
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        if (isIdle())
            return;
        float currentSpeed = Math.abs(speed);
        if (Math.abs(previousSpeed) == currentSpeed)
            return;
        Instruction instruction = getInstruction(currentInstruction);
        if (instruction == null)
            return;
        if (getSpeed() == 0)
            run(-1);

        // Update instruction time with regards to new speed
        currentInstructionDuration = instruction.getDuration(currentInstructionProgress, getTheoreticalSpeed());
        timer = 0;
    }

    public boolean isIdle() {
        return currentInstruction == -1;
    }

    public void onRedstoneUpdate(boolean isPowered, boolean isRunning) {
        if (computerBehaviour.hasAttachedComputer())
            return;
        if (!poweredPreviously && isPowered)
            risingFlank();
        poweredPreviously = isPowered;
        if (!isIdle())
            return;
        if (isPowered == isRunning)
            return;
        if (!world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, getCachedState().with(SequencedGearshiftBlock.STATE, 0), Block.NOTIFY_ALL);
            return;
        }
        if (getSpeed() == 0)
            return;
        run(0);
    }

    public void risingFlank() {
        Instruction instruction = getInstruction(currentInstruction);
        if (instruction == null)
            return;
        if (poweredPreviously)
            return;
        poweredPreviously = true;

        if (Objects.requireNonNull(instruction.onRedstonePulse()) == OnIsPoweredResult.CONTINUE) {
            run(currentInstruction + 1);
        }
    }

    public void run(int instructionIndex) {
        Instruction instruction = getInstruction(instructionIndex);
        if (instruction == null || instruction.instruction == SequencerInstructions.END) {
            if (getModifier() != 0)
                detachKinetics();
            currentInstruction = -1;
            currentInstructionDuration = -1;
            currentInstructionProgress = 0;
            sequenceContext = null;
            timer = 0;
            if (!world.isReceivingRedstonePower(pos))
                world.setBlockState(pos, getCachedState().with(SequencedGearshiftBlock.STATE, 0), Block.NOTIFY_ALL);
            else
                sendData();
            return;
        }

        detachKinetics();
        currentInstructionDuration = instruction.getDuration(0, getTheoreticalSpeed());
        currentInstruction = instructionIndex;
        currentInstructionProgress = 0;
        sequenceContext = SequenceContext.fromGearshift(instruction.instruction, getTheoreticalSpeed() * getModifier(), instruction.value);
        timer = 0;
        world.setBlockState(pos, getCachedState().with(SequencedGearshiftBlock.STATE, instructionIndex + 1), Block.NOTIFY_ALL);
    }

    public Instruction getInstruction(int instructionIndex) {
        return instructionIndex >= 0 && instructionIndex < instructions.size() ? instructions.get(instructionIndex) : null;
    }

    @Override
    protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("InstructionIndex", currentInstruction);
        view.putInt("InstructionDuration", currentInstructionDuration);
        view.putFloat("InstructionProgress", currentInstructionProgress);
        view.putInt("Timer", timer);
        view.putBoolean("PrevPowered", poweredPreviously);
        if (!instructions.isEmpty()) {
            WriteView.ListAppender<Instruction> list = view.getListAppender("Instructions", Instruction.CODEC);
            instructions.forEach(list::add);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        currentInstruction = view.getInt("InstructionIndex", 0);
        currentInstructionDuration = view.getInt("InstructionDuration", 0);
        currentInstructionProgress = view.getFloat("InstructionProgress", 0);
        poweredPreviously = view.getBoolean("PrevPowered", false);
        timer = view.getInt("Timer", 0);
        view.getOptionalTypedListView("Instructions", Instruction.CODEC).ifPresentOrElse(
                list -> {
                    instructions = new Vector<>(5);
                    list.forEach(instructions::add);
                }, () -> instructions = Instruction.createDefault()
        );
        super.read(view, clientPacket);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        computerBehaviour.removePeripheral();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (isVirtual())
            return 1;
        return (!hasSource() || face == getSourceFacing()) ? 1 : getModifier();
    }

    public int getModifier() {
        if (currentInstruction >= instructions.size())
            return 0;
        return isIdle() ? 0 : instructions.get(currentInstruction).getSpeedModifier();
    }

    public Vector<Instruction> getInstructions() {
        return this.instructions;
    }

}