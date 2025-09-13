package com.zurrtum.create.content.equipment.potatoCannon;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPotatoProjectileTypes {
    public static final RegistryKey<PotatoCannonProjectileType> FALLBACK = RegistryKey.of(
        CreateRegistryKeys.POTATO_PROJECTILE_TYPE,
        Identifier.of(MOD_ID, "fallback")
    );
}
