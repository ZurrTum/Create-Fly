package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.DeliverPackagesInstruction;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class DeliverPackagesInstructionRender implements IScheduleInput<DeliverPackagesInstruction> {
    @Override
    public Pair<ItemStack, Text> getSummary(DeliverPackagesInstruction input) {
        return Pair.of(getSecondLineIcon(), CreateLang.translateDirect("schedule.instruction.package_delivery"));
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return AllItems.WHITE_POSTBOX.getDefaultStack();
    }

    @Override
    public List<Text> getTitleAs(DeliverPackagesInstruction input, String type) {
        return ImmutableList.of(
            CreateLang.translate("schedule.instruction.package_delivery.summary").style(Formatting.GOLD).component(),
            CreateLang.translateDirect("schedule.instruction.package_delivery.summary_1").formatted(Formatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.package_delivery.summary_2").formatted(Formatting.GRAY)
        );
    }
}
