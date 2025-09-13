package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class AccumulatedItemCountDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        return Text.literal(String.valueOf(context.sourceConfig().getInt("Collected")));
    }

    public void itemReceived(DisplayLinkBlockEntity be, int amount) {
        if (be.getCachedState().get(DisplayLinkBlock.POWERED, true))
            return;

        int collected = be.getSourceConfig().getInt("Collected", 0);
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