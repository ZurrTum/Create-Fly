package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.content.trains.schedule.condition.RedstoneLinkCondition;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class RedstoneLinkConditionRender implements IScheduleInput<RedstoneLinkCondition> {
    @Override
    public int slotsTargeted() {
        return 2;
    }

    @Override
    public Pair<ItemStack, Text> getSummary(RedstoneLinkCondition input) {
        return Pair.of(
            AllItems.REDSTONE_LINK.getDefaultStack(),
            input.lowActivation() ? CreateLang.translateDirect("schedule.condition.redstone_link_off") : CreateLang.translateDirect(
                "schedule.condition.redstone_link_on")
        );
    }

    @Override
    public List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(CreateLang.translateDirect(slot == 0 ? "logistics.firstFrequency" : "logistics.secondFrequency")
            .formatted(Formatting.RED));
    }

    @Override
    public List<Text> getTitleAs(RedstoneLinkCondition input, String type) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.condition.redstone_link.frequency_" + (input.lowActivation() ? "unpowered" : "powered")),
            Text.literal(" #1 ").formatted(Formatting.GRAY).append(input.freq.getFirst().getStack().getName().copy().formatted(Formatting.DARK_AQUA)),
            Text.literal(" #2 ").formatted(Formatting.GRAY).append(input.freq.getSecond().getStack().getName().copy().formatted(Formatting.DARK_AQUA))
        );
    }

    @Override
    public void setItem(RedstoneLinkCondition input, int slot, ItemStack stack) {
        input.freq.set(slot == 0, Frequency.of(stack));
    }

    @Override
    public ItemStack getItem(RedstoneLinkCondition input, int slot) {
        return input.freq.get(slot == 0).getStack();
    }

    @Override
    public void initConfigurationWidgets(RedstoneLinkCondition input, ModularGuiLineBuilder builder) {
        builder.addSelectionScrollInput(
            20,
            101,
            (i, l) -> i.forOptions(CreateLang.translatedOptions("schedule.condition.redstone_link", "powered", "unpowered"))
                .titled(CreateLang.translateDirect("schedule.condition.redstone_link.frequency_state")),
            "Inverted"
        );
    }
}
