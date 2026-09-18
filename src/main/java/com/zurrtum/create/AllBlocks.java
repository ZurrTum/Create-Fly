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
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock.MagnetBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock.RopeBlock;
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
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;
import static net.minecraft.world.level.block.Blocks.register;

@SuppressWarnings({"unchecked", "rawtypes"})
public class AllBlocks {
    public static final CogWheelBlock COGWHEEL = (CogWheelBlock) register(
        AllBlockItemIds.COGWHEEL,
        CogWheelBlock::small,
        Properties.ofFullCopy(Blocks.ANDESITE).sound(SoundType.WOOD).mapColor(MapColor.DIRT)
    );
    public static final CogWheelBlock LARGE_COGWHEEL = (CogWheelBlock) register(
        AllBlockItemIds.LARGE_COGWHEEL,
        CogWheelBlock::large,
        Properties.ofFullCopy(Blocks.ANDESITE).sound(SoundType.WOOD).mapColor(MapColor.DIRT)
    );
    @SuppressWarnings("deprecation")
    public static final ShaftBlock SHAFT = (ShaftBlock) register(
        AllBlockItemIds.SHAFT,
        ShaftBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).forceSolidOff()
    );
    public static final PoweredShaftBlock POWERED_SHAFT = (PoweredShaftBlock) register(
        AllBlockItemIds.POWERED_SHAFT,
        PoweredShaftBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).forceSolidOn()
    );
    public static final GantryShaftBlock GANTRY_SHAFT = (GantryShaftBlock) register(
        AllBlockItemIds.GANTRY_SHAFT,
        GantryShaftBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.NETHER).forceSolidOn()
    );
    public static final SteamEngineBlock STEAM_ENGINE = (SteamEngineBlock) register(
        AllBlockItemIds.STEAM_ENGINE,
        SteamEngineBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.METAL).forceSolidOn()
    );
    public static final SequencedGearshiftBlock SEQUENCED_GEARSHIFT = (SequencedGearshiftBlock) register(
        AllBlockItemIds.SEQUENCED_GEARSHIFT,
        SequencedGearshiftBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final GantryCarriageBlock GANTRY_CARRIAGE = (GantryCarriageBlock) register(
        AllBlockItemIds.GANTRY_CARRIAGE,
        GantryCarriageBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final CreativeMotorBlock CREATIVE_MOTOR = (CreativeMotorBlock) register(
        AllBlockItemIds.CREATIVE_MOTOR,
        CreativeMotorBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_PURPLE).forceSolidOn()
    );
    public static final SpeedControllerBlock ROTATION_SPEED_CONTROLLER = (SpeedControllerBlock) register(
        AllBlockItemIds.ROTATION_SPEED_CONTROLLER,
        SpeedControllerBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final GearboxBlock GEARBOX = (GearboxBlock) register(
        AllBlockItemIds.GEARBOX,
        GearboxBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
            .pushReaction(PushReaction.PUSH_ONLY)
    );
    public static final WaterWheelBlock WATER_WHEEL = (WaterWheelBlock) register(
        AllBlockItemIds.WATER_WHEEL,
        WaterWheelBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).noOcclusion()
    );
    public static final LargeWaterWheelBlock LARGE_WATER_WHEEL = (LargeWaterWheelBlock) register(
        AllBlockItemIds.LARGE_WATER_WHEEL,
        LargeWaterWheelBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).noOcclusion()
    );
    public static final WaterWheelStructuralBlock WATER_WHEEL_STRUCTURAL = (WaterWheelStructuralBlock) register(
        AllBlockItemIds.WATER_WHEEL_STRUCTURAL,
        WaterWheelStructuralBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).noOcclusion()
            .pushReaction(PushReaction.BLOCK)
    );
    public static final CasingBlock ANDESITE_CASING = (CasingBlock) register(
        AllBlockItemIds.ANDESITE_CASING,
        CasingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).sound(SoundType.WOOD)
    );
    public static final CasingBlock BRASS_CASING = (CasingBlock) register(
        AllBlockItemIds.BRASS_CASING,
        CasingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD)
    );
    public static final CasingBlock COPPER_CASING = (CasingBlock) register(
        AllBlockItemIds.COPPER_CASING,
        CasingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).sound(SoundType.COPPER)
    );
    public static final CasingBlock SHADOW_STEEL_CASING = (CasingBlock) register(
        AllBlockItemIds.SHADOW_STEEL_CASING,
        CasingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_BLACK).sound(SoundType.WOOD)
    );
    public static final CasingBlock REFINED_RADIANCE_CASING = (CasingBlock) register(
        AllBlockItemIds.REFINED_RADIANCE_CASING,
        CasingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.SNOW).sound(SoundType.WOOD).lightLevel(_ -> 12)
    );
    public static final CasingBlock RAILWAY_CASING = (CasingBlock) register(
        AllBlockItemIds.RAILWAY_CASING,
        CasingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_CYAN).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final ItemVaultBlock ITEM_VAULT = (ItemVaultBlock) register(
        AllBlockItemIds.ITEM_VAULT,
        ItemVaultBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).sound(SoundType.NETHERITE_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE)
            .explosionResistance(1200)
    );
    public static final ArmBlock MECHANICAL_ARM = (ArmBlock) register(
        AllBlockItemIds.MECHANICAL_ARM,
        ArmBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final DepotBlock DEPOT = (DepotBlock) register(
        AllBlockItemIds.DEPOT,
        DepotBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY)
    );
    public static final BeltBlock BELT = (BeltBlock) register(
        AllBlockItemIds.BELT,
        BeltBlock::new,
        Properties.of().sound(SoundType.WOOL).strength(0.8f).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ClutchBlock CLUTCH = (ClutchBlock) register(
        AllBlockItemIds.CLUTCH,
        ClutchBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final GearshiftBlock GEARSHIFT = (GearshiftBlock) register(
        AllBlockItemIds.GEARSHIFT,
        GearshiftBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final ChainDriveBlock ENCASED_CHAIN_DRIVE = (ChainDriveBlock) register(
        AllBlockItemIds.ENCASED_CHAIN_DRIVE,
        ChainDriveBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final ChainGearshiftBlock ADJUSTABLE_CHAIN_GEARSHIFT = (ChainGearshiftBlock) register(
        AllBlockItemIds.ADJUSTABLE_CHAIN_GEARSHIFT,
        ChainGearshiftBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.NETHER).noOcclusion()
    );
    public static final ChainConveyorBlock CHAIN_CONVEYOR = (ChainConveyorBlock) register(
        AllBlockItemIds.CHAIN_CONVEYOR,
        ChainConveyorBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedShaftBlock ANDESITE_ENCASED_SHAFT = (EncasedShaftBlock) register(
        AllBlockItemIds.ANDESITE_ENCASED_SHAFT,
        EncasedShaftBlock::andesite,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final EncasedShaftBlock BRASS_ENCASED_SHAFT = (EncasedShaftBlock) register(
        AllBlockItemIds.BRASS_ENCASED_SHAFT,
        EncasedShaftBlock::brass,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final EncasedCogwheelBlock ANDESITE_ENCASED_COGWHEEL = (EncasedCogwheelBlock) register(
        AllBlockItemIds.ANDESITE_ENCASED_COGWHEEL,
        p -> new EncasedCogwheelBlock(p, false, ANDESITE_CASING),
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedCogwheelBlock BRASS_ENCASED_COGWHEEL = (EncasedCogwheelBlock) register(
        AllBlockItemIds.BRASS_ENCASED_COGWHEEL,
        p -> new EncasedCogwheelBlock(p, false, BRASS_CASING),
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final EncasedCogwheelBlock ANDESITE_ENCASED_LARGE_COGWHEEL = (EncasedCogwheelBlock) register(
        AllBlockItemIds.ANDESITE_ENCASED_LARGE_COGWHEEL,
        p -> new EncasedCogwheelBlock(p, true, ANDESITE_CASING),
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedCogwheelBlock BRASS_ENCASED_LARGE_COGWHEEL = (EncasedCogwheelBlock) register(
        AllBlockItemIds.BRASS_ENCASED_LARGE_COGWHEEL,
        p -> new EncasedCogwheelBlock(p, true, BRASS_CASING),
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final HandCrankBlock HAND_CRANK = (HandCrankBlock) register(
        AllBlockItemIds.HAND_CRANK,
        HandCrankBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final ValveHandleBlock COPPER_VALVE_HANDLE = (ValveHandleBlock) register(
        AllBlockItemIds.COPPER_VALVE_HANDLE,
        ValveHandleBlock::copper,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final ColorCollection<ValveHandleBlock> VALVE_HANDLE = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.VALVE_HANDLE,
        Blocks::register,
        ValveHandleBlock::new,
        color -> Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(color.getMapColor())
    );
    public static final RadialChassisBlock RADIAL_CHASSIS = (RadialChassisBlock) register(
        AllBlockItemIds.RADIAL_CHASSIS,
        RadialChassisBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT)
    );
    public static final LinearChassisBlock LINEAR_CHASSIS = (LinearChassisBlock) register(
        AllBlockItemIds.LINEAR_CHASSIS,
        LinearChassisBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final LinearChassisBlock SECONDARY_LINEAR_CHASSIS = (LinearChassisBlock) register(
        AllBlockItemIds.SECONDARY_LINEAR_CHASSIS,
        LinearChassisBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final WindmillBearingBlock WINDMILL_BEARING = (WindmillBearingBlock) register(
        AllBlockItemIds.WINDMILL_BEARING,
        WindmillBearingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalBearingBlock MECHANICAL_BEARING = (MechanicalBearingBlock) register(
        AllBlockItemIds.MECHANICAL_BEARING,
        MechanicalBearingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalPistonBlock MECHANICAL_PISTON = (MechanicalPistonBlock) register(
        AllBlockItemIds.MECHANICAL_PISTON,
        MechanicalPistonBlock::normal,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalPistonBlock STICKY_MECHANICAL_PISTON = (MechanicalPistonBlock) register(
        AllBlockItemIds.STICKY_MECHANICAL_PISTON,
        MechanicalPistonBlock::sticky,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalPistonHeadBlock MECHANICAL_PISTON_HEAD = (MechanicalPistonHeadBlock) register(
        AllBlockItemIds.MECHANICAL_PISTON_HEAD,
        MechanicalPistonHeadBlock::new,
        Properties.ofFullCopy(Blocks.PISTON_HEAD)
            .overrideLootTable(Optional.of(ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(MOD_ID, "blocks/mechanical_piston_head")
            ))).mapColor(MapColor.DIRT).pushReaction(PushReaction.NORMAL)
    );
    public static final PistonExtensionPoleBlock PISTON_EXTENSION_POLE = (PistonExtensionPoleBlock) register(
        AllBlockItemIds.PISTON_EXTENSION_POLE,
        PistonExtensionPoleBlock::new,
        Properties.ofFullCopy(Blocks.PISTON_HEAD)
            .overrideLootTable(Optional.of(ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(MOD_ID, "blocks/piston_extension_pole")
            ))).sound(SoundType.SCAFFOLDING).mapColor(MapColor.DIRT).pushReaction(PushReaction.NORMAL).forceSolidOn()
    );
    public static final SailBlock SAIL_FRAME = (SailBlock) register(
        AllBlockItemIds.SAIL_FRAME,
        SailBlock::frame,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).sound(SoundType.SCAFFOLDING)
            .noOcclusion()
    );
    public static final ColorCollection<SailBlock> SAIL = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.SAIL,
        Blocks::register,
        SailBlock::new,
        color -> Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).sound(SoundType.SCAFFOLDING).noOcclusion()
            .bounceRestitution(0.26f).mapColor(color.getMapColor())
    );
    @SuppressWarnings("deprecation")
    public static final FluidPipeBlock FLUID_PIPE = (FluidPipeBlock) register(
        AllBlockItemIds.FLUID_PIPE,
        FluidPipeBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).forceSolidOff()
    );
    public static final EncasedPipeBlock ENCASED_FLUID_PIPE = (EncasedPipeBlock) register(
        AllBlockItemIds.ENCASED_FLUID_PIPE,
        EncasedPipeBlock::copper,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            .noOcclusion()
    );
    public static final GlassFluidPipeBlock GLASS_FLUID_PIPE = (GlassFluidPipeBlock) register(
        AllBlockItemIds.GLASS_FLUID_PIPE,
        GlassFluidPipeBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).noOcclusion()
    );
    public static final PumpBlock MECHANICAL_PUMP = (PumpBlock) register(
        AllBlockItemIds.MECHANICAL_PUMP,
        PumpBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.STONE)
    );
    public static final BlazeBurnerBlock BLAZE_BURNER = (BlazeBurnerBlock) register(
        AllBlockItemIds.BLAZE_BURNER,
        BlazeBurnerBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).lightLevel(BlazeBurnerBlock::getLight)
    );
    public static final LitBlazeBurnerBlock LIT_BLAZE_BURNER = (LitBlazeBurnerBlock) register(
        AllBlockItemIds.LIT_BLAZE_BURNER,
        LitBlazeBurnerBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_LIGHT_GRAY)
            .lightLevel(LitBlazeBurnerBlock::getLight)
    );
    public static final FluidTankBlock FLUID_TANK = (FluidTankBlock) register(
        AllBlockItemIds.FLUID_TANK,
        FluidTankBlock::regular,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).noOcclusion()
            .isRedstoneConductor((_, _, _) -> true).lightLevel(FluidTankBlock::getLight)
    );
    public static final FluidTankBlock CREATIVE_FLUID_TANK = (FluidTankBlock) register(
        AllBlockItemIds.CREATIVE_FLUID_TANK,
        FluidTankBlock::creative,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).noOcclusion()
            .mapColor(MapColor.COLOR_PURPLE)
    );
    public static final MechanicalPressBlock MECHANICAL_PRESS = (MechanicalPressBlock) register(
        AllBlockItemIds.MECHANICAL_PRESS,
        MechanicalPressBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EjectorBlock WEIGHTED_EJECTOR = (EjectorBlock) register(
        AllBlockItemIds.WEIGHTED_EJECTOR,
        EjectorBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).noOcclusion()
    );
    public static final PulleyBlock ROPE_PULLEY = (PulleyBlock) register(
        AllBlockItemIds.ROPE_PULLEY,
        PulleyBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final RopeBlock ROPE = (RopeBlock) register(
        AllBlockItemIds.ROPE,
        RopeBlock::new,
        Properties.of().sound(SoundType.WOOL).mapColor(MapColor.COLOR_BROWN).pushReaction(PushReaction.BLOCK)
    );
    public static final MagnetBlock PULLEY_MAGNET = (MagnetBlock) register(
        AllBlockItemIds.PULLEY_MAGNET,
        MagnetBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).pushReaction(PushReaction.BLOCK)
    );
    public static final MillstoneBlock MILLSTONE = (MillstoneBlock) register(
        AllBlockItemIds.MILLSTONE,
        MillstoneBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL)
    );
    public static final EncasedFanBlock ENCASED_FAN = (EncasedFanBlock) register(
        AllBlockItemIds.ENCASED_FAN,
        EncasedFanBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final PeculiarBellBlock PECULIAR_BELL = (PeculiarBellBlock) register(
        AllBlockItemIds.PECULIAR_BELL,
        PeculiarBellBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.GOLD).sound(SoundType.ANVIL).noOcclusion()
            .forceSolidOn()
    );
    public static final HauntedBellBlock HAUNTED_BELL = (HauntedBellBlock) register(
        AllBlockItemIds.HAUNTED_BELL,
        HauntedBellBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.SAND).sound(SoundType.ANVIL).noOcclusion()
            .forceSolidOn()
    );
    public static final Block INDUSTRIAL_IRON_BLOCK = register(
        AllBlockItemIds.INDUSTRIAL_IRON_BLOCK,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
            .requiresCorrectToolForDrops()
    );
    public static final Block WEATHERED_IRON_BLOCK = register(
        AllBlockItemIds.WEATHERED_IRON_BLOCK,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
            .requiresCorrectToolForDrops()
    );
    public static final WindowBlock INDUSTRIAL_IRON_WINDOW = (WindowBlock) register(
        AllBlockItemIds.INDUSTRIAL_IRON_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
            .isSuffocating(Blocks::never).isViewBlocking(Blocks::never).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ConnectedGlassPaneBlock INDUSTRIAL_IRON_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.INDUSTRIAL_IRON_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_GRAY)
    );
    public static final WindowBlock WEATHERED_IRON_WINDOW = (WindowBlock) register(
        AllBlockItemIds.WEATHERED_IRON_WINDOW,
        WindowBlock::translucent,
        Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
            .isSuffocating(Blocks::never).isViewBlocking(Blocks::never).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final ConnectedGlassPaneBlock WEATHERED_IRON_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.WEATHERED_IRON_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final SawBlock MECHANICAL_SAW = (SawBlock) register(
        AllBlockItemIds.MECHANICAL_SAW,
        SawBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final BasinBlock BASIN = (BasinBlock) register(
        AllBlockItemIds.BASIN,
        BasinBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final AndesiteFunnelBlock ANDESITE_FUNNEL = (AndesiteFunnelBlock) register(
        AllBlockItemIds.ANDESITE_FUNNEL,
        AndesiteFunnelBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE)
    );
    public static final BeltFunnelBlock ANDESITE_BELT_FUNNEL = (BeltFunnelBlock) register(
        AllBlockItemIds.ANDESITE_BELT_FUNNEL,
        BeltFunnelBlock::andesite,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE)
    );
    public static final BrassFunnelBlock BRASS_FUNNEL = (BrassFunnelBlock) register(
        AllBlockItemIds.BRASS_FUNNEL,
        BrassFunnelBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final BeltFunnelBlock BRASS_BELT_FUNNEL = (BeltFunnelBlock) register(
        AllBlockItemIds.BRASS_BELT_FUNNEL,
        BeltFunnelBlock::brass,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final BeltTunnelBlock ANDESITE_TUNNEL = (BeltTunnelBlock) register(
        AllBlockItemIds.ANDESITE_TUNNEL,
        BeltTunnelBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).noOcclusion()
    );
    public static final BrassTunnelBlock BRASS_TUNNEL = (BrassTunnelBlock) register(
        AllBlockItemIds.BRASS_TUNNEL,
        BrassTunnelBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final ChuteBlock CHUTE = (ChuteBlock) register(
        AllBlockItemIds.CHUTE,
        ChuteBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
            .noOcclusion().isSuffocating(Blocks::never)
    );
    public static final SmartChuteBlock SMART_CHUTE = (SmartChuteBlock) register(
        AllBlockItemIds.SMART_CHUTE,
        SmartChuteBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
            .noOcclusion().isSuffocating(Blocks::never).isRedstoneConductor(Blocks::never)
    );
    public static final ControllerRailBlock CONTROLLER_RAIL = (ControllerRailBlock) register(
        AllBlockItemIds.CONTROLLER_RAIL,
        ControllerRailBlock::new,
        Properties.ofFullCopy(Blocks.POWERED_RAIL).mapColor(MapColor.STONE)
    );
    public static final CartAssemblerBlock CART_ASSEMBLER = (CartAssemblerBlock) register(
        AllBlockItemIds.CART_ASSEMBLER,
        CartAssemblerBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).noOcclusion()
            .pushReaction(PushReaction.BLOCK)
    );
    public static final MinecartAnchorBlock MINECART_ANCHOR = (MinecartAnchorBlock) register(
        AllBlockItemIds.MINECART_ANCHOR,
        MinecartAnchorBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final PloughBlock MECHANICAL_PLOUGH = (PloughBlock) register(
        AllBlockItemIds.MECHANICAL_PLOUGH,
        PloughBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).forceSolidOn()
    );
    public static final HarvesterBlock MECHANICAL_HARVESTER = (HarvesterBlock) register(
        AllBlockItemIds.MECHANICAL_HARVESTER,
        HarvesterBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).forceSolidOn()
    );
    public static final PortableStorageInterfaceBlock PORTABLE_FLUID_INTERFACE = (PortableStorageInterfaceBlock) register(
        AllBlockItemIds.PORTABLE_FLUID_INTERFACE,
        PortableStorageInterfaceBlock::forFluids,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final PortableStorageInterfaceBlock PORTABLE_STORAGE_INTERFACE = (PortableStorageInterfaceBlock) register(
        AllBlockItemIds.PORTABLE_STORAGE_INTERFACE,
        PortableStorageInterfaceBlock::forItems,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final GaugeBlock SPEEDOMETER = (GaugeBlock) register(
        AllBlockItemIds.SPEEDOMETER,
        GaugeBlock::speed,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final GaugeBlock STRESSOMETER = (GaugeBlock) register(
        AllBlockItemIds.STRESSOMETER,
        GaugeBlock::stress,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final CuckooClockBlock CUCKOO_CLOCK = (CuckooClockBlock) register(
        AllBlockItemIds.CUCKOO_CLOCK,
        CuckooClockBlock::regular,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final CuckooClockBlock MYSTERIOUS_CUCKOO_CLOCK = (CuckooClockBlock) register(
        AllBlockItemIds.MYSTERIOUS_CUCKOO_CLOCK,
        CuckooClockBlock::mysterious,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final MechanicalMixerBlock MECHANICAL_MIXER = (MechanicalMixerBlock) register(
        AllBlockItemIds.MECHANICAL_MIXER,
        MechanicalMixerBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).noOcclusion()
    );
    public static final HosePulleyBlock HOSE_PULLEY = (HosePulleyBlock) register(
        AllBlockItemIds.HOSE_PULLEY,
        HosePulleyBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.STONE).noOcclusion()
    );
    public static final SpoutBlock SPOUT = (SpoutBlock) register(
        AllBlockItemIds.SPOUT,
        SpoutBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected())
    );
    public static final ItemDrainBlock ITEM_DRAIN = (ItemDrainBlock) register(
        AllBlockItemIds.ITEM_DRAIN,
        ItemDrainBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected())
    );
    public static final WhistleBlock STEAM_WHISTLE = (WhistleBlock) register(
        AllBlockItemIds.STEAM_WHISTLE,
        WhistleBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.GOLD)
    );
    public static final WhistleExtenderBlock STEAM_WHISTLE_EXTENSION = (WhistleExtenderBlock) register(
        AllBlockItemIds.STEAM_WHISTLE_EXTENSION,
        WhistleExtenderBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.GOLD).forceSolidOn()
    );
    public static final BacktankBlock COPPER_BACKTANK = (BacktankBlock) register(
        AllBlockItemIds.COPPER_BACKTANK,
        BacktankBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected())
    );
    public static final BacktankBlock NETHERITE_BACKTANK = (BacktankBlock) register(
        AllBlockItemIds.NETHERITE_BACKTANK,
        BacktankBlock::new,
        Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
    );
    public static final DeployerBlock DEPLOYER = (DeployerBlock) register(
        AllBlockItemIds.DEPLOYER,
        DeployerBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final TurntableBlock TURNTABLE = (TurntableBlock) register(
        AllBlockItemIds.TURNTABLE,
        TurntableBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final DrillBlock MECHANICAL_DRILL = (DrillBlock) register(
        AllBlockItemIds.MECHANICAL_DRILL,
        DrillBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final ClockworkBearingBlock CLOCKWORK_BEARING = (ClockworkBearingBlock) register(
        AllBlockItemIds.CLOCKWORK_BEARING,
        ClockworkBearingBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final CrushingWheelBlock CRUSHING_WHEEL = (CrushingWheelBlock) register(
        AllBlockItemIds.CRUSHING_WHEEL,
        CrushingWheelBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).noOcclusion()
    );
    public static final CrushingWheelControllerBlock CRUSHING_WHEEL_CONTROLLER = (CrushingWheelControllerBlock) register(
        AllBlockItemIds.CRUSHING_WHEEL_CONTROLLER,
        CrushingWheelControllerBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).noLootTable().noCollision()
            .pushReaction(PushReaction.BLOCK)
    );
    public static final Block RAW_ZINC_BLOCK = register(
        AllBlockItemIds.RAW_ZINC_BLOCK,
        Properties.ofFullCopy(Blocks.RAW_GOLD_BLOCK).mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops()
    );
    public static final Block ZINC_BLOCK = register(
        AllBlockItemIds.ZINC_BLOCK,
        Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops()
    );
    public static final Block ZINC_ORE = register(
        AllBlockItemIds.ZINC_ORE,
        Properties.ofFullCopy(Blocks.GOLD_ORE).mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .sound(SoundType.STONE)
    );
    public static final Block DEEPSLATE_ZINC_ORE = register(
        AllBlockItemIds.DEEPSLATE_ZINC_ORE,
        Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE)
    );
    public static final Block BRASS_BLOCK = register(
        AllBlockItemIds.BRASS_BLOCK,
        Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops()
    );
    public static final FlapDisplayBlock DISPLAY_BOARD = (FlapDisplayBlock) register(
        AllBlockItemIds.DISPLAY_BOARD,
        FlapDisplayBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ClipboardBlock CLIPBOARD = (ClipboardBlock) register(
        AllBlockItemIds.CLIPBOARD,
        ClipboardBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).forceSolidOn()
    );
    public static final DisplayLinkBlock DISPLAY_LINK = (DisplayLinkBlock) register(
        AllBlockItemIds.DISPLAY_LINK,
        DisplayLinkBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final ColorCollection<NixieTubeBlock> NIXIE_TUBE = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.NIXIE_TUBE,
        Blocks::register,
        NixieTubeBlock::new,
        color -> Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel(_ -> 5).forceSolidOn()
            .mapColor(color.getMapColor())
    );
    public static final BracketBlock WOODEN_BRACKET = (BracketBlock) register(
        AllBlockItemIds.WOODEN_BRACKET,
        BracketBlock::new,
        Properties.of().sound(SoundType.SCAFFOLDING)
    );
    public static final BracketBlock METAL_BRACKET = (BracketBlock) register(
        AllBlockItemIds.METAL_BRACKET,
        BracketBlock::new,
        Properties.of().sound(SoundType.NETHERITE_BLOCK)
    );
    public static final GirderBlock METAL_GIRDER = (GirderBlock) register(
        AllBlockItemIds.METAL_GIRDER,
        GirderBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final GirderEncasedShaftBlock METAL_GIRDER_ENCASED_SHAFT = (GirderEncasedShaftBlock) register(
        AllBlockItemIds.METAL_GIRDER_ENCASED_SHAFT,
        GirderEncasedShaftBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final FluidValveBlock FLUID_VALVE = (FluidValveBlock) register(
        AllBlockItemIds.FLUID_VALVE,
        FluidValveBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.COLOR_GRAY)
            .sound(SoundType.NETHERITE_BLOCK)
    );
    public static final SmartFluidPipeBlock SMART_FLUID_PIPE = (SmartFluidPipeBlock) register(
        AllBlockItemIds.SMART_FLUID_PIPE,
        SmartFluidPipeBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final AnalogLeverBlock ANALOG_LEVER = (AnalogLeverBlock) register(
        AllBlockItemIds.ANALOG_LEVER,
        AnalogLeverBlock::new,
        Properties.ofFullCopy(Blocks.LEVER)
    );
    public static final RedstoneContactBlock REDSTONE_CONTACT = (RedstoneContactBlock) register(
        AllBlockItemIds.REDSTONE_CONTACT,
        RedstoneContactBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY)
    );
    public static final RedstoneLinkBlock REDSTONE_LINK = (RedstoneLinkBlock) register(
        AllBlockItemIds.REDSTONE_LINK,
        RedstoneLinkBlock::new,
        Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN).forceSolidOn()
    );
    public static final BrassDiodeBlock PULSE_REPEATER = (BrassDiodeBlock) register(
        AllBlockItemIds.PULSE_REPEATER,
        BrassDiodeBlock::new,
        Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final BrassDiodeBlock PULSE_EXTENDER = (BrassDiodeBlock) register(
        AllBlockItemIds.PULSE_EXTENDER,
        BrassDiodeBlock::new,
        Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final BrassDiodeBlock PULSE_TIMER = (BrassDiodeBlock) register(
        AllBlockItemIds.PULSE_TIMER,
        BrassDiodeBlock::new,
        Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final PoweredLatchBlock POWERED_LATCH = (PoweredLatchBlock) register(
        AllBlockItemIds.POWERED_LATCH,
        PoweredLatchBlock::new,
        Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final ToggleLatchBlock POWERED_TOGGLE_LATCH = (ToggleLatchBlock) register(
        AllBlockItemIds.POWERED_TOGGLE_LATCH,
        ToggleLatchBlock::new,
        Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final RoseQuartzLampBlock ROSE_QUARTZ_LAMP = (RoseQuartzLampBlock) register(
        AllBlockItemIds.ROSE_QUARTZ_LAMP,
        RoseQuartzLampBlock::new,
        Properties.ofFullCopy(Blocks.REDSTONE_LAMP).mapColor(MapColor.TERRACOTTA_PINK)
            .lightLevel(state -> state.getValue(RoseQuartzLampBlock.POWERING) ? 15 : 0)
    );
    public static final SmartObserverBlock SMART_OBSERVER = (SmartObserverBlock) register(
        AllBlockItemIds.SMART_OBSERVER,
        SmartObserverBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
            .isRedstoneConductor(Blocks::never)
    );
    public static final ThresholdSwitchBlock THRESHOLD_SWITCH = (ThresholdSwitchBlock) register(
        AllBlockItemIds.THRESHOLD_SWITCH,
        ThresholdSwitchBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
            .isRedstoneConductor(Blocks::never)
    );
    public static final StickerBlock STICKER = (StickerBlock) register(
        AllBlockItemIds.STICKER,
        StickerBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).noOcclusion().bounceRestitution(1)
    );
    public static final ContraptionControlsBlock CONTRAPTION_CONTROLS = (ContraptionControlsBlock) register(
        AllBlockItemIds.CONTRAPTION_CONTROLS,
        ContraptionControlsBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final ElevatorPulleyBlock ELEVATOR_PULLEY = (ElevatorPulleyBlock) register(
        AllBlockItemIds.ELEVATOR_PULLEY,
        ElevatorPulleyBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final ElevatorContactBlock ELEVATOR_CONTACT = (ElevatorContactBlock) register(
        AllBlockItemIds.ELEVATOR_CONTACT,
        ElevatorContactBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
            .lightLevel(ElevatorContactBlock::getLight)
    );
    public static final SlidingDoorBlock ANDESITE_DOOR = (SlidingDoorBlock) register(
        AllBlockItemIds.ANDESITE_DOOR,
        SlidingDoorBlock::stone_fold,
        Properties.ofFullCopy(Blocks.IRON_DOOR).requiresCorrectToolForDrops().strength(3.0F, 6.0F)
            .mapColor(MapColor.STONE).noOcclusion()
    );
    public static final SlidingDoorBlock BRASS_DOOR = (SlidingDoorBlock) register(
        AllBlockItemIds.BRASS_DOOR,
        SlidingDoorBlock::stone_slide,
        Properties.ofFullCopy(Blocks.IRON_DOOR).requiresCorrectToolForDrops().strength(3.0F, 6.0F)
            .mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final SlidingDoorBlock COPPER_DOOR = (SlidingDoorBlock) register(
        AllBlockItemIds.COPPER_DOOR,
        SlidingDoorBlock::stone_fold,
        Properties.ofFullCopy(Blocks.IRON_DOOR).requiresCorrectToolForDrops().strength(3.0F, 6.0F)
            .mapColor(MapColor.COLOR_ORANGE).noOcclusion()
    );
    public static final SlidingDoorBlock TRAIN_DOOR = (SlidingDoorBlock) register(
        AllBlockItemIds.TRAIN_DOOR,
        SlidingDoorBlock::metal_slide,
        Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 6.0F)
            .mapColor(MapColor.TERRACOTTA_CYAN).noOcclusion()
    );
    public static final SlidingDoorBlock FRAMED_GLASS_DOOR = (SlidingDoorBlock) register(
        AllBlockItemIds.FRAMED_GLASS_DOOR,
        SlidingDoorBlock::glass_slide,
        Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 6.0F)
            .mapColor(MapColor.NONE).noOcclusion()
    );
    public static final NozzleBlock NOZZLE = (NozzleBlock) register(
        AllBlockItemIds.NOZZLE,
        NozzleBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_LIGHT_GRAY)
    );
    public static final DeskBellBlock DESK_BELL = (DeskBellBlock) register(
        AllBlockItemIds.DESK_BELL,
        DeskBellBlock::new,
        Properties.of().mapColor(MapColor.SAND)
    );
    public static final MechanicalCrafterBlock MECHANICAL_CRAFTER = (MechanicalCrafterBlock) register(
        AllBlockItemIds.MECHANICAL_CRAFTER,
        MechanicalCrafterBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final CreativeCrateBlock CREATIVE_CRATE = (CreativeCrateBlock) register(
        AllBlockItemIds.CREATIVE_CRATE,
        CreativeCrateBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_PURPLE)
    );
    public static final TrackBlock TRACK = (TrackBlock) register(
        AllBlockItemIds.TRACK,
        TrackBlock::andesite,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).strength(0.8F).sound(SoundType.METAL)
            .noOcclusion().forceSolidOn().pushReaction(PushReaction.BLOCK)
    );
    public static final FakeTrackBlock FAKE_TRACK = (FakeTrackBlock) register(
        AllBlockItemIds.FAKE_TRACK,
        FakeTrackBlock::new,
        Properties.of().mapColor(MapColor.METAL).randomTicks().noCollision().replaceable()
    );
    public static final SignalBlock TRACK_SIGNAL = (SignalBlock) register(
        AllBlockItemIds.TRACK_SIGNAL,
        SignalBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).noOcclusion()
            .sound(SoundType.NETHERITE_BLOCK)
    );
    public static final StandardBogeyBlock SMALL_BOGEY = (StandardBogeyBlock) register(
        AllBlockItemIds.SMALL_BOGEY,
        StandardBogeyBlock::small,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK)
            .noOcclusion()
    );
    public static final StandardBogeyBlock LARGE_BOGEY = (StandardBogeyBlock) register(
        AllBlockItemIds.LARGE_BOGEY,
        StandardBogeyBlock::large,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK)
            .noOcclusion()
    );
    public static final ControlsBlock TRAIN_CONTROLS = (ControlsBlock) register(
        AllBlockItemIds.TRAIN_CONTROLS,
        ControlsBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final StationBlock TRACK_STATION = (StationBlock) register(
        AllBlockItemIds.TRACK_STATION,
        StationBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final TrackObserverBlock TRACK_OBSERVER = (TrackObserverBlock) register(
        AllBlockItemIds.TRACK_OBSERVER,
        TrackObserverBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).noOcclusion()
            .sound(SoundType.NETHERITE_BLOCK)
    );
    public static final ColorCollection<SeatBlock> SEAT = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.SEAT,
        Blocks::register,
        SeatBlock::new,
        color -> Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(color.getMapColor())
            .bounceRestitution(0.66f)
    );
    public static final RollerBlock MECHANICAL_ROLLER = (RollerBlock) register(
        AllBlockItemIds.MECHANICAL_ROLLER,
        RollerBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).noOcclusion()
    );
    public static final LecternControllerBlock LECTERN_CONTROLLER = (LecternControllerBlock) register(
        AllBlockItemIds.LECTERN_CONTROLLER,
        LecternControllerBlock::new,
        Properties.ofFullCopy(Blocks.LECTERN)
    );
    public static final PackagerBlock PACKAGER = (PackagerBlock) register(
        AllBlockItemIds.PACKAGER,
        PackagerBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().isRedstoneConductor(Blocks::never)
            .mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final CardboardBlock CARDBOARD_BLOCK = (CardboardBlock) register(
        AllBlockItemIds.CARDBOARD_BLOCK,
        CardboardBlock::new,
        Properties.ofFullCopy(Blocks.MUSHROOM_STEM).mapColor(MapColor.COLOR_BROWN).sound(SoundType.CHISELED_BOOKSHELF)
            .ignitedByLava()
    );
    public static final PackagerLinkBlock STOCK_LINK = (PackagerLinkBlock) register(
        AllBlockItemIds.STOCK_LINK,
        PackagerLinkBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final RedstoneRequesterBlock REDSTONE_REQUESTER = (RedstoneRequesterBlock) register(
        AllBlockItemIds.REDSTONE_REQUESTER,
        RedstoneRequesterBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
    );
    public static final RepackagerBlock REPACKAGER = (RepackagerBlock) register(
        AllBlockItemIds.REPACKAGER,
        RepackagerBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().isRedstoneConductor(Blocks::never)
            .mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final StockTickerBlock STOCK_TICKER = (StockTickerBlock) register(
        AllBlockItemIds.STOCK_TICKER,
        StockTickerBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).sound(SoundType.GLASS)
    );
    public static final ColorCollection<TableClothBlock> TABLE_CLOTH = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.TABLE_CLOTH,
        Blocks::register,
        TableClothBlock::new,
        color -> Properties.ofFullCopy(Blocks.CARPET.black()).mapColor(color.getMapColor())
    );
    public static final TableClothBlock ANDESITE_TABLE_CLOTH = (TableClothBlock) register(
        AllBlockItemIds.ANDESITE_TABLE_CLOTH,
        TableClothBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()
    );
    public static final TableClothBlock BRASS_TABLE_CLOTH = (TableClothBlock) register(
        AllBlockItemIds.BRASS_TABLE_CLOTH,
        TableClothBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops()
    );
    public static final TableClothBlock COPPER_TABLE_CLOTH = (TableClothBlock) register(
        AllBlockItemIds.COPPER_TABLE_CLOTH,
        TableClothBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).requiresCorrectToolForDrops()
    );
    public static final ColorCollection<PostboxBlock> POSTBOX = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.POSTBOX,
        Blocks::register,
        PostboxBlock::new,
        color -> Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(color.getMapColor())
    );
    public static final FrogportBlock PACKAGE_FROGPORT = (FrogportBlock) register(
        AllBlockItemIds.PACKAGE_FROGPORT,
        FrogportBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.TERRACOTTA_BLUE)
            .sound(SoundType.NETHERITE_BLOCK)
    );
    public static final FactoryPanelBlock FACTORY_GAUGE = (FactoryPanelBlock) register(
        AllBlockItemIds.FACTORY_GAUGE,
        FactoryPanelBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).noOcclusion().forceSolidOn()
    );
    public static final FlywheelBlock FLYWHEEL = (FlywheelBlock) register(
        AllBlockItemIds.FLYWHEEL,
        FlywheelBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final ItemHatchBlock ITEM_HATCH = (ItemHatchBlock) register(
        AllBlockItemIds.ITEM_HATCH,
        ItemHatchBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final PlacardBlock PLACARD = (PlacardBlock) register(
        AllBlockItemIds.PLACARD,
        PlacardBlock::new,
        Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected()).forceSolidOn()
    );
    public static final ColorCollection<ValveHandleBlock> TOOLBOX = (ColorCollection) ColorCollection.registerBlocks(
        AllBlockItemIds.TOOLBOX,
        Blocks::register,
        ToolboxBlock::new,
        color -> Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).forceSolidOn().mapColor(color.getMapColor())
    );
    public static final SchematicTableBlock SCHEMATIC_TABLE = (SchematicTableBlock) register(
        AllBlockItemIds.SCHEMATIC_TABLE,
        SchematicTableBlock::new,
        Properties.ofFullCopy(Blocks.LECTERN).mapColor(MapColor.PODZOL).forceSolidOn().pushReaction(PushReaction.BLOCK)
    );
    public static final SchematicannonBlock SCHEMATICANNON = (SchematicannonBlock) register(
        AllBlockItemIds.SCHEMATICANNON,
        SchematicannonBlock::new,
        Properties.ofFullCopy(Blocks.DISPENSER).mapColor(MapColor.COLOR_GRAY)
    );
    public static final WindowBlock ORNATE_IRON_WINDOW = (WindowBlock) register(
        AllBlockItemIds.ORNATE_IRON_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
            .isSuffocating(Blocks::never).isViewBlocking(Blocks::never).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final MetalLadderBlock ANDESITE_LADDER = (MetalLadderBlock) register(
        AllBlockItemIds.ANDESITE_LADDER,
        MetalLadderBlock::new,
        Properties.ofFullCopy(Blocks.LADDER).mapColor(MapColor.STONE).sound(SoundType.COPPER)
    );
    public static final MetalLadderBlock BRASS_LADDER = (MetalLadderBlock) register(
        AllBlockItemIds.BRASS_LADDER,
        MetalLadderBlock::new,
        Properties.ofFullCopy(Blocks.LADDER).mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.COPPER)
    );
    public static final MetalLadderBlock COPPER_LADDER = (MetalLadderBlock) register(
        AllBlockItemIds.COPPER_LADDER,
        MetalLadderBlock::new,
        Properties.ofFullCopy(Blocks.LADDER).mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER)
    );
    public static final MetalScaffoldingBlock ANDESITE_SCAFFOLD = (MetalScaffoldingBlock) register(
        AllBlockItemIds.ANDESITE_SCAFFOLD,
        MetalScaffoldingBlock::new,
        Properties.ofFullCopy(Blocks.SCAFFOLDING).sound(SoundType.COPPER).mapColor(MapColor.STONE)
    );
    public static final MetalScaffoldingBlock BRASS_SCAFFOLD = (MetalScaffoldingBlock) register(
        AllBlockItemIds.BRASS_SCAFFOLD,
        MetalScaffoldingBlock::new,
        Properties.ofFullCopy(Blocks.SCAFFOLDING).sound(SoundType.COPPER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final MetalScaffoldingBlock COPPER_SCAFFOLD = (MetalScaffoldingBlock) register(
        AllBlockItemIds.COPPER_SCAFFOLD,
        MetalScaffoldingBlock::new,
        Properties.ofFullCopy(Blocks.SCAFFOLDING).sound(SoundType.COPPER).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final IronBarsBlock ANDESITE_BARS = (IronBarsBlock) register(
        AllBlockItemIds.ANDESITE_BARS,
        IronBarsBlock::new,
        Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.COPPER).mapColor(MapColor.STONE)
    );
    public static final IronBarsBlock BRASS_BARS = (IronBarsBlock) register(
        AllBlockItemIds.BRASS_BARS,
        IronBarsBlock::new,
        Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.COPPER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final IronBarsBlock COPPER_BARS = (IronBarsBlock) register(
        AllBlockItemIds.COPPER_BARS,
        IronBarsBlock::new,
        Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.COPPER).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final TrainTrapdoorBlock TRAIN_TRAPDOOR = (TrainTrapdoorBlock) register(
        AllBlockItemIds.TRAIN_TRAPDOOR,
        TrainTrapdoorBlock::metal,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_CYAN)
    );
    public static final TrainTrapdoorBlock FRAMED_GLASS_TRAPDOOR = (TrainTrapdoorBlock) register(
        AllBlockItemIds.FRAMED_GLASS_TRAPDOOR,
        TrainTrapdoorBlock::metal,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.NONE).noOcclusion()
    );
    public static final Block ANDESITE_ALLOY_BLOCK = register(
        AllBlockItemIds.ANDESITE_ALLOY_BLOCK,
        Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()
    );
    public static final CardboardBlock BOUND_CARDBOARD_BLOCK = (CardboardBlock) register(
        AllBlockItemIds.BOUND_CARDBOARD_BLOCK,
        CardboardBlock::new,
        Properties.ofFullCopy(Blocks.MUSHROOM_STEM).mapColor(MapColor.COLOR_BROWN).sound(SoundType.CHISELED_BOOKSHELF)
            .ignitedByLava()
    );
    public static final ExperienceBlock EXPERIENCE_BLOCK = (ExperienceBlock) register(
        AllBlockItemIds.EXPERIENCE_BLOCK,
        ExperienceBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PLANT).sound(ExperienceBlock.SOUND)
            .requiresCorrectToolForDrops().lightLevel(_ -> 15)
    );
    public static final RotatedPillarBlock ROSE_QUARTZ_BLOCK = (RotatedPillarBlock) register(
        AllBlockItemIds.ROSE_QUARTZ_BLOCK,
        RotatedPillarBlock::new,
        Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE)
    );
    public static final Block ROSE_QUARTZ_TILES = register(
        AllBlockItemIds.ROSE_QUARTZ_TILES,
        Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops()
    );
    public static final Block SMALL_ROSE_QUARTZ_TILES = register(
        AllBlockItemIds.SMALL_ROSE_QUARTZ_TILES,
        Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops()
    );
    public static final WeatheringCopperCollection<Block> COPPER_SHINGLES = WeatheringCopperCollection.registerBlocks(
        AllBlockItemIds.COPPER_SHINGLES,
        Blocks::register,
        AllBlocks::createBlockIgnoreState,
        WeatheringCopperFullBlock::new,
        copied(Blocks.COPPER_BLOCK.weathering())
    );
    public static final WeatheringCopperCollection<Block> COPPER_SHINGLE_SLAB = WeatheringCopperCollection.registerBlocks(
        AllBlockItemIds.COPPER_SHINGLE_SLAB,
        Blocks::register,
        createIgnoreState(SlabBlock::new),
        WeatheringCopperSlabBlock::new,
        copied(Blocks.COPPER_BLOCK.weathering())
    );
    public static final WeatheringCopperCollection<Block> COPPER_SHINGLE_STAIRS = WeatheringCopperCollection.registerBlocks(
        AllBlockItemIds.COPPER_SHINGLE_STAIRS,
        Blocks::register,
        create(COPPER_SHINGLES.waxed(), StairBlock::new),
        create(COPPER_SHINGLES.weathering(), WeatheringCopperStairBlock::new),
        copied(Blocks.COPPER_BLOCK.weathering())
    );
    public static final WeatheringCopperCollection<Block> COPPER_TILES = WeatheringCopperCollection.registerBlocks(
        AllBlockItemIds.COPPER_TILES,
        Blocks::register,
        AllBlocks::createBlockIgnoreState,
        WeatheringCopperFullBlock::new,
        copied(Blocks.COPPER_BLOCK.weathering())
    );
    public static final WeatheringCopperCollection<Block> COPPER_TILE_SLAB = WeatheringCopperCollection.registerBlocks(
        AllBlockItemIds.COPPER_TILE_SLAB,
        Blocks::register,
        createIgnoreState(SlabBlock::new),
        WeatheringCopperSlabBlock::new,
        copied(Blocks.COPPER_BLOCK.weathering())
    );
    public static final WeatheringCopperCollection<Block> COPPER_TILE_STAIRS = WeatheringCopperCollection.registerBlocks(
        AllBlockItemIds.COPPER_TILE_STAIRS,
        Blocks::register,
        create(COPPER_TILES.waxed(), StairBlock::new),
        create(COPPER_TILES.weathering(), WeatheringCopperStairBlock::new),
        copied(Blocks.COPPER_BLOCK.weathering())
    );
    public static final TransparentBlock TILED_GLASS = (TransparentBlock) register(
        AllBlockItemIds.TILED_GLASS,
        TransparentBlock::new,
        Properties.ofFullCopy(Blocks.GLASS)
    );
    public static final ConnectedGlassBlock FRAMED_GLASS = (ConnectedGlassBlock) register(
        AllBlockItemIds.FRAMED_GLASS,
        ConnectedGlassBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
            .isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final ConnectedGlassBlock HORIZONTAL_FRAMED_GLASS = (ConnectedGlassBlock) register(
        AllBlockItemIds.HORIZONTAL_FRAMED_GLASS,
        ConnectedGlassBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
            .isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final ConnectedGlassBlock VERTICAL_FRAMED_GLASS = (ConnectedGlassBlock) register(
        AllBlockItemIds.VERTICAL_FRAMED_GLASS,
        ConnectedGlassBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
            .isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final GlassPaneBlock TILED_GLASS_PANE = (GlassPaneBlock) register(
        AllBlockItemIds.TILED_GLASS_PANE,
        GlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock FRAMED_GLASS_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.FRAMED_GLASS_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock HORIZONTAL_FRAMED_GLASS_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.HORIZONTAL_FRAMED_GLASS_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock VERTICAL_FRAMED_GLASS_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.VERTICAL_FRAMED_GLASS_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final WindowBlock OAK_WINDOW = (WindowBlock) register(
        AllBlockItemIds.OAK_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.WOOD).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock SPRUCE_WINDOW = (WindowBlock) register(
        AllBlockItemIds.SPRUCE_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.PODZOL).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock BIRCH_WINDOW = (WindowBlock) register(
        AllBlockItemIds.BIRCH_WINDOW,
        WindowBlock::translucent,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.SAND).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock JUNGLE_WINDOW = (WindowBlock) register(
        AllBlockItemIds.JUNGLE_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.DIRT).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock ACACIA_WINDOW = (WindowBlock) register(
        AllBlockItemIds.ACACIA_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock DARK_OAK_WINDOW = (WindowBlock) register(
        AllBlockItemIds.DARK_OAK_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock MANGROVE_WINDOW = (WindowBlock) register(
        AllBlockItemIds.MANGROVE_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_RED).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock CRIMSON_WINDOW = (WindowBlock) register(
        AllBlockItemIds.CRIMSON_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.CRIMSON_STEM).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock WARPED_WINDOW = (WindowBlock) register(
        AllBlockItemIds.WARPED_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.WARPED_STEM).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock CHERRY_WINDOW = (WindowBlock) register(
        AllBlockItemIds.CHERRY_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.TERRACOTTA_WHITE).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final WindowBlock BAMBOO_WINDOW = (WindowBlock) register(
        AllBlockItemIds.BAMBOO_WINDOW,
        WindowBlock::new,
        Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).isValidSpawn(Blocks::never)
            .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
    );
    public static final ConnectedGlassPaneBlock OAK_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.OAK_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.WOOD)
    );
    public static final ConnectedGlassPaneBlock SPRUCE_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.SPRUCE_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.PODZOL)
    );
    public static final ConnectedGlassPaneBlock BIRCH_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.BIRCH_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.SAND)
    );
    public static final ConnectedGlassPaneBlock JUNGLE_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.JUNGLE_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.DIRT)
    );
    public static final ConnectedGlassPaneBlock ACACIA_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.ACACIA_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final ConnectedGlassPaneBlock DARK_OAK_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.DARK_OAK_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_BROWN)
    );
    public static final ConnectedGlassPaneBlock MANGROVE_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.MANGROVE_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_RED)
    );
    public static final ConnectedGlassPaneBlock CRIMSON_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.CRIMSON_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.CRIMSON_STEM)
    );
    public static final ConnectedGlassPaneBlock WARPED_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.WARPED_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.WARPED_STEM)
    );
    public static final ConnectedGlassPaneBlock CHERRY_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.CHERRY_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_WHITE)
    );
    public static final ConnectedGlassPaneBlock BAMBOO_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.BAMBOO_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_YELLOW)
    );
    public static final ConnectedGlassPaneBlock ORNATE_IRON_WINDOW_PANE = (ConnectedGlassPaneBlock) register(
        AllBlockItemIds.ORNATE_IRON_WINDOW_PANE,
        ConnectedGlassPaneBlock::new,
        Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final Block CUT_GRANITE = register(
        AllBlockItemIds.CUT_GRANITE,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final StairBlock CUT_GRANITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_GRANITE_STAIRS,
        settings -> new StairBlock(CUT_GRANITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock CUT_GRANITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_GRANITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock CUT_GRANITE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_GRANITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_GRANITE = register(
        AllBlockItemIds.POLISHED_CUT_GRANITE,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final StairBlock POLISHED_CUT_GRANITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_GRANITE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_GRANITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock POLISHED_CUT_GRANITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_GRANITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock POLISHED_CUT_GRANITE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_GRANITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block CUT_GRANITE_BRICKS = register(
        AllBlockItemIds.CUT_GRANITE_BRICKS,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final StairBlock CUT_GRANITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_GRANITE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_GRANITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock CUT_GRANITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_GRANITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock CUT_GRANITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_GRANITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block SMALL_GRANITE_BRICKS = register(
        AllBlockItemIds.SMALL_GRANITE_BRICKS,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final StairBlock SMALL_GRANITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_GRANITE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_GRANITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock SMALL_GRANITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_GRANITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock SMALL_GRANITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_GRANITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block LAYERED_GRANITE = register(
        AllBlockItemIds.LAYERED_GRANITE,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final ConnectedPillarBlock GRANITE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.GRANITE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final Block CUT_DIORITE = register(
        AllBlockItemIds.CUT_DIORITE,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final StairBlock CUT_DIORITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_DIORITE_STAIRS,
        settings -> new StairBlock(CUT_DIORITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock CUT_DIORITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_DIORITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock CUT_DIORITE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_DIORITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_DIORITE = register(
        AllBlockItemIds.POLISHED_CUT_DIORITE,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final StairBlock POLISHED_CUT_DIORITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_DIORITE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_DIORITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock POLISHED_CUT_DIORITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_DIORITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock POLISHED_CUT_DIORITE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_DIORITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block CUT_DIORITE_BRICKS = register(
        AllBlockItemIds.CUT_DIORITE_BRICKS,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final StairBlock CUT_DIORITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_DIORITE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_DIORITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock CUT_DIORITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_DIORITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock CUT_DIORITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_DIORITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block SMALL_DIORITE_BRICKS = register(
        AllBlockItemIds.SMALL_DIORITE_BRICKS,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final StairBlock SMALL_DIORITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_DIORITE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_DIORITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock SMALL_DIORITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_DIORITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock SMALL_DIORITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_DIORITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block LAYERED_DIORITE = register(
        AllBlockItemIds.LAYERED_DIORITE,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final ConnectedPillarBlock DIORITE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.DIORITE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final Block CUT_ANDESITE = register(
        AllBlockItemIds.CUT_ANDESITE,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock CUT_ANDESITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_ANDESITE_STAIRS,
        settings -> new StairBlock(CUT_ANDESITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock CUT_ANDESITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_ANDESITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock CUT_ANDESITE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_ANDESITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_ANDESITE = register(
        AllBlockItemIds.POLISHED_CUT_ANDESITE,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock POLISHED_CUT_ANDESITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_ANDESITE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_ANDESITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock POLISHED_CUT_ANDESITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_ANDESITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock POLISHED_CUT_ANDESITE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_ANDESITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block CUT_ANDESITE_BRICKS = register(
        AllBlockItemIds.CUT_ANDESITE_BRICKS,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock CUT_ANDESITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_ANDESITE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_ANDESITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock CUT_ANDESITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_ANDESITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock CUT_ANDESITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_ANDESITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block SMALL_ANDESITE_BRICKS = register(
        AllBlockItemIds.SMALL_ANDESITE_BRICKS,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock SMALL_ANDESITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_ANDESITE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_ANDESITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock SMALL_ANDESITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_ANDESITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock SMALL_ANDESITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_ANDESITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block LAYERED_ANDESITE = register(
        AllBlockItemIds.LAYERED_ANDESITE,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final ConnectedPillarBlock ANDESITE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.ANDESITE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final Block CUT_CALCITE = register(
        AllBlockItemIds.CUT_CALCITE,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final StairBlock CUT_CALCITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_CALCITE_STAIRS,
        settings -> new StairBlock(CUT_CALCITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock CUT_CALCITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_CALCITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock CUT_CALCITE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_CALCITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_CALCITE = register(
        AllBlockItemIds.POLISHED_CUT_CALCITE,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final StairBlock POLISHED_CUT_CALCITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_CALCITE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_CALCITE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock POLISHED_CUT_CALCITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_CALCITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock POLISHED_CUT_CALCITE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_CALCITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block CUT_CALCITE_BRICKS = register(
        AllBlockItemIds.CUT_CALCITE_BRICKS,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final StairBlock CUT_CALCITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_CALCITE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_CALCITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock CUT_CALCITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_CALCITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock CUT_CALCITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_CALCITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block SMALL_CALCITE_BRICKS = register(
        AllBlockItemIds.SMALL_CALCITE_BRICKS,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final StairBlock SMALL_CALCITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_CALCITE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_CALCITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock SMALL_CALCITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_CALCITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock SMALL_CALCITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_CALCITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block LAYERED_CALCITE = register(
        AllBlockItemIds.LAYERED_CALCITE,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final ConnectedPillarBlock CALCITE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.CALCITE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final Block CUT_DRIPSTONE = register(
        AllBlockItemIds.CUT_DRIPSTONE,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock CUT_DRIPSTONE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_DRIPSTONE_STAIRS,
        settings -> new StairBlock(CUT_DRIPSTONE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock CUT_DRIPSTONE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_DRIPSTONE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock CUT_DRIPSTONE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_DRIPSTONE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block POLISHED_CUT_DRIPSTONE = register(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock POLISHED_CUT_DRIPSTONE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_DRIPSTONE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock POLISHED_CUT_DRIPSTONE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock POLISHED_CUT_DRIPSTONE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block CUT_DRIPSTONE_BRICKS = register(
        AllBlockItemIds.CUT_DRIPSTONE_BRICKS,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock CUT_DRIPSTONE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_DRIPSTONE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_DRIPSTONE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock CUT_DRIPSTONE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_DRIPSTONE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock CUT_DRIPSTONE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_DRIPSTONE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block SMALL_DRIPSTONE_BRICKS = register(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICKS,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock SMALL_DRIPSTONE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_DRIPSTONE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock SMALL_DRIPSTONE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock SMALL_DRIPSTONE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block LAYERED_DRIPSTONE = register(
        AllBlockItemIds.LAYERED_DRIPSTONE,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final ConnectedPillarBlock DRIPSTONE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.DRIPSTONE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final Block CUT_DEEPSLATE = register(
        AllBlockItemIds.CUT_DEEPSLATE,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock CUT_DEEPSLATE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_DEEPSLATE_STAIRS,
        settings -> new StairBlock(CUT_DEEPSLATE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock CUT_DEEPSLATE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_DEEPSLATE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock CUT_DEEPSLATE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_DEEPSLATE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_DEEPSLATE = register(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock POLISHED_CUT_DEEPSLATE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_DEEPSLATE.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock POLISHED_CUT_DEEPSLATE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock POLISHED_CUT_DEEPSLATE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block CUT_DEEPSLATE_BRICKS = register(
        AllBlockItemIds.CUT_DEEPSLATE_BRICKS,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock CUT_DEEPSLATE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_DEEPSLATE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_DEEPSLATE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock CUT_DEEPSLATE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_DEEPSLATE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock CUT_DEEPSLATE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_DEEPSLATE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block SMALL_DEEPSLATE_BRICKS = register(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICKS,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock SMALL_DEEPSLATE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_DEEPSLATE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock SMALL_DEEPSLATE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock SMALL_DEEPSLATE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block LAYERED_DEEPSLATE = register(
        AllBlockItemIds.LAYERED_DEEPSLATE,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final ConnectedPillarBlock DEEPSLATE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.DEEPSLATE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final Block CUT_TUFF = register(AllBlockItemIds.CUT_TUFF, Properties.ofFullCopy(Blocks.TUFF));
    public static final StairBlock CUT_TUFF_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_TUFF_STAIRS,
        settings -> new StairBlock(CUT_TUFF.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock CUT_TUFF_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_TUFF_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock CUT_TUFF_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_TUFF_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block POLISHED_CUT_TUFF = register(
        AllBlockItemIds.POLISHED_CUT_TUFF,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final StairBlock POLISHED_CUT_TUFF_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_TUFF_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_TUFF.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock POLISHED_CUT_TUFF_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_TUFF_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock POLISHED_CUT_TUFF_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_TUFF_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block CUT_TUFF_BRICKS = register(
        AllBlockItemIds.CUT_TUFF_BRICKS,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final StairBlock CUT_TUFF_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_TUFF_BRICK_STAIRS,
        settings -> new StairBlock(CUT_TUFF_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock CUT_TUFF_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_TUFF_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock CUT_TUFF_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_TUFF_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block SMALL_TUFF_BRICKS = register(
        AllBlockItemIds.SMALL_TUFF_BRICKS,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final StairBlock SMALL_TUFF_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_TUFF_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_TUFF_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock SMALL_TUFF_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_TUFF_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock SMALL_TUFF_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_TUFF_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block LAYERED_TUFF = register(AllBlockItemIds.LAYERED_TUFF, Properties.ofFullCopy(Blocks.TUFF));
    public static final ConnectedPillarBlock TUFF_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.TUFF_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final Block ASURINE = register(
        AllBlockItemIds.ASURINE,
        Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLUE).destroyTime(1.25f)
    );
    public static final Block CUT_ASURINE = register(AllBlockItemIds.CUT_ASURINE, Properties.ofFullCopy(ASURINE));
    public static final StairBlock CUT_ASURINE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_ASURINE_STAIRS,
        settings -> new StairBlock(CUT_ASURINE.defaultBlockState(), settings),
        Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock CUT_ASURINE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_ASURINE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock CUT_ASURINE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_ASURINE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_ASURINE = register(
        AllBlockItemIds.POLISHED_CUT_ASURINE,
        Properties.ofFullCopy(ASURINE)
    );
    public static final StairBlock POLISHED_CUT_ASURINE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_ASURINE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_ASURINE.defaultBlockState(), settings),
        Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock POLISHED_CUT_ASURINE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_ASURINE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock POLISHED_CUT_ASURINE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_ASURINE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block CUT_ASURINE_BRICKS = register(
        AllBlockItemIds.CUT_ASURINE_BRICKS,
        Properties.ofFullCopy(ASURINE)
    );
    public static final StairBlock CUT_ASURINE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_ASURINE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_ASURINE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock CUT_ASURINE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_ASURINE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock CUT_ASURINE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_ASURINE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block SMALL_ASURINE_BRICKS = register(
        AllBlockItemIds.SMALL_ASURINE_BRICKS,
        Properties.ofFullCopy(ASURINE)
    );
    public static final StairBlock SMALL_ASURINE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_ASURINE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_ASURINE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock SMALL_ASURINE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_ASURINE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock SMALL_ASURINE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_ASURINE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block LAYERED_ASURINE = register(
        AllBlockItemIds.LAYERED_ASURINE,
        Properties.ofFullCopy(ASURINE)
    );
    public static final ConnectedPillarBlock ASURINE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.ASURINE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(ASURINE)
    );
    public static final Block CRIMSITE = register(
        AllBlockItemIds.CRIMSITE,
        Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_RED).destroyTime(1.25f)
    );
    public static final Block CUT_CRIMSITE = register(AllBlockItemIds.CUT_CRIMSITE, Properties.ofFullCopy(CRIMSITE));
    public static final StairBlock CUT_CRIMSITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_CRIMSITE_STAIRS,
        settings -> new StairBlock(CUT_CRIMSITE.defaultBlockState(), settings),
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock CUT_CRIMSITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_CRIMSITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock CUT_CRIMSITE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_CRIMSITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_CRIMSITE = register(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final StairBlock POLISHED_CUT_CRIMSITE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_CRIMSITE.defaultBlockState(), settings),
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock POLISHED_CUT_CRIMSITE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock POLISHED_CUT_CRIMSITE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block CUT_CRIMSITE_BRICKS = register(
        AllBlockItemIds.CUT_CRIMSITE_BRICKS,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final StairBlock CUT_CRIMSITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_CRIMSITE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_CRIMSITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock CUT_CRIMSITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_CRIMSITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock CUT_CRIMSITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_CRIMSITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block SMALL_CRIMSITE_BRICKS = register(
        AllBlockItemIds.SMALL_CRIMSITE_BRICKS,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final StairBlock SMALL_CRIMSITE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_CRIMSITE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_CRIMSITE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock SMALL_CRIMSITE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_CRIMSITE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock SMALL_CRIMSITE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_CRIMSITE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block LAYERED_CRIMSITE = register(
        AllBlockItemIds.LAYERED_CRIMSITE,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final ConnectedPillarBlock CRIMSITE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.CRIMSITE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(CRIMSITE)
    );
    public static final Block LIMESTONE = register(
        AllBlockItemIds.LIMESTONE,
        Properties.ofFullCopy(Blocks.SANDSTONE).mapColor(MapColor.SAND).destroyTime(1.25f)
    );
    public static final Block CUT_LIMESTONE = register(AllBlockItemIds.CUT_LIMESTONE, Properties.ofFullCopy(LIMESTONE));
    public static final StairBlock CUT_LIMESTONE_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_LIMESTONE_STAIRS,
        settings -> new StairBlock(CUT_LIMESTONE.defaultBlockState(), settings),
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock CUT_LIMESTONE_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_LIMESTONE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock CUT_LIMESTONE_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_LIMESTONE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_LIMESTONE = register(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final StairBlock POLISHED_CUT_LIMESTONE_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_LIMESTONE.defaultBlockState(), settings),
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock POLISHED_CUT_LIMESTONE_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock POLISHED_CUT_LIMESTONE_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE_WALL,
        WallBlock::new,
        Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block CUT_LIMESTONE_BRICKS = register(
        AllBlockItemIds.CUT_LIMESTONE_BRICKS,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final StairBlock CUT_LIMESTONE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_LIMESTONE_BRICK_STAIRS,
        settings -> new StairBlock(CUT_LIMESTONE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock CUT_LIMESTONE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_LIMESTONE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock CUT_LIMESTONE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_LIMESTONE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block SMALL_LIMESTONE_BRICKS = register(
        AllBlockItemIds.SMALL_LIMESTONE_BRICKS,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final StairBlock SMALL_LIMESTONE_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_LIMESTONE_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_LIMESTONE_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock SMALL_LIMESTONE_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_LIMESTONE_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock SMALL_LIMESTONE_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_LIMESTONE_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block LAYERED_LIMESTONE = register(
        AllBlockItemIds.LAYERED_LIMESTONE,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final ConnectedPillarBlock LIMESTONE_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.LIMESTONE_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(LIMESTONE)
    );
    public static final Block OCHRUM = register(
        AllBlockItemIds.OCHRUM,
        Properties.ofFullCopy(Blocks.CALCITE).mapColor(MapColor.TERRACOTTA_YELLOW).destroyTime(1.25f)
    );
    public static final Block CUT_OCHRUM = register(AllBlockItemIds.CUT_OCHRUM, Properties.ofFullCopy(OCHRUM));
    public static final StairBlock CUT_OCHRUM_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_OCHRUM_STAIRS,
        settings -> new StairBlock(CUT_OCHRUM.defaultBlockState(), settings),
        Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock CUT_OCHRUM_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_OCHRUM_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock CUT_OCHRUM_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_OCHRUM_WALL,
        WallBlock::new,
        Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block POLISHED_CUT_OCHRUM = register(
        AllBlockItemIds.POLISHED_CUT_OCHRUM,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final StairBlock POLISHED_CUT_OCHRUM_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_OCHRUM_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_OCHRUM.defaultBlockState(), settings),
        Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock POLISHED_CUT_OCHRUM_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_OCHRUM_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock POLISHED_CUT_OCHRUM_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_OCHRUM_WALL,
        WallBlock::new,
        Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block CUT_OCHRUM_BRICKS = register(
        AllBlockItemIds.CUT_OCHRUM_BRICKS,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final StairBlock CUT_OCHRUM_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_OCHRUM_BRICK_STAIRS,
        settings -> new StairBlock(CUT_OCHRUM_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock CUT_OCHRUM_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_OCHRUM_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock CUT_OCHRUM_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_OCHRUM_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block SMALL_OCHRUM_BRICKS = register(
        AllBlockItemIds.SMALL_OCHRUM_BRICKS,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final StairBlock SMALL_OCHRUM_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_OCHRUM_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_OCHRUM_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock SMALL_OCHRUM_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_OCHRUM_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock SMALL_OCHRUM_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_OCHRUM_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block LAYERED_OCHRUM = register(AllBlockItemIds.LAYERED_OCHRUM, Properties.ofFullCopy(OCHRUM));
    public static final ConnectedPillarBlock OCHRUM_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.OCHRUM_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(OCHRUM)
    );
    public static final Block SCORIA = register(
        AllBlockItemIds.SCORIA,
        Properties.ofFullCopy(Blocks.BLACKSTONE).mapColor(MapColor.COLOR_BROWN)
    );
    public static final Block CUT_SCORIA = register(AllBlockItemIds.CUT_SCORIA, Properties.ofFullCopy(SCORIA));
    public static final StairBlock CUT_SCORIA_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_SCORIA_STAIRS,
        settings -> new StairBlock(CUT_SCORIA.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock CUT_SCORIA_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_SCORIA_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock CUT_SCORIA_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_SCORIA_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block POLISHED_CUT_SCORIA = register(
        AllBlockItemIds.POLISHED_CUT_SCORIA,
        Properties.ofFullCopy(SCORIA)
    );
    public static final StairBlock POLISHED_CUT_SCORIA_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_SCORIA_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_SCORIA.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock POLISHED_CUT_SCORIA_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_SCORIA_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock POLISHED_CUT_SCORIA_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_SCORIA_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block CUT_SCORIA_BRICKS = register(
        AllBlockItemIds.CUT_SCORIA_BRICKS,
        Properties.ofFullCopy(SCORIA)
    );
    public static final StairBlock CUT_SCORIA_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_SCORIA_BRICK_STAIRS,
        settings -> new StairBlock(CUT_SCORIA_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock CUT_SCORIA_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_SCORIA_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock CUT_SCORIA_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_SCORIA_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block SMALL_SCORIA_BRICKS = register(
        AllBlockItemIds.SMALL_SCORIA_BRICKS,
        Properties.ofFullCopy(SCORIA)
    );
    public static final StairBlock SMALL_SCORIA_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_SCORIA_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_SCORIA_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock SMALL_SCORIA_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_SCORIA_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock SMALL_SCORIA_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_SCORIA_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block LAYERED_SCORIA = register(AllBlockItemIds.LAYERED_SCORIA, Properties.ofFullCopy(SCORIA));
    public static final ConnectedPillarBlock SCORIA_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.SCORIA_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(SCORIA)
    );
    public static final Block SCORCHIA = register(
        AllBlockItemIds.SCORCHIA,
        Properties.ofFullCopy(Blocks.BLACKSTONE).mapColor(MapColor.TERRACOTTA_GRAY).destroyTime(1.25f)
    );
    public static final Block CUT_SCORCHIA = register(AllBlockItemIds.CUT_SCORCHIA, Properties.ofFullCopy(SCORCHIA));
    public static final StairBlock CUT_SCORCHIA_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_SCORCHIA_STAIRS,
        settings -> new StairBlock(CUT_SCORCHIA.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock CUT_SCORCHIA_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_SCORCHIA_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock CUT_SCORCHIA_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_SCORCHIA_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block POLISHED_CUT_SCORCHIA = register(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final StairBlock POLISHED_CUT_SCORCHIA_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_SCORCHIA.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock POLISHED_CUT_SCORCHIA_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock POLISHED_CUT_SCORCHIA_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block CUT_SCORCHIA_BRICKS = register(
        AllBlockItemIds.CUT_SCORCHIA_BRICKS,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final StairBlock CUT_SCORCHIA_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_SCORCHIA_BRICK_STAIRS,
        settings -> new StairBlock(CUT_SCORCHIA_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock CUT_SCORCHIA_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_SCORCHIA_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock CUT_SCORCHIA_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_SCORCHIA_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block SMALL_SCORCHIA_BRICKS = register(
        AllBlockItemIds.SMALL_SCORCHIA_BRICKS,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final StairBlock SMALL_SCORCHIA_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_SCORCHIA_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_SCORCHIA_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock SMALL_SCORCHIA_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_SCORCHIA_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock SMALL_SCORCHIA_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_SCORCHIA_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block LAYERED_SCORCHIA = register(
        AllBlockItemIds.LAYERED_SCORCHIA,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final ConnectedPillarBlock SCORCHIA_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.SCORCHIA_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(SCORCHIA)
    );
    public static final Block VERIDIUM = register(
        AllBlockItemIds.VERIDIUM,
        Properties.ofFullCopy(Blocks.TUFF).mapColor(MapColor.WARPED_NYLIUM).destroyTime(1.25f)
    );
    public static final Block CUT_VERIDIUM = register(AllBlockItemIds.CUT_VERIDIUM, Properties.ofFullCopy(VERIDIUM));
    public static final StairBlock CUT_VERIDIUM_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_VERIDIUM_STAIRS,
        settings -> new StairBlock(CUT_VERIDIUM.defaultBlockState(), settings),
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock CUT_VERIDIUM_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_VERIDIUM_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock CUT_VERIDIUM_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_VERIDIUM_WALL,
        WallBlock::new,
        Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block POLISHED_CUT_VERIDIUM = register(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final StairBlock POLISHED_CUT_VERIDIUM_STAIRS = (StairBlock) register(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM_STAIRS,
        settings -> new StairBlock(POLISHED_CUT_VERIDIUM.defaultBlockState(), settings),
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock POLISHED_CUT_VERIDIUM_SLAB = (SlabBlock) register(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock POLISHED_CUT_VERIDIUM_WALL = (WallBlock) register(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM_WALL,
        WallBlock::new,
        Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block CUT_VERIDIUM_BRICKS = register(
        AllBlockItemIds.CUT_VERIDIUM_BRICKS,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final StairBlock CUT_VERIDIUM_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.CUT_VERIDIUM_BRICK_STAIRS,
        settings -> new StairBlock(CUT_VERIDIUM_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock CUT_VERIDIUM_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.CUT_VERIDIUM_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock CUT_VERIDIUM_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.CUT_VERIDIUM_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block SMALL_VERIDIUM_BRICKS = register(
        AllBlockItemIds.SMALL_VERIDIUM_BRICKS,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final StairBlock SMALL_VERIDIUM_BRICK_STAIRS = (StairBlock) register(
        AllBlockItemIds.SMALL_VERIDIUM_BRICK_STAIRS,
        settings -> new StairBlock(SMALL_VERIDIUM_BRICKS.defaultBlockState(), settings),
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock SMALL_VERIDIUM_BRICK_SLAB = (SlabBlock) register(
        AllBlockItemIds.SMALL_VERIDIUM_BRICK_SLAB,
        SlabBlock::new,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock SMALL_VERIDIUM_BRICK_WALL = (WallBlock) register(
        AllBlockItemIds.SMALL_VERIDIUM_BRICK_WALL,
        WallBlock::new,
        Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block LAYERED_VERIDIUM = register(
        AllBlockItemIds.LAYERED_VERIDIUM,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final ConnectedPillarBlock VERIDIUM_PILLAR = (ConnectedPillarBlock) register(
        AllBlockItemIds.VERIDIUM_PILLAR,
        ConnectedPillarBlock::new,
        Properties.ofFullCopy(VERIDIUM)
    );
    public static final Block COPYCAT_BASE = register(
        AllBlockItemIds.COPYCAT_BASE,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.GLOW_LICHEN)
    );
    public static final WrenchableDirectionalBlock COPYCAT_BARS = (WrenchableDirectionalBlock) register(
        AllBlockItemIds.COPYCAT_BARS,
        WrenchableDirectionalBlock::new,
        Properties.of()
    );
    public static final CopycatStepBlock COPYCAT_STEP = (CopycatStepBlock) register(
        AllBlockItemIds.COPYCAT_STEP,
        CopycatStepBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).forceSolidOn().noOcclusion().mapColor(MapColor.NONE)
            .isValidSpawn(Blocks::never).emissiveRendering(CopycatStepBlock::hasEmissiveLighting)
    );
    public static final CopycatPanelBlock COPYCAT_PANEL = (CopycatPanelBlock) register(
        AllBlockItemIds.COPYCAT_PANEL,
        CopycatPanelBlock::new,
        Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.NONE).isValidSpawn(Blocks::never)
            .emissiveRendering(CopycatPanelBlock::hasEmissiveLighting)
    );

    public static final FluidBlock HONEY = AllFluidEntries.HONEY.block = (FluidBlock) register(
        AllBlockIds.HONEY,
        AllFluids::honeyBlock,
        Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final FluidBlock CHOCOLATE = AllFluidEntries.CHOCOLATE.block = (FluidBlock) register(
        AllBlockIds.CHOCOLATE,
        AllFluids::chocolateBlock,
        Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_BROWN)
    );

    public static <T extends BlockBehaviour> Function<WeatheringCopper.WeatherState, Properties> copied(
        WeatheringCopperCollection.ByState<T> states
    ) {
        return state -> Properties.ofFullCopy(states.pick(state));
    }

    public static <T extends Block, R> BiFunction<WeatherState, Properties, R> create(
        WeatheringCopperCollection.ByState<T> states,
        BiFunction<BlockState, Properties, R> factory
    ) {
        return (state, properties) -> factory.apply(states.pick(state).defaultBlockState(), properties);
    }

    public static <T extends Block, R> BiFunction<WeatherState, Properties, R> create(
        WeatheringCopperCollection.ByState<T> states,
        TriFunction<WeatherState, BlockState, Properties, R> factory
    ) {
        return (state, properties) -> factory.apply(state, states.pick(state).defaultBlockState(), properties);
    }

    private static BiFunction<WeatherState, Properties, Block> createIgnoreState(Function<Properties, Block> factory) {
        return (_, properties) -> factory.apply(properties);
    }

    public static Block createBlockIgnoreState(final WeatherState state, final Properties properties) {
        return new Block(properties);
    }

    public static void init() {
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
        VALVE_HANDLE.forEach(CStress.setCapacity(8.0));
        CStress.setCapacity(WATER_WHEEL, 32);
        CStress.setCapacity(LARGE_WATER_WHEEL, 128.0);
        CStress.setCapacity(WINDMILL_BEARING, 512.0);
        CStress.setCapacity(STEAM_ENGINE, 1024.0);
        CStress.setCapacity(CREATIVE_MOTOR, 16384.0);
    }
}
