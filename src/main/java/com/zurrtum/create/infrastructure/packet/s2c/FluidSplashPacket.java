package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;

public record FluidSplashPacket(BlockPos pos, Fluid fluid) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, FluidSplashPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        FluidSplashPacket::pos,
        PacketCodecs.registryValue(RegistryKeys.FLUID),
        FluidSplashPacket::fluid,
        FluidSplashPacket::new
    );

    @Override
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onFluidSplash(this);
    }

    @Override
    public PacketType<FluidSplashPacket> getPacketType() {
        return AllPackets.FLUID_SPLASH;
    }
}
