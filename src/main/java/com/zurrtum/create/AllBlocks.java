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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

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
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).sound(SoundType.WOOD).mapColor(MapColor.DIRT)
    );
    public static final CogWheelBlock LARGE_COGWHEEL = register(
        "large_cogwheel",
        CogWheelBlock::large,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).sound(SoundType.WOOD).mapColor(MapColor.DIRT)
    );
    @SuppressWarnings("deprecation")
    public static final ShaftBlock SHAFT = register(
        "shaft",
        ShaftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).forceSolidOff()
    );
    public static final PoweredShaftBlock POWERED_SHAFT = register(
        "powered_shaft",
        PoweredShaftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).forceSolidOn()
    );
    public static final GantryShaftBlock GANTRY_SHAFT = register(
        "gantry_shaft",
        GantryShaftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.NETHER).forceSolidOn()
    );
    public static final SteamEngineBlock STEAM_ENGINE = register(
        "steam_engine",
        SteamEngineBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.METAL).forceSolidOn()
    );
    public static final SequencedGearshiftBlock SEQUENCED_GEARSHIFT = register(
        "sequenced_gearshift",
        SequencedGearshiftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final GantryCarriageBlock GANTRY_CARRIAGE = register(
        "gantry_carriage",
        GantryCarriageBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final CreativeMotorBlock CREATIVE_MOTOR = register(
        "creative_motor",
        CreativeMotorBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_PURPLE).forceSolidOn()
    );
    public static final SpeedControllerBlock ROTATION_SPEED_CONTROLLER = register(
        "rotation_speed_controller",
        SpeedControllerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final GearboxBlock GEARBOX = register(
        "gearbox",
        GearboxBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion().pushReaction(PushReaction.PUSH_ONLY)
    );
    public static final WaterWheelBlock WATER_WHEEL = register(
        "water_wheel",
        WaterWheelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).noOcclusion()
    );
    public static final LargeWaterWheelBlock LARGE_WATER_WHEEL = register(
        "large_water_wheel",
        LargeWaterWheelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).noOcclusion()
    );
    public static final WaterWheelStructuralBlock WATER_WHEEL_STRUCTURAL = register(
        "water_wheel_structure",
        WaterWheelStructuralBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).noOcclusion().pushReaction(PushReaction.BLOCK)
    );
    public static final CasingBlock ANDESITE_CASING = register(
        "andesite_casing",
        CasingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).sound(SoundType.WOOD)
    );
    public static final CasingBlock BRASS_CASING = register(
        "brass_casing",
        CasingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD)
    );
    public static final CasingBlock COPPER_CASING = register(
        "copper_casing",
        CasingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).sound(SoundType.COPPER)
    );
    public static final CasingBlock SHADOW_STEEL_CASING = register(
        "shadow_steel_casing",
        CasingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_BLACK).sound(SoundType.WOOD)
    );
    public static final CasingBlock REFINED_RADIANCE_CASING = register(
        "refined_radiance_casing",
        CasingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.SNOW).sound(SoundType.WOOD).lightLevel($ -> 12)
    );
    public static final CasingBlock RAILWAY_CASING = register(
        "railway_casing",
        CasingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_CYAN).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final ItemVaultBlock ITEM_VAULT = register(
        "item_vault",
        ItemVaultBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).sound(SoundType.NETHERITE_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE)
            .explosionResistance(1200)
    );
    public static final ArmBlock MECHANICAL_ARM = register(
        "mechanical_arm",
        ArmBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final DepotBlock DEPOT = register(
        "depot",
        DepotBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY)
    );
    public static final BeltBlock BELT = register(
        "belt",
        BeltBlock::new,
        BlockBehaviour.Properties.of().sound(SoundType.WOOL).strength(0.8f).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ClutchBlock CLUTCH = register(
        "clutch",
        ClutchBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final GearshiftBlock GEARSHIFT = register(
        "gearshift",
        GearshiftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final ChainDriveBlock ENCASED_CHAIN_DRIVE = register(
        "encased_chain_drive",
        ChainDriveBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final ChainGearshiftBlock ADJUSTABLE_CHAIN_GEARSHIFT = register(
        "adjustable_chain_gearshift",
        ChainGearshiftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.NETHER).noOcclusion()
    );
    public static final ChainConveyorBlock CHAIN_CONVEYOR = register(
        "chain_conveyor",
        ChainConveyorBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedShaftBlock ANDESITE_ENCASED_SHAFT = register(
        "andesite_encased_shaft",
        EncasedShaftBlock::andesite,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedShaftBlock BRASS_ENCASED_SHAFT = register(
        "brass_encased_shaft",
        EncasedShaftBlock::brass,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final EncasedCogwheelBlock ANDESITE_ENCASED_COGWHEEL = register(
        "andesite_encased_cogwheel",
        p -> new EncasedCogwheelBlock(p, false, AllBlocks.ANDESITE_CASING),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedCogwheelBlock BRASS_ENCASED_COGWHEEL = register(
        "brass_encased_cogwheel",
        p -> new EncasedCogwheelBlock(p, false, AllBlocks.BRASS_CASING),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final EncasedCogwheelBlock ANDESITE_ENCASED_LARGE_COGWHEEL = register(
        "andesite_encased_large_cogwheel",
        p -> new EncasedCogwheelBlock(p, true, AllBlocks.ANDESITE_CASING),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EncasedCogwheelBlock BRASS_ENCASED_LARGE_COGWHEEL = register(
        "brass_encased_large_cogwheel",
        p -> new EncasedCogwheelBlock(p, true, AllBlocks.BRASS_CASING),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final HandCrankBlock HAND_CRANK = register(
        "hand_crank",
        HandCrankBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final ValveHandleBlock COPPER_VALVE_HANDLE = register(
        "copper_valve_handle",
        ValveHandleBlock::copper,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final ValveHandleBlock WHITE_VALVE_HANDLE = register(
        "white_valve_handle",
        ValveHandleBlock.dyed(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.SNOW)
    );
    public static final ValveHandleBlock ORANGE_VALVE_HANDLE = register(
        "orange_valve_handle",
        ValveHandleBlock.dyed(DyeColor.ORANGE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final ValveHandleBlock MAGENTA_VALVE_HANDLE = register(
        "magenta_valve_handle",
        ValveHandleBlock.dyed(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_MAGENTA)
    );
    public static final ValveHandleBlock LIGHT_BLUE_VALVE_HANDLE = register(
        "light_blue_valve_handle",
        ValveHandleBlock.dyed(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_LIGHT_BLUE)
    );
    public static final ValveHandleBlock YELLOW_VALVE_HANDLE = register(
        "yellow_valve_handle",
        ValveHandleBlock.dyed(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_YELLOW)
    );
    public static final ValveHandleBlock LIME_VALVE_HANDLE = register(
        "lime_valve_handle",
        ValveHandleBlock.dyed(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN)
    );
    public static final ValveHandleBlock PINK_VALVE_HANDLE = register(
        "pink_valve_handle",
        ValveHandleBlock.dyed(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_PINK)
    );
    public static final ValveHandleBlock GRAY_VALVE_HANDLE = register(
        "gray_valve_handle",
        ValveHandleBlock.dyed(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ValveHandleBlock LIGHT_GRAY_VALVE_HANDLE = register(
        "light_gray_valve_handle",
        ValveHandleBlock.dyed(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_LIGHT_GRAY)
    );
    public static final ValveHandleBlock CYAN_VALVE_HANDLE = register(
        "cyan_valve_handle",
        ValveHandleBlock.dyed(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_CYAN)
    );
    public static final ValveHandleBlock PURPLE_VALVE_HANDLE = register(
        "purple_valve_handle",
        ValveHandleBlock.dyed(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_PURPLE)
    );
    public static final ValveHandleBlock BLUE_VALVE_HANDLE = register(
        "blue_valve_handle",
        ValveHandleBlock.dyed(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_BLUE)
    );
    public static final ValveHandleBlock BROWN_VALVE_HANDLE = register(
        "brown_valve_handle",
        ValveHandleBlock.dyed(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_BROWN)
    );
    public static final ValveHandleBlock GREEN_VALVE_HANDLE = register(
        "green_valve_handle",
        ValveHandleBlock.dyed(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GREEN)
    );
    public static final ValveHandleBlock RED_VALVE_HANDLE = register(
        "red_valve_handle",
        ValveHandleBlock.dyed(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_RED)
    );
    public static final ValveHandleBlock BLACK_VALVE_HANDLE = register(
        "black_valve_handle",
        ValveHandleBlock.dyed(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_BLACK)
    );
    public static final RadialChassisBlock RADIAL_CHASSIS = register(
        "radial_chassis",
        RadialChassisBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT)
    );
    public static final LinearChassisBlock LINEAR_CHASSIS = register(
        "linear_chassis",
        LinearChassisBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final LinearChassisBlock SECONDARY_LINEAR_CHASSIS = register(
        "secondary_linear_chassis",
        LinearChassisBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final WindmillBearingBlock WINDMILL_BEARING = register(
        "windmill_bearing",
        WindmillBearingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalBearingBlock MECHANICAL_BEARING = register(
        "mechanical_bearing",
        MechanicalBearingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalPistonBlock MECHANICAL_PISTON = register(
        "mechanical_piston",
        MechanicalPistonBlock::normal,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalPistonBlock STICKY_MECHANICAL_PISTON = register(
        "sticky_mechanical_piston",
        MechanicalPistonBlock::sticky,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final MechanicalPistonHeadBlock MECHANICAL_PISTON_HEAD = register(
        "mechanical_piston_head",
        MechanicalPistonHeadBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.PISTON_HEAD).mapColor(MapColor.DIRT).pushReaction(PushReaction.NORMAL)
    );
    public static final PistonExtensionPoleBlock PISTON_EXTENSION_POLE = register(
        "piston_extension_pole",
        PistonExtensionPoleBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.PISTON_HEAD).sound(SoundType.SCAFFOLDING).mapColor(MapColor.DIRT)
            .pushReaction(PushReaction.NORMAL).forceSolidOn()
    );
    public static final SailBlock SAIL_FRAME = register(
        "sail_frame",
        SailBlock::frame,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.DIRT).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock SAIL = register(
        "white_sail",
        SailBlock.withCanvas(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SNOW).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock ORANGE_SAIL = register(
        "orange_sail",
        SailBlock.withCanvas(DyeColor.ORANGE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_ORANGE).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock MAGENTA_SAIL = register(
        "magenta_sail",
        SailBlock.withCanvas(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_MAGENTA).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock LIGHT_BLUE_SAIL = register(
        "light_blue_sail",
        SailBlock.withCanvas(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_BLUE).sound(SoundType.SCAFFOLDING)
            .noOcclusion()
    );
    public static final SailBlock YELLOW_SAIL = register(
        "yellow_sail",
        SailBlock.withCanvas(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_YELLOW).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock LIME_SAIL = register(
        "lime_sail",
        SailBlock.withCanvas(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.SCAFFOLDING)
            .noOcclusion()
    );
    public static final SailBlock PINK_SAIL = register(
        "pink_sail",
        SailBlock.withCanvas(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PINK).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock GRAY_SAIL = register(
        "gray_sail",
        SailBlock.withCanvas(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GRAY).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock LIGHT_GRAY_SAIL = register(
        "light_gray_sail",
        SailBlock.withCanvas(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.SCAFFOLDING)
            .noOcclusion()
    );
    public static final SailBlock CYAN_SAIL = register(
        "cyan_sail",
        SailBlock.withCanvas(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_CYAN).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock PURPLE_SAIL = register(
        "purple_sail",
        SailBlock.withCanvas(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PURPLE).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock BLUE_SAIL = register(
        "blue_sail",
        SailBlock.withCanvas(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLUE).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock BROWN_SAIL = register(
        "brown_sail",
        SailBlock.withCanvas(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BROWN).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock GREEN_SAIL = register(
        "green_sail",
        SailBlock.withCanvas(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GREEN).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock RED_SAIL = register(
        "red_sail",
        SailBlock.withCanvas(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_RED).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    public static final SailBlock BLACK_SAIL = register(
        "black_sail",
        SailBlock.withCanvas(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLACK).sound(SoundType.SCAFFOLDING).noOcclusion()
    );
    @SuppressWarnings("deprecation")
    public static final FluidPipeBlock FLUID_PIPE = register(
        "fluid_pipe",
        FluidPipeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).forceSolidOff()
    );
    public static final EncasedPipeBlock ENCASED_FLUID_PIPE = register(
        "encased_fluid_pipe",
        EncasedPipeBlock::copper,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY).noOcclusion()
    );
    public static final GlassFluidPipeBlock GLASS_FLUID_PIPE = register(
        "glass_fluid_pipe",
        GlassFluidPipeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion()
    );
    public static final PumpBlock MECHANICAL_PUMP = register(
        "mechanical_pump",
        PumpBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.STONE)
    );
    public static final BlazeBurnerBlock BLAZE_BURNER = register(
        "blaze_burner",
        BlazeBurnerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).lightLevel(BlazeBurnerBlock::getLight)
    );
    public static final LitBlazeBurnerBlock LIT_BLAZE_BURNER = register(
        "lit_blaze_burner",
        LitBlazeBurnerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_LIGHT_GRAY).lightLevel(LitBlazeBurnerBlock::getLight)
    );
    public static final FluidTankBlock FLUID_TANK = register(
        "fluid_tank",
        FluidTankBlock::regular,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().isRedstoneConductor((p1, p2, p3) -> true)
            .lightLevel(FluidTankBlock::getLight)
    );
    public static final FluidTankBlock CREATIVE_FLUID_TANK = register(
        "creative_fluid_tank",
        FluidTankBlock::creative,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().mapColor(MapColor.COLOR_PURPLE)
    );
    public static final MechanicalPressBlock MECHANICAL_PRESS = register(
        "mechanical_press",
        MechanicalPressBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final EjectorBlock WEIGHTED_EJECTOR = register(
        "weighted_ejector",
        EjectorBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).noOcclusion()
    );
    public static final PulleyBlock ROPE_PULLEY = register(
        "rope_pulley",
        PulleyBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final PulleyBlock.RopeBlock ROPE = register(
        "rope",
        PulleyBlock.RopeBlock::new,
        BlockBehaviour.Properties.of().sound(SoundType.WOOL).mapColor(MapColor.COLOR_BROWN).pushReaction(PushReaction.BLOCK)
    );
    public static final PulleyBlock.MagnetBlock PULLEY_MAGNET = register(
        "pulley_magnet",
        PulleyBlock.MagnetBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).pushReaction(PushReaction.BLOCK)
    );
    public static final MillstoneBlock MILLSTONE = register(
        "millstone",
        MillstoneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL)
    );
    public static final EncasedFanBlock ENCASED_FAN = register(
        "encased_fan",
        EncasedFanBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final PeculiarBellBlock PECULIAR_BELL = register(
        "peculiar_bell",
        PeculiarBellBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.GOLD).sound(SoundType.ANVIL).noOcclusion().forceSolidOn()
    );
    public static final HauntedBellBlock HAUNTED_BELL = register(
        "haunted_bell",
        HauntedBellBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.SAND).sound(SoundType.ANVIL).noOcclusion().forceSolidOn()
    );
    public static final Block INDUSTRIAL_IRON_BLOCK = register(
        "industrial_iron_block",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
            .requiresCorrectToolForDrops()
    );
    public static final Block WEATHERED_IRON_BLOCK = register(
        "weathered_iron_block",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
            .requiresCorrectToolForDrops()
    );
    public static final WindowBlock INDUSTRIAL_IRON_WINDOW = register(
        "industrial_iron_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
            .isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ConnectedGlassPaneBlock INDUSTRIAL_IRON_WINDOW_PANE = register(
        "industrial_iron_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_GRAY)
    );
    public static final WindowBlock WEATHERED_IRON_WINDOW = register(
        "weathered_iron_window",
        WindowBlock::translucent,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
            .isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final ConnectedGlassPaneBlock WEATHERED_IRON_WINDOW_PANE = register(
        "weathered_iron_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final SawBlock MECHANICAL_SAW = register(
        "mechanical_saw",
        SawBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final BasinBlock BASIN = register(
        "basin",
        BasinBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final AndesiteFunnelBlock ANDESITE_FUNNEL = register(
        "andesite_funnel",
        AndesiteFunnelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE)
    );
    public static final BeltFunnelBlock ANDESITE_BELT_FUNNEL = register(
        "andesite_belt_funnel",
        BeltFunnelBlock::andesite,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE)
    );
    public static final BrassFunnelBlock BRASS_FUNNEL = register(
        "brass_funnel",
        BrassFunnelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final BeltFunnelBlock BRASS_BELT_FUNNEL = register(
        "brass_belt_funnel",
        BeltFunnelBlock::brass,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final BeltTunnelBlock ANDESITE_TUNNEL = register(
        "andesite_tunnel",
        BeltTunnelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).noOcclusion()
    );
    public static final BrassTunnelBlock BRASS_TUNNEL = register(
        "brass_tunnel",
        BrassTunnelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final ChuteBlock CHUTE = register(
        "chute",
        ChuteBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
            .isSuffocating(AllBlocks::never)
    );
    public static final SmartChuteBlock SMART_CHUTE = register(
        "smart_chute",
        SmartChuteBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
            .isSuffocating(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
    );
    public static final ControllerRailBlock CONTROLLER_RAIL = register(
        "controller_rail",
        ControllerRailBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).mapColor(MapColor.STONE)
    );
    public static final CartAssemblerBlock CART_ASSEMBLER = register(
        "cart_assembler",
        CartAssemblerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).noOcclusion().pushReaction(PushReaction.BLOCK)
    );
    public static final MinecartAnchorBlock MINECART_ANCHOR = register(
        "minecart_anchor",
        MinecartAnchorBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final PloughBlock MECHANICAL_PLOUGH = register(
        "mechanical_plough",
        PloughBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).forceSolidOn()
    );
    public static final HarvesterBlock MECHANICAL_HARVESTER = register(
        "mechanical_harvester",
        HarvesterBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).forceSolidOn()
    );
    public static final PortableStorageInterfaceBlock PORTABLE_FLUID_INTERFACE = register(
        "portable_fluid_interface",
        PortableStorageInterfaceBlock::forFluids,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final PortableStorageInterfaceBlock PORTABLE_STORAGE_INTERFACE = register(
        "portable_storage_interface",
        PortableStorageInterfaceBlock::forItems,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final GaugeBlock SPEEDOMETER = register(
        "speedometer",
        GaugeBlock::speed,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final GaugeBlock STRESSOMETER = register(
        "stressometer",
        GaugeBlock::stress,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final CuckooClockBlock CUCKOO_CLOCK = register(
        "cuckoo_clock",
        CuckooClockBlock::regular,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final CuckooClockBlock MYSTERIOUS_CUCKOO_CLOCK = register(
        "mysterious_cuckoo_clock",
        CuckooClockBlock::mysterious,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final MechanicalMixerBlock MECHANICAL_MIXER = register(
        "mechanical_mixer",
        MechanicalMixerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).noOcclusion()
    );
    public static final HosePulleyBlock HOSE_PULLEY = register(
        "hose_pulley",
        HosePulleyBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.STONE).noOcclusion()
    );
    public static final SpoutBlock SPOUT = register("spout", SpoutBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final ItemDrainBlock ITEM_DRAIN = register(
        "item_drain",
        ItemDrainBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WhistleBlock STEAM_WHISTLE = register(
        "steam_whistle",
        WhistleBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.GOLD)
    );
    public static final WhistleExtenderBlock STEAM_WHISTLE_EXTENSION = register(
        "steam_whistle_extension",
        WhistleExtenderBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.GOLD).forceSolidOn()
    );
    public static final BacktankBlock COPPER_BACKTANK = register(
        "copper_backtank",
        BacktankBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final BacktankBlock NETHERITE_BACKTANK = register(
        "netherite_backtank",
        BacktankBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
    );
    public static final DeployerBlock DEPLOYER = register(
        "deployer",
        DeployerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL).noOcclusion()
    );
    public static final TurntableBlock TURNTABLE = register(
        "turntable",
        TurntableBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.PODZOL)
    );
    public static final DrillBlock MECHANICAL_DRILL = register(
        "mechanical_drill",
        DrillBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final ClockworkBearingBlock CLOCKWORK_BEARING = register(
        "clockwork_bearing",
        ClockworkBearingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion()
    );
    public static final CrushingWheelBlock CRUSHING_WHEEL = register(
        "crushing_wheel",
        CrushingWheelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).noOcclusion()
    );
    public static final CrushingWheelControllerBlock CRUSHING_WHEEL_CONTROLLER = register(
        "crushing_wheel_controller",
        CrushingWheelControllerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).noLootTable().noCollision().pushReaction(PushReaction.BLOCK)
    );
    public static final Block RAW_ZINC_BLOCK = register(
        "raw_zinc_block",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_GOLD_BLOCK).mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops()
    );
    public static final Block ZINC_BLOCK = register(
        "zinc_block",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.GLOW_LICHEN).requiresCorrectToolForDrops()
    );
    public static final Block ZINC_ORE = register(
        "zinc_ore",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE).mapColor(MapColor.METAL).requiresCorrectToolForDrops().sound(SoundType.STONE)
    );
    public static final Block DEEPSLATE_ZINC_ORE = register(
        "deepslate_zinc_ore",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE)
    );
    public static final Block BRASS_BLOCK = register(
        "brass_block",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops()
    );
    public static final FlapDisplayBlock DISPLAY_BOARD = register(
        "display_board",
        FlapDisplayBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY)
    );
    public static final ClipboardBlock CLIPBOARD = register(
        "clipboard",
        ClipboardBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).forceSolidOn()
    );
    public static final DisplayLinkBlock DISPLAY_LINK = register(
        "display_link",
        DisplayLinkBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final NixieTubeBlock ORANGE_NIXIE_TUBE = register(
        "nixie_tube",
        NixieTubeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_ORANGE).forceSolidOn()
    );
    public static final NixieTubeBlock WHITE_NIXIE_TUBE = register(
        "white_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.SNOW).forceSolidOn()
    );
    public static final NixieTubeBlock MAGENTA_NIXIE_TUBE = register(
        "magenta_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_MAGENTA).forceSolidOn()
    );
    public static final NixieTubeBlock LIGHT_BLUE_NIXIE_TUBE = register(
        "light_blue_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_LIGHT_BLUE).forceSolidOn()
    );
    public static final NixieTubeBlock YELLOW_NIXIE_TUBE = register(
        "yellow_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_YELLOW).forceSolidOn()
    );
    public static final NixieTubeBlock LIME_NIXIE_TUBE = register(
        "lime_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_LIGHT_GREEN).forceSolidOn()
    );
    public static final NixieTubeBlock PINK_NIXIE_TUBE = register(
        "pink_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_PINK).forceSolidOn()
    );
    public static final NixieTubeBlock GRAY_NIXIE_TUBE = register(
        "gray_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_GRAY).forceSolidOn()
    );
    public static final NixieTubeBlock LIGHT_GRAY_NIXIE_TUBE = register(
        "light_gray_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_LIGHT_GRAY).forceSolidOn()
    );
    public static final NixieTubeBlock CYAN_NIXIE_TUBE = register(
        "cyan_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_CYAN).forceSolidOn()
    );
    public static final NixieTubeBlock PURPLE_NIXIE_TUBE = register(
        "purple_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_PURPLE).forceSolidOn()
    );
    public static final NixieTubeBlock BLUE_NIXIE_TUBE = register(
        "blue_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_BLUE).forceSolidOn()
    );
    public static final NixieTubeBlock BROWN_NIXIE_TUBE = register(
        "brown_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_BROWN).forceSolidOn()
    );
    public static final NixieTubeBlock GREEN_NIXIE_TUBE = register(
        "green_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_GREEN).forceSolidOn()
    );
    public static final NixieTubeBlock RED_NIXIE_TUBE = register(
        "red_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_RED).forceSolidOn()
    );
    public static final NixieTubeBlock BLACK_NIXIE_TUBE = register(
        "black_nixie_tube",
        NixieTubeBlock.dyed(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).lightLevel($ -> 5).mapColor(MapColor.COLOR_BLACK).forceSolidOn()
    );
    public static final BracketBlock WOODEN_BRACKET = register(
        "wooden_bracket",
        BracketBlock::new,
        BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING)
    );
    public static final BracketBlock METAL_BRACKET = register(
        "metal_bracket",
        BracketBlock::new,
        BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK)
    );
    public static final GirderBlock METAL_GIRDER = register(
        "metal_girder",
        GirderBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final GirderEncasedShaftBlock METAL_GIRDER_ENCASED_SHAFT = register(
        "metal_girder_encased_shaft",
        GirderEncasedShaftBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final FluidValveBlock FLUID_VALVE = register(
        "fluid_valve",
        FluidValveBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final SmartFluidPipeBlock SMART_FLUID_PIPE = register(
        "smart_fluid_pipe",
        SmartFluidPipeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final AnalogLeverBlock ANALOG_LEVER = register(
        "analog_lever",
        AnalogLeverBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
    );
    public static final RedstoneContactBlock REDSTONE_CONTACT = register(
        "redstone_contact",
        RedstoneContactBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY)
    );
    public static final RedstoneLinkBlock REDSTONE_LINK = register(
        "redstone_link",
        RedstoneLinkBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.TERRACOTTA_BROWN).forceSolidOn()
    );
    public static final BrassDiodeBlock PULSE_REPEATER = register(
        "pulse_repeater",
        BrassDiodeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final BrassDiodeBlock PULSE_EXTENDER = register(
        "pulse_extender",
        BrassDiodeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final BrassDiodeBlock PULSE_TIMER = register(
        "pulse_timer",
        BrassDiodeBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final PoweredLatchBlock POWERED_LATCH = register(
        "powered_latch",
        PoweredLatchBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final ToggleLatchBlock POWERED_TOGGLE_LATCH = register(
        "powered_toggle_latch",
        ToggleLatchBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
    );
    public static final RoseQuartzLampBlock ROSE_QUARTZ_LAMP = register(
        "rose_quartz_lamp",
        RoseQuartzLampBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP).mapColor(MapColor.TERRACOTTA_PINK)
            .lightLevel(state -> state.getValue(RoseQuartzLampBlock.POWERING) ? 15 : 0)
    );
    public static final SmartObserverBlock SMART_OBSERVER = register(
        "content_observer",
        SmartObserverBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion().isRedstoneConductor(AllBlocks::never)
    );
    public static final ThresholdSwitchBlock THRESHOLD_SWITCH = register(
        "stockpile_switch",
        ThresholdSwitchBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion().isRedstoneConductor(AllBlocks::never)
    );
    public static final StickerBlock STICKER = register(
        "sticker",
        StickerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).noOcclusion()
    );
    public static final ContraptionControlsBlock CONTRAPTION_CONTROLS = register(
        "contraption_controls",
        ContraptionControlsBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.PODZOL)
    );
    public static final ElevatorPulleyBlock ELEVATOR_PULLEY = register(
        "elevator_pulley",
        ElevatorPulleyBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN)
    );
    public static final ElevatorContactBlock ELEVATOR_CONTACT = register(
        "elevator_contact",
        ElevatorContactBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).lightLevel(ElevatorContactBlock::getLight)
    );
    public static final SlidingDoorBlock ANDESITE_DOOR = register(
        "andesite_door",
        SlidingDoorBlock::stone_fold,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).requiresCorrectToolForDrops().strength(3.0F, 6.0F).mapColor(MapColor.STONE)
            .noOcclusion()
    );
    public static final SlidingDoorBlock BRASS_DOOR = register(
        "brass_door",
        SlidingDoorBlock::stone_slide,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).requiresCorrectToolForDrops().strength(3.0F, 6.0F).mapColor(MapColor.TERRACOTTA_YELLOW)
            .noOcclusion()
    );
    public static final SlidingDoorBlock COPPER_DOOR = register(
        "copper_door",
        SlidingDoorBlock::stone_fold,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_DOOR).requiresCorrectToolForDrops().strength(3.0F, 6.0F).mapColor(MapColor.COLOR_ORANGE)
            .noOcclusion()
    );
    public static final SlidingDoorBlock TRAIN_DOOR = register(
        "train_door",
        SlidingDoorBlock::metal_slide,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 6.0F).mapColor(MapColor.TERRACOTTA_CYAN)
            .noOcclusion()
    );
    public static final SlidingDoorBlock FRAMED_GLASS_DOOR = register(
        "framed_glass_door",
        SlidingDoorBlock::glass_slide,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 6.0F).mapColor(MapColor.NONE)
            .noOcclusion()
    );
    public static final NozzleBlock NOZZLE = register(
        "nozzle",
        NozzleBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_LIGHT_GRAY)
    );
    public static final DeskBellBlock DESK_BELL = register("desk_bell", DeskBellBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND));
    public static final MechanicalCrafterBlock MECHANICAL_CRAFTER = register(
        "mechanical_crafter",
        MechanicalCrafterBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).noOcclusion()
    );
    public static final CreativeCrateBlock CREATIVE_CRATE = register(
        "creative_crate",
        CreativeCrateBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_PURPLE)
    );
    public static final TrackBlock TRACK = register(
        "track",
        TrackBlock::andesite,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.METAL).strength(0.8F).sound(SoundType.METAL).noOcclusion()
            .forceSolidOn().pushReaction(PushReaction.BLOCK)
    );
    public static final FakeTrackBlock FAKE_TRACK = register(
        "fake_track",
        FakeTrackBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.METAL).randomTicks().noCollision().replaceable()
    );
    public static final SignalBlock TRACK_SIGNAL = register(
        "track_signal",
        SignalBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).noOcclusion().sound(SoundType.NETHERITE_BLOCK)
    );
    public static final StandardBogeyBlock SMALL_BOGEY = register(
        "small_bogey",
        StandardBogeyBlock::small,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
    );
    public static final StandardBogeyBlock LARGE_BOGEY = register(
        "large_bogey",
        StandardBogeyBlock::large,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
    );
    public static final ControlsBlock TRAIN_CONTROLS = register(
        "controls",
        ControlsBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BROWN).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final StationBlock TRACK_STATION = register(
        "track_station",
        StationBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final TrackObserverBlock TRACK_OBSERVER = register(
        "track_observer",
        TrackObserverBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PODZOL).noOcclusion().sound(SoundType.NETHERITE_BLOCK)
    );
    public static final SeatBlock WHITE_SEAT = register(
        "white_seat",
        SeatBlock.dyed(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SNOW)
    );
    public static final SeatBlock ORANGE_SEAT = register(
        "orange_seat",
        SeatBlock.dyed(DyeColor.ORANGE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final SeatBlock MAGENTA_SEAT = register(
        "magenta_seat",
        SeatBlock.dyed(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_MAGENTA)
    );
    public static final SeatBlock LIGHT_BLUE_SEAT = register(
        "light_blue_seat",
        SeatBlock.dyed(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_BLUE)
    );
    public static final SeatBlock YELLOW_SEAT = register(
        "yellow_seat",
        SeatBlock.dyed(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_YELLOW)
    );
    public static final SeatBlock LIME_SEAT = register(
        "lime_seat",
        SeatBlock.dyed(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GREEN)
    );
    public static final SeatBlock PINK_SEAT = register(
        "pink_seat",
        SeatBlock.dyed(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PINK)
    );
    public static final SeatBlock GRAY_SEAT = register(
        "gray_seat",
        SeatBlock.dyed(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GRAY)
    );
    public static final SeatBlock LIGHT_GRAY_SEAT = register(
        "light_gray_seat",
        SeatBlock.dyed(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GRAY)
    );
    public static final SeatBlock CYAN_SEAT = register(
        "cyan_seat",
        SeatBlock.dyed(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_CYAN)
    );
    public static final SeatBlock PURPLE_SEAT = register(
        "purple_seat",
        SeatBlock.dyed(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PURPLE)
    );
    public static final SeatBlock BLUE_SEAT = register(
        "blue_seat",
        SeatBlock.dyed(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLUE)
    );
    public static final SeatBlock BROWN_SEAT = register(
        "brown_seat",
        SeatBlock.dyed(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BROWN)
    );
    public static final SeatBlock GREEN_SEAT = register(
        "green_seat",
        SeatBlock.dyed(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GREEN)
    );
    public static final SeatBlock RED_SEAT = register(
        "red_seat",
        SeatBlock.dyed(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_RED)
    );
    public static final SeatBlock BLACK_SEAT = register(
        "black_seat",
        SeatBlock.dyed(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLACK)
    );
    public static final RollerBlock MECHANICAL_ROLLER = register(
        "mechanical_roller",
        RollerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.COLOR_GRAY).noOcclusion()
    );
    public static final LecternControllerBlock LECTERN_CONTROLLER = register(
        "lectern_controller",
        LecternControllerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN)
    );
    public static final PackagerBlock PACKAGER = register(
        "packager",
        PackagerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().isRedstoneConductor(AllBlocks::never).mapColor(MapColor.TERRACOTTA_BLUE)
            .sound(SoundType.NETHERITE_BLOCK)
    );
    public static final CardboardBlock CARDBOARD_BLOCK = register(
        "cardboard_block",
        CardboardBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM).mapColor(MapColor.COLOR_BROWN).sound(SoundType.CHISELED_BOOKSHELF).ignitedByLava()
    );
    public static final PackagerLinkBlock STOCK_LINK = register(
        "stock_link",
        PackagerLinkBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final RedstoneRequesterBlock REDSTONE_REQUESTER = register(
        "redstone_requester",
        RedstoneRequesterBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).sound(SoundType.NETHERITE_BLOCK).noOcclusion()
    );
    public static final RepackagerBlock REPACKAGER = register(
        "repackager",
        RepackagerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().isRedstoneConductor(AllBlocks::never).mapColor(MapColor.TERRACOTTA_BLUE)
            .sound(SoundType.NETHERITE_BLOCK)
    );
    public static final StockTickerBlock STOCK_TICKER = register(
        "stock_ticker",
        StockTickerBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).sound(SoundType.GLASS)
    );
    public static final TableClothBlock WHITE_TABLE_CLOTH = register(
        "white_table_cloth",
        TableClothBlock.dyed(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.SNOW)
    );
    public static final TableClothBlock ORANGE_TABLE_CLOTH = register(
        "orange_table_cloth",
        TableClothBlock.dyed(DyeColor.ORANGE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final TableClothBlock MAGENTA_TABLE_CLOTH = register(
        "magenta_table_cloth",
        TableClothBlock.dyed(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_MAGENTA)
    );
    public static final TableClothBlock LIGHT_BLUE_TABLE_CLOTH = register(
        "light_blue_table_cloth",
        TableClothBlock.dyed(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_LIGHT_BLUE)
    );
    public static final TableClothBlock YELLOW_TABLE_CLOTH = register(
        "yellow_table_cloth",
        TableClothBlock.dyed(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_YELLOW)
    );
    public static final TableClothBlock LIME_TABLE_CLOTH = register(
        "lime_table_cloth",
        TableClothBlock.dyed(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_LIGHT_GREEN)
    );
    public static final TableClothBlock PINK_TABLE_CLOTH = register(
        "pink_table_cloth",
        TableClothBlock.dyed(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_PINK)
    );
    public static final TableClothBlock GRAY_TABLE_CLOTH = register(
        "gray_table_cloth",
        TableClothBlock.dyed(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_GRAY)
    );
    public static final TableClothBlock LIGHT_GRAY_TABLE_CLOTH = register(
        "light_gray_table_cloth",
        TableClothBlock.dyed(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_LIGHT_GRAY)
    );
    public static final TableClothBlock CYAN_TABLE_CLOTH = register(
        "cyan_table_cloth",
        TableClothBlock.dyed(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_CYAN)
    );
    public static final TableClothBlock PURPLE_TABLE_CLOTH = register(
        "purple_table_cloth",
        TableClothBlock.dyed(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_PURPLE)
    );
    public static final TableClothBlock BLUE_TABLE_CLOTH = register(
        "blue_table_cloth",
        TableClothBlock.dyed(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_BLUE)
    );
    public static final TableClothBlock BROWN_TABLE_CLOTH = register(
        "brown_table_cloth",
        TableClothBlock.dyed(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_BROWN)
    );
    public static final TableClothBlock GREEN_TABLE_CLOTH = register(
        "green_table_cloth",
        TableClothBlock.dyed(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_GREEN)
    );
    public static final TableClothBlock RED_TABLE_CLOTH = register(
        "red_table_cloth",
        TableClothBlock.dyed(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_RED)
    );
    public static final TableClothBlock BLACK_TABLE_CLOTH = register(
        "black_table_cloth",
        TableClothBlock.dyed(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET).mapColor(MapColor.COLOR_BLACK)
    );
    public static final TableClothBlock ANDESITE_TABLE_CLOTH = register(
        "andesite_table_cloth",
        TableClothBlock.styled("andesite"),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()
    );
    public static final TableClothBlock BRASS_TABLE_CLOTH = register(
        "brass_table_cloth",
        TableClothBlock.styled("brass"),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops()
    );
    public static final TableClothBlock COPPER_TABLE_CLOTH = register(
        "copper_table_cloth",
        TableClothBlock.styled("copper"),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops()
    );
    public static final PostboxBlock WHITE_POSTBOX = register(
        "white_postbox",
        PostboxBlock.dyed(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SNOW)
    );
    public static final PostboxBlock ORANGE_POSTBOX = register(
        "orange_postbox",
        PostboxBlock.dyed(DyeColor.ORANGE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final PostboxBlock MAGENTA_POSTBOX = register(
        "magenta_postbox",
        PostboxBlock.dyed(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_MAGENTA)
    );
    public static final PostboxBlock LIGHT_BLUE_POSTBOX = register(
        "light_blue_postbox",
        PostboxBlock.dyed(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_BLUE)
    );
    public static final PostboxBlock YELLOW_POSTBOX = register(
        "yellow_postbox",
        PostboxBlock.dyed(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_YELLOW)
    );
    public static final PostboxBlock LIME_POSTBOX = register(
        "lime_postbox",
        PostboxBlock.dyed(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GREEN)
    );
    public static final PostboxBlock PINK_POSTBOX = register(
        "pink_postbox",
        PostboxBlock.dyed(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PINK)
    );
    public static final PostboxBlock GRAY_POSTBOX = register(
        "gray_postbox",
        PostboxBlock.dyed(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GRAY)
    );
    public static final PostboxBlock LIGHT_GRAY_POSTBOX = register(
        "light_gray_postbox",
        PostboxBlock.dyed(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GRAY)
    );
    public static final PostboxBlock CYAN_POSTBOX = register(
        "cyan_postbox",
        PostboxBlock.dyed(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_CYAN)
    );
    public static final PostboxBlock PURPLE_POSTBOX = register(
        "purple_postbox",
        PostboxBlock.dyed(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PURPLE)
    );
    public static final PostboxBlock BLUE_POSTBOX = register(
        "blue_postbox",
        PostboxBlock.dyed(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLUE)
    );
    public static final PostboxBlock BROWN_POSTBOX = register(
        "brown_postbox",
        PostboxBlock.dyed(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BROWN)
    );
    public static final PostboxBlock GREEN_POSTBOX = register(
        "green_postbox",
        PostboxBlock.dyed(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GREEN)
    );
    public static final PostboxBlock RED_POSTBOX = register(
        "red_postbox",
        PostboxBlock.dyed(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_RED)
    );
    public static final PostboxBlock BLACK_POSTBOX = register(
        "black_postbox",
        PostboxBlock.dyed(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLACK)
    );
    public static final FrogportBlock PACKAGE_FROGPORT = register(
        "package_frogport",
        FrogportBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final FactoryPanelBlock FACTORY_GAUGE = register(
        "factory_gauge",
        FactoryPanelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().forceSolidOn()
    );
    public static final FlywheelBlock FLYWHEEL = register(
        "flywheel",
        FlywheelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final ItemHatchBlock ITEM_HATCH = register(
        "item_hatch",
        ItemHatchBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
    );
    public static final PlacardBlock PLACARD = register(
        "placard",
        PlacardBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).forceSolidOn()
    );
    public static final ToolboxBlock WHITE_TOOLBOX = register(
        "white_toolbox",
        ToolboxBlock.dyed(DyeColor.WHITE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.SNOW).forceSolidOn()
    );
    public static final ToolboxBlock ORANGE_TOOLBOX = register(
        "orange_toolbox",
        ToolboxBlock.dyed(DyeColor.ORANGE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_ORANGE).forceSolidOn()
    );
    public static final ToolboxBlock MAGENTA_TOOLBOX = register(
        "magenta_toolbox",
        ToolboxBlock.dyed(DyeColor.MAGENTA),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_MAGENTA).forceSolidOn()
    );
    public static final ToolboxBlock LIGHT_BLUE_TOOLBOX = register(
        "light_blue_toolbox",
        ToolboxBlock.dyed(DyeColor.LIGHT_BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_BLUE).forceSolidOn()
    );
    public static final ToolboxBlock YELLOW_TOOLBOX = register(
        "yellow_toolbox",
        ToolboxBlock.dyed(DyeColor.YELLOW),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_YELLOW).forceSolidOn()
    );
    public static final ToolboxBlock LIME_TOOLBOX = register(
        "lime_toolbox",
        ToolboxBlock.dyed(DyeColor.LIME),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GREEN).forceSolidOn()
    );
    public static final ToolboxBlock PINK_TOOLBOX = register(
        "pink_toolbox",
        ToolboxBlock.dyed(DyeColor.PINK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PINK).forceSolidOn()
    );
    public static final ToolboxBlock GRAY_TOOLBOX = register(
        "gray_toolbox",
        ToolboxBlock.dyed(DyeColor.GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GRAY).forceSolidOn()
    );
    public static final ToolboxBlock LIGHT_GRAY_TOOLBOX = register(
        "light_gray_toolbox",
        ToolboxBlock.dyed(DyeColor.LIGHT_GRAY),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_LIGHT_GRAY).forceSolidOn()
    );
    public static final ToolboxBlock CYAN_TOOLBOX = register(
        "cyan_toolbox",
        ToolboxBlock.dyed(DyeColor.CYAN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_CYAN).forceSolidOn()
    );
    public static final ToolboxBlock PURPLE_TOOLBOX = register(
        "purple_toolbox",
        ToolboxBlock.dyed(DyeColor.PURPLE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_PURPLE).forceSolidOn()
    );
    public static final ToolboxBlock BLUE_TOOLBOX = register(
        "blue_toolbox",
        ToolboxBlock.dyed(DyeColor.BLUE),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLUE).forceSolidOn()
    );
    public static final ToolboxBlock BROWN_TOOLBOX = register(
        "brown_toolbox",
        ToolboxBlock.dyed(DyeColor.BROWN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BROWN).forceSolidOn()
    );
    public static final ToolboxBlock GREEN_TOOLBOX = register(
        "green_toolbox",
        ToolboxBlock.dyed(DyeColor.GREEN),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_GREEN).forceSolidOn()
    );
    public static final ToolboxBlock RED_TOOLBOX = register(
        "red_toolbox",
        ToolboxBlock.dyed(DyeColor.RED),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_RED).forceSolidOn()
    );
    public static final ToolboxBlock BLACK_TOOLBOX = register(
        "black_toolbox",
        ToolboxBlock.dyed(DyeColor.BLACK),
        BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD).mapColor(MapColor.COLOR_BLACK).forceSolidOn()
    );
    public static final SchematicTableBlock SCHEMATIC_TABLE = register(
        "schematic_table",
        SchematicTableBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN).mapColor(MapColor.PODZOL).forceSolidOn().pushReaction(PushReaction.BLOCK)
    );
    public static final SchematicannonBlock SCHEMATICANNON = register(
        "schematicannon",
        SchematicannonBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DISPENSER).mapColor(MapColor.COLOR_GRAY)
    );
    public static final WindowBlock ORNATE_IRON_WINDOW = register(
        "ornate_iron_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
            .isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final MetalLadderBlock ANDESITE_LADDER = register(
        "andesite_ladder",
        MetalLadderBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER).mapColor(MapColor.STONE).sound(SoundType.COPPER)
    );
    public static final MetalLadderBlock BRASS_LADDER = register(
        "brass_ladder",
        MetalLadderBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER).mapColor(MapColor.TERRACOTTA_YELLOW).sound(SoundType.COPPER)
    );
    public static final MetalLadderBlock COPPER_LADDER = register(
        "copper_ladder",
        MetalLadderBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER).mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER)
    );
    public static final MetalScaffoldingBlock ANDESITE_SCAFFOLD = register(
        "andesite_scaffolding",
        MetalScaffoldingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SCAFFOLDING).sound(SoundType.COPPER).mapColor(MapColor.STONE)
    );
    public static final MetalScaffoldingBlock BRASS_SCAFFOLD = register(
        "brass_scaffolding",
        MetalScaffoldingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SCAFFOLDING).sound(SoundType.COPPER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final MetalScaffoldingBlock COPPER_SCAFFOLD = register(
        "copper_scaffolding",
        MetalScaffoldingBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SCAFFOLDING).sound(SoundType.COPPER).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final IronBarsBlock ANDESITE_BARS = register(
        "andesite_bars",
        IronBarsBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.COPPER).mapColor(MapColor.STONE)
    );
    public static final IronBarsBlock BRASS_BARS = register(
        "brass_bars",
        IronBarsBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.COPPER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final IronBarsBlock COPPER_BARS = register(
        "copper_bars",
        IronBarsBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.COPPER).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final TrainTrapdoorBlock TRAIN_TRAPDOOR = register(
        "train_trapdoor",
        TrainTrapdoorBlock::metal,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_CYAN)
    );
    public static final TrainTrapdoorBlock FRAMED_GLASS_TRAPDOOR = register(
        "framed_glass_trapdoor",
        TrainTrapdoorBlock::metal,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.NONE).noOcclusion()
    );
    public static final Block ANDESITE_ALLOY_BLOCK = register(
        "andesite_alloy_block",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).mapColor(MapColor.STONE).requiresCorrectToolForDrops()
    );
    public static final CardboardBlock BOUND_CARDBOARD_BLOCK = register(
        "bound_cardboard_block",
        CardboardBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM).mapColor(MapColor.COLOR_BROWN).sound(SoundType.CHISELED_BOOKSHELF).ignitedByLava()
    );
    public static final ExperienceBlock EXPERIENCE_BLOCK = register(
        "experience_block",
        ExperienceBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.PLANT).sound(ExperienceBlock.SOUND).requiresCorrectToolForDrops()
            .lightLevel(s -> 15)
    );
    public static final RotatedPillarBlock ROSE_QUARTZ_BLOCK = register(
        "rose_quartz_block",
        RotatedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK).mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops()
            .sound(SoundType.DEEPSLATE)
    );
    public static final Block ROSE_QUARTZ_TILES = register(
        "rose_quartz_tiles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops()
    );
    public static final Block SMALL_ROSE_QUARTZ_TILES = register(
        "small_rose_quartz_tiles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.TERRACOTTA_PINK).requiresCorrectToolForDrops()
    );
    public static final WeatheringCopperFullBlock COPPER_SHINGLES = register(
        "copper_shingles",
        settings -> new WeatheringCopperFullBlock(WeatherState.UNAFFECTED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WeatheringCopperFullBlock EXPOSED_COPPER_SHINGLES = register(
        "exposed_copper_shingles",
        settings -> new WeatheringCopperFullBlock(WeatherState.EXPOSED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final WeatheringCopperFullBlock WEATHERED_COPPER_SHINGLES = register(
        "weathered_copper_shingles",
        settings -> new WeatheringCopperFullBlock(WeatherState.WEATHERED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final WeatheringCopperFullBlock OXIDIZED_COPPER_SHINGLES = register(
        "oxidized_copper_shingles",
        settings -> new WeatheringCopperFullBlock(WeatherState.OXIDIZED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final Block WAXED_COPPER_SHINGLES = register(
        "waxed_copper_shingles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final Block WAXED_EXPOSED_COPPER_SHINGLES = register(
        "waxed_exposed_copper_shingles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final Block WAXED_WEATHERED_COPPER_SHINGLES = register(
        "waxed_weathered_copper_shingles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final Block WAXED_OXIDIZED_COPPER_SHINGLES = register(
        "waxed_oxidized_copper_shingles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final WeatheringCopperSlabBlock COPPER_SHINGLE_SLAB = register(
        "copper_shingle_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.UNAFFECTED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WeatheringCopperSlabBlock EXPOSED_COPPER_SHINGLE_SLAB = register(
        "exposed_copper_shingle_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.EXPOSED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final WeatheringCopperSlabBlock WEATHERED_COPPER_SHINGLE_SLAB = register(
        "weathered_copper_shingle_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.WEATHERED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final WeatheringCopperSlabBlock OXIDIZED_COPPER_SHINGLE_SLAB = register(
        "oxidized_copper_shingle_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.OXIDIZED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final SlabBlock WAXED_COPPER_SHINGLE_SLAB = register(
        "waxed_copper_shingle_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final SlabBlock WAXED_EXPOSED_COPPER_SHINGLE_SLAB = register(
        "waxed_exposed_copper_shingle_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final SlabBlock WAXED_WEATHERED_COPPER_SHINGLE_SLAB = register(
        "waxed_weathered_copper_shingle_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final SlabBlock WAXED_OXIDIZED_COPPER_SHINGLE_SLAB = register(
        "waxed_oxidized_copper_shingle_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final WeatheringCopperStairBlock COPPER_SHINGLE_STAIRS = register(
        "copper_shingle_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.UNAFFECTED, COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WeatheringCopperStairBlock EXPOSED_COPPER_SHINGLE_STAIRS = register(
        "exposed_copper_shingle_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.EXPOSED, EXPOSED_COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final WeatheringCopperStairBlock WEATHERED_COPPER_SHINGLE_STAIRS = register(
        "weathered_copper_shingle_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.WEATHERED, WEATHERED_COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final WeatheringCopperStairBlock OXIDIZED_COPPER_SHINGLE_STAIRS = register(
        "oxidized_copper_shingle_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.OXIDIZED, OXIDIZED_COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final StairBlock WAXED_COPPER_SHINGLE_STAIRS = register(
        "waxed_copper_shingle_stairs",
        settings -> new StairBlock(COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final StairBlock WAXED_EXPOSED_COPPER_SHINGLE_STAIRS = register(
        "waxed_exposed_copper_shingle_stairs",
        settings -> new StairBlock(EXPOSED_COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final StairBlock WAXED_WEATHERED_COPPER_SHINGLE_STAIRS = register(
        "waxed_weathered_copper_shingle_stairs",
        settings -> new StairBlock(WEATHERED_COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final StairBlock WAXED_OXIDIZED_COPPER_SHINGLE_STAIRS = register(
        "waxed_oxidized_copper_shingle_stairs",
        settings -> new StairBlock(OXIDIZED_COPPER_SHINGLES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final WeatheringCopperFullBlock COPPER_TILES = register(
        "copper_tiles",
        settings -> new WeatheringCopperFullBlock(WeatherState.UNAFFECTED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WeatheringCopperFullBlock EXPOSED_COPPER_TILES = register(
        "exposed_copper_tiles",
        settings -> new WeatheringCopperFullBlock(WeatherState.EXPOSED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final WeatheringCopperFullBlock WEATHERED_COPPER_TILES = register(
        "weathered_copper_tiles",
        settings -> new WeatheringCopperFullBlock(WeatherState.WEATHERED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final WeatheringCopperFullBlock OXIDIZED_COPPER_TILES = register(
        "oxidized_copper_tiles",
        settings -> new WeatheringCopperFullBlock(WeatherState.OXIDIZED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final Block WAXED_COPPER_TILES = register(
        "waxed_copper_tiles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final Block WAXED_EXPOSED_COPPER_TILES = register(
        "waxed_exposed_copper_tiles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final Block WAXED_WEATHERED_COPPER_TILES = register(
        "waxed_weathered_copper_tiles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final Block WAXED_OXIDIZED_COPPER_TILES = register(
        "waxed_oxidized_copper_tiles",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final WeatheringCopperSlabBlock COPPER_TILE_SLAB = register(
        "copper_tile_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.UNAFFECTED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WeatheringCopperSlabBlock EXPOSED_COPPER_TILE_SLAB = register(
        "exposed_copper_tile_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.EXPOSED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final WeatheringCopperSlabBlock WEATHERED_COPPER_TILE_SLAB = register(
        "weathered_copper_tile_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.WEATHERED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final WeatheringCopperSlabBlock OXIDIZED_COPPER_TILE_SLAB = register(
        "oxidized_copper_tile_slab",
        settings -> new WeatheringCopperSlabBlock(WeatherState.OXIDIZED, settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final SlabBlock WAXED_COPPER_TILE_SLAB = register(
        "waxed_copper_tile_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final SlabBlock WAXED_EXPOSED_COPPER_TILE_SLAB = register(
        "waxed_exposed_copper_tile_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final SlabBlock WAXED_WEATHERED_COPPER_TILE_SLAB = register(
        "waxed_weathered_copper_tile_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final SlabBlock WAXED_OXIDIZED_COPPER_TILE_SLAB = register(
        "waxed_oxidized_copper_tile_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final WeatheringCopperStairBlock COPPER_TILE_STAIRS = register(
        "copper_tile_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.UNAFFECTED, COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final WeatheringCopperStairBlock EXPOSED_COPPER_TILE_STAIRS = register(
        "exposed_copper_tile_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.EXPOSED, EXPOSED_COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final WeatheringCopperStairBlock WEATHERED_COPPER_TILE_STAIRS = register(
        "weathered_copper_tile_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.WEATHERED, WEATHERED_COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final WeatheringCopperStairBlock OXIDIZED_COPPER_TILE_STAIRS = register(
        "oxidized_copper_tile_stairs",
        settings -> new WeatheringCopperStairBlock(WeatherState.OXIDIZED, OXIDIZED_COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final StairBlock WAXED_COPPER_TILE_STAIRS = register(
        "waxed_copper_tile_stairs",
        settings -> new StairBlock(COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
    );
    public static final StairBlock WAXED_EXPOSED_COPPER_TILE_STAIRS = register(
        "waxed_exposed_copper_tile_stairs",
        settings -> new StairBlock(EXPOSED_COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.EXPOSED_COPPER)
    );
    public static final StairBlock WAXED_WEATHERED_COPPER_TILE_STAIRS = register(
        "waxed_weathered_copper_tile_stairs",
        settings -> new StairBlock(WEATHERED_COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.WEATHERED_COPPER)
    );
    public static final StairBlock WAXED_OXIDIZED_COPPER_TILE_STAIRS = register(
        "waxed_oxidized_copper_tile_stairs",
        settings -> new StairBlock(OXIDIZED_COPPER_TILES.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.OXIDIZED_COPPER)
    );
    public static final TransparentBlock TILED_GLASS = register(
        "tiled_glass",
        TransparentBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
    );
    public static final ConnectedGlassBlock FRAMED_GLASS = register(
        "framed_glass",
        ConnectedGlassBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
            .isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final ConnectedGlassBlock HORIZONTAL_FRAMED_GLASS = register(
        "horizontal_framed_glass",
        ConnectedGlassBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
            .isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final ConnectedGlassBlock VERTICAL_FRAMED_GLASS = register(
        "vertical_framed_glass",
        ConnectedGlassBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isValidSpawn(AllBlocks::never).isRedstoneConductor(AllBlocks::never)
            .isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final GlassPaneBlock TILED_GLASS_PANE = register(
        "tiled_glass_pane",
        GlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock FRAMED_GLASS_PANE = register(
        "framed_glass_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock HORIZONTAL_FRAMED_GLASS_PANE = register(
        "horizontal_framed_glass_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final ConnectedGlassPaneBlock VERTICAL_FRAMED_GLASS_PANE = register(
        "vertical_framed_glass_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)
    );
    public static final WindowBlock OAK_WINDOW = register(
        "oak_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.WOOD).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock SPRUCE_WINDOW = register(
        "spruce_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.PODZOL).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock BIRCH_WINDOW = register(
        "birch_window",
        WindowBlock::translucent,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.SAND).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock JUNGLE_WINDOW = register(
        "jungle_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.DIRT).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock ACACIA_WINDOW = register(
        "acacia_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_ORANGE).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock DARK_OAK_WINDOW = register(
        "dark_oak_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_BROWN).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock MANGROVE_WINDOW = register(
        "mangrove_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_RED).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock CRIMSON_WINDOW = register(
        "crimson_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.CRIMSON_STEM).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock WARPED_WINDOW = register(
        "warped_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.WARPED_STEM).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock CHERRY_WINDOW = register(
        "cherry_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.TERRACOTTA_WHITE).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final WindowBlock BAMBOO_WINDOW = register(
        "bamboo_window",
        WindowBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_YELLOW).isValidSpawn(AllBlocks::never)
            .isRedstoneConductor(AllBlocks::never).isSuffocating(AllBlocks::never).isViewBlocking(AllBlocks::never)
    );
    public static final ConnectedGlassPaneBlock OAK_WINDOW_PANE = register(
        "oak_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.WOOD)
    );
    public static final ConnectedGlassPaneBlock SPRUCE_WINDOW_PANE = register(
        "spruce_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.PODZOL)
    );
    public static final ConnectedGlassPaneBlock BIRCH_WINDOW_PANE = register(
        "birch_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.SAND)
    );
    public static final ConnectedGlassPaneBlock JUNGLE_WINDOW_PANE = register(
        "jungle_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.DIRT)
    );
    public static final ConnectedGlassPaneBlock ACACIA_WINDOW_PANE = register(
        "acacia_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_ORANGE)
    );
    public static final ConnectedGlassPaneBlock DARK_OAK_WINDOW_PANE = register(
        "dark_oak_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_BROWN)
    );
    public static final ConnectedGlassPaneBlock MANGROVE_WINDOW_PANE = register(
        "mangrove_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_RED)
    );
    public static final ConnectedGlassPaneBlock CRIMSON_WINDOW_PANE = register(
        "crimson_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.CRIMSON_STEM)
    );
    public static final ConnectedGlassPaneBlock WARPED_WINDOW_PANE = register(
        "warped_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.WARPED_STEM)
    );
    public static final ConnectedGlassPaneBlock CHERRY_WINDOW_PANE = register(
        "cherry_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_WHITE)
    );
    public static final ConnectedGlassPaneBlock BAMBOO_WINDOW_PANE = register(
        "bamboo_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.COLOR_YELLOW)
    );
    public static final ConnectedGlassPaneBlock ORNATE_IRON_WINDOW_PANE = register(
        "ornate_iron_window_pane",
        ConnectedGlassPaneBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final Block CUT_GRANITE = register("cut_granite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE));
    public static final StairBlock CUT_GRANITE_STAIRS = register(
        "cut_granite_stairs",
        settings -> new StairBlock(CUT_GRANITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock CUT_GRANITE_SLAB = register(
        "cut_granite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock CUT_GRANITE_WALL = register(
        "cut_granite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_GRANITE = register(
        "polished_cut_granite",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final StairBlock POLISHED_CUT_GRANITE_STAIRS = register(
        "polished_cut_granite_stairs",
        settings -> new StairBlock(POLISHED_CUT_GRANITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock POLISHED_CUT_GRANITE_SLAB = register(
        "polished_cut_granite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock POLISHED_CUT_GRANITE_WALL = register(
        "polished_cut_granite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block CUT_GRANITE_BRICKS = register("cut_granite_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE));
    public static final StairBlock CUT_GRANITE_BRICK_STAIRS = register(
        "cut_granite_brick_stairs",
        settings -> new StairBlock(CUT_GRANITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock CUT_GRANITE_BRICK_SLAB = register(
        "cut_granite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock CUT_GRANITE_BRICK_WALL = register(
        "cut_granite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block SMALL_GRANITE_BRICKS = register(
        "small_granite_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final StairBlock SMALL_GRANITE_BRICK_STAIRS = register(
        "small_granite_brick_stairs",
        settings -> new StairBlock(SMALL_GRANITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final SlabBlock SMALL_GRANITE_BRICK_SLAB = register(
        "small_granite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final WallBlock SMALL_GRANITE_BRICK_WALL = register(
        "small_granite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE).forceSolidOn()
    );
    public static final Block LAYERED_GRANITE = register("layered_granite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE));
    public static final ConnectedPillarBlock GRANITE_PILLAR = register(
        "granite_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE)
    );
    public static final Block CUT_DIORITE = register("cut_diorite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE));
    public static final StairBlock CUT_DIORITE_STAIRS = register(
        "cut_diorite_stairs",
        settings -> new StairBlock(CUT_DIORITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock CUT_DIORITE_SLAB = register(
        "cut_diorite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock CUT_DIORITE_WALL = register(
        "cut_diorite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_DIORITE = register(
        "polished_cut_diorite",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final StairBlock POLISHED_CUT_DIORITE_STAIRS = register(
        "polished_cut_diorite_stairs",
        settings -> new StairBlock(POLISHED_CUT_DIORITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock POLISHED_CUT_DIORITE_SLAB = register(
        "polished_cut_diorite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock POLISHED_CUT_DIORITE_WALL = register(
        "polished_cut_diorite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block CUT_DIORITE_BRICKS = register("cut_diorite_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE));
    public static final StairBlock CUT_DIORITE_BRICK_STAIRS = register(
        "cut_diorite_brick_stairs",
        settings -> new StairBlock(CUT_DIORITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock CUT_DIORITE_BRICK_SLAB = register(
        "cut_diorite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock CUT_DIORITE_BRICK_WALL = register(
        "cut_diorite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block SMALL_DIORITE_BRICKS = register(
        "small_diorite_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final StairBlock SMALL_DIORITE_BRICK_STAIRS = register(
        "small_diorite_brick_stairs",
        settings -> new StairBlock(SMALL_DIORITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final SlabBlock SMALL_DIORITE_BRICK_SLAB = register(
        "small_diorite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final WallBlock SMALL_DIORITE_BRICK_WALL = register(
        "small_diorite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE).forceSolidOn()
    );
    public static final Block LAYERED_DIORITE = register("layered_diorite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE));
    public static final ConnectedPillarBlock DIORITE_PILLAR = register(
        "diorite_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
    );
    public static final Block CUT_ANDESITE = register("cut_andesite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));
    public static final StairBlock CUT_ANDESITE_STAIRS = register(
        "cut_andesite_stairs",
        settings -> new StairBlock(CUT_ANDESITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock CUT_ANDESITE_SLAB = register(
        "cut_andesite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock CUT_ANDESITE_WALL = register(
        "cut_andesite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_ANDESITE = register(
        "polished_cut_andesite",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock POLISHED_CUT_ANDESITE_STAIRS = register(
        "polished_cut_andesite_stairs",
        settings -> new StairBlock(POLISHED_CUT_ANDESITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock POLISHED_CUT_ANDESITE_SLAB = register(
        "polished_cut_andesite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock POLISHED_CUT_ANDESITE_WALL = register(
        "polished_cut_andesite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block CUT_ANDESITE_BRICKS = register(
        "cut_andesite_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock CUT_ANDESITE_BRICK_STAIRS = register(
        "cut_andesite_brick_stairs",
        settings -> new StairBlock(CUT_ANDESITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock CUT_ANDESITE_BRICK_SLAB = register(
        "cut_andesite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock CUT_ANDESITE_BRICK_WALL = register(
        "cut_andesite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block SMALL_ANDESITE_BRICKS = register(
        "small_andesite_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final StairBlock SMALL_ANDESITE_BRICK_STAIRS = register(
        "small_andesite_brick_stairs",
        settings -> new StairBlock(SMALL_ANDESITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final SlabBlock SMALL_ANDESITE_BRICK_SLAB = register(
        "small_andesite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final WallBlock SMALL_ANDESITE_BRICK_WALL = register(
        "small_andesite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).forceSolidOn()
    );
    public static final Block LAYERED_ANDESITE = register("layered_andesite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));
    public static final ConnectedPillarBlock ANDESITE_PILLAR = register(
        "andesite_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
    );
    public static final Block CUT_CALCITE = register("cut_calcite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE));
    public static final StairBlock CUT_CALCITE_STAIRS = register(
        "cut_calcite_stairs",
        settings -> new StairBlock(CUT_CALCITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock CUT_CALCITE_SLAB = register(
        "cut_calcite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock CUT_CALCITE_WALL = register(
        "cut_calcite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_CALCITE = register(
        "polished_cut_calcite",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final StairBlock POLISHED_CUT_CALCITE_STAIRS = register(
        "polished_cut_calcite_stairs",
        settings -> new StairBlock(POLISHED_CUT_CALCITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock POLISHED_CUT_CALCITE_SLAB = register(
        "polished_cut_calcite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock POLISHED_CUT_CALCITE_WALL = register(
        "polished_cut_calcite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block CUT_CALCITE_BRICKS = register("cut_calcite_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE));
    public static final StairBlock CUT_CALCITE_BRICK_STAIRS = register(
        "cut_calcite_brick_stairs",
        settings -> new StairBlock(CUT_CALCITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock CUT_CALCITE_BRICK_SLAB = register(
        "cut_calcite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock CUT_CALCITE_BRICK_WALL = register(
        "cut_calcite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block SMALL_CALCITE_BRICKS = register(
        "small_calcite_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final StairBlock SMALL_CALCITE_BRICK_STAIRS = register(
        "small_calcite_brick_stairs",
        settings -> new StairBlock(SMALL_CALCITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final SlabBlock SMALL_CALCITE_BRICK_SLAB = register(
        "small_calcite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final WallBlock SMALL_CALCITE_BRICK_WALL = register(
        "small_calcite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).forceSolidOn()
    );
    public static final Block LAYERED_CALCITE = register("layered_calcite", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE));
    public static final ConnectedPillarBlock CALCITE_PILLAR = register(
        "calcite_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
    );
    public static final Block CUT_DRIPSTONE = register("cut_dripstone", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK));
    public static final StairBlock CUT_DRIPSTONE_STAIRS = register(
        "cut_dripstone_stairs",
        settings -> new StairBlock(CUT_DRIPSTONE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock CUT_DRIPSTONE_SLAB = register(
        "cut_dripstone_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock CUT_DRIPSTONE_WALL = register(
        "cut_dripstone_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block POLISHED_CUT_DRIPSTONE = register(
        "polished_cut_dripstone",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock POLISHED_CUT_DRIPSTONE_STAIRS = register(
        "polished_cut_dripstone_stairs",
        settings -> new StairBlock(POLISHED_CUT_DRIPSTONE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock POLISHED_CUT_DRIPSTONE_SLAB = register(
        "polished_cut_dripstone_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock POLISHED_CUT_DRIPSTONE_WALL = register(
        "polished_cut_dripstone_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block CUT_DRIPSTONE_BRICKS = register(
        "cut_dripstone_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock CUT_DRIPSTONE_BRICK_STAIRS = register(
        "cut_dripstone_brick_stairs",
        settings -> new StairBlock(CUT_DRIPSTONE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock CUT_DRIPSTONE_BRICK_SLAB = register(
        "cut_dripstone_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock CUT_DRIPSTONE_BRICK_WALL = register(
        "cut_dripstone_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block SMALL_DRIPSTONE_BRICKS = register(
        "small_dripstone_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final StairBlock SMALL_DRIPSTONE_BRICK_STAIRS = register(
        "small_dripstone_brick_stairs",
        settings -> new StairBlock(SMALL_DRIPSTONE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final SlabBlock SMALL_DRIPSTONE_BRICK_SLAB = register(
        "small_dripstone_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final WallBlock SMALL_DRIPSTONE_BRICK_WALL = register(
        "small_dripstone_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK).forceSolidOn()
    );
    public static final Block LAYERED_DRIPSTONE = register(
        "layered_dripstone",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final ConnectedPillarBlock DRIPSTONE_PILLAR = register(
        "dripstone_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DRIPSTONE_BLOCK)
    );
    public static final Block CUT_DEEPSLATE = register("cut_deepslate", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));
    public static final StairBlock CUT_DEEPSLATE_STAIRS = register(
        "cut_deepslate_stairs",
        settings -> new StairBlock(CUT_DEEPSLATE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock CUT_DEEPSLATE_SLAB = register(
        "cut_deepslate_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock CUT_DEEPSLATE_WALL = register(
        "cut_deepslate_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_DEEPSLATE = register(
        "polished_cut_deepslate",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock POLISHED_CUT_DEEPSLATE_STAIRS = register(
        "polished_cut_deepslate_stairs",
        settings -> new StairBlock(POLISHED_CUT_DEEPSLATE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock POLISHED_CUT_DEEPSLATE_SLAB = register(
        "polished_cut_deepslate_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock POLISHED_CUT_DEEPSLATE_WALL = register(
        "polished_cut_deepslate_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block CUT_DEEPSLATE_BRICKS = register(
        "cut_deepslate_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock CUT_DEEPSLATE_BRICK_STAIRS = register(
        "cut_deepslate_brick_stairs",
        settings -> new StairBlock(CUT_DEEPSLATE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock CUT_DEEPSLATE_BRICK_SLAB = register(
        "cut_deepslate_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock CUT_DEEPSLATE_BRICK_WALL = register(
        "cut_deepslate_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block SMALL_DEEPSLATE_BRICKS = register(
        "small_deepslate_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final StairBlock SMALL_DEEPSLATE_BRICK_STAIRS = register(
        "small_deepslate_brick_stairs",
        settings -> new StairBlock(SMALL_DEEPSLATE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final SlabBlock SMALL_DEEPSLATE_BRICK_SLAB = register(
        "small_deepslate_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final WallBlock SMALL_DEEPSLATE_BRICK_WALL = register(
        "small_deepslate_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).forceSolidOn()
    );
    public static final Block LAYERED_DEEPSLATE = register("layered_deepslate", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));
    public static final ConnectedPillarBlock DEEPSLATE_PILLAR = register(
        "deepslate_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
    );
    public static final Block CUT_TUFF = register("cut_tuff", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));
    public static final StairBlock CUT_TUFF_STAIRS = register(
        "cut_tuff_stairs",
        settings -> new StairBlock(CUT_TUFF.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock CUT_TUFF_SLAB = register("cut_tuff_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));
    public static final WallBlock CUT_TUFF_WALL = register(
        "cut_tuff_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block POLISHED_CUT_TUFF = register("polished_cut_tuff", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));
    public static final StairBlock POLISHED_CUT_TUFF_STAIRS = register(
        "polished_cut_tuff_stairs",
        settings -> new StairBlock(POLISHED_CUT_TUFF.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock POLISHED_CUT_TUFF_SLAB = register(
        "polished_cut_tuff_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock POLISHED_CUT_TUFF_WALL = register(
        "polished_cut_tuff_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block CUT_TUFF_BRICKS = register("cut_tuff_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));
    public static final StairBlock CUT_TUFF_BRICK_STAIRS = register(
        "cut_tuff_brick_stairs",
        settings -> new StairBlock(CUT_TUFF_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock CUT_TUFF_BRICK_SLAB = register(
        "cut_tuff_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock CUT_TUFF_BRICK_WALL = register(
        "cut_tuff_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block SMALL_TUFF_BRICKS = register("small_tuff_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));
    public static final StairBlock SMALL_TUFF_BRICK_STAIRS = register(
        "small_tuff_brick_stairs",
        settings -> new StairBlock(SMALL_TUFF_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final SlabBlock SMALL_TUFF_BRICK_SLAB = register(
        "small_tuff_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final WallBlock SMALL_TUFF_BRICK_WALL = register(
        "small_tuff_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).forceSolidOn()
    );
    public static final Block LAYERED_TUFF = register("layered_tuff", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));
    public static final ConnectedPillarBlock TUFF_PILLAR = register(
        "tuff_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
    );
    public static final Block ASURINE = register(
        "asurine",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_BLUE).destroyTime(1.25f)
    );
    public static final Block CUT_ASURINE = register("cut_asurine", Block::new, BlockBehaviour.Properties.ofFullCopy(ASURINE));
    public static final StairBlock CUT_ASURINE_STAIRS = register(
        "cut_asurine_stairs",
        settings -> new StairBlock(CUT_ASURINE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock CUT_ASURINE_SLAB = register("cut_asurine_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(ASURINE));
    public static final WallBlock CUT_ASURINE_WALL = register(
        "cut_asurine_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_ASURINE = register("polished_cut_asurine", Block::new, BlockBehaviour.Properties.ofFullCopy(ASURINE));
    public static final StairBlock POLISHED_CUT_ASURINE_STAIRS = register(
        "polished_cut_asurine_stairs",
        settings -> new StairBlock(POLISHED_CUT_ASURINE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock POLISHED_CUT_ASURINE_SLAB = register(
        "polished_cut_asurine_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock POLISHED_CUT_ASURINE_WALL = register(
        "polished_cut_asurine_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block CUT_ASURINE_BRICKS = register("cut_asurine_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(ASURINE));
    public static final StairBlock CUT_ASURINE_BRICK_STAIRS = register(
        "cut_asurine_brick_stairs",
        settings -> new StairBlock(CUT_ASURINE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock CUT_ASURINE_BRICK_SLAB = register(
        "cut_asurine_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock CUT_ASURINE_BRICK_WALL = register(
        "cut_asurine_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block SMALL_ASURINE_BRICKS = register("small_asurine_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(ASURINE));
    public static final StairBlock SMALL_ASURINE_BRICK_STAIRS = register(
        "small_asurine_brick_stairs",
        settings -> new StairBlock(SMALL_ASURINE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final SlabBlock SMALL_ASURINE_BRICK_SLAB = register(
        "small_asurine_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final WallBlock SMALL_ASURINE_BRICK_WALL = register(
        "small_asurine_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE).forceSolidOn()
    );
    public static final Block LAYERED_ASURINE = register("layered_asurine", Block::new, BlockBehaviour.Properties.ofFullCopy(ASURINE));
    public static final ConnectedPillarBlock ASURINE_PILLAR = register(
        "asurine_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(ASURINE)
    );
    public static final Block CRIMSITE = register(
        "crimsite",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_RED).destroyTime(1.25f)
    );
    public static final Block CUT_CRIMSITE = register("cut_crimsite", Block::new, BlockBehaviour.Properties.ofFullCopy(CRIMSITE));
    public static final StairBlock CUT_CRIMSITE_STAIRS = register(
        "cut_crimsite_stairs",
        settings -> new StairBlock(CUT_CRIMSITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock CUT_CRIMSITE_SLAB = register("cut_crimsite_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(CRIMSITE));
    public static final WallBlock CUT_CRIMSITE_WALL = register(
        "cut_crimsite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_CRIMSITE = register("polished_cut_crimsite", Block::new, BlockBehaviour.Properties.ofFullCopy(CRIMSITE));
    public static final StairBlock POLISHED_CUT_CRIMSITE_STAIRS = register(
        "polished_cut_crimsite_stairs",
        settings -> new StairBlock(POLISHED_CUT_CRIMSITE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock POLISHED_CUT_CRIMSITE_SLAB = register(
        "polished_cut_crimsite_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock POLISHED_CUT_CRIMSITE_WALL = register(
        "polished_cut_crimsite_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block CUT_CRIMSITE_BRICKS = register("cut_crimsite_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(CRIMSITE));
    public static final StairBlock CUT_CRIMSITE_BRICK_STAIRS = register(
        "cut_crimsite_brick_stairs",
        settings -> new StairBlock(CUT_CRIMSITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock CUT_CRIMSITE_BRICK_SLAB = register(
        "cut_crimsite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock CUT_CRIMSITE_BRICK_WALL = register(
        "cut_crimsite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block SMALL_CRIMSITE_BRICKS = register("small_crimsite_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(CRIMSITE));
    public static final StairBlock SMALL_CRIMSITE_BRICK_STAIRS = register(
        "small_crimsite_brick_stairs",
        settings -> new StairBlock(SMALL_CRIMSITE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final SlabBlock SMALL_CRIMSITE_BRICK_SLAB = register(
        "small_crimsite_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final WallBlock SMALL_CRIMSITE_BRICK_WALL = register(
        "small_crimsite_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE).forceSolidOn()
    );
    public static final Block LAYERED_CRIMSITE = register("layered_crimsite", Block::new, BlockBehaviour.Properties.ofFullCopy(CRIMSITE));
    public static final ConnectedPillarBlock CRIMSITE_PILLAR = register(
        "crimsite_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(CRIMSITE)
    );
    public static final Block LIMESTONE = register(
        "limestone",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE).mapColor(MapColor.SAND).destroyTime(1.25f)
    );
    public static final Block CUT_LIMESTONE = register("cut_limestone", Block::new, BlockBehaviour.Properties.ofFullCopy(LIMESTONE));
    public static final StairBlock CUT_LIMESTONE_STAIRS = register(
        "cut_limestone_stairs",
        settings -> new StairBlock(CUT_LIMESTONE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock CUT_LIMESTONE_SLAB = register(
        "cut_limestone_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock CUT_LIMESTONE_WALL = register(
        "cut_limestone_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block POLISHED_CUT_LIMESTONE = register(
        "polished_cut_limestone",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final StairBlock POLISHED_CUT_LIMESTONE_STAIRS = register(
        "polished_cut_limestone_stairs",
        settings -> new StairBlock(POLISHED_CUT_LIMESTONE.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock POLISHED_CUT_LIMESTONE_SLAB = register(
        "polished_cut_limestone_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock POLISHED_CUT_LIMESTONE_WALL = register(
        "polished_cut_limestone_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block CUT_LIMESTONE_BRICKS = register("cut_limestone_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(LIMESTONE));
    public static final StairBlock CUT_LIMESTONE_BRICK_STAIRS = register(
        "cut_limestone_brick_stairs",
        settings -> new StairBlock(CUT_LIMESTONE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock CUT_LIMESTONE_BRICK_SLAB = register(
        "cut_limestone_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock CUT_LIMESTONE_BRICK_WALL = register(
        "cut_limestone_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block SMALL_LIMESTONE_BRICKS = register(
        "small_limestone_bricks",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final StairBlock SMALL_LIMESTONE_BRICK_STAIRS = register(
        "small_limestone_brick_stairs",
        settings -> new StairBlock(SMALL_LIMESTONE_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final SlabBlock SMALL_LIMESTONE_BRICK_SLAB = register(
        "small_limestone_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final WallBlock SMALL_LIMESTONE_BRICK_WALL = register(
        "small_limestone_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE).forceSolidOn()
    );
    public static final Block LAYERED_LIMESTONE = register("layered_limestone", Block::new, BlockBehaviour.Properties.ofFullCopy(LIMESTONE));
    public static final ConnectedPillarBlock LIMESTONE_PILLAR = register(
        "limestone_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(LIMESTONE)
    );
    public static final Block OCHRUM = register(
        "ochrum",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE).mapColor(MapColor.TERRACOTTA_YELLOW).destroyTime(1.25f)
    );
    public static final Block CUT_OCHRUM = register("cut_ochrum", Block::new, BlockBehaviour.Properties.ofFullCopy(OCHRUM));
    public static final StairBlock CUT_OCHRUM_STAIRS = register(
        "cut_ochrum_stairs",
        settings -> new StairBlock(CUT_OCHRUM.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock CUT_OCHRUM_SLAB = register("cut_ochrum_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(OCHRUM));
    public static final WallBlock CUT_OCHRUM_WALL = register(
        "cut_ochrum_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block POLISHED_CUT_OCHRUM = register("polished_cut_ochrum", Block::new, BlockBehaviour.Properties.ofFullCopy(OCHRUM));
    public static final StairBlock POLISHED_CUT_OCHRUM_STAIRS = register(
        "polished_cut_ochrum_stairs",
        settings -> new StairBlock(POLISHED_CUT_OCHRUM.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock POLISHED_CUT_OCHRUM_SLAB = register(
        "polished_cut_ochrum_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock POLISHED_CUT_OCHRUM_WALL = register(
        "polished_cut_ochrum_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block CUT_OCHRUM_BRICKS = register("cut_ochrum_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(OCHRUM));
    public static final StairBlock CUT_OCHRUM_BRICK_STAIRS = register(
        "cut_ochrum_brick_stairs",
        settings -> new StairBlock(CUT_OCHRUM_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock CUT_OCHRUM_BRICK_SLAB = register(
        "cut_ochrum_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock CUT_OCHRUM_BRICK_WALL = register(
        "cut_ochrum_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block SMALL_OCHRUM_BRICKS = register("small_ochrum_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(OCHRUM));
    public static final StairBlock SMALL_OCHRUM_BRICK_STAIRS = register(
        "small_ochrum_brick_stairs",
        settings -> new StairBlock(SMALL_OCHRUM_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final SlabBlock SMALL_OCHRUM_BRICK_SLAB = register(
        "small_ochrum_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final WallBlock SMALL_OCHRUM_BRICK_WALL = register(
        "small_ochrum_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM).forceSolidOn()
    );
    public static final Block LAYERED_OCHRUM = register("layered_ochrum", Block::new, BlockBehaviour.Properties.ofFullCopy(OCHRUM));
    public static final ConnectedPillarBlock OCHRUM_PILLAR = register(
        "ochrum_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(OCHRUM)
    );
    public static final Block SCORIA = register(
        "scoria",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE).mapColor(MapColor.COLOR_BROWN)
    );
    public static final Block CUT_SCORIA = register("cut_scoria", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORIA));
    public static final StairBlock CUT_SCORIA_STAIRS = register(
        "cut_scoria_stairs",
        settings -> new StairBlock(CUT_SCORIA.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock CUT_SCORIA_SLAB = register("cut_scoria_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SCORIA));
    public static final WallBlock CUT_SCORIA_WALL = register(
        "cut_scoria_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block POLISHED_CUT_SCORIA = register("polished_cut_scoria", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORIA));
    public static final StairBlock POLISHED_CUT_SCORIA_STAIRS = register(
        "polished_cut_scoria_stairs",
        settings -> new StairBlock(POLISHED_CUT_SCORIA.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock POLISHED_CUT_SCORIA_SLAB = register(
        "polished_cut_scoria_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock POLISHED_CUT_SCORIA_WALL = register(
        "polished_cut_scoria_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block CUT_SCORIA_BRICKS = register("cut_scoria_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORIA));
    public static final StairBlock CUT_SCORIA_BRICK_STAIRS = register(
        "cut_scoria_brick_stairs",
        settings -> new StairBlock(CUT_SCORIA_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock CUT_SCORIA_BRICK_SLAB = register(
        "cut_scoria_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock CUT_SCORIA_BRICK_WALL = register(
        "cut_scoria_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block SMALL_SCORIA_BRICKS = register("small_scoria_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORIA));
    public static final StairBlock SMALL_SCORIA_BRICK_STAIRS = register(
        "small_scoria_brick_stairs",
        settings -> new StairBlock(SMALL_SCORIA_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final SlabBlock SMALL_SCORIA_BRICK_SLAB = register(
        "small_scoria_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final WallBlock SMALL_SCORIA_BRICK_WALL = register(
        "small_scoria_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA).forceSolidOn()
    );
    public static final Block LAYERED_SCORIA = register("layered_scoria", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORIA));
    public static final ConnectedPillarBlock SCORIA_PILLAR = register(
        "scoria_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORIA)
    );
    public static final Block SCORCHIA = register(
        "scorchia",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE).mapColor(MapColor.TERRACOTTA_GRAY).destroyTime(1.25f)
    );
    public static final Block CUT_SCORCHIA = register("cut_scorchia", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORCHIA));
    public static final StairBlock CUT_SCORCHIA_STAIRS = register(
        "cut_scorchia_stairs",
        settings -> new StairBlock(CUT_SCORCHIA.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock CUT_SCORCHIA_SLAB = register("cut_scorchia_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SCORCHIA));
    public static final WallBlock CUT_SCORCHIA_WALL = register(
        "cut_scorchia_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block POLISHED_CUT_SCORCHIA = register("polished_cut_scorchia", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORCHIA));
    public static final StairBlock POLISHED_CUT_SCORCHIA_STAIRS = register(
        "polished_cut_scorchia_stairs",
        settings -> new StairBlock(POLISHED_CUT_SCORCHIA.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock POLISHED_CUT_SCORCHIA_SLAB = register(
        "polished_cut_scorchia_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock POLISHED_CUT_SCORCHIA_WALL = register(
        "polished_cut_scorchia_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block CUT_SCORCHIA_BRICKS = register("cut_scorchia_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORCHIA));
    public static final StairBlock CUT_SCORCHIA_BRICK_STAIRS = register(
        "cut_scorchia_brick_stairs",
        settings -> new StairBlock(CUT_SCORCHIA_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock CUT_SCORCHIA_BRICK_SLAB = register(
        "cut_scorchia_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock CUT_SCORCHIA_BRICK_WALL = register(
        "cut_scorchia_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block SMALL_SCORCHIA_BRICKS = register("small_scorchia_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORCHIA));
    public static final StairBlock SMALL_SCORCHIA_BRICK_STAIRS = register(
        "small_scorchia_brick_stairs",
        settings -> new StairBlock(SMALL_SCORCHIA_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final SlabBlock SMALL_SCORCHIA_BRICK_SLAB = register(
        "small_scorchia_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final WallBlock SMALL_SCORCHIA_BRICK_WALL = register(
        "small_scorchia_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA).forceSolidOn()
    );
    public static final Block LAYERED_SCORCHIA = register("layered_scorchia", Block::new, BlockBehaviour.Properties.ofFullCopy(SCORCHIA));
    public static final ConnectedPillarBlock SCORCHIA_PILLAR = register(
        "scorchia_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(SCORCHIA)
    );
    public static final Block VERIDIUM = register(
        "veridium",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).mapColor(MapColor.WARPED_NYLIUM).destroyTime(1.25f)
    );
    public static final Block CUT_VERIDIUM = register("cut_veridium", Block::new, BlockBehaviour.Properties.ofFullCopy(VERIDIUM));
    public static final StairBlock CUT_VERIDIUM_STAIRS = register(
        "cut_veridium_stairs",
        settings -> new StairBlock(CUT_VERIDIUM.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock CUT_VERIDIUM_SLAB = register("cut_veridium_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(VERIDIUM));
    public static final WallBlock CUT_VERIDIUM_WALL = register(
        "cut_veridium_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block POLISHED_CUT_VERIDIUM = register("polished_cut_veridium", Block::new, BlockBehaviour.Properties.ofFullCopy(VERIDIUM));
    public static final StairBlock POLISHED_CUT_VERIDIUM_STAIRS = register(
        "polished_cut_veridium_stairs",
        settings -> new StairBlock(POLISHED_CUT_VERIDIUM.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock POLISHED_CUT_VERIDIUM_SLAB = register(
        "polished_cut_veridium_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock POLISHED_CUT_VERIDIUM_WALL = register(
        "polished_cut_veridium_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block CUT_VERIDIUM_BRICKS = register("cut_veridium_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(VERIDIUM));
    public static final StairBlock CUT_VERIDIUM_BRICK_STAIRS = register(
        "cut_veridium_brick_stairs",
        settings -> new StairBlock(CUT_VERIDIUM_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock CUT_VERIDIUM_BRICK_SLAB = register(
        "cut_veridium_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock CUT_VERIDIUM_BRICK_WALL = register(
        "cut_veridium_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block SMALL_VERIDIUM_BRICKS = register("small_veridium_bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(VERIDIUM));
    public static final StairBlock SMALL_VERIDIUM_BRICK_STAIRS = register(
        "small_veridium_brick_stairs",
        settings -> new StairBlock(SMALL_VERIDIUM_BRICKS.defaultBlockState(), settings),
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final SlabBlock SMALL_VERIDIUM_BRICK_SLAB = register(
        "small_veridium_brick_slab",
        SlabBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final WallBlock SMALL_VERIDIUM_BRICK_WALL = register(
        "small_veridium_brick_wall",
        WallBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM).forceSolidOn()
    );
    public static final Block LAYERED_VERIDIUM = register("layered_veridium", Block::new, BlockBehaviour.Properties.ofFullCopy(VERIDIUM));
    public static final ConnectedPillarBlock VERIDIUM_PILLAR = register(
        "veridium_pillar",
        ConnectedPillarBlock::new,
        BlockBehaviour.Properties.ofFullCopy(VERIDIUM)
    );
    public static final Block COPYCAT_BASE = register(
        "copycat_base",
        Block::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.GLOW_LICHEN)
    );
    public static final WrenchableDirectionalBlock COPYCAT_BARS = register(
        "copycat_bars",
        WrenchableDirectionalBlock::new,
        BlockBehaviour.Properties.of()
    );
    public static final CopycatStepBlock COPYCAT_STEP = register(
        "copycat_step",
        CopycatStepBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).forceSolidOn().noOcclusion().mapColor(MapColor.NONE).isValidSpawn(AllBlocks::never)
    );
    public static final CopycatPanelBlock COPYCAT_PANEL = register(
        "copycat_panel",
        CopycatPanelBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.NONE).isValidSpawn(AllBlocks::never)
    );

    public static final FluidBlock HONEY = register(
        AllFluids.HONEY,
        FluidBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_YELLOW)
    );
    public static final FluidBlock CHOCOLATE = register(
        AllFluids.CHOCOLATE,
        FluidBlock::new,
        BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_BROWN)
    );

    private static boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
        return false;
    }

    private static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    private static <T extends FluidBlock> T register(
        FlowableFluid fluid,
        BiFunction<FlowableFluid, BlockBehaviour.Properties, T> factory,
        BlockBehaviour.Properties settings
    ) {
        T block = register(BuiltInRegistries.FLUID.getKey(fluid).getPath(), blockSettings -> factory.apply(fluid, blockSettings), settings);
        fluid.getEntry().block = block;
        return block;
    }

    private static <T extends Block> T register(String id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties settings) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, id));
        T block = factory.apply(settings.setId(key));
        ALL.add(block);
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
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
