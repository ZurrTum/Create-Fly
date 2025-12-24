package com.zurrtum.create.compat.computercraft.implementation.luaObjects;

import com.zurrtum.create.content.logistics.BigItemStack;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import net.minecraft.core.RegistryAccess;

import java.util.Map;

public class LuaBigItemStack implements LuaComparable {
    private final RegistryAccess registryAccess;
    private final BigItemStack stack;

    public LuaBigItemStack(RegistryAccess registryAccess, BigItemStack stack) {
        this.registryAccess = registryAccess;
        this.stack = stack;
    }

    @Override
    public Map<?, ?> getTableRepresentation() {
        Map<String, Object> details = VanillaDetailRegistries.ITEM_STACK.getDetails(registryAccess, stack.stack);
        // Add count to the details
        details.put("count", stack.count);
        return details;
    }
}
