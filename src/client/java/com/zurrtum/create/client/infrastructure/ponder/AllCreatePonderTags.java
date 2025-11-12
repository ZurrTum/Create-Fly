package com.zurrtum.create.client.infrastructure.ponder;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class AllCreatePonderTags {

    public static final Identifier KINETIC_RELAYS = loc("kinetic_relays");
    public static final Identifier KINETIC_SOURCES = loc("kinetic_sources");
    public static final Identifier KINETIC_APPLIANCES = loc("kinetic_appliances");
    public static final Identifier FLUIDS = loc("fluids");
    public static final Identifier LOGISTICS = loc("logistics");
    public static final Identifier HIGH_LOGISTICS = loc("high_logistics");
    public static final Identifier REDSTONE = loc("redstone");
    public static final Identifier DECORATION = loc("decoration");
    public static final Identifier CREATIVE = loc("creative");
    public static final Identifier MOVEMENT_ANCHOR = loc("movement_anchor");
    public static final Identifier CONTRAPTION_ACTOR = loc("contraption_actor");
    public static final Identifier CONTRAPTION_ASSEMBLY = loc("contraption_assembly");
    public static final Identifier SAILS = loc("windmill_sails");
    public static final Identifier ARM_TARGETS = loc("arm_targets");
    public static final Identifier TRAIN_RELATED = loc("train_related");
    public static final Identifier DISPLAY_SOURCES = loc("display_sources");
    public static final Identifier DISPLAY_TARGETS = loc("display_targets");
    public static final Identifier THRESHOLD_SWITCH_TARGETS = loc("threshold_switch_targets");

    private static Identifier loc(String id) {
        return Create.asResource(id);
    }

    public static void register(PonderTagRegistrationHelper<Identifier> helper) {

        PonderTagRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(RegisteredObjectsHelper::getKeyOrThrow);

        helper.registerTag(KINETIC_RELAYS).addToIndex().item(AllItems.COGWHEEL, true, false).title("Kinetic Blocks")
            .description("Components which help relaying Rotational Force elsewhere").register();

        helper.registerTag(KINETIC_SOURCES).addToIndex().item(AllItems.WATER_WHEEL, true, false).title("Kinetic Sources")
            .description("Components which generate Rotational Force").register();

        helper.registerTag(KINETIC_APPLIANCES).addToIndex().item(AllItems.MECHANICAL_PRESS, true, false).title("Kinetic Appliances")
            .description("Components which make use of Rotational Force").register();

        helper.registerTag(FLUIDS).addToIndex().item(AllItems.FLUID_PIPE, true, false).title("Fluid Manipulators")
            .description("Components which help relaying and making use of Fluids").register();

        helper.registerTag(LOGISTICS).addToIndex().item(Blocks.CHEST, true, false).title("Item Transportation")
            .description("Components which help moving items around").register();

        helper.registerTag(HIGH_LOGISTICS).addToIndex().item(AllItems.STOCK_TICKER, true, false).title("High Logistics")
            .description("Components which help manage distributed item storage and automated requests around your factory").register();

        helper.registerTag(REDSTONE).addToIndex().item(Items.REDSTONE, true, false).title("Logic Components")
            .description("Components which help with redstone engineering").register();

        helper.registerTag(DECORATION).addToIndex().item(Items.ROSE_BUSH, true, false).title("Aesthetics")
            .description("Components used mostly for decorative purposes").register();

        helper.registerTag(CREATIVE).addToIndex().item(AllItems.CREATIVE_CRATE, true, false).title("Creative Mode")
            .description("Components not usually available for Survival Mode").register();

        helper.registerTag(MOVEMENT_ANCHOR).addToIndex().item(AllItems.MECHANICAL_PISTON, true, false).title("Movement Anchors")
            .description("Components which allow the creation of moving contraptions, animating an attached structure in a variety of ways")
            .register();

        helper.registerTag(CONTRAPTION_ACTOR).addToIndex().item(AllItems.MECHANICAL_HARVESTER, true, false).title("Contraption Actors")
            .description("Components which expose special behaviour when attached to a moving contraption").register();

        helper.registerTag(CONTRAPTION_ASSEMBLY).addToIndex().item(AllItems.SUPER_GLUE, true, false).title("Block Attachment Utility")
            .description("Tools and Components used to assemble structures moved as an animated Contraption").register();

        helper.registerTag(SAILS).item(AllItems.WINDMILL_BEARING).title("Sails for Windmill Bearings").description(
                "Blocks that count towards the strength of a Windmill Contraption when assembled. Each of these have equal efficiency in doing so.")
            .register();

        helper.registerTag(ARM_TARGETS).item(AllItems.MECHANICAL_ARM).title("Targets for Mechanical Arms")
            .description("Components which can be selected as inputs or outputs to the Mechanical Arm").register();

        helper.registerTag(TRAIN_RELATED).addToIndex().item(AllItems.TRACK, true, false).title("Railway Equipment")
            .description("Components used in the construction or management of Train Contraptions").register();

        //		helper.registerTag(RECENTLY_UPDATED)
        //				.addToIndex()
        //				.item(AllItems.CLIPBOARD.get())
        //				.title("Recent Changes")
        //				.description("Components that have been added or changed significantly in the latest versions of Create")
        //				.register();

        helper.registerTag(DISPLAY_SOURCES).item(AllItems.DISPLAY_LINK).title("Sources for Display Links")
            .description("Components or Blocks which offer some data that can be read with a Display Link").register();

        helper.registerTag(DISPLAY_TARGETS).item(AllItems.DISPLAY_LINK).title("Targets for Display Links")
            .description("Components or Blocks which can process and display the data received from a Display Link").register();

        helper.registerTag(THRESHOLD_SWITCH_TARGETS).item(AllItems.THRESHOLD_SWITCH).title("Targets for Threshold Switches")
            .description("Threshold Switches can read from these blocks, as well as most item and fluid containers.").register();

        //		HELPER.addToTag(RECENTLY_UPDATED);

        HELPER.addToTag(KINETIC_RELAYS).add(AllItems.SHAFT).add(AllItems.COGWHEEL).add(AllItems.LARGE_COGWHEEL).add(AllItems.BELT_CONNECTOR)
            .add(AllItems.GEARBOX).add(AllItems.VERTICAL_GEARBOX).add(AllItems.CLUTCH).add(AllItems.GEARSHIFT).add(AllItems.ENCASED_CHAIN_DRIVE)
            .add(AllItems.ADJUSTABLE_CHAIN_GEARSHIFT).add(AllItems.CHAIN_CONVEYOR).add(AllItems.SEQUENCED_GEARSHIFT)
            .add(AllItems.ROTATION_SPEED_CONTROLLER);

        HELPER.addToTag(KINETIC_SOURCES).add(AllItems.HAND_CRANK).add(AllItems.COPPER_VALVE_HANDLE).add(AllItems.WATER_WHEEL)
            .add(AllItems.LARGE_WATER_WHEEL).add(AllItems.WINDMILL_BEARING).add(AllItems.STEAM_ENGINE).add(AllItems.CREATIVE_MOTOR);

        HELPER.addToTag(TRAIN_RELATED).add(AllItems.TRACK).add(AllItems.TRACK_STATION).add(AllItems.TRACK_SIGNAL).add(AllItems.TRACK_OBSERVER)
            .add(AllItems.TRAIN_CONTROLS).add(AllItems.SCHEDULE).add(AllItems.TRAIN_DOOR).add(AllItems.TRAIN_TRAPDOOR).add(AllItems.RAILWAY_CASING);

        HELPER.addToTag(KINETIC_APPLIANCES).add(AllItems.MILLSTONE).add(AllItems.TURNTABLE).add(AllItems.ENCASED_FAN).add(AllItems.CUCKOO_CLOCK)
            .add(AllItems.MECHANICAL_PRESS).add(AllItems.MECHANICAL_MIXER).add(AllItems.MECHANICAL_CRAFTER).add(AllItems.MECHANICAL_DRILL)
            .add(AllItems.MECHANICAL_SAW).add(AllItems.DEPLOYER).add(AllItems.MECHANICAL_PUMP).add(AllItems.MECHANICAL_ARM)
            .add(AllItems.MECHANICAL_PISTON).add(AllItems.ROPE_PULLEY).add(AllItems.ELEVATOR_PULLEY).add(AllItems.MECHANICAL_BEARING)
            .add(AllItems.GANTRY_SHAFT).add(AllItems.GANTRY_CARRIAGE).add(AllItems.CLOCKWORK_BEARING).add(AllItems.DISPLAY_BOARD)
            .add(AllItems.CRUSHING_WHEEL);

        HELPER.addToTag(FLUIDS).add(AllItems.FLUID_PIPE).add(AllItems.MECHANICAL_PUMP).add(AllItems.FLUID_VALVE).add(AllItems.SMART_FLUID_PIPE)
            .add(AllItems.HOSE_PULLEY).add(AllItems.ITEM_DRAIN).add(AllItems.SPOUT).add(AllItems.PORTABLE_FLUID_INTERFACE).add(AllItems.FLUID_TANK)
            .add(AllItems.CREATIVE_FLUID_TANK);

        HELPER.addToTag(ARM_TARGETS).add(AllItems.MECHANICAL_ARM).add(AllItems.BELT_CONNECTOR).add(AllItems.CHUTE).add(AllItems.DEPOT)
            .add(AllItems.WEIGHTED_EJECTOR).add(AllItems.BASIN).add(AllItems.ANDESITE_FUNNEL).add(AllItems.BRASS_FUNNEL)
            .add(AllItems.MECHANICAL_CRAFTER).add(AllItems.MILLSTONE).add(AllItems.DEPLOYER).add(AllItems.MECHANICAL_SAW).add(AllItems.BLAZE_BURNER)
            .add(AllItems.CRUSHING_WHEEL).add(AllItems.TRACK_STATION);

        itemHelper.addToTag(ARM_TARGETS).add(Blocks.COMPOSTER).add(Blocks.JUKEBOX).add(Blocks.CAMPFIRE).add(Blocks.SOUL_CAMPFIRE)
            .add(Blocks.RESPAWN_ANCHOR);

        HELPER.addToTag(LOGISTICS).add(AllItems.BELT_CONNECTOR).add(AllItems.FILTER).add(AllItems.ATTRIBUTE_FILTER).add(AllItems.CHUTE)
            .add(AllItems.SMART_CHUTE).add(AllItems.ITEM_VAULT).add(AllItems.DEPOT).add(AllItems.WEIGHTED_EJECTOR).add(AllItems.MECHANICAL_ARM)
            .add(AllItems.ANDESITE_FUNNEL).add(AllItems.BRASS_FUNNEL).add(AllItems.ANDESITE_TUNNEL).add(AllItems.BRASS_TUNNEL)
            .add(AllItems.SMART_OBSERVER).add(AllItems.THRESHOLD_SWITCH).add(AllItems.CREATIVE_CRATE).add(AllItems.PORTABLE_STORAGE_INTERFACE);

        HELPER.addToTag(DECORATION).add(AllItems.ORANGE_NIXIE_TUBE).add(AllItems.DISPLAY_BOARD).add(AllItems.CUCKOO_CLOCK)
            .add(AllItems.WOODEN_BRACKET).add(AllItems.METAL_BRACKET).add(AllItems.METAL_GIRDER).add(AllItems.ANDESITE_CASING)
            .add(AllItems.BRASS_CASING).add(AllItems.COPPER_CASING).add(AllItems.RAILWAY_CASING);

        HELPER.addToTag(CREATIVE).add(AllItems.CREATIVE_CRATE).add(AllItems.CREATIVE_FLUID_TANK).add(AllItems.CREATIVE_MOTOR);

        HELPER.addToTag(SAILS).add(AllItems.SAIL).add(AllItems.SAIL_FRAME);

        itemHelper.addToTag(SAILS).add(Blocks.WHITE_WOOL);

        HELPER.addToTag(REDSTONE).add(AllItems.SMART_OBSERVER).add(AllItems.THRESHOLD_SWITCH).add(AllItems.ORANGE_NIXIE_TUBE)
            .add(AllItems.REDSTONE_CONTACT).add(AllItems.ANALOG_LEVER).add(AllItems.REDSTONE_LINK).add(AllItems.PULSE_EXTENDER)
            .add(AllItems.PULSE_REPEATER).add(AllItems.PULSE_TIMER).add(AllItems.POWERED_LATCH).add(AllItems.POWERED_TOGGLE_LATCH)
            .add(AllItems.ROSE_QUARTZ_LAMP);

        HELPER.addToTag(MOVEMENT_ANCHOR).add(AllItems.MECHANICAL_PISTON).add(AllItems.WINDMILL_BEARING).add(AllItems.MECHANICAL_BEARING)
            .add(AllItems.CLOCKWORK_BEARING).add(AllItems.ROPE_PULLEY).add(AllItems.ELEVATOR_PULLEY).add(AllItems.GANTRY_CARRIAGE)
            .add(AllItems.CART_ASSEMBLER).add(AllItems.TRACK_STATION);

        HELPER.addToTag(CONTRAPTION_ASSEMBLY).add(AllItems.LINEAR_CHASSIS).add(AllItems.SECONDARY_LINEAR_CHASSIS).add(AllItems.RADIAL_CHASSIS)
            .add(AllItems.SUPER_GLUE).add(AllItems.STICKER);

        itemHelper.addToTag(CONTRAPTION_ASSEMBLY).add(Blocks.SLIME_BLOCK).add(Blocks.HONEY_BLOCK);

        HELPER.addToTag(HIGH_LOGISTICS).add(AllItems.PACKAGER).add(AllItems.STOCK_LINK).add(AllItems.STOCK_TICKER).add(AllItems.PACKAGE_FROGPORT)
            .add(AllItems.WHITE_POSTBOX).add(AllItems.REDSTONE_REQUESTER).add(AllItems.RED_TABLE_CLOTH).add(AllItems.FACTORY_GAUGE)
            .add(AllItems.REPACKAGER).add(AllItems.PACKAGE_FILTER);

        HELPER.addToTag(CONTRAPTION_ACTOR).add(AllItems.MECHANICAL_HARVESTER).add(AllItems.MECHANICAL_PLOUGH).add(AllItems.MECHANICAL_DRILL)
            .add(AllItems.MECHANICAL_SAW).add(AllItems.DEPLOYER).add(AllItems.PORTABLE_STORAGE_INTERFACE).add(AllItems.PORTABLE_FLUID_INTERFACE)
            .add(AllItems.MECHANICAL_BEARING).add(AllItems.ANDESITE_FUNNEL).add(AllItems.BRASS_FUNNEL).add(AllItems.WHITE_SEAT)
            .add(AllItems.TRAIN_CONTROLS).add(AllItems.CONTRAPTION_CONTROLS).add(AllItems.REDSTONE_CONTACT);

        itemHelper.addToTag(CONTRAPTION_ACTOR).add(Blocks.BELL).add(Blocks.DISPENSER).add(Blocks.DROPPER);

        HELPER.addToTag(DISPLAY_SOURCES).add(AllItems.WHITE_SEAT).add(AllItems.ORANGE_NIXIE_TUBE).add(AllItems.THRESHOLD_SWITCH)
            .add(AllItems.SMART_OBSERVER).add(AllItems.ANDESITE_TUNNEL).add(AllItems.TRACK_OBSERVER).add(AllItems.TRACK_STATION)
            .add(AllItems.DISPLAY_LINK).add(AllItems.BRASS_TUNNEL).add(AllItems.CUCKOO_CLOCK).add(AllItems.STRESSOMETER).add(AllItems.SPEEDOMETER)
            .add(AllItems.FLUID_TANK).add(AllItems.FACTORY_GAUGE).add(AllItems.BELT_CONNECTOR);

        itemHelper.addToTag(DISPLAY_SOURCES).add(Blocks.ENCHANTING_TABLE).add(Blocks.RESPAWN_ANCHOR).add(Blocks.COMMAND_BLOCK).add(Blocks.TARGET);

        HELPER.addToTag(THRESHOLD_SWITCH_TARGETS).add(AllItems.ROPE_PULLEY).add(AllItems.ITEM_VAULT).add(AllItems.FLUID_TANK);

        itemHelper.addToTag(THRESHOLD_SWITCH_TARGETS).add(Blocks.CHEST).add(Blocks.BARREL);

        //TODO
        //        Mods.COMPUTERCRAFT.executeIfInstalled(() -> () -> {
        //            Block computer = BuiltInRegistries.BLOCK.get(Mods.COMPUTERCRAFT.rl("computer_advanced"));
        //            if (computer != Blocks.AIR)
        //                itemHelper.addToTag(DISPLAY_SOURCES).add(computer);
        //        });

        HELPER.addToTag(DISPLAY_TARGETS).add(AllItems.ORANGE_NIXIE_TUBE).add(AllItems.DISPLAY_BOARD).add(AllItems.DISPLAY_LINK);

        itemHelper.addToTag(DISPLAY_TARGETS).add(Blocks.OAK_SIGN).add(Blocks.LECTERN);
    }

}