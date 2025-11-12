package com.zurrtum.create.content.equipment.potatoCannon;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPotatoProjectileTypes {
    public static final ResourceKey<PotatoCannonProjectileType> FALLBACK = ResourceKey.create(
        CreateRegistryKeys.POTATO_PROJECTILE_TYPE,
        ResourceLocation.fromNamespaceAndPath(MOD_ID, "fallback")
    );
}
