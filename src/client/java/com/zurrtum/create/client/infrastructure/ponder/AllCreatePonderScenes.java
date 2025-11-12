package com.zurrtum.create.client.infrastructure.ponder;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.infrastructure.ponder.scenes.*;
import com.zurrtum.create.client.infrastructure.ponder.scenes.fluid.*;
import com.zurrtum.create.client.infrastructure.ponder.scenes.highLogistics.*;
import com.zurrtum.create.client.infrastructure.ponder.scenes.trains.*;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import java.util.Arrays;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AllCreatePonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

        HELPER.forComponents(AllItems.SHAFT).addStoryBoard("shaft/relay", KineticsScenes::shaftAsRelay, AllCreatePonderTags.KINETIC_RELAYS);
        HELPER.forComponents(AllItems.SHAFT, AllItems.ANDESITE_ENCASED_SHAFT, AllItems.BRASS_ENCASED_SHAFT)
            .addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased);

        HELPER.forComponents(AllItems.COGWHEEL).addStoryBoard("cog/small", KineticsScenes::cogAsRelay, AllCreatePonderTags.KINETIC_RELAYS)
            .addStoryBoard("cog/speedup", KineticsScenes::cogsSpeedUp).addStoryBoard("cog/encasing", KineticsScenes::cogwheelsCanBeEncased);

        HELPER.forComponents(AllItems.LARGE_COGWHEEL).addStoryBoard("cog/speedup", KineticsScenes::cogsSpeedUp)
            .addStoryBoard("cog/large", KineticsScenes::largeCogAsRelay, AllCreatePonderTags.KINETIC_RELAYS)
            .addStoryBoard("cog/encasing", KineticsScenes::cogwheelsCanBeEncased);

        HELPER.forComponents(AllItems.BELT_CONNECTOR).addStoryBoard("belt/connect", BeltScenes::beltConnector, AllCreatePonderTags.KINETIC_RELAYS)
            .addStoryBoard("belt/directions", BeltScenes::directions)
            .addStoryBoard("belt/transport", BeltScenes::transport, AllCreatePonderTags.LOGISTICS)
            .addStoryBoard("belt/encasing", BeltScenes::beltsCanBeEncased);

        HELPER.forComponents(AllItems.ANDESITE_CASING, AllItems.BRASS_CASING).addStoryBoard("shaft/encasing", KineticsScenes::shaftsCanBeEncased)
            .addStoryBoard("belt/encasing", BeltScenes::beltsCanBeEncased);

        HELPER.forComponents(AllItems.GEARBOX, AllItems.VERTICAL_GEARBOX)
            .addStoryBoard("gearbox", KineticsScenes::gearbox, AllCreatePonderTags.KINETIC_RELAYS);

        HELPER.addStoryBoard(AllItems.CLUTCH, "clutch", KineticsScenes::clutch, AllCreatePonderTags.KINETIC_RELAYS);
        HELPER.addStoryBoard(AllItems.GEARSHIFT, "gearshift", KineticsScenes::gearshift, AllCreatePonderTags.KINETIC_RELAYS);

        HELPER.forComponents(AllItems.SEQUENCED_GEARSHIFT).addStoryBoard("sequenced_gearshift", KineticsScenes::sequencedGearshift);

        HELPER.forComponents(AllItems.ENCASED_FAN).addStoryBoard("fan/direction", FanScenes::direction, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("fan/processing", FanScenes::processing);

        HELPER.forComponents(AllItems.CREATIVE_MOTOR)
            .addStoryBoard("creative_motor", KineticsScenes::creativeMotor, AllCreatePonderTags.KINETIC_SOURCES)
            .addStoryBoard("creative_motor_mojang", KineticsScenes::creativeMotorMojang);
        HELPER.addStoryBoard(AllItems.WATER_WHEEL, "water_wheel", KineticsScenes::waterWheel, AllCreatePonderTags.KINETIC_SOURCES);
        HELPER.addStoryBoard(AllItems.LARGE_WATER_WHEEL, "large_water_wheel", KineticsScenes::largeWaterWheel, AllCreatePonderTags.KINETIC_SOURCES);

        HELPER.addStoryBoard(AllItems.HAND_CRANK, "hand_crank", KineticsScenes::handCrank, AllCreatePonderTags.KINETIC_SOURCES);

        HELPER.addStoryBoard(AllItems.COPPER_VALVE_HANDLE, "valve_handle", KineticsScenes::valveHandle, AllCreatePonderTags.KINETIC_SOURCES);
        HELPER.forComponents(
            AllItems.WHITE_VALVE_HANDLE,
            AllItems.ORANGE_VALVE_HANDLE,
            AllItems.MAGENTA_VALVE_HANDLE,
            AllItems.LIGHT_BLUE_VALVE_HANDLE,
            AllItems.YELLOW_VALVE_HANDLE,
            AllItems.LIME_VALVE_HANDLE,
            AllItems.PINK_VALVE_HANDLE,
            AllItems.GRAY_VALVE_HANDLE,
            AllItems.LIGHT_GRAY_VALVE_HANDLE,
            AllItems.CYAN_VALVE_HANDLE,
            AllItems.PURPLE_VALVE_HANDLE,
            AllItems.BLUE_VALVE_HANDLE,
            AllItems.BROWN_VALVE_HANDLE,
            AllItems.GREEN_VALVE_HANDLE,
            AllItems.RED_VALVE_HANDLE,
            AllItems.BLACK_VALVE_HANDLE
        ).addStoryBoard("valve_handle", KineticsScenes::valveHandle);

        HELPER.addStoryBoard(
            AllItems.ENCASED_CHAIN_DRIVE,
            "chain_drive/relay",
            ChainDriveScenes::chainDriveAsRelay,
            AllCreatePonderTags.KINETIC_RELAYS
        );
        HELPER.forComponents(AllItems.ENCASED_CHAIN_DRIVE, AllItems.ADJUSTABLE_CHAIN_GEARSHIFT)
            .addStoryBoard("chain_drive/gearshift", ChainDriveScenes::adjustableChainGearshift);

        HELPER.forComponents(AllItems.ROTATION_SPEED_CONTROLLER).addStoryBoard("speed_controller", KineticsScenes::speedController);

        // Gauges
        HELPER.addStoryBoard(AllItems.SPEEDOMETER, "gauges", KineticsScenes::speedometer);
        HELPER.addStoryBoard(AllItems.STRESSOMETER, "gauges", KineticsScenes::stressometer);

        // Item Processing
        HELPER.addStoryBoard(AllItems.MILLSTONE, "millstone", ProcessingScenes::millstone);
        HELPER.addStoryBoard(AllItems.CRUSHING_WHEEL, "crushing_wheel", ProcessingScenes::crushingWheels);
        HELPER.addStoryBoard(AllItems.MECHANICAL_MIXER, "mechanical_mixer/mixing", ProcessingScenes::mixing);
        HELPER.forComponents(AllItems.MECHANICAL_PRESS).addStoryBoard("mechanical_press/pressing", ProcessingScenes::pressing)
            .addStoryBoard("mechanical_press/compacting", ProcessingScenes::compacting);
        HELPER.forComponents(AllItems.BASIN).addStoryBoard("basin", ProcessingScenes::basin)
            .addStoryBoard("mechanical_mixer/mixing", ProcessingScenes::mixing)
            .addStoryBoard("mechanical_press/compacting", ProcessingScenes::compacting);
        HELPER.addStoryBoard(AllItems.EMPTY_BLAZE_BURNER, "empty_blaze_burner", ProcessingScenes::emptyBlazeBurner);
        HELPER.addStoryBoard(AllItems.BLAZE_BURNER, "blaze_burner", ProcessingScenes::blazeBurner);
        HELPER.addStoryBoard(AllItems.DEPOT, "depot", BeltScenes::depot);
        HELPER.forComponents(AllItems.WEIGHTED_EJECTOR).addStoryBoard("weighted_ejector/eject", EjectorScenes::ejector)
            .addStoryBoard("weighted_ejector/split", EjectorScenes::splitY).addStoryBoard("weighted_ejector/redstone", EjectorScenes::redstone);

        // Crafters
        HELPER.forComponents(AllItems.MECHANICAL_CRAFTER).addStoryBoard("mechanical_crafter/setup", CrafterScenes::setup)
            .addStoryBoard("mechanical_crafter/connect", CrafterScenes::connect);
        HELPER.forComponents(AllItems.MECHANICAL_CRAFTER, AllItems.CRAFTER_SLOT_COVER)
            .addStoryBoard("mechanical_crafter/covers", CrafterScenes::covers);

        // Vaults
        HELPER.forComponents(AllItems.ITEM_VAULT).addStoryBoard("item_vault/storage", ItemVaultScenes::storage, AllCreatePonderTags.LOGISTICS)
            .addStoryBoard("item_vault/sizes", ItemVaultScenes::sizes);

        // Chutes
        HELPER.forComponents(AllItems.CHUTE).addStoryBoard("chute/downward", ChuteScenes::downward, AllCreatePonderTags.LOGISTICS)
            .addStoryBoard("chute/upward", ChuteScenes::upward);
        HELPER.forComponents(AllItems.CHUTE, AllItems.SMART_CHUTE).addStoryBoard("chute/smart", ChuteScenes::smart);

        // Funnels
        HELPER.addStoryBoard(AllItems.BRASS_FUNNEL, "funnels/brass", FunnelScenes::brass);
        HELPER.forComponents(AllItems.ANDESITE_FUNNEL, AllItems.BRASS_FUNNEL)
            .addStoryBoard("funnels/intro", FunnelScenes::intro, AllCreatePonderTags.LOGISTICS)
            .addStoryBoard("funnels/direction", FunnelScenes::directionality).addStoryBoard("funnels/compat", FunnelScenes::compat)
            .addStoryBoard("funnels/redstone", FunnelScenes::redstone).addStoryBoard("funnels/transposer", FunnelScenes::transposer);
        HELPER.addStoryBoard(AllItems.ANDESITE_FUNNEL, "funnels/brass", FunnelScenes::brass);

        // Tunnels
        HELPER.addStoryBoard(AllItems.ANDESITE_TUNNEL, "tunnels/andesite", TunnelScenes::andesite);
        HELPER.forComponents(AllItems.BRASS_TUNNEL).addStoryBoard("tunnels/brass", TunnelScenes::brass)
            .addStoryBoard("tunnels/brass_modes", TunnelScenes::brassModes);

        // Chassis & Super Glue
        HELPER.forComponents(AllItems.LINEAR_CHASSIS, AllItems.SECONDARY_LINEAR_CHASSIS)
            .addStoryBoard("chassis/linear_group", ChassisScenes::linearGroup, AllCreatePonderTags.CONTRAPTION_ASSEMBLY)
            .addStoryBoard("chassis/linear_attachment", ChassisScenes::linearAttachement);
        HELPER.forComponents(AllItems.RADIAL_CHASSIS)
            .addStoryBoard("chassis/radial", ChassisScenes::radial, AllCreatePonderTags.CONTRAPTION_ASSEMBLY);
        HELPER.forComponents(AllItems.SUPER_GLUE).addStoryBoard("super_glue", ChassisScenes::superGlue, AllCreatePonderTags.CONTRAPTION_ASSEMBLY);
        HELPER.forComponents(AllItems.STICKER).addStoryBoard("sticker", RedstoneScenes::sticker, AllCreatePonderTags.CONTRAPTION_ASSEMBLY);

        // Mechanical Arm
        HELPER.forComponents(AllItems.MECHANICAL_ARM).addStoryBoard("mechanical_arm/setup", ArmScenes::setup, AllCreatePonderTags.ARM_TARGETS)
            .addStoryBoard("mechanical_arm/filter", ArmScenes::filtering).addStoryBoard("mechanical_arm/modes", ArmScenes::modes)
            .addStoryBoard("mechanical_arm/redstone", ArmScenes::redstone);

        // Mechanical Piston
        HELPER.forComponents(AllItems.MECHANICAL_PISTON, AllItems.STICKY_MECHANICAL_PISTON).addStoryBoard(
            "mechanical_piston/anchor",
            PistonScenes::movement,
            AllCreatePonderTags.KINETIC_APPLIANCES,
            AllCreatePonderTags.MOVEMENT_ANCHOR
        );
        HELPER.forComponents(AllItems.MECHANICAL_PISTON, AllItems.STICKY_MECHANICAL_PISTON, AllItems.PISTON_EXTENSION_POLE)
            .addStoryBoard("mechanical_piston/piston_pole", PistonScenes::poles);
        HELPER.forComponents(AllItems.MECHANICAL_PISTON, AllItems.STICKY_MECHANICAL_PISTON)
            .addStoryBoard("mechanical_piston/modes", PistonScenes::movementModes);

        // Pulleys
        HELPER.forComponents(AllItems.ROPE_PULLEY)
            .addStoryBoard("rope_pulley/anchor", PulleyScenes::movement, AllCreatePonderTags.KINETIC_APPLIANCES, AllCreatePonderTags.MOVEMENT_ANCHOR)
            .addStoryBoard("rope_pulley/modes", PulleyScenes::movementModes).addStoryBoard("rope_pulley/multi_rope", PulleyScenes::multiRope)
            .addStoryBoard("rope_pulley/attachment", PulleyScenes::attachment);
        HELPER.forComponents(AllItems.ELEVATOR_PULLEY).addStoryBoard("elevator_pulley/elevator", ElevatorScenes::elevator)
            .addStoryBoard("elevator_pulley/multi_rope", ElevatorScenes::multiRope);

        // Windmill Bearing
        HELPER.forComponents(AllItems.WINDMILL_BEARING)
            .addStoryBoard("windmill_bearing/source", BearingScenes::windmillsAsSource, AllCreatePonderTags.KINETIC_SOURCES)
            .addStoryBoard("windmill_bearing/structure", BearingScenes::windmillsAnyStructure, AllCreatePonderTags.MOVEMENT_ANCHOR);
        HELPER.forComponents(AllItems.SAIL).addStoryBoard("sail", BearingScenes::sail);
        HELPER.forComponents(AllItems.SAIL_FRAME).addStoryBoard("sail", BearingScenes::sailFrame);

        // Mechanical Bearing
        HELPER.forComponents(AllItems.MECHANICAL_BEARING).addStoryBoard(
                "mechanical_bearing/anchor",
                BearingScenes::mechanicalBearing,
                AllCreatePonderTags.KINETIC_APPLIANCES,
                AllCreatePonderTags.MOVEMENT_ANCHOR
            ).addStoryBoard("mechanical_bearing/modes", BearingScenes::bearingModes)
            .addStoryBoard("mechanical_bearing/stabilized", BearingScenes::stabilizedBearings, AllCreatePonderTags.CONTRAPTION_ACTOR);

        // Clockwork Bearing
        HELPER.addStoryBoard(
            AllItems.CLOCKWORK_BEARING,
            "clockwork_bearing",
            BearingScenes::clockwork,
            AllCreatePonderTags.KINETIC_APPLIANCES,
            AllCreatePonderTags.MOVEMENT_ANCHOR
        );

        // Gantries
        HELPER.addStoryBoard(
            AllItems.GANTRY_SHAFT,
            "gantry/intro",
            GantryScenes::introForShaft,
            AllCreatePonderTags.KINETIC_APPLIANCES,
            AllCreatePonderTags.MOVEMENT_ANCHOR
        );
        HELPER.addStoryBoard(
            AllItems.GANTRY_CARRIAGE,
            "gantry/intro",
            GantryScenes::introForPinion,
            AllCreatePonderTags.KINETIC_APPLIANCES,
            AllCreatePonderTags.MOVEMENT_ANCHOR
        );
        HELPER.forComponents(AllItems.GANTRY_SHAFT, AllItems.GANTRY_CARRIAGE).addStoryBoard("gantry/redstone", GantryScenes::redstone)
            .addStoryBoard("gantry/direction", GantryScenes::direction).addStoryBoard("gantry/subgantry", GantryScenes::subgantry);

        // Cart Assembler
        HELPER.forComponents(AllItems.CART_ASSEMBLER)
            .addStoryBoard("cart_assembler/anchor", CartAssemblerScenes::anchor, AllCreatePonderTags.MOVEMENT_ANCHOR)
            .addStoryBoard("cart_assembler/modes", CartAssemblerScenes::modes).addStoryBoard("cart_assembler/dual", CartAssemblerScenes::dual)
            .addStoryBoard("cart_assembler/rails", CartAssemblerScenes::rails);

        // Movement Actors
        HELPER.forComponents(AllItems.PORTABLE_STORAGE_INTERFACE)
            .addStoryBoard("portable_interface/transfer", MovementActorScenes::psiTransfer, AllCreatePonderTags.CONTRAPTION_ACTOR)
            .addStoryBoard("portable_interface/redstone", MovementActorScenes::psiRedstone);
        HELPER.forComponents(AllItems.REDSTONE_CONTACT).addStoryBoard("redstone_contact", RedstoneScenes::contact);
        HELPER.forComponents(AllItems.MECHANICAL_SAW)
            .addStoryBoard("mechanical_saw/processing", MechanicalSawScenes::processing, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("mechanical_saw/breaker", MechanicalSawScenes::treeCutting)
            .addStoryBoard("mechanical_saw/contraption", MechanicalSawScenes::contraption, AllCreatePonderTags.CONTRAPTION_ACTOR);
        HELPER.forComponents(AllItems.MECHANICAL_DRILL)
            .addStoryBoard("mechanical_drill/breaker", MechanicalDrillScenes::breaker, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("mechanical_drill/contraption", MechanicalDrillScenes::contraption, AllCreatePonderTags.CONTRAPTION_ACTOR);
        HELPER.forComponents(AllItems.DEPLOYER).addStoryBoard("deployer/filter", DeployerScenes::filter, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("deployer/modes", DeployerScenes::modes).addStoryBoard("deployer/processing", DeployerScenes::processing)
            .addStoryBoard("deployer/redstone", DeployerScenes::redstone)
            .addStoryBoard("deployer/contraption", DeployerScenes::contraption, AllCreatePonderTags.CONTRAPTION_ACTOR);
        HELPER.forComponents(AllItems.MECHANICAL_HARVESTER).addStoryBoard("harvester", MovementActorScenes::harvester);
        HELPER.forComponents(AllItems.MECHANICAL_PLOUGH).addStoryBoard("plough", MovementActorScenes::plough);
        HELPER.forComponents(AllItems.CONTRAPTION_CONTROLS).addStoryBoard("contraption_controls", MovementActorScenes::contraptionControls);
        HELPER.forComponents(AllItems.MECHANICAL_ROLLER).addStoryBoard("mechanical_roller/clear_and_pave", RollerScenes::clearAndPave)
            .addStoryBoard("mechanical_roller/fill", RollerScenes::fill);

        // Fluids
        HELPER.forComponents(AllItems.FLUID_PIPE).addStoryBoard("fluid_pipe/flow", PipeScenes::flow, AllCreatePonderTags.FLUIDS)
            .addStoryBoard("fluid_pipe/interaction", PipeScenes::interaction).addStoryBoard("fluid_pipe/encasing", PipeScenes::encasing);
        HELPER.forComponents(AllItems.COPPER_CASING).addStoryBoard("fluid_pipe/encasing", PipeScenes::encasing);
        HELPER.forComponents(AllItems.MECHANICAL_PUMP)
            .addStoryBoard("mechanical_pump/flow", PumpScenes::flow, AllCreatePonderTags.FLUIDS, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("mechanical_pump/speed", PumpScenes::speed);
        HELPER.forComponents(AllItems.FLUID_VALVE)
            .addStoryBoard("fluid_valve", PipeScenes::valve, AllCreatePonderTags.FLUIDS, AllCreatePonderTags.KINETIC_APPLIANCES);
        HELPER.forComponents(AllItems.SMART_FLUID_PIPE).addStoryBoard("smart_pipe", PipeScenes::smart, AllCreatePonderTags.FLUIDS);
        HELPER.forComponents(AllItems.FLUID_TANK).addStoryBoard("fluid_tank/storage", FluidTankScenes::storage, AllCreatePonderTags.FLUIDS)
            .addStoryBoard("fluid_tank/sizes", FluidTankScenes::sizes);
        HELPER.forComponents(AllItems.CREATIVE_FLUID_TANK)
            .addStoryBoard("fluid_tank/storage_creative", FluidTankScenes::creative, AllCreatePonderTags.FLUIDS, AllCreatePonderTags.CREATIVE)
            .addStoryBoard("fluid_tank/sizes_creative", FluidTankScenes::sizes);
        HELPER.forComponents(AllItems.HOSE_PULLEY)
            .addStoryBoard("hose_pulley/intro", HosePulleyScenes::intro, AllCreatePonderTags.FLUIDS, AllCreatePonderTags.KINETIC_APPLIANCES)
            .addStoryBoard("hose_pulley/level", HosePulleyScenes::level).addStoryBoard("hose_pulley/infinite", HosePulleyScenes::infinite);
        HELPER.forComponents(AllItems.SPOUT).addStoryBoard("spout", SpoutScenes::filling, AllCreatePonderTags.FLUIDS);
        HELPER.forComponents(AllItems.ITEM_DRAIN).addStoryBoard("item_drain", DrainScenes::emptying, AllCreatePonderTags.FLUIDS);
        HELPER.forComponents(AllItems.PORTABLE_FLUID_INTERFACE).addStoryBoard(
            "portable_interface/transfer_fluid",
            FluidMovementActorScenes::transfer,
            AllCreatePonderTags.FLUIDS,
            AllCreatePonderTags.CONTRAPTION_ACTOR
        ).addStoryBoard("portable_interface/redstone_fluid", MovementActorScenes::psiRedstone);

        // Redstone
        HELPER.forComponents(AllItems.PULSE_EXTENDER).addStoryBoard("pulse_extender", RedstoneScenes::pulseExtender);
        HELPER.forComponents(AllItems.PULSE_REPEATER).addStoryBoard("pulse_repeater", RedstoneScenes::pulseRepeater);
        HELPER.forComponents(AllItems.POWERED_LATCH).addStoryBoard("powered_latch", RedstoneScenes::poweredLatch);
        HELPER.forComponents(AllItems.POWERED_TOGGLE_LATCH).addStoryBoard("powered_toggle_latch", RedstoneScenes::poweredToggleLatch);
        HELPER.forComponents(AllItems.ANALOG_LEVER).addStoryBoard("analog_lever", RedstoneScenes::analogLever);
        HELPER.forComponents(AllItems.ORANGE_NIXIE_TUBE).addStoryBoard("nixie_tube", RedstoneScenes::nixieTube);
        HELPER.forComponents(AllItems.REDSTONE_LINK).addStoryBoard("redstone_link", RedstoneScenes::redstoneLink);
        HELPER.forComponents(AllItems.ROSE_QUARTZ_LAMP).addStoryBoard("rose_quartz_lamp", RedstoneScenes2::roseQuartzLamp);
        HELPER.forComponents(AllItems.PULSE_TIMER).addStoryBoard("pulse_timer", RedstoneScenes2::pulseTimer);

        HELPER.forComponents(AllItems.SMART_OBSERVER).addStoryBoard("smart_observer", DetectorScenes::smartObserver);
        HELPER.forComponents(AllItems.THRESHOLD_SWITCH).addStoryBoard("threshold_switch", DetectorScenes::thresholdSwitch);

        // Hilo
        HELPER.forComponents(AllItems.CHAIN_CONVEYOR).addStoryBoard("high_logistics/chain_conveyor", FrogAndConveyorScenes::conveyor);
        HELPER.forComponents(AllItems.PACKAGE_FROGPORT).addStoryBoard("high_logistics/package_frogport", FrogAndConveyorScenes::frogPort);
        HELPER.forComponents(
            AllItems.WHITE_POSTBOX,
            AllItems.ORANGE_POSTBOX,
            AllItems.MAGENTA_POSTBOX,
            AllItems.LIGHT_BLUE_POSTBOX,
            AllItems.YELLOW_POSTBOX,
            AllItems.LIME_POSTBOX,
            AllItems.PINK_POSTBOX,
            AllItems.GRAY_POSTBOX,
            AllItems.LIGHT_GRAY_POSTBOX,
            AllItems.CYAN_POSTBOX,
            AllItems.PURPLE_POSTBOX,
            AllItems.BLUE_POSTBOX,
            AllItems.BROWN_POSTBOX,
            AllItems.GREEN_POSTBOX,
            AllItems.RED_POSTBOX,
            AllItems.BLACK_POSTBOX
        ).addStoryBoard("high_logistics/package_postbox", PostboxScenes::postbox);
        HELPER.forComponents(AllItems.PACKAGER).addStoryBoard("high_logistics/packager", PackagerScenes::packager)
            .addStoryBoard("high_logistics/packager_address", PackagerScenes::packagerAddress);
        HELPER.forComponents(AllItems.STOCK_LINK).addStoryBoard("high_logistics/stock_link", StockLinkScenes::stockLink);
        HELPER.forComponents(AllItems.STOCK_TICKER).addStoryBoard("high_logistics/stock_ticker", StockTickerScenes::stockTicker)
            .addStoryBoard("high_logistics/stock_ticker_address", StockTickerScenes::stockTickerAddress);
        HELPER.forComponents(AllItems.REDSTONE_REQUESTER).addStoryBoard("high_logistics/redstone_requester", RequesterAndShopScenes::requester);
        HELPER.forComponents(AllItems.REPACKAGER).addStoryBoard("high_logistics/repackager", RepackagerScenes::repackager);
        HELPER.forComponents(
            AllItems.WHITE_TABLE_CLOTH,
            AllItems.ORANGE_TABLE_CLOTH,
            AllItems.MAGENTA_TABLE_CLOTH,
            AllItems.LIGHT_BLUE_TABLE_CLOTH,
            AllItems.YELLOW_TABLE_CLOTH,
            AllItems.LIME_TABLE_CLOTH,
            AllItems.PINK_TABLE_CLOTH,
            AllItems.GRAY_TABLE_CLOTH,
            AllItems.LIGHT_GRAY_TABLE_CLOTH,
            AllItems.CYAN_TABLE_CLOTH,
            AllItems.PURPLE_TABLE_CLOTH,
            AllItems.BLUE_TABLE_CLOTH,
            AllItems.BROWN_TABLE_CLOTH,
            AllItems.GREEN_TABLE_CLOTH,
            AllItems.RED_TABLE_CLOTH,
            AllItems.BLACK_TABLE_CLOTH,
            AllItems.ANDESITE_TABLE_CLOTH,
            AllItems.BRASS_TABLE_CLOTH,
            AllItems.COPPER_TABLE_CLOTH
        ).addStoryBoard("high_logistics/table_cloth", TableClothScenes::tableCloth);
        HELPER.forComponents(AllItems.FACTORY_GAUGE).addStoryBoard("high_logistics/factory_gauge_restocker", FactoryGaugeScenes::restocker)
            .addStoryBoard("high_logistics/factory_gauge_recipe", FactoryGaugeScenes::recipe)
            .addStoryBoard("high_logistics/factory_gauge_crafting", FactoryGaugeScenes::crafting)
            .addStoryBoard("high_logistics/factory_gauge_links", FactoryGaugeScenes::links);

        // Trains
        HELPER.forComponents(Arrays.stream(TrackMaterial.allBlocks()).map(Block::asItem).toArray(Item[]::new))
            .addStoryBoard("train_track/placement", TrackScenes::placement).addStoryBoard("train_track/portal", TrackScenes::portal)
            .addStoryBoard("train_track/chunks", TrackScenes::chunks);

        HELPER.forComponents(AllItems.TRACK_STATION).addStoryBoard("train_station/assembly", TrainStationScenes::assembly)
            .addStoryBoard("train_station/schedule", TrainStationScenes::autoSchedule);

        HELPER.forComponents(AllItems.TRACK_SIGNAL).addStoryBoard("train_signal/placement", TrainSignalScenes::placement)
            .addStoryBoard("train_signal/signaling", TrainSignalScenes::signaling)
            .addStoryBoard("train_signal/redstone", TrainSignalScenes::redstone);

        HELPER.forComponents(AllItems.SCHEDULE).addStoryBoard("train_schedule", TrainScenes::schedule);

        HELPER.forComponents(AllItems.TRAIN_CONTROLS).addStoryBoard("train_controls", TrainScenes::controls);

        HELPER.forComponents(AllItems.TRACK_OBSERVER).addStoryBoard("train_observer", TrackObserverScenes::observe);

        // Display Link
        HELPER.forComponents(AllItems.DISPLAY_LINK).addStoryBoard("display_link", DisplayScenes::link)
            .addStoryBoard("display_link_redstone", DisplayScenes::redstone);
        HELPER.forComponents(AllItems.DISPLAY_BOARD).addStoryBoard("display_board", DisplayScenes::board);

        // Steam
        HELPER.forComponents(AllItems.STEAM_WHISTLE).addStoryBoard("steam_whistle", SteamScenes::whistle);
        HELPER.forComponents(AllItems.STEAM_ENGINE).addStoryBoard("steam_engine", SteamScenes::engine);

    }
}