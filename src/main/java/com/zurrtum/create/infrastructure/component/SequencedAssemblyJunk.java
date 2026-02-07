package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;
import java.util.Random;

public record SequencedAssemblyJunk(float chance, List<ProcessingOutput> junks) {
    public static final Codec<SequencedAssemblyJunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("chance").forGetter(SequencedAssemblyJunk::chance),
        ProcessingOutput.CODEC.listOf().fieldOf("junks").forGetter(SequencedAssemblyJunk::junks)
    ).apply(instance, SequencedAssemblyJunk::new));
    public static final PacketCodec<RegistryByteBuf, SequencedAssemblyJunk> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT,
        SequencedAssemblyJunk::chance,
        ProcessingOutput.STREAM_CODEC.collect(PacketCodecs.toList()),
        SequencedAssemblyJunk::junks,
        SequencedAssemblyJunk::new
    );
    public static final Random random = new Random();

    public boolean hasJunk() {
        return random.nextFloat() > chance;
    }

    public ItemStack getJunk() {
        if (junks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        float totalWeight = 0;
        for (ProcessingOutput junk : junks) {
            totalWeight += junk.chance();
        }
        float number = random.nextFloat() * totalWeight;
        for (ProcessingOutput junk : junks) {
            number -= junk.chance();
            if (number < 0)
                return junk.create();
        }
        return ItemStack.EMPTY;
    }
}
