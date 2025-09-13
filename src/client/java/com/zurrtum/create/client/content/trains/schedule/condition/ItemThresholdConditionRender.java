package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.schedule.condition.ItemThresholdCondition;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemThresholdConditionRender extends CargoThresholdConditionRender<ItemThresholdCondition> {
    @Override
    protected Text getUnit(ItemThresholdCondition input) {
        return Text.literal(input.inStacks() ? "▤" : "");
    }

    @Override
    protected ItemStack getIcon(ItemThresholdCondition input) {
        return input.stack.item();
    }

    @Override
    public void setItem(ItemThresholdCondition input, int slot, ItemStack stack) {
        input.stack = FilterItemStack.of(stack);
    }

    @Override
    public ItemStack getItem(ItemThresholdCondition input, int slot) {
        return input.stack.item();
    }

    @Override
    public List<Text> getTitleAs(ItemThresholdCondition input, String type) {
        FilterItemStack stack = input.stack;
        return ImmutableList.of(
            CreateLang.translateDirect(
                "schedule.condition.threshold.train_holds",
                CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(input.getOperator().name()))
            ), CreateLang.translateDirect(
                "schedule.condition.threshold.x_units_of_item",
                input.getThreshold(),
                CreateLang.translateDirect("schedule.condition.threshold." + (input.inStacks() ? "stacks" : "items")),
                stack.isEmpty() ? CreateLang.translateDirect("schedule.condition.threshold.anything") : stack.isFilterItem() ? CreateLang.translateDirect(
                    "schedule.condition.threshold.matching_content") : stack.item().getName()
            ).formatted(Formatting.DARK_AQUA)
        );
    }

    @Override
    public void initConfigurationWidgets(ItemThresholdCondition input, ModularGuiLineBuilder builder) {
        super.initConfigurationWidgets(input, builder);
        builder.addSelectionScrollInput(
            71, 50, (i, l) -> {
                i.forOptions(ImmutableList.of(
                    CreateLang.translateDirect("schedule.condition.threshold.items"),
                    CreateLang.translateDirect("schedule.condition.threshold.stacks")
                )).titled(CreateLang.translateDirect("schedule.condition.threshold.item_measure"));
            }, "Measure"
        );
    }
}
