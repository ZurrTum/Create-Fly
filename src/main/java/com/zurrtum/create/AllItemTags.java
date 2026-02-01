package com.zurrtum.create;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.zurrtum.create.Create.MOD_ID;

public class AllItemTags {
    public static final TagKey<Item> BLAZE_BURNER_FUEL_REGULAR = register("blaze_burner_fuel/regular");
    public static final TagKey<Item> BLAZE_BURNER_FUEL_SPECIAL = register("blaze_burner_fuel/special");
    public static final TagKey<Item> CASING = register("casing");
    public static final TagKey<Item> CONTRAPTION_CONTROLLED = register("contraption_controlled");
    public static final TagKey<Item> CREATE_INGOTS = register("create_ingots");
    public static final TagKey<Item> CRUSHED_RAW_MATERIALS = register("crushed_raw_materials");
    public static final TagKey<Item> INVALID_FOR_TRACK_PAVING = register("invalid_for_track_paving");
    public static final TagKey<Item> DEPLOYABLE_DRINK = register("deployable_drink");
    public static final TagKey<Item> PRESSURIZED_AIR_SOURCES = register("pressurized_air_sources");
    public static final TagKey<Item> SANDPAPER = register("sandpaper");
    public static final TagKey<Item> SEATS = register("seats");
    public static final TagKey<Item> POSTBOXES = register("postboxes");
    public static final TagKey<Item> TABLE_CLOTHS = register("table_cloths");
    public static final TagKey<Item> DYED_TABLE_CLOTHS = register("dyed_table_cloths");
    public static final TagKey<Item> PULPIFIABLE = register("pulpifiable");
    public static final TagKey<Item> SLEEPERS = register("sleepers");
    public static final TagKey<Item> TOOLBOXES = register("toolboxes");
    public static final TagKey<Item> PACKAGES = register("packages");
    public static final TagKey<Item> CHAIN_RIDEABLE = register("chain_rideable");
    public static final TagKey<Item> TRACKS = register("tracks");
    public static final TagKey<Item> UPRIGHT_ON_BELT = register("upright_on_belt");
    public static final TagKey<Item> NOT_UPRIGHT_ON_BELT = register("not_upright_on_belt");
    public static final TagKey<Item> NOT_POTION = register("not_potion");
    public static final TagKey<Item> VALVE_HANDLES = register("valve_handles");
    public static final TagKey<Item> DISPENSE_BEHAVIOR_WRAP_BLACKLIST = register("dispense_behavior_wrap_blacklist");
    public static final TagKey<Item> REPAIRS_COPPER_ARMOR = register("repairs_copper_armor");
    public static final TagKey<Item> REPAIRS_CARDBOARD_ARMOR = register("repairs_cardboard_armor");
    public static final TagKey<Item> ENCHANTMENT_KNOCKBACK = register("enchantment/knockback");
    public static final TagKey<Item> ENCHANTMENT_LOOTING = register("enchantment/looting");
    public static final TagKey<Item> ENCHANTMENT_DENY_MENDING = register("enchantment/deny_mending");
    public static final TagKey<Item> ENCHANTMENT_DENY_UNBREAKING = register("enchantment/deny_unbreaking");
    public static final TagKey<Item> ENCHANTMENT_DENY_INFINITY = register("enchantment/deny_infinity");
    public static final TagKey<Item> ENCHANTMENT_DENY_AQUA_AFFINITY = register("enchantment/deny_aqua_affinity");

    public static final TagKey<Item> PLATES = register("c", "plates");
    public static final TagKey<Item> OBSIDIAN_DUST = register("c", "dusts/obsidian");
    public static final TagKey<Item> DYES = register("c", "dyes");
    public static final TagKey<Item> SLIME_BALLS = register("c", "slime_balls");
    public static final TagKey<Item> TOOLS_WRENCH = register("c", "tools/wrench");
    public static final TagKey<Item> OBSIDIAN_PLATES = register("c", "plates/obsidian");
    public static final TagKey<Item> CARDBOARD_PLATES = register("c", "plates/cardboard");
    public static final TagKey<Item> CERTUS_QUARTZ = register("c", "gems/certus_quartz");
    public static final TagKey<Item> AMETRINE_ORES = register("c", "ores/ametrine");
    public static final TagKey<Item> ANTHRACITE_ORES = register("c", "ores/anthracite");
    public static final TagKey<Item> EMERALDITE_ORES = register("c", "ores/emeraldite");
    public static final TagKey<Item> LIGNITE_ORES = register("c", "ores/lignite");
    public static final TagKey<Item> CARDBOARD_STORAGE_BLOCKS = register("c", "storage_blocks/cardboard");
    public static final TagKey<Item> ANDESITE_ALLOY_STORAGE_BLOCKS = register("c", "storage_blocks/andesite_alloy");
    public static final TagKey<Item> CHOCOLATE_BUCKETS = register("c", "buckets/chocolate");
    public static final TagKey<Item> HONEY_BUCKETS = register("c", "buckets/honey");
    public static final TagKey<Item> FOODS_CHOCOLATE = register("c", "foods/chocolate");
    public static final TagKey<Item> DRINKS_TEA = register("c", "drinks/tea");
    public static final TagKey<Item> FLOURS = register("c", "flours");
    public static final TagKey<Item> WHEAT_FLOURS = register("c", "flours/wheat");
    public static final TagKey<Item> WHEAT_DOUGHS = register("c", "foods/dough/wheat");

    public static final TagKey<Item> ALLURITE = register("stone_types/galosphere/allurite");
    public static final TagKey<Item> AMETHYST = register("stone_types/galosphere/amethyst");
    public static final TagKey<Item> LUMIERE = register("stone_types/galosphere/lumiere");

    public static final TagKey<Item> UA_CORAL = register("upgrade_aquatic/coral");
    public static final TagKey<Item> CURIOS_HEAD = register("curios", "head");

    private static TagKey<Item> register(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    private static TagKey<Item> register(String namespace, String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, name));
    }

    private static final Map<TagKey<Item>, DyeColor> dyesTag = Util.make(
        new HashMap<>(), map -> {
            for (DyeColor color : DyeColor.values()) {
                map.put(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + color.getName())), color);
            }
        }
    );

    @Nullable
    public static DyeColor getDyeColor(ItemStack stack) {
        DyeColor color = stack.get(DataComponents.DYE);
        if (color != null) {
            return color;
        }
        return dyesTag.entrySet().stream().filter(entry -> stack.is(entry.getKey())).map(Map.Entry::getValue).findAny().orElse(null);
    }

    public static void register() {
    }
}
