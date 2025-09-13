package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class DeployerTooltipBehaviour extends KineticTooltipBehaviour<DeployerBlockEntity> {
    public DeployerTooltipBehaviour(DeployerBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
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
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("tooltip.deployer.header").forGoggles(tooltip);

        CreateLang.translate("tooltip.deployer." + (blockEntity.mode == Mode.USE ? "using" : "punching")).style(Formatting.YELLOW)
            .forGoggles(tooltip);

        ItemStack heldItem = blockEntity.heldItem;
        if (!heldItem.isEmpty())
            CreateLang.translate("tooltip.deployer.contains", heldItem.getName(), heldItem.getCount()).style(Formatting.GREEN).forGoggles(tooltip);

        float stressAtBase = blockEntity.calculateStressApplied();
        if (StressImpact.isEnabled() && !MathHelper.approximatelyEquals(stressAtBase, 0)) {
            tooltip.add(ScreenTexts.EMPTY);
            addStressImpactStats(tooltip, stressAtBase);
        }

        return true;
    }
}
