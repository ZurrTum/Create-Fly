package com.zurrtum.create;

import com.zurrtum.create.content.logistics.box.PackageStyles.PackageStyle;

import java.util.ArrayList;
import java.util.List;

public class AllPackageStyles {
    public static List<PackageStyle> ALL = new ArrayList<>();
    public static PackageStyle CARDBOARD_12X12 = cardboard(12, 12, 23f);
    public static PackageStyle CARDBOARD_10X12 = cardboard(10, 12, 22f);
    public static PackageStyle CARDBOARD_10X8 = cardboard(10, 8, 18f);
    public static PackageStyle CARDBOARD_12X10 = cardboard(12, 10, 21f);
    public static PackageStyle RARE_CREEPER = rare("creeper");
    public static PackageStyle RARE_DARCY = rare("darcy");
    public static PackageStyle RARE_EVAN = rare("evan");
    public static PackageStyle RARE_JINX = rare("jinx");
    public static PackageStyle RARE_KRYPPERS = rare("kryppers");
    public static PackageStyle RARE_SIMI = rare("simi");
    public static PackageStyle RARE_STARLOTTE = rare("starlotte");
    public static PackageStyle RARE_THUNDER = rare("thunder");
    public static PackageStyle RARE_UP = rare("up");
    public static PackageStyle RARE_VECTOR = rare("vector");

    public static PackageStyle cardboard(int width, int height, float riggingOffset) {
        PackageStyle style = new PackageStyle("cardboard", width, height, riggingOffset, false);
        ALL.add(style);
        return style;
    }

    public static PackageStyle rare(String name) {
        PackageStyle style = new PackageStyle("rare_" + name, 12, 10, 21f, true);
        ALL.add(style);
        return style;
    }

    public static void register() {
    }
}
