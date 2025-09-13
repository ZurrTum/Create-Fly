package com.zurrtum.create.content.trains.entity;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class CarriageSyncDataSerializer implements TrackedDataHandler<CarriageSyncData> {
    private static final PacketCodec<PacketByteBuf, CarriageSyncData> CODEC = PacketCodec.of(CarriageSyncData::write, CarriageSyncData::new);

    @Override
    public PacketCodec<? super RegistryByteBuf, CarriageSyncData> codec() {
        return CODEC;
    }

    @Override
    public CarriageSyncData copy(CarriageSyncData data) {
        return data.copy();
    }
}
