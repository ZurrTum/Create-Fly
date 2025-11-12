package com.zurrtum.create.content.logistics.tableCloth;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class TableClothBlockItem extends BlockItem {

    public TableClothBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.has(AllDataComponents.AUTO_REQUEST_DATA);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplay displayComponent,
        Consumer<Component> textConsumer,
        TooltipFlag type
    ) {
        super.appendHoverText(stack, context, displayComponent, textConsumer, type);
        if (!isFoil(stack))
            return;

        textConsumer.accept(Component.translatable("create.table_cloth.shop_configured").withStyle(ChatFormatting.GOLD));

        RedstoneRequesterBlock.appendRequesterTooltip(stack, textConsumer);
    }

}
