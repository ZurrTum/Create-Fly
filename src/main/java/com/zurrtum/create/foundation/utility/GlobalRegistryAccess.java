package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.Create;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class GlobalRegistryAccess {
    private static final Supplier<@Nullable RegistryAccess> supplier;

    static {
        if (AllClientHandle.INSTANCE.isClient()) {
            supplier = () -> AllClientHandle.INSTANCE.getPlayer().registryAccess();
        } else {
            supplier = () -> {
                MinecraftServer server = Create.SERVER;
                if (server == null) {
                    return null;
                }
                return server.registryAccess();
            };
        }
    }

    @Nullable
    public static RegistryAccess get() {
        return supplier.get();
    }

    public static RegistryAccess getOrThrow() {
        RegistryAccess registryAccess = get();
        if (registryAccess == null) {
            throw new IllegalStateException("Could not get RegistryAccess");
        }
        return registryAccess;
    }
}
