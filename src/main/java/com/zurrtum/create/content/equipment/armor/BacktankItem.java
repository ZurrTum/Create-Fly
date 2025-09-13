package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class BacktankItem extends BlockItem {
    public static final EquipmentSlot SLOT = EquipmentSlot.CHEST;
    public static final int BAR_COLOR = 0xEFEFEF;

    public BacktankItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static BacktankItem copper(Settings settings) {
        return new BacktankItem(AllBlocks.COPPER_BACKTANK, settings);
    }

    public static BacktankItem netherite(Settings settings) {
        return new BacktankItem(AllBlocks.NETHERITE_BACKTANK, settings);
    }

    public ItemStack getMaxAirStack() {
        ItemStack stack = getDefaultStack();
        stack.set(AllDataComponents.BACKTANK_AIR, BacktankUtil.maxAirWithoutEnchants());
        return stack;
    }

    //TODO
    //    @Override
    //    public boolean supportsEnchantment(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
    //        if (enchantment.is(Enchantments.MENDING) || enchantment.is(Enchantments.UNBREAKING))
    //            return false;
    //        return super.supportsEnchantment(stack, enchantment);
    //    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * MathHelper.clamp(getRemainingAir(stack) / ((float) BacktankUtil.maxAir(stack)), 0, 1));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    public static int getRemainingAir(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.BACKTANK_AIR, 0);
    }
}
