package com.zurrtum.create;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.schedule.condition.*;
import com.zurrtum.create.content.trains.schedule.destination.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllSchedules {
    public static List<Pair<Identifier, Function<Identifier, ? extends ScheduleInstruction>>> INSTRUCTION_TYPES = new ArrayList<>();
    public static List<Pair<Identifier, Function<Identifier, ? extends ScheduleWaitCondition>>> CONDITION_TYPES = new ArrayList<>();
    public static final Identifier DESTINATION = registerInstruction("destination", DestinationInstruction::new);
    public static final Identifier PACKAGE_DELIVERY = registerInstruction("package_delivery", DeliverPackagesInstruction::new);
    public static final Identifier PACKAGE_RETRIEVAL = registerInstruction("package_retrieval", FetchPackagesInstruction::new);
    public static final Identifier RENAME = registerInstruction("rename", ChangeTitleInstruction::new);
    public static final Identifier THROTTLE = registerInstruction("throttle", ChangeThrottleInstruction::new);
    public static final Identifier DELAY = registerCondition("delay", ScheduledDelay::new);
    public static final Identifier TIME_OF_DAY = registerCondition("time_of_day", TimeOfDayCondition::new);
    public static final Identifier FLUID_THRESHOLD = registerCondition("fluid_threshold", FluidThresholdCondition::new);
    public static final Identifier ITEM_THRESHOLD = registerCondition("item_threshold", ItemThresholdCondition::new);
    public static final Identifier REDSTONE_LINK = registerCondition("redstone_link", RedstoneLinkCondition::new);
    public static final Identifier PLAYER_COUNT = registerCondition("player_count", PlayerPassengerCondition::new);
    public static final Identifier IDLE = registerCondition("idle", IdleCargoCondition::new);
    public static final Identifier UNLOADED = registerCondition("unloaded", StationUnloadedCondition::new);
    public static final Identifier POWERED = registerCondition("powered", StationPoweredCondition::new);

    public static ScheduleInstruction createScheduleInstruction(Identifier location) {
        for (Pair<Identifier, Function<Identifier, ? extends ScheduleInstruction>> type : INSTRUCTION_TYPES) {
            if (type.getFirst().equals(location)) {
                return type.getSecond().apply(location);
            }
        }
        return null;
    }

    public static ScheduleWaitCondition createScheduleWaitCondition(Identifier location) {
        for (Pair<Identifier, Function<Identifier, ? extends ScheduleWaitCondition>> type : CONDITION_TYPES) {
            if (type.getFirst().equals(location)) {
                return type.getSecond().apply(location);
            }
        }
        return null;
    }

    private static Identifier registerInstruction(String name, Function<Identifier, ? extends ScheduleInstruction> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        INSTRUCTION_TYPES.add(Pair.of(id, factory));
        return id;
    }

    private static Identifier registerCondition(String name, Function<Identifier, ? extends ScheduleWaitCondition> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        CONDITION_TYPES.add(Pair.of(id, factory));
        return id;
    }

    public static void register() {
    }
}
