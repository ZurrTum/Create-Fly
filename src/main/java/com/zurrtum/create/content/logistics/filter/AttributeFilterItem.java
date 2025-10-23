package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.filter.FilterItemStack.AttributeFilterItemStack;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttributeFilterItem extends FilterItem {
    protected AttributeFilterItem(Settings properties) {
        super(properties);
    }

    @Override
    public List<Text> makeSummary(ItemStack filter) {
        List<Text> list = new ArrayList<>();

        AttributeFilterWhitelistMode whitelistMode = filter.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
        list.add((whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ ? Text.translatable(
            "create.gui.attribute_filter.allow_list_conjunctive") : whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ ? Text.translatable(
            "create.gui.attribute_filter.allow_list_disjunctive") : Text.translatable("create.gui.attribute_filter.deny_list")).formatted(Formatting.GOLD));

        int count = 0;
        List<ItemAttributeEntry> attributes = filter.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, List.of());
        for (ItemAttributeEntry attributeEntry : attributes) {
            ItemAttribute attribute = attributeEntry.attribute();
            if (attribute == null)
                continue;
            boolean inverted = attributeEntry.inverted();
            if (count > 3) {
                list.add(Text.literal("- ...").formatted(Formatting.DARK_GRAY));
                break;
            }
            list.add(Text.literal("- ").append(attribute.format(inverted)));
            count++;
        }

        if (count == 0)
            return Collections.emptyList();

        return list;
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        ItemStack heldItem = player.getMainHandStack();
        ItemStack.PACKET_CODEC.encode(extraData, heldItem);
        return new AttributeFilterMenu(id, inv, heldItem);
    }

    @Override
    public ComponentType<?> getComponentType() {
        return AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES;
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack filter) {
        return new AttributeFilterItemStack(filter);
    }

    @Override
    public ItemStack[] getFilterItems(ItemStack stack) {
        AttributeFilterWhitelistMode whitelistMode = stack.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
        List<ItemAttributeEntry> attributes = stack.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, List.of());

        if (whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ && attributes.size() == 1) {
            ItemAttribute attribute = attributes.getFirst().attribute();
            if (attribute instanceof InTagAttribute(TagKey<Item> tag)) {
                List<ItemStack> stacks = new ArrayList<>();
                for (RegistryEntry<Item> holder : Registries.ITEM.iterateEntries(tag)) {
                    stacks.add(new ItemStack(holder.value()));
                }
                return stacks.toArray(ItemStack[]::new);
            }
        }
        return new ItemStack[0];
    }
}