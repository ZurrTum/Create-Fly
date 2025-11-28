package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.compat.Mods;
import com.zurrtum.create.content.redstone.displayLink.source.*;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllDisplaySources {
    public static final ItemNameDisplaySource ITEM_NAMES = register("item_names", ItemNameDisplaySource::new);
    public static final TimeOfDayDisplaySource TIME_OF_DAY = register("time_of_day", TimeOfDayDisplaySource::new);
    public static final StopWatchDisplaySource STOPWATCH = register("stopwatch", StopWatchDisplaySource::new);
    public static final KineticSpeedDisplaySource KINETIC_SPEED = register("kinetic_speed", KineticSpeedDisplaySource::new);
    public static final KineticStressDisplaySource KINETIC_STRESS = register("kinetic_stress", KineticStressDisplaySource::new);
    public static final BoilerDisplaySource BOILER = register("boiler", BoilerDisplaySource::new);
    public static final CurrentFloorDisplaySource CURRENT_FLOOR = register("current_floor", CurrentFloorDisplaySource::new);
    public static final StationSummaryDisplaySource STATION_SUMMARY = register("station_summary", StationSummaryDisplaySource::new);
    public static final TrainStatusDisplaySource TRAIN_STATUS = register("train_status", TrainStatusDisplaySource::new);
    public static final ObservedTrainNameSource OBSERVED_TRAIN_NAME = register("observed_train_name", ObservedTrainNameSource::new);
    public static final AccumulatedItemCountDisplaySource ACCUMULATE_ITEMS = register("accumulate_items", AccumulatedItemCountDisplaySource::new);
    public static final ItemThroughputDisplaySource ITEM_THROUGHPUT = register("item_throughput", ItemThroughputDisplaySource::new);
    public static final ItemCountDisplaySource COUNT_ITEMS = register("count_items", ItemCountDisplaySource::new);
    public static final ItemListDisplaySource LIST_ITEMS = register("list_items", ItemListDisplaySource::new);
    public static final FluidAmountDisplaySource COUNT_FLUIDS = register("count_fluids", FluidAmountDisplaySource::new);
    public static final FluidListDisplaySource LIST_FLUIDS = register("list_fluids", FluidListDisplaySource::new);
    public static final PackageAddressDisplaySource READ_PACKAGE_ADDRESS = register("read_package_address", PackageAddressDisplaySource::new);
    public static final FillLevelDisplaySource FILL_LEVEL = register("fill_level", FillLevelDisplaySource::new);
    public static final FactoryGaugeDisplaySource GAUGE_STATUS = register("factory_gauge", FactoryGaugeDisplaySource::new);
    public static final EntityNameDisplaySource ENTITY_NAME = register("entity_name", EntityNameDisplaySource::new);
    public static final DeathCounterDisplaySource DEATH_COUNT = register("death_count", DeathCounterDisplaySource::new);
    public static final ScoreboardDisplaySource SCOREBOARD = register("scoreboard", ScoreboardDisplaySource::new);
    public static final EnchantPowerDisplaySource ENCHANT_POWER = register("enchant_power", EnchantPowerDisplaySource::new);
    public static final RedstonePowerDisplaySource REDSTONE_POWER = register("redstone_power", RedstonePowerDisplaySource::new);
    public static final NixieTubeDisplaySource NIXIE_TUBE = register("nixie_tube", NixieTubeDisplaySource::new);
    public static final ComputerDisplaySource COMPUTER = register("computer", ComputerDisplaySource::new);

    private static <T extends DisplaySource> T register(String id, Supplier<T> factory) {
        return Registry.register(CreateRegistries.DISPLAY_SOURCE, Identifier.of(MOD_ID, id), factory.get());
    }

    public static void register(DisplaySource display, Block... blocks) {
        for (Block block : blocks) {
            DisplaySource.BY_BLOCK.add(block, display);
        }
    }

    public static void register(DisplaySource display, BlockEntityType<?> type) {
        DisplaySource.BY_BLOCK_ENTITY.add(type, display);
    }

    public static void register() {
        register(ITEM_NAMES, AllBlocks.BELT, AllBlocks.DEPOT, AllBlocks.WEIGHTED_EJECTOR);
        register(TIME_OF_DAY, AllBlocks.CUCKOO_CLOCK);
        register(STOPWATCH, AllBlocks.CUCKOO_CLOCK);
        register(KINETIC_SPEED, AllBlocks.SPEEDOMETER);
        register(KINETIC_STRESS, AllBlocks.STRESSOMETER);
        register(BOILER, AllBlocks.FLUID_TANK);
        register(CURRENT_FLOOR, AllBlocks.ELEVATOR_CONTACT);
        register(STATION_SUMMARY, AllBlocks.TRACK_STATION);
        register(TRAIN_STATUS, AllBlocks.TRACK_STATION);
        register(OBSERVED_TRAIN_NAME, AllBlocks.TRACK_OBSERVER);
        register(ACCUMULATE_ITEMS, AllBlocks.ANDESITE_TUNNEL, AllBlocks.BRASS_TUNNEL);
        register(ITEM_THROUGHPUT, AllBlocks.ANDESITE_TUNNEL, AllBlocks.BRASS_TUNNEL);
        register(COUNT_ITEMS, AllBlocks.SMART_OBSERVER);
        register(LIST_ITEMS, AllBlocks.SMART_OBSERVER);
        register(COUNT_FLUIDS, AllBlocks.SMART_OBSERVER);
        register(LIST_FLUIDS, AllBlocks.SMART_OBSERVER);
        register(READ_PACKAGE_ADDRESS, AllBlocks.SMART_OBSERVER);
        register(FILL_LEVEL, AllBlocks.THRESHOLD_SWITCH);
        register(GAUGE_STATUS, AllBlocks.FACTORY_GAUGE);
        register(
                ENTITY_NAME,
                AllBlocks.WHITE_SEAT,
                AllBlocks.ORANGE_SEAT,
                AllBlocks.MAGENTA_SEAT,
                AllBlocks.LIGHT_BLUE_SEAT,
                AllBlocks.YELLOW_SEAT,
                AllBlocks.LIME_SEAT,
                AllBlocks.PINK_SEAT,
                AllBlocks.GRAY_SEAT,
                AllBlocks.LIGHT_GRAY_SEAT,
                AllBlocks.CYAN_SEAT,
                AllBlocks.PURPLE_SEAT,
                AllBlocks.BLUE_SEAT,
                AllBlocks.BROWN_SEAT,
                AllBlocks.GREEN_SEAT,
                AllBlocks.RED_SEAT,
                AllBlocks.BLACK_SEAT
        );
        register(DEATH_COUNT, Blocks.RESPAWN_ANCHOR);
        register(SCOREBOARD, Blocks.COMMAND_BLOCK);
        register(ENCHANT_POWER, Blocks.ENCHANTING_TABLE);
        register(REDSTONE_POWER, Blocks.TARGET);
        register(NIXIE_TUBE, AllBlockEntityTypes.NIXIE_TUBE);
        if (Mods.COMPUTERCRAFT.isLoaded()) {
            RegistryEntryAddedCallback.event(Registries.BLOCK).register((rawId, id, object) -> {
                List<String> types = List.of("wired_modem_full", "computer_normal", "computer_advanced", "computer_command");
                for (String name : types) {
                    Identifier cc_id = Mods.COMPUTERCRAFT.identifier(name);
                    if (cc_id.equals(id)) {
                        DisplaySource.BY_BLOCK.add(object, COMPUTER);
                    }
                }
            });

        }
    }
}
