package com.zurrtum.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public record ChanceOutput(float chance, ItemStackTemplate stack) {
    public static Codec<ChanceOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.optionalFieldOf("chance", 1F)
            .forGetter(ChanceOutput::chance), ItemStackTemplate.MAP_CODEC.forGetter(ChanceOutput::stack)
    ).apply(instance, ChanceOutput::new));
    public static StreamCodec<RegistryFriendlyByteBuf, ChanceOutput> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        ChanceOutput::chance,
        ItemStackTemplate.STREAM_CODEC,
        ChanceOutput::stack,
        ChanceOutput::new
    );

    @Nullable
    public ItemStack get(RandomSource random) {
        if (chance == 1) {
            return stack.create();
        }
        int count = stack.count();
        for (int i = 0, n = count; i < n; i++) {
            if (random.nextFloat() > chance) {
                count--;
            }
        }
        if (count == 0) {
            return null;
        }
        return new ItemStack(stack.item(), count, stack.components());
    }
}
