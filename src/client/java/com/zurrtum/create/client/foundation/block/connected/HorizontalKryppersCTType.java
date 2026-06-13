package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class HorizontalKryppersCTType extends CTType {
    public static final int LEFT_FLAG = 1 << 1;
    public static final int RIGHT_FLAG = 1 << 2;
    public static final CTPosStep LEFT = new CTPosStep(LEFT_FLAG, -1, 0);
    public static final CTPosStep RIGHT = new CTPosStep(RIGHT_FLAG, 1, 0);
    public static final CTPosStep[] HORIZONTAL = new CTPosStep[]{LEFT, RIGHT};
    public static final int MAP = 1 << (LEFT_FLAG | RIGHT_FLAG) | 2 << RIGHT_FLAG | 3 << LEFT_FLAG;

    public HorizontalKryppersCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "horizontal_kryppers"), HORIZONTAL);
    }

    @Override
    public int getTextureIndex(int context) {
        return MAP >> context & AXIS_FLAGS;
    }
}
