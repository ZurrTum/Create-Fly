package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ChuteTooltipBehaviour extends TooltipBehaviour<ChuteBlockEntity> implements IHaveGoggleInformation {
    public ChuteTooltipBehaviour(ChuteBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean downward = blockEntity.getItemMotion() < 0;
        CreateLang.translate("tooltip.chute.header").forGoggles(tooltip);

        float pull = blockEntity.pull;
        float push = blockEntity.push;
        if (pull == 0 && push == 0)
            CreateLang.translate("tooltip.chute.no_fans_attached").style(ChatFormatting.GRAY).forGoggles(tooltip);
        if (pull != 0)
            CreateLang.translate("tooltip.chute.fans_" + (pull > 0 ? "pull_up" : "push_down")).style(ChatFormatting.GRAY).forGoggles(tooltip);
        if (push != 0)
            CreateLang.translate("tooltip.chute.fans_" + (push > 0 ? "push_up" : "pull_down")).style(ChatFormatting.GRAY).forGoggles(tooltip);

        CreateLang.text("-> ").add(CreateLang.translate("tooltip.chute.items_move_" + (downward ? "down" : "up"))).style(ChatFormatting.YELLOW)
            .forGoggles(tooltip);
        ItemStack item = blockEntity.getItem();
        if (!item.isEmpty())
            CreateLang.translate("tooltip.chute.contains", item.getHoverName().getString(), item.getCount()).style(ChatFormatting.GREEN).forGoggles(tooltip);

        return true;
    }
}
