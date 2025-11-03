package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PackageOrderData(
    int orderId, int linkIndex, boolean isFinalLink, int fragmentIndex, boolean isFinal, @Nullable PackageOrderWithCrafts orderContext
) {
    public PackageOrderData(
        int orderId,
        int linkIndex,
        boolean isFinalLink,
        int fragmentIndex,
        boolean isFinal,
        Optional<PackageOrderWithCrafts> orderContext
    ) {
        this(orderId, linkIndex, isFinalLink, fragmentIndex, isFinal, orderContext.orElse(null));
    }

    public static final Codec<PackageOrderData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("order_id").forGetter(PackageOrderData::orderId),
        Codec.INT.fieldOf("link_index").forGetter(PackageOrderData::linkIndex),
        Codec.BOOL.fieldOf("is_final_link").forGetter(PackageOrderData::isFinalLink),
        Codec.INT.fieldOf("fragment_index").forGetter(PackageOrderData::fragmentIndex),
        Codec.BOOL.fieldOf("is_final").forGetter(PackageOrderData::isFinal),
        PackageOrderWithCrafts.CODEC.optionalFieldOf("order_context").forGetter(i -> Optional.ofNullable(i.orderContext))
    ).apply(instance, PackageOrderData::new));

    public static final PacketCodec<RegistryByteBuf, PackageOrderData> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER,
        PackageOrderData::orderId,
        PacketCodecs.INTEGER,
        PackageOrderData::linkIndex,
        PacketCodecs.BOOLEAN,
        PackageOrderData::isFinalLink,
        PacketCodecs.INTEGER,
        PackageOrderData::fragmentIndex,
        PacketCodecs.BOOLEAN,
        PackageOrderData::isFinal,
        CatnipStreamCodecBuilders.nullable(PackageOrderWithCrafts.STREAM_CODEC),
        PackageOrderData::orderContext,
        PackageOrderData::new
    );
}