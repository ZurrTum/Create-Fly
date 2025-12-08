package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.infrastructure.items.BaseInventory;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record BlueprintPreviewPacket(
    List<ItemStack> available, List<ItemStack> missing, ItemStack result
) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPreviewPacket> CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
        BlueprintPreviewPacket::available,
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
        BlueprintPreviewPacket::missing,
        ItemStack.OPTIONAL_STREAM_CODEC,
        BlueprintPreviewPacket::result,
        BlueprintPreviewPacket::new
    );
    public static final BlueprintPreviewPacket EMPTY = new BlueprintPreviewPacket(List.of(), List.of(), ItemStack.EMPTY);

    public static Object2IntLinkedOpenCustomHashMap<ItemStack> createMap() {
        return new Object2IntLinkedOpenCustomHashMap<>(BaseInventory.ITEM_STACK_HASH_STRATEGY);
    }

    public static Object2IntLinkedOpenCustomHashMap<ItemStack> createMap(Object2IntLinkedOpenCustomHashMap<ItemStack> source) {
        return new Object2IntLinkedOpenCustomHashMap<>(source, BaseInventory.ITEM_STACK_HASH_STRATEGY);
    }

    public BlueprintPreviewPacket(
        Object2IntLinkedOpenCustomHashMap<ItemStack> available,
        Object2IntLinkedOpenCustomHashMap<ItemStack> missing,
        ItemStack result
    ) {
        this(toList(available), toList(missing), result);
    }

    public BlueprintPreviewPacket(Object2IntLinkedOpenCustomHashMap<ItemStack> available, List<ItemStack> missing, ItemStack result) {
        this(toList(available), missing, result);
    }

    private static List<ItemStack> toList(Object2IntLinkedOpenCustomHashMap<ItemStack> map) {
        ObjectBidirectionalIterator<Object2IntMap.Entry<ItemStack>> iterator = map.object2IntEntrySet().fastIterator();
        List<ItemStack> result = new ArrayList<>();
        while (iterator.hasNext()) {
            Object2IntMap.Entry<ItemStack> entry = iterator.next();
            ItemStack stack = entry.getKey();
            int maxCount = stack.getMaxStackSize();
            int count = entry.getIntValue();
            while (count > maxCount) {
                result.add(stack.copyWithCount(maxCount));
                count -= maxCount;
            }
            result.add(stack.copyWithCount(count));
        }
        return result;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        AllClientHandle.INSTANCE.onBlueprintPreview(this);
    }

    @Override
    public PacketType<BlueprintPreviewPacket> type() {
        return AllPackets.BLUEPRINT_PREVIEW;
    }
}