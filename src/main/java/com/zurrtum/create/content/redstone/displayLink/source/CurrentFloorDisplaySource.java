package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CurrentFloorDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof ElevatorContactBlockEntity ecbe))
            return EMPTY_LINE;
        return Text.literal(ecbe.lastReportedCurrentFloor);
    }

    @Override
    protected String getTranslationKey() {
        return "current_floor";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }

}
