package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class ItemThroughputDisplaySource extends AccumulatedItemCountDisplaySource {
    private final NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    static final int POOL_SIZE = 10;

    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        NbtCompound conf = context.sourceConfig();
        if (conf.contains("Inactive"))
            return ZERO.copy();

        double interval = 20 * Math.pow(60, conf.getInt("Interval", 0));
        double rate = conf.getFloat("Rate", 0) * interval;

        if (rate > 0) {
            long previousTime = conf.getLong("LastReceived", 0);
            long gameTime = context.blockEntity().getWorld().getTime();
            int diff = (int) (gameTime - previousTime);
            if (diff > 0) {
                // Too long since last item
                int lastAmount = conf.getInt("LastReceivedAmount", 0);
                double timeBetweenStacks = lastAmount / rate;
                if (diff > timeBetweenStacks * 2)
                    conf.putBoolean("Inactive", true);
            }
        }

        if (MathHelper.approximatelyEquals(rate, 0)) {
            rate = 0;
        }
        return Text.literal(format.format(rate).replace("\u00A0", " "));
    }

    public void itemReceived(DisplayLinkBlockEntity be, int amount) {
        if (be.getCachedState().get(DisplayLinkBlock.POWERED, true))
            return;

        NbtCompound conf = be.getSourceConfig();
        long gameTime = be.getWorld().getTime();

        if (!conf.contains("LastReceived")) {
            conf.putLong("LastReceived", gameTime);
            return;
        }

        long previousTime = conf.getLong("LastReceived", 0);
        NbtList rates = conf.getListOrEmpty("PrevRates");

        if (rates.size() != POOL_SIZE) {
            rates = new NbtList();
            for (int i = 0; i < POOL_SIZE; i++)
                rates.add(NbtFloat.of(-1));
        }

        int poolIndex = conf.getInt("Index", 0) % POOL_SIZE;
        rates.set(poolIndex, NbtFloat.of((float) (amount / (double) (gameTime - previousTime))));

        float rate = 0;
        int validIntervals = 0;
        for (int i = 0; i < POOL_SIZE; i++) {
            float pooledRate = rates.getFloat(i, 0);
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