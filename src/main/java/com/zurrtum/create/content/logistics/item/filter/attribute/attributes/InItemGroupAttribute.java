package com.zurrtum.create.content.logistics.item.filter.attribute.attributes;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItemAttributeTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InItemGroupAttribute implements ItemAttribute {
    public static final MapCodec<InItemGroupAttribute> CODEC = Registries.ITEM_GROUP.getCodec().xmap(InItemGroupAttribute::new, i -> i.group)
        .fieldOf("value");

    public static final PacketCodec<ByteBuf, InItemGroupAttribute> PACKET_CODEC = CatnipStreamCodecBuilders.nullable(Identifier.PACKET_CODEC)
        .xmap(i -> new InItemGroupAttribute(Registries.ITEM_GROUP.get(i)), i -> i.group == null ? null : Registries.ITEM_GROUP.getId(i.group));

    @Nullable
    private ItemGroup group;

    public InItemGroupAttribute(@Nullable ItemGroup group) {
        this.group = group;
    }

    private static boolean tabContainsItem(ItemGroup tab, ItemStack stack) {
        return tab.contains(stack) || tab.contains(new ItemStack(stack.getItem()));
    }

    @Override
    public boolean appliesTo(ItemStack stack, World world) {
        if (group == null)
            return false;

        if (group.getDisplayStacks().isEmpty() && group.getSearchTabStacks().isEmpty()) {

            try {
                group.updateEntries(new ItemGroup.DisplayContext(world.getEnabledFeatures(), false, world.getRegistryManager()));
            } catch (RuntimeException | LinkageError e) {
                Create.LOGGER.error("Attribute Filter: Item Group {} crashed while building contents.", group.getDisplayName().getString(), e);
                group = null;
                return false;
            }

        }

        return tabContainsItem(group, stack);
    }

    @Override
    public String getTranslationKey() {
        return "in_item_group";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{group == null ? "<none>" : group.getDisplayName().getString()};
    }

    @Override
    public ItemAttributeType getType() {
        return AllItemAttributeTypes.IN_ITEM_GROUP;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InItemGroupAttribute that))
            return false;

        return Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group);
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new InItemGroupAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack stack, World level) {
            List<ItemAttribute> list = new ArrayList<>();

            for (ItemGroup tab : Registries.ITEM_GROUP) {
                if (tab.shouldDisplay() && tab.getType() == ItemGroup.Type.CATEGORY && tabContainsItem(tab, stack)) {
                    list.add(new InItemGroupAttribute(tab));
                }
            }

            return list;
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<? super RegistryByteBuf, ? extends ItemAttribute> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
