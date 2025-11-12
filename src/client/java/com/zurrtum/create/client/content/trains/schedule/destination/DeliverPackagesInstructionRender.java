package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.DeliverPackagesInstruction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DeliverPackagesInstructionRender implements IScheduleInput<DeliverPackagesInstruction> {
    @Override
    public Pair<ItemStack, Component> getSummary(DeliverPackagesInstruction input) {
        return Pair.of(getSecondLineIcon(), CreateLang.translateDirect("schedule.instruction.package_delivery"));
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return AllItems.WHITE_POSTBOX.getDefaultInstance();
    }

    @Override
    public List<Component> getTitleAs(DeliverPackagesInstruction input, String type) {
        return ImmutableList.of(
            CreateLang.translate("schedule.instruction.package_delivery.summary").style(ChatFormatting.GOLD).component(),
            CreateLang.translateDirect("schedule.instruction.package_delivery.summary_1").withStyle(ChatFormatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.package_delivery.summary_2").withStyle(ChatFormatting.GRAY)
        );
    }
}
