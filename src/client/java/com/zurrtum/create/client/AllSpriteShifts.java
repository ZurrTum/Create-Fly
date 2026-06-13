package com.zurrtum.create.client;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SpriteShifter;
import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShifter;
import com.zurrtum.create.client.foundation.block.connected.CTType;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class AllSpriteShifts {
    private static final Map<WoodType, CTSpriteShiftEntry> WOODEN_WINDOWS = Util.make(
        new IdentityHashMap<>(), map -> {
            for (WoodType woodType : new WoodType[]{
                WoodType.OAK,
                WoodType.SPRUCE,
                WoodType.BIRCH,
                WoodType.ACACIA,
                WoodType.JUNGLE,
                WoodType.DARK_OAK,
                WoodType.MANGROVE,
                WoodType.CRIMSON,
                WoodType.WARPED,
                WoodType.CHERRY,
                WoodType.BAMBOO
            }) {
                map.put(woodType, vertical("palettes/" + woodType.name() + "_window"));
            }
        }
    );
    public static final Map<WeatherState, CTSpriteShiftEntry> COPPER_SHINGLES = Util.make(
        new EnumMap<>(WeatherState.class), map -> {
            for (WeatherState state : WeatherState.values()) {
                String pref = "copper/" + (state == WeatherState.UNAFFECTED ? "" : Lang.asId(state.name()) + "_");
                map.put(state, getCT(AllCTTypes.ROOF, pref + "copper_roof_top", pref + "copper_shingles_top"));
            }
        }
    );
    public static final Map<WeatherState, CTSpriteShiftEntry> COPPER_TILES = Util.make(
        new EnumMap<>(WeatherState.class), map -> {
            for (WeatherState state : WeatherState.values()) {
                String pref = "copper/" + (state == WeatherState.UNAFFECTED ? "" : Lang.asId(state.name()) + "_");
                map.put(state, getCT(AllCTTypes.ROOF, pref + "copper_roof_top", pref + "copper_tiles_top"));
            }
        }
    );
    public static final Map<DyeColor, SpriteShiftEntry> DYED_BELTS = Util.make(
        new EnumMap<>(DyeColor.class), map -> {
            for (DyeColor color : DyeColor.VALUES) {
                map.put(color, get("block/belt", "block/belt/" + color.getSerializedName() + "_scroll"));
            }
        }
    );
    public static final Map<DyeColor, SpriteShiftEntry> DYED_OFFSET_BELTS = Util.make(
        new EnumMap<>(DyeColor.class), map -> {
            for (DyeColor color : DyeColor.VALUES) {
                map.put(color, get("block/belt_offset", "block/belt/" + color.getSerializedName() + "_scroll"));
            }
        }
    );
    public static final Map<DyeColor, SpriteShiftEntry> DYED_DIAGONAL_BELTS = Util.make(
        new EnumMap<>(DyeColor.class), map -> {
            for (DyeColor color : DyeColor.VALUES) {
                map.put(
                    color,
                    get("block/belt_diagonal", "block/belt/" + color.getSerializedName() + "_diagonal_scroll")
                );
            }
        }
    );
    public static final SpriteShiftEntry BURNER_FLAME = get(
        "block/blaze_burner_flame",
        "block/blaze_burner_flame_scroll"
    );
    public static final SpriteShiftEntry SUPER_BURNER_FLAME = get(
        "block/blaze_burner_flame",
        "block/blaze_burner_flame_superheated_scroll"
    );
    public static final CTSpriteShiftEntry ANDESITE_SCAFFOLD = horizontal("scaffold/andesite_scaffold");
    public static final CTSpriteShiftEntry BRASS_SCAFFOLD = horizontal("scaffold/brass_scaffold");
    public static final CTSpriteShiftEntry COPPER_SCAFFOLD = horizontal("scaffold/copper_scaffold");
    public static final CTSpriteShiftEntry ANDESITE_SCAFFOLD_INSIDE = horizontal("scaffold/andesite_scaffold_inside");
    public static final CTSpriteShiftEntry BRASS_SCAFFOLD_INSIDE = horizontal("scaffold/brass_scaffold_inside");
    public static final CTSpriteShiftEntry COPPER_SCAFFOLD_INSIDE = horizontal("scaffold/copper_scaffold_inside");
    public static final CTSpriteShiftEntry FRAMED_GLASS = getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        "palettes/framed_glass",
        "palettes/framed_glass"
    );
    public static final CTSpriteShiftEntry HORIZONTAL_FRAMED_GLASS = getCT(
        AllCTTypes.HORIZONTAL,
        "palettes/framed_glass",
        "palettes/horizontal_framed_glass"
    );
    public static final CTSpriteShiftEntry VERTICAL_FRAMED_GLASS = getCT(
        AllCTTypes.VERTICAL,
        "palettes/framed_glass",
        "palettes/vertical_framed_glass"
    );
    public static final CTSpriteShiftEntry ORNATE_IRON_WINDOW = vertical("palettes/ornate_iron_window");
    public static final CTSpriteShiftEntry INDUSTRIAL_IRON_WINDOW = getCT(
        AllCTTypes.RECTANGLE,
        "palettes/industrial_iron_window"
    );
    public static final CTSpriteShiftEntry OLD_FACTORY_WINDOW_1 = getCT(
        AllCTTypes.RECTANGLE_WITH_ORIGINAL,
        "palettes/weathered_iron_window",
        "palettes/weathered_iron_window_1"
    );
    public static final CTSpriteShiftEntry OLD_FACTORY_WINDOW_2 = getCT(
        AllCTTypes.RECTANGLE_WITH_ORIGINAL,
        "palettes/weathered_iron_window",
        "palettes/weathered_iron_window_2"
    );
    public static final CTSpriteShiftEntry OLD_FACTORY_WINDOW_3 = getCT(
        AllCTTypes.RECTANGLE_WITH_ORIGINAL,
        "palettes/weathered_iron_window",
        "palettes/weathered_iron_window_3"
    );
    public static final CTSpriteShiftEntry OLD_FACTORY_WINDOW_4 = getCT(
        AllCTTypes.RECTANGLE_WITH_ORIGINAL,
        "palettes/weathered_iron_window",
        "palettes/weathered_iron_window_4"
    );
    public static final CTSpriteShiftEntry CRAFTER_SIDE = vertical("crafter_side");
    public static final CTSpriteShiftEntry CRAFTER_OTHERSIDE = horizontal("crafter_side");
    public static final CTSpriteShiftEntry ANDESITE_ENCASED_COGWHEEL_SIDE = vertical("andesite_encased_cogwheel_side");
    public static final CTSpriteShiftEntry ANDESITE_ENCASED_COGWHEEL_OTHERSIDE = horizontal(
        "andesite_encased_cogwheel_side");
    public static final CTSpriteShiftEntry BRASS_ENCASED_COGWHEEL_SIDE = vertical("brass_encased_cogwheel_side");
    public static final CTSpriteShiftEntry BRASS_ENCASED_COGWHEEL_OTHERSIDE = horizontal("brass_encased_cogwheel_side");
    public static final CTSpriteShiftEntry GIRDER_POLE = vertical("girder_pole_side");
    public static final CTSpriteShiftEntry ANDESITE_CASING = omni("andesite_casing");
    public static final CTSpriteShiftEntry BRASS_CASING = omni("brass_casing");
    public static final CTSpriteShiftEntry COPPER_CASING = omni("copper_casing");
    public static final CTSpriteShiftEntry SHADOW_STEEL_CASING = omni("shadow_steel_casing");
    public static final CTSpriteShiftEntry REFINED_RADIANCE_CASING = omni("refined_radiance_casing");
    public static final CTSpriteShiftEntry RAILWAY_CASING = omni("railway_casing");
    public static final CTSpriteShiftEntry RAILWAY_CASING_SIDE = omni("railway_casing_side");
    public static final CTSpriteShiftEntry CREATIVE_CASING = getCT(AllCTTypes.RECTANGLE, "creative_casing");
    public static final CTSpriteShiftEntry CHASSIS_SIDE = omni("linear_chassis_side");
    public static final CTSpriteShiftEntry SECONDARY_CHASSIS_SIDE = omni("secondary_linear_chassis_side");
    public static final CTSpriteShiftEntry CHASSIS = omni("linear_chassis_end");
    public static final CTSpriteShiftEntry CHASSIS_STICKY = omni("linear_chassis_end_sticky");
    public static final CTSpriteShiftEntry BRASS_TUNNEL_TOP = vertical("tunnel/brass_tunnel_top");
    public static final CTSpriteShiftEntry FLUID_TANK = getCT(AllCTTypes.RECTANGLE, "fluid_tank");
    public static final CTSpriteShiftEntry FLUID_TANK_TOP = getCT(
        AllCTTypes.RECTANGLE_WITHOUT_SINGLE,
        "fluid_tank_top"
    );
    public static final CTSpriteShiftEntry FLUID_TANK_INNER = getCT(
        AllCTTypes.RECTANGLE_WITHOUT_SINGLE,
        "fluid_tank_inner"
    );
    public static final CTSpriteShiftEntry CREATIVE_FLUID_TANK = getCT(AllCTTypes.RECTANGLE, "creative_fluid_tank");
    public static final CTSpriteShiftEntry VAULT_TOP_MEDIUM = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_VERTICAL_MIDDLE,
        "vault/vault_top_small",
        "vault/vault_top_medium"
    );
    public static final CTSpriteShiftEntry VAULT_TOP_LARGE = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_HORIZONTAL_SINGLE,
        "vault/vault_top_small",
        "vault/vault_top_large"
    );
    public static final CTSpriteShiftEntry VAULT_FRONT_MEDIUM = getSpriteCT(
        AllCTTypes.SQUARE,
        "vault/vault_front_small",
        "vault/vault_front_medium"
    );
    public static final CTSpriteShiftEntry VAULT_FRONT_LARGE = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_SINGLE,
        "vault/vault_front_small",
        "vault/vault_front_large"
    );
    public static final CTSpriteShiftEntry VAULT_SIDE_MEDIUM = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_VERTICAL_MIDDLE,
        "vault/vault_side_small",
        "vault/vault_side_medium"
    );
    public static final CTSpriteShiftEntry VAULT_SIDE_LARGE = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_HORIZONTAL_SINGLE,
        "vault/vault_side_small",
        "vault/vault_side_large"
    );
    public static final CTSpriteShiftEntry VAULT_BOTTOM_MEDIUM = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_VERTICAL_MIDDLE,
        "vault/vault_bottom_small",
        "vault/vault_bottom_medium"
    );
    public static final CTSpriteShiftEntry VAULT_BOTTOM_LARGE = getSpriteCT(
        AllCTTypes.RECTANGLE_WITHOUT_HORIZONTAL_SINGLE,
        "vault/vault_bottom_small",
        "vault/vault_bottom_large"
    );
    public static final SpriteShiftEntry ELEVATOR_BELT = get(
        "block/elevator_pulley_belt",
        "block/elevator_pulley_belt_scroll"
    );
    public static final SpriteShiftEntry ROPE_PULLEY_COIL = get(
        "block/rope_pulley_coil",
        "block/rope_pulley_coil_scroll"
    );
    public static final SpriteShiftEntry ELEVATOR_COIL = get(
        "block/elevator_pulley_coil",
        "block/elevator_pulley_coil_scroll"
    );
    public static final SpriteShiftEntry HOSE_PULLEY_COIL = get(
        "block/hose_pulley_coil",
        "block/hose_pulley_coil_scroll"
    );
    public static final SpriteShiftEntry FACTORY_PANEL_CONNECTIONS = get(
        "block/factory_panel_connections",
        "block/factory_panel_connections_animated"
    );
    public static final SpriteShiftEntry BELT = get("block/belt", "block/belt_scroll");
    public static final SpriteShiftEntry BELT_OFFSET = get("block/belt_offset", "block/belt_scroll");
    public static final SpriteShiftEntry BELT_DIAGONAL = get("block/belt_diagonal", "block/belt_diagonal_scroll");
    public static final SpriteShiftEntry ANDESIDE_BELT_CASING = get(
        "block/belt/brass_belt_casing",
        "block/belt/andesite_belt_casing"
    );
    public static final SpriteShiftEntry CRAFTER_THINGIES = get("block/crafter_thingies", "block/crafter_thingies");
    public static final SpriteShiftEntry BOGEY_BELT = get("block/bogey/belt", "block/bogey/belt_scroll");

    //

    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

    private static CTSpriteShiftEntry horizontal(String name) {
        return getCT(AllCTTypes.HORIZONTAL, name);
    }

    private static CTSpriteShiftEntry vertical(String name) {
        return getCT(AllCTTypes.VERTICAL, name);
    }

    //

    private static SpriteShiftEntry get(String originalLocation, String targetLocation) {
        return SpriteShifter.get(Create.asResource(originalLocation), Create.asResource(targetLocation));
    }

    private static CTSpriteShiftEntry getSpriteCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(
            type,
            Create.asResource("block/" + blockTextureName),
            Create.asResource("block/" + connectedTextureName)
        );
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return getSpriteCT(type, blockTextureName, connectedTextureName + "_connected");
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getSpriteCT(type, blockTextureName, blockTextureName + "_connected");
    }

    //

    public static CTSpriteShiftEntry getWoodenWindow(WoodType woodType) {
        return WOODEN_WINDOWS.get(woodType);
    }

}
