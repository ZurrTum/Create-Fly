package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFuelTimes {
    public static final ResourceKey<ContextIntProvider> CARDBOARD = createKey("cooking/time_cardboard");
    public static final ResourceKey<ContextIntProvider> CARDBOARD_BLOCK = createKey("cooking/time_cardboard_block");
    public static final ResourceKey<ContextIntProvider> BLAZE_CAKE = createKey("cooking/time_blaze_cake");
    public static final ResourceKey<ContextIntProvider> CREATIVE = createKey("cooking/time_creative");

    private static ResourceKey<ContextIntProvider> createKey(String location) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Identifier.fromNamespaceAndPath(MOD_ID, location));
    }

    public static void register() {
    }
}
