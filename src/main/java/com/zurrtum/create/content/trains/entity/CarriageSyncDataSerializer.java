package com.zurrtum.create.content.trains.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;

public class CarriageSyncDataSerializer implements EntityDataSerializer<CarriageSyncData> {
    private static final StreamCodec<FriendlyByteBuf, CarriageSyncData> CODEC = StreamCodec.ofMember(CarriageSyncData::write, CarriageSyncData::new);

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, CarriageSyncData> codec() {
        return CODEC;
    }

    @Override
    public CarriageSyncData copy(CarriageSyncData data) {
        return data.copy();
    }
}
