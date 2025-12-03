package com.zurrtum.create.infrastructure.packet.s2c;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.infrastructure.items.BaseInventory;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

import java.util.ArrayList;
import java.util.List;

public record BlueprintPreviewPacket(
    List<ItemStack> available, List<ItemStack> missing, ItemStack result
) implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, BlueprintPreviewPacket> CODEC = PacketCodec.tuple(
        ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
        BlueprintPreviewPacket::available,
        ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
        BlueprintPreviewPacket::missing,
        ItemStack.OPTIONAL_PACKET_CODEC,
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
            int maxCount = stack.getMaxCount();
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
    public void apply(ClientPlayPacketListener listener) {
        AllClientHandle.INSTANCE.onBlueprintPreview(this);
    }

    @Override
    public PacketType<BlueprintPreviewPacket> getPacketType() {
        return AllPackets.BLUEPRINT_PREVIEW;
    }
}