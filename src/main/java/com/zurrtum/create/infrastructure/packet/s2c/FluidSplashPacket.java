package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.material.Fluid;

public record FluidSplashPacket(BlockPos pos, Fluid fluid) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSplashPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        FluidSplashPacket::pos,
        ByteBufCodecs.registry(Registries.FLUID),
        FluidSplashPacket::fluid,
        FluidSplashPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onFluidSplash(this);
    }

    @Override
    public PacketType<FluidSplashPacket> type() {
        return AllPackets.FLUID_SPLASH;
    }
}
