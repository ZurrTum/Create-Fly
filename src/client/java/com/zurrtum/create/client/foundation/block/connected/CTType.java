package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class CTType {
    public static final int UP_FLAG = 1;
    public static final int DOWN_FLAG = 1 << 1;
    public static final int LEFT_FLAG = 1 << 2;
    public static final int RIGHT_FLAG = 1 << 3;
    public static final int TOP_LEFT_FLAG = 1 << 4;
    public static final int TOP_RIGHT_FLAG = 1 << 5;
    public static final int BOTTOM_LEFT_FLAG = 1 << 6;
    public static final int BOTTOM_RIGHT_FLAG = 1 << 7;
    public static final int AXIS_FLAGS = 0b11;
    public static final int AXIS_ALIGNED_FLAGS = 0b1111;
    public static final int ALL_FLAGS = 0b11111111;
    public static final CTPosStep UP = new CTPosStep(UP_FLAG, 0, 1);
    public static final CTPosStep DOWN = new CTPosStep(DOWN_FLAG, 0, -1);
    public static final CTPosStep LEFT = new CTPosStep(LEFT_FLAG, -1, 0);
    public static final CTPosStep RIGHT = new CTPosStep(RIGHT_FLAG, 1, 0);
    public static final CTPosStep TOP_LEFT = new CTPosStep(UP_FLAG | LEFT_FLAG, TOP_LEFT_FLAG, -1, 1);
    public static final CTPosStep TOP_RIGHT = new CTPosStep(UP_FLAG | RIGHT_FLAG, TOP_RIGHT_FLAG, 1, 1);
    public static final CTPosStep BOTTOM_LEFT = new CTPosStep(DOWN_FLAG | LEFT_FLAG, BOTTOM_LEFT_FLAG, -1, -1);
    public static final CTPosStep BOTTOM_RIGHT = new CTPosStep(DOWN_FLAG | RIGHT_FLAG, BOTTOM_RIGHT_FLAG, 1, -1);
    public static final CTPosStep[] VERTICAL = new CTPosStep[]{UP, DOWN};
    public static final CTPosStep[] AXIS_ALIGNED = new CTPosStep[]{UP, DOWN, LEFT, RIGHT};
    public static final CTPosStep[] ALL = new CTPosStep[]{
        UP, DOWN, LEFT, RIGHT, TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    };

    private final Identifier id;
    private final int spriteSize;
    private final CTPosStep[] steps;

    public CTType(Identifier id, int spriteSize, CTPosStep[] steps) {
        this.id = id;
        this.spriteSize = spriteSize;
        this.steps = steps;
    }

    public CTType(Identifier id, CTPosStep[] steps) {
        this(id, 1 << steps.length, steps);
    }

    public CTType(String name, CTPosStep[] steps) {
        this(Identifier.fromNamespaceAndPath(MOD_ID, name), steps);
    }

    public Identifier getId() {
        return id;
    }

    public int getSpriteSize() {
        return spriteSize;
    }

    public CTPosStep[] getSteps() {
        return steps;
    }

    public int getTextureIndex(int context) {
        return context;
    }

    public boolean replaceOriginal() {
        return false;
    }
}
