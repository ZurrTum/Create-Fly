package com.zurrtum.create.client;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import net.minecraft.world.item.Item;

public class AllItemTooltips {
    public static void register(Item item) {
        TooltipModifier.REGISTRY.register(
            item,
            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE).andThen(TooltipModifier.mapNull(KineticStats.create(item)))
        );
    }

    public static void register(Item item, Item otherItem) {
        register(item);
        ItemDescription.referKey(item, () -> otherItem);
    }

    public static void register(Item item, String id) {
        register(item);
        ItemDescription.useKey(item, id);
    }

    public static void register() {
        register(AllItems.ATTRIBUTE_FILTER);
        register(AllItems.BLAZE_CAKE);
        register(AllItems.BUILDERS_TEA);
        register(AllItems.CARDBOARD_SWORD);
        register(AllItems.CHAIN_CONVEYOR);
        register(AllItems.CLIPBOARD);
        register(AllItems.CLOCKWORK_BEARING);
        register(AllItems.CONTROLLER_RAIL);
        register(AllItems.COPPER_BACKTANK);
        register(AllItems.COPPER_DIVING_BOOTS);
        register(AllItems.COPPER_DIVING_HELMET);
        register(AllItems.COPPER_VALVE_HANDLE);
        register(AllItems.CRAFTING_BLUEPRINT);
        register(AllItems.CREATIVE_BLAZE_CAKE);
        register(AllItems.CREATIVE_CRATE);
        register(AllItems.CREATIVE_MOTOR);
        register(AllItems.CRUSHING_WHEEL);
        register(AllItems.CUCKOO_CLOCK);
        register(AllItems.DEPLOYER);
        register(AllItems.DESK_BELL);
        register(AllItems.ELEVATOR_PULLEY);
        register(AllItems.EMPTY_SCHEMATIC);
        register(AllItems.ENCASED_FAN);
        register(AllItems.EXP_NUGGET);
        register(AllItems.EXTENDO_GRIP);
        register(AllItems.FILTER);
        register(AllItems.FLYWHEEL);
        register(AllItems.GOGGLES);
        register(AllItems.HAND_CRANK);
        register(AllItems.HAUNTED_BELL);
        register(AllItems.HOSE_PULLEY);
        register(AllItems.ITEM_HATCH);
        register(AllItems.LARGE_WATER_WHEEL);
        register(AllItems.LINKED_CONTROLLER);
        register(AllItems.MECHANICAL_ARM);
        register(AllItems.MECHANICAL_BEARING);
        register(AllItems.MECHANICAL_CRAFTER);
        register(AllItems.MECHANICAL_DRILL);
        register(AllItems.MECHANICAL_MIXER);
        register(AllItems.MECHANICAL_PISTON);
        register(AllItems.MECHANICAL_PRESS);
        register(AllItems.MECHANICAL_PUMP);
        register(AllItems.MECHANICAL_SAW);
        register(AllItems.METAL_BRACKET);
        register(AllItems.MILLSTONE);
        register(AllItems.MINECART_COUPLING);
        register(AllItems.MYSTERIOUS_CUCKOO_CLOCK);
        register(AllItems.NETHERITE_BACKTANK);
        register(AllItems.NETHERITE_DIVING_BOOTS);
        register(AllItems.NETHERITE_DIVING_HELMET);
        register(AllItems.NOZZLE);
        register(AllItems.PACKAGE_FILTER);
        register(AllItems.PECULIAR_BELL);
        register(AllItems.PLACARD);
        register(AllItems.POTATO_CANNON);
        register(AllItems.ROPE_PULLEY);
        register(AllItems.SAND_PAPER);
        register(AllItems.SCHEMATIC);
        register(AllItems.SCHEMATICANNON);
        register(AllItems.SCHEMATIC_AND_QUILL);
        register(AllItems.SCHEMATIC_TABLE);
        register(AllItems.STEAM_ENGINE);
        register(AllItems.STICKY_MECHANICAL_PISTON);
        register(AllItems.TREE_FERTILIZER);
        register(AllItems.TURNTABLE);
        register(AllItems.WAND_OF_SYMMETRY);
        register(AllItems.WATER_WHEEL);
        register(AllItems.WEIGHTED_EJECTOR);
        register(AllItems.WINDMILL_BEARING);
        register(AllItems.WOODEN_BRACKET);
        register(AllItems.WORLDSHAPER);
        register(AllItems.WRENCH);
        register(AllItems.COPYCAT_STEP);
        register(AllItems.COPYCAT_PANEL);
        register(AllItems.RED_SAND_PAPER, AllItems.SAND_PAPER);
        register(AllItems.CARDBOARD_HELMET, "item.create.cardboard_armor");
        register(AllItems.CARDBOARD_CHESTPLATE, "item.create.cardboard_armor");
        register(AllItems.CARDBOARD_LEGGINGS, "item.create.cardboard_armor");
        register(AllItems.CARDBOARD_BOOTS, "item.create.cardboard_armor");
        register(AllItems.WHITE_TOOLBOX, "block.create.toolbox");
        register(AllItems.ORANGE_TOOLBOX, "block.create.toolbox");
        register(AllItems.MAGENTA_TOOLBOX, "block.create.toolbox");
        register(AllItems.LIGHT_BLUE_TOOLBOX, "block.create.toolbox");
        register(AllItems.YELLOW_TOOLBOX, "block.create.toolbox");
        register(AllItems.LIME_TOOLBOX, "block.create.toolbox");
        register(AllItems.PINK_TOOLBOX, "block.create.toolbox");
        register(AllItems.GRAY_TOOLBOX, "block.create.toolbox");
        register(AllItems.LIGHT_GRAY_TOOLBOX, "block.create.toolbox");
        register(AllItems.CYAN_TOOLBOX, "block.create.toolbox");
        register(AllItems.PURPLE_TOOLBOX, "block.create.toolbox");
        register(AllItems.BLUE_TOOLBOX, "block.create.toolbox");
        register(AllItems.BROWN_TOOLBOX, "block.create.toolbox");
        register(AllItems.GREEN_TOOLBOX, "block.create.toolbox");
        register(AllItems.RED_TOOLBOX, "block.create.toolbox");
        register(AllItems.BLACK_TOOLBOX, "block.create.toolbox");
    }
}
