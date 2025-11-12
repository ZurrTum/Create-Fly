package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.api.goggles.IHaveHoveringInformation;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.List;

import static net.minecraft.ChatFormatting.*;

public class KineticTooltipBehaviour<T extends KineticBlockEntity> extends TooltipBehaviour<T> implements IHaveGoggleInformation, IHaveHoveringInformation {
    public KineticTooltipBehaviour(T be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean notFastEnough = !blockEntity.isSpeedRequirementFulfilled() && blockEntity.getSpeed() != 0;

        if (blockEntity.isOverStressed() && AllConfigs.client().enableOverstressedTooltip.get()) {
            CreateLang.translate("gui.stressometer.overstressed").style(GOLD).forGoggles(tooltip);
            Component hint = CreateLang.translateDirect("gui.contraptions.network_overstressed");
            List<Component> cutString = TooltipHelper.cutTextComponent(hint, Palette.GRAY_AND_WHITE);
            for (Component component : cutString)
                CreateLang.builder().add(component.copy()).forGoggles(tooltip);
            return true;
        }

        if (notFastEnough) {
            CreateLang.translate("tooltip.speedRequirement").style(GOLD).forGoggles(tooltip);
            MutableComponent hint = CreateLang.translateDirect(
                "gui.contraptions.not_fast_enough",
                I18n.get(blockEntity.getBlockState().getBlock().getDescriptionId())
            );
            List<Component> cutString = TooltipHelper.cutTextComponent(hint, Palette.GRAY_AND_WHITE);
            for (Component component : cutString)
                CreateLang.builder().add(component.copy()).forGoggles(tooltip);
            return true;
        }

        return false;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled())
            return false;
        float stressAtBase = blockEntity.calculateStressApplied();
        if (Mth.equal(stressAtBase, 0))
            return false;

        CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);

        addStressImpactStats(tooltip, stressAtBase);

        return true;
    }

    protected void addStressImpactStats(List<Component> tooltip, float stressAtBase) {
        CreateLang.translate("tooltip.stressImpact").style(GRAY).forGoggles(tooltip);

        float stressTotal = stressAtBase * Math.abs(blockEntity.getTheoreticalSpeed());

        CreateLang.number(stressTotal).translate("generic.unit.stress").style(AQUA).space()
            .add(CreateLang.translate("gui.goggles.at_current_speed").style(DARK_GRAY)).forGoggles(tooltip, 1);
    }
}
