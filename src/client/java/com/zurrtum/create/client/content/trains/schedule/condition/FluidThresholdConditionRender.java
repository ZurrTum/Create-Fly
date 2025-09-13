package com.zurrtum.create.client.content.trains.schedule.condition;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.schedule.condition.FluidThresholdCondition;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class FluidThresholdConditionRender extends CargoThresholdConditionRender<FluidThresholdCondition> {
    @Override
    protected Text getUnit(FluidThresholdCondition input) {
        return Text.literal("b");
    }

    @Override
    protected ItemStack getIcon(FluidThresholdCondition input) {
        return input.compareStack.item();
    }

    private FluidStack loadFluid(FluidThresholdCondition input) {
        return input.compareStack.fluid(MinecraftClient.getInstance().world);
    }

    @Override
    public List<Text> getTitleAs(FluidThresholdCondition input, String type) {
        return ImmutableList.of(
            CreateLang.translateDirect(
                "schedule.condition.threshold.train_holds",
                CreateLang.translateDirect("schedule.condition.threshold." + Lang.asId(input.getOperator().name()))
            ), CreateLang.translateDirect(
                "schedule.condition.threshold.x_units_of_item",
                input.getThreshold(),
                CreateLang.translateDirect("schedule.condition.threshold.buckets"),
                input.compareStack.isEmpty() ? CreateLang.translateDirect("schedule.condition.threshold.anything") : input.compareStack.isFilterItem() ? CreateLang.translateDirect(
                    "schedule.condition.threshold.matching_content") : loadFluid(input).getName()
            ).formatted(Formatting.DARK_AQUA)
        );
    }

    @Override
    public void setItem(FluidThresholdCondition input, int slot, ItemStack stack) {
        input.compareStack = FilterItemStack.of(stack);
    }

    @Override
    public ItemStack getItem(FluidThresholdCondition input, int slot) {
        return input.compareStack.item();
    }

    @Override
    public void initConfigurationWidgets(FluidThresholdCondition input, ModularGuiLineBuilder builder) {
        super.initConfigurationWidgets(input, builder);
        builder.addSelectionScrollInput(
            71, 50, (i, l) -> {
                i.forOptions(ImmutableList.of(CreateLang.translateDirect("schedule.condition.threshold.buckets"))).titled(null);
            }, "Measure"
        );
    }
}
