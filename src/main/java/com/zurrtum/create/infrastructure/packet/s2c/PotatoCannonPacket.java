package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public record PotatoCannonPacket(
    Vec3 location, Vec3 motion, ItemStack item, InteractionHand hand, float pitch, boolean self
) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, PotatoCannonPacket> CODEC = StreamCodec.composite(
        Vec3.STREAM_CODEC,
        PotatoCannonPacket::location,
        Vec3.STREAM_CODEC,
        PotatoCannonPacket::motion,
        ItemStack.OPTIONAL_STREAM_CODEC,
        PotatoCannonPacket::item,
        CatnipStreamCodecs.HAND,
        PotatoCannonPacket::hand,
        ByteBufCodecs.FLOAT,
        PotatoCannonPacket::pitch,
        ByteBufCodecs.BOOL,
        PotatoCannonPacket::self,
        PotatoCannonPacket::new
    );

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onPotatoCannon(listener, this);
    }

    @Override
    public PacketType<PotatoCannonPacket> type() {
        return AllPackets.POTATO_CANNON;
    }
}
