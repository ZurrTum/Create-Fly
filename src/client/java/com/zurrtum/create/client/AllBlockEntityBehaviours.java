package com.zurrtum.create.client;

import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorClientBehaviour;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.content.logistics.tableCloth.TableClothFilteringBehaviour;
import com.zurrtum.create.client.content.redstone.link.LinkBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.WhistleAnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.*;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.*;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.ClockworkBearingBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.zurrtum.create.content.contraptions.chassis.ChassisBlockEntity;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockEntity;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlockEntity;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlockEntity;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.zurrtum.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.zurrtum.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainDrive.ChainGearshiftBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
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
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.logistics.itemHatch.ItemHatchBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.zurrtum.create.content.redstone.diodes.PulseExtenderBlockEntity;
import com.zurrtum.create.content.redstone.diodes.PulseRepeaterBlockEntity;
import com.zurrtum.create.content.redstone.diodes.PulseTimerBlockEntity;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.zurrtum.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class AllBlockEntityBehaviours {
    public static Map<Class<? extends SmartBlockEntity>, Function<? extends SmartBlockEntity, BlockEntityBehaviour<?>>[]> ALL = new Reference2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends SmartBlockEntity> void addBehaviours(T blockEntity, ArrayList<BlockEntityBehaviour<?>> behaviours) {
        Function<? extends SmartBlockEntity, BlockEntityBehaviour<?>>[] factorys = ALL.get(blockEntity.getClass());
        if (factorys != null) {
            for (Function<T, BlockEntityBehaviour<?>> factory : (Function<T, BlockEntityBehaviour<?>>[]) factorys) {
                behaviours.add(factory.apply(blockEntity));
            }
        }
    }

    @SafeVarargs
    public static <T extends SmartBlockEntity> void add(Class<T> type, Function<T, BlockEntityBehaviour<?>>... factory) {
        ALL.put(type, factory);
    }

    public static void register() {
        add(CreativeMotorBlockEntity.class, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new, KineticScrollValueBehaviour::motor);
        add(WaterWheelBlockEntity.class, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(SpeedControllerBlockEntity.class, KineticAudioBehaviour::new, KineticScrollValueBehaviour::controller);
        add(PoweredShaftBlockEntity.class, PoweredShaftTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(SteamEngineBlockEntity.class, SteamEngineTooltipBehaviour::new, RotationDirectionScrollBehaviour::engine);
        add(ChainConveyorBlockEntity.class, KineticTooltipBehaviour::new, KineticAudioBehaviour::new, ChainConveyorClientBehaviour::new);
        add(HandCrankBlockEntity.class, GeneratingKineticTooltipBehaviour::new, HandCrankAudioBehaviour::new);
        add(ValveHandleBlockEntity.class, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new, ValveHandleScrollValueBehaviour::new);
        add(ArmBlockEntity.class, KineticAudioBehaviour::new);
        add(BacktankBlockEntity.class, KineticAudioBehaviour::new);
        add(BeltBlockEntity.class, KineticAudioBehaviour::new);
        add(BracketedKineticBlockEntity.class, KineticAudioBehaviour::new);
        add(ChainGearshiftBlockEntity.class, KineticAudioBehaviour::new);
        add(ClutchBlockEntity.class, KineticAudioBehaviour::new);
        add(GantryShaftBlockEntity.class, KineticAudioBehaviour::new);
        add(GantryCarriageBlockEntity.class, GantryCarriageTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(GearboxBlockEntity.class, KineticAudioBehaviour::new);
        add(GearshiftBlockEntity.class, KineticAudioBehaviour::new);
        add(LargeWaterWheelBlockEntity.class, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(SequencedGearshiftBlockEntity.class, KineticAudioBehaviour::new);
        add(SimpleKineticBlockEntity.class, KineticAudioBehaviour::new);
        add(ChassisBlockEntity.class, ChassisScrollValueBehaviour::new);
        add(
            WindmillBearingBlockEntity.class,
            MechanicalBearingTooltipBehaviour::new,
            KineticAudioBehaviour::new,
            RotationDirectionScrollBehaviour::windmill
        );
        add(FluidTankBlockEntity.class, FluidTankTooltipBehaviour::new);
        add(EjectorBlockEntity.class, KineticAudioBehaviour::new, KineticScrollValueBehaviour::ejector);
        add(PulleyBlockEntity.class, LinearActuatorTooltipBehaviour::new, KineticAudioBehaviour::new, MovementModeScrollBehaviour::pulley);
        add(MillstoneBlockEntity.class, KineticTooltipBehaviour::new, KineticAudioBehaviour::new, MillstoneAudioBehaviour::new);
        add(EncasedFanBlockEntity.class, KineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(SawBlockEntity.class, FilteringBehaviour::saw, SawAudioBehaviour::new);
        add(BasinBlockEntity.class, BasinTooltipBehaviour::new, FilteringBehaviour::basin);
        add(FunnelBlockEntity.class, FilteringBehaviour::funnel);
        add(BrassTunnelBlockEntity.class, BrassTunnelTooltipBehaviour::new, SidedFilteringBehaviour::tunnel, TunnelSelectionModeScrollBehaviour::new);
        add(ChuteBlockEntity.class, ChuteTooltipBehaviour::new);
        add(SmartChuteBlockEntity.class, ChuteTooltipBehaviour::new, FilteringBehaviour::chute);
        add(CartAssemblerBlockEntity.class, CartAssemblerTooltipBehaviour::new, CartMovementScrollBehaviour::new);
        add(MechanicalPistonBlockEntity.class, LinearActuatorTooltipBehaviour::new, KineticAudioBehaviour::new, MovementModeScrollBehaviour::piston);
        add(MechanicalBearingBlockEntity.class, MechanicalBearingTooltipBehaviour::new, KineticAudioBehaviour::new, RotationModeScrollBehaviour::new);
        add(SpeedGaugeBlockEntity.class, SpeedGaugeTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(StressGaugeBlockEntity.class, StressGaugeTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(CuckooClockBlockEntity.class, CuckooClockAnimationBehaviour::new, KineticAudioBehaviour::new);
        add(MechanicalMixerBlockEntity.class, KineticTooltipBehaviour::new, MechanicalMixerAudioBehaviour::new);
        add(HosePulleyBlockEntity.class, HosePulleyTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(SpoutBlockEntity.class, SpoutTooltipBehaviour::new);
        add(ItemDrainBlockEntity.class, ItemDrainTooltipBehaviour::new);
        add(WhistleBlockEntity.class, WhistleTooltipBehaviour::new, WhistleAnimationBehaviour::new, WhistleAudioBehaviour::new);
        add(DeployerBlockEntity.class, DeployerTooltipBehaviour::new, FilteringBehaviour::deployer, KineticAudioBehaviour::new);
        add(TurntableBlockEntity.class, KineticAudioBehaviour::new);
        add(ClockworkBearingBlockEntity.class, ClockHandsScrollBehaviour::new);
        add(CrushingWheelControllerBlockEntity.class, CrushingWheelControllerAudioBehaviour::new);
        add(FlapDisplayBlockEntity.class, KineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(FluidValveBlockEntity.class, KineticAudioBehaviour::new);
        add(SmartFluidPipeBlockEntity.class, FilteringBehaviour::pipe);
        add(AnalogLeverBlockEntity.class, AnalogLeverTooltipBehaviour::new);
        add(RedstoneLinkBlockEntity.class, LinkBehaviour::new);
        add(PulseRepeaterBlockEntity.class, BrassDiodeScrollValueBehaviour::new);
        add(PulseExtenderBlockEntity.class, BrassDiodeScrollValueBehaviour::new);
        add(PulseTimerBlockEntity.class, BrassDiodeScrollValueBehaviour::new);
        add(SmartObserverBlockEntity.class, FilteringBehaviour::observer);
        add(ThresholdSwitchBlockEntity.class, FilteringBehaviour::threshold);
        add(ContraptionControlsBlockEntity.class, FilteringBehaviour::controls);
        add(CreativeCrateBlockEntity.class, FilteringBehaviour::crate);
        add(ArmBlockEntity.class, ArmTooltipBehaviour::new, ArmSelectionModeScrollBehaviour::new, KineticAudioBehaviour::new);
        add(TrackObserverBlockEntity.class, FilteringBehaviour::observer);
        add(RollerBlockEntity.class, FilteringBehaviour::roller, RollingModeScrollBehaviour::new);
        add(StockTickerBlockEntity.class, StockTickerTooltipBehaviour::new);
        add(TableClothBlockEntity.class, TableClothFilteringBehaviour::new);
        add(FrogportBlockEntity.class, FrogportClientAudioBehaviour::new, FrogportTooltipBehaviour::new);
        add(FactoryPanelBlockEntity.class, FactoryPanelBehaviour.allSlot());
        add(ItemHatchBlockEntity.class, FilteringBehaviour::hatch);
    }
}
