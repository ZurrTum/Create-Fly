package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AccumulatedItemCountDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        return Component.literal(String.valueOf(context.sourceConfig().getInt("Collected")));
    }

    public void itemReceived(DisplayLinkBlockEntity be, int amount) {
        if (be.getBlockState().getValueOrElse(DisplayLinkBlock.POWERED, true))
            return;

        int collected = be.getSourceConfig().getIntOr("Collected", 0);
        be.getSourceConfig().putInt("Collected", collected + amount);
        be.updateGatheredData();
    }

    @Override
    protected String getTranslationKey() {
        return "accumulate_items";
    }

    @Override
    public int getPassiveRefreshTicks() {
        return 200;
    }

    @Override
    public void onSignalReset(DisplayLinkContext context) {
        context.sourceConfig().remove("Collected");
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}