package com.zurrtum.create.client;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
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
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import java.util.function.Function;

public class AllBlockEntityBehaviours {
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends SmartBlockEntity> void add(BlockEntityType<T> type, Function<T, BlockEntityBehaviour<?>>... factories) {
        for (Function<T, BlockEntityBehaviour<?>> factory : factories) {
            BlockEntityBehaviour.CLIENT_REGISTRY.add(type, (Function<SmartBlockEntity, BlockEntityBehaviour<?>>) factory);
        }
    }

    public static void register() {
        add(AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.ANALOG_LEVER, AnalogLeverTooltipBehaviour::new);
        add(AllBlockEntityTypes.BACKTANK, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.BASIN, BasinTooltipBehaviour::new, FilteringBehaviour::basin);
        add(AllBlockEntityTypes.BELT, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.BRACKETED_KINETIC, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(
            AllBlockEntityTypes.BRASS_TUNNEL,
            BrassTunnelTooltipBehaviour::new,
            SidedFilteringBehaviour::tunnel,
            TunnelSelectionModeScrollBehaviour::new
        );
        add(AllBlockEntityTypes.CART_ASSEMBLER, CartAssemblerTooltipBehaviour::new, CartMovementScrollBehaviour::new);
        add(AllBlockEntityTypes.CHAIN_CONVEYOR, ChainConveyorClientBehaviour::new, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.CHASSIS, ChassisScrollValueBehaviour::new);
        add(AllBlockEntityTypes.CHUTE, ChuteTooltipBehaviour::new);
        add(AllBlockEntityTypes.CLUTCH, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.CLOCKWORK_BEARING, ClockHandsScrollBehaviour::new, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.CONTRAPTION_CONTROLS, FilteringBehaviour::controls);
        add(AllBlockEntityTypes.CREATIVE_CRATE, FilteringBehaviour::crate);
        add(AllBlockEntityTypes.CRUSHING_WHEEL, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, CrushingWheelControllerAudioBehaviour::new);
        add(AllBlockEntityTypes.CUCKOO_CLOCK, CuckooClockAnimationBehaviour::new, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.DEPLOYER, DeployerTooltipBehaviour::new, FilteringBehaviour::deployer, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.DRILL, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.ELEVATOR_PULLEY, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.ENCASED_COGWHEEL, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.ENCASED_FAN, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.FACTORY_PANEL, FactoryPanelBehaviour.allSlot());
        add(AllBlockEntityTypes.FLAP_DISPLAY, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.FLUID_TANK, FluidTankTooltipBehaviour::new);
        add(AllBlockEntityTypes.FLUID_VALVE, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.FUNNEL, FilteringBehaviour::funnel);
        add(AllBlockEntityTypes.GANTRY_PINION, GantryCarriageTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.GANTRY_SHAFT, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.GEARBOX, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.GEARSHIFT, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.HAND_CRANK, GeneratingKineticTooltipBehaviour::new, HandCrankAudioBehaviour::new);
        add(AllBlockEntityTypes.HOSE_PULLEY, HosePulleyTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.ITEM_DRAIN, ItemDrainTooltipBehaviour::new);
        add(AllBlockEntityTypes.ITEM_HATCH, FilteringBehaviour::hatch);
        add(AllBlockEntityTypes.LARGE_WATER_WHEEL, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.MECHANICAL_ARM, ArmSelectionModeScrollBehaviour::new, ArmTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(
            AllBlockEntityTypes.MECHANICAL_BEARING,
            KineticAudioBehaviour::new,
            MechanicalBearingTooltipBehaviour::new,
            RotationModeScrollBehaviour::new
        );
        add(AllBlockEntityTypes.MECHANICAL_CRAFTER, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.MECHANICAL_MIXER, KineticAudioBehaviour::new, MechanicalMixerAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(
            AllBlockEntityTypes.MECHANICAL_PISTON,
            KineticAudioBehaviour::new,
            LinearActuatorTooltipBehaviour::new,
            MovementModeScrollBehaviour::piston
        );
        add(AllBlockEntityTypes.MECHANICAL_PRESS, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.MECHANICAL_PUMP, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.MECHANICAL_ROLLER, FilteringBehaviour::roller, RollingModeScrollBehaviour::new);
        add(AllBlockEntityTypes.MILLSTONE, KineticAudioBehaviour::new, KineticTooltipBehaviour::new, MillstoneAudioBehaviour::new);
        add(AllBlockEntityTypes.MOTOR, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new, KineticScrollValueBehaviour::motor);
        add(AllBlockEntityTypes.PACKAGE_FROGPORT, FrogportClientAudioBehaviour::new, FrogportTooltipBehaviour::new);
        add(AllBlockEntityTypes.POWERED_SHAFT, KineticAudioBehaviour::new, PoweredShaftTooltipBehaviour::new);
        add(AllBlockEntityTypes.PULSE_EXTENDER, BrassDiodeScrollValueBehaviour::new);
        add(AllBlockEntityTypes.PULSE_REPEATER, BrassDiodeScrollValueBehaviour::new);
        add(AllBlockEntityTypes.PULSE_TIMER, BrassDiodeScrollValueBehaviour::new);
        add(AllBlockEntityTypes.REDSTONE_LINK, LinkBehaviour::new);
        add(AllBlockEntityTypes.ROPE_PULLEY, KineticAudioBehaviour::new, LinearActuatorTooltipBehaviour::new, MovementModeScrollBehaviour::pulley);
        add(
            AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER,
            KineticAudioBehaviour::new,
            KineticScrollValueBehaviour::controller,
            KineticTooltipBehaviour::new
        );
        add(AllBlockEntityTypes.SAW, FilteringBehaviour::saw, KineticAudioBehaviour::new, KineticTooltipBehaviour::new, SawAudioBehaviour::new);
        add(AllBlockEntityTypes.SEQUENCED_GEARSHIFT, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(AllBlockEntityTypes.SMART_CHUTE, ChuteTooltipBehaviour::new, FilteringBehaviour::chute);
        add(AllBlockEntityTypes.SMART_FLUID_PIPE, FilteringBehaviour::pipe);
        add(AllBlockEntityTypes.SMART_OBSERVER, FilteringBehaviour::observer);
        add(AllBlockEntityTypes.SPEEDOMETER, KineticAudioBehaviour::new, SpeedGaugeTooltipBehaviour::new);
        add(AllBlockEntityTypes.SPOUT, SpoutTooltipBehaviour::new);
        add(AllBlockEntityTypes.STEAM_ENGINE, RotationDirectionScrollBehaviour::engine, SteamEngineTooltipBehaviour::new);
        add(AllBlockEntityTypes.STEAM_WHISTLE, WhistleAnimationBehaviour::new, WhistleAudioBehaviour::new, WhistleTooltipBehaviour::new);
        add(AllBlockEntityTypes.STOCK_TICKER, StockTickerTooltipBehaviour::new);
        add(AllBlockEntityTypes.STRESSOMETER, KineticAudioBehaviour::new, StressGaugeTooltipBehaviour::new);
        add(AllBlockEntityTypes.TABLE_CLOTH, TableClothFilteringBehaviour::new);
        add(AllBlockEntityTypes.THRESHOLD_SWITCH, FilteringBehaviour::threshold);
        add(AllBlockEntityTypes.TRACK_OBSERVER, FilteringBehaviour::observer);
        add(AllBlockEntityTypes.TURNTABLE, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        add(
            AllBlockEntityTypes.VALVE_HANDLE,
            GeneratingKineticTooltipBehaviour::new,
            KineticAudioBehaviour::new,
            ValveHandleScrollValueBehaviour::new
        );
        add(AllBlockEntityTypes.WATER_WHEEL, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.WEIGHTED_EJECTOR, KineticAudioBehaviour::new, KineticScrollValueBehaviour::ejector, KineticTooltipBehaviour::new);
        add(
            AllBlockEntityTypes.WINDMILL_BEARING,
            KineticAudioBehaviour::new,
            MechanicalBearingTooltipBehaviour::new,
            RotationDirectionScrollBehaviour::windmill
        );
    }
}
