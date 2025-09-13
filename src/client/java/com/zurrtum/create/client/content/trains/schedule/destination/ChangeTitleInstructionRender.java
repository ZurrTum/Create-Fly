package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.ChangeTitleInstruction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ChangeTitleInstructionRender extends TextScheduleInstructionRender<ChangeTitleInstruction> {
    @Override
    public Pair<ItemStack, Text> getSummary(ChangeTitleInstruction input) {
        return Pair.of(icon(), Text.literal(input.getLabelText()));
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return icon();
    }

    private ItemStack icon() {
        return new ItemStack(Items.NAME_TAG);
    }

    @Override
    public List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.name_edit_box"),
            CreateLang.translateDirect("schedule.instruction.name_edit_box_1").formatted(Formatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.name_edit_box_2").formatted(Formatting.DARK_GRAY)
        );
    }
}
