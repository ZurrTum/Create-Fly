package com.zurrtum.create;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBlockTags {
    public static final TagKey<Block> BRITTLE = register("brittle");
    public static final TagKey<Block> CASING = register("casing");
    public static final TagKey<Block> COPYCAT_ALLOW = register("copycat_allow");
    public static final TagKey<Block> COPYCAT_DENY = register("copycat_deny");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_BLASTING = register("fan_processing_catalysts/blasting");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_HAUNTING = register("fan_processing_catalysts/haunting");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_SMOKING = register("fan_processing_catalysts/smoking");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_SPLASHING = register("fan_processing_catalysts/splashing");
    public static final TagKey<Block> FAN_TRANSPARENT = register("fan_transparent");
    public static final TagKey<Block> GIRDABLE_TRACKS = register("girdable_tracks");
    public static final TagKey<Block> MOVABLE_EMPTY_COLLIDER = register("movable_empty_collider");
    public static final TagKey<Block> NON_MOVABLE = register("non_movable");
    public static final TagKey<Block> NON_BREAKABLE = register("non_breakable");
    public static final TagKey<Block> PASSIVE_BOILER_HEATERS = register("passive_boiler_heaters");
    public static final TagKey<Block> SAFE_NBT = register("safe_nbt");
    public static final TagKey<Block> SEATS = register("seats");
    public static final TagKey<Block> POSTBOXES = register("postboxes");
    public static final TagKey<Block> TABLE_CLOTHS = register("table_cloths");
    public static final TagKey<Block> TOOLBOXES = register("toolboxes");
    public static final TagKey<Block> TRACKS = register("tracks");
    public static final TagKey<Block> TREE_ATTACHMENTS = register("tree_attachments");
    public static final TagKey<Block> VALVE_HANDLES = register("valve_handles");
    public static final TagKey<Block> WINDMILL_SAILS = register("windmill_sails");
    public static final TagKey<Block> WRENCH_PICKUP = register("wrench_pickup");
    public static final TagKey<Block> CHEST_MOUNTED_STORAGE = register("chest_mounted_storage");
    public static final TagKey<Block> SIMPLE_MOUNTED_STORAGE = register("simple_mounted_storage");
    public static final TagKey<Block> FALLBACK_MOUNTED_STORAGE_BLACKLIST = register("fallback_mounted_storage_blacklist");
    public static final TagKey<Block> ROOTS = register("roots");
    public static final TagKey<Block> SUGAR_CANE_VARIANTS = register("sugar_cane_variants");
    public static final TagKey<Block> NON_HARVESTABLE = register("non_harvestable");
    public static final TagKey<Block> SINGLE_BLOCK_INVENTORIES = register("single_block_inventories");

    public static final TagKey<Block> CORALS = register("corals");

    public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = register("c", "relocation_not_supported");
    public static final TagKey<Block> CARDBOARD_STORAGE_BLOCKS = register("c", "storage_blocks/cardboard");
    public static final TagKey<Block> ANDESITE_ALLOY_STORAGE_BLOCKS = register("c", "storage_blocks/andesite_alloy");

    public static final TagKey<Block> SLIMY_LOGS = register("tconstruct", "slimy_logs");
    public static final TagKey<Block> NON_DOUBLE_DOOR = register("quark", "non_double_door");

    private static TagKey<Block> register(String name) {
        return TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name));
    }

    private static TagKey<Block> register(String namespace, String name) {
        return TagKey.of(RegistryKeys.BLOCK, Identifier.of(namespace, name));
    }

    public static void register() {
    }
}
