package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class HorizontalCTType extends CTType {
    public static final int RIGHT_FLAG = 1;
    public static final int LEFT_FLAG = 1 << 1;
    public static final CTPosStep RIGHT = new CTPosStep(0, RIGHT_FLAG, 1, 0);
    public static final CTPosStep LEFT = new CTPosStep(0, LEFT_FLAG, -1, 0);
    public static final CTPosStep[] HORIZONTAL = new CTPosStep[]{RIGHT, LEFT};

    public HorizontalCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "horizontal"), HORIZONTAL);
    }
}
