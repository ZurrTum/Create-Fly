package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.zurrtum.create.content.contraptions.actors.plough.PloughBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlock;
import com.zurrtum.create.content.contraptions.actors.seat.SeatBlock;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.contraptions.bearing.ClockworkBearingBlock;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlock;
import com.zurrtum.create.content.contraptions.bearing.SailBlock;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlock;
import com.zurrtum.create.content.contraptions.chassis.LinearChassisBlock;
import com.zurrtum.create.content.contraptions.chassis.RadialChassisBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlock.MinecartAnchorBlock;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonHeadBlock;
import com.zurrtum.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock;
import com.zurrtum.create.content.decoration.CardboardBlock;
import com.zurrtum.create.content.decoration.MetalLadderBlock;
import com.zurrtum.create.content.decoration.MetalScaffoldingBlock;
import com.zurrtum.create.content.decoration.TrainTrapdoorBlock;
import com.zurrtum.create.content.decoration.bracket.BracketBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatPanelBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatStepBlock;
import com.zurrtum.create.content.decoration.encasing.CasingBlock;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import com.zurrtum.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.zurrtum.create.content.decoration.palettes.*;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleExtenderBlock;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.bell.HauntedBellBlock;
import com.zurrtum.create.content.equipment.bell.PeculiarBellBlock;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlock;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlock;
import com.zurrtum.create.content.fluids.pipes.EncasedPipeBlock;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlock;
import com.zurrtum.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlock;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlock;
import com.zurrtum.create.content.fluids.pump.PumpBlock;
import com.zurrtum.create.content.fluids.spout.SpoutBlock;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.zurrtum.create.content.kinetics.chainDrive.ChainDriveBlock;
import com.zurrtum.create.content.kinetics.chainDrive.ChainGearshiftBlock;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlock;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelBlock;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlock;
import com.zurrtum.create.content.kinetics.drill.DrillBlock;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlock;
import com.zurrtum.create.content.kinetics.fan.NozzleBlock;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlock;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlock;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlock;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlock;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlock;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.content.kinetics.transmission.ClutchBlock;
import com.zurrtum.create.content.kinetics.transmission.GearshiftBlock;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlock;
import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlock;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import com.zurrtum.create.content.logistics.chute.ChuteBlock;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlock;
import com.zurrtum.create.content.logistics.crate.CreativeCrateBlock;
import com.zurrtum.create.content.logistics.depot.DepotBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.funnel.AndesiteFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.BrassFunnelBlock;
import com.zurrtum.create.content.logistics.itemHatch.ItemHatchBlock;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlock;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.repackager.RepackagerBlock;
import com.zurrtum.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlock;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlock;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlock;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlock;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlock;
import com.zurrtum.create.content.materials.ExperienceBlock;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.LitBlazeBurnerBlock;
import com.zurrtum.create.content.redstone.RoseQuartzLampBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.contact.RedstoneContactBlock;
import com.zurrtum.create.content.redstone.deskBell.DeskBellBlock;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock;
import com.zurrtum.create.content.redstone.diodes.PoweredLatchBlock;
import com.zurrtum.create.content.redstone.diodes.ToggleLatchBlock;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlock;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlock;
import com.zurrtum.create.content.redstone.nixieTube.NixieTubeBlock;
import com.zurrtum.create.content.redstone.rail.ControllerRailBlock;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlock;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlock;
import com.zurrtum.create.content.schematics.table.SchematicTableBlock;
import com.zurrtum.create.content.trains.bogey.StandardBogeyBlock;
import com.zurrtum.create.content.trains.display.FlapDisplayBlock;
import com.zurrtum.create.content.trains.observer.TrackObserverBlock;
import com.zurrtum.create.content.trains.signal.SignalBlock;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.track.FakeTrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.infrastructure.config.CStress;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.block.*;
import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBlocks {
    public static final List<Block> ALL = new ArrayList<>();
    public static final CogWheelBlock COGWHEEL = register(
        "cogwheel",
        CogWheelBlock::small,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.DIRT_BROWN)
    );
    public static final CogWheelBlock LARGE_COGWHEEL = register(
        "large_cogwheel",
        CogWheelBlock::large,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.DIRT_BROWN)
    );
    @SuppressWarnings("deprecation")
    public static final ShaftBlock SHAFT = register(
        "shaft",
        ShaftBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.IRON_GRAY).notSolid()
    );
    public static final PoweredShaftBlock POWERED_SHAFT = register(
        "powered_shaft",
        PoweredShaftBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.IRON_GRAY).solid()
    );
    public static final GantryShaftBlock GANTRY_SHAFT = register(
        "gantry_shaft",
        GantryShaftBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.DARK_RED).solid()
    );
    public static final SteamEngineBlock STEAM_ENGINE = register(
        "steam_engine",
        SteamEngineBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.IRON_GRAY).solid()
    );
    public static final SequencedGearshiftBlock SEQUENCED_GEARSHIFT = register(
        "sequenced_gearshift",
        SequencedGearshiftBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque()
    );
    public static final GantryCarriageBlock GANTRY_CARRIAGE = register(
        "gantry_carriage",
        GantryCarriageBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final CreativeMotorBlock CREATIVE_MOTOR = register(
        "creative_motor",
        CreativeMotorBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.PURPLE).solid()
    );
    public static final SpeedControllerBlock ROTATION_SPEED_CONTROLLER = register(
        "rotation_speed_controller",
        SpeedControllerBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).nonOpaque()
    );
    public static final GearboxBlock GEARBOX = register(
        "gearbox",
        GearboxBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque().pistonBehavior(PistonBehavior.PUSH_ONLY)
    );
    public static final WaterWheelBlock WATER_WHEEL = register(
        "water_wheel",
        WaterWheelBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT_BROWN).nonOpaque()
    );
    public static final LargeWaterWheelBlock LARGE_WATER_WHEEL = register(
        "large_water_wheel",
        LargeWaterWheelBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT_BROWN).nonOpaque()
    );
    public static final WaterWheelStructuralBlock WATER_WHEEL_STRUCTURAL = register(
        "water_wheel_structure",
        WaterWheelStructuralBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT_BROWN).nonOpaque().pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final CasingBlock ANDESITE_CASING = register(
        "andesite_casing",
        CasingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).sounds(BlockSoundGroup.WOOD)
    );
    public static final CasingBlock BRASS_CASING = register(
        "brass_casing",
        CasingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).sounds(BlockSoundGroup.WOOD)
    );
    public static final CasingBlock COPPER_CASING = register(
        "copper_casing",
        CasingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).sounds(BlockSoundGroup.COPPER)
    );
    public static final CasingBlock SHADOW_STEEL_CASING = register(
        "shadow_steel_casing",
        CasingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.BLACK).sounds(BlockSoundGroup.WOOD)
    );
    public static final CasingBlock REFINED_RADIANCE_CASING = register(
        "refined_radiance_casing",
        CasingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.WHITE).sounds(BlockSoundGroup.WOOD).luminance($ -> 12)
    );
    public static final CasingBlock RAILWAY_CASING = register(
        "railway_casing",
        CasingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_CYAN).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final ItemVaultBlock ITEM_VAULT = register(
        "item_vault",
        ItemVaultBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).sounds(BlockSoundGroup.NETHERITE).mapColor(MapColor.TERRACOTTA_BLUE).resistance(1200)
    );
    public static final ArmBlock MECHANICAL_ARM = register(
        "mechanical_arm",
        ArmBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final DepotBlock DEPOT = register("depot", DepotBlock::new, AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY));
    public static final BeltBlock BELT = register(
        "belt",
        BeltBlock::new,
        AbstractBlock.Settings.create().sounds(BlockSoundGroup.WOOL).strength(0.8f).mapColor(MapColor.GRAY)
    );
    public static final ClutchBlock CLUTCH = register(
        "clutch",
        ClutchBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final GearshiftBlock GEARSHIFT = register(
        "gearshift",
        GearshiftBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final ChainDriveBlock ENCASED_CHAIN_DRIVE = register(
        "encased_chain_drive",
        ChainDriveBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final ChainGearshiftBlock ADJUSTABLE_CHAIN_GEARSHIFT = register(
        "adjustable_chain_gearshift",
        ChainGearshiftBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.DARK_RED).nonOpaque()
    );
    public static final ChainConveyorBlock CHAIN_CONVEYOR = register(
        "chain_conveyor",
        ChainConveyorBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final EncasedShaftBlock ANDESITE_ENCASED_SHAFT = register(
        "andesite_encased_shaft",
        EncasedShaftBlock::andesite,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final EncasedShaftBlock BRASS_ENCASED_SHAFT = register(
        "brass_encased_shaft",
        EncasedShaftBlock::brass,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque()
    );
    public static final EncasedCogwheelBlock ANDESITE_ENCASED_COGWHEEL = register(
        "andesite_encased_cogwheel",
        p -> new EncasedCogwheelBlock(p, false, AllBlocks.ANDESITE_CASING),
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final EncasedCogwheelBlock BRASS_ENCASED_COGWHEEL = register(
        "brass_encased_cogwheel",
        p -> new EncasedCogwheelBlock(p, false, AllBlocks.BRASS_CASING),
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque()
    );
    public static final EncasedCogwheelBlock ANDESITE_ENCASED_LARGE_COGWHEEL = register(
        "andesite_encased_large_cogwheel",
        p -> new EncasedCogwheelBlock(p, true, AllBlocks.ANDESITE_CASING),
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final EncasedCogwheelBlock BRASS_ENCASED_LARGE_COGWHEEL = register(
        "brass_encased_large_cogwheel",
        p -> new EncasedCogwheelBlock(p, true, AllBlocks.BRASS_CASING),
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque()
    );
    public static final HandCrankBlock HAND_CRANK = register(
        "hand_crank",
        HandCrankBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final ValveHandleBlock COPPER_VALVE_HANDLE = register(
        "copper_valve_handle",
        ValveHandleBlock::copper,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final ValveHandleBlock WHITE_VALVE_HANDLE = register(
        "white_valve_handle",
        ValveHandleBlock.dyed(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.WHITE)
    );
    public static final ValveHandleBlock ORANGE_VALVE_HANDLE = register(
        "orange_valve_handle",
        ValveHandleBlock.dyed(DyeColor.ORANGE),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.ORANGE)
    );
    public static final ValveHandleBlock MAGENTA_VALVE_HANDLE = register(
        "magenta_valve_handle",
        ValveHandleBlock.dyed(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.MAGENTA)
    );
    public static final ValveHandleBlock LIGHT_BLUE_VALVE_HANDLE = register(
        "light_blue_valve_handle",
        ValveHandleBlock.dyed(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.LIGHT_BLUE)
    );
    public static final ValveHandleBlock YELLOW_VALVE_HANDLE = register(
        "yellow_valve_handle",
        ValveHandleBlock.dyed(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.YELLOW)
    );
    public static final ValveHandleBlock LIME_VALVE_HANDLE = register(
        "lime_valve_handle",
        ValveHandleBlock.dyed(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.LIME)
    );
    public static final ValveHandleBlock PINK_VALVE_HANDLE = register(
        "pink_valve_handle",
        ValveHandleBlock.dyed(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.PINK)
    );
    public static final ValveHandleBlock GRAY_VALVE_HANDLE = register(
        "gray_valve_handle",
        ValveHandleBlock.dyed(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GRAY)
    );
    public static final ValveHandleBlock LIGHT_GRAY_VALVE_HANDLE = register(
        "light_gray_valve_handle",
        ValveHandleBlock.dyed(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.LIGHT_GRAY)
    );
    public static final ValveHandleBlock CYAN_VALVE_HANDLE = register(
        "cyan_valve_handle",
        ValveHandleBlock.dyed(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.CYAN)
    );
    public static final ValveHandleBlock PURPLE_VALVE_HANDLE = register(
        "purple_valve_handle",
        ValveHandleBlock.dyed(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.PURPLE)
    );
    public static final ValveHandleBlock BLUE_VALVE_HANDLE = register(
        "blue_valve_handle",
        ValveHandleBlock.dyed(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.BLUE)
    );
    public static final ValveHandleBlock BROWN_VALVE_HANDLE = register(
        "brown_valve_handle",
        ValveHandleBlock.dyed(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.BROWN)
    );
    public static final ValveHandleBlock GREEN_VALVE_HANDLE = register(
        "green_valve_handle",
        ValveHandleBlock.dyed(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GREEN)
    );
    public static final ValveHandleBlock RED_VALVE_HANDLE = register(
        "red_valve_handle",
        ValveHandleBlock.dyed(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.RED)
    );
    public static final ValveHandleBlock BLACK_VALVE_HANDLE = register(
        "black_valve_handle",
        ValveHandleBlock.dyed(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.BLACK)
    );
    public static final RadialChassisBlock RADIAL_CHASSIS = register(
        "radial_chassis",
        RadialChassisBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT_BROWN)
    );
    public static final LinearChassisBlock LINEAR_CHASSIS = register(
        "linear_chassis",
        LinearChassisBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final LinearChassisBlock SECONDARY_LINEAR_CHASSIS = register(
        "secondary_linear_chassis",
        LinearChassisBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final WindmillBearingBlock WINDMILL_BEARING = register(
        "windmill_bearing",
        WindmillBearingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final MechanicalBearingBlock MECHANICAL_BEARING = register(
        "mechanical_bearing",
        MechanicalBearingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final MechanicalPistonBlock MECHANICAL_PISTON = register(
        "mechanical_piston",
        MechanicalPistonBlock::normal,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final MechanicalPistonBlock STICKY_MECHANICAL_PISTON = register(
        "sticky_mechanical_piston",
        MechanicalPistonBlock::sticky,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final MechanicalPistonHeadBlock MECHANICAL_PISTON_HEAD = register(
        "mechanical_piston_head",
        MechanicalPistonHeadBlock::new,
        AbstractBlock.Settings.copy(Blocks.PISTON_HEAD).mapColor(MapColor.DIRT_BROWN).pistonBehavior(PistonBehavior.NORMAL)
    );
    public static final PistonExtensionPoleBlock PISTON_EXTENSION_POLE = register(
        "piston_extension_pole",
        PistonExtensionPoleBlock::new,
        AbstractBlock.Settings.copy(Blocks.PISTON_HEAD).sounds(BlockSoundGroup.SCAFFOLDING).mapColor(MapColor.DIRT_BROWN)
            .pistonBehavior(PistonBehavior.NORMAL).solid()
    );
    public static final SailBlock SAIL_FRAME = register(
        "sail_frame",
        SailBlock::frame,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT_BROWN).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock SAIL = register(
        "white_sail",
        SailBlock.withCanvas(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.WHITE).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock ORANGE_SAIL = register(
        "orange_sail",
        SailBlock.withCanvas(DyeColor.ORANGE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.ORANGE).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock MAGENTA_SAIL = register(
        "magenta_sail",
        SailBlock.withCanvas(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.MAGENTA).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock LIGHT_BLUE_SAIL = register(
        "light_blue_sail",
        SailBlock.withCanvas(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_BLUE).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock YELLOW_SAIL = register(
        "yellow_sail",
        SailBlock.withCanvas(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock LIME_SAIL = register(
        "lime_sail",
        SailBlock.withCanvas(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIME).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock PINK_SAIL = register(
        "pink_sail",
        SailBlock.withCanvas(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PINK).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock GRAY_SAIL = register(
        "gray_sail",
        SailBlock.withCanvas(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock LIGHT_GRAY_SAIL = register(
        "light_gray_sail",
        SailBlock.withCanvas(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_GRAY).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock CYAN_SAIL = register(
        "cyan_sail",
        SailBlock.withCanvas(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.CYAN).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock PURPLE_SAIL = register(
        "purple_sail",
        SailBlock.withCanvas(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PURPLE).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock BLUE_SAIL = register(
        "blue_sail",
        SailBlock.withCanvas(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLUE).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock BROWN_SAIL = register(
        "brown_sail",
        SailBlock.withCanvas(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BROWN).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock GREEN_SAIL = register(
        "green_sail",
        SailBlock.withCanvas(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GREEN).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock RED_SAIL = register(
        "red_sail",
        SailBlock.withCanvas(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.RED).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    public static final SailBlock BLACK_SAIL = register(
        "black_sail",
        SailBlock.withCanvas(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLACK).sounds(BlockSoundGroup.SCAFFOLDING).nonOpaque()
    );
    @SuppressWarnings("deprecation")
    public static final FluidPipeBlock FLUID_PIPE = register(
        "fluid_pipe",
        FluidPipeBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).notSolid()
    );
    public static final EncasedPipeBlock ENCASED_FLUID_PIPE = register(
        "encased_fluid_pipe",
        EncasedPipeBlock::copper,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).nonOpaque()
    );
    public static final GlassFluidPipeBlock GLASS_FLUID_PIPE = register(
        "glass_fluid_pipe",
        GlassFluidPipeBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).nonOpaque()
    );
    public static final PumpBlock MECHANICAL_PUMP = register(
        "mechanical_pump",
        PumpBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.STONE_GRAY)
    );
    public static final BlazeBurnerBlock BLAZE_BURNER = register(
        "blaze_burner",
        BlazeBurnerBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).luminance(BlazeBurnerBlock::getLight)
    );
    public static final LitBlazeBurnerBlock LIT_BLAZE_BURNER = register(
        "lit_blaze_burner",
        LitBlazeBurnerBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.LIGHT_GRAY).luminance(LitBlazeBurnerBlock::getLight)
    );
    public static final FluidTankBlock FLUID_TANK = register(
        "fluid_tank",
        FluidTankBlock::regular,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).nonOpaque().solidBlock((p1, p2, p3) -> true).luminance(FluidTankBlock::getLight)
    );
    public static final FluidTankBlock CREATIVE_FLUID_TANK = register(
        "creative_fluid_tank",
        FluidTankBlock::creative,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).nonOpaque().mapColor(MapColor.PURPLE)
    );
    public static final MechanicalPressBlock MECHANICAL_PRESS = register(
        "mechanical_press",
        MechanicalPressBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final EjectorBlock WEIGHTED_EJECTOR = register(
        "weighted_ejector",
        EjectorBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY).nonOpaque()
    );
    public static final PulleyBlock ROPE_PULLEY = register(
        "rope_pulley",
        PulleyBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final PulleyBlock.RopeBlock ROPE = register(
        "rope",
        PulleyBlock.RopeBlock::new,
        AbstractBlock.Settings.create().sounds(BlockSoundGroup.WOOL).mapColor(MapColor.BROWN).pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final PulleyBlock.MagnetBlock PULLEY_MAGNET = register(
        "pulley_magnet",
        PulleyBlock.MagnetBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final MillstoneBlock MILLSTONE = register(
        "millstone",
        MillstoneBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.IRON_GRAY)
    );
    public static final EncasedFanBlock ENCASED_FAN = register(
        "encased_fan",
        EncasedFanBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final PeculiarBellBlock PECULIAR_BELL = register(
        "peculiar_bell",
        PeculiarBellBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GOLD).sounds(BlockSoundGroup.ANVIL).nonOpaque().solid()
    );
    public static final HauntedBellBlock HAUNTED_BELL = register(
        "haunted_bell",
        HauntedBellBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.PALE_YELLOW).sounds(BlockSoundGroup.ANVIL).nonOpaque().solid()
    );
    public static final Block INDUSTRIAL_IRON_BLOCK = register(
        "industrial_iron_block",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE).requiresTool()
    );
    public static final Block WEATHERED_IRON_BLOCK = register(
        "weathered_iron_block",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE).requiresTool()
    );
    public static final WindowBlock INDUSTRIAL_IRON_WINDOW = register(
        "industrial_iron_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never).suffocates(AllBlocks::never)
            .blockVision(AllBlocks::never).mapColor(MapColor.GRAY)
    );
    public static final ConnectedGlassPaneBlock INDUSTRIAL_IRON_WINDOW_PANE = register(
        "industrial_iron_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.GRAY)
    );
    public static final WindowBlock WEATHERED_IRON_WINDOW = register(
        "weathered_iron_window",
        WindowBlock::translucent,
        AbstractBlock.Settings.copy(Blocks.GLASS).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never).suffocates(AllBlocks::never)
            .blockVision(AllBlocks::never).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final ConnectedGlassPaneBlock WEATHERED_IRON_WINDOW_PANE = register(
        "weathered_iron_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final SawBlock MECHANICAL_SAW = register(
        "mechanical_saw",
        SawBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final BasinBlock BASIN = register(
        "basin",
        BasinBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final AndesiteFunnelBlock ANDESITE_FUNNEL = register(
        "andesite_funnel",
        AndesiteFunnelBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY)
    );
    public static final BeltFunnelBlock ANDESITE_BELT_FUNNEL = register(
        "andesite_belt_funnel",
        BeltFunnelBlock::andesite,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY)
    );
    public static final BrassFunnelBlock BRASS_FUNNEL = register(
        "brass_funnel",
        BrassFunnelBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final BeltFunnelBlock BRASS_BELT_FUNNEL = register(
        "brass_belt_funnel",
        BeltFunnelBlock::brass,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final BeltTunnelBlock ANDESITE_TUNNEL = register(
        "andesite_tunnel",
        BeltTunnelBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY).nonOpaque()
    );
    public static final BrassTunnelBlock BRASS_TUNNEL = register(
        "brass_tunnel",
        BrassTunnelBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_YELLOW).nonOpaque()
    );
    public static final ChuteBlock CHUTE = register(
        "chute",
        ChuteBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE).nonOpaque()
            .suffocates(AllBlocks::never)
    );
    public static final SmartChuteBlock SMART_CHUTE = register(
        "smart_chute",
        SmartChuteBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE).nonOpaque()
            .suffocates(AllBlocks::never).solidBlock(AllBlocks::never)
    );
    public static final ControllerRailBlock CONTROLLER_RAIL = register(
        "controller_rail",
        ControllerRailBlock::new,
        AbstractBlock.Settings.copy(Blocks.POWERED_RAIL).mapColor(MapColor.STONE_GRAY)
    );
    public static final CartAssemblerBlock CART_ASSEMBLER = register(
        "cart_assembler",
        CartAssemblerBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY).nonOpaque().pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final MinecartAnchorBlock MINECART_ANCHOR = register(
        "minecart_anchor",
        MinecartAnchorBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final PloughBlock MECHANICAL_PLOUGH = register(
        "mechanical_plough",
        PloughBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY).solid()
    );
    public static final HarvesterBlock MECHANICAL_HARVESTER = register(
        "mechanical_harvester",
        HarvesterBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.IRON_GRAY).solid()
    );
    public static final PortableStorageInterfaceBlock PORTABLE_FLUID_INTERFACE = register(
        "portable_fluid_interface",
        PortableStorageInterfaceBlock::forFluids,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final PortableStorageInterfaceBlock PORTABLE_STORAGE_INTERFACE = register(
        "portable_storage_interface",
        PortableStorageInterfaceBlock::forItems,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final GaugeBlock SPEEDOMETER = register(
        "speedometer",
        GaugeBlock::speed,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final GaugeBlock STRESSOMETER = register(
        "stressometer",
        GaugeBlock::stress,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final CuckooClockBlock CUCKOO_CLOCK = register(
        "cuckoo_clock",
        CuckooClockBlock::regular,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final CuckooClockBlock MYSTERIOUS_CUCKOO_CLOCK = register(
        "mysterious_cuckoo_clock",
        CuckooClockBlock::mysterious,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final MechanicalMixerBlock MECHANICAL_MIXER = register(
        "mechanical_mixer",
        MechanicalMixerBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY).nonOpaque()
    );
    public static final HosePulleyBlock HOSE_PULLEY = register(
        "hose_pulley",
        HosePulleyBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.STONE_GRAY).nonOpaque()
    );
    public static final SpoutBlock SPOUT = register("spout", SpoutBlock::new, AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK));
    public static final ItemDrainBlock ITEM_DRAIN = register("item_drain", ItemDrainBlock::new, AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK));
    public static final WhistleBlock STEAM_WHISTLE = register(
        "steam_whistle",
        WhistleBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GOLD)
    );
    public static final WhistleExtenderBlock STEAM_WHISTLE_EXTENSION = register(
        "steam_whistle_extension",
        WhistleExtenderBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GOLD).solid()
    );
    public static final BacktankBlock COPPER_BACKTANK = register(
        "copper_backtank",
        BacktankBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final BacktankBlock NETHERITE_BACKTANK = register(
        "netherite_backtank",
        BacktankBlock::new,
        AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)
    );
    public static final DeployerBlock DEPLOYER = register(
        "deployer",
        DeployerBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN).nonOpaque()
    );
    public static final TurntableBlock TURNTABLE = register(
        "turntable",
        TurntableBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final DrillBlock MECHANICAL_DRILL = register(
        "mechanical_drill",
        DrillBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final ClockworkBearingBlock CLOCKWORK_BEARING = register(
        "clockwork_bearing",
        ClockworkBearingBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque()
    );
    public static final CrushingWheelBlock CRUSHING_WHEEL = register(
        "crushing_wheel",
        CrushingWheelBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.IRON_GRAY).nonOpaque()
    );
    public static final CrushingWheelControllerBlock CRUSHING_WHEEL_CONTROLLER = register(
        "crushing_wheel_controller",
        CrushingWheelControllerBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY).dropsNothing().noCollision().pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final Block RAW_ZINC_BLOCK = register(
        "raw_zinc_block",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.RAW_GOLD_BLOCK).mapColor(MapColor.LICHEN_GREEN).requiresTool()
    );
    public static final Block ZINC_BLOCK = register(
        "zinc_block",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.LICHEN_GREEN).requiresTool()
    );
    public static final Block ZINC_ORE = register(
        "zinc_ore",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_ORE).mapColor(MapColor.IRON_GRAY).requiresTool().sounds(BlockSoundGroup.STONE)
    );
    public static final Block DEEPSLATE_ZINC_ORE = register(
        "deepslate_zinc_ore",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE_GOLD_ORE).mapColor(MapColor.STONE_GRAY).requiresTool().sounds(BlockSoundGroup.DEEPSLATE)
    );
    public static final Block BRASS_BLOCK = register(
        "brass_block",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).requiresTool()
    );
    public static final FlapDisplayBlock DISPLAY_BOARD = register(
        "display_board",
        FlapDisplayBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY)
    );
    public static final ClipboardBlock CLIPBOARD = register(
        "clipboard",
        ClipboardBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).solid()
    );
    public static final DisplayLinkBlock DISPLAY_LINK = register(
        "display_link",
        DisplayLinkBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final NixieTubeBlock ORANGE_NIXIE_TUBE = register(
        "nixie_tube",
        NixieTubeBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.ORANGE).solid()
    );
    public static final NixieTubeBlock WHITE_NIXIE_TUBE = register(
        "white_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.WHITE).solid()
    );
    public static final NixieTubeBlock MAGENTA_NIXIE_TUBE = register(
        "magenta_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.MAGENTA).solid()
    );
    public static final NixieTubeBlock LIGHT_BLUE_NIXIE_TUBE = register(
        "light_blue_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.LIGHT_BLUE).solid()
    );
    public static final NixieTubeBlock YELLOW_NIXIE_TUBE = register(
        "yellow_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.YELLOW).solid()
    );
    public static final NixieTubeBlock LIME_NIXIE_TUBE = register(
        "lime_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.LIME).solid()
    );
    public static final NixieTubeBlock PINK_NIXIE_TUBE = register(
        "pink_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.PINK).solid()
    );
    public static final NixieTubeBlock GRAY_NIXIE_TUBE = register(
        "gray_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.GRAY).solid()
    );
    public static final NixieTubeBlock LIGHT_GRAY_NIXIE_TUBE = register(
        "light_gray_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.LIGHT_GRAY).solid()
    );
    public static final NixieTubeBlock CYAN_NIXIE_TUBE = register(
        "cyan_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.CYAN).solid()
    );
    public static final NixieTubeBlock PURPLE_NIXIE_TUBE = register(
        "purple_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.PURPLE).solid()
    );
    public static final NixieTubeBlock BLUE_NIXIE_TUBE = register(
        "blue_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.BLUE).solid()
    );
    public static final NixieTubeBlock BROWN_NIXIE_TUBE = register(
        "brown_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.BROWN).solid()
    );
    public static final NixieTubeBlock GREEN_NIXIE_TUBE = register(
        "green_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.GREEN).solid()
    );
    public static final NixieTubeBlock RED_NIXIE_TUBE = register(
        "red_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.RED).solid()
    );
    public static final NixieTubeBlock BLACK_NIXIE_TUBE = register(
        "black_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).luminance($ -> 5).mapColor(MapColor.BLACK).solid()
    );
    public static final BracketBlock WOODEN_BRACKET = register(
        "wooden_bracket",
        BracketBlock::new,
        AbstractBlock.Settings.create().sounds(BlockSoundGroup.SCAFFOLDING)
    );
    public static final BracketBlock METAL_BRACKET = register(
        "metal_bracket",
        BracketBlock::new,
        AbstractBlock.Settings.create().sounds(BlockSoundGroup.NETHERITE)
    );
    public static final GirderBlock METAL_GIRDER = register(
        "metal_girder",
        GirderBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final GirderEncasedShaftBlock METAL_GIRDER_ENCASED_SHAFT = register(
        "metal_girder_encased_shaft",
        GirderEncasedShaftBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final FluidValveBlock FLUID_VALVE = register(
        "fluid_valve",
        FluidValveBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GRAY).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final SmartFluidPipeBlock SMART_FLUID_PIPE = register(
        "smart_fluid_pipe",
        SmartFluidPipeBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final AnalogLeverBlock ANALOG_LEVER = register("analog_lever", AnalogLeverBlock::new, AbstractBlock.Settings.copy(Blocks.LEVER));
    public static final RedstoneContactBlock REDSTONE_CONTACT = register(
        "redstone_contact",
        RedstoneContactBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY)
    );
    public static final RedstoneLinkBlock REDSTONE_LINK = register(
        "redstone_link",
        RedstoneLinkBlock::new,
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN).solid()
    );
    public static final BrassDiodeBlock PULSE_REPEATER = register(
        "pulse_repeater",
        BrassDiodeBlock::new,
        AbstractBlock.Settings.copy(Blocks.REPEATER)
    );
    public static final BrassDiodeBlock PULSE_EXTENDER = register(
        "pulse_extender",
        BrassDiodeBlock::new,
        AbstractBlock.Settings.copy(Blocks.REPEATER)
    );
    public static final BrassDiodeBlock PULSE_TIMER = register("pulse_timer", BrassDiodeBlock::new, AbstractBlock.Settings.copy(Blocks.REPEATER));
    public static final PoweredLatchBlock POWERED_LATCH = register(
        "powered_latch",
        PoweredLatchBlock::new,
        AbstractBlock.Settings.copy(Blocks.REPEATER)
    );
    public static final ToggleLatchBlock POWERED_TOGGLE_LATCH = register(
        "powered_toggle_latch",
        ToggleLatchBlock::new,
        AbstractBlock.Settings.copy(Blocks.REPEATER)
    );
    public static final RoseQuartzLampBlock ROSE_QUARTZ_LAMP = register(
        "rose_quartz_lamp",
        RoseQuartzLampBlock::new,
        AbstractBlock.Settings.copy(Blocks.REDSTONE_LAMP).mapColor(MapColor.TERRACOTTA_PINK)
            .luminance(state -> state.get(RoseQuartzLampBlock.POWERING) ? 15 : 0)
    );
    public static final SmartObserverBlock SMART_OBSERVER = register(
        "content_observer",
        SmartObserverBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque().solidBlock(AllBlocks::never)
    );
    public static final ThresholdSwitchBlock THRESHOLD_SWITCH = register(
        "stockpile_switch",
        ThresholdSwitchBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).nonOpaque().solidBlock(AllBlocks::never)
    );
    public static final StickerBlock STICKER = register("sticker", StickerBlock::new, AbstractBlock.Settings.copy(Blocks.ANDESITE).nonOpaque());
    public static final ContraptionControlsBlock CONTRAPTION_CONTROLS = register(
        "contraption_controls",
        ContraptionControlsBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final ElevatorPulleyBlock ELEVATOR_PULLEY = register(
        "elevator_pulley",
        ElevatorPulleyBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final ElevatorContactBlock ELEVATOR_CONTACT = register(
        "elevator_contact",
        ElevatorContactBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).luminance(ElevatorContactBlock::getLight)
    );
    public static final SlidingDoorBlock ANDESITE_DOOR = register(
        "andesite_door",
        SlidingDoorBlock::stone_fold,
        AbstractBlock.Settings.copy(Blocks.IRON_DOOR).requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.STONE_GRAY).nonOpaque()
    );
    public static final SlidingDoorBlock BRASS_DOOR = register(
        "brass_door",
        SlidingDoorBlock::stone_slide,
        AbstractBlock.Settings.copy(Blocks.IRON_DOOR).requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.TERRACOTTA_YELLOW).nonOpaque()
    );
    public static final SlidingDoorBlock COPPER_DOOR = register(
        "copper_door",
        SlidingDoorBlock::stone_fold,
        AbstractBlock.Settings.copy(Blocks.IRON_DOOR).requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.ORANGE).nonOpaque()
    );
    public static final SlidingDoorBlock TRAIN_DOOR = register(
        "train_door",
        SlidingDoorBlock::metal_slide,
        AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.TERRACOTTA_CYAN).nonOpaque()
    );
    public static final SlidingDoorBlock FRAMED_GLASS_DOOR = register(
        "framed_glass_door",
        SlidingDoorBlock::glass_slide,
        AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).requiresTool().strength(3.0F, 6.0F).mapColor(MapColor.CLEAR).nonOpaque()
    );
    public static final NozzleBlock NOZZLE = register(
        "nozzle",
        NozzleBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.LIGHT_GRAY)
    );
    public static final DeskBellBlock DESK_BELL = register(
        "desk_bell",
        DeskBellBlock::new,
        AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW)
    );
    public static final MechanicalCrafterBlock MECHANICAL_CRAFTER = register(
        "mechanical_crafter",
        MechanicalCrafterBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).nonOpaque()
    );
    public static final CreativeCrateBlock CREATIVE_CRATE = register(
        "creative_crate",
        CreativeCrateBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.PURPLE)
    );
    public static final TrackBlock TRACK = register(
        "track",
        TrackBlock::andesite,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.IRON_GRAY).strength(0.8F).sounds(BlockSoundGroup.METAL).nonOpaque().solid()
            .pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final FakeTrackBlock FAKE_TRACK = register(
        "fake_track",
        FakeTrackBlock::new,
        AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).ticksRandomly().noCollision().replaceable()
    );
    public static final SignalBlock TRACK_SIGNAL = register(
        "track_signal",
        SignalBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.SPRUCE_BROWN).nonOpaque().sounds(BlockSoundGroup.NETHERITE)
    );
    public static final StandardBogeyBlock SMALL_BOGEY = register(
        "small_bogey",
        StandardBogeyBlock::small,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.SPRUCE_BROWN).sounds(BlockSoundGroup.NETHERITE).nonOpaque()
    );
    public static final StandardBogeyBlock LARGE_BOGEY = register(
        "large_bogey",
        StandardBogeyBlock::large,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.SPRUCE_BROWN).sounds(BlockSoundGroup.NETHERITE).nonOpaque()
    );
    public static final ControlsBlock TRAIN_CONTROLS = register(
        "controls",
        ControlsBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final StationBlock TRACK_STATION = register(
        "track_station",
        StationBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.SPRUCE_BROWN).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final TrackObserverBlock TRACK_OBSERVER = register(
        "track_observer",
        TrackObserverBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.SPRUCE_BROWN).nonOpaque().sounds(BlockSoundGroup.NETHERITE)
    );
    public static final SeatBlock WHITE_SEAT = register(
        "white_seat",
        SeatBlock.dyed(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.WHITE)
    );
    public static final SeatBlock ORANGE_SEAT = register(
        "orange_seat",
        SeatBlock.dyed(DyeColor.ORANGE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.ORANGE)
    );
    public static final SeatBlock MAGENTA_SEAT = register(
        "magenta_seat",
        SeatBlock.dyed(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.MAGENTA)
    );
    public static final SeatBlock LIGHT_BLUE_SEAT = register(
        "light_blue_seat",
        SeatBlock.dyed(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_BLUE)
    );
    public static final SeatBlock YELLOW_SEAT = register(
        "yellow_seat",
        SeatBlock.dyed(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.YELLOW)
    );
    public static final SeatBlock LIME_SEAT = register(
        "lime_seat",
        SeatBlock.dyed(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIME)
    );
    public static final SeatBlock PINK_SEAT = register(
        "pink_seat",
        SeatBlock.dyed(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PINK)
    );
    public static final SeatBlock GRAY_SEAT = register(
        "gray_seat",
        SeatBlock.dyed(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GRAY)
    );
    public static final SeatBlock LIGHT_GRAY_SEAT = register(
        "light_gray_seat",
        SeatBlock.dyed(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_GRAY)
    );
    public static final SeatBlock CYAN_SEAT = register(
        "cyan_seat",
        SeatBlock.dyed(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.CYAN)
    );
    public static final SeatBlock PURPLE_SEAT = register(
        "purple_seat",
        SeatBlock.dyed(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PURPLE)
    );
    public static final SeatBlock BLUE_SEAT = register(
        "blue_seat",
        SeatBlock.dyed(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLUE)
    );
    public static final SeatBlock BROWN_SEAT = register(
        "brown_seat",
        SeatBlock.dyed(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BROWN)
    );
    public static final SeatBlock GREEN_SEAT = register(
        "green_seat",
        SeatBlock.dyed(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GREEN)
    );
    public static final SeatBlock RED_SEAT = register(
        "red_seat",
        SeatBlock.dyed(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.RED)
    );
    public static final SeatBlock BLACK_SEAT = register(
        "black_seat",
        SeatBlock.dyed(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLACK)
    );
    public static final RollerBlock MECHANICAL_ROLLER = register(
        "mechanical_roller",
        RollerBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.GRAY).nonOpaque()
    );
    public static final LecternControllerBlock LECTERN_CONTROLLER = register(
        "lectern_controller",
        LecternControllerBlock::new,
        AbstractBlock.Settings.copy(Blocks.LECTERN)
    );
    public static final PackagerBlock PACKAGER = register(
        "packager",
        PackagerBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque().solidBlock(AllBlocks::never).mapColor(MapColor.TERRACOTTA_BLUE)
            .sounds(BlockSoundGroup.NETHERITE)
    );
    public static final CardboardBlock CARDBOARD_BLOCK = register(
        "cardboard_block",
        CardboardBlock::new,
        AbstractBlock.Settings.copy(Blocks.MUSHROOM_STEM).mapColor(MapColor.BROWN).sounds(BlockSoundGroup.CHISELED_BOOKSHELF).burnable()
    );
    public static final PackagerLinkBlock STOCK_LINK = register(
        "stock_link",
        PackagerLinkBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final RedstoneRequesterBlock REDSTONE_REQUESTER = register(
        "redstone_requester",
        RedstoneRequesterBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).sounds(BlockSoundGroup.NETHERITE).nonOpaque()
    );
    public static final RepackagerBlock REPACKAGER = register(
        "repackager",
        RepackagerBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque().solidBlock(AllBlocks::never).mapColor(MapColor.TERRACOTTA_BLUE)
            .sounds(BlockSoundGroup.NETHERITE)
    );
    public static final StockTickerBlock STOCK_TICKER = register(
        "stock_ticker",
        StockTickerBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).sounds(BlockSoundGroup.GLASS)
    );
    public static final TableClothBlock WHITE_TABLE_CLOTH = register(
        "white_table_cloth",
        TableClothBlock.dyed(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.WHITE)
    );
    public static final TableClothBlock ORANGE_TABLE_CLOTH = register(
        "orange_table_cloth",
        TableClothBlock.dyed(DyeColor.ORANGE),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.ORANGE)
    );
    public static final TableClothBlock MAGENTA_TABLE_CLOTH = register(
        "magenta_table_cloth",
        TableClothBlock.dyed(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.MAGENTA)
    );
    public static final TableClothBlock LIGHT_BLUE_TABLE_CLOTH = register(
        "light_blue_table_cloth",
        TableClothBlock.dyed(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.LIGHT_BLUE)
    );
    public static final TableClothBlock YELLOW_TABLE_CLOTH = register(
        "yellow_table_cloth",
        TableClothBlock.dyed(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.YELLOW)
    );
    public static final TableClothBlock LIME_TABLE_CLOTH = register(
        "lime_table_cloth",
        TableClothBlock.dyed(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.LIME)
    );
    public static final TableClothBlock PINK_TABLE_CLOTH = register(
        "pink_table_cloth",
        TableClothBlock.dyed(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.PINK)
    );
    public static final TableClothBlock GRAY_TABLE_CLOTH = register(
        "gray_table_cloth",
        TableClothBlock.dyed(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.GRAY)
    );
    public static final TableClothBlock LIGHT_GRAY_TABLE_CLOTH = register(
        "light_gray_table_cloth",
        TableClothBlock.dyed(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.LIGHT_GRAY)
    );
    public static final TableClothBlock CYAN_TABLE_CLOTH = register(
        "cyan_table_cloth",
        TableClothBlock.dyed(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.CYAN)
    );
    public static final TableClothBlock PURPLE_TABLE_CLOTH = register(
        "purple_table_cloth",
        TableClothBlock.dyed(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.PURPLE)
    );
    public static final TableClothBlock BLUE_TABLE_CLOTH = register(
        "blue_table_cloth",
        TableClothBlock.dyed(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.BLUE)
    );
    public static final TableClothBlock BROWN_TABLE_CLOTH = register(
        "brown_table_cloth",
        TableClothBlock.dyed(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.BROWN)
    );
    public static final TableClothBlock GREEN_TABLE_CLOTH = register(
        "green_table_cloth",
        TableClothBlock.dyed(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.GREEN)
    );
    public static final TableClothBlock RED_TABLE_CLOTH = register(
        "red_table_cloth",
        TableClothBlock.dyed(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.RED)
    );
    public static final TableClothBlock BLACK_TABLE_CLOTH = register(
        "black_table_cloth",
        TableClothBlock.dyed(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.BLACK_CARPET).mapColor(MapColor.BLACK)
    );
    public static final TableClothBlock ANDESITE_TABLE_CLOTH = register(
        "andesite_table_cloth",
        TableClothBlock.styled("andesite"),
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY).requiresTool()
    );
    public static final TableClothBlock BRASS_TABLE_CLOTH = register(
        "brass_table_cloth",
        TableClothBlock.styled("brass"),
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).requiresTool()
    );
    public static final TableClothBlock COPPER_TABLE_CLOTH = register(
        "copper_table_cloth",
        TableClothBlock.styled("copper"),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).requiresTool()
    );
    public static final PostboxBlock WHITE_POSTBOX = register(
        "white_postbox",
        PostboxBlock.dyed(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.WHITE)
    );
    public static final PostboxBlock ORANGE_POSTBOX = register(
        "orange_postbox",
        PostboxBlock.dyed(DyeColor.ORANGE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.ORANGE)
    );
    public static final PostboxBlock MAGENTA_POSTBOX = register(
        "magenta_postbox",
        PostboxBlock.dyed(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.MAGENTA)
    );
    public static final PostboxBlock LIGHT_BLUE_POSTBOX = register(
        "light_blue_postbox",
        PostboxBlock.dyed(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_BLUE)
    );
    public static final PostboxBlock YELLOW_POSTBOX = register(
        "yellow_postbox",
        PostboxBlock.dyed(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.YELLOW)
    );
    public static final PostboxBlock LIME_POSTBOX = register(
        "lime_postbox",
        PostboxBlock.dyed(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIME)
    );
    public static final PostboxBlock PINK_POSTBOX = register(
        "pink_postbox",
        PostboxBlock.dyed(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PINK)
    );
    public static final PostboxBlock GRAY_POSTBOX = register(
        "gray_postbox",
        PostboxBlock.dyed(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GRAY)
    );
    public static final PostboxBlock LIGHT_GRAY_POSTBOX = register(
        "light_gray_postbox",
        PostboxBlock.dyed(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_GRAY)
    );
    public static final PostboxBlock CYAN_POSTBOX = register(
        "cyan_postbox",
        PostboxBlock.dyed(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.CYAN)
    );
    public static final PostboxBlock PURPLE_POSTBOX = register(
        "purple_postbox",
        PostboxBlock.dyed(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PURPLE)
    );
    public static final PostboxBlock BLUE_POSTBOX = register(
        "blue_postbox",
        PostboxBlock.dyed(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLUE)
    );
    public static final PostboxBlock BROWN_POSTBOX = register(
        "brown_postbox",
        PostboxBlock.dyed(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BROWN)
    );
    public static final PostboxBlock GREEN_POSTBOX = register(
        "green_postbox",
        PostboxBlock.dyed(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GREEN)
    );
    public static final PostboxBlock RED_POSTBOX = register(
        "red_postbox",
        PostboxBlock.dyed(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.RED)
    );
    public static final PostboxBlock BLACK_POSTBOX = register(
        "black_postbox",
        PostboxBlock.dyed(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLACK)
    );
    public static final FrogportBlock PACKAGE_FROGPORT = register(
        "package_frogport",
        FrogportBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque().mapColor(MapColor.TERRACOTTA_BLUE).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final FactoryPanelBlock FACTORY_GAUGE = register(
        "factory_gauge",
        FactoryPanelBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).nonOpaque().solid()
    );
    public static final FlywheelBlock FLYWHEEL = register(
        "flywheel",
        FlywheelBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque().mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final ItemHatchBlock ITEM_HATCH = register(
        "item_hatch",
        ItemHatchBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE).sounds(BlockSoundGroup.NETHERITE)
    );
    public static final PlacardBlock PLACARD = register("placard", PlacardBlock::new, AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).solid());
    public static final ToolboxBlock WHITE_TOOLBOX = register(
        "white_toolbox",
        ToolboxBlock.dyed(DyeColor.WHITE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.WHITE).solid()
    );
    public static final ToolboxBlock ORANGE_TOOLBOX = register(
        "orange_toolbox",
        ToolboxBlock.dyed(DyeColor.ORANGE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.ORANGE).solid()
    );
    public static final ToolboxBlock MAGENTA_TOOLBOX = register(
        "magenta_toolbox",
        ToolboxBlock.dyed(DyeColor.MAGENTA),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.MAGENTA).solid()
    );
    public static final ToolboxBlock LIGHT_BLUE_TOOLBOX = register(
        "light_blue_toolbox",
        ToolboxBlock.dyed(DyeColor.LIGHT_BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_BLUE).solid()
    );
    public static final ToolboxBlock YELLOW_TOOLBOX = register(
        "yellow_toolbox",
        ToolboxBlock.dyed(DyeColor.YELLOW),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.YELLOW).solid()
    );
    public static final ToolboxBlock LIME_TOOLBOX = register(
        "lime_toolbox",
        ToolboxBlock.dyed(DyeColor.LIME),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIME).solid()
    );
    public static final ToolboxBlock PINK_TOOLBOX = register(
        "pink_toolbox",
        ToolboxBlock.dyed(DyeColor.PINK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PINK).solid()
    );
    public static final ToolboxBlock GRAY_TOOLBOX = register(
        "gray_toolbox",
        ToolboxBlock.dyed(DyeColor.GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GRAY).solid()
    );
    public static final ToolboxBlock LIGHT_GRAY_TOOLBOX = register(
        "light_gray_toolbox",
        ToolboxBlock.dyed(DyeColor.LIGHT_GRAY),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.LIGHT_GRAY).solid()
    );
    public static final ToolboxBlock CYAN_TOOLBOX = register(
        "cyan_toolbox",
        ToolboxBlock.dyed(DyeColor.CYAN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.CYAN).solid()
    );
    public static final ToolboxBlock PURPLE_TOOLBOX = register(
        "purple_toolbox",
        ToolboxBlock.dyed(DyeColor.PURPLE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PURPLE).solid()
    );
    public static final ToolboxBlock BLUE_TOOLBOX = register(
        "blue_toolbox",
        ToolboxBlock.dyed(DyeColor.BLUE),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLUE).solid()
    );
    public static final ToolboxBlock BROWN_TOOLBOX = register(
        "brown_toolbox",
        ToolboxBlock.dyed(DyeColor.BROWN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BROWN).solid()
    );
    public static final ToolboxBlock GREEN_TOOLBOX = register(
        "green_toolbox",
        ToolboxBlock.dyed(DyeColor.GREEN),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.GREEN).solid()
    );
    public static final ToolboxBlock RED_TOOLBOX = register(
        "red_toolbox",
        ToolboxBlock.dyed(DyeColor.RED),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.RED).solid()
    );
    public static final ToolboxBlock BLACK_TOOLBOX = register(
        "black_toolbox",
        ToolboxBlock.dyed(DyeColor.BLACK),
        AbstractBlock.Settings.copy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.BLACK).solid()
    );
    public static final SchematicTableBlock SCHEMATIC_TABLE = register(
        "schematic_table",
        SchematicTableBlock::new,
        AbstractBlock.Settings.copy(Blocks.LECTERN).mapColor(MapColor.SPRUCE_BROWN).solid().pistonBehavior(PistonBehavior.BLOCK)
    );
    public static final SchematicannonBlock SCHEMATICANNON = register(
        "schematicannon",
        SchematicannonBlock::new,
        AbstractBlock.Settings.copy(Blocks.DISPENSER).mapColor(MapColor.GRAY)
    );
    public static final WindowBlock ORNATE_IRON_WINDOW = register(
        "ornate_iron_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never).suffocates(AllBlocks::never)
            .blockVision(AllBlocks::never).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final MetalLadderBlock ANDESITE_LADDER = register(
        "andesite_ladder",
        MetalLadderBlock::new,
        AbstractBlock.Settings.copy(Blocks.LADDER).mapColor(MapColor.STONE_GRAY).sounds(BlockSoundGroup.COPPER)
    );
    public static final MetalLadderBlock BRASS_LADDER = register(
        "brass_ladder",
        MetalLadderBlock::new,
        AbstractBlock.Settings.copy(Blocks.LADDER).mapColor(MapColor.TERRACOTTA_YELLOW).sounds(BlockSoundGroup.COPPER)
    );
    public static final MetalLadderBlock COPPER_LADDER = register(
        "copper_ladder",
        MetalLadderBlock::new,
        AbstractBlock.Settings.copy(Blocks.LADDER).mapColor(MapColor.ORANGE).sounds(BlockSoundGroup.COPPER)
    );
    public static final MetalScaffoldingBlock ANDESITE_SCAFFOLD = register(
        "andesite_scaffolding",
        MetalScaffoldingBlock::new,
        AbstractBlock.Settings.copy(Blocks.SCAFFOLDING).sounds(BlockSoundGroup.COPPER).mapColor(MapColor.STONE_GRAY)
    );
    public static final MetalScaffoldingBlock BRASS_SCAFFOLD = register(
        "brass_scaffolding",
        MetalScaffoldingBlock::new,
        AbstractBlock.Settings.copy(Blocks.SCAFFOLDING).sounds(BlockSoundGroup.COPPER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final MetalScaffoldingBlock COPPER_SCAFFOLD = register(
        "copper_scaffolding",
        MetalScaffoldingBlock::new,
        AbstractBlock.Settings.copy(Blocks.SCAFFOLDING).sounds(BlockSoundGroup.COPPER).mapColor(MapColor.ORANGE)
    );
    public static final PaneBlock ANDESITE_BARS = register(
        "andesite_bars",
        PaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.IRON_BARS).sounds(BlockSoundGroup.COPPER).mapColor(MapColor.STONE_GRAY)
    );
    public static final PaneBlock BRASS_BARS = register(
        "brass_bars",
        PaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.IRON_BARS).sounds(BlockSoundGroup.COPPER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final PaneBlock COPPER_BARS = register(
        "copper_bars",
        PaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.IRON_BARS).sounds(BlockSoundGroup.COPPER).mapColor(MapColor.ORANGE)
    );
    public static final TrainTrapdoorBlock TRAIN_TRAPDOOR = register(
        "train_trapdoor",
        TrainTrapdoorBlock::metal,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_CYAN)
    );
    public static final TrainTrapdoorBlock FRAMED_GLASS_TRAPDOOR = register(
        "framed_glass_trapdoor",
        TrainTrapdoorBlock::metal,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.CLEAR).nonOpaque()
    );
    public static final Block ANDESITE_ALLOY_BLOCK = register(
        "andesite_alloy_block",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).mapColor(MapColor.STONE_GRAY).requiresTool()
    );
    public static final CardboardBlock BOUND_CARDBOARD_BLOCK = register(
        "bound_cardboard_block",
        CardboardBlock::new,
        AbstractBlock.Settings.copy(Blocks.MUSHROOM_STEM).mapColor(MapColor.BROWN).sounds(BlockSoundGroup.CHISELED_BOOKSHELF).burnable()
    );
    public static final ExperienceBlock EXPERIENCE_BLOCK = register(
        "experience_block",
        ExperienceBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.DARK_GREEN).sounds(ExperienceBlock.SOUND).requiresTool().luminance(s -> 15)
    );
    public static final PillarBlock ROSE_QUARTZ_BLOCK = register(
        "rose_quartz_block",
        PillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.AMETHYST_BLOCK).mapColor(MapColor.TERRACOTTA_PINK).requiresTool().sounds(BlockSoundGroup.DEEPSLATE)
    );
    public static final Block ROSE_QUARTZ_TILES = register(
        "rose_quartz_tiles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).mapColor(MapColor.TERRACOTTA_PINK).requiresTool()
    );
    public static final Block SMALL_ROSE_QUARTZ_TILES = register(
        "small_rose_quartz_tiles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).mapColor(MapColor.TERRACOTTA_PINK).requiresTool()
    );
    public static final OxidizableBlock COPPER_SHINGLES = register(
        "copper_shingles",
        settings -> new OxidizableBlock(OxidationLevel.UNAFFECTED, settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final OxidizableBlock EXPOSED_COPPER_SHINGLES = register(
        "exposed_copper_shingles",
        settings -> new OxidizableBlock(OxidationLevel.EXPOSED, settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final OxidizableBlock WEATHERED_COPPER_SHINGLES = register(
        "weathered_copper_shingles",
        settings -> new OxidizableBlock(OxidationLevel.WEATHERED, settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final OxidizableBlock OXIDIZED_COPPER_SHINGLES = register(
        "oxidized_copper_shingles",
        settings -> new OxidizableBlock(OxidationLevel.OXIDIZED, settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final Block WAXED_COPPER_SHINGLES = register("waxed_copper_shingles", Block::new, AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK));
    public static final Block WAXED_EXPOSED_COPPER_SHINGLES = register(
        "waxed_exposed_copper_shingles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final Block WAXED_WEATHERED_COPPER_SHINGLES = register(
        "waxed_weathered_copper_shingles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final Block WAXED_OXIDIZED_COPPER_SHINGLES = register(
        "waxed_oxidized_copper_shingles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final OxidizableSlabBlock COPPER_SHINGLE_SLAB = register(
        "copper_shingle_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.UNAFFECTED, settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final OxidizableSlabBlock EXPOSED_COPPER_SHINGLE_SLAB = register(
        "exposed_copper_shingle_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.EXPOSED, settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final OxidizableSlabBlock WEATHERED_COPPER_SHINGLE_SLAB = register(
        "weathered_copper_shingle_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.WEATHERED, settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final OxidizableSlabBlock OXIDIZED_COPPER_SHINGLE_SLAB = register(
        "oxidized_copper_shingle_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.OXIDIZED, settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final SlabBlock WAXED_COPPER_SHINGLE_SLAB = register(
        "waxed_copper_shingle_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final SlabBlock WAXED_EXPOSED_COPPER_SHINGLE_SLAB = register(
        "waxed_exposed_copper_shingle_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final SlabBlock WAXED_WEATHERED_COPPER_SHINGLE_SLAB = register(
        "waxed_weathered_copper_shingle_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final SlabBlock WAXED_OXIDIZED_COPPER_SHINGLE_SLAB = register(
        "waxed_oxidized_copper_shingle_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final OxidizableStairsBlock COPPER_SHINGLE_STAIRS = register(
        "copper_shingle_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.UNAFFECTED, COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final OxidizableStairsBlock EXPOSED_COPPER_SHINGLE_STAIRS = register(
        "exposed_copper_shingle_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.EXPOSED, EXPOSED_COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final OxidizableStairsBlock WEATHERED_COPPER_SHINGLE_STAIRS = register(
        "weathered_copper_shingle_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.WEATHERED, WEATHERED_COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final OxidizableStairsBlock OXIDIZED_COPPER_SHINGLE_STAIRS = register(
        "oxidized_copper_shingle_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.OXIDIZED, OXIDIZED_COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final StairsBlock WAXED_COPPER_SHINGLE_STAIRS = register(
        "waxed_copper_shingle_stairs",
        settings -> new StairsBlock(COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final StairsBlock WAXED_EXPOSED_COPPER_SHINGLE_STAIRS = register(
        "waxed_exposed_copper_shingle_stairs",
        settings -> new StairsBlock(EXPOSED_COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final StairsBlock WAXED_WEATHERED_COPPER_SHINGLE_STAIRS = register(
        "waxed_weathered_copper_shingle_stairs",
        settings -> new StairsBlock(WEATHERED_COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final StairsBlock WAXED_OXIDIZED_COPPER_SHINGLE_STAIRS = register(
        "waxed_oxidized_copper_shingle_stairs",
        settings -> new StairsBlock(OXIDIZED_COPPER_SHINGLES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final OxidizableBlock COPPER_TILES = register(
        "copper_tiles",
        settings -> new OxidizableBlock(OxidationLevel.UNAFFECTED, settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final OxidizableBlock EXPOSED_COPPER_TILES = register(
        "exposed_copper_tiles",
        settings -> new OxidizableBlock(OxidationLevel.EXPOSED, settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final OxidizableBlock WEATHERED_COPPER_TILES = register(
        "weathered_copper_tiles",
        settings -> new OxidizableBlock(OxidationLevel.WEATHERED, settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final OxidizableBlock OXIDIZED_COPPER_TILES = register(
        "oxidized_copper_tiles",
        settings -> new OxidizableBlock(OxidationLevel.OXIDIZED, settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final Block WAXED_COPPER_TILES = register("waxed_copper_tiles", Block::new, AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK));
    public static final Block WAXED_EXPOSED_COPPER_TILES = register(
        "waxed_exposed_copper_tiles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final Block WAXED_WEATHERED_COPPER_TILES = register(
        "waxed_weathered_copper_tiles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final Block WAXED_OXIDIZED_COPPER_TILES = register(
        "waxed_oxidized_copper_tiles",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final OxidizableSlabBlock COPPER_TILE_SLAB = register(
        "copper_tile_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.UNAFFECTED, settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final OxidizableSlabBlock EXPOSED_COPPER_TILE_SLAB = register(
        "exposed_copper_tile_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.EXPOSED, settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final OxidizableSlabBlock WEATHERED_COPPER_TILE_SLAB = register(
        "weathered_copper_tile_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.WEATHERED, settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final OxidizableSlabBlock OXIDIZED_COPPER_TILE_SLAB = register(
        "oxidized_copper_tile_slab",
        settings -> new OxidizableSlabBlock(OxidationLevel.OXIDIZED, settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final SlabBlock WAXED_COPPER_TILE_SLAB = register(
        "waxed_copper_tile_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final SlabBlock WAXED_EXPOSED_COPPER_TILE_SLAB = register(
        "waxed_exposed_copper_tile_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final SlabBlock WAXED_WEATHERED_COPPER_TILE_SLAB = register(
        "waxed_weathered_copper_tile_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final SlabBlock WAXED_OXIDIZED_COPPER_TILE_SLAB = register(
        "waxed_oxidized_copper_tile_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final OxidizableStairsBlock COPPER_TILE_STAIRS = register(
        "copper_tile_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.UNAFFECTED, COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final OxidizableStairsBlock EXPOSED_COPPER_TILE_STAIRS = register(
        "exposed_copper_tile_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.EXPOSED, EXPOSED_COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final OxidizableStairsBlock WEATHERED_COPPER_TILE_STAIRS = register(
        "weathered_copper_tile_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.WEATHERED, WEATHERED_COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final OxidizableStairsBlock OXIDIZED_COPPER_TILE_STAIRS = register(
        "oxidized_copper_tile_stairs",
        settings -> new OxidizableStairsBlock(OxidationLevel.OXIDIZED, OXIDIZED_COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final StairsBlock WAXED_COPPER_TILE_STAIRS = register(
        "waxed_copper_tile_stairs",
        settings -> new StairsBlock(COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
    );
    public static final StairsBlock WAXED_EXPOSED_COPPER_TILE_STAIRS = register(
        "waxed_exposed_copper_tile_stairs",
        settings -> new StairsBlock(EXPOSED_COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.EXPOSED_COPPER)
    );
    public static final StairsBlock WAXED_WEATHERED_COPPER_TILE_STAIRS = register(
        "waxed_weathered_copper_tile_stairs",
        settings -> new StairsBlock(WEATHERED_COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.WEATHERED_COPPER)
    );
    public static final StairsBlock WAXED_OXIDIZED_COPPER_TILE_STAIRS = register(
        "waxed_oxidized_copper_tile_stairs",
        settings -> new StairsBlock(OXIDIZED_COPPER_TILES.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.OXIDIZED_COPPER)
    );
    public static final TransparentBlock TILED_GLASS = register("tiled_glass", TransparentBlock::new, AbstractBlock.Settings.copy(Blocks.GLASS));
    public static final ConnectedGlassBlock FRAMED_GLASS = register(
        "framed_glass",
        ConnectedGlassBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never).suffocates(AllBlocks::never)
            .blockVision(AllBlocks::never)
    );
    public static final ConnectedGlassBlock HORIZONTAL_FRAMED_GLASS = register(
        "horizontal_framed_glass",
        ConnectedGlassBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never).suffocates(AllBlocks::never)
            .blockVision(AllBlocks::never)
    );
    public static final ConnectedGlassBlock VERTICAL_FRAMED_GLASS = register(
        "vertical_framed_glass",
        ConnectedGlassBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never).suffocates(AllBlocks::never)
            .blockVision(AllBlocks::never)
    );
    public static final GlassPaneBlock TILED_GLASS_PANE = register(
        "tiled_glass_pane",
        GlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock FRAMED_GLASS_PANE = register(
        "framed_glass_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock HORIZONTAL_FRAMED_GLASS_PANE = register(
        "horizontal_framed_glass_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock VERTICAL_FRAMED_GLASS_PANE = register(
        "vertical_framed_glass_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE)
    );
    public static final WindowBlock OAK_WINDOW = register(
        "oak_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.OAK_TAN).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock SPRUCE_WINDOW = register(
        "spruce_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.SPRUCE_BROWN).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock BIRCH_WINDOW = register(
        "birch_window",
        WindowBlock::translucent,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.PALE_YELLOW).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock JUNGLE_WINDOW = register(
        "jungle_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.DIRT_BROWN).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock ACACIA_WINDOW = register(
        "acacia_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.ORANGE).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock DARK_OAK_WINDOW = register(
        "dark_oak_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.BROWN).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock MANGROVE_WINDOW = register(
        "mangrove_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.RED).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock CRIMSON_WINDOW = register(
        "crimson_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.DULL_PINK).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock WARPED_WINDOW = register(
        "warped_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.DARK_AQUA).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock CHERRY_WINDOW = register(
        "cherry_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.TERRACOTTA_WHITE).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final WindowBlock BAMBOO_WINDOW = register(
        "bamboo_window",
        WindowBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS).mapColor(MapColor.YELLOW).allowsSpawning(AllBlocks::never).solidBlock(AllBlocks::never)
            .suffocates(AllBlocks::never).blockVision(AllBlocks::never)
    );
    public static final ConnectedGlassPaneBlock OAK_WINDOW_PANE = register(
        "oak_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.OAK_TAN)
    );
    public static final ConnectedGlassPaneBlock SPRUCE_WINDOW_PANE = register(
        "spruce_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.SPRUCE_BROWN)
    );
    public static final ConnectedGlassPaneBlock BIRCH_WINDOW_PANE = register(
        "birch_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.PALE_YELLOW)
    );
    public static final ConnectedGlassPaneBlock JUNGLE_WINDOW_PANE = register(
        "jungle_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.DIRT_BROWN)
    );
    public static final ConnectedGlassPaneBlock ACACIA_WINDOW_PANE = register(
        "acacia_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.ORANGE)
    );
    public static final ConnectedGlassPaneBlock DARK_OAK_WINDOW_PANE = register(
        "dark_oak_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.BROWN)
    );
    public static final ConnectedGlassPaneBlock MANGROVE_WINDOW_PANE = register(
        "mangrove_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.RED)
    );
    public static final ConnectedGlassPaneBlock CRIMSON_WINDOW_PANE = register(
        "crimson_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.DULL_PINK)
    );
    public static final ConnectedGlassPaneBlock WARPED_WINDOW_PANE = register(
        "warped_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.DARK_AQUA)
    );
    public static final ConnectedGlassPaneBlock CHERRY_WINDOW_PANE = register(
        "cherry_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_WHITE)
    );
    public static final ConnectedGlassPaneBlock BAMBOO_WINDOW_PANE = register(
        "bamboo_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.YELLOW)
    );
    public static final ConnectedGlassPaneBlock ORNATE_IRON_WINDOW_PANE = register(
        "ornate_iron_window_pane",
        ConnectedGlassPaneBlock::new,
        AbstractBlock.Settings.copy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final Block CUT_GRANITE = register("cut_granite", Block::new, AbstractBlock.Settings.copy(Blocks.GRANITE));
    public static final StairsBlock CUT_GRANITE_STAIRS = register(
        "cut_granite_stairs",
        settings -> new StairsBlock(CUT_GRANITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final SlabBlock CUT_GRANITE_SLAB = register("cut_granite_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.GRANITE));
    public static final WallBlock CUT_GRANITE_WALL = register(
        "cut_granite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE).solid()
    );
    public static final Block POLISHED_CUT_GRANITE = register("polished_cut_granite", Block::new, AbstractBlock.Settings.copy(Blocks.GRANITE));
    public static final StairsBlock POLISHED_CUT_GRANITE_STAIRS = register(
        "polished_cut_granite_stairs",
        settings -> new StairsBlock(POLISHED_CUT_GRANITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final SlabBlock POLISHED_CUT_GRANITE_SLAB = register(
        "polished_cut_granite_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final WallBlock POLISHED_CUT_GRANITE_WALL = register(
        "polished_cut_granite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE).solid()
    );
    public static final Block CUT_GRANITE_BRICKS = register("cut_granite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.GRANITE));
    public static final StairsBlock CUT_GRANITE_BRICK_STAIRS = register(
        "cut_granite_brick_stairs",
        settings -> new StairsBlock(CUT_GRANITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final SlabBlock CUT_GRANITE_BRICK_SLAB = register(
        "cut_granite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final WallBlock CUT_GRANITE_BRICK_WALL = register(
        "cut_granite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE).solid()
    );
    public static final Block SMALL_GRANITE_BRICKS = register("small_granite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.GRANITE));
    public static final StairsBlock SMALL_GRANITE_BRICK_STAIRS = register(
        "small_granite_brick_stairs",
        settings -> new StairsBlock(SMALL_GRANITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final SlabBlock SMALL_GRANITE_BRICK_SLAB = register(
        "small_granite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final WallBlock SMALL_GRANITE_BRICK_WALL = register(
        "small_granite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE).solid()
    );
    public static final Block LAYERED_GRANITE = register("layered_granite", Block::new, AbstractBlock.Settings.copy(Blocks.GRANITE));
    public static final ConnectedPillarBlock GRANITE_PILLAR = register(
        "granite_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.GRANITE)
    );
    public static final Block CUT_DIORITE = register("cut_diorite", Block::new, AbstractBlock.Settings.copy(Blocks.DIORITE));
    public static final StairsBlock CUT_DIORITE_STAIRS = register(
        "cut_diorite_stairs",
        settings -> new StairsBlock(CUT_DIORITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final SlabBlock CUT_DIORITE_SLAB = register("cut_diorite_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.DIORITE));
    public static final WallBlock CUT_DIORITE_WALL = register(
        "cut_diorite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE).solid()
    );
    public static final Block POLISHED_CUT_DIORITE = register("polished_cut_diorite", Block::new, AbstractBlock.Settings.copy(Blocks.DIORITE));
    public static final StairsBlock POLISHED_CUT_DIORITE_STAIRS = register(
        "polished_cut_diorite_stairs",
        settings -> new StairsBlock(POLISHED_CUT_DIORITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final SlabBlock POLISHED_CUT_DIORITE_SLAB = register(
        "polished_cut_diorite_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final WallBlock POLISHED_CUT_DIORITE_WALL = register(
        "polished_cut_diorite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE).solid()
    );
    public static final Block CUT_DIORITE_BRICKS = register("cut_diorite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.DIORITE));
    public static final StairsBlock CUT_DIORITE_BRICK_STAIRS = register(
        "cut_diorite_brick_stairs",
        settings -> new StairsBlock(CUT_DIORITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final SlabBlock CUT_DIORITE_BRICK_SLAB = register(
        "cut_diorite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final WallBlock CUT_DIORITE_BRICK_WALL = register(
        "cut_diorite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE).solid()
    );
    public static final Block SMALL_DIORITE_BRICKS = register("small_diorite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.DIORITE));
    public static final StairsBlock SMALL_DIORITE_BRICK_STAIRS = register(
        "small_diorite_brick_stairs",
        settings -> new StairsBlock(SMALL_DIORITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final SlabBlock SMALL_DIORITE_BRICK_SLAB = register(
        "small_diorite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final WallBlock SMALL_DIORITE_BRICK_WALL = register(
        "small_diorite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE).solid()
    );
    public static final Block LAYERED_DIORITE = register("layered_diorite", Block::new, AbstractBlock.Settings.copy(Blocks.DIORITE));
    public static final ConnectedPillarBlock DIORITE_PILLAR = register(
        "diorite_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.DIORITE)
    );
    public static final Block CUT_ANDESITE = register("cut_andesite", Block::new, AbstractBlock.Settings.copy(Blocks.ANDESITE));
    public static final StairsBlock CUT_ANDESITE_STAIRS = register(
        "cut_andesite_stairs",
        settings -> new StairsBlock(CUT_ANDESITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final SlabBlock CUT_ANDESITE_SLAB = register("cut_andesite_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.ANDESITE));
    public static final WallBlock CUT_ANDESITE_WALL = register(
        "cut_andesite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).solid()
    );
    public static final Block POLISHED_CUT_ANDESITE = register("polished_cut_andesite", Block::new, AbstractBlock.Settings.copy(Blocks.ANDESITE));
    public static final StairsBlock POLISHED_CUT_ANDESITE_STAIRS = register(
        "polished_cut_andesite_stairs",
        settings -> new StairsBlock(POLISHED_CUT_ANDESITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final SlabBlock POLISHED_CUT_ANDESITE_SLAB = register(
        "polished_cut_andesite_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final WallBlock POLISHED_CUT_ANDESITE_WALL = register(
        "polished_cut_andesite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).solid()
    );
    public static final Block CUT_ANDESITE_BRICKS = register("cut_andesite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.ANDESITE));
    public static final StairsBlock CUT_ANDESITE_BRICK_STAIRS = register(
        "cut_andesite_brick_stairs",
        settings -> new StairsBlock(CUT_ANDESITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final SlabBlock CUT_ANDESITE_BRICK_SLAB = register(
        "cut_andesite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final WallBlock CUT_ANDESITE_BRICK_WALL = register(
        "cut_andesite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).solid()
    );
    public static final Block SMALL_ANDESITE_BRICKS = register("small_andesite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.ANDESITE));
    public static final StairsBlock SMALL_ANDESITE_BRICK_STAIRS = register(
        "small_andesite_brick_stairs",
        settings -> new StairsBlock(SMALL_ANDESITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final SlabBlock SMALL_ANDESITE_BRICK_SLAB = register(
        "small_andesite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final WallBlock SMALL_ANDESITE_BRICK_WALL = register(
        "small_andesite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE).solid()
    );
    public static final Block LAYERED_ANDESITE = register("layered_andesite", Block::new, AbstractBlock.Settings.copy(Blocks.ANDESITE));
    public static final ConnectedPillarBlock ANDESITE_PILLAR = register(
        "andesite_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.ANDESITE)
    );
    public static final Block CUT_CALCITE = register("cut_calcite", Block::new, AbstractBlock.Settings.copy(Blocks.CALCITE));
    public static final StairsBlock CUT_CALCITE_STAIRS = register(
        "cut_calcite_stairs",
        settings -> new StairsBlock(CUT_CALCITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final SlabBlock CUT_CALCITE_SLAB = register("cut_calcite_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.CALCITE));
    public static final WallBlock CUT_CALCITE_WALL = register(
        "cut_calcite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE).solid()
    );
    public static final Block POLISHED_CUT_CALCITE = register("polished_cut_calcite", Block::new, AbstractBlock.Settings.copy(Blocks.CALCITE));
    public static final StairsBlock POLISHED_CUT_CALCITE_STAIRS = register(
        "polished_cut_calcite_stairs",
        settings -> new StairsBlock(POLISHED_CUT_CALCITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final SlabBlock POLISHED_CUT_CALCITE_SLAB = register(
        "polished_cut_calcite_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final WallBlock POLISHED_CUT_CALCITE_WALL = register(
        "polished_cut_calcite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE).solid()
    );
    public static final Block CUT_CALCITE_BRICKS = register("cut_calcite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.CALCITE));
    public static final StairsBlock CUT_CALCITE_BRICK_STAIRS = register(
        "cut_calcite_brick_stairs",
        settings -> new StairsBlock(CUT_CALCITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final SlabBlock CUT_CALCITE_BRICK_SLAB = register(
        "cut_calcite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final WallBlock CUT_CALCITE_BRICK_WALL = register(
        "cut_calcite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE).solid()
    );
    public static final Block SMALL_CALCITE_BRICKS = register("small_calcite_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.CALCITE));
    public static final StairsBlock SMALL_CALCITE_BRICK_STAIRS = register(
        "small_calcite_brick_stairs",
        settings -> new StairsBlock(SMALL_CALCITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final SlabBlock SMALL_CALCITE_BRICK_SLAB = register(
        "small_calcite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final WallBlock SMALL_CALCITE_BRICK_WALL = register(
        "small_calcite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE).solid()
    );
    public static final Block LAYERED_CALCITE = register("layered_calcite", Block::new, AbstractBlock.Settings.copy(Blocks.CALCITE));
    public static final ConnectedPillarBlock CALCITE_PILLAR = register(
        "calcite_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE)
    );
    public static final Block CUT_DRIPSTONE = register("cut_dripstone", Block::new, AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK));
    public static final StairsBlock CUT_DRIPSTONE_STAIRS = register(
        "cut_dripstone_stairs",
        settings -> new StairsBlock(CUT_DRIPSTONE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock CUT_DRIPSTONE_SLAB = register(
        "cut_dripstone_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock CUT_DRIPSTONE_WALL = register(
        "cut_dripstone_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK).solid()
    );
    public static final Block POLISHED_CUT_DRIPSTONE = register(
        "polished_cut_dripstone",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairsBlock POLISHED_CUT_DRIPSTONE_STAIRS = register(
        "polished_cut_dripstone_stairs",
        settings -> new StairsBlock(POLISHED_CUT_DRIPSTONE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock POLISHED_CUT_DRIPSTONE_SLAB = register(
        "polished_cut_dripstone_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock POLISHED_CUT_DRIPSTONE_WALL = register(
        "polished_cut_dripstone_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK).solid()
    );
    public static final Block CUT_DRIPSTONE_BRICKS = register(
        "cut_dripstone_bricks",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairsBlock CUT_DRIPSTONE_BRICK_STAIRS = register(
        "cut_dripstone_brick_stairs",
        settings -> new StairsBlock(CUT_DRIPSTONE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock CUT_DRIPSTONE_BRICK_SLAB = register(
        "cut_dripstone_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock CUT_DRIPSTONE_BRICK_WALL = register(
        "cut_dripstone_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK).solid()
    );
    public static final Block SMALL_DRIPSTONE_BRICKS = register(
        "small_dripstone_bricks",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairsBlock SMALL_DRIPSTONE_BRICK_STAIRS = register(
        "small_dripstone_brick_stairs",
        settings -> new StairsBlock(SMALL_DRIPSTONE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock SMALL_DRIPSTONE_BRICK_SLAB = register(
        "small_dripstone_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock SMALL_DRIPSTONE_BRICK_WALL = register(
        "small_dripstone_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK).solid()
    );
    public static final Block LAYERED_DRIPSTONE = register("layered_dripstone", Block::new, AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK));
    public static final ConnectedPillarBlock DRIPSTONE_PILLAR = register(
        "dripstone_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final Block CUT_DEEPSLATE = register("cut_deepslate", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
    public static final StairsBlock CUT_DEEPSLATE_STAIRS = register(
        "cut_deepslate_stairs",
        settings -> new StairsBlock(CUT_DEEPSLATE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock CUT_DEEPSLATE_SLAB = register("cut_deepslate_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
    public static final WallBlock CUT_DEEPSLATE_WALL = register(
        "cut_deepslate_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).solid()
    );
    public static final Block POLISHED_CUT_DEEPSLATE = register("polished_cut_deepslate", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
    public static final StairsBlock POLISHED_CUT_DEEPSLATE_STAIRS = register(
        "polished_cut_deepslate_stairs",
        settings -> new StairsBlock(POLISHED_CUT_DEEPSLATE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock POLISHED_CUT_DEEPSLATE_SLAB = register(
        "polished_cut_deepslate_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final WallBlock POLISHED_CUT_DEEPSLATE_WALL = register(
        "polished_cut_deepslate_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).solid()
    );
    public static final Block CUT_DEEPSLATE_BRICKS = register("cut_deepslate_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
    public static final StairsBlock CUT_DEEPSLATE_BRICK_STAIRS = register(
        "cut_deepslate_brick_stairs",
        settings -> new StairsBlock(CUT_DEEPSLATE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock CUT_DEEPSLATE_BRICK_SLAB = register(
        "cut_deepslate_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final WallBlock CUT_DEEPSLATE_BRICK_WALL = register(
        "cut_deepslate_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).solid()
    );
    public static final Block SMALL_DEEPSLATE_BRICKS = register("small_deepslate_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
    public static final StairsBlock SMALL_DEEPSLATE_BRICK_STAIRS = register(
        "small_deepslate_brick_stairs",
        settings -> new StairsBlock(SMALL_DEEPSLATE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock SMALL_DEEPSLATE_BRICK_SLAB = register(
        "small_deepslate_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final WallBlock SMALL_DEEPSLATE_BRICK_WALL = register(
        "small_deepslate_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).solid()
    );
    public static final Block LAYERED_DEEPSLATE = register("layered_deepslate", Block::new, AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
    public static final ConnectedPillarBlock DEEPSLATE_PILLAR = register(
        "deepslate_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
    );
    public static final Block CUT_TUFF = register("cut_tuff", Block::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final StairsBlock CUT_TUFF_STAIRS = register(
        "cut_tuff_stairs",
        settings -> new StairsBlock(CUT_TUFF.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.TUFF)
    );
    public static final SlabBlock CUT_TUFF_SLAB = register("cut_tuff_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final WallBlock CUT_TUFF_WALL = register("cut_tuff_wall", WallBlock::new, AbstractBlock.Settings.copy(Blocks.TUFF).solid());
    public static final Block POLISHED_CUT_TUFF = register("polished_cut_tuff", Block::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final StairsBlock POLISHED_CUT_TUFF_STAIRS = register(
        "polished_cut_tuff_stairs",
        settings -> new StairsBlock(POLISHED_CUT_TUFF.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.TUFF)
    );
    public static final SlabBlock POLISHED_CUT_TUFF_SLAB = register(
        "polished_cut_tuff_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(Blocks.TUFF)
    );
    public static final WallBlock POLISHED_CUT_TUFF_WALL = register(
        "polished_cut_tuff_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.TUFF).solid()
    );
    public static final Block CUT_TUFF_BRICKS = register("cut_tuff_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final StairsBlock CUT_TUFF_BRICK_STAIRS = register(
        "cut_tuff_brick_stairs",
        settings -> new StairsBlock(CUT_TUFF_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.TUFF)
    );
    public static final SlabBlock CUT_TUFF_BRICK_SLAB = register("cut_tuff_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final WallBlock CUT_TUFF_BRICK_WALL = register(
        "cut_tuff_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.TUFF).solid()
    );
    public static final Block SMALL_TUFF_BRICKS = register("small_tuff_bricks", Block::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final StairsBlock SMALL_TUFF_BRICK_STAIRS = register(
        "small_tuff_brick_stairs",
        settings -> new StairsBlock(SMALL_TUFF_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(Blocks.TUFF)
    );
    public static final SlabBlock SMALL_TUFF_BRICK_SLAB = register("small_tuff_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final WallBlock SMALL_TUFF_BRICK_WALL = register(
        "small_tuff_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(Blocks.TUFF).solid()
    );
    public static final Block LAYERED_TUFF = register("layered_tuff", Block::new, AbstractBlock.Settings.copy(Blocks.TUFF));
    public static final ConnectedPillarBlock TUFF_PILLAR = register(
        "tuff_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(Blocks.TUFF)
    );
    public static final Block ASURINE = register(
        "asurine",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).mapColor(MapColor.BLUE).hardness(1.25f)
    );
    public static final Block CUT_ASURINE = register("cut_asurine", Block::new, AbstractBlock.Settings.copy(ASURINE));
    public static final StairsBlock CUT_ASURINE_STAIRS = register(
        "cut_asurine_stairs",
        settings -> new StairsBlock(CUT_ASURINE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final SlabBlock CUT_ASURINE_SLAB = register("cut_asurine_slab", SlabBlock::new, AbstractBlock.Settings.copy(ASURINE));
    public static final WallBlock CUT_ASURINE_WALL = register("cut_asurine_wall", WallBlock::new, AbstractBlock.Settings.copy(ASURINE).solid());
    public static final Block POLISHED_CUT_ASURINE = register("polished_cut_asurine", Block::new, AbstractBlock.Settings.copy(ASURINE));
    public static final StairsBlock POLISHED_CUT_ASURINE_STAIRS = register(
        "polished_cut_asurine_stairs",
        settings -> new StairsBlock(POLISHED_CUT_ASURINE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final SlabBlock POLISHED_CUT_ASURINE_SLAB = register(
        "polished_cut_asurine_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final WallBlock POLISHED_CUT_ASURINE_WALL = register(
        "polished_cut_asurine_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(ASURINE).solid()
    );
    public static final Block CUT_ASURINE_BRICKS = register("cut_asurine_bricks", Block::new, AbstractBlock.Settings.copy(ASURINE));
    public static final StairsBlock CUT_ASURINE_BRICK_STAIRS = register(
        "cut_asurine_brick_stairs",
        settings -> new StairsBlock(CUT_ASURINE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final SlabBlock CUT_ASURINE_BRICK_SLAB = register("cut_asurine_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(ASURINE));
    public static final WallBlock CUT_ASURINE_BRICK_WALL = register(
        "cut_asurine_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(ASURINE).solid()
    );
    public static final Block SMALL_ASURINE_BRICKS = register("small_asurine_bricks", Block::new, AbstractBlock.Settings.copy(ASURINE));
    public static final StairsBlock SMALL_ASURINE_BRICK_STAIRS = register(
        "small_asurine_brick_stairs",
        settings -> new StairsBlock(SMALL_ASURINE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final SlabBlock SMALL_ASURINE_BRICK_SLAB = register(
        "small_asurine_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final WallBlock SMALL_ASURINE_BRICK_WALL = register(
        "small_asurine_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(ASURINE).solid()
    );
    public static final Block LAYERED_ASURINE = register("layered_asurine", Block::new, AbstractBlock.Settings.copy(ASURINE));
    public static final ConnectedPillarBlock ASURINE_PILLAR = register(
        "asurine_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(ASURINE)
    );
    public static final Block CRIMSITE = register(
        "crimsite",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.DEEPSLATE).mapColor(MapColor.RED).hardness(1.25f)
    );
    public static final Block CUT_CRIMSITE = register("cut_crimsite", Block::new, AbstractBlock.Settings.copy(CRIMSITE));
    public static final StairsBlock CUT_CRIMSITE_STAIRS = register(
        "cut_crimsite_stairs",
        settings -> new StairsBlock(CUT_CRIMSITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final SlabBlock CUT_CRIMSITE_SLAB = register("cut_crimsite_slab", SlabBlock::new, AbstractBlock.Settings.copy(CRIMSITE));
    public static final WallBlock CUT_CRIMSITE_WALL = register("cut_crimsite_wall", WallBlock::new, AbstractBlock.Settings.copy(CRIMSITE).solid());
    public static final Block POLISHED_CUT_CRIMSITE = register("polished_cut_crimsite", Block::new, AbstractBlock.Settings.copy(CRIMSITE));
    public static final StairsBlock POLISHED_CUT_CRIMSITE_STAIRS = register(
        "polished_cut_crimsite_stairs",
        settings -> new StairsBlock(POLISHED_CUT_CRIMSITE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final SlabBlock POLISHED_CUT_CRIMSITE_SLAB = register(
        "polished_cut_crimsite_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final WallBlock POLISHED_CUT_CRIMSITE_WALL = register(
        "polished_cut_crimsite_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE).solid()
    );
    public static final Block CUT_CRIMSITE_BRICKS = register("cut_crimsite_bricks", Block::new, AbstractBlock.Settings.copy(CRIMSITE));
    public static final StairsBlock CUT_CRIMSITE_BRICK_STAIRS = register(
        "cut_crimsite_brick_stairs",
        settings -> new StairsBlock(CUT_CRIMSITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final SlabBlock CUT_CRIMSITE_BRICK_SLAB = register(
        "cut_crimsite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final WallBlock CUT_CRIMSITE_BRICK_WALL = register(
        "cut_crimsite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE).solid()
    );
    public static final Block SMALL_CRIMSITE_BRICKS = register("small_crimsite_bricks", Block::new, AbstractBlock.Settings.copy(CRIMSITE));
    public static final StairsBlock SMALL_CRIMSITE_BRICK_STAIRS = register(
        "small_crimsite_brick_stairs",
        settings -> new StairsBlock(SMALL_CRIMSITE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final SlabBlock SMALL_CRIMSITE_BRICK_SLAB = register(
        "small_crimsite_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final WallBlock SMALL_CRIMSITE_BRICK_WALL = register(
        "small_crimsite_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE).solid()
    );
    public static final Block LAYERED_CRIMSITE = register("layered_crimsite", Block::new, AbstractBlock.Settings.copy(CRIMSITE));
    public static final ConnectedPillarBlock CRIMSITE_PILLAR = register(
        "crimsite_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(CRIMSITE)
    );
    public static final Block LIMESTONE = register(
        "limestone",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.SANDSTONE).mapColor(MapColor.PALE_YELLOW).hardness(1.25f)
    );
    public static final Block CUT_LIMESTONE = register("cut_limestone", Block::new, AbstractBlock.Settings.copy(LIMESTONE));
    public static final StairsBlock CUT_LIMESTONE_STAIRS = register(
        "cut_limestone_stairs",
        settings -> new StairsBlock(CUT_LIMESTONE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final SlabBlock CUT_LIMESTONE_SLAB = register("cut_limestone_slab", SlabBlock::new, AbstractBlock.Settings.copy(LIMESTONE));
    public static final WallBlock CUT_LIMESTONE_WALL = register("cut_limestone_wall", WallBlock::new, AbstractBlock.Settings.copy(LIMESTONE).solid());
    public static final Block POLISHED_CUT_LIMESTONE = register("polished_cut_limestone", Block::new, AbstractBlock.Settings.copy(LIMESTONE));
    public static final StairsBlock POLISHED_CUT_LIMESTONE_STAIRS = register(
        "polished_cut_limestone_stairs",
        settings -> new StairsBlock(POLISHED_CUT_LIMESTONE.getDefaultState(), settings),
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final SlabBlock POLISHED_CUT_LIMESTONE_SLAB = register(
        "polished_cut_limestone_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final WallBlock POLISHED_CUT_LIMESTONE_WALL = register(
        "polished_cut_limestone_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE).solid()
    );
    public static final Block CUT_LIMESTONE_BRICKS = register("cut_limestone_bricks", Block::new, AbstractBlock.Settings.copy(LIMESTONE));
    public static final StairsBlock CUT_LIMESTONE_BRICK_STAIRS = register(
        "cut_limestone_brick_stairs",
        settings -> new StairsBlock(CUT_LIMESTONE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final SlabBlock CUT_LIMESTONE_BRICK_SLAB = register(
        "cut_limestone_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final WallBlock CUT_LIMESTONE_BRICK_WALL = register(
        "cut_limestone_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE).solid()
    );
    public static final Block SMALL_LIMESTONE_BRICKS = register("small_limestone_bricks", Block::new, AbstractBlock.Settings.copy(LIMESTONE));
    public static final StairsBlock SMALL_LIMESTONE_BRICK_STAIRS = register(
        "small_limestone_brick_stairs",
        settings -> new StairsBlock(SMALL_LIMESTONE_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final SlabBlock SMALL_LIMESTONE_BRICK_SLAB = register(
        "small_limestone_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final WallBlock SMALL_LIMESTONE_BRICK_WALL = register(
        "small_limestone_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE).solid()
    );
    public static final Block LAYERED_LIMESTONE = register("layered_limestone", Block::new, AbstractBlock.Settings.copy(LIMESTONE));
    public static final ConnectedPillarBlock LIMESTONE_PILLAR = register(
        "limestone_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(LIMESTONE)
    );
    public static final Block OCHRUM = register(
        "ochrum",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.CALCITE).mapColor(MapColor.TERRACOTTA_YELLOW).hardness(1.25f)
    );
    public static final Block CUT_OCHRUM = register("cut_ochrum", Block::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final StairsBlock CUT_OCHRUM_STAIRS = register(
        "cut_ochrum_stairs",
        settings -> new StairsBlock(CUT_OCHRUM.getDefaultState(), settings),
        AbstractBlock.Settings.copy(OCHRUM)
    );
    public static final SlabBlock CUT_OCHRUM_SLAB = register("cut_ochrum_slab", SlabBlock::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final WallBlock CUT_OCHRUM_WALL = register("cut_ochrum_wall", WallBlock::new, AbstractBlock.Settings.copy(OCHRUM).solid());
    public static final Block POLISHED_CUT_OCHRUM = register("polished_cut_ochrum", Block::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final StairsBlock POLISHED_CUT_OCHRUM_STAIRS = register(
        "polished_cut_ochrum_stairs",
        settings -> new StairsBlock(POLISHED_CUT_OCHRUM.getDefaultState(), settings),
        AbstractBlock.Settings.copy(OCHRUM)
    );
    public static final SlabBlock POLISHED_CUT_OCHRUM_SLAB = register(
        "polished_cut_ochrum_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(OCHRUM)
    );
    public static final WallBlock POLISHED_CUT_OCHRUM_WALL = register(
        "polished_cut_ochrum_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(OCHRUM).solid()
    );
    public static final Block CUT_OCHRUM_BRICKS = register("cut_ochrum_bricks", Block::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final StairsBlock CUT_OCHRUM_BRICK_STAIRS = register(
        "cut_ochrum_brick_stairs",
        settings -> new StairsBlock(CUT_OCHRUM_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(OCHRUM)
    );
    public static final SlabBlock CUT_OCHRUM_BRICK_SLAB = register("cut_ochrum_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final WallBlock CUT_OCHRUM_BRICK_WALL = register(
        "cut_ochrum_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(OCHRUM).solid()
    );
    public static final Block SMALL_OCHRUM_BRICKS = register("small_ochrum_bricks", Block::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final StairsBlock SMALL_OCHRUM_BRICK_STAIRS = register(
        "small_ochrum_brick_stairs",
        settings -> new StairsBlock(SMALL_OCHRUM_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(OCHRUM)
    );
    public static final SlabBlock SMALL_OCHRUM_BRICK_SLAB = register("small_ochrum_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final WallBlock SMALL_OCHRUM_BRICK_WALL = register(
        "small_ochrum_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(OCHRUM).solid()
    );
    public static final Block LAYERED_OCHRUM = register("layered_ochrum", Block::new, AbstractBlock.Settings.copy(OCHRUM));
    public static final ConnectedPillarBlock OCHRUM_PILLAR = register(
        "ochrum_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(OCHRUM)
    );
    public static final Block SCORIA = register("scoria", Block::new, AbstractBlock.Settings.copy(Blocks.BLACKSTONE).mapColor(MapColor.BROWN));
    public static final Block CUT_SCORIA = register("cut_scoria", Block::new, AbstractBlock.Settings.copy(SCORIA));
    public static final StairsBlock CUT_SCORIA_STAIRS = register(
        "cut_scoria_stairs",
        settings -> new StairsBlock(CUT_SCORIA.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORIA)
    );
    public static final SlabBlock CUT_SCORIA_SLAB = register("cut_scoria_slab", SlabBlock::new, AbstractBlock.Settings.copy(SCORIA));
    public static final WallBlock CUT_SCORIA_WALL = register("cut_scoria_wall", WallBlock::new, AbstractBlock.Settings.copy(SCORIA).solid());
    public static final Block POLISHED_CUT_SCORIA = register("polished_cut_scoria", Block::new, AbstractBlock.Settings.copy(SCORIA));
    public static final StairsBlock POLISHED_CUT_SCORIA_STAIRS = register(
        "polished_cut_scoria_stairs",
        settings -> new StairsBlock(POLISHED_CUT_SCORIA.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORIA)
    );
    public static final SlabBlock POLISHED_CUT_SCORIA_SLAB = register(
        "polished_cut_scoria_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(SCORIA)
    );
    public static final WallBlock POLISHED_CUT_SCORIA_WALL = register(
        "polished_cut_scoria_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(SCORIA).solid()
    );
    public static final Block CUT_SCORIA_BRICKS = register("cut_scoria_bricks", Block::new, AbstractBlock.Settings.copy(SCORIA));
    public static final StairsBlock CUT_SCORIA_BRICK_STAIRS = register(
        "cut_scoria_brick_stairs",
        settings -> new StairsBlock(CUT_SCORIA_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORIA)
    );
    public static final SlabBlock CUT_SCORIA_BRICK_SLAB = register("cut_scoria_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(SCORIA));
    public static final WallBlock CUT_SCORIA_BRICK_WALL = register(
        "cut_scoria_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(SCORIA).solid()
    );
    public static final Block SMALL_SCORIA_BRICKS = register("small_scoria_bricks", Block::new, AbstractBlock.Settings.copy(SCORIA));
    public static final StairsBlock SMALL_SCORIA_BRICK_STAIRS = register(
        "small_scoria_brick_stairs",
        settings -> new StairsBlock(SMALL_SCORIA_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORIA)
    );
    public static final SlabBlock SMALL_SCORIA_BRICK_SLAB = register("small_scoria_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(SCORIA));
    public static final WallBlock SMALL_SCORIA_BRICK_WALL = register(
        "small_scoria_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(SCORIA).solid()
    );
    public static final Block LAYERED_SCORIA = register("layered_scoria", Block::new, AbstractBlock.Settings.copy(SCORIA));
    public static final ConnectedPillarBlock SCORIA_PILLAR = register(
        "scoria_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(SCORIA)
    );
    public static final Block SCORCHIA = register(
        "scorchia",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.BLACKSTONE).mapColor(MapColor.TERRACOTTA_GRAY).hardness(1.25f)
    );
    public static final Block CUT_SCORCHIA = register("cut_scorchia", Block::new, AbstractBlock.Settings.copy(SCORCHIA));
    public static final StairsBlock CUT_SCORCHIA_STAIRS = register(
        "cut_scorchia_stairs",
        settings -> new StairsBlock(CUT_SCORCHIA.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final SlabBlock CUT_SCORCHIA_SLAB = register("cut_scorchia_slab", SlabBlock::new, AbstractBlock.Settings.copy(SCORCHIA));
    public static final WallBlock CUT_SCORCHIA_WALL = register("cut_scorchia_wall", WallBlock::new, AbstractBlock.Settings.copy(SCORCHIA).solid());
    public static final Block POLISHED_CUT_SCORCHIA = register("polished_cut_scorchia", Block::new, AbstractBlock.Settings.copy(SCORCHIA));
    public static final StairsBlock POLISHED_CUT_SCORCHIA_STAIRS = register(
        "polished_cut_scorchia_stairs",
        settings -> new StairsBlock(POLISHED_CUT_SCORCHIA.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final SlabBlock POLISHED_CUT_SCORCHIA_SLAB = register(
        "polished_cut_scorchia_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final WallBlock POLISHED_CUT_SCORCHIA_WALL = register(
        "polished_cut_scorchia_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA).solid()
    );
    public static final Block CUT_SCORCHIA_BRICKS = register("cut_scorchia_bricks", Block::new, AbstractBlock.Settings.copy(SCORCHIA));
    public static final StairsBlock CUT_SCORCHIA_BRICK_STAIRS = register(
        "cut_scorchia_brick_stairs",
        settings -> new StairsBlock(CUT_SCORCHIA_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final SlabBlock CUT_SCORCHIA_BRICK_SLAB = register(
        "cut_scorchia_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final WallBlock CUT_SCORCHIA_BRICK_WALL = register(
        "cut_scorchia_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA).solid()
    );
    public static final Block SMALL_SCORCHIA_BRICKS = register("small_scorchia_bricks", Block::new, AbstractBlock.Settings.copy(SCORCHIA));
    public static final StairsBlock SMALL_SCORCHIA_BRICK_STAIRS = register(
        "small_scorchia_brick_stairs",
        settings -> new StairsBlock(SMALL_SCORCHIA_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final SlabBlock SMALL_SCORCHIA_BRICK_SLAB = register(
        "small_scorchia_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final WallBlock SMALL_SCORCHIA_BRICK_WALL = register(
        "small_scorchia_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA).solid()
    );
    public static final Block LAYERED_SCORCHIA = register("layered_scorchia", Block::new, AbstractBlock.Settings.copy(SCORCHIA));
    public static final ConnectedPillarBlock SCORCHIA_PILLAR = register(
        "scorchia_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(SCORCHIA)
    );
    public static final Block VERIDIUM = register(
        "veridium",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.TUFF).mapColor(MapColor.TEAL).hardness(1.25f)
    );
    public static final Block CUT_VERIDIUM = register("cut_veridium", Block::new, AbstractBlock.Settings.copy(VERIDIUM));
    public static final StairsBlock CUT_VERIDIUM_STAIRS = register(
        "cut_veridium_stairs",
        settings -> new StairsBlock(CUT_VERIDIUM.getDefaultState(), settings),
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final SlabBlock CUT_VERIDIUM_SLAB = register("cut_veridium_slab", SlabBlock::new, AbstractBlock.Settings.copy(VERIDIUM));
    public static final WallBlock CUT_VERIDIUM_WALL = register("cut_veridium_wall", WallBlock::new, AbstractBlock.Settings.copy(VERIDIUM).solid());
    public static final Block POLISHED_CUT_VERIDIUM = register("polished_cut_veridium", Block::new, AbstractBlock.Settings.copy(VERIDIUM));
    public static final StairsBlock POLISHED_CUT_VERIDIUM_STAIRS = register(
        "polished_cut_veridium_stairs",
        settings -> new StairsBlock(POLISHED_CUT_VERIDIUM.getDefaultState(), settings),
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final SlabBlock POLISHED_CUT_VERIDIUM_SLAB = register(
        "polished_cut_veridium_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final WallBlock POLISHED_CUT_VERIDIUM_WALL = register(
        "polished_cut_veridium_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM).solid()
    );
    public static final Block CUT_VERIDIUM_BRICKS = register("cut_veridium_bricks", Block::new, AbstractBlock.Settings.copy(VERIDIUM));
    public static final StairsBlock CUT_VERIDIUM_BRICK_STAIRS = register(
        "cut_veridium_brick_stairs",
        settings -> new StairsBlock(CUT_VERIDIUM_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final SlabBlock CUT_VERIDIUM_BRICK_SLAB = register(
        "cut_veridium_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final WallBlock CUT_VERIDIUM_BRICK_WALL = register(
        "cut_veridium_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM).solid()
    );
    public static final Block SMALL_VERIDIUM_BRICKS = register("small_veridium_bricks", Block::new, AbstractBlock.Settings.copy(VERIDIUM));
    public static final StairsBlock SMALL_VERIDIUM_BRICK_STAIRS = register(
        "small_veridium_brick_stairs",
        settings -> new StairsBlock(SMALL_VERIDIUM_BRICKS.getDefaultState(), settings),
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final SlabBlock SMALL_VERIDIUM_BRICK_SLAB = register(
        "small_veridium_brick_slab",
        SlabBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final WallBlock SMALL_VERIDIUM_BRICK_WALL = register(
        "small_veridium_brick_wall",
        WallBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM).solid()
    );
    public static final Block LAYERED_VERIDIUM = register("layered_veridium", Block::new, AbstractBlock.Settings.copy(VERIDIUM));
    public static final ConnectedPillarBlock VERIDIUM_PILLAR = register(
        "veridium_pillar",
        ConnectedPillarBlock::new,
        AbstractBlock.Settings.copy(VERIDIUM)
    );
    public static final Block COPYCAT_BASE = register(
        "copycat_base",
        Block::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.LICHEN_GREEN)
    );
    public static final WrenchableDirectionalBlock COPYCAT_BARS = register(
        "copycat_bars",
        WrenchableDirectionalBlock::new,
        AbstractBlock.Settings.create()
    );
    public static final CopycatStepBlock COPYCAT_STEP = register(
        "copycat_step",
        CopycatStepBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).solid().nonOpaque().mapColor(MapColor.CLEAR).allowsSpawning(AllBlocks::never)
    );
    public static final CopycatPanelBlock COPYCAT_PANEL = register(
        "copycat_panel",
        CopycatPanelBlock::new,
        AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque().mapColor(MapColor.CLEAR).allowsSpawning(AllBlocks::never)
    );

    public static final FluidBlock HONEY = register(
        AllFluids.HONEY,
        FluidBlock::new,
        AbstractBlock.Settings.copy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final FluidBlock CHOCOLATE = register(
        AllFluids.CHOCOLATE,
        FluidBlock::new,
        AbstractBlock.Settings.copy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_BROWN)
    );

    private static boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    private static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    private static <T extends FluidBlock> T register(
        FlowableFluid fluid,
        BiFunction<FlowableFluid, AbstractBlock.Settings, T> factory,
        AbstractBlock.Settings settings
    ) {
        T block = register(Registries.FLUID.getId(fluid).getPath(), blockSettings -> factory.apply(fluid, blockSettings), settings);
        fluid.getEntry().block = block;
        return block;
    }

    private static <T extends Block> T register(String id, Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, id));
        T block = factory.apply(settings.registryKey(key));
        ALL.add(block);
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static void register() {
        CStress.setNoImpact(COGWHEEL);
        CStress.setNoImpact(LARGE_COGWHEEL);
        CStress.setNoImpact(SHAFT);
        CStress.setNoImpact(SEQUENCED_GEARSHIFT);
        CStress.setNoImpact(GANTRY_SHAFT);
        CStress.setNoImpact(ROTATION_SPEED_CONTROLLER);
        CStress.setNoImpact(GEARBOX);
        CStress.setNoImpact(BELT);
        CStress.setNoImpact(CLUTCH);
        CStress.setNoImpact(ENCASED_CHAIN_DRIVE);
        CStress.setNoImpact(ADJUSTABLE_CHAIN_GEARSHIFT);
        CStress.setNoImpact(ANDESITE_ENCASED_SHAFT);
        CStress.setNoImpact(BRASS_ENCASED_SHAFT);
        CStress.setNoImpact(ANDESITE_ENCASED_COGWHEEL);
        CStress.setNoImpact(BRASS_ENCASED_COGWHEEL);
        CStress.setNoImpact(ANDESITE_ENCASED_LARGE_COGWHEEL);
        CStress.setNoImpact(BRASS_ENCASED_LARGE_COGWHEEL);
        CStress.setNoImpact(SPEEDOMETER);
        CStress.setNoImpact(STRESSOMETER);
        CStress.setNoImpact(DISPLAY_BOARD);
        CStress.setNoImpact(FLYWHEEL);
        CStress.setImpact(CHAIN_CONVEYOR, 1);
        CStress.setImpact(CUCKOO_CLOCK, 1);
        CStress.setImpact(MYSTERIOUS_CUCKOO_CLOCK, 1);
        CStress.setImpact(MECHANICAL_ARM, 2.0);
        CStress.setImpact(WEIGHTED_EJECTOR, 2.0);
        CStress.setImpact(ENCASED_FAN, 2.0);
        CStress.setImpact(MECHANICAL_CRAFTER, 2.0);
        CStress.setImpact(MECHANICAL_BEARING, 4.0);
        CStress.setImpact(MECHANICAL_PISTON, 4.0);
        CStress.setImpact(STICKY_MECHANICAL_PISTON, 4.0);
        CStress.setImpact(GLASS_FLUID_PIPE, 4.0);
        CStress.setImpact(MECHANICAL_PUMP, 4.0);
        CStress.setImpact(ROPE_PULLEY, 4.0);
        CStress.setImpact(MILLSTONE, 4.0);
        CStress.setImpact(MECHANICAL_SAW, 4.0);
        CStress.setImpact(MECHANICAL_MIXER, 4.0);
        CStress.setImpact(HOSE_PULLEY, 4.0);
        CStress.setImpact(COPPER_BACKTANK, 4.0);
        CStress.setImpact(NETHERITE_BACKTANK, 4.0);
        CStress.setImpact(DEPLOYER, 4.0);
        CStress.setImpact(TURNTABLE, 4.0);
        CStress.setImpact(MECHANICAL_DRILL, 4.0);
        CStress.setImpact(CLOCKWORK_BEARING, 4.0);
        CStress.setImpact(ELEVATOR_PULLEY, 4.0);
        CStress.setImpact(MECHANICAL_PRESS, 8.0);
        CStress.setImpact(CRUSHING_WHEEL, 8.0);
        CStress.setCapacity(HAND_CRANK, 8.0);
        CStress.setCapacity(COPPER_VALVE_HANDLE, 8.0);
        CStress.setCapacity(WHITE_VALVE_HANDLE, 8.0);
        CStress.setCapacity(ORANGE_VALVE_HANDLE, 8.0);
        CStress.setCapacity(MAGENTA_VALVE_HANDLE, 8.0);
        CStress.setCapacity(LIGHT_BLUE_VALVE_HANDLE, 8.0);
        CStress.setCapacity(YELLOW_VALVE_HANDLE, 8.0);
        CStress.setCapacity(LIME_VALVE_HANDLE, 8.0);
        CStress.setCapacity(PINK_VALVE_HANDLE, 8.0);
        CStress.setCapacity(GRAY_VALVE_HANDLE, 8.0);
        CStress.setCapacity(LIGHT_GRAY_VALVE_HANDLE, 8.0);
        CStress.setCapacity(CYAN_VALVE_HANDLE, 8.0);
        CStress.setCapacity(PURPLE_VALVE_HANDLE, 8.0);
        CStress.setCapacity(BLUE_VALVE_HANDLE, 8.0);
        CStress.setCapacity(BROWN_VALVE_HANDLE, 8.0);
        CStress.setCapacity(GREEN_VALVE_HANDLE, 8.0);
        CStress.setCapacity(RED_VALVE_HANDLE, 8.0);
        CStress.setCapacity(BLACK_VALVE_HANDLE, 8.0);
        CStress.setCapacity(WATER_WHEEL, 32);
        CStress.setCapacity(LARGE_WATER_WHEEL, 128.0);
        CStress.setCapacity(WINDMILL_BEARING, 512.0);
        CStress.setCapacity(STEAM_ENGINE, 1024.0);
        CStress.setCapacity(CREATIVE_MOTOR, 16384.0);
    }
}
