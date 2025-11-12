package com.zurrtum.create.client.content.logistics.tunnel;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import net.minecraft.core.Direction;

public class BrassTunnelModeSlot extends CenteredSideValueBoxTransform {

    public BrassTunnelModeSlot() {
        super((state, d) -> d == Direction.UP);
    }

    @Override
    public int getOverrideColor() {
        return 0xFF592424;
    }

}
