package com.zurrtum.create.compat.computercraft.implementation.luaObjects;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class LuaItemStack implements LuaComparable {
    private final ItemStack stack;

    public LuaItemStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public Map<?, ?> getTableRepresentation() {
        return VanillaDetailRegistries.ITEM_STACK.getDetails(stack);
    }
}
