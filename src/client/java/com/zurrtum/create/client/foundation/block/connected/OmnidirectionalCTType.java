package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class OmnidirectionalCTType extends CTType {
    private static final int UP = UP_FLAG;
    private static final int DOWN = DOWN_FLAG;
    private static final int LEFT = LEFT_FLAG;
    private static final int RIGHT = RIGHT_FLAG;
    private static final int TOP_LEFT = TOP_LEFT_FLAG;
    private static final int TOP_RIGHT = TOP_RIGHT_FLAG;
    private static final int BOTTOM_LEFT = BOTTOM_LEFT_FLAG;
    private static final int BOTTOM_RIGHT = BOTTOM_RIGHT_FLAG;
    private static final int UP_DOWN_LEFT = UP | DOWN | LEFT;
    private static final int UP_DOWN_RIGHT = UP | DOWN | RIGHT;
    private static final int UP_LEFT_RIGHT = UP | LEFT | RIGHT;
    private static final int DOWN_LEFT_RIGHT = DOWN | LEFT | RIGHT;
    private static final int UP_DOWN_LEFT_RIGHT = UP | DOWN | LEFT | RIGHT;
    private static final int TOP_LEFT_RIGHT = TOP_LEFT | TOP_RIGHT;
    private static final int BOTTOM_LEFT_RIGHT = BOTTOM_LEFT | BOTTOM_RIGHT;

    public static final int[] MAP;
    public static final int SIZE;

    static {
        MAP = new int[ALL_FLAGS + 1];
        int index = 0;
        MAP[UP] = ++index;
        MAP[DOWN] = ++index;
        MAP[UP | DOWN] = ++index;
        MAP[LEFT] = ++index;
        MAP[UP | LEFT] = ++index;
        MAP[DOWN | LEFT] = ++index;
        MAP[UP_DOWN_LEFT] = ++index;
        MAP[UP | LEFT | TOP_LEFT] = ++index;
        MAP[DOWN | LEFT | BOTTOM_LEFT] = ++index;
        MAP[RIGHT] = ++index;
        MAP[UP | RIGHT] = ++index;
        MAP[DOWN | RIGHT] = ++index;
        MAP[UP_DOWN_RIGHT] = ++index;
        MAP[UP | RIGHT | TOP_RIGHT] = ++index;
        MAP[DOWN | RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[LEFT | RIGHT] = ++index;
        MAP[UP_LEFT_RIGHT] = ++index;
        MAP[DOWN_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT | BOTTOM_LEFT] = ++index;
        MAP[UP_DOWN_LEFT | TOP_LEFT] = ++index;
        MAP[UP_DOWN_LEFT | TOP_LEFT | BOTTOM_LEFT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | BOTTOM_LEFT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_RIGHT | BOTTOM_LEFT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT | BOTTOM_LEFT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_LEFT] = ++index;
        MAP[UP_DOWN_RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[UP_DOWN_RIGHT | TOP_RIGHT] = ++index;
        MAP[UP_DOWN_RIGHT | TOP_RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT | BOTTOM_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[UP_LEFT_RIGHT | TOP_LEFT] = ++index;
        MAP[UP_LEFT_RIGHT | TOP_RIGHT] = ++index;
        MAP[UP_LEFT_RIGHT | TOP_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | BOTTOM_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_RIGHT | BOTTOM_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT | BOTTOM_LEFT_RIGHT] = ++index;
        MAP[UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_LEFT_RIGHT] = ++index;
        MAP[DOWN_LEFT_RIGHT | BOTTOM_LEFT] = ++index;
        MAP[DOWN_LEFT_RIGHT | BOTTOM_RIGHT] = ++index;
        MAP[DOWN_LEFT_RIGHT | BOTTOM_LEFT_RIGHT] = ++index;
        SIZE = index + 1;
    }

    public OmnidirectionalCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "omnidirectional"), SIZE, ALL);
    }

    @Override
    public int getTextureIndex(int context) {
        return MAP[context & ALL_FLAGS];
    }
}
