package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class ItemThroughputDisplaySource extends AccumulatedItemCountDisplaySource {
    private final NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    static final int POOL_SIZE = 10;

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        CompoundTag conf = context.sourceConfig();
        if (conf.contains("Inactive"))
            return ZERO.copy();

        double interval = 20 * Math.pow(60, conf.getIntOr("Interval", 0));
        double rate = conf.getFloatOr("Rate", 0) * interval;

        if (rate > 0) {
            long previousTime = conf.getLongOr("LastReceived", 0);
            long gameTime = context.blockEntity().getLevel().getGameTime();
            int diff = (int) (gameTime - previousTime);
            if (diff > 0) {
                // Too long since last item
                int lastAmount = conf.getIntOr("LastReceivedAmount", 0);
                double timeBetweenStacks = lastAmount / rate;
                if (diff > timeBetweenStacks * 2)
                    conf.putBoolean("Inactive", true);
            }
        }

        if (Mth.equal(rate, 0)) {
            rate = 0;
        }
        return Component.literal(format.format(rate).replace("\u00A0", " "));
    }

    public void itemReceived(DisplayLinkBlockEntity be, int amount) {
        if (be.getBlockState().getValueOrElse(DisplayLinkBlock.POWERED, true))
            return;

        CompoundTag conf = be.getSourceConfig();
        long gameTime = be.getLevel().getGameTime();

        if (!conf.contains("LastReceived")) {
            conf.putLong("LastReceived", gameTime);
            return;
        }

        long previousTime = conf.getLongOr("LastReceived", 0);
        ListTag rates = conf.getListOrEmpty("PrevRates");

        if (rates.size() != POOL_SIZE) {
            rates = new ListTag();
            for (int i = 0; i < POOL_SIZE; i++)
                rates.add(FloatTag.valueOf(-1));
        }

        int poolIndex = conf.getIntOr("Index", 0) % POOL_SIZE;
        rates.set(poolIndex, FloatTag.valueOf((float) (amount / (double) (gameTime - previousTime))));

        float rate = 0;
        int validIntervals = 0;
        for (int i = 0; i < POOL_SIZE; i++) {
            float pooledRate = rates.getFloatOr(i, 0);
            if (pooledRate >= 0) {
                rate += pooledRate;
                validIntervals++;
            }
        }

        conf.remove("Rate");
        if (validIntervals > 0) {
            rate /= validIntervals;
            conf.putFloat("Rate", rate);
        }

        conf.remove("Inactive");
        conf.putInt("LastReceivedAmount", amount);
        conf.putLong("LastReceived", gameTime);
        conf.putInt("Index", poolIndex + 1);
        conf.put("PrevRates", rates);
        be.updateGatheredData();
    }

    @Override
    protected String getTranslationKey() {
        return "item_throughput";
    }

}