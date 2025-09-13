package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.PacketType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

public record FluidSplashPacket(BlockPos pos, Fluid fluid) implements S2CPacket {
    public static final PacketCodec<RegistryByteBuf, FluidSplashPacket> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC,
        FluidSplashPacket::pos,
        PacketCodecs.registryValue(RegistryKeys.FLUID),
        FluidSplashPacket::fluid,
        FluidSplashPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, FluidSplashPacket> callback() {
        return AllClientHandle::onFluidSplash;
    }

    @Override
    public PacketType<FluidSplashPacket> getPacketType() {
        return AllPackets.FLUID_SPLASH;
    }
}
