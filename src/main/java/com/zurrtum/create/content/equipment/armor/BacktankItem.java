package com.zurrtum.create.content.equipment.armor;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.item.LayeredArmorItem;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import static com.zurrtum.create.Create.MOD_ID;

public class BacktankItem extends BlockItem {
    public static final EquipmentSlot SLOT = EquipmentSlot.CHEST;
    public static final int BAR_COLOR = 0xEFEFEF;

    public BacktankItem(Block block, Properties settings) {
        super(block, settings);
    }

    public static BacktankItem copper(Properties settings) {
        return new BacktankItem(AllBlocks.COPPER_BACKTANK, settings);
    }

    public static BacktankItem netherite(Properties settings) {
        Identifier layer = Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/netherite_diving_layer.png");
        return new Layered(AllBlocks.NETHERITE_BACKTANK, settings, layer);
    }

    public ItemStack getMaxAirStack() {
        ItemStack stack = getDefaultInstance();
        stack.set(AllDataComponents.BACKTANK_AIR, BacktankUtil.maxAirWithoutEnchants());
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * Mth.clamp(getRemainingAir(stack) / ((float) BacktankUtil.maxAir(stack)), 0, 1));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    public static int getRemainingAir(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.BACKTANK_AIR, 0);
    }

    public static class Layered extends BacktankItem implements LayeredArmorItem {
        private final Identifier layer;

        public Layered(Block block, Properties settings, Identifier layer) {
            super(block, settings);
            this.layer = layer;
        }

        @Override
        public Identifier getLayerTexture() {
            return layer;
        }
    }
}
