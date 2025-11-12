package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class DeployerTooltipBehaviour extends KineticTooltipBehaviour<DeployerBlockEntity> {
    public DeployerTooltipBehaviour(DeployerBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (super.addToTooltip(tooltip, isPlayerSneaking))
            return true;
        if (blockEntity.getSpeed() == 0)
            return false;
        if (blockEntity.overflowItems.isEmpty())
            return false;
        TooltipHelper.addHint(tooltip, "hint.full_deployer");
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("tooltip.deployer.header").forGoggles(tooltip);

        CreateLang.translate("tooltip.deployer." + (blockEntity.mode == Mode.USE ? "using" : "punching")).style(ChatFormatting.YELLOW)
            .forGoggles(tooltip);

        ItemStack heldItem = blockEntity.heldItem;
        if (!heldItem.isEmpty())
            CreateLang.translate("tooltip.deployer.contains", heldItem.getHoverName(), heldItem.getCount()).style(ChatFormatting.GREEN).forGoggles(tooltip);

        float stressAtBase = blockEntity.calculateStressApplied();
        if (StressImpact.isEnabled() && !Mth.equal(stressAtBase, 0)) {
            tooltip.add(CommonComponents.EMPTY);
            addStressImpactStats(tooltip, stressAtBase);
        }

        return true;
    }
}
