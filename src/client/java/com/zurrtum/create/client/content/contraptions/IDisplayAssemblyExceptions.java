package com.zurrtum.create.client.content.contraptions;

import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.AssemblyException;

import java.util.Arrays;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public interface IDisplayAssemblyExceptions {

    default boolean addExceptionToTooltip(List<Component> tooltip) {
        AssemblyException e = getLastAssemblyException();
        if (e == null)
            return false;

        if (!tooltip.isEmpty())
            tooltip.add(CommonComponents.EMPTY);

        CreateLang.translate("gui.assembly.exception").style(ChatFormatting.GOLD).forGoggles(tooltip);

        String text = e.component.getString();
        Arrays.stream(text.split("\n")).forEach(l -> TooltipHelper.cutStringTextComponent(l, Palette.GRAY_AND_WHITE)
            .forEach(c -> CreateLang.builder().add(c).forGoggles(tooltip)));

        return true;
    }

    AssemblyException getLastAssemblyException();

}
