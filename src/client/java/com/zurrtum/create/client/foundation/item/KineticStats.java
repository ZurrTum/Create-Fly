package com.zurrtum.create.client.foundation.item;

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.api.stress.BlockStressValues.GeneratedRpm;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

public class KineticStats implements TooltipModifier {
    protected final Block block;

    public KineticStats(Block block) {
        this.block = block;
    }

    @Nullable
    public static KineticStats create(Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof IRotate || block instanceof SteamEngineBlock) {
                return new KineticStats(block);
            }
        }
        return null;
    }

    @Override
    public void modify(List<Component> tooltip, Player player) {
        List<Component> kineticStats = getKineticStats(block, player);
        if (!kineticStats.isEmpty()) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.addAll(kineticStats);
        }
    }

    public static List<Component> getKineticStats(Block block, Player player) {
        List<Component> list = new ArrayList<>();

        CKinetics config = AllConfigs.server().kinetics;
        LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");
        LangBuilder suUnit = CreateLang.translate("generic.unit.stress");

        boolean hasGoggles = GogglesItem.isWearingGoggles(player);

        boolean showStressImpact;
        if (block instanceof IRotate) {
            showStressImpact = !((IRotate) block).hideStressImpact();
        } else {
            showStressImpact = true;
        }

        boolean hasStressImpact = StressImpact.isEnabled() && showStressImpact && BlockStressValues.getImpact(block) > 0;
        boolean hasStressCapacity = StressImpact.isEnabled() && BlockStressValues.getCapacity(block) > 0;

        if (hasStressImpact) {
            CreateLang.translate("tooltip.stressImpact").style(GRAY).addTo(list);

            double impact = BlockStressValues.getImpact(block);
            StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH : (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
            LangBuilder builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1)).style(impactId.getAbsoluteColor()));

            if (hasGoggles) {
                builder.add(CreateLang.number(impact)).text("x ").add(rpmUnit).addTo(list);
            } else
                builder.translate("tooltip.stressImpact." + Lang.asId(impactId.name())).addTo(list);
        }

        if (hasStressCapacity) {
            CreateLang.translate("tooltip.capacityProvided").style(GRAY).addTo(list);

            double capacity = BlockStressValues.getCapacity(block);
            GeneratedRpm generatedRPM = BlockStressValues.RPM.get(block);

            StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.HIGH : (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.LOW);
            StressImpact opposite = StressImpact.values()[StressImpact.values().length - 2 - impactId.ordinal()];
            LangBuilder builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1)).style(opposite.getAbsoluteColor()));

            if (hasGoggles) {
                builder.add(CreateLang.number(capacity)).text("x ").add(rpmUnit).addTo(list);

                if (generatedRPM != null) {
                    LangBuilder amount = CreateLang.number(capacity * generatedRPM.value()).add(suUnit);
                    CreateLang.text(" -> ").add(generatedRPM.mayGenerateLess() ? CreateLang.translate("tooltip.up_to", amount) : amount)
                        .style(DARK_GRAY).addTo(list);
                }
            } else
                builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name())).addTo(list);
        }

        return list;
    }
}
