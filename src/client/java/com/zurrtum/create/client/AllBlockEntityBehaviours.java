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
        add(AllBlockEntityTypes.MOTOR, KineticScrollValueBehaviour::motor);
        add(AllBlockEntityTypes.WATER_WHEEL, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER, KineticAudioBehaviour::new, KineticScrollValueBehaviour::controller);
        add(AllBlockEntityTypes.POWERED_SHAFT, PoweredShaftTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.STEAM_ENGINE, SteamEngineTooltipBehaviour::new, RotationDirectionScrollBehaviour::engine);
        add(AllBlockEntityTypes.CHAIN_CONVEYOR, KineticTooltipBehaviour::new, KineticAudioBehaviour::new, ChainConveyorClientBehaviour::new);
        add(AllBlockEntityTypes.HAND_CRANK, GeneratingKineticTooltipBehaviour::new, HandCrankAudioBehaviour::new);
        add(
            AllBlockEntityTypes.VALVE_HANDLE,
            GeneratingKineticTooltipBehaviour::new,
            KineticAudioBehaviour::new,
            ValveHandleScrollValueBehaviour::new
        );
        add(AllBlockEntityTypes.BACKTANK, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.BELT, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.BRACKETED_KINETIC, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.ADJUSTABLE_CHAIN_GEARSHIFT, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.CLUTCH, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.GANTRY_SHAFT, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.GANTRY_PINION, GantryCarriageTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.GEARBOX, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.GEARSHIFT, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.LARGE_WATER_WHEEL, GeneratingKineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.SEQUENCED_GEARSHIFT, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.ENCASED_COGWHEEL, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.CHASSIS, ChassisScrollValueBehaviour::new);
        add(
            AllBlockEntityTypes.WINDMILL_BEARING,
            MechanicalBearingTooltipBehaviour::new,
            KineticAudioBehaviour::new,
            RotationDirectionScrollBehaviour::windmill
        );
        add(AllBlockEntityTypes.FLUID_TANK, FluidTankTooltipBehaviour::new);
        add(AllBlockEntityTypes.WEIGHTED_EJECTOR, KineticAudioBehaviour::new, KineticScrollValueBehaviour::ejector);
        add(AllBlockEntityTypes.ROPE_PULLEY, LinearActuatorTooltipBehaviour::new, KineticAudioBehaviour::new, MovementModeScrollBehaviour::pulley);
        add(AllBlockEntityTypes.MILLSTONE, KineticTooltipBehaviour::new, KineticAudioBehaviour::new, MillstoneAudioBehaviour::new);
        add(AllBlockEntityTypes.ENCASED_FAN, KineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.SAW, FilteringBehaviour::saw, SawAudioBehaviour::new);
        add(AllBlockEntityTypes.BASIN, BasinTooltipBehaviour::new, FilteringBehaviour::basin);
        add(AllBlockEntityTypes.FUNNEL, FilteringBehaviour::funnel);
        add(
            AllBlockEntityTypes.BRASS_TUNNEL,
            BrassTunnelTooltipBehaviour::new,
            SidedFilteringBehaviour::tunnel,
            TunnelSelectionModeScrollBehaviour::new
        );
        add(AllBlockEntityTypes.CHUTE, ChuteTooltipBehaviour::new);
        add(AllBlockEntityTypes.SMART_CHUTE, ChuteTooltipBehaviour::new, FilteringBehaviour::chute);
        add(AllBlockEntityTypes.CART_ASSEMBLER, CartAssemblerTooltipBehaviour::new, CartMovementScrollBehaviour::new);
        add(
            AllBlockEntityTypes.MECHANICAL_PISTON,
            LinearActuatorTooltipBehaviour::new,
            KineticAudioBehaviour::new,
            MovementModeScrollBehaviour::piston
        );
        add(
            AllBlockEntityTypes.MECHANICAL_BEARING,
            MechanicalBearingTooltipBehaviour::new,
            KineticAudioBehaviour::new,
            RotationModeScrollBehaviour::new
        );
        add(AllBlockEntityTypes.SPEEDOMETER, SpeedGaugeTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.STRESSOMETER, StressGaugeTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.CUCKOO_CLOCK, CuckooClockAnimationBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.MECHANICAL_MIXER, KineticTooltipBehaviour::new, MechanicalMixerAudioBehaviour::new);
        add(AllBlockEntityTypes.HOSE_PULLEY, HosePulleyTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.SPOUT, SpoutTooltipBehaviour::new);
        add(AllBlockEntityTypes.ITEM_DRAIN, ItemDrainTooltipBehaviour::new);
        add(AllBlockEntityTypes.STEAM_WHISTLE, WhistleTooltipBehaviour::new, WhistleAnimationBehaviour::new, WhistleAudioBehaviour::new);
        add(AllBlockEntityTypes.DEPLOYER, DeployerTooltipBehaviour::new, FilteringBehaviour::deployer, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.TURNTABLE, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.CLOCKWORK_BEARING, ClockHandsScrollBehaviour::new);
        add(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, CrushingWheelControllerAudioBehaviour::new);
        add(AllBlockEntityTypes.FLAP_DISPLAY, KineticTooltipBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.FLUID_VALVE, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.SMART_FLUID_PIPE, FilteringBehaviour::pipe);
        add(AllBlockEntityTypes.ANALOG_LEVER, AnalogLeverTooltipBehaviour::new);
        add(AllBlockEntityTypes.REDSTONE_LINK, LinkBehaviour::new);
        add(AllBlockEntityTypes.PULSE_REPEATER, BrassDiodeScrollValueBehaviour::new);
        add(AllBlockEntityTypes.PULSE_EXTENDER, BrassDiodeScrollValueBehaviour::new);
        add(AllBlockEntityTypes.PULSE_TIMER, BrassDiodeScrollValueBehaviour::new);
        add(AllBlockEntityTypes.SMART_OBSERVER, FilteringBehaviour::observer);
        add(AllBlockEntityTypes.THRESHOLD_SWITCH, FilteringBehaviour::threshold);
        add(AllBlockEntityTypes.CONTRAPTION_CONTROLS, FilteringBehaviour::controls);
        add(AllBlockEntityTypes.CREATIVE_CRATE, FilteringBehaviour::crate);
        add(AllBlockEntityTypes.MECHANICAL_ARM, ArmTooltipBehaviour::new, ArmSelectionModeScrollBehaviour::new, KineticAudioBehaviour::new);
        add(AllBlockEntityTypes.TRACK_OBSERVER, FilteringBehaviour::observer);
        add(AllBlockEntityTypes.MECHANICAL_ROLLER, FilteringBehaviour::roller, RollingModeScrollBehaviour::new);
        add(AllBlockEntityTypes.STOCK_TICKER, StockTickerTooltipBehaviour::new);
        add(AllBlockEntityTypes.TABLE_CLOTH, TableClothFilteringBehaviour::new);
        add(AllBlockEntityTypes.PACKAGE_FROGPORT, FrogportClientAudioBehaviour::new, FrogportTooltipBehaviour::new);
        add(AllBlockEntityTypes.FACTORY_PANEL, FactoryPanelBehaviour.allSlot());
        add(AllBlockEntityTypes.ITEM_HATCH, FilteringBehaviour::hatch);
        add(AllBlockEntityTypes.MECHANICAL_PUMP, KineticTooltipBehaviour::new);
    }
}
