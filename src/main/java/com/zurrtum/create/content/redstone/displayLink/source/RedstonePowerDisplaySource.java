package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RedstonePowerDisplaySource extends PercentOrProgressBarDisplaySource {
    @Override
    protected String getTranslationKey() {
        return "redstone_power";
    }

    @Override
    protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
        return Component.literal(String.valueOf((int) (currentLevel * 15)));
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected Float getProgress(DisplayLinkContext context) {
        BlockState blockState = context.level().getBlockState(context.getSourcePos());
        return Math.max(context.level().getDirectSignalTo(context.getSourcePos()), blockState.getValueOrElse(BlockStateProperties.POWER, 0)) / 15f;
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return context.sourceConfig().getIntOr("Mode", 0) != 0;
    }
}