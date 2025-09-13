package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class EnchantPowerDisplaySource extends NumericSingleLineDisplaySource {

    protected static final Random random = Random.create();
    protected static final ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof EnchantingTableBlockEntity))
            return ZERO.copy();

        BlockPos pos = context.getSourcePos();
        World level = context.level();
        int enchantPower = 0;

        for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            if (EnchantingTableBlock.canAccessPowerProvider(level, pos, blockPos)) {
                enchantPower++;
            }
        }


        int cost = EnchantmentHelper.calculateRequiredExperienceLevel(random, 2, enchantPower, stack);

        return Text.literal(String.valueOf(cost));
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