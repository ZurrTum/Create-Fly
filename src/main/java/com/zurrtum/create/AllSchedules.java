package com.zurrtum.create;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.schedule.condition.*;
import com.zurrtum.create.content.trains.schedule.destination.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class AllSchedules {
    public static List<Pair<ResourceLocation, Function<ResourceLocation, ? extends ScheduleInstruction>>> INSTRUCTION_TYPES = new ArrayList<>();
    public static List<Pair<ResourceLocation, Function<ResourceLocation, ? extends ScheduleWaitCondition>>> CONDITION_TYPES = new ArrayList<>();
    public static final ResourceLocation DESTINATION = registerInstruction("destination", DestinationInstruction::new);
    public static final ResourceLocation PACKAGE_DELIVERY = registerInstruction("package_delivery", DeliverPackagesInstruction::new);
    public static final ResourceLocation PACKAGE_RETRIEVAL = registerInstruction("package_retrieval", FetchPackagesInstruction::new);
    public static final ResourceLocation RENAME = registerInstruction("rename", ChangeTitleInstruction::new);
    public static final ResourceLocation THROTTLE = registerInstruction("throttle", ChangeThrottleInstruction::new);
    public static final ResourceLocation DELAY = registerCondition("delay", ScheduledDelay::new);
    public static final ResourceLocation TIME_OF_DAY = registerCondition("time_of_day", TimeOfDayCondition::new);
    public static final ResourceLocation FLUID_THRESHOLD = registerCondition("fluid_threshold", FluidThresholdCondition::new);
    public static final ResourceLocation ITEM_THRESHOLD = registerCondition("item_threshold", ItemThresholdCondition::new);
    public static final ResourceLocation REDSTONE_LINK = registerCondition("redstone_link", RedstoneLinkCondition::new);
    public static final ResourceLocation PLAYER_COUNT = registerCondition("player_count", PlayerPassengerCondition::new);
    public static final ResourceLocation IDLE = registerCondition("idle", IdleCargoCondition::new);
    public static final ResourceLocation UNLOADED = registerCondition("unloaded", StationUnloadedCondition::new);
    public static final ResourceLocation POWERED = registerCondition("powered", StationPoweredCondition::new);

    public static ScheduleInstruction createScheduleInstruction(ResourceLocation location) {
        for (Pair<ResourceLocation, Function<ResourceLocation, ? extends ScheduleInstruction>> type : INSTRUCTION_TYPES) {
            if (type.getFirst().equals(location)) {
                return type.getSecond().apply(location);
            }
        }
        return null;
    }

    public static ScheduleWaitCondition createScheduleWaitCondition(ResourceLocation location) {
        for (Pair<ResourceLocation, Function<ResourceLocation, ? extends ScheduleWaitCondition>> type : CONDITION_TYPES) {
            if (type.getFirst().equals(location)) {
                return type.getSecond().apply(location);
            }
        }
        return null;
    }

    private static ResourceLocation registerInstruction(String name, Function<ResourceLocation, ? extends ScheduleInstruction> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        INSTRUCTION_TYPES.add(Pair.of(id, factory));
        return id;
    }

    private static ResourceLocation registerCondition(String name, Function<ResourceLocation, ? extends ScheduleWaitCondition> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        CONDITION_TYPES.add(Pair.of(id, factory));
        return id;
    }

    public static void register() {
    }
}
