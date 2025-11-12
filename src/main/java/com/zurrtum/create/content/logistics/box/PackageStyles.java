package com.zurrtum.create.content.logistics.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import static com.zurrtum.create.Create.MOD_ID;

public class PackageStyles {
    public record PackageStyle(String namespace, String type, int width, int height, float riggingOffset, boolean rare) {
        public PackageStyle(String type, int width, int height, float riggingOffset, boolean rare) {
            this(MOD_ID, type, width, height, riggingOffset, rare);
        }

        public Identifier getItemId() {
            String size = "_" + width + "x" + height;
            String id = type + "_package" + (rare ? "" : size);
            return Identifier.fromNamespaceAndPath(namespace, id);
        }

        public Identifier getModel() {
            if (type.equals("cardboard")) {
                return Identifier.fromNamespaceAndPath(namespace, "item/package/cardboard_" + width + "x" + height);
            } else {
                return getItemId().withPrefix("item/");
            }
        }

        public Identifier getRiggingModel() {
            String size = width + "x" + height;
            return Identifier.fromNamespaceAndPath(namespace, "item/package/rigging_" + size);
        }
    }

    public static final List<PackageItem> ALL_BOXES = new ArrayList<>();
    public static final List<PackageItem> STANDARD_BOXES = new ArrayList<>();
    public static final List<PackageItem> RARE_BOXES = new ArrayList<>();

    private static final Random STYLE_PICKER = new Random();
    private static final int RARE_CHANCE = 7500; // addons, have mercy

    public static ItemStack getRandomBox() {
        List<PackageItem> pool = STYLE_PICKER.nextInt(RARE_CHANCE) == 0 ? RARE_BOXES : STANDARD_BOXES;
        return new ItemStack(pool.get(STYLE_PICKER.nextInt(pool.size())));
    }

    public static ItemStack getDefaultBox() {
        return new ItemStack(ALL_BOXES.getFirst());
    }

    private static PackageStyle rare(String name) {
        return new PackageStyle("rare_" + name, 12, 10, 21f, true);
    }
}