package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Vector;

public class Instruction {
    public static final Codec<Instruction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SequencerInstructions.CODEC.fieldOf("Type").forGetter(instruction -> instruction.instruction),
        InstructionSpeedModifiers.CODEC.fieldOf("Modifier").forGetter(instruction -> instruction.speedModifier),
        Codec.INT.fieldOf("Value").forGetter(instruction -> instruction.value)
    ).apply(instance, Instruction::new));
    public static final StreamCodec<FriendlyByteBuf, Instruction> STREAM_CODEC = StreamCodec.composite(
        SequencerInstructions.STREAM_CODEC,
        instruction -> instruction.instruction,
        InstructionSpeedModifiers.STREAM_CODEC,
        instruction -> instruction.speedModifier,
        ByteBufCodecs.VAR_INT,
        instruction -> instruction.value,
        Instruction::new
    );

    public SequencerInstructions instruction;
    public InstructionSpeedModifiers speedModifier;
    public int value;

    public Instruction(SequencerInstructions instruction) {
        this(instruction, 1);
    }

    public Instruction(SequencerInstructions instruction, int value) {
        this(instruction, InstructionSpeedModifiers.FORWARD, value);
    }

    public Instruction(SequencerInstructions instruction, InstructionSpeedModifiers speedModifier, int value) {
        this.instruction = instruction;
        this.speedModifier = speedModifier;
        this.value = value;
    }

    int getDuration(float currentProgress, float speed) {
        speed *= speedModifier.value;
        speed = Math.abs(speed);
        double target = value - currentProgress;

        return switch (instruction) {

            // Always overshoot, target will stop early
            case TURN_ANGLE -> (int) Math.ceil(target / KineticBlockEntity.convertToAngular(speed)) + 2;
            case TURN_DISTANCE -> (int) Math.ceil(target / KineticBlockEntity.convertToLinear(speed)) + 2;

            // Timing instructions
            case DELAY -> (int) target;
            case AWAIT -> -1;
            default -> 0;
        };
    }

    float getTickProgress(float speed) {
        return switch (instruction) {
            case TURN_ANGLE -> KineticBlockEntity.convertToAngular(speed);
            case TURN_DISTANCE -> KineticBlockEntity.convertToLinear(speed);
            case DELAY -> 1;
            default -> 0;
        };
    }

    int getSpeedModifier() {
        return switch (instruction) {
            case TURN_ANGLE, TURN_DISTANCE -> speedModifier.value;
            default -> 0;
        };
    }

    OnIsPoweredResult onRedstonePulse() {
        return instruction == SequencerInstructions.AWAIT ? OnIsPoweredResult.CONTINUE : OnIsPoweredResult.NOTHING;
    }

    public static ListTag serializeAll(List<Instruction> instructions) {
        ListTag list = new ListTag();
        instructions.forEach(i -> list.add(i.serialize()));
        return list;
    }

    public static Vector<Instruction> deserializeAll(ListTag list) {
        if (list.isEmpty())
            return createDefault();
        Vector<Instruction> instructions = new Vector<>(5);
        list.forEach(inbt -> instructions.add(deserialize((CompoundTag) inbt)));
        return instructions;
    }

    public static Vector<Instruction> createDefault() {
        Vector<Instruction> instructions = new Vector<>(5);
        instructions.add(new Instruction(SequencerInstructions.TURN_ANGLE, 90));
        instructions.add(new Instruction(SequencerInstructions.END));
        return instructions;
    }

    CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        NBTHelper.writeEnum(tag, "Type", instruction);
        NBTHelper.writeEnum(tag, "Modifier", speedModifier);
        tag.putInt("Value", value);
        return tag;
    }

    static Instruction deserialize(CompoundTag tag) {
        Instruction instruction = new Instruction(NBTHelper.readEnum(tag, "Type", SequencerInstructions.class));
        instruction.speedModifier = NBTHelper.readEnum(tag, "Modifier", InstructionSpeedModifiers.class);
        instruction.value = tag.getIntOr("Value", 0);
        return instruction;
    }

}