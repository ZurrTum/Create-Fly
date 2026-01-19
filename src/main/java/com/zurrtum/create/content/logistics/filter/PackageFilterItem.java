package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack.PackageFilterItemStack;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PackageFilterItem extends FilterItem {
    protected PackageFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public List<Component> makeSummary(ItemStack filter) {
        String address = PackageItem.getAddress(filter);
        if (address.isBlank())
            return Collections.emptyList();

        return List.of(Component.literal("-> ").withStyle(ChatFormatting.GRAY).append(Component.literal(address).withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        ItemStack heldItem = player.getMainHandItem();
        ItemStack.STREAM_CODEC.encode(extraData, heldItem);
        return new PackageFilterMenu(id, inv, heldItem);
    }

    @Override
    public DataComponentType<?> getComponentType() {
        return AllDataComponents.PACKAGE_ADDRESS;
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack filter) {
        return new PackageFilterItemStack(filter);
    }

    @Override
    public ItemStack[] getFilterItems(ItemStack stack) {
        return new ItemStack[0];
    }
}