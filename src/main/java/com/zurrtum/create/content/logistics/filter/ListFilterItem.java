package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.filter.FilterItemStack.ListFilterItemStack;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFilterItem extends FilterItem {
    protected ListFilterItem(Settings properties) {
        super(properties);
    }

    @Override
    public List<Text> makeSummary(ItemStack filter) {
        List<Text> list = new ArrayList<>();

        ItemStackHandler filterItems = getFilterItemHandler(filter);
        boolean blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);

        list.add((blacklist ? Text.translatable("create.gui.filter.deny_list") : Text.translatable("create.gui.filter.allow_list")).formatted(
            Formatting.GOLD));
        int count = 0;
        for (int i = 0, size = filterItems.size(); i < size; i++) {
            if (count > 3) {
                list.add(Text.literal("- ...").formatted(Formatting.DARK_GRAY));
                break;
            }

            ItemStack filterStack = filterItems.getStack(i);
            if (filterStack.isEmpty())
                continue;
            list.add(Text.literal("- ").append(filterStack.getName()).formatted(Formatting.GRAY));
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
        return new FilterMenu(id, inv, heldItem);
    }

    @Override
    public ComponentType<?> getComponentType() {
        return AllDataComponents.FILTER_ITEMS;
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack filter) {
        return new ListFilterItemStack(filter);
    }

    public ItemStackHandler getFilterItemHandler(ItemStack stack) {
        ItemStackHandler newInv = new ItemStackHandler(18);
        ContainerComponent contents = stack.getOrDefault(AllDataComponents.FILTER_ITEMS, ContainerComponent.DEFAULT);
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