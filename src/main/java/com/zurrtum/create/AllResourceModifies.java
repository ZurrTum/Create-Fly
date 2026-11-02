package com.zurrtum.create;

import com.zurrtum.create.infrastructure.resources.AxeModify;
import com.zurrtum.create.infrastructure.resources.BlockTransformerModify;
import com.zurrtum.create.infrastructure.resources.EnchantmentModify;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.infrastructure.resources.ResourceLoader.registerBlockTransformer;
import static com.zurrtum.create.infrastructure.resources.ResourceLoader.registerEnchantment;

public class AllResourceModifies {
    public static BlockTransformerModify AXE = registerBlockTransformer(
        Identifier.withDefaultNamespace("axe"),
        new AxeModify()
    );
    public static EnchantmentModify KNOCKBACK = registerEnchantment(
        Identifier.withDefaultNamespace("knockback"),
        EnchantmentModify.allow(AllItemTags.ENCHANTMENT_KNOCKBACK)
    );
    public static EnchantmentModify LOOTING = registerEnchantment(
        Identifier.withDefaultNamespace("looting"),
        EnchantmentModify.allow(AllItemTags.ENCHANTMENT_LOOTING)
    );
    public static EnchantmentModify MENDING = registerEnchantment(
        Identifier.withDefaultNamespace("mending"),
        EnchantmentModify.deny(AllItemTags.ENCHANTMENT_DENY_MENDING)
    );
    public static EnchantmentModify UNBREAKING = registerEnchantment(
        Identifier.withDefaultNamespace("unbreaking"),
        EnchantmentModify.deny(AllItemTags.ENCHANTMENT_DENY_UNBREAKING)
    );
    public static EnchantmentModify INFINITY = registerEnchantment(
        Identifier.withDefaultNamespace("infinity"),
        EnchantmentModify.deny(AllItemTags.ENCHANTMENT_DENY_INFINITY)
    );
    public static EnchantmentModify AQUA_AFFINITY = registerEnchantment(
        Identifier.withDefaultNamespace("aqua_affinity"),
        EnchantmentModify.deny(AllItemTags.ENCHANTMENT_DENY_AQUA_AFFINITY)
    );

    public static void register() {
    }
}
