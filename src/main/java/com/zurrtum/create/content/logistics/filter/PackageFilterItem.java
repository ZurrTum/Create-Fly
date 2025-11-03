package com.zurrtum.create.content.logistics.filter;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack.PackageFilterItemStack;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PackageFilterItem extends FilterItem {
    protected PackageFilterItem(Settings properties) {
        super(properties);
    }

    @Override
    public List<Text> makeSummary(ItemStack filter) {
        String address = PackageItem.getAddress(filter);
        if (address.isBlank())
            return Collections.emptyList();

        return List.of(Text.literal("-> ").formatted(Formatting.GRAY).append(Text.literal(address).formatted(Formatting.GOLD)));
    }

    @Override
    public @Nullable MenuBase<?> createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        ItemStack heldItem = player.getMainHandStack();
        ItemStack.PACKET_CODEC.encode(extraData, heldItem);
        return new PackageFilterMenu(id, inv, heldItem);
    }

    @Override
    public ComponentType<?> getComponentType() {
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