package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.trains.schedule.destination.FetchPackagesInstruction;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FetchPackagesInstructionRender extends TextScheduleInstructionRender<FetchPackagesInstruction> {
    @Override
    public Pair<ItemStack, Text> getSummary(FetchPackagesInstruction input) {
        return Pair.of(getSecondLineIcon(), CreateLang.translateDirect("schedule.instruction.package_retrieval"));
    }

    @Override
    public List<Text> getTitleAs(FetchPackagesInstruction input, String type) {
        return ImmutableList.of(
            CreateLang.translate("schedule.instruction.package_retrieval.summary").style(Formatting.GOLD).component(),
            CreateLang.translateDirect("generic.in_quotes", Text.literal(input.getLabelText())),
            CreateLang.translateDirect("schedule.instruction.package_retrieval.summary_1").formatted(Formatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.package_retrieval.summary_2").formatted(Formatting.GRAY)
        );
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return PackageStyles.getDefaultBox();
    }

    @Override
    public @Nullable List<Text> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box"),
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_1").formatted(Formatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_2").formatted(Formatting.DARK_GRAY),
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_3").formatted(Formatting.DARK_GRAY)
        );
    }

    @Override
    protected void modifyEditBox(TextFieldWidget box) {
        box.setTextPredicate(s -> StringUtils.countMatches(s, '*') <= 3);
    }
}
