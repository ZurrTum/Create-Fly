package com.zurrtum.create.client.content.trains.schedule.destination;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.gui.widget.FilterEditBox;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.trains.schedule.destination.FetchPackagesInstruction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FetchPackagesInstructionRender extends TextScheduleInstructionRender<FetchPackagesInstruction> {
    @Override
    public Pair<ItemStack, Component> getSummary(FetchPackagesInstruction input) {
        return Pair.of(getSecondLineIcon(), CreateLang.translateDirect("schedule.instruction.package_retrieval"));
    }

    @Override
    public List<Component> getTitleAs(FetchPackagesInstruction input, String type) {
        return ImmutableList.of(
            CreateLang.translate("schedule.instruction.package_retrieval.summary").style(ChatFormatting.GOLD).component(),
            CreateLang.translateDirect("generic.in_quotes", Component.literal(input.getLabelText())),
            CreateLang.translateDirect("schedule.instruction.package_retrieval.summary_1").withStyle(ChatFormatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.package_retrieval.summary_2").withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return PackageStyles.getDefaultBox();
    }

    @Override
    public @Nullable List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box"),
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_1").withStyle(ChatFormatting.GRAY),
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_2").withStyle(ChatFormatting.DARK_GRAY),
            CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_3").withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    protected void modifyEditBox(FilterEditBox box) {
        box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
    }
}
