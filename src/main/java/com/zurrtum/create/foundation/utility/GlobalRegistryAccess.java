package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.Create;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class GlobalRegistryAccess {
    private static final Supplier<@Nullable DynamicRegistryManager> supplier;

    static {
        if (AllClientHandle.INSTANCE.isClient()) {
            supplier = () -> AllClientHandle.INSTANCE.getPlayer().getRegistryManager();
        } else {
            supplier = () -> {
                MinecraftServer server = Create.SERVER;
                if (server == null) {
                    return null;
                }
                return server.getRegistryManager();
            };
        }
    }

    @Nullable
    public static DynamicRegistryManager get() {
        return supplier.get();
    }

    public static DynamicRegistryManager getOrThrow() {
        DynamicRegistryManager registryAccess = get();
        if (registryAccess == null) {
            throw new IllegalStateException("Could not get RegistryAccess");
        }
        return registryAccess;
    }
}
