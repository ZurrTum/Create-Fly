package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ShoppingList(@Unmodifiable List<IntAttached<BlockPos>> purchases, UUID shopOwner, UUID shopNetwork) {
    public static final Codec<ShoppingList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        IntAttached.codec(BlockPos.CODEC).listOf().fieldOf("purchases").forGetter(ShoppingList::purchases),
        Uuids.INT_STREAM_CODEC.fieldOf("shop_owner").forGetter(ShoppingList::shopOwner),
        Uuids.INT_STREAM_CODEC.fieldOf("shop_network").forGetter(ShoppingList::shopNetwork)
    ).apply(instance, ShoppingList::new));

    public static final PacketCodec<PacketByteBuf, ShoppingList> STREAM_CODEC = PacketCodec.tuple(
        CatnipStreamCodecBuilders.list(IntAttached.streamCodec(BlockPos.PACKET_CODEC)),
        ShoppingList::purchases,
        Uuids.PACKET_CODEC,
        ShoppingList::shopOwner,
        Uuids.PACKET_CODEC,
        ShoppingList::shopNetwork,
        ShoppingList::new
    );

    public ShoppingList duplicate() {
        return new ShoppingList(
            new ArrayList<>(purchases.stream().map(ia -> IntAttached.with(ia.getFirst(), ia.getSecond())).toList()),
            shopOwner,
            shopNetwork
        );
    }

    public int getPurchases(BlockPos clothPos) {
        for (IntAttached<BlockPos> entry : purchases)
            if (clothPos.equals(entry.getValue()))
                return entry.getFirst();
        return 0;
    }

    public Couple<InventorySummary> bakeEntries(WorldAccess level, @Nullable BlockPos clothPosToIgnore) {
        InventorySummary input = new InventorySummary();
        InventorySummary output = new InventorySummary();

        for (IntAttached<BlockPos> entry : purchases) {
            if (clothPosToIgnore != null && clothPosToIgnore.equals(entry.getValue()))
                continue;
            if (!(level.getBlockEntity(entry.getValue()) instanceof TableClothBlockEntity dcbe))
                continue;
            input.add(dcbe.getPaymentItem(), dcbe.getPaymentAmount() * entry.getFirst());
            for (BigItemStack stackEntry : dcbe.requestData.encodedRequest().stacks())
                output.add(stackEntry.stack, stackEntry.count * entry.getFirst());
        }

        return Couple.create(output, input);
    }

    public static class Mutable {
        private final List<IntAttached<BlockPos>> purchases = new ArrayList<>();
        private final UUID shopOwner;
        private final UUID shopNetwork;

        public Mutable(ShoppingList list) {
            this.purchases.addAll(list.purchases);
            this.shopOwner = list.shopOwner;
            this.shopNetwork = list.shopNetwork;
        }

        // Y value of clothPos is pixel perfect (x16)
        public void addPurchases(BlockPos clothPos, int amount) {
            for (IntAttached<BlockPos> entry : purchases) {
                if (clothPos.equals(entry.getValue())) {
                    entry.setFirst(entry.getFirst() + amount);
                    return;
                }
            }
            purchases.add(IntAttached.with(amount, clothPos));
        }

        public ShoppingList toImmutable() {
            return new ShoppingList(purchases, shopOwner, shopNetwork);
        }
    }
}
