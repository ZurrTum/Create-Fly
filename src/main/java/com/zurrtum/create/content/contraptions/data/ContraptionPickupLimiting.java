package com.zurrtum.create.content.contraptions.data;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;

public class ContraptionPickupLimiting {
    /// The default NBT limit, defined by {@link PacketByteBuf#readNbt()}.
    public static final int NBT_LIMIT = 2_097_152;

    // increased nbt limits provided by other mods.
    public static final int PACKET_FIXER_LIMIT = NBT_LIMIT * 100;
    public static final int XL_PACKETS_LIMIT = Integer.MAX_VALUE;

    // leave some space for the rest of the packet.
    public static final int BUFFER = 20_000;

    // the actual limit to be used
    public static final int LIMIT = Util.make(() -> {
        // the smallest limit needs to be used, as we can't guarantee that all mixins are applied if multiple are present.
        //TODO
        //        if (Mods.PACKETFIXER.isLoaded()) {
        //            return PACKET_FIXER_LIMIT;
        //        } else if (Mods.XLPACKETS.isLoaded()) {
        //            return XL_PACKETS_LIMIT;
        //        }

        // none are present, use vanilla default
        return NBT_LIMIT;
    }) - BUFFER;

    /**
     * @return true if the given NBT is too large for a contraption to be picked up with a wrench.
     */
    public static boolean isTooLargeForPickup(NbtElement data) {
        return nbtSize(data) > LIMIT;
    }

    /**
     * @return the size of the given NBT when read by the client according to {@link NbtSizeTracker}
     */
    private static long nbtSize(NbtElement data) {
        PacketByteBuf test = new PacketByteBuf(Unpooled.buffer());
        test.writeNbt(data);
        NbtSizeTracker sizeTracker = NbtSizeTracker.ofUnlimitedBytes();
        test.readNbt(sizeTracker);
        long size = sizeTracker.getAllocatedBytes();
        test.release();
        return size;
    }
}
