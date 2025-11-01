package com.zurrtum.create.client;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ContraptionControlsRenderer;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterRenderer;
import com.zurrtum.create.client.content.contraptions.actors.psi.PSIVisual;
import com.zurrtum.create.client.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import com.zurrtum.create.client.content.contraptions.actors.roller.RollerRenderer;
import com.zurrtum.create.client.content.contraptions.bearing.BearingRenderer;
import com.zurrtum.create.client.content.contraptions.bearing.BearingVisual;
import com.zurrtum.create.client.content.contraptions.chassis.StickerRenderer;
import com.zurrtum.create.client.content.contraptions.chassis.StickerVisual;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorPulleyRenderer;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorPulleyVisual;
import com.zurrtum.create.client.content.contraptions.gantry.GantryCarriageRenderer;
import com.zurrtum.create.client.content.contraptions.gantry.GantryCarriageVisual;
import com.zurrtum.create.client.content.contraptions.piston.MechanicalPistonRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.PulleyRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.RopePulleyVisual;
import com.zurrtum.create.client.content.decoration.placard.PlacardRenderer;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.client.content.decoration.steamWhistle.WhistleRenderer;
import com.zurrtum.create.client.content.equipment.armor.BacktankRenderer;
import com.zurrtum.create.client.content.equipment.bell.BellRenderer;
import com.zurrtum.create.client.content.equipment.toolbox.ToolBoxVisual;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxRenderer;
import com.zurrtum.create.client.content.fluids.PumpRenderer;
import com.zurrtum.create.client.content.fluids.drain.ItemDrainRenderer;
import com.zurrtum.create.client.content.fluids.hosePulley.HosePulleyRenderer;
import com.zurrtum.create.client.content.fluids.hosePulley.HosePulleyVisual;
import com.zurrtum.create.client.content.fluids.pipes.GlassPipeVisual;
import com.zurrtum.create.client.content.fluids.pipes.TransparentStraightPipeRenderer;
import com.zurrtum.create.client.content.fluids.pipes.valve.FluidValveRenderer;
import com.zurrtum.create.client.content.fluids.pipes.valve.FluidValveVisual;
import com.zurrtum.create.client.content.fluids.spout.SpoutRenderer;
import com.zurrtum.create.client.content.fluids.tank.FluidTankRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.content.kinetics.belt.BeltRenderer;
import com.zurrtum.create.client.content.kinetics.belt.BeltVisual;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorVisual;
import com.zurrtum.create.client.content.kinetics.clock.CuckooClockRenderer;
import com.zurrtum.create.client.content.kinetics.crafter.MechanicalCrafterRenderer;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankRenderer;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankVisual;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleRenderer;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleVisual;
import com.zurrtum.create.client.content.kinetics.deployer.DeployerRenderer;
import com.zurrtum.create.client.content.kinetics.deployer.DeployerVisual;
import com.zurrtum.create.client.content.kinetics.drill.DrillRenderer;
import com.zurrtum.create.client.content.kinetics.fan.EncasedFanRenderer;
import com.zurrtum.create.client.content.kinetics.fan.FanVisual;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelRenderer;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelVisual;
import com.zurrtum.create.client.content.kinetics.gauge.GaugeRenderer;
import com.zurrtum.create.client.content.kinetics.gauge.GaugeVisual;
import com.zurrtum.create.client.content.kinetics.gearbox.GearboxRenderer;
import com.zurrtum.create.client.content.kinetics.gearbox.GearboxVisual;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmRenderer;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmVisual;
import com.zurrtum.create.client.content.kinetics.millstone.MillstoneRenderer;
import com.zurrtum.create.client.content.kinetics.mixer.MechanicalMixerRenderer;
import com.zurrtum.create.client.content.kinetics.mixer.MixerVisual;
import com.zurrtum.create.client.content.kinetics.motor.CreativeMotorRenderer;
import com.zurrtum.create.client.content.kinetics.press.MechanicalPressRenderer;
import com.zurrtum.create.client.content.kinetics.press.PressVisual;
import com.zurrtum.create.client.content.kinetics.saw.SawRenderer;
import com.zurrtum.create.client.content.kinetics.saw.SawVisual;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.zurrtum.create.client.content.kinetics.speedController.SpeedControllerRenderer;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineRenderer;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineVisual;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftVisual;
import com.zurrtum.create.client.content.kinetics.waterwheel.WaterWheelRenderer;
import com.zurrtum.create.client.content.kinetics.waterwheel.WaterWheelVisual;
import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer;
import com.zurrtum.create.client.content.logistics.chute.SmartChuteRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.logistics.depot.EjectorRenderer;
import com.zurrtum.create.client.content.logistics.depot.EjectorVisual;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelRenderer;
import com.zurrtum.create.client.content.logistics.funnel.FunnelRenderer;
import com.zurrtum.create.client.content.logistics.funnel.FunnelVisual;
import com.zurrtum.create.client.content.logistics.packagePort.frogport.FrogportRenderer;
import com.zurrtum.create.client.content.logistics.packagePort.frogport.FrogportVisual;
import com.zurrtum.create.client.content.logistics.packagePort.postbox.PostboxRenderer;
import com.zurrtum.create.client.content.logistics.packager.PackagerRenderer;
import com.zurrtum.create.client.content.logistics.packager.PackagerVisual;
import com.zurrtum.create.client.content.logistics.tableCloth.TableClothRenderer;
import com.zurrtum.create.client.content.logistics.tunnel.BeltTunnelRenderer;
import com.zurrtum.create.client.content.logistics.tunnel.BeltTunnelVisual;
import com.zurrtum.create.client.content.processing.basin.BasinRenderer;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerVisual;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverRenderer;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverVisual;
import com.zurrtum.create.client.content.redstone.deskBell.DeskBellRenderer;
import com.zurrtum.create.client.content.redstone.diodes.BrassDiodeRenderer;
import com.zurrtum.create.client.content.redstone.diodes.BrassDiodeVisual;
import com.zurrtum.create.client.content.redstone.displayLink.LinkBulbRenderer;
import com.zurrtum.create.client.content.redstone.link.controller.LecternControllerRenderer;
import com.zurrtum.create.client.content.redstone.nixieTube.NixieTubeRenderer;
import com.zurrtum.create.client.content.schematics.cannon.SchematicannonRenderer;
import com.zurrtum.create.client.content.schematics.cannon.SchematicannonVisual;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityVisual;
import com.zurrtum.create.client.content.trains.display.FlapDisplayRenderer;
import com.zurrtum.create.client.content.trains.observer.TrackObserverRenderer;
import com.zurrtum.create.client.content.trains.observer.TrackObserverVisual;
import com.zurrtum.create.client.content.trains.signal.SignalRenderer;
import com.zurrtum.create.client.content.trains.signal.SignalVisual;
import com.zurrtum.create.client.content.trains.station.StationRenderer;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.content.trains.track.TrackVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

import java.util.function.Predicate;

public class AllBlockEntityRenders {
    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void visual(
        BlockEntityType<P> type,
        BlockEntityRendererFactory<T, S> rendererFactory,
        SimpleBlockEntityVisualizer.Factory<P> visualizerFactory
    ) {
        visual(type, rendererFactory, visualizerFactory, blockEntity -> true);
    }

    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void normal(
        BlockEntityType<P> type,
        BlockEntityRendererFactory<T, S> rendererFactory,
        SimpleBlockEntityVisualizer.Factory<P> visualizerFactory
    ) {
        visual(type, rendererFactory, visualizerFactory, blockEntity -> false);
    }

    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void visual(
        BlockEntityType<P> type,
        BlockEntityRendererFactory<T, S> rendererFactory,
        SimpleBlockEntityVisualizer.Factory<P> visualizerFactory,
        Predicate<P> skipVanillaRender
    ) {
        BlockEntityRendererFactories.register(type, rendererFactory);
        SimpleBlockEntityVisualizer.builder(type).factory(visualizerFactory).skipVanillaRender(skipVanillaRender).apply();
    }

    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void render(
        BlockEntityType<P> type,
        BlockEntityRendererFactory<T, S> rendererFactory
    ) {
        BlockEntityRendererFactories.register(type, rendererFactory);
    }

    public static void register() {
        visual(AllBlockEntityTypes.BRACKETED_KINETIC, BracketedKineticBlockEntityRenderer::new, BracketedKineticBlockEntityVisual::create);
        visual(AllBlockEntityTypes.MOTOR, CreativeMotorRenderer::new, OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF));
        normal(AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER, SpeedControllerRenderer::new, SingleAxisRotatingVisual::shaft);
        visual(AllBlockEntityTypes.WATER_WHEEL, WaterWheelRenderer::standard, WaterWheelVisual::standard);
        visual(AllBlockEntityTypes.LARGE_WATER_WHEEL, WaterWheelRenderer::large, WaterWheelVisual::large);
        render(AllBlockEntityTypes.DEPOT, DepotRenderer::new);
        visual(AllBlockEntityTypes.BELT, BeltRenderer::new, BeltVisual::new, BeltBlockEntity::shouldSkipVanillaRender);
        visual(AllBlockEntityTypes.GEARBOX, GearboxRenderer::new, GearboxVisual::new);
        visual(AllBlockEntityTypes.CLUTCH, SplitShaftRenderer::new, SplitShaftVisual::new);
        visual(AllBlockEntityTypes.GEARSHIFT, SplitShaftRenderer::new, SplitShaftVisual::new);
        visual(AllBlockEntityTypes.SEQUENCED_GEARSHIFT, SplitShaftRenderer::new, SplitShaftVisual::new);
        visual(AllBlockEntityTypes.ENCASED_SHAFT, ShaftRenderer::new, SingleAxisRotatingVisual::shaft);
        visual(AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT, ShaftRenderer::new, SingleAxisRotatingVisual::shaft);
        normal(AllBlockEntityTypes.CHAIN_CONVEYOR, ChainConveyorRenderer::new, ChainConveyorVisual::new);
        visual(AllBlockEntityTypes.ENCASED_COGWHEEL, EncasedCogRenderer::small, EncasedCogVisual::small);
        visual(AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL, EncasedCogRenderer::large, EncasedCogVisual::large);
        normal(AllBlockEntityTypes.HAND_CRANK, HandCrankRenderer::new, HandCrankVisual::new);
        normal(AllBlockEntityTypes.VALVE_HANDLE, ValveHandleRenderer::new, ValveHandleVisual::new);
        normal(AllBlockEntityTypes.WINDMILL_BEARING, BearingRenderer::new, BearingVisual::new);
        normal(AllBlockEntityTypes.MECHANICAL_PUMP, PumpRenderer::new, SingleAxisRotatingVisual.ofZ(AllPartialModels.MECHANICAL_PUMP_COG));
        render(AllBlockEntityTypes.FLUID_TANK, FluidTankRenderer::new);
        render(AllBlockEntityTypes.CREATIVE_FLUID_TANK, FluidTankRenderer::new);
        visual(AllBlockEntityTypes.GLASS_FLUID_PIPE, TransparentStraightPipeRenderer::new, GlassPipeVisual::new);
        visual(AllBlockEntityTypes.STEAM_ENGINE, SteamEngineRenderer::new, SteamEngineVisual::new);
        visual(AllBlockEntityTypes.POWERED_SHAFT, KineticBlockEntityRenderer::new, SingleAxisRotatingVisual.of(AllPartialModels.POWERED_SHAFT));
        visual(AllBlockEntityTypes.HEATER, BlazeBurnerRenderer::new, BlazeBurnerVisual::new);
        visual(AllBlockEntityTypes.MECHANICAL_PRESS, MechanicalPressRenderer::new, PressVisual::new);
        normal(AllBlockEntityTypes.WEIGHTED_EJECTOR, EjectorRenderer::new, EjectorVisual::new);
        visual(AllBlockEntityTypes.ROPE_PULLEY, PulleyRenderer::new, RopePulleyVisual::new);
        visual(AllBlockEntityTypes.MILLSTONE, MillstoneRenderer::new, SingleAxisRotatingVisual.of(AllPartialModels.MILLSTONE_COG));
        visual(AllBlockEntityTypes.ENCASED_FAN, EncasedFanRenderer::new, FanVisual::new);
        render(AllBlockEntityTypes.PECULIAR_BELL, BellRenderer::new);
        render(AllBlockEntityTypes.HAUNTED_BELL, BellRenderer::new);
        normal(AllBlockEntityTypes.SAW, SawRenderer::new, SawVisual::new);
        render(AllBlockEntityTypes.BASIN, BasinRenderer::new);
        normal(AllBlockEntityTypes.FUNNEL, FunnelRenderer::new, FunnelVisual::new);
        normal(AllBlockEntityTypes.ANDESITE_TUNNEL, BeltTunnelRenderer::new, BeltTunnelVisual::new);
        normal(AllBlockEntityTypes.BRASS_TUNNEL, BeltTunnelRenderer::new, BeltTunnelVisual::new);
        render(AllBlockEntityTypes.CHUTE, ChuteRenderer::new);
        render(AllBlockEntityTypes.SMART_CHUTE, SmartChuteRenderer::new);
        visual(AllBlockEntityTypes.MECHANICAL_PISTON, MechanicalPistonRenderer::new, SingleAxisRotatingVisual::shaft);
        render(AllBlockEntityTypes.HARVESTER, HarvesterRenderer::new);
        normal(AllBlockEntityTypes.MECHANICAL_BEARING, BearingRenderer::new, BearingVisual::new);
        normal(AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, PortableStorageInterfaceRenderer::new, PSIVisual::new);
        normal(AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, PortableStorageInterfaceRenderer::new, PSIVisual::new);
        normal(AllBlockEntityTypes.SPEEDOMETER, GaugeRenderer::speed, GaugeVisual.Speed::new);
        normal(AllBlockEntityTypes.STRESSOMETER, GaugeRenderer::stress, GaugeVisual.Stress::new);
        normal(AllBlockEntityTypes.CUCKOO_CLOCK, CuckooClockRenderer::new, OrientedRotatingVisual.backHorizontal(AllPartialModels.SHAFT_HALF));
        normal(AllBlockEntityTypes.MECHANICAL_MIXER, MechanicalMixerRenderer::new, MixerVisual::new);
        normal(AllBlockEntityTypes.HOSE_PULLEY, HosePulleyRenderer::new, HosePulleyVisual::new);
        render(AllBlockEntityTypes.SPOUT, SpoutRenderer::new);
        render(AllBlockEntityTypes.ITEM_DRAIN, ItemDrainRenderer::new);
        render(AllBlockEntityTypes.STEAM_WHISTLE, WhistleRenderer::new);
        normal(AllBlockEntityTypes.BACKTANK, BacktankRenderer::new, SingleAxisRotatingVisual::backtank);
        normal(AllBlockEntityTypes.DEPLOYER, DeployerRenderer::new, DeployerVisual::new);
        visual(AllBlockEntityTypes.TURNTABLE, KineticBlockEntityRenderer::new, SingleAxisRotatingVisual.of(AllPartialModels.TURNTABLE));
        visual(AllBlockEntityTypes.DRILL, DrillRenderer::new, OrientedRotatingVisual.of(AllPartialModels.DRILL_HEAD));
        visual(AllBlockEntityTypes.GANTRY_SHAFT, KineticBlockEntityRenderer::new, OrientedRotatingVisual::gantryShaft);
        normal(AllBlockEntityTypes.GANTRY_PINION, GantryCarriageRenderer::new, GantryCarriageVisual::new);
        normal(AllBlockEntityTypes.CLOCKWORK_BEARING, BearingRenderer::new, BearingVisual::new);
        visual(AllBlockEntityTypes.CRUSHING_WHEEL, KineticBlockEntityRenderer::new, SingleAxisRotatingVisual.of(AllPartialModels.CRUSHING_WHEEL));
        normal(AllBlockEntityTypes.FLAP_DISPLAY, FlapDisplayRenderer::new, SingleAxisRotatingVisual.of(AllPartialModels.SHAFTLESS_COGWHEEL));
        render(AllBlockEntityTypes.DISPLAY_LINK, LinkBulbRenderer::new);
        render(AllBlockEntityTypes.NIXIE_TUBE, NixieTubeRenderer::new);
        normal(AllBlockEntityTypes.FLUID_VALVE, FluidValveRenderer::new, FluidValveVisual::new);
        render(AllBlockEntityTypes.SMART_FLUID_PIPE, SmartBlockEntityRenderer::new);
        visual(AllBlockEntityTypes.ANALOG_LEVER, AnalogLeverRenderer::new, AnalogLeverVisual::new);
        render(AllBlockEntityTypes.REDSTONE_LINK, SmartBlockEntityRenderer::new);
        visual(AllBlockEntityTypes.PULSE_REPEATER, BrassDiodeRenderer::new, BrassDiodeVisual::new);
        visual(AllBlockEntityTypes.PULSE_EXTENDER, BrassDiodeRenderer::new, BrassDiodeVisual::new);
        visual(AllBlockEntityTypes.PULSE_TIMER, BrassDiodeRenderer::new, BrassDiodeVisual::new);
        render(AllBlockEntityTypes.SMART_OBSERVER, SmartBlockEntityRenderer::new);
        render(AllBlockEntityTypes.THRESHOLD_SWITCH, SmartBlockEntityRenderer::new);
        visual(AllBlockEntityTypes.STICKER, StickerRenderer::new, StickerVisual::new);
        render(AllBlockEntityTypes.CONTRAPTION_CONTROLS, ContraptionControlsRenderer::new);
        visual(AllBlockEntityTypes.ELEVATOR_PULLEY, ElevatorPulleyRenderer::new, ElevatorPulleyVisual::new);
        render(AllBlockEntityTypes.SLIDING_DOOR, SlidingDoorRenderer::new);
        render(AllBlockEntityTypes.DESK_BELL, DeskBellRenderer::new);
        normal(
            AllBlockEntityTypes.MECHANICAL_CRAFTER,
            MechanicalCrafterRenderer::new,
            SingleAxisRotatingVisual.of(AllPartialModels.SHAFTLESS_COGWHEEL)
        );
        render(AllBlockEntityTypes.CREATIVE_CRATE, SmartBlockEntityRenderer::new);
        normal(AllBlockEntityTypes.MECHANICAL_ARM, ArmRenderer::new, ArmVisual::new);
        normal(AllBlockEntityTypes.TRACK, TrackRenderer::new, TrackVisual::new);
        visual(AllBlockEntityTypes.BOGEY, BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
        normal(AllBlockEntityTypes.TRACK_SIGNAL, SignalRenderer::new, SignalVisual::new);
        render(AllBlockEntityTypes.TRACK_STATION, StationRenderer::new);
        normal(AllBlockEntityTypes.TRACK_OBSERVER, TrackObserverRenderer::new, TrackObserverVisual::new);
        render(AllBlockEntityTypes.MECHANICAL_ROLLER, RollerRenderer::new);
        render(AllBlockEntityTypes.LECTERN_CONTROLLER, LecternControllerRenderer::new);
        normal(AllBlockEntityTypes.PACKAGER, PackagerRenderer::new, PackagerVisual::new);
        render(AllBlockEntityTypes.PACKAGER_LINK, LinkBulbRenderer::new);
        normal(AllBlockEntityTypes.REPACKAGER, PackagerRenderer::new, PackagerVisual::new);
        render(AllBlockEntityTypes.TABLE_CLOTH, TableClothRenderer::new);
        render(AllBlockEntityTypes.PACKAGE_POSTBOX, PostboxRenderer::new);
        normal(AllBlockEntityTypes.PACKAGE_FROGPORT, FrogportRenderer::new, FrogportVisual::new);
        render(AllBlockEntityTypes.FACTORY_PANEL, FactoryPanelRenderer::new);
        visual(AllBlockEntityTypes.FLYWHEEL, FlywheelRenderer::new, FlywheelVisual::new);
        render(AllBlockEntityTypes.ITEM_HATCH, SmartBlockEntityRenderer::new);
        render(AllBlockEntityTypes.PLACARD, PlacardRenderer::new);
        visual(AllBlockEntityTypes.TOOLBOX, ToolboxRenderer::new, ToolBoxVisual::new);
        normal(AllBlockEntityTypes.SCHEMATICANNON, SchematicannonRenderer::new, SchematicannonVisual::new);
    }
}
