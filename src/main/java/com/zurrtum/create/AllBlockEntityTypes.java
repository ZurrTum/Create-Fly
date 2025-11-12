package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import com.zurrtum.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.zurrtum.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.ClockworkBearingBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.zurrtum.create.content.contraptions.chassis.ChassisBlockEntity;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlockEntity;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.content.decoration.placard.PlacardBlockEntity;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import com.zurrtum.create.content.equipment.bell.HauntedBellBlockEntity;
import com.zurrtum.create.content.equipment.bell.PeculiarBellBlockEntity;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainDrive.ChainGearshiftBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelBlockEntity;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.drill.DrillBlockEntity;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.ClutchBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.GearshiftBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import com.zurrtum.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.logistics.itemHatch.ItemHatchBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.zurrtum.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlockEntity;
import com.zurrtum.create.content.redstone.diodes.PulseExtenderBlockEntity;
import com.zurrtum.create.content.redstone.diodes.PulseRepeaterBlockEntity;
import com.zurrtum.create.content.redstone.diodes.PulseTimerBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.zurrtum.create.content.schematics.table.SchematicTableBlockEntity;
import com.zurrtum.create.content.trains.bogey.StandardBogeyBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.FakeTrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackMaterial;

import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBlockEntityTypes {
    public static final BlockEntityType<BracketedKineticBlockEntity> BRACKETED_KINETIC = register(
        "bracketed_kinetic",
        BracketedKineticBlockEntity::new,
        AllBlocks.SHAFT,
        AllBlocks.COGWHEEL,
        AllBlocks.LARGE_COGWHEEL
    );
    public static final BlockEntityType<CreativeMotorBlockEntity> MOTOR = register("motor", CreativeMotorBlockEntity::new, AllBlocks.CREATIVE_MOTOR);
    public static final BlockEntityType<SequencedGearshiftBlockEntity> SEQUENCED_GEARSHIFT = register(
        "sequenced_gearshift",
        SequencedGearshiftBlockEntity::new,
        AllBlocks.SEQUENCED_GEARSHIFT
    );
    public static final BlockEntityType<PoweredShaftBlockEntity> POWERED_SHAFT = register(
        "powered_shaft",
        PoweredShaftBlockEntity::new,
        AllBlocks.POWERED_SHAFT
    );
    public static final BlockEntityType<GantryShaftBlockEntity> GANTRY_SHAFT = register(
        "gantry_shaft",
        GantryShaftBlockEntity::new,
        AllBlocks.GANTRY_SHAFT
    );
    public static final BlockEntityType<SteamEngineBlockEntity> STEAM_ENGINE = register(
        "steam_engine",
        SteamEngineBlockEntity::new,
        AllBlocks.STEAM_ENGINE
    );
    public static final BlockEntityType<GantryCarriageBlockEntity> GANTRY_PINION = register(
        "gantry_pinion",
        GantryCarriageBlockEntity::new,
        AllBlocks.GANTRY_CARRIAGE
    );
    public static final BlockEntityType<SpeedControllerBlockEntity> ROTATION_SPEED_CONTROLLER = register(
        "rotation_speed_controller",
        SpeedControllerBlockEntity::new,
        AllBlocks.ROTATION_SPEED_CONTROLLER
    );
    public static final BlockEntityType<GearboxBlockEntity> GEARBOX = register("gearbox", GearboxBlockEntity::new, AllBlocks.GEARBOX);
    public static final BlockEntityType<WaterWheelBlockEntity> WATER_WHEEL = register(
        "water_wheel",
        WaterWheelBlockEntity::new,
        AllBlocks.WATER_WHEEL
    );
    public static final BlockEntityType<LargeWaterWheelBlockEntity> LARGE_WATER_WHEEL = register(
        "large_water_wheel",
        LargeWaterWheelBlockEntity::new,
        AllBlocks.LARGE_WATER_WHEEL
    );
    public static final BlockEntityType<ItemVaultBlockEntity> ITEM_VAULT = register("item_vault", ItemVaultBlockEntity::new, AllBlocks.ITEM_VAULT);
    public static final BlockEntityType<ArmBlockEntity> MECHANICAL_ARM = register("mechanical_arm", ArmBlockEntity::new, AllBlocks.MECHANICAL_ARM);
    public static final BlockEntityType<DepotBlockEntity> DEPOT = register("depot", DepotBlockEntity::new, AllBlocks.DEPOT);
    public static final BlockEntityType<BeltBlockEntity> BELT = register("belt", BeltBlockEntity::new, AllBlocks.BELT);
    public static final BlockEntityType<ClutchBlockEntity> CLUTCH = register("clutch", ClutchBlockEntity::new, AllBlocks.CLUTCH);
    public static final BlockEntityType<GearshiftBlockEntity> GEARSHIFT = register("gearshift", GearshiftBlockEntity::new, AllBlocks.GEARSHIFT);
    public static final BlockEntityType<KineticBlockEntity> ENCASED_SHAFT = register(
        "encased_shaft",
        KineticBlockEntity::encased,
        AllBlocks.ANDESITE_ENCASED_SHAFT,
        AllBlocks.BRASS_ENCASED_SHAFT,
        AllBlocks.ENCASED_CHAIN_DRIVE,
        AllBlocks.METAL_GIRDER_ENCASED_SHAFT
    );
    public static final BlockEntityType<ChainGearshiftBlockEntity> ADJUSTABLE_CHAIN_GEARSHIFT = register(
        "adjustable_chain_gearshift",
        ChainGearshiftBlockEntity::new,
        AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT
    );
    public static final BlockEntityType<ChainConveyorBlockEntity> CHAIN_CONVEYOR = register(
        "chain_conveyor",
        ChainConveyorBlockEntity::new,
        AllBlocks.CHAIN_CONVEYOR
    );
    public static final BlockEntityType<SimpleKineticBlockEntity> ENCASED_COGWHEEL = register(
        "encased_cogwheel",
        SimpleKineticBlockEntity::small,
        AllBlocks.ANDESITE_ENCASED_COGWHEEL,
        AllBlocks.BRASS_ENCASED_COGWHEEL
    );
    public static final BlockEntityType<SimpleKineticBlockEntity> ENCASED_LARGE_COGWHEEL = register(
        "encased_large_cogwheel",
        SimpleKineticBlockEntity::large,
        AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL,
        AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL
    );
    public static final BlockEntityType<HandCrankBlockEntity> HAND_CRANK = register("hand_crank", HandCrankBlockEntity::new, AllBlocks.HAND_CRANK);
    public static final BlockEntityType<ValveHandleBlockEntity> VALVE_HANDLE = register(
        "valve_handle",
        ValveHandleBlockEntity::new,
        AllBlocks.COPPER_VALVE_HANDLE,
        AllBlocks.WHITE_VALVE_HANDLE,
        AllBlocks.ORANGE_VALVE_HANDLE,
        AllBlocks.MAGENTA_VALVE_HANDLE,
        AllBlocks.LIGHT_BLUE_VALVE_HANDLE,
        AllBlocks.YELLOW_VALVE_HANDLE,
        AllBlocks.LIME_VALVE_HANDLE,
        AllBlocks.PINK_VALVE_HANDLE,
        AllBlocks.GRAY_VALVE_HANDLE,
        AllBlocks.LIGHT_GRAY_VALVE_HANDLE,
        AllBlocks.CYAN_VALVE_HANDLE,
        AllBlocks.PURPLE_VALVE_HANDLE,
        AllBlocks.BLUE_VALVE_HANDLE,
        AllBlocks.BROWN_VALVE_HANDLE,
        AllBlocks.GREEN_VALVE_HANDLE,
        AllBlocks.RED_VALVE_HANDLE,
        AllBlocks.BLACK_VALVE_HANDLE
    );
    public static final BlockEntityType<ChassisBlockEntity> CHASSIS = register(
        "chassis",
        ChassisBlockEntity::new,
        AllBlocks.RADIAL_CHASSIS,
        AllBlocks.LINEAR_CHASSIS,
        AllBlocks.SECONDARY_LINEAR_CHASSIS
    );
    public static final BlockEntityType<WindmillBearingBlockEntity> WINDMILL_BEARING = register(
        "windmill_bearing",
        WindmillBearingBlockEntity::new,
        AllBlocks.WINDMILL_BEARING
    );
    public static final BlockEntityType<MechanicalBearingBlockEntity> MECHANICAL_BEARING = register(
        "mechanical_bearing",
        MechanicalBearingBlockEntity::new,
        AllBlocks.MECHANICAL_BEARING
    );
    public static final BlockEntityType<MechanicalPistonBlockEntity> MECHANICAL_PISTON = register(
        "mechanical_piston",
        MechanicalPistonBlockEntity::new,
        AllBlocks.MECHANICAL_PISTON,
        AllBlocks.STICKY_MECHANICAL_PISTON
    );
    public static final BlockEntityType<PumpBlockEntity> MECHANICAL_PUMP = register(
        "mechanical_pump",
        PumpBlockEntity::new,
        AllBlocks.MECHANICAL_PUMP
    );
    public static final BlockEntityType<FluidPipeBlockEntity> FLUID_PIPE = register("fluid_pipe", FluidPipeBlockEntity::pipe, AllBlocks.FLUID_PIPE);
    public static final BlockEntityType<FluidPipeBlockEntity> ENCASED_FLUID_PIPE = register(
        "encased_fluid_pipe",
        FluidPipeBlockEntity::encased,
        AllBlocks.ENCASED_FLUID_PIPE
    );
    public static final BlockEntityType<StraightPipeBlockEntity> GLASS_FLUID_PIPE = register(
        "glass_fluid_pipe",
        StraightPipeBlockEntity::new,
        AllBlocks.GLASS_FLUID_PIPE
    );
    public static final BlockEntityType<BlazeBurnerBlockEntity> HEATER = register(
        "blaze_heater",
        BlazeBurnerBlockEntity::new,
        AllBlocks.BLAZE_BURNER
    );
    public static final BlockEntityType<FluidTankBlockEntity> FLUID_TANK = register("fluid_tank", FluidTankBlockEntity::tank, AllBlocks.FLUID_TANK);
    public static final BlockEntityType<CreativeFluidTankBlockEntity> CREATIVE_FLUID_TANK = register(
        "creative_fluid_tank",
        CreativeFluidTankBlockEntity::new,
        AllBlocks.CREATIVE_FLUID_TANK
    );
    public static final BlockEntityType<MechanicalPressBlockEntity> MECHANICAL_PRESS = register(
        "mechanical_press",
        MechanicalPressBlockEntity::new,
        AllBlocks.MECHANICAL_PRESS
    );
    public static final BlockEntityType<EjectorBlockEntity> WEIGHTED_EJECTOR = register(
        "weighted_ejector",
        EjectorBlockEntity::new,
        AllBlocks.WEIGHTED_EJECTOR
    );
    public static final BlockEntityType<PulleyBlockEntity> ROPE_PULLEY = register("rope_pulley", PulleyBlockEntity::new, AllBlocks.ROPE_PULLEY);
    public static final BlockEntityType<MillstoneBlockEntity> MILLSTONE = register("millstone", MillstoneBlockEntity::new, AllBlocks.MILLSTONE);
    public static final BlockEntityType<EncasedFanBlockEntity> ENCASED_FAN = register(
        "encased_fan",
        EncasedFanBlockEntity::new,
        AllBlocks.ENCASED_FAN
    );
    public static final BlockEntityType<PeculiarBellBlockEntity> PECULIAR_BELL = register(
        "peculiar_bell",
        PeculiarBellBlockEntity::new,
        AllBlocks.PECULIAR_BELL
    );
    public static final BlockEntityType<HauntedBellBlockEntity> HAUNTED_BELL = register(
        "cursed_bell",
        HauntedBellBlockEntity::new,
        AllBlocks.HAUNTED_BELL
    );
    public static final BlockEntityType<SawBlockEntity> SAW = register("saw", SawBlockEntity::new, AllBlocks.MECHANICAL_SAW);
    public static final BlockEntityType<BasinBlockEntity> BASIN = register("basin", BasinBlockEntity::new, AllBlocks.BASIN);
    public static final BlockEntityType<FunnelBlockEntity> FUNNEL = register(
        "funnel",
        FunnelBlockEntity::new,
        AllBlocks.ANDESITE_FUNNEL,
        AllBlocks.ANDESITE_BELT_FUNNEL,
        AllBlocks.BRASS_FUNNEL,
        AllBlocks.BRASS_BELT_FUNNEL
    );
    public static final BlockEntityType<BeltTunnelBlockEntity> ANDESITE_TUNNEL = register(
        "andesite_tunnel",
        BeltTunnelBlockEntity::andesite,
        AllBlocks.ANDESITE_TUNNEL
    );
    public static final BlockEntityType<BrassTunnelBlockEntity> BRASS_TUNNEL = register(
        "brass_tunnel",
        BrassTunnelBlockEntity::new,
        AllBlocks.BRASS_TUNNEL
    );
    public static final BlockEntityType<ChuteBlockEntity> CHUTE = register("chute", ChuteBlockEntity::new, AllBlocks.CHUTE);
    public static final BlockEntityType<SmartChuteBlockEntity> SMART_CHUTE = register(
        "smart_chute",
        SmartChuteBlockEntity::new,
        AllBlocks.SMART_CHUTE
    );
    public static final BlockEntityType<CartAssemblerBlockEntity> CART_ASSEMBLER = register(
        "cart_assembler",
        CartAssemblerBlockEntity::new,
        AllBlocks.CART_ASSEMBLER
    );
    public static final BlockEntityType<HarvesterBlockEntity> HARVESTER = register(
        "harvester",
        HarvesterBlockEntity::new,
        AllBlocks.MECHANICAL_HARVESTER
    );
    public static final BlockEntityType<PortableFluidInterfaceBlockEntity> PORTABLE_FLUID_INTERFACE = register(
        "portable_fluid_interface",
        PortableFluidInterfaceBlockEntity::new,
        AllBlocks.PORTABLE_FLUID_INTERFACE
    );
    public static final BlockEntityType<PortableItemInterfaceBlockEntity> PORTABLE_STORAGE_INTERFACE = register(
        "portable_storage_interface",
        PortableItemInterfaceBlockEntity::new,
        AllBlocks.PORTABLE_STORAGE_INTERFACE
    );
    public static final BlockEntityType<SpeedGaugeBlockEntity> SPEEDOMETER = register(
        "speedometer",
        SpeedGaugeBlockEntity::new,
        AllBlocks.SPEEDOMETER
    );
    public static final BlockEntityType<StressGaugeBlockEntity> STRESSOMETER = register(
        "stressometer",
        StressGaugeBlockEntity::new,
        AllBlocks.STRESSOMETER
    );
    public static final BlockEntityType<CuckooClockBlockEntity> CUCKOO_CLOCK = register(
        "cuckoo_clock",
        CuckooClockBlockEntity::new,
        AllBlocks.CUCKOO_CLOCK,
        AllBlocks.MYSTERIOUS_CUCKOO_CLOCK
    );
    public static final BlockEntityType<MechanicalMixerBlockEntity> MECHANICAL_MIXER = register(
        "mechanical_mixer",
        MechanicalMixerBlockEntity::new,
        AllBlocks.MECHANICAL_MIXER
    );
    public static final BlockEntityType<HosePulleyBlockEntity> HOSE_PULLEY = register(
        "hose_pulley",
        HosePulleyBlockEntity::new,
        AllBlocks.HOSE_PULLEY
    );
    public static final BlockEntityType<SpoutBlockEntity> SPOUT = register("spout", SpoutBlockEntity::new, AllBlocks.SPOUT);
    public static final BlockEntityType<ItemDrainBlockEntity> ITEM_DRAIN = register("item_drain", ItemDrainBlockEntity::new, AllBlocks.ITEM_DRAIN);
    public static final BlockEntityType<WhistleBlockEntity> STEAM_WHISTLE = register(
        "steam_whistle",
        WhistleBlockEntity::new,
        AllBlocks.STEAM_WHISTLE
    );
    public static final BlockEntityType<BacktankBlockEntity> BACKTANK = register(
        "backtank",
        BacktankBlockEntity::new,
        AllBlocks.COPPER_BACKTANK,
        AllBlocks.NETHERITE_BACKTANK
    );
    public static final BlockEntityType<DeployerBlockEntity> DEPLOYER = register("deployer", DeployerBlockEntity::new, AllBlocks.DEPLOYER);
    public static final BlockEntityType<TurntableBlockEntity> TURNTABLE = register("turntable", TurntableBlockEntity::new, AllBlocks.TURNTABLE);
    public static final BlockEntityType<DrillBlockEntity> DRILL = register("drill", DrillBlockEntity::new, AllBlocks.MECHANICAL_DRILL);
    public static final BlockEntityType<ClockworkBearingBlockEntity> CLOCKWORK_BEARING = register(
        "clockwork_bearing",
        ClockworkBearingBlockEntity::new,
        AllBlocks.CLOCKWORK_BEARING
    );
    public static final BlockEntityType<CrushingWheelBlockEntity> CRUSHING_WHEEL = register(
        "crushing_wheel",
        CrushingWheelBlockEntity::new,
        AllBlocks.CRUSHING_WHEEL
    );
    public static final BlockEntityType<CrushingWheelControllerBlockEntity> CRUSHING_WHEEL_CONTROLLER = register(
        "crushing_wheel_controller",
        CrushingWheelControllerBlockEntity::new,
        AllBlocks.CRUSHING_WHEEL_CONTROLLER
    );
    public static final BlockEntityType<FlapDisplayBlockEntity> FLAP_DISPLAY = register(
        "flap_display",
        FlapDisplayBlockEntity::new,
        AllBlocks.DISPLAY_BOARD
    );
    public static final BlockEntityType<ClipboardBlockEntity> CLIPBOARD = register("clipboard", ClipboardBlockEntity::new, AllBlocks.CLIPBOARD);
    public static final BlockEntityType<DisplayLinkBlockEntity> DISPLAY_LINK = register(
        "display_link",
        DisplayLinkBlockEntity::new,
        AllBlocks.DISPLAY_LINK
    );
    public static final BlockEntityType<NixieTubeBlockEntity> NIXIE_TUBE = register(
        "nixie_tube",
        NixieTubeBlockEntity::new,
        AllBlocks.ORANGE_NIXIE_TUBE,
        AllBlocks.WHITE_NIXIE_TUBE,
        AllBlocks.MAGENTA_NIXIE_TUBE,
        AllBlocks.LIGHT_BLUE_NIXIE_TUBE,
        AllBlocks.YELLOW_NIXIE_TUBE,
        AllBlocks.LIME_NIXIE_TUBE,
        AllBlocks.PINK_NIXIE_TUBE,
        AllBlocks.GRAY_NIXIE_TUBE,
        AllBlocks.LIGHT_GRAY_NIXIE_TUBE,
        AllBlocks.CYAN_NIXIE_TUBE,
        AllBlocks.PURPLE_NIXIE_TUBE,
        AllBlocks.BLUE_NIXIE_TUBE,
        AllBlocks.BROWN_NIXIE_TUBE,
        AllBlocks.GREEN_NIXIE_TUBE,
        AllBlocks.RED_NIXIE_TUBE,
        AllBlocks.BLACK_NIXIE_TUBE
    );
    public static final BlockEntityType<FluidValveBlockEntity> FLUID_VALVE = register(
        "fluid_valve",
        FluidValveBlockEntity::new,
        AllBlocks.FLUID_VALVE
    );
    public static final BlockEntityType<SmartFluidPipeBlockEntity> SMART_FLUID_PIPE = register(
        "smart_fluid_pipe",
        SmartFluidPipeBlockEntity::new,
        AllBlocks.SMART_FLUID_PIPE
    );
    public static final BlockEntityType<AnalogLeverBlockEntity> ANALOG_LEVER = register(
        "analog_lever",
        AnalogLeverBlockEntity::new,
        AllBlocks.ANALOG_LEVER
    );
    public static final BlockEntityType<RedstoneLinkBlockEntity> REDSTONE_LINK = register(
        "redstone_link",
        RedstoneLinkBlockEntity::new,
        AllBlocks.REDSTONE_LINK
    );
    public static final BlockEntityType<PulseRepeaterBlockEntity> PULSE_REPEATER = register(
        "pulse_repeater",
        PulseRepeaterBlockEntity::new,
        AllBlocks.PULSE_REPEATER
    );
    public static final BlockEntityType<PulseExtenderBlockEntity> PULSE_EXTENDER = register(
        "pulse_extender",
        PulseExtenderBlockEntity::new,
        AllBlocks.PULSE_EXTENDER
    );
    public static final BlockEntityType<PulseTimerBlockEntity> PULSE_TIMER = register(
        "pulse_timer",
        PulseTimerBlockEntity::new,
        AllBlocks.PULSE_TIMER
    );
    public static final BlockEntityType<SmartObserverBlockEntity> SMART_OBSERVER = register(
        "content_observer",
        SmartObserverBlockEntity::new,
        AllBlocks.SMART_OBSERVER
    );
    public static final BlockEntityType<ThresholdSwitchBlockEntity> THRESHOLD_SWITCH = register(
        "stockpile_switch",
        ThresholdSwitchBlockEntity::new,
        AllBlocks.THRESHOLD_SWITCH
    );
    public static final BlockEntityType<StickerBlockEntity> STICKER = register("sticker", StickerBlockEntity::new, AllBlocks.STICKER);
    public static final BlockEntityType<ContraptionControlsBlockEntity> CONTRAPTION_CONTROLS = register(
        "contraption_controls",
        ContraptionControlsBlockEntity::new,
        AllBlocks.CONTRAPTION_CONTROLS
    );
    public static final BlockEntityType<ElevatorPulleyBlockEntity> ELEVATOR_PULLEY = register(
        "elevator_pulley",
        ElevatorPulleyBlockEntity::new,
        AllBlocks.ELEVATOR_PULLEY
    );
    public static final BlockEntityType<ElevatorContactBlockEntity> ELEVATOR_CONTACT = register(
        "elevator_contact",
        ElevatorContactBlockEntity::new,
        AllBlocks.ELEVATOR_CONTACT
    );
    public static final BlockEntityType<SlidingDoorBlockEntity> SLIDING_DOOR = register(
        "sliding_door",
        SlidingDoorBlockEntity::new,
        AllBlocks.ANDESITE_DOOR,
        AllBlocks.BRASS_DOOR,
        AllBlocks.COPPER_DOOR,
        AllBlocks.TRAIN_DOOR,
        AllBlocks.FRAMED_GLASS_DOOR
    );
    public static final BlockEntityType<NozzleBlockEntity> NOZZLE = register("nozzle", NozzleBlockEntity::new, AllBlocks.NOZZLE);
    public static final BlockEntityType<DeskBellBlockEntity> DESK_BELL = register("desk_bell", DeskBellBlockEntity::new, AllBlocks.DESK_BELL);
    public static final BlockEntityType<MechanicalCrafterBlockEntity> MECHANICAL_CRAFTER = register(
        "mechanical_crafter",
        MechanicalCrafterBlockEntity::new,
        AllBlocks.MECHANICAL_CRAFTER
    );
    public static final BlockEntityType<CreativeCrateBlockEntity> CREATIVE_CRATE = register(
        "creative_crate",
        CreativeCrateBlockEntity::new,
        AllBlocks.CREATIVE_CRATE
    );
    public static final BlockEntityType<TrackBlockEntity> TRACK = register("track", TrackBlockEntity::new, TrackMaterial.allBlocks());
    public static final BlockEntityType<FakeTrackBlockEntity> FAKE_TRACK = register("fake_track", FakeTrackBlockEntity::new, AllBlocks.FAKE_TRACK);
    public static final BlockEntityType<StandardBogeyBlockEntity> BOGEY = register(
        "bogey",
        StandardBogeyBlockEntity::new,
        AllBlocks.SMALL_BOGEY,
        AllBlocks.LARGE_BOGEY
    );
    public static final BlockEntityType<SignalBlockEntity> TRACK_SIGNAL = register("track_signal", SignalBlockEntity::new, AllBlocks.TRACK_SIGNAL);
    public static final BlockEntityType<StationBlockEntity> TRACK_STATION = register(
        "track_station",
        StationBlockEntity::new,
        AllBlocks.TRACK_STATION
    );
    public static final BlockEntityType<TrackObserverBlockEntity> TRACK_OBSERVER = register(
        "track_observer",
        TrackObserverBlockEntity::new,
        AllBlocks.TRACK_OBSERVER
    );
    public static final BlockEntityType<RollerBlockEntity> MECHANICAL_ROLLER = register(
        "mechanical_roller",
        RollerBlockEntity::new,
        AllBlocks.MECHANICAL_ROLLER
    );
    public static final BlockEntityType<LecternControllerBlockEntity> LECTERN_CONTROLLER = register(
        "lectern_controller",
        LecternControllerBlockEntity::new,
        AllBlocks.LECTERN_CONTROLLER
    );
    public static final BlockEntityType<PackagerBlockEntity> PACKAGER = register("packager", PackagerBlockEntity::new, AllBlocks.PACKAGER);
    public static final BlockEntityType<PackagerLinkBlockEntity> PACKAGER_LINK = register(
        "packager_link",
        PackagerLinkBlockEntity::new,
        AllBlocks.STOCK_LINK
    );
    public static final BlockEntityType<RedstoneRequesterBlockEntity> REDSTONE_REQUESTER = register(
        "redstone_requester",
        RedstoneRequesterBlockEntity::new,
        AllBlocks.REDSTONE_REQUESTER
    );
    public static final BlockEntityType<RepackagerBlockEntity> REPACKAGER = register("repackager", RepackagerBlockEntity::new, AllBlocks.REPACKAGER);
    public static final BlockEntityType<StockTickerBlockEntity> STOCK_TICKER = register(
        "stock_ticker",
        StockTickerBlockEntity::new,
        AllBlocks.STOCK_TICKER
    );
    public static final BlockEntityType<TableClothBlockEntity> TABLE_CLOTH = register(
        "table_cloth",
        TableClothBlockEntity::new,
        AllBlocks.WHITE_TABLE_CLOTH,
        AllBlocks.ORANGE_TABLE_CLOTH,
        AllBlocks.MAGENTA_TABLE_CLOTH,
        AllBlocks.LIGHT_BLUE_TABLE_CLOTH,
        AllBlocks.YELLOW_TABLE_CLOTH,
        AllBlocks.LIME_TABLE_CLOTH,
        AllBlocks.PINK_TABLE_CLOTH,
        AllBlocks.GRAY_TABLE_CLOTH,
        AllBlocks.LIGHT_GRAY_TABLE_CLOTH,
        AllBlocks.CYAN_TABLE_CLOTH,
        AllBlocks.PURPLE_TABLE_CLOTH,
        AllBlocks.BLUE_TABLE_CLOTH,
        AllBlocks.BROWN_TABLE_CLOTH,
        AllBlocks.GREEN_TABLE_CLOTH,
        AllBlocks.RED_TABLE_CLOTH,
        AllBlocks.BLACK_TABLE_CLOTH,
        AllBlocks.ANDESITE_TABLE_CLOTH,
        AllBlocks.BRASS_TABLE_CLOTH,
        AllBlocks.COPPER_TABLE_CLOTH
    );
    public static final BlockEntityType<PostboxBlockEntity> PACKAGE_POSTBOX = register(
        "package_postbox",
        PostboxBlockEntity::new,
        AllBlocks.WHITE_POSTBOX,
        AllBlocks.ORANGE_POSTBOX,
        AllBlocks.MAGENTA_POSTBOX,
        AllBlocks.LIGHT_BLUE_POSTBOX,
        AllBlocks.YELLOW_POSTBOX,
        AllBlocks.LIME_POSTBOX,
        AllBlocks.PINK_POSTBOX,
        AllBlocks.GRAY_POSTBOX,
        AllBlocks.LIGHT_GRAY_POSTBOX,
        AllBlocks.CYAN_POSTBOX,
        AllBlocks.PURPLE_POSTBOX,
        AllBlocks.BLUE_POSTBOX,
        AllBlocks.BROWN_POSTBOX,
        AllBlocks.GREEN_POSTBOX,
        AllBlocks.RED_POSTBOX,
        AllBlocks.BLACK_POSTBOX
    );
    public static final BlockEntityType<FrogportBlockEntity> PACKAGE_FROGPORT = register(
        "package_frogport",
        FrogportBlockEntity::new,
        AllBlocks.PACKAGE_FROGPORT
    );
    public static final BlockEntityType<FactoryPanelBlockEntity> FACTORY_PANEL = register(
        "factory_panel",
        FactoryPanelBlockEntity::new,
        AllBlocks.FACTORY_GAUGE
    );
    public static final BlockEntityType<FlywheelBlockEntity> FLYWHEEL = register("flywheel", FlywheelBlockEntity::new, AllBlocks.FLYWHEEL);
    public static final BlockEntityType<ItemHatchBlockEntity> ITEM_HATCH = register("item_hatch", ItemHatchBlockEntity::new, AllBlocks.ITEM_HATCH);
    public static final BlockEntityType<PlacardBlockEntity> PLACARD = register("placard", PlacardBlockEntity::new, AllBlocks.PLACARD);
    public static final BlockEntityType<ToolboxBlockEntity> TOOLBOX = register(
        "toolbox",
        ToolboxBlockEntity::new,
        AllBlocks.WHITE_TOOLBOX,
        AllBlocks.ORANGE_TOOLBOX,
        AllBlocks.MAGENTA_TOOLBOX,
        AllBlocks.LIGHT_BLUE_TOOLBOX,
        AllBlocks.YELLOW_TOOLBOX,
        AllBlocks.LIME_TOOLBOX,
        AllBlocks.PINK_TOOLBOX,
        AllBlocks.GRAY_TOOLBOX,
        AllBlocks.LIGHT_GRAY_TOOLBOX,
        AllBlocks.CYAN_TOOLBOX,
        AllBlocks.PURPLE_TOOLBOX,
        AllBlocks.BLUE_TOOLBOX,
        AllBlocks.BROWN_TOOLBOX,
        AllBlocks.GREEN_TOOLBOX,
        AllBlocks.RED_TOOLBOX,
        AllBlocks.BLACK_TOOLBOX
    );
    public static final BlockEntityType<SchematicTableBlockEntity> SCHEMATIC_TABLE = register(
        "schematic_table",
        SchematicTableBlockEntity::new,
        AllBlocks.SCHEMATIC_TABLE
    );
    public static final BlockEntityType<SchematicannonBlockEntity> SCHEMATICANNON = register(
        "schematicannon",
        SchematicannonBlockEntity::new,
        AllBlocks.SCHEMATICANNON
    );
    public static final BlockEntityType<CopycatBlockEntity> COPYCAT = register(
        "copycat",
        CopycatBlockEntity::new,
        AllBlocks.COPYCAT_STEP,
        AllBlocks.COPYCAT_PANEL
    );

    private static <T extends BlockEntity> BlockEntityType<T> register(String id, BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
        return Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, id),
            new BlockEntityType<>(factory, Set.of(blocks))
        );
    }

    public static void register() {
    }
}
