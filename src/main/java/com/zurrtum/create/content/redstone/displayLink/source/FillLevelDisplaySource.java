package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import net.minecraft.block.entity.BlockEntity;

public class FillLevelDisplaySource extends PercentOrProgressBarDisplaySource {
    @Override
    protected Float getProgress(DisplayLinkContext context) {
        BlockEntity be = context.getSourceBlockEntity();
        if (!(be instanceof ThresholdSwitchBlockEntity tsbe))
            return null;
        return Math.max(0, (float) (tsbe.currentLevel - tsbe.currentMinLevel) / (tsbe.currentMaxLevel - tsbe.currentMinLevel));
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return context.sourceConfig().getInt("Mode", 0) != 0;
    }

    @Override
    protected String getTranslationKey() {
        return "fill_level";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

}