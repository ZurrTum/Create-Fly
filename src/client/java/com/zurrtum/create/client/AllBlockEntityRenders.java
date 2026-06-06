package com.zurrtum.create.client;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ContraptionControlsRenderer;
import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ContraptionControlsVisual;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterRenderer;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterVisual;
import com.zurrtum.create.client.content.contraptions.actors.psi.PSIVisual;
import com.zurrtum.create.client.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import com.zurrtum.create.client.content.contraptions.actors.roller.RollerRenderer;
import com.zurrtum.create.client.content.contraptions.actors.roller.RollerVisual;
import com.zurrtum.create.client.content.contraptions.bearing.BearingRenderer;
import com.zurrtum.create.client.content.contraptions.bearing.BearingVisual;
import com.zurrtum.create.client.content.contraptions.chassis.StickerRenderer;
import com.zurrtum.create.client.content.contraptions.chassis.StickerVisual;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorPulleyRenderer;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorPulleyVisual;
import com.zurrtum.create.client.content.contraptions.gantry.GantryCarriageRenderer;
import com.zurrtum.create.client.content.contraptions.gantry.GantryCarriageVisual;
import com.zurrtum.create.client.content.contraptions.pulley.PulleyRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.RopePulleyVisual;
import com.zurrtum.create.client.content.decoration.placard.PlacardRenderer;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorVisual;
import com.zurrtum.create.client.content.decoration.steamWhistle.WhistleRenderer;
import com.zurrtum.create.client.content.decoration.steamWhistle.WhistleVisual;
import com.zurrtum.create.client.content.equipment.armor.BacktankRenderer;
import com.zurrtum.create.client.content.equipment.armor.BacktankVisual;
import com.zurrtum.create.client.content.equipment.bell.BellRenderer;
import com.zurrtum.create.client.content.equipment.bell.BellVisual;
import com.zurrtum.create.client.content.equipment.toolbox.ToolBoxVisual;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxRenderer;
import com.zurrtum.create.client.content.fluids.PumpRenderer;
import com.zurrtum.create.client.content.fluids.PumpVisual;
import com.zurrtum.create.client.content.fluids.drain.ItemDrainRenderer;
import com.zurrtum.create.client.content.fluids.hosePulley.HosePulleyRenderer;
import com.zurrtum.create.client.content.fluids.hosePulley.HosePulleyVisual;
import com.zurrtum.create.client.content.fluids.pipes.GlassPipeVisual;
import com.zurrtum.create.client.content.fluids.pipes.TransparentStraightPipeRenderer;
import com.zurrtum.create.client.content.fluids.pipes.valve.FluidValveRenderer;
import com.zurrtum.create.client.content.fluids.pipes.valve.FluidValveVisual;
import com.zurrtum.create.client.content.fluids.spout.SpoutRenderer;
import com.zurrtum.create.client.content.fluids.spout.SpoutVisual;
import com.zurrtum.create.client.content.fluids.tank.FluidTankRenderer;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.content.kinetics.belt.BeltRenderer;
import com.zurrtum.create.client.content.kinetics.belt.BeltVisual;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorVisual;
import com.zurrtum.create.client.content.kinetics.clock.CuckooClockRenderer;
import com.zurrtum.create.client.content.kinetics.clock.CuckooClockVisual;
import com.zurrtum.create.client.content.kinetics.crafter.MechanicalCrafterRenderer;
import com.zurrtum.create.client.content.kinetics.crafter.MechanicalCrafterVisual;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankRenderer;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankVisual;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleRenderer;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleVisual;
import com.zurrtum.create.client.content.kinetics.crusher.CrushingWheelVisual;
import com.zurrtum.create.client.content.kinetics.deployer.DeployerRenderer;
import com.zurrtum.create.client.content.kinetics.deployer.DeployerVisual;
import com.zurrtum.create.client.content.kinetics.drill.DrillRenderer;
import com.zurrtum.create.client.content.kinetics.drill.DrillVisual;
import com.zurrtum.create.client.content.kinetics.fan.EncasedFanRenderer;
import com.zurrtum.create.client.content.kinetics.fan.FanVisual;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelRenderer;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelVisual;
import com.zurrtum.create.client.content.kinetics.gantry.GantryShaftRenderer;
import com.zurrtum.create.client.content.kinetics.gauge.GaugeRenderer;
import com.zurrtum.create.client.content.kinetics.gauge.GaugeVisual.Speed;
import com.zurrtum.create.client.content.kinetics.gauge.GaugeVisual.Stress;
import com.zurrtum.create.client.content.kinetics.gearbox.GearboxRenderer;
import com.zurrtum.create.client.content.kinetics.gearbox.GearboxVisual;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmRenderer;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmVisual;
import com.zurrtum.create.client.content.kinetics.millstone.MillstoneRenderer;
import com.zurrtum.create.client.content.kinetics.millstone.MillstoneVisual;
import com.zurrtum.create.client.content.kinetics.mixer.MechanicalMixerRenderer;
import com.zurrtum.create.client.content.kinetics.mixer.MixerVisual;
import com.zurrtum.create.client.content.kinetics.motor.CreativeMotorRenderer;
import com.zurrtum.create.client.content.kinetics.press.MechanicalPressRenderer;
import com.zurrtum.create.client.content.kinetics.press.PressVisual;
import com.zurrtum.create.client.content.kinetics.saw.SawRenderer;
import com.zurrtum.create.client.content.kinetics.saw.SawVisual;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedLargeCogRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedSmallCogRenderer;
import com.zurrtum.create.client.content.kinetics.steamEngine.PoweredShaftRenderer;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineRenderer;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineVisual;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftVisual;
import com.zurrtum.create.client.content.kinetics.turntable.TurntableVisual;
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
import com.zurrtum.create.client.content.redstone.deskBell.DeskBellVisual;
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
import com.zurrtum.create.client.content.trains.display.FlapDisplayVisual;
import com.zurrtum.create.client.content.trains.observer.TrackObserverRenderer;
import com.zurrtum.create.client.content.trains.observer.TrackObserverVisual;
import com.zurrtum.create.client.content.trains.signal.SignalRenderer;
import com.zurrtum.create.client.content.trains.signal.SignalVisual;
import com.zurrtum.create.client.content.trains.station.StationRenderer;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.content.trains.track.TrackVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer.Factory;
import com.zurrtum.create.client.foundation.blockEntity.renderer.FilterBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.LinkBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Predicate;

public class AllBlockEntityRenders {
    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void visual(
        BlockEntityType<P> type,
        BlockEntityRendererProvider<T, S> rendererFactory,
        Factory<P> visualizerFactory
    ) {
        visual(type, rendererFactory, visualizerFactory, blockEntity -> true);
    }

    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void normal(
        BlockEntityType<P> type,
        BlockEntityRendererProvider<T, S> rendererFactory,
        Factory<P> visualizerFactory
    ) {
        visual(type, rendererFactory, visualizerFactory, blockEntity -> false);
    }

    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void visual(
        BlockEntityType<P> type,
        BlockEntityRendererProvider<T, S> rendererFactory,
        Factory<P> visualizerFactory,
        Predicate<P> skipVanillaRender
    ) {
        BlockEntityRenderers.register(type, rendererFactory);
        SimpleBlockEntityVisualizer.builder(type).factory(visualizerFactory).skipVanillaRender(skipVanillaRender)
            .apply();
    }

    public static <T extends BlockEntity, P extends T, S extends BlockEntityRenderState> void render(
        BlockEntityType<P> type,
        BlockEntityRendererProvider<T, S> rendererFactory
    ) {
        BlockEntityRenderers.register(type, rendererFactory);
    }

    public static void register() {
        visual(
            AllBlockEntityTypes.BRACKETED_KINETIC,
            BracketedKineticBlockEntityRenderer::new,
            BracketedKineticBlockEntityVisual::create
        );
        visual(
            AllBlockEntityTypes.MOTOR,
            CreativeMotorRenderer::new,
            OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF)
        );
        visual(AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER, ShaftRenderer::new, SingleAxisRotatingVisual::shaft);
        visual(AllBlockEntityTypes.WATER_WHEEL, WaterWheelRenderer::standard, WaterWheelVisual::standard);
        visual(AllBlockEntityTypes.LARGE_WATER_WHEEL, WaterWheelRenderer::large, WaterWheelVisual::large);
        render(AllBlockEntityTypes.DEPOT, DepotRenderer::new);
        visual(AllBlockEntityTypes.BELT, BeltRenderer::new, BeltVisual::new, BeltVisual::shouldSkipVanillaRender);
        visual(AllBlockEntityTypes.GEARBOX, GearboxRenderer::new, GearboxVisual::new);
        visual(AllBlockEntityTypes.CLUTCH, SplitShaftRenderer::new, SplitShaftVisual::new);
        visual(AllBlockEntityTypes.GEARSHIFT, SplitShaftRenderer::new, SplitShaftVisual::new);
        visual(AllBlockEntityTypes.SEQUENCED_GEARSHIFT, SplitShaftRenderer::new, SplitShaftVisual::new);
        visual(AllBlockEntityTypes.ENCASED_SHAFT, ShaftRenderer::new, SingleAxisRotatingVisual::shaft);
        visual(AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT, ShaftRenderer::new, SingleAxisRotatingVisual::shaft);
        normal(AllBlockEntityTypes.CHAIN_CONVEYOR, ChainConveyorRenderer::new, ChainConveyorVisual::new);
        visual(AllBlockEntityTypes.ENCASED_COGWHEEL, EncasedSmallCogRenderer::new, EncasedCogVisual::small);
        visual(AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL, EncasedLargeCogRenderer::new, EncasedCogVisual::large);
        visual(AllBlockEntityTypes.HAND_CRANK, HandCrankRenderer::new, HandCrankVisual::new);
        visual(AllBlockEntityTypes.VALVE_HANDLE, ValveHandleRenderer::new, ValveHandleVisual::new);
        visual(AllBlockEntityTypes.WINDMILL_BEARING, BearingRenderer::new, BearingVisual::new);
        visual(AllBlockEntityTypes.MECHANICAL_PUMP, PumpRenderer::new, PumpVisual::new);
        render(AllBlockEntityTypes.FLUID_TANK, FluidTankRenderer::new);
        render(AllBlockEntityTypes.CREATIVE_FLUID_TANK, FluidTankRenderer::new);
        visual(AllBlockEntityTypes.GLASS_FLUID_PIPE, TransparentStraightPipeRenderer::new, GlassPipeVisual::new);
        visual(AllBlockEntityTypes.STEAM_ENGINE, SteamEngineRenderer::new, SteamEngineVisual::new);
        visual(
            AllBlockEntityTypes.POWERED_SHAFT,
            PoweredShaftRenderer::new,
            SingleAxisRotatingVisual.of(AllPartialModels.POWERED_SHAFT)
        );
        visual(AllBlockEntityTypes.HEATER, BlazeBurnerRenderer::new, BlazeBurnerVisual::new);
        visual(AllBlockEntityTypes.MECHANICAL_PRESS, MechanicalPressRenderer::new, PressVisual::new);
        normal(AllBlockEntityTypes.WEIGHTED_EJECTOR, EjectorRenderer::new, EjectorVisual::new);
        visual(AllBlockEntityTypes.ROPE_PULLEY, PulleyRenderer::new, RopePulleyVisual::new);
        visual(AllBlockEntityTypes.MILLSTONE, MillstoneRenderer::new, MillstoneVisual::new);
        visual(AllBlockEntityTypes.ENCASED_FAN, EncasedFanRenderer::new, FanVisual::new);
        visual(
            AllBlockEntityTypes.PECULIAR_BELL,
            BellRenderer.of(AllPartialModels.PECULIAR_BELL),
            BellVisual.of(AllPartialModels.PECULIAR_BELL)
        );
        visual(
            AllBlockEntityTypes.HAUNTED_BELL,
            BellRenderer.of(AllPartialModels.HAUNTED_BELL),
            BellVisual.of(AllPartialModels.HAUNTED_BELL)
        );
        normal(AllBlockEntityTypes.SAW, SawRenderer::new, SawVisual::new);
        render(AllBlockEntityTypes.BASIN, BasinRenderer::new);
        normal(AllBlockEntityTypes.FUNNEL, FunnelRenderer::new, FunnelVisual::new);
        normal(AllBlockEntityTypes.ANDESITE_TUNNEL, BeltTunnelRenderer::new, BeltTunnelVisual::new);
        normal(AllBlockEntityTypes.BRASS_TUNNEL, BeltTunnelRenderer::new, BeltTunnelVisual::new);
        render(AllBlockEntityTypes.CHUTE, ChuteRenderer::new);
        render(AllBlockEntityTypes.SMART_CHUTE, SmartChuteRenderer::new);
        visual(AllBlockEntityTypes.MECHANICAL_PISTON, ShaftRenderer::new, SingleAxisRotatingVisual::shaft);
        visual(AllBlockEntityTypes.HARVESTER, HarvesterRenderer::new, HarvesterVisual::new);
        visual(AllBlockEntityTypes.MECHANICAL_BEARING, BearingRenderer::new, BearingVisual::new);
        visual(AllBlockEntityTypes.PORTABLE_FLUID_INTERFACE, PortableStorageInterfaceRenderer::new, PSIVisual::new);
        visual(AllBlockEntityTypes.PORTABLE_STORAGE_INTERFACE, PortableStorageInterfaceRenderer::new, PSIVisual::new);
        visual(AllBlockEntityTypes.SPEEDOMETER, GaugeRenderer::speed, Speed::new);
        visual(AllBlockEntityTypes.STRESSOMETER, GaugeRenderer::stress, Stress::new);
        visual(AllBlockEntityTypes.CUCKOO_CLOCK, CuckooClockRenderer::new, CuckooClockVisual::new);
        visual(AllBlockEntityTypes.MECHANICAL_MIXER, MechanicalMixerRenderer::new, MixerVisual::new);
        visual(AllBlockEntityTypes.HOSE_PULLEY, HosePulleyRenderer::new, HosePulleyVisual::new);
        normal(AllBlockEntityTypes.SPOUT, SpoutRenderer::new, SpoutVisual::new);
        render(AllBlockEntityTypes.ITEM_DRAIN, ItemDrainRenderer::new);
        visual(AllBlockEntityTypes.STEAM_WHISTLE, WhistleRenderer::new, WhistleVisual::new);
        visual(AllBlockEntityTypes.BACKTANK, BacktankRenderer::new, BacktankVisual::new);
        normal(AllBlockEntityTypes.DEPLOYER, DeployerRenderer::new, DeployerVisual::new);
        visual(
            AllBlockEntityTypes.TURNTABLE,
            SingleAxisRotatingRenderer.of(AllPartialModels.TURNTABLE),
            TurntableVisual::new
        );
        visual(AllBlockEntityTypes.DRILL, DrillRenderer::new, DrillVisual::new);
        visual(AllBlockEntityTypes.GANTRY_SHAFT, GantryShaftRenderer::new, OrientedRotatingVisual::gantryShaft);
        visual(AllBlockEntityTypes.GANTRY_PINION, GantryCarriageRenderer::new, GantryCarriageVisual::new);
        visual(AllBlockEntityTypes.CLOCKWORK_BEARING, BearingRenderer::new, BearingVisual::new);
        visual(
            AllBlockEntityTypes.CRUSHING_WHEEL,
            SingleAxisRotatingRenderer.of(AllPartialModels.CRUSHING_WHEEL),
            CrushingWheelVisual::new
        );
        normal(AllBlockEntityTypes.FLAP_DISPLAY, FlapDisplayRenderer::new, FlapDisplayVisual::new);
        render(AllBlockEntityTypes.DISPLAY_LINK, LinkBulbRenderer::new);
        render(AllBlockEntityTypes.NIXIE_TUBE, NixieTubeRenderer::new);
        visual(AllBlockEntityTypes.FLUID_VALVE, FluidValveRenderer::new, FluidValveVisual::new);
        render(AllBlockEntityTypes.SMART_FLUID_PIPE, FilterBlockEntityRenderer::new);
        visual(AllBlockEntityTypes.ANALOG_LEVER, AnalogLeverRenderer::new, AnalogLeverVisual::new);
        render(AllBlockEntityTypes.REDSTONE_LINK, LinkBlockEntityRenderer::new);
        visual(AllBlockEntityTypes.PULSE_REPEATER, BrassDiodeRenderer::new, BrassDiodeVisual::new);
        visual(AllBlockEntityTypes.PULSE_EXTENDER, BrassDiodeRenderer::new, BrassDiodeVisual::new);
        visual(AllBlockEntityTypes.PULSE_TIMER, BrassDiodeRenderer::new, BrassDiodeVisual::new);
        render(AllBlockEntityTypes.SMART_OBSERVER, FilterBlockEntityRenderer::new);
        render(AllBlockEntityTypes.THRESHOLD_SWITCH, FilterBlockEntityRenderer::new);
        visual(AllBlockEntityTypes.STICKER, StickerRenderer::new, StickerVisual::new);
        normal(
            AllBlockEntityTypes.CONTRAPTION_CONTROLS,
            ContraptionControlsRenderer::new,
            ContraptionControlsVisual::new
        );
        visual(AllBlockEntityTypes.ELEVATOR_PULLEY, ElevatorPulleyRenderer::new, ElevatorPulleyVisual::new);
        visual(AllBlockEntityTypes.SLIDING_DOOR, SlidingDoorRenderer::new, SlidingDoorVisual::create);
        visual(AllBlockEntityTypes.DESK_BELL, DeskBellRenderer::new, DeskBellVisual::new);
        normal(AllBlockEntityTypes.MECHANICAL_CRAFTER, MechanicalCrafterRenderer::new, MechanicalCrafterVisual::new);
        render(AllBlockEntityTypes.CREATIVE_CRATE, FilterBlockEntityRenderer::new);
        normal(AllBlockEntityTypes.MECHANICAL_ARM, ArmRenderer::new, ArmVisual::new);
        visual(AllBlockEntityTypes.TRACK, TrackRenderer::new, TrackVisual::new);
        visual(AllBlockEntityTypes.BOGEY, BogeyBlockEntityRenderer::new, BogeyBlockEntityVisual::new);
        visual(AllBlockEntityTypes.TRACK_SIGNAL, SignalRenderer::new, SignalVisual::new);
        render(AllBlockEntityTypes.TRACK_STATION, StationRenderer::new);
        normal(AllBlockEntityTypes.TRACK_OBSERVER, TrackObserverRenderer::new, TrackObserverVisual::new);
        normal(AllBlockEntityTypes.MECHANICAL_ROLLER, RollerRenderer::new, RollerVisual::new);
        render(AllBlockEntityTypes.LECTERN_CONTROLLER, LecternControllerRenderer::new);
        normal(AllBlockEntityTypes.PACKAGER, PackagerRenderer::new, PackagerVisual::new);
        render(AllBlockEntityTypes.PACKAGER_LINK, LinkBulbRenderer::new);
        normal(AllBlockEntityTypes.REPACKAGER, PackagerRenderer::new, PackagerVisual::new);
        render(AllBlockEntityTypes.TABLE_CLOTH, TableClothRenderer::new);
        render(AllBlockEntityTypes.PACKAGE_POSTBOX, PostboxRenderer::new);
        normal(AllBlockEntityTypes.PACKAGE_FROGPORT, FrogportRenderer::new, FrogportVisual::new);
        render(AllBlockEntityTypes.FACTORY_PANEL, FactoryPanelRenderer::new);
        visual(AllBlockEntityTypes.FLYWHEEL, FlywheelRenderer::new, FlywheelVisual::new);
        render(AllBlockEntityTypes.ITEM_HATCH, FilterBlockEntityRenderer::new);
        render(AllBlockEntityTypes.PLACARD, PlacardRenderer::new);
        visual(AllBlockEntityTypes.TOOLBOX, ToolboxRenderer::new, ToolBoxVisual::new);
        normal(AllBlockEntityTypes.SCHEMATICANNON, SchematicannonRenderer::new, SchematicannonVisual::new);
    }
}
