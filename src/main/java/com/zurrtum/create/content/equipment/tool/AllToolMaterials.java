package com.zurrtum.create.content.equipment.tool;

import com.zurrtum.create.AllItemTags;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public class AllToolMaterials {
    public static final ToolMaterial CARDBOARD = register(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 1, 1, 2, 1, AllItemTags.REPAIRS_CARDBOARD_ARMOR);

    private static ToolMaterial register(
        TagKey<Block> incorrectBlocksForDrops,
        int durability,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        TagKey<Item> repairItems
    ) {
        return new ToolMaterial(incorrectBlocksForDrops, durability, speed, attackDamageBonus, enchantmentValue, repairItems);
    }

    public static void register() {
    }
}
