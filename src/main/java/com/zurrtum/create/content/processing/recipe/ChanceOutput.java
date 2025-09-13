package com.zurrtum.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public record ChanceOutput(float chance, ItemStack stack) {
    public static Codec<ChanceOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.optionalFieldOf("chance", 1F)
            .forGetter(ChanceOutput::chance), ItemStack.MAP_CODEC.forGetter(ChanceOutput::stack)
    ).apply(instance, ChanceOutput::new));
    public static PacketCodec<RegistryByteBuf, ChanceOutput> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT,
        ChanceOutput::chance,
        ItemStack.PACKET_CODEC,
        ChanceOutput::stack,
        ChanceOutput::new
    );

    @Nullable
    public ItemStack get(Random random) {
        if (chance == 1) {
            return stack.copy();
        }
        int count = stack.getCount();
        for (int i = 0, n = count; i < n; i++) {
            if (random.nextFloat() > chance) {
                count--;
            }
        }
        if (count == 0) {
            return null;
        }
        return stack.copyWithCount(count);
    }
}
