package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockItem;
import com.zurrtum.create.content.contraptions.bearing.BlankSailBlockItem;
import com.zurrtum.create.content.contraptions.glue.SuperGlueItem;
import com.zurrtum.create.content.contraptions.minecart.MinecartCouplingItem;
import com.zurrtum.create.content.contraptions.mounted.CartAssemblerBlockItem;
import com.zurrtum.create.content.contraptions.mounted.MinecartContraptionItem;
import com.zurrtum.create.content.decoration.MetalScaffoldingBlockItem;
import com.zurrtum.create.content.decoration.bracket.BracketBlockItem;
import com.zurrtum.create.content.equipment.BuildersTeaItem;
import com.zurrtum.create.content.equipment.TreeFertilizerItem;
import com.zurrtum.create.content.equipment.armor.*;
import com.zurrtum.create.content.equipment.blueprint.BlueprintItem;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockItem;
import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.content.equipment.tool.AllToolMaterials;
import com.zurrtum.create.content.equipment.tool.CardboardSwordItem;
import com.zurrtum.create.content.equipment.wrench.WrenchItem;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.WorldshaperItem;
import com.zurrtum.create.content.fluids.tank.FluidTankItem;
import com.zurrtum.create.content.kinetics.belt.item.BeltConnectorItem;
import com.zurrtum.create.content.kinetics.gearbox.VerticalGearboxItem;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmItem;
import com.zurrtum.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockItem;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.chute.ChuteItem;
import com.zurrtum.create.content.logistics.depot.EjectorItem;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.zurrtum.create.content.logistics.filter.AttributeFilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.ListFilterItem;
import com.zurrtum.create.content.logistics.filter.PackageFilterItem;
import com.zurrtum.create.content.logistics.funnel.FunnelItem;
import com.zurrtum.create.content.logistics.packagePort.PackagePortItem;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.zurrtum.create.content.logistics.tableCloth.ShoppingListItem;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockItem;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelItem;
import com.zurrtum.create.content.logistics.vault.ItemVaultItem;
import com.zurrtum.create.content.materials.ExperienceNuggetItem;
import com.zurrtum.create.content.processing.AssemblyOperatorBlockItem;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockItem;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import com.zurrtum.create.content.redstone.contact.RedstoneContactItem;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockItem;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import com.zurrtum.create.content.schematics.SchematicAndQuillItem;
import com.zurrtum.create.content.schematics.SchematicItem;
import com.zurrtum.create.content.trains.schedule.ScheduleItem;
import com.zurrtum.create.content.trains.track.TrackBlockItem;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.foundation.item.TagDependentIngredientItem;
import com.zurrtum.create.foundation.item.UncontainableBlockItem;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public class AllItems {
    public static final CogwheelBlockItem COGWHEEL = register(AllBlocks.COGWHEEL, CogwheelBlockItem::new);
    public static final CogwheelBlockItem LARGE_COGWHEEL = register(AllBlocks.LARGE_COGWHEEL, CogwheelBlockItem::new);
    public static final BlockItem SHAFT = register(AllBlocks.SHAFT);
    public static final BlockItem GANTRY_SHAFT = register(AllBlocks.GANTRY_SHAFT);
    public static final BlockItem SEQUENCED_GEARSHIFT = register(AllBlocks.SEQUENCED_GEARSHIFT);
    public static final BlockItem STEAM_ENGINE = register(AllBlocks.STEAM_ENGINE);
    public static final BlockItem GANTRY_CARRIAGE = register(AllBlocks.GANTRY_CARRIAGE);
    public static final LargeWaterWheelBlockItem LARGE_WATER_WHEEL = register(AllBlocks.LARGE_WATER_WHEEL, LargeWaterWheelBlockItem::new);
    public static final BlockItem CREATIVE_MOTOR = register(AllBlocks.CREATIVE_MOTOR, BlockItem::new, new Item.Settings().rarity(Rarity.EPIC));
    public static final BlockItem ROTATION_SPEED_CONTROLLER = register(AllBlocks.ROTATION_SPEED_CONTROLLER);
    public static final BlockItem GEARBOX = register(AllBlocks.GEARBOX);
    public static final BlockItem WATER_WHEEL = register(AllBlocks.WATER_WHEEL);
    public static final BlockItem ANDESITE_CASING = register(AllBlocks.ANDESITE_CASING);
    public static final BlockItem BRASS_CASING = register(AllBlocks.BRASS_CASING);
    public static final BlockItem COPPER_CASING = register(AllBlocks.COPPER_CASING);
    public static final BlockItem SHADOW_STEEL_CASING = register(AllBlocks.SHADOW_STEEL_CASING);
    public static final BlockItem REFINED_RADIANCE_CASING = register(AllBlocks.REFINED_RADIANCE_CASING);
    public static final BlockItem RAILWAY_CASING = register(AllBlocks.RAILWAY_CASING);
    public static final ArmItem MECHANICAL_ARM = register(AllBlocks.MECHANICAL_ARM, ArmItem::new);
    public static final BlockItem DEPOT = register(AllBlocks.DEPOT);
    public static final BlockItem CLUTCH = register(AllBlocks.CLUTCH);
    public static final BlockItem GEARSHIFT = register(AllBlocks.GEARSHIFT);
    public static final BlockItem ENCASED_CHAIN_DRIVE = register(AllBlocks.ENCASED_CHAIN_DRIVE);
    public static final BlockItem ADJUSTABLE_CHAIN_GEARSHIFT = register(AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT);
    public static final BlockItem CHAIN_CONVEYOR = register(AllBlocks.CHAIN_CONVEYOR);
    public static final BlockItem HAND_CRANK = register(AllBlocks.HAND_CRANK);
    public static final BlockItem COPPER_VALVE_HANDLE = register(AllBlocks.COPPER_VALVE_HANDLE);
    public static final BlockItem WHITE_VALVE_HANDLE = register(AllBlocks.WHITE_VALVE_HANDLE);
    public static final BlockItem ORANGE_VALVE_HANDLE = register(AllBlocks.ORANGE_VALVE_HANDLE);
    public static final BlockItem MAGENTA_VALVE_HANDLE = register(AllBlocks.MAGENTA_VALVE_HANDLE);
    public static final BlockItem LIGHT_BLUE_VALVE_HANDLE = register(AllBlocks.LIGHT_BLUE_VALVE_HANDLE);
    public static final BlockItem YELLOW_VALVE_HANDLE = register(AllBlocks.YELLOW_VALVE_HANDLE);
    public static final BlockItem LIME_VALVE_HANDLE = register(AllBlocks.LIME_VALVE_HANDLE);
    public static final BlockItem PINK_VALVE_HANDLE = register(AllBlocks.PINK_VALVE_HANDLE);
    public static final BlockItem GRAY_VALVE_HANDLE = register(AllBlocks.GRAY_VALVE_HANDLE);
    public static final BlockItem LIGHT_GRAY_VALVE_HANDLE = register(AllBlocks.LIGHT_GRAY_VALVE_HANDLE);
    public static final BlockItem CYAN_VALVE_HANDLE = register(AllBlocks.CYAN_VALVE_HANDLE);
    public static final BlockItem PURPLE_VALVE_HANDLE = register(AllBlocks.PURPLE_VALVE_HANDLE);
    public static final BlockItem BLUE_VALVE_HANDLE = register(AllBlocks.BLUE_VALVE_HANDLE);
    public static final BlockItem BROWN_VALVE_HANDLE = register(AllBlocks.BROWN_VALVE_HANDLE);
    public static final BlockItem GREEN_VALVE_HANDLE = register(AllBlocks.GREEN_VALVE_HANDLE);
    public static final BlockItem RED_VALVE_HANDLE = register(AllBlocks.RED_VALVE_HANDLE);
    public static final BlockItem BLACK_VALVE_HANDLE = register(AllBlocks.BLACK_VALVE_HANDLE);
    public static final BlockItem RADIAL_CHASSIS = register(AllBlocks.RADIAL_CHASSIS);
    public static final BlockItem LINEAR_CHASSIS = register(AllBlocks.LINEAR_CHASSIS);
    public static final BlockItem SECONDARY_LINEAR_CHASSIS = register(AllBlocks.SECONDARY_LINEAR_CHASSIS);
    public static final BlockItem WINDMILL_BEARING = register(AllBlocks.WINDMILL_BEARING);
    public static final BlockItem MECHANICAL_BEARING = register(AllBlocks.MECHANICAL_BEARING);
    public static final BlockItem MECHANICAL_PISTON = register(AllBlocks.MECHANICAL_PISTON);
    public static final BlockItem STICKY_MECHANICAL_PISTON = register(AllBlocks.STICKY_MECHANICAL_PISTON);
    public static final BlockItem PISTON_EXTENSION_POLE = register(AllBlocks.PISTON_EXTENSION_POLE);
    public static final BlockItem SAIL_FRAME = register(AllBlocks.SAIL_FRAME);
    public static final BlockItem SAIL = register(AllBlocks.SAIL, BlankSailBlockItem::new);
    public static final BlockItem FLUID_PIPE = register(AllBlocks.FLUID_PIPE);
    public static final BlockItem MECHANICAL_PUMP = register(AllBlocks.MECHANICAL_PUMP);
    public static final BlazeBurnerBlockItem BLAZE_BURNER = register(AllBlocks.BLAZE_BURNER, BlazeBurnerBlockItem::withBlaze);
    public static final FluidTankItem FLUID_TANK = register(AllBlocks.FLUID_TANK, FluidTankItem::new);
    public static final FluidTankItem CREATIVE_FLUID_TANK = register(
        AllBlocks.CREATIVE_FLUID_TANK,
        FluidTankItem::new,
        new Item.Settings().rarity(Rarity.EPIC)
    );
    public static final AssemblyOperatorBlockItem MECHANICAL_PRESS = register(AllBlocks.MECHANICAL_PRESS, AssemblyOperatorBlockItem::new);
    public static final EjectorItem WEIGHTED_EJECTOR = register(AllBlocks.WEIGHTED_EJECTOR, EjectorItem::new);
    public static final BlockItem ROPE_PULLEY = register(AllBlocks.ROPE_PULLEY);
    public static final BlockItem MILLSTONE = register(AllBlocks.MILLSTONE);
    public static final BlockItem ENCASED_FAN = register(AllBlocks.ENCASED_FAN);
    public static final BlockItem PECULIAR_BELL = register(AllBlocks.PECULIAR_BELL);
    public static final BlockItem HAUNTED_BELL = register(AllBlocks.HAUNTED_BELL);
    public static final BlockItem INDUSTRIAL_IRON_BLOCK = register(AllBlocks.INDUSTRIAL_IRON_BLOCK);
    public static final BlockItem WEATHERED_IRON_BLOCK = register(AllBlocks.WEATHERED_IRON_BLOCK);
    public static final BlockItem INDUSTRIAL_IRON_WINDOW = register(AllBlocks.INDUSTRIAL_IRON_WINDOW);
    public static final BlockItem INDUSTRIAL_IRON_WINDOW_PANE = register(AllBlocks.INDUSTRIAL_IRON_WINDOW_PANE);
    public static final BlockItem WEATHERED_IRON_WINDOW = register(AllBlocks.WEATHERED_IRON_WINDOW);
    public static final BlockItem WEATHERED_IRON_WINDOW_PANE = register(AllBlocks.WEATHERED_IRON_WINDOW_PANE);
    public static final BlockItem MECHANICAL_SAW = register(AllBlocks.MECHANICAL_SAW);
    public static final BlockItem BASIN = register(AllBlocks.BASIN);
    public static final FunnelItem ANDESITE_FUNNEL = register(AllBlocks.ANDESITE_FUNNEL, FunnelItem::new);
    public static final FunnelItem BRASS_FUNNEL = register(AllBlocks.BRASS_FUNNEL, FunnelItem::new);
    public static final BeltTunnelItem ANDESITE_TUNNEL = register(AllBlocks.ANDESITE_TUNNEL, BeltTunnelItem::new);
    public static final BeltTunnelItem BRASS_TUNNEL = register(AllBlocks.BRASS_TUNNEL, BeltTunnelItem::new);
    public static final ChuteItem CHUTE = register(AllBlocks.CHUTE, ChuteItem::new);
    public static final BlockItem SMART_CHUTE = register(AllBlocks.SMART_CHUTE);
    public static final CartAssemblerBlockItem CART_ASSEMBLER = register(AllBlocks.CART_ASSEMBLER, CartAssemblerBlockItem::new);
    public static final BlockItem CONTROLLER_RAIL = register(AllBlocks.CONTROLLER_RAIL);
    public static final BlockItem MECHANICAL_PLOUGH = register(AllBlocks.MECHANICAL_PLOUGH);
    public static final BlockItem MECHANICAL_HARVESTER = register(AllBlocks.MECHANICAL_HARVESTER);
    public static final BlockItem PORTABLE_FLUID_INTERFACE = register(AllBlocks.PORTABLE_FLUID_INTERFACE);
    public static final BlockItem PORTABLE_STORAGE_INTERFACE = register(AllBlocks.PORTABLE_STORAGE_INTERFACE);
    public static final BlockItem SPEEDOMETER = register(AllBlocks.SPEEDOMETER);
    public static final BlockItem STRESSOMETER = register(AllBlocks.STRESSOMETER);
    public static final BlockItem CUCKOO_CLOCK = register(AllBlocks.CUCKOO_CLOCK);
    public static final BlockItem MYSTERIOUS_CUCKOO_CLOCK = register(AllBlocks.MYSTERIOUS_CUCKOO_CLOCK);
    public static final BlockItem MECHANICAL_MIXER = register(AllBlocks.MECHANICAL_MIXER, AssemblyOperatorBlockItem::new);
    public static final BlockItem HOSE_PULLEY = register(AllBlocks.HOSE_PULLEY);
    public static final AssemblyOperatorBlockItem SPOUT = register(AllBlocks.SPOUT, AssemblyOperatorBlockItem::new);
    public static final BlockItem ITEM_DRAIN = register(AllBlocks.ITEM_DRAIN);
    public static final BlockItem STEAM_WHISTLE = register(AllBlocks.STEAM_WHISTLE);
    public static final AssemblyOperatorBlockItem DEPLOYER = register(AllBlocks.DEPLOYER, AssemblyOperatorBlockItem::new);
    public static final BlockItem TURNTABLE = register(AllBlocks.TURNTABLE);
    public static final BlockItem MECHANICAL_DRILL = register(AllBlocks.MECHANICAL_DRILL);
    public static final BlockItem CLOCKWORK_BEARING = register(AllBlocks.CLOCKWORK_BEARING);
    public static final BlockItem CRUSHING_WHEEL = register(AllBlocks.CRUSHING_WHEEL);
    public static final BlockItem RAW_ZINC_BLOCK = register(AllBlocks.RAW_ZINC_BLOCK);
    public static final BlockItem ZINC_BLOCK = register(AllBlocks.ZINC_BLOCK);
    public static final BlockItem ZINC_ORE = register(AllBlocks.ZINC_ORE);
    public static final BlockItem DEEPSLATE_ZINC_ORE = register(AllBlocks.DEEPSLATE_ZINC_ORE);
    public static final BlockItem BRASS_BLOCK = register(AllBlocks.BRASS_BLOCK);
    public static final BlockItem DISPLAY_BOARD = register(AllBlocks.DISPLAY_BOARD);
    public static final ClipboardBlockItem CLIPBOARD = register(AllBlocks.CLIPBOARD, ClipboardBlockItem::new);
    public static final DisplayLinkBlockItem DISPLAY_LINK = register(AllBlocks.DISPLAY_LINK, DisplayLinkBlockItem::new);
    public static final BlockItem ORANGE_NIXIE_TUBE = register(AllBlocks.ORANGE_NIXIE_TUBE);
    public static final BracketBlockItem WOODEN_BRACKET = register(AllBlocks.WOODEN_BRACKET, BracketBlockItem::new);
    public static final BracketBlockItem METAL_BRACKET = register(AllBlocks.METAL_BRACKET, BracketBlockItem::new);
    public static final BlockItem METAL_GIRDER = register(AllBlocks.METAL_GIRDER);
    public static final BlockItem FLUID_VALVE = register(AllBlocks.FLUID_VALVE);
    public static final BlockItem SMART_FLUID_PIPE = register(AllBlocks.SMART_FLUID_PIPE);
    public static final BlockItem ANALOG_LEVER = register(AllBlocks.ANALOG_LEVER);
    public static final RedstoneContactItem REDSTONE_CONTACT = register(AllBlocks.REDSTONE_CONTACT, RedstoneContactItem::new);
    public static final BlockItem REDSTONE_LINK = register(AllBlocks.REDSTONE_LINK);
    public static final BlockItem PULSE_REPEATER = register(AllBlocks.PULSE_REPEATER);
    public static final BlockItem PULSE_EXTENDER = register(AllBlocks.PULSE_EXTENDER);
    public static final BlockItem PULSE_TIMER = register(AllBlocks.PULSE_TIMER);
    public static final BlockItem POWERED_LATCH = register(AllBlocks.POWERED_LATCH);
    public static final BlockItem POWERED_TOGGLE_LATCH = register(AllBlocks.POWERED_TOGGLE_LATCH);
    public static final BlockItem ROSE_QUARTZ_LAMP = register(AllBlocks.ROSE_QUARTZ_LAMP);
    public static final BlockItem SMART_OBSERVER = register(AllBlocks.SMART_OBSERVER);
    public static final BlockItem THRESHOLD_SWITCH = register(AllBlocks.THRESHOLD_SWITCH);
    public static final BlockItem STICKER = register(AllBlocks.STICKER);
    public static final BlockItem CONTRAPTION_CONTROLS = register(AllBlocks.CONTRAPTION_CONTROLS);
    public static final BlockItem ELEVATOR_PULLEY = register(AllBlocks.ELEVATOR_PULLEY);
    public static final BlockItem ELEVATOR_CONTACT = register(AllBlocks.ELEVATOR_CONTACT);
    public static final BlockItem ANDESITE_DOOR = register(AllBlocks.ANDESITE_DOOR);
    public static final BlockItem BRASS_DOOR = register(AllBlocks.BRASS_DOOR);
    public static final BlockItem COPPER_DOOR = register(AllBlocks.COPPER_DOOR);
    public static final BlockItem TRAIN_DOOR = register(AllBlocks.TRAIN_DOOR);
    public static final BlockItem FRAMED_GLASS_DOOR = register(AllBlocks.FRAMED_GLASS_DOOR);
    public static final BlockItem NOZZLE = register(AllBlocks.NOZZLE);
    public static final BlockItem DESK_BELL = register(AllBlocks.DESK_BELL);
    public static final BlockItem MECHANICAL_CRAFTER = register(AllBlocks.MECHANICAL_CRAFTER);
    public static final BlockItem CREATIVE_CRATE = register(AllBlocks.CREATIVE_CRATE, BlockItem::new, new Item.Settings().rarity(Rarity.EPIC));
    public static final ItemVaultItem ITEM_VAULT = register(AllBlocks.ITEM_VAULT, ItemVaultItem::new);
    public static final TrackBlockItem TRACK = register(AllBlocks.TRACK, TrackBlockItem::new);
    public static final BlockItem TRAIN_CONTROLS = register(AllBlocks.TRAIN_CONTROLS);
    public static final TrackTargetingBlockItem TRACK_STATION = register(AllBlocks.TRACK_STATION, TrackTargetingBlockItem::station);
    public static final TrackTargetingBlockItem TRACK_SIGNAL = register(AllBlocks.TRACK_SIGNAL, TrackTargetingBlockItem::signal);
    public static final TrackTargetingBlockItem TRACK_OBSERVER = register(AllBlocks.TRACK_OBSERVER, TrackTargetingBlockItem::observer);
    public static final BlockItem WHITE_SEAT = register(AllBlocks.WHITE_SEAT);
    public static final BlockItem ORANGE_SEAT = register(AllBlocks.ORANGE_SEAT);
    public static final BlockItem MAGENTA_SEAT = register(AllBlocks.MAGENTA_SEAT);
    public static final BlockItem LIGHT_BLUE_SEAT = register(AllBlocks.LIGHT_BLUE_SEAT);
    public static final BlockItem YELLOW_SEAT = register(AllBlocks.YELLOW_SEAT);
    public static final BlockItem LIME_SEAT = register(AllBlocks.LIME_SEAT);
    public static final BlockItem PINK_SEAT = register(AllBlocks.PINK_SEAT);
    public static final BlockItem GRAY_SEAT = register(AllBlocks.GRAY_SEAT);
    public static final BlockItem LIGHT_GRAY_SEAT = register(AllBlocks.LIGHT_GRAY_SEAT);
    public static final BlockItem CYAN_SEAT = register(AllBlocks.CYAN_SEAT);
    public static final BlockItem PURPLE_SEAT = register(AllBlocks.PURPLE_SEAT);
    public static final BlockItem BLUE_SEAT = register(AllBlocks.BLUE_SEAT);
    public static final BlockItem BROWN_SEAT = register(AllBlocks.BROWN_SEAT);
    public static final BlockItem GREEN_SEAT = register(AllBlocks.GREEN_SEAT);
    public static final BlockItem RED_SEAT = register(AllBlocks.RED_SEAT);
    public static final BlockItem BLACK_SEAT = register(AllBlocks.BLACK_SEAT);
    public static final RollerBlockItem MECHANICAL_ROLLER = register(AllBlocks.MECHANICAL_ROLLER, RollerBlockItem::new);
    public static final BlockItem PACKAGER = register(AllBlocks.PACKAGER);
    public static final BlockItem CARDBOARD_BLOCK = register(AllBlocks.CARDBOARD_BLOCK);
    public static final LogisticallyLinkedBlockItem STOCK_LINK = register(AllBlocks.STOCK_LINK, LogisticallyLinkedBlockItem::new);
    public static final RedstoneRequesterBlockItem REDSTONE_REQUESTER = register(AllBlocks.REDSTONE_REQUESTER, RedstoneRequesterBlockItem::new);
    public static final BlockItem REPACKAGER = register(AllBlocks.REPACKAGER);
    public static final LogisticallyLinkedBlockItem STOCK_TICKER = register(AllBlocks.STOCK_TICKER, LogisticallyLinkedBlockItem::new);
    public static final TableClothBlockItem WHITE_TABLE_CLOTH = register(AllBlocks.WHITE_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem ORANGE_TABLE_CLOTH = register(AllBlocks.ORANGE_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem MAGENTA_TABLE_CLOTH = register(AllBlocks.MAGENTA_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem LIGHT_BLUE_TABLE_CLOTH = register(AllBlocks.LIGHT_BLUE_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem YELLOW_TABLE_CLOTH = register(AllBlocks.YELLOW_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem LIME_TABLE_CLOTH = register(AllBlocks.LIME_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem PINK_TABLE_CLOTH = register(AllBlocks.PINK_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem GRAY_TABLE_CLOTH = register(AllBlocks.GRAY_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem LIGHT_GRAY_TABLE_CLOTH = register(AllBlocks.LIGHT_GRAY_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem CYAN_TABLE_CLOTH = register(AllBlocks.CYAN_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem PURPLE_TABLE_CLOTH = register(AllBlocks.PURPLE_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem BLUE_TABLE_CLOTH = register(AllBlocks.BLUE_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem BROWN_TABLE_CLOTH = register(AllBlocks.BROWN_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem GREEN_TABLE_CLOTH = register(AllBlocks.GREEN_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem RED_TABLE_CLOTH = register(AllBlocks.RED_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem BLACK_TABLE_CLOTH = register(AllBlocks.BLACK_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem ANDESITE_TABLE_CLOTH = register(AllBlocks.ANDESITE_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem BRASS_TABLE_CLOTH = register(AllBlocks.BRASS_TABLE_CLOTH, TableClothBlockItem::new);
    public static final TableClothBlockItem COPPER_TABLE_CLOTH = register(AllBlocks.COPPER_TABLE_CLOTH, TableClothBlockItem::new);
    public static final PackagePortItem WHITE_POSTBOX = register(AllBlocks.WHITE_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem ORANGE_POSTBOX = register(AllBlocks.ORANGE_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem MAGENTA_POSTBOX = register(AllBlocks.MAGENTA_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem LIGHT_BLUE_POSTBOX = register(AllBlocks.LIGHT_BLUE_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem YELLOW_POSTBOX = register(AllBlocks.YELLOW_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem LIME_POSTBOX = register(AllBlocks.LIME_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem PINK_POSTBOX = register(AllBlocks.PINK_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem GRAY_POSTBOX = register(AllBlocks.GRAY_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem LIGHT_GRAY_POSTBOX = register(AllBlocks.LIGHT_GRAY_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem CYAN_POSTBOX = register(AllBlocks.CYAN_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem PURPLE_POSTBOX = register(AllBlocks.PURPLE_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem BLUE_POSTBOX = register(AllBlocks.BLUE_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem BROWN_POSTBOX = register(AllBlocks.BROWN_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem GREEN_POSTBOX = register(AllBlocks.GREEN_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem RED_POSTBOX = register(AllBlocks.RED_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem BLACK_POSTBOX = register(AllBlocks.BLACK_POSTBOX, PackagePortItem::new);
    public static final PackagePortItem PACKAGE_FROGPORT = register(AllBlocks.PACKAGE_FROGPORT, PackagePortItem::new);
    public static final FactoryPanelBlockItem FACTORY_GAUGE = register(AllBlocks.FACTORY_GAUGE, FactoryPanelBlockItem::new);
    public static final BlockItem FLYWHEEL = register(AllBlocks.FLYWHEEL);
    public static final BlockItem ITEM_HATCH = register(AllBlocks.ITEM_HATCH);
    public static final BlockItem PLACARD = register(AllBlocks.PLACARD);
    public static final UncontainableBlockItem WHITE_TOOLBOX = register(AllBlocks.WHITE_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem ORANGE_TOOLBOX = register(AllBlocks.ORANGE_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem MAGENTA_TOOLBOX = register(AllBlocks.MAGENTA_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem LIGHT_BLUE_TOOLBOX = register(AllBlocks.LIGHT_BLUE_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem YELLOW_TOOLBOX = register(AllBlocks.YELLOW_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem LIME_TOOLBOX = register(AllBlocks.LIME_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem PINK_TOOLBOX = register(AllBlocks.PINK_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem GRAY_TOOLBOX = register(AllBlocks.GRAY_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem LIGHT_GRAY_TOOLBOX = register(AllBlocks.LIGHT_GRAY_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem CYAN_TOOLBOX = register(AllBlocks.CYAN_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem PURPLE_TOOLBOX = register(AllBlocks.PURPLE_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem BLUE_TOOLBOX = register(AllBlocks.BLUE_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem BROWN_TOOLBOX = register(AllBlocks.BROWN_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem GREEN_TOOLBOX = register(AllBlocks.GREEN_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem RED_TOOLBOX = register(AllBlocks.RED_TOOLBOX, UncontainableBlockItem::new);
    public static final UncontainableBlockItem BLACK_TOOLBOX = register(AllBlocks.BLACK_TOOLBOX, UncontainableBlockItem::new);
    public static final BlockItem SCHEMATIC_TABLE = register(AllBlocks.SCHEMATIC_TABLE);
    public static final BlockItem SCHEMATICANNON = register(AllBlocks.SCHEMATICANNON);
    public static final BlockItem ANDESITE_ENCASED_SHAFT = register(AllBlocks.ANDESITE_ENCASED_SHAFT);
    public static final BlockItem BRASS_ENCASED_SHAFT = register(AllBlocks.BRASS_ENCASED_SHAFT);
    public static final BlockItem ORNATE_IRON_WINDOW = register(AllBlocks.ORNATE_IRON_WINDOW);
    public static final BlockItem ANDESITE_LADDER = register(AllBlocks.ANDESITE_LADDER);
    public static final BlockItem BRASS_LADDER = register(AllBlocks.BRASS_LADDER);
    public static final BlockItem COPPER_LADDER = register(AllBlocks.COPPER_LADDER);
    public static final MetalScaffoldingBlockItem ANDESITE_SCAFFOLD = register(AllBlocks.ANDESITE_SCAFFOLD, MetalScaffoldingBlockItem::new);
    public static final MetalScaffoldingBlockItem BRASS_SCAFFOLD = register(AllBlocks.BRASS_SCAFFOLD, MetalScaffoldingBlockItem::new);
    public static final MetalScaffoldingBlockItem COPPER_SCAFFOLD = register(AllBlocks.COPPER_SCAFFOLD, MetalScaffoldingBlockItem::new);
    public static final BlockItem ANDESITE_BARS = register(AllBlocks.ANDESITE_BARS);
    public static final BlockItem BRASS_BARS = register(AllBlocks.BRASS_BARS);
    public static final BlockItem COPPER_BARS = register(AllBlocks.COPPER_BARS);
    public static final BlockItem TRAIN_TRAPDOOR = register(AllBlocks.TRAIN_TRAPDOOR);
    public static final BlockItem FRAMED_GLASS_TRAPDOOR = register(AllBlocks.FRAMED_GLASS_TRAPDOOR);
    public static final BlockItem ANDESITE_ALLOY_BLOCK = register(AllBlocks.ANDESITE_ALLOY_BLOCK);
    public static final BlockItem BOUND_CARDBOARD_BLOCK = register(AllBlocks.BOUND_CARDBOARD_BLOCK);
    public static final BlockItem EXPERIENCE_BLOCK = register(
        AllBlocks.EXPERIENCE_BLOCK,
        BlockItem::new,
        new Item.Settings().rarity(Rarity.UNCOMMON)
    );
    public static final BlockItem ROSE_QUARTZ_BLOCK = register(AllBlocks.ROSE_QUARTZ_BLOCK);
    public static final BlockItem ROSE_QUARTZ_TILES = register(AllBlocks.ROSE_QUARTZ_TILES);
    public static final BlockItem SMALL_ROSE_QUARTZ_TILES = register(AllBlocks.SMALL_ROSE_QUARTZ_TILES);
    public static final BlockItem COPPER_SHINGLES = register(AllBlocks.COPPER_SHINGLES);
    public static final BlockItem EXPOSED_COPPER_SHINGLES = register(AllBlocks.EXPOSED_COPPER_SHINGLES);
    public static final BlockItem WEATHERED_COPPER_SHINGLES = register(AllBlocks.WEATHERED_COPPER_SHINGLES);
    public static final BlockItem OXIDIZED_COPPER_SHINGLES = register(AllBlocks.OXIDIZED_COPPER_SHINGLES);
    public static final BlockItem WAXED_COPPER_SHINGLES = register(AllBlocks.WAXED_COPPER_SHINGLES);
    public static final BlockItem WAXED_EXPOSED_COPPER_SHINGLES = register(AllBlocks.WAXED_EXPOSED_COPPER_SHINGLES);
    public static final BlockItem WAXED_WEATHERED_COPPER_SHINGLES = register(AllBlocks.WAXED_WEATHERED_COPPER_SHINGLES);
    public static final BlockItem WAXED_OXIDIZED_COPPER_SHINGLES = register(AllBlocks.WAXED_OXIDIZED_COPPER_SHINGLES);
    public static final BlockItem COPPER_SHINGLE_SLAB = register(AllBlocks.COPPER_SHINGLE_SLAB);
    public static final BlockItem EXPOSED_COPPER_SHINGLE_SLAB = register(AllBlocks.EXPOSED_COPPER_SHINGLE_SLAB);
    public static final BlockItem WEATHERED_COPPER_SHINGLE_SLAB = register(AllBlocks.WEATHERED_COPPER_SHINGLE_SLAB);
    public static final BlockItem OXIDIZED_COPPER_SHINGLE_SLAB = register(AllBlocks.OXIDIZED_COPPER_SHINGLE_SLAB);
    public static final BlockItem WAXED_COPPER_SHINGLE_SLAB = register(AllBlocks.WAXED_COPPER_SHINGLE_SLAB);
    public static final BlockItem WAXED_EXPOSED_COPPER_SHINGLE_SLAB = register(AllBlocks.WAXED_EXPOSED_COPPER_SHINGLE_SLAB);
    public static final BlockItem WAXED_WEATHERED_COPPER_SHINGLE_SLAB = register(AllBlocks.WAXED_WEATHERED_COPPER_SHINGLE_SLAB);
    public static final BlockItem WAXED_OXIDIZED_COPPER_SHINGLE_SLAB = register(AllBlocks.WAXED_OXIDIZED_COPPER_SHINGLE_SLAB);
    public static final BlockItem COPPER_SHINGLE_STAIRS = register(AllBlocks.COPPER_SHINGLE_STAIRS);
    public static final BlockItem EXPOSED_COPPER_SHINGLE_STAIRS = register(AllBlocks.EXPOSED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem WEATHERED_COPPER_SHINGLE_STAIRS = register(AllBlocks.WEATHERED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem OXIDIZED_COPPER_SHINGLE_STAIRS = register(AllBlocks.OXIDIZED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem WAXED_COPPER_SHINGLE_STAIRS = register(AllBlocks.WAXED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem WAXED_EXPOSED_COPPER_SHINGLE_STAIRS = register(AllBlocks.WAXED_EXPOSED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem WAXED_WEATHERED_COPPER_SHINGLE_STAIRS = register(AllBlocks.WAXED_WEATHERED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem WAXED_OXIDIZED_COPPER_SHINGLE_STAIRS = register(AllBlocks.WAXED_OXIDIZED_COPPER_SHINGLE_STAIRS);
    public static final BlockItem COPPER_TILES = register(AllBlocks.COPPER_TILES);
    public static final BlockItem EXPOSED_COPPER_TILES = register(AllBlocks.EXPOSED_COPPER_TILES);
    public static final BlockItem WEATHERED_COPPER_TILES = register(AllBlocks.WEATHERED_COPPER_TILES);
    public static final BlockItem OXIDIZED_COPPER_TILES = register(AllBlocks.OXIDIZED_COPPER_TILES);
    public static final BlockItem WAXED_COPPER_TILES = register(AllBlocks.WAXED_COPPER_TILES);
    public static final BlockItem WAXED_EXPOSED_COPPER_TILES = register(AllBlocks.WAXED_EXPOSED_COPPER_TILES);
    public static final BlockItem WAXED_WEATHERED_COPPER_TILES = register(AllBlocks.WAXED_WEATHERED_COPPER_TILES);
    public static final BlockItem WAXED_OXIDIZED_COPPER_TILES = register(AllBlocks.WAXED_OXIDIZED_COPPER_TILES);
    public static final BlockItem COPPER_TILE_SLAB = register(AllBlocks.COPPER_TILE_SLAB);
    public static final BlockItem EXPOSED_COPPER_TILE_SLAB = register(AllBlocks.EXPOSED_COPPER_TILE_SLAB);
    public static final BlockItem WEATHERED_COPPER_TILE_SLAB = register(AllBlocks.WEATHERED_COPPER_TILE_SLAB);
    public static final BlockItem OXIDIZED_COPPER_TILE_SLAB = register(AllBlocks.OXIDIZED_COPPER_TILE_SLAB);
    public static final BlockItem WAXED_COPPER_TILE_SLAB = register(AllBlocks.WAXED_COPPER_TILE_SLAB);
    public static final BlockItem WAXED_EXPOSED_COPPER_TILE_SLAB = register(AllBlocks.WAXED_EXPOSED_COPPER_TILE_SLAB);
    public static final BlockItem WAXED_WEATHERED_COPPER_TILE_SLAB = register(AllBlocks.WAXED_WEATHERED_COPPER_TILE_SLAB);
    public static final BlockItem WAXED_OXIDIZED_COPPER_TILE_SLAB = register(AllBlocks.WAXED_OXIDIZED_COPPER_TILE_SLAB);
    public static final BlockItem COPPER_TILE_STAIRS = register(AllBlocks.COPPER_TILE_STAIRS);
    public static final BlockItem EXPOSED_COPPER_TILE_STAIRS = register(AllBlocks.EXPOSED_COPPER_TILE_STAIRS);
    public static final BlockItem WEATHERED_COPPER_TILE_STAIRS = register(AllBlocks.WEATHERED_COPPER_TILE_STAIRS);
    public static final BlockItem OXIDIZED_COPPER_TILE_STAIRS = register(AllBlocks.OXIDIZED_COPPER_TILE_STAIRS);
    public static final BlockItem WAXED_COPPER_TILE_STAIRS = register(AllBlocks.WAXED_COPPER_TILE_STAIRS);
    public static final BlockItem WAXED_EXPOSED_COPPER_TILE_STAIRS = register(AllBlocks.WAXED_EXPOSED_COPPER_TILE_STAIRS);
    public static final BlockItem WAXED_WEATHERED_COPPER_TILE_STAIRS = register(AllBlocks.WAXED_WEATHERED_COPPER_TILE_STAIRS);
    public static final BlockItem WAXED_OXIDIZED_COPPER_TILE_STAIRS = register(AllBlocks.WAXED_OXIDIZED_COPPER_TILE_STAIRS);
    public static final BlockItem TILED_GLASS = register(AllBlocks.TILED_GLASS);
    public static final BlockItem FRAMED_GLASS = register(AllBlocks.FRAMED_GLASS);
    public static final BlockItem HORIZONTAL_FRAMED_GLASS = register(AllBlocks.HORIZONTAL_FRAMED_GLASS);
    public static final BlockItem VERTICAL_FRAMED_GLASS = register(AllBlocks.VERTICAL_FRAMED_GLASS);
    public static final BlockItem TILED_GLASS_PANE = register(AllBlocks.TILED_GLASS_PANE);
    public static final BlockItem FRAMED_GLASS_PANE = register(AllBlocks.FRAMED_GLASS_PANE);
    public static final BlockItem HORIZONTAL_FRAMED_GLASS_PANE = register(AllBlocks.HORIZONTAL_FRAMED_GLASS_PANE);
    public static final BlockItem VERTICAL_FRAMED_GLASS_PANE = register(AllBlocks.VERTICAL_FRAMED_GLASS_PANE);
    public static final BlockItem OAK_WINDOW = register(AllBlocks.OAK_WINDOW);
    public static final BlockItem SPRUCE_WINDOW = register(AllBlocks.SPRUCE_WINDOW);
    public static final BlockItem BIRCH_WINDOW = register(AllBlocks.BIRCH_WINDOW);
    public static final BlockItem JUNGLE_WINDOW = register(AllBlocks.JUNGLE_WINDOW);
    public static final BlockItem ACACIA_WINDOW = register(AllBlocks.ACACIA_WINDOW);
    public static final BlockItem DARK_OAK_WINDOW = register(AllBlocks.DARK_OAK_WINDOW);
    public static final BlockItem MANGROVE_WINDOW = register(AllBlocks.MANGROVE_WINDOW);
    public static final BlockItem CRIMSON_WINDOW = register(AllBlocks.CRIMSON_WINDOW);
    public static final BlockItem WARPED_WINDOW = register(AllBlocks.WARPED_WINDOW);
    public static final BlockItem CHERRY_WINDOW = register(AllBlocks.CHERRY_WINDOW);
    public static final BlockItem BAMBOO_WINDOW = register(AllBlocks.BAMBOO_WINDOW);
    public static final BlockItem OAK_WINDOW_PANE = register(AllBlocks.OAK_WINDOW_PANE);
    public static final BlockItem SPRUCE_WINDOW_PANE = register(AllBlocks.SPRUCE_WINDOW_PANE);
    public static final BlockItem BIRCH_WINDOW_PANE = register(AllBlocks.BIRCH_WINDOW_PANE);
    public static final BlockItem JUNGLE_WINDOW_PANE = register(AllBlocks.JUNGLE_WINDOW_PANE);
    public static final BlockItem ACACIA_WINDOW_PANE = register(AllBlocks.ACACIA_WINDOW_PANE);
    public static final BlockItem DARK_OAK_WINDOW_PANE = register(AllBlocks.DARK_OAK_WINDOW_PANE);
    public static final BlockItem MANGROVE_WINDOW_PANE = register(AllBlocks.MANGROVE_WINDOW_PANE);
    public static final BlockItem CRIMSON_WINDOW_PANE = register(AllBlocks.CRIMSON_WINDOW_PANE);
    public static final BlockItem WARPED_WINDOW_PANE = register(AllBlocks.WARPED_WINDOW_PANE);
    public static final BlockItem CHERRY_WINDOW_PANE = register(AllBlocks.CHERRY_WINDOW_PANE);
    public static final BlockItem BAMBOO_WINDOW_PANE = register(AllBlocks.BAMBOO_WINDOW_PANE);
    public static final BlockItem ORNATE_IRON_WINDOW_PANE = register(AllBlocks.ORNATE_IRON_WINDOW_PANE);
    public static final BlockItem CUT_GRANITE = register(AllBlocks.CUT_GRANITE);
    public static final BlockItem CUT_GRANITE_STAIRS = register(AllBlocks.CUT_GRANITE_STAIRS);
    public static final BlockItem CUT_GRANITE_SLAB = register(AllBlocks.CUT_GRANITE_SLAB);
    public static final BlockItem CUT_GRANITE_WALL = register(AllBlocks.CUT_GRANITE_WALL);
    public static final BlockItem POLISHED_CUT_GRANITE = register(AllBlocks.POLISHED_CUT_GRANITE);
    public static final BlockItem POLISHED_CUT_GRANITE_STAIRS = register(AllBlocks.POLISHED_CUT_GRANITE_STAIRS);
    public static final BlockItem POLISHED_CUT_GRANITE_SLAB = register(AllBlocks.POLISHED_CUT_GRANITE_SLAB);
    public static final BlockItem POLISHED_CUT_GRANITE_WALL = register(AllBlocks.POLISHED_CUT_GRANITE_WALL);
    public static final BlockItem CUT_GRANITE_BRICKS = register(AllBlocks.CUT_GRANITE_BRICKS);
    public static final BlockItem CUT_GRANITE_BRICK_STAIRS = register(AllBlocks.CUT_GRANITE_BRICK_STAIRS);
    public static final BlockItem CUT_GRANITE_BRICK_SLAB = register(AllBlocks.CUT_GRANITE_BRICK_SLAB);
    public static final BlockItem CUT_GRANITE_BRICK_WALL = register(AllBlocks.CUT_GRANITE_BRICK_WALL);
    public static final BlockItem SMALL_GRANITE_BRICKS = register(AllBlocks.SMALL_GRANITE_BRICKS);
    public static final BlockItem SMALL_GRANITE_BRICK_STAIRS = register(AllBlocks.SMALL_GRANITE_BRICK_STAIRS);
    public static final BlockItem SMALL_GRANITE_BRICK_SLAB = register(AllBlocks.SMALL_GRANITE_BRICK_SLAB);
    public static final BlockItem SMALL_GRANITE_BRICK_WALL = register(AllBlocks.SMALL_GRANITE_BRICK_WALL);
    public static final BlockItem LAYERED_GRANITE = register(AllBlocks.LAYERED_GRANITE);
    public static final BlockItem GRANITE_PILLAR = register(AllBlocks.GRANITE_PILLAR);
    public static final BlockItem CUT_DIORITE = register(AllBlocks.CUT_DIORITE);
    public static final BlockItem CUT_DIORITE_STAIRS = register(AllBlocks.CUT_DIORITE_STAIRS);
    public static final BlockItem CUT_DIORITE_SLAB = register(AllBlocks.CUT_DIORITE_SLAB);
    public static final BlockItem CUT_DIORITE_WALL = register(AllBlocks.CUT_DIORITE_WALL);
    public static final BlockItem POLISHED_CUT_DIORITE = register(AllBlocks.POLISHED_CUT_DIORITE);
    public static final BlockItem POLISHED_CUT_DIORITE_STAIRS = register(AllBlocks.POLISHED_CUT_DIORITE_STAIRS);
    public static final BlockItem POLISHED_CUT_DIORITE_SLAB = register(AllBlocks.POLISHED_CUT_DIORITE_SLAB);
    public static final BlockItem POLISHED_CUT_DIORITE_WALL = register(AllBlocks.POLISHED_CUT_DIORITE_WALL);
    public static final BlockItem CUT_DIORITE_BRICKS = register(AllBlocks.CUT_DIORITE_BRICKS);
    public static final BlockItem CUT_DIORITE_BRICK_STAIRS = register(AllBlocks.CUT_DIORITE_BRICK_STAIRS);
    public static final BlockItem CUT_DIORITE_BRICK_SLAB = register(AllBlocks.CUT_DIORITE_BRICK_SLAB);
    public static final BlockItem CUT_DIORITE_BRICK_WALL = register(AllBlocks.CUT_DIORITE_BRICK_WALL);
    public static final BlockItem SMALL_DIORITE_BRICKS = register(AllBlocks.SMALL_DIORITE_BRICKS);
    public static final BlockItem SMALL_DIORITE_BRICK_STAIRS = register(AllBlocks.SMALL_DIORITE_BRICK_STAIRS);
    public static final BlockItem SMALL_DIORITE_BRICK_SLAB = register(AllBlocks.SMALL_DIORITE_BRICK_SLAB);
    public static final BlockItem SMALL_DIORITE_BRICK_WALL = register(AllBlocks.SMALL_DIORITE_BRICK_WALL);
    public static final BlockItem LAYERED_DIORITE = register(AllBlocks.LAYERED_DIORITE);
    public static final BlockItem DIORITE_PILLAR = register(AllBlocks.DIORITE_PILLAR);
    public static final BlockItem CUT_ANDESITE = register(AllBlocks.CUT_ANDESITE);
    public static final BlockItem CUT_ANDESITE_STAIRS = register(AllBlocks.CUT_ANDESITE_STAIRS);
    public static final BlockItem CUT_ANDESITE_SLAB = register(AllBlocks.CUT_ANDESITE_SLAB);
    public static final BlockItem CUT_ANDESITE_WALL = register(AllBlocks.CUT_ANDESITE_WALL);
    public static final BlockItem POLISHED_CUT_ANDESITE = register(AllBlocks.POLISHED_CUT_ANDESITE);
    public static final BlockItem POLISHED_CUT_ANDESITE_STAIRS = register(AllBlocks.POLISHED_CUT_ANDESITE_STAIRS);
    public static final BlockItem POLISHED_CUT_ANDESITE_SLAB = register(AllBlocks.POLISHED_CUT_ANDESITE_SLAB);
    public static final BlockItem POLISHED_CUT_ANDESITE_WALL = register(AllBlocks.POLISHED_CUT_ANDESITE_WALL);
    public static final BlockItem CUT_ANDESITE_BRICKS = register(AllBlocks.CUT_ANDESITE_BRICKS);
    public static final BlockItem CUT_ANDESITE_BRICK_STAIRS = register(AllBlocks.CUT_ANDESITE_BRICK_STAIRS);
    public static final BlockItem CUT_ANDESITE_BRICK_SLAB = register(AllBlocks.CUT_ANDESITE_BRICK_SLAB);
    public static final BlockItem CUT_ANDESITE_BRICK_WALL = register(AllBlocks.CUT_ANDESITE_BRICK_WALL);
    public static final BlockItem SMALL_ANDESITE_BRICKS = register(AllBlocks.SMALL_ANDESITE_BRICKS);
    public static final BlockItem SMALL_ANDESITE_BRICK_STAIRS = register(AllBlocks.SMALL_ANDESITE_BRICK_STAIRS);
    public static final BlockItem SMALL_ANDESITE_BRICK_SLAB = register(AllBlocks.SMALL_ANDESITE_BRICK_SLAB);
    public static final BlockItem SMALL_ANDESITE_BRICK_WALL = register(AllBlocks.SMALL_ANDESITE_BRICK_WALL);
    public static final BlockItem LAYERED_ANDESITE = register(AllBlocks.LAYERED_ANDESITE);
    public static final BlockItem ANDESITE_PILLAR = register(AllBlocks.ANDESITE_PILLAR);
    public static final BlockItem CUT_CALCITE = register(AllBlocks.CUT_CALCITE);
    public static final BlockItem CUT_CALCITE_STAIRS = register(AllBlocks.CUT_CALCITE_STAIRS);
    public static final BlockItem CUT_CALCITE_SLAB = register(AllBlocks.CUT_CALCITE_SLAB);
    public static final BlockItem CUT_CALCITE_WALL = register(AllBlocks.CUT_CALCITE_WALL);
    public static final BlockItem POLISHED_CUT_CALCITE = register(AllBlocks.POLISHED_CUT_CALCITE);
    public static final BlockItem POLISHED_CUT_CALCITE_STAIRS = register(AllBlocks.POLISHED_CUT_CALCITE_STAIRS);
    public static final BlockItem POLISHED_CUT_CALCITE_SLAB = register(AllBlocks.POLISHED_CUT_CALCITE_SLAB);
    public static final BlockItem POLISHED_CUT_CALCITE_WALL = register(AllBlocks.POLISHED_CUT_CALCITE_WALL);
    public static final BlockItem CUT_CALCITE_BRICKS = register(AllBlocks.CUT_CALCITE_BRICKS);
    public static final BlockItem CUT_CALCITE_BRICK_STAIRS = register(AllBlocks.CUT_CALCITE_BRICK_STAIRS);
    public static final BlockItem CUT_CALCITE_BRICK_SLAB = register(AllBlocks.CUT_CALCITE_BRICK_SLAB);
    public static final BlockItem CUT_CALCITE_BRICK_WALL = register(AllBlocks.CUT_CALCITE_BRICK_WALL);
    public static final BlockItem SMALL_CALCITE_BRICKS = register(AllBlocks.SMALL_CALCITE_BRICKS);
    public static final BlockItem SMALL_CALCITE_BRICK_STAIRS = register(AllBlocks.SMALL_CALCITE_BRICK_STAIRS);
    public static final BlockItem SMALL_CALCITE_BRICK_SLAB = register(AllBlocks.SMALL_CALCITE_BRICK_SLAB);
    public static final BlockItem SMALL_CALCITE_BRICK_WALL = register(AllBlocks.SMALL_CALCITE_BRICK_WALL);
    public static final BlockItem LAYERED_CALCITE = register(AllBlocks.LAYERED_CALCITE);
    public static final BlockItem CALCITE_PILLAR = register(AllBlocks.CALCITE_PILLAR);
    public static final BlockItem CUT_DRIPSTONE = register(AllBlocks.CUT_DRIPSTONE);
    public static final BlockItem CUT_DRIPSTONE_STAIRS = register(AllBlocks.CUT_DRIPSTONE_STAIRS);
    public static final BlockItem CUT_DRIPSTONE_SLAB = register(AllBlocks.CUT_DRIPSTONE_SLAB);
    public static final BlockItem CUT_DRIPSTONE_WALL = register(AllBlocks.CUT_DRIPSTONE_WALL);
    public static final BlockItem POLISHED_CUT_DRIPSTONE = register(AllBlocks.POLISHED_CUT_DRIPSTONE);
    public static final BlockItem POLISHED_CUT_DRIPSTONE_STAIRS = register(AllBlocks.POLISHED_CUT_DRIPSTONE_STAIRS);
    public static final BlockItem POLISHED_CUT_DRIPSTONE_SLAB = register(AllBlocks.POLISHED_CUT_DRIPSTONE_SLAB);
    public static final BlockItem POLISHED_CUT_DRIPSTONE_WALL = register(AllBlocks.POLISHED_CUT_DRIPSTONE_WALL);
    public static final BlockItem CUT_DRIPSTONE_BRICKS = register(AllBlocks.CUT_DRIPSTONE_BRICKS);
    public static final BlockItem CUT_DRIPSTONE_BRICK_STAIRS = register(AllBlocks.CUT_DRIPSTONE_BRICK_STAIRS);
    public static final BlockItem CUT_DRIPSTONE_BRICK_SLAB = register(AllBlocks.CUT_DRIPSTONE_BRICK_SLAB);
    public static final BlockItem CUT_DRIPSTONE_BRICK_WALL = register(AllBlocks.CUT_DRIPSTONE_BRICK_WALL);
    public static final BlockItem SMALL_DRIPSTONE_BRICKS = register(AllBlocks.SMALL_DRIPSTONE_BRICKS);
    public static final BlockItem SMALL_DRIPSTONE_BRICK_STAIRS = register(AllBlocks.SMALL_DRIPSTONE_BRICK_STAIRS);
    public static final BlockItem SMALL_DRIPSTONE_BRICK_SLAB = register(AllBlocks.SMALL_DRIPSTONE_BRICK_SLAB);
    public static final BlockItem SMALL_DRIPSTONE_BRICK_WALL = register(AllBlocks.SMALL_DRIPSTONE_BRICK_WALL);
    public static final BlockItem LAYERED_DRIPSTONE = register(AllBlocks.LAYERED_DRIPSTONE);
    public static final BlockItem DRIPSTONE_PILLAR = register(AllBlocks.DRIPSTONE_PILLAR);
    public static final BlockItem CUT_DEEPSLATE = register(AllBlocks.CUT_DEEPSLATE);
    public static final BlockItem CUT_DEEPSLATE_STAIRS = register(AllBlocks.CUT_DEEPSLATE_STAIRS);
    public static final BlockItem CUT_DEEPSLATE_SLAB = register(AllBlocks.CUT_DEEPSLATE_SLAB);
    public static final BlockItem CUT_DEEPSLATE_WALL = register(AllBlocks.CUT_DEEPSLATE_WALL);
    public static final BlockItem POLISHED_CUT_DEEPSLATE = register(AllBlocks.POLISHED_CUT_DEEPSLATE);
    public static final BlockItem POLISHED_CUT_DEEPSLATE_STAIRS = register(AllBlocks.POLISHED_CUT_DEEPSLATE_STAIRS);
    public static final BlockItem POLISHED_CUT_DEEPSLATE_SLAB = register(AllBlocks.POLISHED_CUT_DEEPSLATE_SLAB);
    public static final BlockItem POLISHED_CUT_DEEPSLATE_WALL = register(AllBlocks.POLISHED_CUT_DEEPSLATE_WALL);
    public static final BlockItem CUT_DEEPSLATE_BRICKS = register(AllBlocks.CUT_DEEPSLATE_BRICKS);
    public static final BlockItem CUT_DEEPSLATE_BRICK_STAIRS = register(AllBlocks.CUT_DEEPSLATE_BRICK_STAIRS);
    public static final BlockItem CUT_DEEPSLATE_BRICK_SLAB = register(AllBlocks.CUT_DEEPSLATE_BRICK_SLAB);
    public static final BlockItem CUT_DEEPSLATE_BRICK_WALL = register(AllBlocks.CUT_DEEPSLATE_BRICK_WALL);
    public static final BlockItem SMALL_DEEPSLATE_BRICKS = register(AllBlocks.SMALL_DEEPSLATE_BRICKS);
    public static final BlockItem SMALL_DEEPSLATE_BRICK_STAIRS = register(AllBlocks.SMALL_DEEPSLATE_BRICK_STAIRS);
    public static final BlockItem SMALL_DEEPSLATE_BRICK_SLAB = register(AllBlocks.SMALL_DEEPSLATE_BRICK_SLAB);
    public static final BlockItem SMALL_DEEPSLATE_BRICK_WALL = register(AllBlocks.SMALL_DEEPSLATE_BRICK_WALL);
    public static final BlockItem LAYERED_DEEPSLATE = register(AllBlocks.LAYERED_DEEPSLATE);
    public static final BlockItem DEEPSLATE_PILLAR = register(AllBlocks.DEEPSLATE_PILLAR);
    public static final BlockItem CUT_TUFF = register(AllBlocks.CUT_TUFF);
    public static final BlockItem CUT_TUFF_STAIRS = register(AllBlocks.CUT_TUFF_STAIRS);
    public static final BlockItem CUT_TUFF_SLAB = register(AllBlocks.CUT_TUFF_SLAB);
    public static final BlockItem CUT_TUFF_WALL = register(AllBlocks.CUT_TUFF_WALL);
    public static final BlockItem POLISHED_CUT_TUFF = register(AllBlocks.POLISHED_CUT_TUFF);
    public static final BlockItem POLISHED_CUT_TUFF_STAIRS = register(AllBlocks.POLISHED_CUT_TUFF_STAIRS);
    public static final BlockItem POLISHED_CUT_TUFF_SLAB = register(AllBlocks.POLISHED_CUT_TUFF_SLAB);
    public static final BlockItem POLISHED_CUT_TUFF_WALL = register(AllBlocks.POLISHED_CUT_TUFF_WALL);
    public static final BlockItem CUT_TUFF_BRICKS = register(AllBlocks.CUT_TUFF_BRICKS);
    public static final BlockItem CUT_TUFF_BRICK_STAIRS = register(AllBlocks.CUT_TUFF_BRICK_STAIRS);
    public static final BlockItem CUT_TUFF_BRICK_SLAB = register(AllBlocks.CUT_TUFF_BRICK_SLAB);
    public static final BlockItem CUT_TUFF_BRICK_WALL = register(AllBlocks.CUT_TUFF_BRICK_WALL);
    public static final BlockItem SMALL_TUFF_BRICKS = register(AllBlocks.SMALL_TUFF_BRICKS);
    public static final BlockItem SMALL_TUFF_BRICK_STAIRS = register(AllBlocks.SMALL_TUFF_BRICK_STAIRS);
    public static final BlockItem SMALL_TUFF_BRICK_SLAB = register(AllBlocks.SMALL_TUFF_BRICK_SLAB);
    public static final BlockItem SMALL_TUFF_BRICK_WALL = register(AllBlocks.SMALL_TUFF_BRICK_WALL);
    public static final BlockItem LAYERED_TUFF = register(AllBlocks.LAYERED_TUFF);
    public static final BlockItem TUFF_PILLAR = register(AllBlocks.TUFF_PILLAR);
    public static final BlockItem ASURINE = register(AllBlocks.ASURINE);
    public static final BlockItem CUT_ASURINE = register(AllBlocks.CUT_ASURINE);
    public static final BlockItem CUT_ASURINE_STAIRS = register(AllBlocks.CUT_ASURINE_STAIRS);
    public static final BlockItem CUT_ASURINE_SLAB = register(AllBlocks.CUT_ASURINE_SLAB);
    public static final BlockItem CUT_ASURINE_WALL = register(AllBlocks.CUT_ASURINE_WALL);
    public static final BlockItem POLISHED_CUT_ASURINE = register(AllBlocks.POLISHED_CUT_ASURINE);
    public static final BlockItem POLISHED_CUT_ASURINE_STAIRS = register(AllBlocks.POLISHED_CUT_ASURINE_STAIRS);
    public static final BlockItem POLISHED_CUT_ASURINE_SLAB = register(AllBlocks.POLISHED_CUT_ASURINE_SLAB);
    public static final BlockItem POLISHED_CUT_ASURINE_WALL = register(AllBlocks.POLISHED_CUT_ASURINE_WALL);
    public static final BlockItem CUT_ASURINE_BRICKS = register(AllBlocks.CUT_ASURINE_BRICKS);
    public static final BlockItem CUT_ASURINE_BRICK_STAIRS = register(AllBlocks.CUT_ASURINE_BRICK_STAIRS);
    public static final BlockItem CUT_ASURINE_BRICK_SLAB = register(AllBlocks.CUT_ASURINE_BRICK_SLAB);
    public static final BlockItem CUT_ASURINE_BRICK_WALL = register(AllBlocks.CUT_ASURINE_BRICK_WALL);
    public static final BlockItem SMALL_ASURINE_BRICKS = register(AllBlocks.SMALL_ASURINE_BRICKS);
    public static final BlockItem SMALL_ASURINE_BRICK_STAIRS = register(AllBlocks.SMALL_ASURINE_BRICK_STAIRS);
    public static final BlockItem SMALL_ASURINE_BRICK_SLAB = register(AllBlocks.SMALL_ASURINE_BRICK_SLAB);
    public static final BlockItem SMALL_ASURINE_BRICK_WALL = register(AllBlocks.SMALL_ASURINE_BRICK_WALL);
    public static final BlockItem LAYERED_ASURINE = register(AllBlocks.LAYERED_ASURINE);
    public static final BlockItem ASURINE_PILLAR = register(AllBlocks.ASURINE_PILLAR);
    public static final BlockItem CRIMSITE = register(AllBlocks.CRIMSITE);
    public static final BlockItem CUT_CRIMSITE = register(AllBlocks.CUT_CRIMSITE);
    public static final BlockItem CUT_CRIMSITE_STAIRS = register(AllBlocks.CUT_CRIMSITE_STAIRS);
    public static final BlockItem CUT_CRIMSITE_SLAB = register(AllBlocks.CUT_CRIMSITE_SLAB);
    public static final BlockItem CUT_CRIMSITE_WALL = register(AllBlocks.CUT_CRIMSITE_WALL);
    public static final BlockItem POLISHED_CUT_CRIMSITE = register(AllBlocks.POLISHED_CUT_CRIMSITE);
    public static final BlockItem POLISHED_CUT_CRIMSITE_STAIRS = register(AllBlocks.POLISHED_CUT_CRIMSITE_STAIRS);
    public static final BlockItem POLISHED_CUT_CRIMSITE_SLAB = register(AllBlocks.POLISHED_CUT_CRIMSITE_SLAB);
    public static final BlockItem POLISHED_CUT_CRIMSITE_WALL = register(AllBlocks.POLISHED_CUT_CRIMSITE_WALL);
    public static final BlockItem CUT_CRIMSITE_BRICKS = register(AllBlocks.CUT_CRIMSITE_BRICKS);
    public static final BlockItem CUT_CRIMSITE_BRICK_STAIRS = register(AllBlocks.CUT_CRIMSITE_BRICK_STAIRS);
    public static final BlockItem CUT_CRIMSITE_BRICK_SLAB = register(AllBlocks.CUT_CRIMSITE_BRICK_SLAB);
    public static final BlockItem CUT_CRIMSITE_BRICK_WALL = register(AllBlocks.CUT_CRIMSITE_BRICK_WALL);
    public static final BlockItem SMALL_CRIMSITE_BRICKS = register(AllBlocks.SMALL_CRIMSITE_BRICKS);
    public static final BlockItem SMALL_CRIMSITE_BRICK_STAIRS = register(AllBlocks.SMALL_CRIMSITE_BRICK_STAIRS);
    public static final BlockItem SMALL_CRIMSITE_BRICK_SLAB = register(AllBlocks.SMALL_CRIMSITE_BRICK_SLAB);
    public static final BlockItem SMALL_CRIMSITE_BRICK_WALL = register(AllBlocks.SMALL_CRIMSITE_BRICK_WALL);
    public static final BlockItem LAYERED_CRIMSITE = register(AllBlocks.LAYERED_CRIMSITE);
    public static final BlockItem CRIMSITE_PILLAR = register(AllBlocks.CRIMSITE_PILLAR);
    public static final BlockItem LIMESTONE = register(AllBlocks.LIMESTONE);
    public static final BlockItem CUT_LIMESTONE = register(AllBlocks.CUT_LIMESTONE);
    public static final BlockItem CUT_LIMESTONE_STAIRS = register(AllBlocks.CUT_LIMESTONE_STAIRS);
    public static final BlockItem CUT_LIMESTONE_SLAB = register(AllBlocks.CUT_LIMESTONE_SLAB);
    public static final BlockItem CUT_LIMESTONE_WALL = register(AllBlocks.CUT_LIMESTONE_WALL);
    public static final BlockItem POLISHED_CUT_LIMESTONE = register(AllBlocks.POLISHED_CUT_LIMESTONE);
    public static final BlockItem POLISHED_CUT_LIMESTONE_STAIRS = register(AllBlocks.POLISHED_CUT_LIMESTONE_STAIRS);
    public static final BlockItem POLISHED_CUT_LIMESTONE_SLAB = register(AllBlocks.POLISHED_CUT_LIMESTONE_SLAB);
    public static final BlockItem POLISHED_CUT_LIMESTONE_WALL = register(AllBlocks.POLISHED_CUT_LIMESTONE_WALL);
    public static final BlockItem CUT_LIMESTONE_BRICKS = register(AllBlocks.CUT_LIMESTONE_BRICKS);
    public static final BlockItem CUT_LIMESTONE_BRICK_STAIRS = register(AllBlocks.CUT_LIMESTONE_BRICK_STAIRS);
    public static final BlockItem CUT_LIMESTONE_BRICK_SLAB = register(AllBlocks.CUT_LIMESTONE_BRICK_SLAB);
    public static final BlockItem CUT_LIMESTONE_BRICK_WALL = register(AllBlocks.CUT_LIMESTONE_BRICK_WALL);
    public static final BlockItem SMALL_LIMESTONE_BRICKS = register(AllBlocks.SMALL_LIMESTONE_BRICKS);
    public static final BlockItem SMALL_LIMESTONE_BRICK_STAIRS = register(AllBlocks.SMALL_LIMESTONE_BRICK_STAIRS);
    public static final BlockItem SMALL_LIMESTONE_BRICK_SLAB = register(AllBlocks.SMALL_LIMESTONE_BRICK_SLAB);
    public static final BlockItem SMALL_LIMESTONE_BRICK_WALL = register(AllBlocks.SMALL_LIMESTONE_BRICK_WALL);
    public static final BlockItem LAYERED_LIMESTONE = register(AllBlocks.LAYERED_LIMESTONE);
    public static final BlockItem LIMESTONE_PILLAR = register(AllBlocks.LIMESTONE_PILLAR);
    public static final BlockItem OCHRUM = register(AllBlocks.OCHRUM);
    public static final BlockItem CUT_OCHRUM = register(AllBlocks.CUT_OCHRUM);
    public static final BlockItem CUT_OCHRUM_STAIRS = register(AllBlocks.CUT_OCHRUM_STAIRS);
    public static final BlockItem CUT_OCHRUM_SLAB = register(AllBlocks.CUT_OCHRUM_SLAB);
    public static final BlockItem CUT_OCHRUM_WALL = register(AllBlocks.CUT_OCHRUM_WALL);
    public static final BlockItem POLISHED_CUT_OCHRUM = register(AllBlocks.POLISHED_CUT_OCHRUM);
    public static final BlockItem POLISHED_CUT_OCHRUM_STAIRS = register(AllBlocks.POLISHED_CUT_OCHRUM_STAIRS);
    public static final BlockItem POLISHED_CUT_OCHRUM_SLAB = register(AllBlocks.POLISHED_CUT_OCHRUM_SLAB);
    public static final BlockItem POLISHED_CUT_OCHRUM_WALL = register(AllBlocks.POLISHED_CUT_OCHRUM_WALL);
    public static final BlockItem CUT_OCHRUM_BRICKS = register(AllBlocks.CUT_OCHRUM_BRICKS);
    public static final BlockItem CUT_OCHRUM_BRICK_STAIRS = register(AllBlocks.CUT_OCHRUM_BRICK_STAIRS);
    public static final BlockItem CUT_OCHRUM_BRICK_SLAB = register(AllBlocks.CUT_OCHRUM_BRICK_SLAB);
    public static final BlockItem CUT_OCHRUM_BRICK_WALL = register(AllBlocks.CUT_OCHRUM_BRICK_WALL);
    public static final BlockItem SMALL_OCHRUM_BRICKS = register(AllBlocks.SMALL_OCHRUM_BRICKS);
    public static final BlockItem SMALL_OCHRUM_BRICK_STAIRS = register(AllBlocks.SMALL_OCHRUM_BRICK_STAIRS);
    public static final BlockItem SMALL_OCHRUM_BRICK_SLAB = register(AllBlocks.SMALL_OCHRUM_BRICK_SLAB);
    public static final BlockItem SMALL_OCHRUM_BRICK_WALL = register(AllBlocks.SMALL_OCHRUM_BRICK_WALL);
    public static final BlockItem LAYERED_OCHRUM = register(AllBlocks.LAYERED_OCHRUM);
    public static final BlockItem OCHRUM_PILLAR = register(AllBlocks.OCHRUM_PILLAR);
    public static final BlockItem SCORIA = register(AllBlocks.SCORIA);
    public static final BlockItem CUT_SCORIA = register(AllBlocks.CUT_SCORIA);
    public static final BlockItem CUT_SCORIA_STAIRS = register(AllBlocks.CUT_SCORIA_STAIRS);
    public static final BlockItem CUT_SCORIA_SLAB = register(AllBlocks.CUT_SCORIA_SLAB);
    public static final BlockItem CUT_SCORIA_WALL = register(AllBlocks.CUT_SCORIA_WALL);
    public static final BlockItem POLISHED_CUT_SCORIA = register(AllBlocks.POLISHED_CUT_SCORIA);
    public static final BlockItem POLISHED_CUT_SCORIA_STAIRS = register(AllBlocks.POLISHED_CUT_SCORIA_STAIRS);
    public static final BlockItem POLISHED_CUT_SCORIA_SLAB = register(AllBlocks.POLISHED_CUT_SCORIA_SLAB);
    public static final BlockItem POLISHED_CUT_SCORIA_WALL = register(AllBlocks.POLISHED_CUT_SCORIA_WALL);
    public static final BlockItem CUT_SCORIA_BRICKS = register(AllBlocks.CUT_SCORIA_BRICKS);
    public static final BlockItem CUT_SCORIA_BRICK_STAIRS = register(AllBlocks.CUT_SCORIA_BRICK_STAIRS);
    public static final BlockItem CUT_SCORIA_BRICK_SLAB = register(AllBlocks.CUT_SCORIA_BRICK_SLAB);
    public static final BlockItem CUT_SCORIA_BRICK_WALL = register(AllBlocks.CUT_SCORIA_BRICK_WALL);
    public static final BlockItem SMALL_SCORIA_BRICKS = register(AllBlocks.SMALL_SCORIA_BRICKS);
    public static final BlockItem SMALL_SCORIA_BRICK_STAIRS = register(AllBlocks.SMALL_SCORIA_BRICK_STAIRS);
    public static final BlockItem SMALL_SCORIA_BRICK_SLAB = register(AllBlocks.SMALL_SCORIA_BRICK_SLAB);
    public static final BlockItem SMALL_SCORIA_BRICK_WALL = register(AllBlocks.SMALL_SCORIA_BRICK_WALL);
    public static final BlockItem LAYERED_SCORIA = register(AllBlocks.LAYERED_SCORIA);
    public static final BlockItem SCORIA_PILLAR = register(AllBlocks.SCORIA_PILLAR);
    public static final BlockItem SCORCHIA = register(AllBlocks.SCORCHIA);
    public static final BlockItem CUT_SCORCHIA = register(AllBlocks.CUT_SCORCHIA);
    public static final BlockItem CUT_SCORCHIA_STAIRS = register(AllBlocks.CUT_SCORCHIA_STAIRS);
    public static final BlockItem CUT_SCORCHIA_SLAB = register(AllBlocks.CUT_SCORCHIA_SLAB);
    public static final BlockItem CUT_SCORCHIA_WALL = register(AllBlocks.CUT_SCORCHIA_WALL);
    public static final BlockItem POLISHED_CUT_SCORCHIA = register(AllBlocks.POLISHED_CUT_SCORCHIA);
    public static final BlockItem POLISHED_CUT_SCORCHIA_STAIRS = register(AllBlocks.POLISHED_CUT_SCORCHIA_STAIRS);
    public static final BlockItem POLISHED_CUT_SCORCHIA_SLAB = register(AllBlocks.POLISHED_CUT_SCORCHIA_SLAB);
    public static final BlockItem POLISHED_CUT_SCORCHIA_WALL = register(AllBlocks.POLISHED_CUT_SCORCHIA_WALL);
    public static final BlockItem CUT_SCORCHIA_BRICKS = register(AllBlocks.CUT_SCORCHIA_BRICKS);
    public static final BlockItem CUT_SCORCHIA_BRICK_STAIRS = register(AllBlocks.CUT_SCORCHIA_BRICK_STAIRS);
    public static final BlockItem CUT_SCORCHIA_BRICK_SLAB = register(AllBlocks.CUT_SCORCHIA_BRICK_SLAB);
    public static final BlockItem CUT_SCORCHIA_BRICK_WALL = register(AllBlocks.CUT_SCORCHIA_BRICK_WALL);
    public static final BlockItem SMALL_SCORCHIA_BRICKS = register(AllBlocks.SMALL_SCORCHIA_BRICKS);
    public static final BlockItem SMALL_SCORCHIA_BRICK_STAIRS = register(AllBlocks.SMALL_SCORCHIA_BRICK_STAIRS);
    public static final BlockItem SMALL_SCORCHIA_BRICK_SLAB = register(AllBlocks.SMALL_SCORCHIA_BRICK_SLAB);
    public static final BlockItem SMALL_SCORCHIA_BRICK_WALL = register(AllBlocks.SMALL_SCORCHIA_BRICK_WALL);
    public static final BlockItem LAYERED_SCORCHIA = register(AllBlocks.LAYERED_SCORCHIA);
    public static final BlockItem SCORCHIA_PILLAR = register(AllBlocks.SCORCHIA_PILLAR);
    public static final BlockItem VERIDIUM = register(AllBlocks.VERIDIUM);
    public static final BlockItem CUT_VERIDIUM = register(AllBlocks.CUT_VERIDIUM);
    public static final BlockItem CUT_VERIDIUM_STAIRS = register(AllBlocks.CUT_VERIDIUM_STAIRS);
    public static final BlockItem CUT_VERIDIUM_SLAB = register(AllBlocks.CUT_VERIDIUM_SLAB);
    public static final BlockItem CUT_VERIDIUM_WALL = register(AllBlocks.CUT_VERIDIUM_WALL);
    public static final BlockItem POLISHED_CUT_VERIDIUM = register(AllBlocks.POLISHED_CUT_VERIDIUM);
    public static final BlockItem POLISHED_CUT_VERIDIUM_STAIRS = register(AllBlocks.POLISHED_CUT_VERIDIUM_STAIRS);
    public static final BlockItem POLISHED_CUT_VERIDIUM_SLAB = register(AllBlocks.POLISHED_CUT_VERIDIUM_SLAB);
    public static final BlockItem POLISHED_CUT_VERIDIUM_WALL = register(AllBlocks.POLISHED_CUT_VERIDIUM_WALL);
    public static final BlockItem CUT_VERIDIUM_BRICKS = register(AllBlocks.CUT_VERIDIUM_BRICKS);
    public static final BlockItem CUT_VERIDIUM_BRICK_STAIRS = register(AllBlocks.CUT_VERIDIUM_BRICK_STAIRS);
    public static final BlockItem CUT_VERIDIUM_BRICK_SLAB = register(AllBlocks.CUT_VERIDIUM_BRICK_SLAB);
    public static final BlockItem CUT_VERIDIUM_BRICK_WALL = register(AllBlocks.CUT_VERIDIUM_BRICK_WALL);
    public static final BlockItem SMALL_VERIDIUM_BRICKS = register(AllBlocks.SMALL_VERIDIUM_BRICKS);
    public static final BlockItem SMALL_VERIDIUM_BRICK_STAIRS = register(AllBlocks.SMALL_VERIDIUM_BRICK_STAIRS);
    public static final BlockItem SMALL_VERIDIUM_BRICK_SLAB = register(AllBlocks.SMALL_VERIDIUM_BRICK_SLAB);
    public static final BlockItem SMALL_VERIDIUM_BRICK_WALL = register(AllBlocks.SMALL_VERIDIUM_BRICK_WALL);
    public static final BlockItem LAYERED_VERIDIUM = register(AllBlocks.LAYERED_VERIDIUM);
    public static final BlockItem VERIDIUM_PILLAR = register(AllBlocks.VERIDIUM_PILLAR);
    public static final BlockItem COPYCAT_STEP = register(AllBlocks.COPYCAT_STEP);
    public static final BlockItem COPYCAT_PANEL = register(AllBlocks.COPYCAT_PANEL);

    public static final BucketItem HONEY_BUCKET = register(AllFluids.HONEY, BucketItem::new);
    public static final BucketItem CHOCOLATE_BUCKET = register(AllFluids.CHOCOLATE, BucketItem::new);

    public static final VerticalGearboxItem VERTICAL_GEARBOX = register("vertical_gearbox", VerticalGearboxItem::new);
    public static final GogglesItem GOGGLES = register(
        "goggles",
        GogglesItem::new,
        new Item.Settings().maxCount(1).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.HEAD).build())
    );
    public static final Item BRASS_HAND = register("brass_hand");
    public static final Item ANDESITE_ALLOY = register("andesite_alloy");
    public static final WrenchItem WRENCH = register("wrench", WrenchItem::new, new Item.Settings().maxCount(1));
    public static final BeltConnectorItem BELT_CONNECTOR = register("belt_connector", BeltConnectorItem::new);
    public static final SuperGlueItem SUPER_GLUE = register("super_glue", SuperGlueItem::new, new Item.Settings().maxDamage(99));
    public static final BlazeBurnerBlockItem EMPTY_BLAZE_BURNER = register("empty_blaze_burner", BlazeBurnerBlockItem::empty);
    public static final BuildersTeaItem BUILDERS_TEA = register(
        "builders_tea", BuildersTeaItem::new, new Item.Settings().maxCount(16).food(
            new FoodComponent.Builder().nutrition(1).saturationModifier(.6F).alwaysEdible().build(),
            ConsumableComponent.builder().consumeSeconds(2.1F).useAction(UseAction.DRINK).sound(SoundEvents.ENTITY_GENERIC_DRINK)
                .consumeParticles(false)
                .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.HASTE, 3 * 60 * 20, 0, false, false))).build()
        )
    );
    public static final Item BLAZE_CAKE_BASE = register("blaze_cake_base");
    public static final Item BLAZE_CAKE = register("blaze_cake");
    public static final Item CREATIVE_BLAZE_CAKE = register("creative_blaze_cake", new Item.Settings().rarity(Rarity.EPIC));
    public static final Item COPPER_SHEET = register("copper_sheet");
    public static final Item BRASS_SHEET = register("brass_sheet");
    public static final Item IRON_SHEET = register("iron_sheet");
    public static final Item GOLDEN_SHEET = register("golden_sheet");
    public static final Item PULP = register("pulp");
    public static final Item CARDBOARD = register("cardboard");
    public static final Item BRASS_INGOT = register("brass_ingot");
    public static final Item WHEAT_FLOUR = register("wheat_flour");
    public static final Item DOUGH = register("dough");
    public static final Item PROPELLER = register("propeller");
    public static final Item COPPER_NUGGET = register("copper_nugget");
    public static final Item ZINC_NUGGET = register("zinc_nugget");
    public static final Item BRASS_NUGGET = register("brass_nugget");
    public static final Item CRUSHED_IRON = register("crushed_raw_iron");
    public static final Item CRUSHED_GOLD = register("crushed_raw_gold");
    public static final Item CRUSHED_COPPER = register("crushed_raw_copper");
    public static final Item CRUSHED_ZINC = register("crushed_raw_zinc");
    public static final ListFilterItem FILTER = register("filter", FilterItem::regular);
    public static final AttributeFilterItem ATTRIBUTE_FILTER = register("attribute_filter", FilterItem::attribute);
    public static final PackageFilterItem PACKAGE_FILTER = register("package_filter", FilterItem::address);
    public static final MinecartCouplingItem MINECART_COUPLING = register("minecart_coupling", MinecartCouplingItem::new);
    public static final MinecartContraptionItem MINECART_CONTRAPTION = register("minecart_contraption", MinecartContraptionItem::rideable);
    public static final MinecartContraptionItem FURNACE_MINECART_CONTRAPTION = register(
        "furnace_minecart_contraption",
        MinecartContraptionItem::furnace
    );
    public static final MinecartContraptionItem CHEST_MINECART_CONTRAPTION = register("chest_minecart_contraption", MinecartContraptionItem::chest);
    public static final Item CINDER_FLOUR = register("cinder_flour");
    public static final Item BAR_OF_CHOCOLATE = register(
        "bar_of_chocolate",
        new Item.Settings().food(new FoodComponent.Builder().nutrition(6).saturationModifier(0.3F).build())
    );
    public static final Item ZINC_INGOT = register("zinc_ingot");
    public static final Item STURDY_SHEET = register("sturdy_sheet");
    public static final Item CHOCOLATE_BERRIES = register(
        "chocolate_glazed_berries",
        new Item.Settings().food(new FoodComponent.Builder().nutrition(7).saturationModifier(0.8F).build())
    );
    public static final Item HONEYED_APPLE = register(
        "honeyed_apple",
        new Item.Settings().food(new FoodComponent.Builder().nutrition(8).saturationModifier(0.8F).build())
    );
    public static final Item SWEET_ROLL = register(
        "sweet_roll",
        new Item.Settings().food(new FoodComponent.Builder().nutrition(6).saturationModifier(0.8F).build())
    );
    public static final DivingHelmetItem COPPER_DIVING_HELMET = register(
        "copper_diving_helmet",
        DivingHelmetItem::new,
        new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(AllArmorMaterials.COPPER.durability()))
            .attributeModifiers(DivingHelmetItem.createAttributeModifiers(AllArmorMaterials.COPPER))
            .enchantable(AllArmorMaterials.COPPER.enchantmentValue()).component(
                DataComponentTypes.EQUIPPABLE,
                EquippableComponent.builder(EquipmentSlot.HEAD).equipSound(AllArmorMaterials.COPPER.equipSound())
                    .model(AllArmorMaterials.COPPER.assetId()).build()
            ).repairable(AllArmorMaterials.COPPER.repairIngredient())
    );
    public static final BacktankItem COPPER_BACKTANK = register(
        "copper_backtank",
        BacktankItem::copper,
        AllArmorMaterials.chest(AllArmorMaterials.COPPER)
    );
    public static final DivingBootsItem COPPER_DIVING_BOOTS = register(
        "copper_diving_boots",
        DivingBootsItem::new,
        new Item.Settings().armor(AllArmorMaterials.COPPER, EquipmentType.BOOTS)
    );
    public static final DivingHelmetItem NETHERITE_DIVING_HELMET = register(
        "netherite_diving_helmet",
        DivingHelmetItem::new,
        new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(AllArmorMaterials.NETHERITE.durability()))
            .attributeModifiers(DivingHelmetItem.createAttributeModifiers(AllArmorMaterials.NETHERITE))
            .enchantable(AllArmorMaterials.NETHERITE.enchantmentValue()).component(
                DataComponentTypes.EQUIPPABLE,
                EquippableComponent.builder(EquipmentSlot.HEAD).equipSound(AllArmorMaterials.NETHERITE.equipSound())
                    .model(AllArmorMaterials.NETHERITE.assetId()).build()
            ).repairable(AllArmorMaterials.NETHERITE.repairIngredient()).fireproof()
    );
    public static final BacktankItem NETHERITE_BACKTANK = register(
        "netherite_backtank",
        BacktankItem::netherite,
        AllArmorMaterials.chest(AllArmorMaterials.NETHERITE).fireproof()
    );
    public static final DivingBootsItem NETHERITE_DIVING_BOOTS = register(
        "netherite_diving_boots",
        DivingBootsItem::new,
        new Item.Settings().armor(AllArmorMaterials.NETHERITE, EquipmentType.BOOTS).fireproof()
    );
    public static final Item ROSE_QUARTZ = register("rose_quartz");
    public static final Item POLISHED_ROSE_QUARTZ = register("polished_rose_quartz");
    public static final SandPaperItem SAND_PAPER = register("sand_paper", SandPaperItem::new, new Item.Settings().maxDamage(8).enchantable(1));
    public static final SandPaperItem RED_SAND_PAPER = register(
        "red_sand_paper",
        SandPaperItem::new,
        new Item.Settings().maxDamage(8).enchantable(1)
    );
    public static final Item PRECISION_MECHANISM = register("precision_mechanism");
    public static final Item POWDERED_OBSIDIAN = register("powdered_obsidian");
    public static final Item WHISK = register("whisk");
    public static final Item CRAFTER_SLOT_COVER = register("crafter_slot_cover");
    public static final Item ELECTRON_TUBE = register("electron_tube");
    public static final Item TRANSMITTER = register("transmitter");
    public static final Item RAW_ZINC = register("raw_zinc");
    public static final SequencedAssemblyItem INCOMPLETE_PRECISION_MECHANISM = register(
        "incomplete_precision_mechanism",
        SequencedAssemblyItem::new,
        new Item.Settings().maxCount(1)
    );
    public static final SequencedAssemblyItem INCOMPLETE_REINFORCED_SHEET = register(
        "unprocessed_obsidian_sheet",
        SequencedAssemblyItem::new,
        new Item.Settings().maxCount(1)
    );
    public static final SequencedAssemblyItem INCOMPLETE_TRACK = register(
        "incomplete_track",
        SequencedAssemblyItem::new,
        new Item.Settings().maxCount(1)
    );
    public static final ExperienceNuggetItem EXP_NUGGET = register(
        "experience_nugget",
        ExperienceNuggetItem::new,
        new Item.Settings().rarity(Rarity.UNCOMMON)
    );
    public static final ScheduleItem SCHEDULE = register("schedule", ScheduleItem::new);
    public static final PotatoCannonItem POTATO_CANNON = register("potato_cannon", PotatoCannonItem::new, new Item.Settings().maxDamage(100));
    public static final ExtendoGripItem EXTENDO_GRIP = register(
        "extendo_grip",
        ExtendoGripItem::new,
        new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(200).attributeModifiers(ExtendoGripItem.rangeModifier)
    );
    public static final LinkedControllerItem LINKED_CONTROLLER = register(
        "linked_controller",
        LinkedControllerItem::new,
        new Item.Settings().maxCount(1)
    );
    public static final PackageItem CARDBOARD_PACKAGE_12X12 = register(
        "cardboard_package_12x12",
        PackageItem.styled(AllPackageStyles.CARDBOARD_12X12),
        new Item.Settings().maxCount(1).translationKey("item.create.package")
    );
    public static final PackageItem CARDBOARD_PACKAGE_10X12 = register(
        "cardboard_package_10x12",
        PackageItem.styled(AllPackageStyles.CARDBOARD_10X12),
        new Item.Settings().maxCount(1).translationKey("item.create.package")
    );
    public static final PackageItem CARDBOARD_PACKAGE_10X8 = register(
        "cardboard_package_10x8",
        PackageItem.styled(AllPackageStyles.CARDBOARD_10X8),
        new Item.Settings().maxCount(1).translationKey("item.create.package")
    );
    public static final PackageItem CARDBOARD_PACKAGE_12X10 = register(
        "cardboard_package_12x10",
        PackageItem.styled(AllPackageStyles.CARDBOARD_12X10),
        new Item.Settings().maxCount(1).translationKey("item.create.package")
    );
    public static final PackageItem RARE_CREEPER_PACKAGE = register(
        "rare_creeper_package",
        PackageItem.styled(AllPackageStyles.RARE_CREEPER),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_DARCY_PACKAGE = register(
        "rare_darcy_package",
        PackageItem.styled(AllPackageStyles.RARE_DARCY),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_EVAN_PACKAGE = register(
        "rare_evan_package",
        PackageItem.styled(AllPackageStyles.RARE_EVAN),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_JINX_PACKAGE = register(
        "rare_jinx_package",
        PackageItem.styled(AllPackageStyles.RARE_JINX),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_KRYPPERS_PACKAGE = register(
        "rare_kryppers_package",
        PackageItem.styled(AllPackageStyles.RARE_KRYPPERS),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_SIMI_PACKAGE = register(
        "rare_simi_package",
        PackageItem.styled(AllPackageStyles.RARE_SIMI),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_STARLOTTE_PACKAGE = register(
        "rare_starlotte_package",
        PackageItem.styled(AllPackageStyles.RARE_STARLOTTE),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_THUNDER_PACKAGE = register(
        "rare_thunder_package",
        PackageItem.styled(AllPackageStyles.RARE_THUNDER),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_UP_PACKAGE = register(
        "rare_up_package",
        PackageItem.styled(AllPackageStyles.RARE_UP),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final PackageItem RARE_VECTOR_PACKAGE = register(
        "rare_vector_package",
        PackageItem.styled(AllPackageStyles.RARE_VECTOR),
        new Item.Settings().maxCount(1).translationKey("item.create.rare_package")
    );
    public static final ShoppingListItem SHOPPING_LIST = register("shopping_list", ShoppingListItem::new, new Item.Settings().maxCount(1));
    public static final CardboardArmorItem CARDBOARD_HELMET = register(
        "cardboard_helmet",
        CardboardArmorItem::new,
        new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(AllArmorMaterials.CARDBOARD.durability()))
            .attributeModifiers(AllArmorMaterials.CARDBOARD.createAttributeModifiers(EquipmentType.HELMET))
            .enchantable(AllArmorMaterials.CARDBOARD.enchantmentValue()).component(
                DataComponentTypes.EQUIPPABLE,
                EquippableComponent.builder(EquipmentType.HELMET.getEquipmentSlot()).equipSound(AllArmorMaterials.CARDBOARD.equipSound())
                    .model(AllArmorMaterials.CARDBOARD.assetId()).cameraOverlay(Identifier.of(MOD_ID, "misc/package_blur")).build()
            ).repairable(AllArmorMaterials.CARDBOARD.repairIngredient())
    );
    public static final CardboardArmorItem CARDBOARD_CHESTPLATE = register(
        "cardboard_chestplate",
        CardboardArmorItem::new,
        new Item.Settings().armor(AllArmorMaterials.CARDBOARD, EquipmentType.CHESTPLATE)
    );
    public static final CardboardArmorItem CARDBOARD_LEGGINGS = register(
        "cardboard_leggings",
        CardboardArmorItem::new,
        new Item.Settings().armor(AllArmorMaterials.CARDBOARD, EquipmentType.LEGGINGS)
    );
    public static final CardboardArmorItem CARDBOARD_BOOTS = register(
        "cardboard_boots",
        CardboardArmorItem::new,
        new Item.Settings().armor(AllArmorMaterials.CARDBOARD, EquipmentType.BOOTS)
    );
    @SuppressWarnings("deprecation")
    public static final CardboardSwordItem CARDBOARD_SWORD = register(
        "cardboard_sword",
        CardboardSwordItem::new,
        new Item.Settings().maxDamage(AllToolMaterials.CARDBOARD.durability()).repairable(AllToolMaterials.CARDBOARD.repairItems())
            .enchantable(AllToolMaterials.CARDBOARD.enchantmentValue()).component(
                DataComponentTypes.TOOL, new ToolComponent(
                    List.of(
                        ToolComponent.Rule.ofAlwaysDropping(RegistryEntryList.of(Blocks.COBWEB.getRegistryEntry()), 15.0F),
                        ToolComponent.Rule.of(
                            Registries.createEntryLookup(Registries.BLOCK).getOrThrow(BlockTags.SWORD_INSTANTLY_MINES),
                            Float.MAX_VALUE
                        ),
                        ToolComponent.Rule.of(Registries.createEntryLookup(Registries.BLOCK).getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
                    ), 1.0F, 2, false
                )
            ).attributeModifiers(AttributeModifiersComponent.builder().add(
                EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(
                    Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                    3.0F + AllToolMaterials.CARDBOARD.attackDamageBonus(),
                    EntityAttributeModifier.Operation.ADD_VALUE
                ), AttributeModifierSlot.MAINHAND
            ).add(
                EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, 1.0F, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND
            ).add(EntityAttributes.ATTACK_KNOCKBACK, CardboardSwordItem.KNOCKBACK_MODIFIER, AttributeModifierSlot.MAINHAND).build())
            .component(DataComponentTypes.WEAPON, new WeaponComponent(1))
    );
    public static final BlueprintItem CRAFTING_BLUEPRINT = register("crafting_blueprint", BlueprintItem::new);
    public static final TreeFertilizerItem TREE_FERTILIZER = register("tree_fertilizer", TreeFertilizerItem::new);
    public static final SymmetryWandItem WAND_OF_SYMMETRY = register(
        "wand_of_symmetry",
        SymmetryWandItem::new,
        new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)
    );
    public static final Item EMPTY_SCHEMATIC = register("empty_schematic", new Item.Settings().maxCount(1));
    public static final SchematicAndQuillItem SCHEMATIC_AND_QUILL = register(
        "schematic_and_quill",
        SchematicAndQuillItem::new,
        new Item.Settings().maxCount(1)
    );
    public static final SchematicItem SCHEMATIC = register("schematic", SchematicItem::new, new Item.Settings().maxCount(1));
    public static final WorldshaperItem WORLDSHAPER = register(
        "handheld_worldshaper",
        WorldshaperItem::new,
        new Item.Settings().maxCount(1).rarity(Rarity.EPIC)
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_OSMIUM = register("crushed_raw_osmium", TagDependentIngredientItem.tag("ores/osmium"));
    public static final TagDependentIngredientItem CRUSHED_RAW_PLATINUM = register(
        "crushed_raw_platinum",
        TagDependentIngredientItem.tag("ores/platinum")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_SILVER = register("crushed_raw_silver", TagDependentIngredientItem.tag("ores/silver"));
    public static final TagDependentIngredientItem CRUSHED_RAW_TIN = register("crushed_raw_tin", TagDependentIngredientItem.tag("ores/tin"));
    public static final TagDependentIngredientItem CRUSHED_RAW_LEAD = register("crushed_raw_lead", TagDependentIngredientItem.tag("ores/lead"));
    public static final TagDependentIngredientItem CRUSHED_RAW_QUICKSILVER = register(
        "crushed_raw_quicksilver",
        TagDependentIngredientItem.tag("ores/quicksilver")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_ALUMINUM = register(
        "crushed_raw_aluminum",
        TagDependentIngredientItem.tag("ores/aluminum")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_URANIUM = register(
        "crushed_raw_uranium",
        TagDependentIngredientItem.tag("ores/uranium")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_NICKEL = register("crushed_raw_nickel", TagDependentIngredientItem.tag("ores/nickel"));

    private static <T extends BucketItem> T register(FlowableFluid fluid, BiFunction<Fluid, Item.Settings, T> factory) {
        T bucket = register(
            Registries.FLUID.getId(fluid).withSuffixedPath("_bucket"),
            settings -> factory.apply(fluid, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)
        );
        fluid.getEntry().bucket = bucket;
        return bucket;
    }

    private static BlockItem register(Block block) {
        return register(block, BlockItem::new);
    }

    private static <T extends Block, U extends Item> U register(T block, BiFunction<T, Item.Settings, U> factory) {
        return register(block, factory, new Item.Settings());
    }

    @SuppressWarnings("deprecation")
    private static <T extends Block, U extends Item> U register(T block, BiFunction<T, Item.Settings, U> factory, Item.Settings settings) {
        return register(
            block.getRegistryEntry().registryKey().getValue(),
            itemSettings -> factory.apply(block, itemSettings),
            settings.useBlockPrefixedTranslationKey()
        );
    }

    private static Item register(String id) {
        return register(id, Item::new);
    }

    private static Item register(String id, Item.Settings settings) {
        return register(Identifier.of(MOD_ID, id), Item::new, settings);
    }

    private static <T extends Item> T register(String id, Function<Item.Settings, T> factory) {
        return register(Identifier.of(MOD_ID, id), factory, new Item.Settings());
    }

    private static <T extends Item> T register(String id, Function<Item.Settings, T> factory, Item.Settings settings) {
        return register(Identifier.of(MOD_ID, id), factory, settings);
    }

    private static <T extends Item> T register(Identifier id, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        T item = factory.apply(settings.registryKey(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, item);
        }
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void register() {
    }
}
