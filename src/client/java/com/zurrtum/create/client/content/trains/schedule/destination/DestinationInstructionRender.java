package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DestinationInstructionRender extends TextScheduleInstructionRender<DestinationInstruction> {
    @Override
    public Pair<ItemStack, Component> getSummary(DestinationInstruction input) {
        return Pair.of(AllItems.TRACK_STATION.getDefaultInstance(), Component.literal(input.getLabelText()));
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return AllItems.TRACK_STATION.getDefaultInstance();
    }

    @Override
    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.filter_edit_box"),
            CreateLang.translateDirect("schedule.instruction.filter_edit_box_1").withStyle(ChatFormatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.filter_edit_box_2").withStyle(ChatFormatting.DARK_GRAY),
            CreateLang.translateDirect("schedule.instruction.filter_edit_box_3").withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    protected void modifyEditBox(EditBox box) {
        box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
    }
}