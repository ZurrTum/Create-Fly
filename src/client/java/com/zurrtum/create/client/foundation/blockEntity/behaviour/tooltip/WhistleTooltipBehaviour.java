package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import net.minecraft.text.Text;

import java.util.List;

public class WhistleTooltipBehaviour extends TooltipBehaviour<WhistleBlockEntity> implements IHaveGoggleInformation {
    public WhistleTooltipBehaviour(WhistleBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
        String[] pitches = CreateLang.translateDirect("generic.notes").getString().split(";");
        CreateLang.translate("generic.pitch", pitches[blockEntity.pitch % pitches.length]).forGoggles(tooltip);
        return true;
    }
}
