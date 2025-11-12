package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.filter.FilterItemStack.ListFilterItemStack;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class ListFilterItem extends FilterItem {
    protected ListFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> makeSummary(ItemStack filter) {
        List<Component> list = new ArrayList<>();

        ItemStackHandler filterItems = getFilterItemHandler(filter);
        boolean blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);

        list.add((blacklist ? Component.translatable("create.gui.filter.deny_list") : Component.translatable("create.gui.filter.allow_list")).withStyle(
            ChatFormatting.GOLD));
        int count = 0;
        for (int i = 0, size = filterItems.getContainerSize(); i < size; i++) {
            if (count > 3) {
                list.add(Component.literal("- ...").withStyle(ChatFormatting.DARK_GRAY));
                break;
            }

            ItemStack filterStack = filterItems.getItem(i);
            if (filterStack.isEmpty())
                continue;
            list.add(Component.literal("- ").append(filterStack.getHoverName()).withStyle(ChatFormatting.GRAY));
            count++;
        }

        if (count == 0)
            return Collections.emptyList();

        return list;
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack.STREAM_CODEC.encode(extraData, heldItem);
        return new FilterMenu(id, inv, heldItem);
    }

    @Override
    public DataComponentType<?> getComponentType() {
        return AllDataComponents.FILTER_ITEMS;
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack filter) {
        return new ListFilterItemStack(filter);
    }

    public ItemStackHandler getFilterItemHandler(ItemStack stack) {
        ItemStackHandler newInv = new ItemStackHandler(18);
        ItemContainerContents contents = stack.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
        ItemHelper.fillItemStackHandler(contents, newInv);
        return newInv;
    }

    @Override
    public ItemStack[] getFilterItems(ItemStack stack) {
        if (stack.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false))
            return new ItemStack[0];
        return ItemHelper.getNonEmptyStacks(getFilterItemHandler(stack)).toArray(ItemStack[]::new);
    }
}