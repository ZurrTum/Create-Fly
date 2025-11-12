package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.util.TriConsumer;

public record FluidSplashPacket(BlockPos pos, Fluid fluid) implements S2CPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSplashPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        FluidSplashPacket::pos,
        ByteBufCodecs.registry(Registries.FLUID),
        FluidSplashPacket::fluid,
        FluidSplashPacket::new
    );

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, FluidSplashPacket> callback() {
        return AllClientHandle::onFluidSplash;
    }

    @Override
    public PacketType<FluidSplashPacket> type() {
        return AllPackets.FLUID_SPLASH;
    }
}
