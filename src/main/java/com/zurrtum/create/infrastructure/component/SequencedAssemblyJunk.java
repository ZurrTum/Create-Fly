package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ChanceOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public record SequencedAssemblyJunk(float chance, List<ChanceOutput> junks) {
    public static final Codec<SequencedAssemblyJunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("chance").forGetter(SequencedAssemblyJunk::chance),
        ChanceOutput.CODEC.listOf().fieldOf("junks").forGetter(SequencedAssemblyJunk::junks)
    ).apply(instance, SequencedAssemblyJunk::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyJunk> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        SequencedAssemblyJunk::chance,
        ChanceOutput.PACKET_CODEC.apply(ByteBufCodecs.list()),
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
        for (ChanceOutput junk : junks) {
            totalWeight += junk.chance();
        }
        float number = random.nextFloat() * totalWeight;
        for (ChanceOutput junk : junks) {
            number -= junk.chance();
            if (number < 0)
                return junk.stack().create();
        }
        return ItemStack.EMPTY;
    }
}
