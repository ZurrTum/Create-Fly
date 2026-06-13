package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class RectangleWithoutVerticalMiddleCTType extends CTType {
    private static final int UP = UP_FLAG;
    private static final int DOWN = DOWN_FLAG;
    private static final int LEFT = LEFT_FLAG;
    private static final int RIGHT = RIGHT_FLAG;

    public static final int[] MAP;
    public static final int SIZE;

    static {
        int index = 0;
        MAP = new int[AXIS_ALIGNED_FLAGS + 1];
        MAP[DOWN] = ++index;
        MAP[DOWN | RIGHT] = ++index;
        MAP[DOWN | LEFT] = ++index;
        MAP[UP | DOWN] = ++index;
        MAP[UP | DOWN | RIGHT] = ++index;
        MAP[UP | DOWN | LEFT] = ++index;
        MAP[UP] = ++index;
        MAP[UP | RIGHT] = ++index;
        MAP[UP | LEFT] = ++index;
        MAP[RIGHT] = ++index;
        MAP[LEFT] = ++index;
        SIZE = index + 1;
    }

    public RectangleWithoutVerticalMiddleCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "rectangle_without_vertical_middle"), SIZE, AXIS_ALIGNED);
    }

    @Override
    public int getTextureIndex(int context) {
        return MAP[context & AXIS_ALIGNED_FLAGS];
    }
}
