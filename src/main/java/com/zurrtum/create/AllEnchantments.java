package com.zurrtum.create;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllEnchantments {
    public static final RegistryKey<Enchantment> POTATO_RECOVERY = register("potato_recovery");
    public static final RegistryKey<Enchantment> CAPACITY = register("capacity");

    private static RegistryKey<Enchantment> register(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, id));
    }
}
