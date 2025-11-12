package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BrassTunnelTooltipBehaviour extends TooltipBehaviour<BrassTunnelBlockEntity> implements IHaveGoggleInformation {
    public BrassTunnelTooltipBehaviour(BrassTunnelBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        List<ItemStack> allStacks = blockEntity.grabAllStacksOfGroup(true);
        if (allStacks.isEmpty())
            return false;

        CreateLang.translate("tooltip.brass_tunnel.contains").style(ChatFormatting.WHITE).forGoggles(tooltip);
        for (ItemStack item : allStacks) {
            CreateLang.translate("tooltip.brass_tunnel.contains_entry", item.getHoverName().getString(), item.getCount()).style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        }
        CreateLang.translate("tooltip.brass_tunnel.retrieve").style(ChatFormatting.DARK_GRAY).forGoggles(tooltip);

        return true;
    }
}
