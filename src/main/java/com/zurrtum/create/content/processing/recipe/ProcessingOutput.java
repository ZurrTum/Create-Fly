package com.zurrtum.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public record ProcessingOutput(Holder<Item> item, int count, DataComponentPatch components, float chance) {
    public static Codec<ProcessingOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Item.CODEC.fieldOf("id").forGetter(ProcessingOutput::item),
        ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(ProcessingOutput::count),
        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ProcessingOutput::components),
        Codec.FLOAT.optionalFieldOf("chance", 1F).forGetter(ProcessingOutput::chance)
    ).apply(instance, ProcessingOutput::new));
    public static StreamCodec<RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.composite(
        Item.STREAM_CODEC,
        ProcessingOutput::item,
        ByteBufCodecs.VAR_INT,
        ProcessingOutput::count,
        DataComponentPatch.STREAM_CODEC,
        ProcessingOutput::components,
        ByteBufCodecs.FLOAT,
        ProcessingOutput::chance,
        ProcessingOutput::new
    );

    public ProcessingOutput(ItemStack stack) {
        this(stack.getItemHolder(), stack.getCount(), stack.getComponentsPatch(), 1);
    }

    public ProcessingOutput(Holder<Item> item, int count, int chance) {
        this(item, count, DataComponentPatch.EMPTY, chance);
    }

    public ProcessingOutput(Holder<Item> item, int count) {
        this(item, count, DataComponentPatch.EMPTY, 1);
    }

    @SuppressWarnings("deprecation")
    public ProcessingOutput(Item item, int count, float chance) {
        this(item.builtInRegistryHolder(), count, DataComponentPatch.EMPTY, chance);
    }

    public ProcessingOutput(Item item, int count) {
        this(item, count, 1);
    }

    @SuppressWarnings("deprecation")
    public ProcessingOutput(Block block, int count, float chance) {
        this(block.asItem().builtInRegistryHolder(), count, DataComponentPatch.EMPTY, chance);
    }

    public ProcessingOutput(Block block, int count) {
        this(block, count, 1);
    }

    public ItemStack create() {
        return new ItemStack(item, count, components);
    }

    public static void rollOutput(RandomSource random, List<ProcessingOutput> outputs, Consumer<ItemStack> consumer) {
        for (ProcessingOutput output : outputs) {
            ItemStack stack = output.rollOutput(random);
            if (stack != null) {
                consumer.accept(stack);
            }
        }
    }

    @Nullable
    public ItemStack rollOutput(RandomSource random) {
        if (chance == 1) {
            return new ItemStack(item, count, components);
        }
        int count = this.count;
        for (int i = 0, n = count; i < n; i++) {
            if (random.nextFloat() > chance) {
                count--;
            }
        }
        if (count == 0) {
            return null;
        }
        return new ItemStack(item, count, components);
    }
}
