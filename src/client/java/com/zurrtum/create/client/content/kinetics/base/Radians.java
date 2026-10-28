package com.zurrtum.create.client.content.kinetics.base;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class Radians {
    public static final float DEG_2 = Mth.DEG_TO_RAD * 2;
    public static final float DEG_5 = Mth.DEG_TO_RAD * 5;
    public static final float DEG_10 = Mth.DEG_TO_RAD * 10;
    public static final float DEG_12P5 = Mth.DEG_TO_RAD * 12.5f;
    public static final float DEG_15 = Mth.DEG_TO_RAD * 15;
    public static final float DEG_16 = Mth.DEG_TO_RAD * 16;
    public static final float DEG_20 = Mth.DEG_TO_RAD * 20;
    public static final float DEG_22P5 = Mth.DEG_TO_RAD * 22.5f;
    public static final float DEG_24 = Mth.DEG_TO_RAD * 24;
    public static final float DEG_25 = Mth.DEG_TO_RAD * 25;
    public static final float DEG_30 = Mth.DEG_TO_RAD * 30;
    public static final float DEG_35 = Mth.DEG_TO_RAD * 35;
    public static final float DEG_40 = Mth.DEG_TO_RAD * 40;
    public static final float DEG_45 = Mth.DEG_TO_RAD * 45;
    public static final float DEG_50 = Mth.DEG_TO_RAD * 50;
    public static final float DEG_55 = Mth.DEG_TO_RAD * 55;
    public static final float DEG_65 = Mth.DEG_TO_RAD * 65;
    public static final float DEG_67P5 = Mth.DEG_TO_RAD * 67.5f;
    public static final float DEG_70 = Mth.DEG_TO_RAD * 70;
    public static final float DEG_75 = Mth.DEG_TO_RAD * 75;
    public static final float DEG_80 = Mth.DEG_TO_RAD * 80;
    public static final float DEG_90 = Mth.HALF_PI;
    public static final float DEG_105 = Mth.DEG_TO_RAD * 105;
    public static final float DEG_112P5 = Mth.DEG_TO_RAD * 112.5f;
    public static final float DEG_120 = Mth.DEG_TO_RAD * 120;
    public static final float DEG_135 = Mth.DEG_TO_RAD * 135;
    public static final float DEG_145 = Mth.DEG_TO_RAD * 145;
    public static final float DEG_150 = Mth.DEG_TO_RAD * 150;
    public static final float DEG_158 = Mth.DEG_TO_RAD * 158;
    public static final float DEG_180 = Mth.PI;
    public static final float DEG_200 = Mth.DEG_TO_RAD * 200;
    public static final float DEG_225 = Mth.DEG_TO_RAD * 225;
    public static final float DEG_230 = Mth.DEG_TO_RAD * 230;
    public static final float DEG_270 = Mth.DEG_TO_RAD * 270;
    public static final float DEG_290 = Mth.DEG_TO_RAD * 290;
    public static final float DEG_292P5 = Mth.DEG_TO_RAD * 292.5f;
    public static final float DEG_305 = Mth.DEG_TO_RAD * 305;
    public static final float DEG_315 = Mth.DEG_TO_RAD * 315;
    public static final float DEG_320 = Mth.DEG_TO_RAD * 320;
    public static final float DEG_325 = Mth.DEG_TO_RAD * 325;
    public static final float DEG_337P5 = Mth.DEG_TO_RAD * 337.5f;
    public static final float DEG_338 = Mth.DEG_TO_RAD * 338;
    public static final float DEG_340 = Mth.DEG_TO_RAD * 340;
    public static final float DEG_344P5 = Mth.DEG_TO_RAD * 344.5f;
    public static final float DEG_345 = Mth.DEG_TO_RAD * 345;
    public static final float DEG_348P75 = Mth.DEG_TO_RAD * 348.75f;
    public static final float DEG_350 = Mth.DEG_TO_RAD * 350;
    public static final float DEG_351P5 = Mth.DEG_TO_RAD * 351.5f;
    public static final float DEG_360 = Mth.TWO_PI;

    public static float horizontal(Direction facing) {
        if (facing == Direction.WEST) {
            return DEG_270;
        }
        if (facing == Direction.NORTH) {
            return DEG_180;
        }
        if (facing == Direction.EAST) {
            return DEG_90;
        }
        return 0;
    }

    public static float horizontalPlus90(Direction facing) {
        if (facing == Direction.WEST) {
            return 0;
        }
        if (facing == Direction.NORTH) {
            return DEG_270;
        }
        if (facing == Direction.EAST) {
            return DEG_180;
        }
        return DEG_90;
    }

    public static float horizontalPlus180(Direction facing) {
        if (facing == Direction.WEST) {
            return DEG_90;
        }
        if (facing == Direction.NORTH) {
            return 0;
        }
        if (facing == Direction.EAST) {
            return DEG_270;
        }
        return DEG_180;
    }

    public static float horizontalPlus270(Direction facing) {
        if (facing == Direction.WEST) {
            return DEG_180;
        }
        if (facing == Direction.NORTH) {
            return DEG_90;
        }
        if (facing == Direction.EAST) {
            return 0;
        }
        return DEG_270;
    }

    public static float negatedHorizontalPlus180(Direction facing) {
        if (facing == Direction.WEST) {
            return DEG_270;
        }
        if (facing == Direction.NORTH) {
            return 0;
        }
        if (facing == Direction.EAST) {
            return DEG_90;
        }
        return DEG_180;
    }

    public static float vertical(Direction facing) {
        if (facing == Direction.UP) {
            return DEG_270;
        }
        if (facing == Direction.DOWN) {
            return DEG_90;
        }
        return 0;
    }

    public static float verticalPlus90(Direction facing) {
        if (facing == Direction.UP) {
            return 0;
        }
        if (facing == Direction.DOWN) {
            return DEG_180;
        }
        return DEG_90;
    }

    public static float negatedVerticalPlus90(Direction facing) {
        if (facing == Direction.UP) {
            return 0;
        }
        if (facing == Direction.DOWN) {
            return DEG_180;
        }
        return DEG_270;
    }
}
