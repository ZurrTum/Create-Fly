package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BrassTunnelTooltipBehaviour extends TooltipBehaviour<BrassTunnelBlockEntity> implements IHaveGoggleInformation {
    public BrassTunnelTooltipBehaviour(BrassTunnelBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        List<ItemStack> allStacks = blockEntity.grabAllStacksOfGroup(true);
        if (allStacks.isEmpty())
            return false;

        CreateLang.translate("tooltip.brass_tunnel.contains").style(Formatting.WHITE).forGoggles(tooltip);
        for (ItemStack item : allStacks) {
            CreateLang.translate("tooltip.brass_tunnel.contains_entry", item.getName().getString(), item.getCount()).style(Formatting.GRAY)
                .forGoggles(tooltip);
        }
        CreateLang.translate("tooltip.brass_tunnel.retrieve").style(Formatting.DARK_GRAY).forGoggles(tooltip);

        return true;
    }
}
