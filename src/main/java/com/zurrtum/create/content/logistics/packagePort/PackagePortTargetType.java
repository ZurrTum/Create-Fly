package com.zurrtum.create.content.logistics.packagePort;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public interface PackagePortTargetType {
    MapCodec<? extends PackagePortTarget> codec();

    PacketCodec<? super RegistryByteBuf, ? extends PackagePortTarget> packetCodec();
}
