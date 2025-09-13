package com.zurrtum.create.content.kinetics.transmission.sequencer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;
import java.util.Vector;

public class Instruction {
    public static final Codec<Instruction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SequencerInstructions.CODEC.fieldOf("Type").forGetter(instruction -> instruction.instruction),
        InstructionSpeedModifiers.CODEC.fieldOf("Modifier").forGetter(instruction -> instruction.speedModifier),
        Codec.INT.fieldOf("Value").forGetter(instruction -> instruction.value)
    ).apply(instance, Instruction::new));
    public static final PacketCodec<PacketByteBuf, Instruction> STREAM_CODEC = PacketCodec.tuple(
        SequencerInstructions.STREAM_CODEC,
        instruction -> instruction.instruction,
        InstructionSpeedModifiers.STREAM_CODEC,
        instruction -> instruction.speedModifier,
        PacketCodecs.VAR_INT,
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

        switch (instruction) {

            // Always overshoot, target will stop early
            case TURN_ANGLE:
                double degreesPerTick = KineticBlockEntity.convertToAngular(speed);
                return (int) Math.ceil(target / degreesPerTick) + 2;
            case TURN_DISTANCE:
                double metersPerTick = KineticBlockEntity.convertToLinear(speed);
                return (int) Math.ceil(target / metersPerTick) + 2;

            // Timing instructions
            case DELAY:
                return (int) target;
            case AWAIT:
                return -1;
            case END:
            default:
                break;

        }
        return 0;
    }

    float getTickProgress(float speed) {
        switch (instruction) {

            case TURN_ANGLE:
                return KineticBlockEntity.convertToAngular(speed);

            case TURN_DISTANCE:
                return KineticBlockEntity.convertToLinear(speed);

            case DELAY:
                return 1;

            case AWAIT:
            case END:
            default:
                break;

        }
        return 0;
    }

    int getSpeedModifier() {
        switch (instruction) {

            case TURN_ANGLE:
            case TURN_DISTANCE:
                return speedModifier.value;

            case END:
            case DELAY:
            case AWAIT:
            default:
                break;

        }
        return 0;
    }

    OnIsPoweredResult onRedstonePulse() {
        return instruction == SequencerInstructions.AWAIT ? OnIsPoweredResult.CONTINUE : OnIsPoweredResult.NOTHING;
    }

    public static NbtList serializeAll(List<Instruction> instructions) {
        NbtList list = new NbtList();
        instructions.forEach(i -> list.add(i.serialize()));
        return list;
    }

    public static Vector<Instruction> deserializeAll(NbtList list) {
        if (list.isEmpty())
            return createDefault();
        Vector<Instruction> instructions = new Vector<>(5);
        list.forEach(inbt -> instructions.add(deserialize((NbtCompound) inbt)));
        return instructions;
    }

    public static Vector<Instruction> createDefault() {
        Vector<Instruction> instructions = new Vector<>(5);
        instructions.add(new Instruction(SequencerInstructions.TURN_ANGLE, 90));
        instructions.add(new Instruction(SequencerInstructions.END));
        return instructions;
    }

    NbtCompound serialize() {
        NbtCompound tag = new NbtCompound();
        NBTHelper.writeEnum(tag, "Type", instruction);
        NBTHelper.writeEnum(tag, "Modifier", speedModifier);
        tag.putInt("Value", value);
        return tag;
    }

    static Instruction deserialize(NbtCompound tag) {
        Instruction instruction = new Instruction(NBTHelper.readEnum(tag, "Type", SequencerInstructions.class));
        instruction.speedModifier = NBTHelper.readEnum(tag, "Modifier", InstructionSpeedModifiers.class);
        instruction.value = tag.getInt("Value", 0);
        return instruction;
    }

}