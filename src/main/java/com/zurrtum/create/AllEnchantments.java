package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import static com.zurrtum.create.Create.MOD_ID;

public class AllEnchantments {
    public static final ResourceKey<Enchantment> POTATO_RECOVERY = register("potato_recovery");
    public static final ResourceKey<Enchantment> CAPACITY = register("capacity");

    private static ResourceKey<Enchantment> register(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }
}
