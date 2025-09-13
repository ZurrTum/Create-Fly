package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ChuteTooltipBehaviour extends TooltipBehaviour<ChuteBlockEntity> implements IHaveGoggleInformation {
    public ChuteTooltipBehaviour(ChuteBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        boolean downward = blockEntity.getItemMotion() < 0;
        CreateLang.translate("tooltip.chute.header").forGoggles(tooltip);

        float pull = blockEntity.pull;
        float push = blockEntity.push;
        if (pull == 0 && push == 0)
            CreateLang.translate("tooltip.chute.no_fans_attached").style(Formatting.GRAY).forGoggles(tooltip);
        if (pull != 0)
            CreateLang.translate("tooltip.chute.fans_" + (pull > 0 ? "pull_up" : "push_down")).style(Formatting.GRAY).forGoggles(tooltip);
        if (push != 0)
            CreateLang.translate("tooltip.chute.fans_" + (push > 0 ? "push_up" : "pull_down")).style(Formatting.GRAY).forGoggles(tooltip);

        CreateLang.text("-> ").add(CreateLang.translate("tooltip.chute.items_move_" + (downward ? "down" : "up"))).style(Formatting.YELLOW)
            .forGoggles(tooltip);
        ItemStack item = blockEntity.getItem();
        if (!item.isEmpty())
            CreateLang.translate("tooltip.chute.contains", item.getName().getString(), item.getCount()).style(Formatting.GREEN).forGoggles(tooltip);

        return true;
    }
}
