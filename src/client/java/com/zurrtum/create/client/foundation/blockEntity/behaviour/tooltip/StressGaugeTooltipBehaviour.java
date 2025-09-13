package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.infrastructure.packet.c2s.GaugeObservedPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class StressGaugeTooltipBehaviour extends GaugeTooltipBehaviour<StressGaugeBlockEntity> {
    public StressGaugeTooltipBehaviour(StressGaugeBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        if (!StressImpact.isEnabled())
            return false;

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        double capacity = blockEntity.getNetworkCapacity();
        float networkStress = blockEntity.getNetworkStress();
        double stressFraction = networkStress / (capacity == 0 ? 1 : capacity);

        CreateLang.translate("gui.stressometer.title").style(Formatting.GRAY).forGoggles(tooltip);

        if (blockEntity.getTheoreticalSpeed() == 0)
            CreateLang.text(TooltipHelper.makeProgressBar(3, 0)).translate("gui.stressometer.no_rotation").style(Formatting.DARK_GRAY)
                .forGoggles(tooltip);
        else {
            getFormattedStressText(stressFraction).forGoggles(tooltip);
            CreateLang.translate("gui.stressometer.capacity").style(Formatting.GRAY).forGoggles(tooltip);

            double remainingCapacity = capacity - networkStress;

            LangBuilder su = CreateLang.translate("generic.unit.stress");
            LangBuilder stressTip = CreateLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());

            if (remainingCapacity != capacity)
                stressTip.text(Formatting.GRAY, " / ").add(CreateLang.number(capacity).add(su).style(Formatting.DARK_GRAY));

            stressTip.forGoggles(tooltip, 1);
        }

        BlockPos pos = blockEntity.getPos();
        if (!pos.equals(StressGaugeBlockEntity.lastSent))
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new GaugeObservedPacket(StressGaugeBlockEntity.lastSent = pos));

        return true;
    }

    public static LangBuilder getFormattedStressText(double stressPercent) {
        StressImpact stressLevel = StressImpact.of(stressPercent);
        return CreateLang.text(TooltipHelper.makeProgressBar(3, Math.min(stressLevel.ordinal() + 1, 3)))
            .translate("tooltip.stressImpact." + Lang.asId(stressLevel.name())).text(String.format(" (%s%%) ", (int) (stressPercent * 100)))
            .style(stressLevel.getRelativeColor());
    }
}
