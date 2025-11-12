package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public record PotatoCannonPacket(
    Vec3 location, Vec3 motion, ItemStack item, InteractionHand hand, float pitch, boolean self
) implements ShootGadgetPacket {
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
    public boolean runInMain() {
        return true;
    }

    @Override
    public <T> TriConsumer<AllClientHandle<T>, T, PotatoCannonPacket> callback() {
        return AllClientHandle::onPotatoCannon;
    }

    @Override
    public PacketType<PotatoCannonPacket> type() {
        return AllPackets.POTATO_CANNON;
    }
}
