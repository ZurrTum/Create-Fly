package com.zurrtum.create.content.kinetics.belt.transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.logistics.box.PackageItem;

import java.util.Optional;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class TransportedItemStack implements Comparable<TransportedItemStack> {
    public static final Codec<TransportedItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.OPTIONAL_CODEC.fieldOf("Item").forGetter(i -> i.stack),
        Codec.FLOAT.fieldOf("Pos").forGetter(i -> i.beltPosition),
        Codec.FLOAT.fieldOf("PrevPos").forGetter(i -> i.prevBeltPosition),
        Codec.FLOAT.fieldOf("Offset").forGetter(i -> i.sideOffset),
        Codec.FLOAT.fieldOf("PrevOffset").forGetter(i -> i.prevSideOffset),
        Codec.INT.fieldOf("InSegment").forGetter(i -> i.insertedAt),
        Codec.INT.fieldOf("Angle").forGetter(i -> i.angle),
        Direction.CODEC.fieldOf("InDirection").forGetter(i -> i.insertedFrom),
        CreateRegistries.FAN_PROCESSING_TYPE.byNameCodec().optionalFieldOf("FanProcessingType").forGetter(i -> Optional.ofNullable(i.processedBy)),
        Codec.INT.optionalFieldOf("FanProcessingTime", 0).forGetter(i -> i.processingTime),
        Codec.BOOL.fieldOf("Locked").forGetter(i -> i.locked),
        Codec.BOOL.fieldOf("LockedExternally").forGetter(i -> i.lockedExternally)
    ).apply(instance, TransportedItemStack::new));

    private static final Random R = new Random();

    public ItemStack stack;
    public float beltPosition;
    public float sideOffset;
    public int angle;
    public int insertedAt;
    public Direction insertedFrom;
    public boolean locked;
    public boolean lockedExternally;

    public float prevBeltPosition;
    public float prevSideOffset;

    public FanProcessingType processedBy;
    public int processingTime;

    public TransportedItemStack(ItemStack stack) {
        this.stack = stack;
        if (PackageItem.isPackage(stack)) {
            angle = R.nextInt(4) * 90 + R.nextInt(20) - 10;
        } else {
            boolean centered = BeltHelper.isItemUpright(stack);
            angle = centered ? 180 : R.nextInt(360);
        }
        sideOffset = prevSideOffset = getTargetSideOffset();
        insertedFrom = Direction.UP;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private TransportedItemStack(
        ItemStack stack,
        float beltPosition,
        float prevBeltPosition,
        float sideOffset,
        float prevSideOffset,
        int insertedAt,
        int angle,
        Direction insertedFrom,
        Optional<FanProcessingType> processedBy,
        int processingTime,
        boolean locked,
        boolean lockedExternally
    ) {
        this.stack = stack;
        this.beltPosition = beltPosition;
        this.prevBeltPosition = prevBeltPosition;
        this.sideOffset = sideOffset;
        this.prevSideOffset = prevSideOffset;
        this.insertedAt = insertedAt;
        this.angle = angle;
        this.insertedFrom = insertedFrom;
        this.locked = locked;
        this.lockedExternally = lockedExternally;
        this.processedBy = processedBy.orElse(null);
        this.processingTime = processingTime;
    }

    public float getTargetSideOffset() {
        return (angle - 180) / (360 * 3f);
    }

    @Override
    public int compareTo(TransportedItemStack o) {
        return beltPosition < o.beltPosition ? 1 : beltPosition > o.beltPosition ? -1 : 0;
    }

    public TransportedItemStack getSimilar() {
        TransportedItemStack copy = new TransportedItemStack(stack.copy());
        copy.beltPosition = beltPosition;
        copy.insertedAt = insertedAt;
        copy.insertedFrom = insertedFrom;
        copy.prevBeltPosition = prevBeltPosition;
        copy.prevSideOffset = prevSideOffset;
        copy.processedBy = processedBy;
        copy.processingTime = processingTime;
        return copy;
    }

    public TransportedItemStack copy() {
        TransportedItemStack copy = getSimilar();
        copy.angle = angle;
        copy.sideOffset = sideOffset;
        return copy;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        if (!stack.isEmpty()) {
            nbt.store("Item", ItemStack.CODEC, stack);
        }
        nbt.putFloat("Pos", beltPosition);
        nbt.putFloat("PrevPos", prevBeltPosition);
        nbt.putFloat("Offset", sideOffset);
        nbt.putFloat("PrevOffset", prevSideOffset);
        nbt.putInt("InSegment", insertedAt);
        nbt.putInt("Angle", angle);
        nbt.putInt("InDirection", insertedFrom.get3DDataValue());

        if (processedBy != null) {
            Identifier key = CreateRegistries.FAN_PROCESSING_TYPE.getKey(processedBy);
            if (key == null)
                throw new IllegalArgumentException("Could not get id for FanProcessingType " + processedBy + "!");

            nbt.putString("FanProcessingType", key.toString());
            nbt.putInt("FanProcessingTime", processingTime);
        }

        if (locked)
            nbt.putBoolean("Locked", locked);
        if (lockedExternally)
            nbt.putBoolean("LockedExternally", lockedExternally);
        return nbt;
    }

    public static TransportedItemStack read(CompoundTag nbt, HolderLookup.Provider registries) {
        ItemStack source = nbt.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        TransportedItemStack stack = new TransportedItemStack(source);
        stack.beltPosition = nbt.getFloatOr("Pos", 0);
        stack.prevBeltPosition = nbt.getFloatOr("PrevPos", 0);
        stack.sideOffset = nbt.getFloatOr("Offset", 0);
        stack.prevSideOffset = nbt.getFloatOr("PrevOffset", 0);
        stack.insertedAt = nbt.getIntOr("InSegment", 0);
        stack.angle = nbt.getIntOr("Angle", 0);
        stack.insertedFrom = Direction.from3DDataValue(nbt.getIntOr("InDirection", 0));
        stack.locked = nbt.getBooleanOr("Locked", false);
        stack.lockedExternally = nbt.getBooleanOr("LockedExternally", false);

        if (nbt.contains("FanProcessingType")) {
            stack.processedBy = FanProcessingType.parse(nbt.getStringOr("FanProcessingType", ""));
            stack.processingTime = nbt.getIntOr("FanProcessingTime", 0);
        }

        return stack;
    }

    public void clearFanProcessingData() {
        processedBy = null;
        processingTime = 0;
    }

}
