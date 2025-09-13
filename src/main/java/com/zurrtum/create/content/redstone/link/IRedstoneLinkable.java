package com.zurrtum.create.content.redstone.link;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.minecraft.util.math.BlockPos;

public interface IRedstoneLinkable {

    int getTransmittedStrength();

    void setReceivedStrength(int power);

    boolean isListening();

    boolean isAlive();

    Couple<Frequency> getNetworkKey();

    BlockPos getLocation();

}
