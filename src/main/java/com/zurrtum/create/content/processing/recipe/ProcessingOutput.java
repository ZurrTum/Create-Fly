package com.zurrtum.create.content.processing.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public record ProcessingOutput(RegistryEntry<Item> item, int count, ComponentChanges components, float chance) {
    public static Codec<ProcessingOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Item.ENTRY_CODEC.fieldOf("id").forGetter(ProcessingOutput::item),
        Codecs.rangedInt(1, 99).optionalFieldOf("count", 1).forGetter(ProcessingOutput::count),
        ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ProcessingOutput::components),
        Codec.FLOAT.optionalFieldOf("chance", 1F).forGetter(ProcessingOutput::chance)
    ).apply(instance, ProcessingOutput::new));
    public static PacketCodec<RegistryByteBuf, ProcessingOutput> STREAM_CODEC = PacketCodec.tuple(
        Item.ENTRY_PACKET_CODEC,
        ProcessingOutput::item,
        PacketCodecs.VAR_INT,
        ProcessingOutput::count,
        ComponentChanges.PACKET_CODEC,
        ProcessingOutput::components,
        PacketCodecs.FLOAT,
        ProcessingOutput::chance,
        ProcessingOutput::new
    );

    public ProcessingOutput(ItemStack stack) {
        this(stack.getRegistryEntry(), stack.getCount(), stack.getComponentChanges(), 1);
    }

    public ProcessingOutput(RegistryEntry<Item> item, int count, int chance) {
        this(item, count, ComponentChanges.EMPTY, chance);
    }

    public ProcessingOutput(RegistryEntry<Item> item, int count) {
        this(item, count, ComponentChanges.EMPTY, 1);
    }

    @SuppressWarnings("deprecation")
    public ProcessingOutput(Item item, int count, float chance) {
        this(item.getRegistryEntry(), count, ComponentChanges.EMPTY, chance);
    }

    public ProcessingOutput(Item item, int count) {
        this(item, count, 1);
    }

    @SuppressWarnings("deprecation")
    public ProcessingOutput(Block block, int count, float chance) {
        this(block.asItem().getRegistryEntry(), count, ComponentChanges.EMPTY, chance);
    }

    public ProcessingOutput(Block block, int count) {
        this(block, count, 1);
    }

    public ItemStack create() {
        return new ItemStack(item, count, components);
    }

    public static void rollOutput(Random random, List<ProcessingOutput> outputs, Consumer<ItemStack> consumer) {
        for (ProcessingOutput output : outputs) {
            ItemStack stack = output.rollOutput(random);
            if (stack != null) {
                consumer.accept(stack);
            }
        }
    }

    @Nullable
    public ItemStack rollOutput(Random random) {
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
