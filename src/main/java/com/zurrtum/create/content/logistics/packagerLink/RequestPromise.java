package com.zurrtum.create.content.logistics.packagerLink;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.logistics.BigItemStack;

import java.util.Comparator;

public class RequestPromise {
    public static final Codec<RequestPromise> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("ticks_existed").forGetter(i -> i.ticksExisted),
        BigItemStack.CODEC.fieldOf("promised_stack").forGetter(i -> i.promisedStack)
    ).apply(instance, RequestPromise::new));

    public int ticksExisted = 0;
    public BigItemStack promisedStack;

    public RequestPromise(BigItemStack promisedStack) {
        this.promisedStack = promisedStack;
    }

    public RequestPromise(int ticksExisted, BigItemStack promisedStack) {
        this.ticksExisted = ticksExisted;
        this.promisedStack = promisedStack;
    }

    public void tick() {
        ticksExisted++;
    }

    public static Comparator<? super RequestPromise> ageComparator() {
        return (i1, i2) -> Integer.compare(i2.ticksExisted, i1.ticksExisted);
    }
}
