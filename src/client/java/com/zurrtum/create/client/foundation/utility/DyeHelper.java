package com.zurrtum.create.client.foundation.utility;

import com.zurrtum.create.catnip.data.Couple;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.DyeColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DyeHelper {

    public static ItemConvertible getWoolOfDye(DyeColor color) {
        return WOOL_TABLE.getOrDefault(color, () -> Blocks.WHITE_WOOL).get();
    }

    public static Couple<Integer> getDyeColors(DyeColor color) {
        return DYE_TABLE.getOrDefault(color, DYE_TABLE.get(DyeColor.WHITE));
    }

    /**
     * Adds a dye color s.t. Create's blocks can use it instead of defaulting to white.
     *
     * @param color       Dye color to add
     * @param brightColor Front (bright) RGB color
     * @param darkColor   Back (dark) RGB color
     * @param wool        Supplier of wool item/block corresponding to the color
     */
    public static void addDye(DyeColor color, Integer brightColor, Integer darkColor, Supplier<ItemConvertible> wool) {
        DYE_TABLE.put(color, Couple.create(brightColor, darkColor));
        WOOL_TABLE.put(color, wool);
    }

    private static void addDye(DyeColor color, Integer brightColor, Integer darkColor, ItemConvertible wool) {
        addDye(color, brightColor, darkColor, () -> wool);
    }

    private static final Map<DyeColor, Supplier<ItemConvertible>> WOOL_TABLE = new HashMap<>();

    private static final Map<DyeColor, Couple<Integer>> DYE_TABLE = new HashMap<>();

    static {
        // DyeColor, ( Front RGB, Back RGB )
        addDye(DyeColor.BLACK, 0x45403B, 0x21201F, Blocks.BLACK_WOOL);
        addDye(DyeColor.RED, 0xB13937, 0x632737, Blocks.RED_WOOL);
        addDye(DyeColor.GREEN, 0x208A46, 0x1D6045, Blocks.GREEN_WOOL);
        addDye(DyeColor.BROWN, 0xAC855C, 0x68533E, Blocks.BROWN_WOOL);

        addDye(DyeColor.BLUE, 0x5391E1, 0x504B90, Blocks.BLUE_WOOL);
        addDye(DyeColor.GRAY, 0x5D666F, 0x313538, Blocks.GRAY_WOOL);
        addDye(DyeColor.LIGHT_GRAY, 0x95969B, 0x707070, Blocks.LIGHT_GRAY_WOOL);
        addDye(DyeColor.PURPLE, 0x9F54AE, 0x63366C, Blocks.PURPLE_WOOL);

        addDye(DyeColor.CYAN, 0x3EABB4, 0x3C7872, Blocks.CYAN_WOOL);
        addDye(DyeColor.PINK, 0xD5A8CB, 0xB86B95, Blocks.PINK_WOOL);
        addDye(DyeColor.LIME, 0xA3DF55, 0x4FB16F, Blocks.LIME_WOOL);
        addDye(DyeColor.YELLOW, 0xE6D756, 0xE9AC29, Blocks.YELLOW_WOOL);

        addDye(DyeColor.LIGHT_BLUE, 0x69CED2, 0x508AA5, Blocks.LIGHT_BLUE_WOOL);
        addDye(DyeColor.ORANGE, 0xEE9246, 0xD94927, Blocks.ORANGE_WOOL);
        addDye(DyeColor.MAGENTA, 0xF062B0, 0xC04488, Blocks.MAGENTA_WOOL);
        addDye(DyeColor.WHITE, 0xEDEAE5, 0xBBB6B0, Blocks.WHITE_WOOL);
    }
}
