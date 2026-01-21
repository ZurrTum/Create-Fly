package com.zurrtum.create.content.kinetics.chainConveyor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class ChainConveyorPackage {
    public static final Codec<ChainConveyorPackage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("Position")
            .forGetter(i -> i.chainPosition), ItemStack.OPTIONAL_CODEC.fieldOf("Item").forGetter(i -> i.item)
    ).apply(instance, ChainConveyorPackage::new));
    public static final Codec<ChainConveyorPackage> CLIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("Position")
            .forGetter(i -> i.chainPosition),
        ItemStack.OPTIONAL_CODEC.fieldOf("Item").forGetter(i -> i.item),
        Codec.INT.optionalFieldOf("NetID", 0).forGetter(i -> i.netId)
    ).apply(
        instance, (chainPosition, item, netId) -> {
            if (netId > 0) {
                return new ChainConveyorPackage(chainPosition, item, netId);
            } else {
                return new ChainConveyorPackage(chainPosition, item);
            }
        }
    ));

    // Server creates unique ids for chain boxes
    public static final AtomicInteger netIdGenerator = new AtomicInteger();

    public float chainPosition;
    public ItemStack item;
    public int netId;
    public boolean justFlipped;

    public @Nullable Vec3 worldPosition;
    public float yaw;
    public @Nullable Object physicsData;

    public ChainConveyorPackage(float chainPosition, ItemStack item) {
        this(chainPosition, item, netIdGenerator.incrementAndGet());
    }

    public ChainConveyorPackage(float chainPosition, ItemStack item, int netId) {
        this.chainPosition = chainPosition;
        this.item = item;
        this.netId = netId;
    }
}
