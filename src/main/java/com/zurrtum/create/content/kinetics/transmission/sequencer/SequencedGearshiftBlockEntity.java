package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SequencedGearshiftBlockEntity extends SplitShaftBlockEntity {

    public Vector<Instruction> instructions;
    int currentInstruction;
    int currentInstructionDuration;
    float currentInstructionProgress;
    int timer;
    boolean poweredPreviously;

    //TODO
    //    public AbstractComputerBehaviour computerBehaviour;

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

        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            NBTHelper.writeEnum(nbt, "Mode", instruction);
            nbt.putDouble("Value", relativeValue);
            return nbt;
        }

        public static SequenceContext fromNBT(CompoundTag nbt) {
            if (nbt.isEmpty())
                return null;
            return new SequenceContext(NBTHelper.readEnum(nbt, "Mode", SequencerInstructions.class), nbt.getDoubleOr("Value", 0));
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

    //TODO
    //    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    //        if (Mods.COMPUTERCRAFT.isLoaded()) {
    //            event.registerBlockEntity(
    //                PeripheralCapability.get(),
    //                AllBlockEntityTypes.SEQUENCED_GEARSHIFT.get(),
    //                (be, context) -> be.computerBehaviour.getPeripheralCapability()
    //            );
    //        }
    //    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        //TODO
        //        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (isIdle())
            return;
        if (level.isClientSide())
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
        //TODO
        //        if (computerBehaviour.hasAttachedComputer())
        //            return;
        if (!poweredPreviously && isPowered)
            risingFlank();
        poweredPreviously = isPowered;
        if (!isIdle())
            return;
        if (isPowered == isRunning)
            return;
        if (!level.hasNeighborSignal(worldPosition)) {
            level.setBlock(worldPosition, getBlockState().setValue(SequencedGearshiftBlock.STATE, 0), Block.UPDATE_ALL);
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
            if (!level.hasNeighborSignal(worldPosition))
                level.setBlock(worldPosition, getBlockState().setValue(SequencedGearshiftBlock.STATE, 0), Block.UPDATE_ALL);
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
        level.setBlock(worldPosition, getBlockState().setValue(SequencedGearshiftBlock.STATE, instructionIndex + 1), Block.UPDATE_ALL);
    }

    public Instruction getInstruction(int instructionIndex) {
        return instructionIndex >= 0 && instructionIndex < instructions.size() ? instructions.get(instructionIndex) : null;
    }

    @Override
    protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.putInt("InstructionIndex", currentInstruction);
        view.putInt("InstructionDuration", currentInstructionDuration);
        view.putFloat("InstructionProgress", currentInstructionProgress);
        view.putInt("Timer", timer);
        view.putBoolean("PrevPowered", poweredPreviously);
        if (!instructions.isEmpty()) {
            ValueOutput.TypedOutputList<Instruction> list = view.list("Instructions", Instruction.CODEC);
            instructions.forEach(list::add);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        currentInstruction = view.getIntOr("InstructionIndex", 0);
        currentInstructionDuration = view.getIntOr("InstructionDuration", 0);
        currentInstructionProgress = view.getFloatOr("InstructionProgress", 0);
        poweredPreviously = view.getBooleanOr("PrevPowered", false);
        timer = view.getIntOr("Timer", 0);
        view.list("Instructions", Instruction.CODEC).ifPresentOrElse(
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
        //TODO
        //        computerBehaviour.removePeripheral();
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