package com.zurrtum.create.compat.computercraft.implementation.luaObjects;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class LuaItemStack implements LuaComparable {
    private final RegistryAccess registryAccess;
    private final ItemStack stack;

    public LuaItemStack(RegistryAccess registryAccess, ItemStack stack) {
        this.registryAccess = registryAccess;
        this.stack = stack;
    }

    @Override
    public Map<?, ?> getTableRepresentation() {
        return VanillaDetailRegistries.ITEM_STACK.getDetails(registryAccess, stack);
    }
}
