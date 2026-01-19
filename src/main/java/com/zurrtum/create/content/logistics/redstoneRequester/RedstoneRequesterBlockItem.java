package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class RedstoneRequesterBlockItem extends LogisticallyLinkedBlockItem {

    public RedstoneRequesterBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext tooltipContext,
        TooltipDisplay displayComponent,
        Consumer<Component> textConsumer,
        TooltipFlag type
    ) {
        if (!isTuned(stack))
            return;

        if (!stack.has(AllDataComponents.AUTO_REQUEST_DATA)) {
            super.appendHoverText(stack, tooltipContext, displayComponent, textConsumer, type);
            return;
        }

        textConsumer.accept(Component.translatable("create.logistically_linked.tooltip").withStyle(ChatFormatting.GOLD));
        RedstoneRequesterBlock.appendRequesterTooltip(stack, textConsumer);
    }

}
