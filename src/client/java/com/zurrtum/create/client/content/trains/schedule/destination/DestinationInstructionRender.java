package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DestinationInstructionRender extends TextScheduleInstructionRender<DestinationInstruction> {
    @Override
    public Pair<ItemStack, Text> getSummary(DestinationInstruction input) {
        return Pair.of(AllItems.TRACK_STATION.getDefaultStack(), Text.literal(input.getLabelText()));
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return AllItems.TRACK_STATION.getDefaultStack();
    }

    @Override
    public List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.filter_edit_box"),
            CreateLang.translateDirect("schedule.instruction.filter_edit_box_1").formatted(Formatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.filter_edit_box_2").formatted(Formatting.DARK_GRAY),
            CreateLang.translateDirect("schedule.instruction.filter_edit_box_3").formatted(Formatting.DARK_GRAY)
        );
    }

    @Override
    protected void modifyEditBox(TextFieldWidget box) {
        box.setTextPredicate(s -> StringUtils.countMatches(s, '*') <= 3);
    }
}