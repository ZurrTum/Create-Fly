package com.zurrtum.create.infrastructure.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;

/**
 * Package ordering context containing additional information of package orders.
 */
public record PackageOrderWithCrafts(PackageOrder orderedStacks, List<CraftingEntry> orderedCrafts) {

    public static final Codec<PackageOrderWithCrafts> CODEC = Codec.withAlternative(

        RecordCodecBuilder.create(i -> i.group(
            PackageOrder.CODEC.fieldOf("ordered_stacks").forGetter(PackageOrderWithCrafts::orderedStacks),
            CraftingEntry.CODEC.listOf().fieldOf("ordered_crafts").forGetter(PackageOrderWithCrafts::orderedCrafts)
        ).apply(i, PackageOrderWithCrafts::new)),

        // Legacy format (6.0.0 - 6.0.2)
        RecordCodecBuilder.create(instance -> instance.group(BigItemStack.CODEC.listOf().fieldOf("entries").forGetter(PackageOrderWithCrafts::stacks))
            .apply(instance, PackageOrderWithCrafts::simple))

    );

    public static final PacketCodec<RegistryByteBuf, PackageOrderWithCrafts> STREAM_CODEC = PacketCodec.tuple(
        PackageOrder.STREAM_CODEC,
        s -> s.orderedStacks,
        CatnipStreamCodecBuilders.list(CraftingEntry.STREAM_CODEC),
        s -> s.orderedCrafts,
        PackageOrderWithCrafts::new
    );

    public static PackageOrderWithCrafts empty() {
        return new PackageOrderWithCrafts(PackageOrder.empty(), List.of());
    }

    public static PackageOrderWithCrafts simple(List<BigItemStack> orderedStacks) {
        return new PackageOrderWithCrafts(new PackageOrder(orderedStacks), List.of());
    }

    public static PackageOrderWithCrafts singleRecipe(List<BigItemStack> pattern) {
        return new PackageOrderWithCrafts(PackageOrder.empty(), List.of(new CraftingEntry(new PackageOrder(pattern), 1)));
    }

    public record CraftingEntry(PackageOrder pattern, int count) {

        public static final Codec<CraftingEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            PackageOrder.CODEC.fieldOf("pattern")
                .forGetter(CraftingEntry::pattern), Codec.INT.fieldOf("count").forGetter(CraftingEntry::count)
        ).apply(i, CraftingEntry::new));

        public static final PacketCodec<RegistryByteBuf, CraftingEntry> STREAM_CODEC = PacketCodec.tuple(
            PackageOrder.STREAM_CODEC,
            CraftingEntry::pattern,
            PacketCodecs.VAR_INT,
            CraftingEntry::count,
            CraftingEntry::new
        );


    }

    public static boolean hasCraftingInformation(PackageOrderWithCrafts context) {
        if (context == null)
            return false;
        // Only a valid crafting packet if it contains exactly one recipe
        return context.orderedCrafts.size() == 1;
    }

    public List<BigItemStack> getCraftingInformation() {
        return orderedCrafts.get(0).pattern.stacks();
    }

    public List<BigItemStack> stacks() {
        return orderedStacks.stacks();
    }

    public boolean isEmpty() {
        return orderedStacks.isEmpty();
    }

    public boolean orderedStacksMatchOrderedRecipes() {
        if (orderedCrafts.isEmpty())
            return false;

        InventorySummary stacks = new InventorySummary();
        InventorySummary crafts = new InventorySummary();

        stacks().forEach(stacks::add);
        orderedCrafts.forEach(ce -> ce.pattern.stacks().forEach(bis -> crafts.add(new BigItemStack(bis.stack, bis.count * ce.count))));

        List<BigItemStack> stackEntries = stacks.getStacks();
        if (stackEntries.size() != crafts.getStacks().size())
            return false;
        for (BigItemStack bis : stackEntries)
            if (crafts.getCountOf(bis.stack) != bis.count)
                return false;
        return true;
    }

}
