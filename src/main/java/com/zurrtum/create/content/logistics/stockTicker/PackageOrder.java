package com.zurrtum.create.content.logistics.stockTicker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.BigItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.Collections;
import java.util.List;

public record PackageOrder(List<BigItemStack> stacks) {
    public static final Codec<PackageOrder> CODEC = RecordCodecBuilder.create(instance -> instance.group(BigItemStack.CODEC.listOf()
        .fieldOf("entries").forGetter(PackageOrder::stacks)).apply(instance, PackageOrder::new));

    public static final PacketCodec<RegistryByteBuf, PackageOrder> STREAM_CODEC = CatnipStreamCodecBuilders.list(BigItemStack.STREAM_CODEC)
        .xmap(PackageOrder::new, PackageOrder::stacks);

    public static PackageOrder empty() {
        return new PackageOrder(Collections.emptyList());
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }
}
