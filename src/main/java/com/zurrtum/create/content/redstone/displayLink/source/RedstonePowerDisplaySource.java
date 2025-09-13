package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class RedstonePowerDisplaySource extends PercentOrProgressBarDisplaySource {
    @Override
    protected String getTranslationKey() {
        return "redstone_power";
    }

    @Override
    protected MutableText formatNumeric(DisplayLinkContext context, Float currentLevel) {
        return Text.literal(String.valueOf((int) (currentLevel * 15)));
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected Float getProgress(DisplayLinkContext context) {
        BlockState blockState = context.level().getBlockState(context.getSourcePos());
        return Math.max(context.level().getReceivedStrongRedstonePower(context.getSourcePos()), blockState.get(Properties.POWER, 0)) / 15f;
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return context.sourceConfig().getInt("Mode", 0) != 0;
    }
}