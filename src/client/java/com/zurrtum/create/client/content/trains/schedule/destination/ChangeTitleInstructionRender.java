package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.schedule.destination.ChangeTitleInstruction;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ChangeTitleInstructionRender extends TextScheduleInstructionRender<ChangeTitleInstruction> {
    @Override
    public Pair<ItemStack, Component> getSummary(ChangeTitleInstruction input) {
        return Pair.of(icon(), Component.literal(input.getLabelText()));
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return icon();
    }

    private ItemStack icon() {
        return new ItemStack(Items.NAME_TAG);
    }

    @Override
    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.name_edit_box"),
            CreateLang.translateDirect("schedule.instruction.name_edit_box_1").withStyle(ChatFormatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.name_edit_box_2").withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
