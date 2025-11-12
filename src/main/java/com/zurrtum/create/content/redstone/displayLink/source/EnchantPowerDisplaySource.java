package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

public class EnchantPowerDisplaySource extends NumericSingleLineDisplaySource {

    protected static final RandomSource random = RandomSource.create();
    protected static final ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof EnchantingTableBlockEntity))
            return ZERO.copy();

        BlockPos pos = context.getSourcePos();
        Level level = context.level();
        int enchantPower = 0;

        for (BlockPos blockPos : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, pos, blockPos)) {
                enchantPower++;
            }
        }


        int cost = EnchantmentHelper.getEnchantmentCost(random, 2, enchantPower, stack);

        return Component.literal(String.valueOf(cost));
    }

    @Override
    protected String getTranslationKey() {
        return "max_enchant_level";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}