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
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Tool.Rule;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.function.BiFunction;

import static com.zurrtum.create.Create.MOD_ID;
import static net.minecraft.world.item.Items.registerBlock;
import static net.minecraft.world.item.Items.registerItem;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AllItems {
    public static final CogwheelBlockItem COGWHEEL = (CogwheelBlockItem) registerBlock(
        AllBlockItemIds.COGWHEEL,
        AllBlocks.COGWHEEL,
        CogwheelBlockItem::new
    );
    public static final BlockItem ANDESITE_ENCASED_COGWHEEL = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_ENCASED_COGWHEEL,
        AllBlocks.ANDESITE_ENCASED_COGWHEEL
    );
    public static final BlockItem BRASS_ENCASED_COGWHEEL = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_ENCASED_COGWHEEL,
        AllBlocks.BRASS_ENCASED_COGWHEEL
    );
    public static final CogwheelBlockItem LARGE_COGWHEEL = (CogwheelBlockItem) registerBlock(
        AllBlockItemIds.LARGE_COGWHEEL,
        AllBlocks.LARGE_COGWHEEL,
        CogwheelBlockItem::new
    );
    public static final BlockItem SHAFT = (BlockItem) registerBlock(AllBlockItemIds.SHAFT, AllBlocks.SHAFT);
    public static final BlockItem GANTRY_SHAFT = (BlockItem) registerBlock(
        AllBlockItemIds.GANTRY_SHAFT,
        AllBlocks.GANTRY_SHAFT
    );
    public static final BlockItem SEQUENCED_GEARSHIFT = (BlockItem) registerBlock(
        AllBlockItemIds.SEQUENCED_GEARSHIFT,
        AllBlocks.SEQUENCED_GEARSHIFT
    );
    public static final BlockItem STEAM_ENGINE = (BlockItem) registerBlock(
        AllBlockItemIds.STEAM_ENGINE,
        AllBlocks.STEAM_ENGINE
    );
    public static final BlockItem GANTRY_CARRIAGE = (BlockItem) registerBlock(
        AllBlockItemIds.GANTRY_CARRIAGE,
        AllBlocks.GANTRY_CARRIAGE
    );
    public static final LargeWaterWheelBlockItem LARGE_WATER_WHEEL = (LargeWaterWheelBlockItem) registerBlock(AllBlockItemIds.LARGE_WATER_WHEEL,
        AllBlocks.LARGE_WATER_WHEEL,
        LargeWaterWheelBlockItem::new
    );
    public static final BlockItem CREATIVE_MOTOR = (BlockItem) registerBlock(
        AllBlockItemIds.CREATIVE_MOTOR,
        AllBlocks.CREATIVE_MOTOR,
        BlockItem::new,
        new Properties().rarity(Rarity.EPIC)
    );
    public static final BlockItem ROTATION_SPEED_CONTROLLER = (BlockItem) registerBlock(
        AllBlockItemIds.ROTATION_SPEED_CONTROLLER,
        AllBlocks.ROTATION_SPEED_CONTROLLER
    );
    public static final BlockItem GEARBOX = (BlockItem) registerBlock(AllBlockItemIds.GEARBOX, AllBlocks.GEARBOX);
    public static final BlockItem WATER_WHEEL = (BlockItem) registerBlock(
        AllBlockItemIds.WATER_WHEEL,
        AllBlocks.WATER_WHEEL
    );
    public static final BlockItem ANDESITE_CASING = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_CASING,
        AllBlocks.ANDESITE_CASING
    );
    public static final BlockItem BRASS_CASING = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_CASING,
        AllBlocks.BRASS_CASING
    );
    public static final BlockItem COPPER_CASING = (BlockItem) registerBlock(
        AllBlockItemIds.COPPER_CASING,
        AllBlocks.COPPER_CASING
    );
    public static final BlockItem SHADOW_STEEL_CASING = (BlockItem) registerBlock(
        AllBlockItemIds.SHADOW_STEEL_CASING,
        AllBlocks.SHADOW_STEEL_CASING
    );
    public static final BlockItem REFINED_RADIANCE_CASING = (BlockItem) registerBlock(
        AllBlockItemIds.REFINED_RADIANCE_CASING,
        AllBlocks.REFINED_RADIANCE_CASING
    );
    public static final BlockItem RAILWAY_CASING = (BlockItem) registerBlock(
        AllBlockItemIds.RAILWAY_CASING,
        AllBlocks.RAILWAY_CASING
    );
    public static final ArmItem MECHANICAL_ARM = (ArmItem) registerBlock(
        AllBlockItemIds.MECHANICAL_ARM,
        AllBlocks.MECHANICAL_ARM,
        ArmItem::new
    );
    public static final BlockItem DEPOT = (BlockItem) registerBlock(AllBlockItemIds.DEPOT, AllBlocks.DEPOT);
    public static final BlockItem CLUTCH = (BlockItem) registerBlock(AllBlockItemIds.CLUTCH, AllBlocks.CLUTCH);
    public static final BlockItem GEARSHIFT = (BlockItem) registerBlock(AllBlockItemIds.GEARSHIFT, AllBlocks.GEARSHIFT);
    public static final BlockItem ENCASED_CHAIN_DRIVE = (BlockItem) registerBlock(
        AllBlockItemIds.ENCASED_CHAIN_DRIVE,
        AllBlocks.ENCASED_CHAIN_DRIVE
    );
    public static final BlockItem ADJUSTABLE_CHAIN_GEARSHIFT = (BlockItem) registerBlock(
        AllBlockItemIds.ADJUSTABLE_CHAIN_GEARSHIFT,
        AllBlocks.ADJUSTABLE_CHAIN_GEARSHIFT
    );
    public static final BlockItem CHAIN_CONVEYOR = (BlockItem) registerBlock(
        AllBlockItemIds.CHAIN_CONVEYOR,
        AllBlocks.CHAIN_CONVEYOR
    );
    public static final BlockItem HAND_CRANK = (BlockItem) registerBlock(
        AllBlockItemIds.HAND_CRANK,
        AllBlocks.HAND_CRANK
    );
    public static final BlockItem COPPER_VALVE_HANDLE = (BlockItem) registerBlock(
        AllBlockItemIds.COPPER_VALVE_HANDLE,
        AllBlocks.COPPER_VALVE_HANDLE
    );
    public static final ColorCollection<BlockItem> VALVE_HANDLE = (ColorCollection) ColorCollection.registerBlockItems(AllBlockItemIds.VALVE_HANDLE,
        (ColorCollection) AllBlocks.VALVE_HANDLE,
        AllItems::registerBlockIgnoreColor
    );
    public static final BlockItem RADIAL_CHASSIS = (BlockItem) registerBlock(
        AllBlockItemIds.RADIAL_CHASSIS,
        AllBlocks.RADIAL_CHASSIS
    );
    public static final BlockItem LINEAR_CHASSIS = (BlockItem) registerBlock(
        AllBlockItemIds.LINEAR_CHASSIS,
        AllBlocks.LINEAR_CHASSIS
    );
    public static final BlockItem SECONDARY_LINEAR_CHASSIS = (BlockItem) registerBlock(
        AllBlockItemIds.SECONDARY_LINEAR_CHASSIS,
        AllBlocks.SECONDARY_LINEAR_CHASSIS
    );
    public static final BlockItem WINDMILL_BEARING = (BlockItem) registerBlock(
        AllBlockItemIds.WINDMILL_BEARING,
        AllBlocks.WINDMILL_BEARING
    );
    public static final BlockItem MECHANICAL_BEARING = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_BEARING,
        AllBlocks.MECHANICAL_BEARING
    );
    public static final BlockItem MECHANICAL_PISTON = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_PISTON,
        AllBlocks.MECHANICAL_PISTON
    );
    public static final BlockItem STICKY_MECHANICAL_PISTON = (BlockItem) registerBlock(
        AllBlockItemIds.STICKY_MECHANICAL_PISTON,
        AllBlocks.STICKY_MECHANICAL_PISTON
    );
    public static final BlockItem PISTON_EXTENSION_POLE = (BlockItem) registerBlock(
        AllBlockItemIds.PISTON_EXTENSION_POLE,
        AllBlocks.PISTON_EXTENSION_POLE
    );
    public static final BlockItem SAIL_FRAME = (BlockItem) registerBlock(
        AllBlockItemIds.SAIL_FRAME,
        AllBlocks.SAIL_FRAME
    );
    public static final BlankSailBlockItem SAIL = (BlankSailBlockItem) registerBlock(
        AllBlockItemIds.SAIL.white(),
        AllBlocks.SAIL.white(),
        BlankSailBlockItem::new
    );
    public static final BlockItem FLUID_PIPE = (BlockItem) registerBlock(
        AllBlockItemIds.FLUID_PIPE,
        AllBlocks.FLUID_PIPE
    );
    public static final BlockItem MECHANICAL_PUMP = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_PUMP,
        AllBlocks.MECHANICAL_PUMP
    );
    public static final BlazeBurnerBlockItem BLAZE_BURNER = (BlazeBurnerBlockItem) registerBlock(
        AllBlockItemIds.BLAZE_BURNER,
        AllBlocks.BLAZE_BURNER,
        BlazeBurnerBlockItem::withBlaze
    );
    public static final FluidTankItem FLUID_TANK = (FluidTankItem) registerBlock(
        AllBlockItemIds.FLUID_TANK,
        AllBlocks.FLUID_TANK,
        FluidTankItem::new
    );
    public static final FluidTankItem CREATIVE_FLUID_TANK = (FluidTankItem) registerBlock(
        AllBlockItemIds.CREATIVE_FLUID_TANK,
        AllBlocks.CREATIVE_FLUID_TANK,
        FluidTankItem::new,
        new Properties().rarity(Rarity.EPIC)
    );
    public static final AssemblyOperatorBlockItem MECHANICAL_PRESS = (AssemblyOperatorBlockItem) registerBlock(AllBlockItemIds.MECHANICAL_PRESS,
        AllBlocks.MECHANICAL_PRESS,
        AssemblyOperatorBlockItem::new
    );
    public static final EjectorItem WEIGHTED_EJECTOR = (EjectorItem) registerBlock(
        AllBlockItemIds.WEIGHTED_EJECTOR,
        AllBlocks.WEIGHTED_EJECTOR,
        EjectorItem::new
    );
    public static final BlockItem ROPE_PULLEY = (BlockItem) registerBlock(
        AllBlockItemIds.ROPE_PULLEY,
        AllBlocks.ROPE_PULLEY
    );
    public static final BlockItem MILLSTONE = (BlockItem) registerBlock(AllBlockItemIds.MILLSTONE, AllBlocks.MILLSTONE);
    public static final BlockItem ENCASED_FAN = (BlockItem) registerBlock(
        AllBlockItemIds.ENCASED_FAN,
        AllBlocks.ENCASED_FAN
    );
    public static final BlockItem PECULIAR_BELL = (BlockItem) registerBlock(
        AllBlockItemIds.PECULIAR_BELL,
        AllBlocks.PECULIAR_BELL
    );
    public static final BlockItem HAUNTED_BELL = (BlockItem) registerBlock(
        AllBlockItemIds.HAUNTED_BELL,
        AllBlocks.HAUNTED_BELL
    );
    public static final BlockItem INDUSTRIAL_IRON_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.INDUSTRIAL_IRON_BLOCK,
        AllBlocks.INDUSTRIAL_IRON_BLOCK
    );
    public static final BlockItem WEATHERED_IRON_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.WEATHERED_IRON_BLOCK,
        AllBlocks.WEATHERED_IRON_BLOCK
    );
    public static final BlockItem INDUSTRIAL_IRON_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.INDUSTRIAL_IRON_WINDOW,
        AllBlocks.INDUSTRIAL_IRON_WINDOW
    );
    public static final BlockItem INDUSTRIAL_IRON_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.INDUSTRIAL_IRON_WINDOW_PANE,
        AllBlocks.INDUSTRIAL_IRON_WINDOW_PANE
    );
    public static final BlockItem WEATHERED_IRON_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.WEATHERED_IRON_WINDOW,
        AllBlocks.WEATHERED_IRON_WINDOW
    );
    public static final BlockItem WEATHERED_IRON_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.WEATHERED_IRON_WINDOW_PANE,
        AllBlocks.WEATHERED_IRON_WINDOW_PANE
    );
    public static final BlockItem MECHANICAL_SAW = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_SAW,
        AllBlocks.MECHANICAL_SAW
    );
    public static final BlockItem BASIN = (BlockItem) registerBlock(AllBlockItemIds.BASIN, AllBlocks.BASIN);
    public static final FunnelItem ANDESITE_FUNNEL = (FunnelItem) registerBlock(
        AllBlockItemIds.ANDESITE_FUNNEL,
        AllBlocks.ANDESITE_FUNNEL,
        FunnelItem::new
    );
    public static final FunnelItem BRASS_FUNNEL = (FunnelItem) registerBlock(
        AllBlockItemIds.BRASS_FUNNEL,
        AllBlocks.BRASS_FUNNEL,
        FunnelItem::new
    );
    public static final BeltTunnelItem ANDESITE_TUNNEL = (BeltTunnelItem) registerBlock(
        AllBlockItemIds.ANDESITE_TUNNEL,
        AllBlocks.ANDESITE_TUNNEL,
        BeltTunnelItem::new
    );
    public static final BeltTunnelItem BRASS_TUNNEL = (BeltTunnelItem) registerBlock(
        AllBlockItemIds.BRASS_TUNNEL,
        AllBlocks.BRASS_TUNNEL,
        BeltTunnelItem::new
    );
    public static final ChuteItem CHUTE = (ChuteItem) registerBlock(
        AllBlockItemIds.CHUTE,
        AllBlocks.CHUTE,
        ChuteItem::new
    );
    public static final BlockItem SMART_CHUTE = (BlockItem) registerBlock(
        AllBlockItemIds.SMART_CHUTE,
        AllBlocks.SMART_CHUTE
    );
    public static final CartAssemblerBlockItem CART_ASSEMBLER = (CartAssemblerBlockItem) registerBlock(
        AllBlockItemIds.CART_ASSEMBLER,
        AllBlocks.CART_ASSEMBLER,
        CartAssemblerBlockItem::new
    );
    public static final BlockItem CONTROLLER_RAIL = (BlockItem) registerBlock(
        AllBlockItemIds.CONTROLLER_RAIL,
        AllBlocks.CONTROLLER_RAIL
    );
    public static final BlockItem MECHANICAL_PLOUGH = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_PLOUGH,
        AllBlocks.MECHANICAL_PLOUGH
    );
    public static final BlockItem MECHANICAL_HARVESTER = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_HARVESTER,
        AllBlocks.MECHANICAL_HARVESTER
    );
    public static final BlockItem PORTABLE_FLUID_INTERFACE = (BlockItem) registerBlock(
        AllBlockItemIds.PORTABLE_FLUID_INTERFACE,
        AllBlocks.PORTABLE_FLUID_INTERFACE
    );
    public static final BlockItem PORTABLE_STORAGE_INTERFACE = (BlockItem) registerBlock(
        AllBlockItemIds.PORTABLE_STORAGE_INTERFACE,
        AllBlocks.PORTABLE_STORAGE_INTERFACE
    );
    public static final BlockItem SPEEDOMETER = (BlockItem) registerBlock(
        AllBlockItemIds.SPEEDOMETER,
        AllBlocks.SPEEDOMETER
    );
    public static final BlockItem STRESSOMETER = (BlockItem) registerBlock(
        AllBlockItemIds.STRESSOMETER,
        AllBlocks.STRESSOMETER
    );
    public static final BlockItem CUCKOO_CLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.CUCKOO_CLOCK,
        AllBlocks.CUCKOO_CLOCK
    );
    public static final BlockItem MYSTERIOUS_CUCKOO_CLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.MYSTERIOUS_CUCKOO_CLOCK,
        AllBlocks.MYSTERIOUS_CUCKOO_CLOCK
    );
    public static final BlockItem MECHANICAL_MIXER = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_MIXER,
        AllBlocks.MECHANICAL_MIXER,
        AssemblyOperatorBlockItem::new
    );
    public static final BlockItem HOSE_PULLEY = (BlockItem) registerBlock(
        AllBlockItemIds.HOSE_PULLEY,
        AllBlocks.HOSE_PULLEY
    );
    public static final AssemblyOperatorBlockItem SPOUT = (AssemblyOperatorBlockItem) registerBlock(
        AllBlockItemIds.SPOUT,
        AllBlocks.SPOUT,
        AssemblyOperatorBlockItem::new
    );
    public static final BlockItem ITEM_DRAIN = (BlockItem) registerBlock(
        AllBlockItemIds.ITEM_DRAIN,
        AllBlocks.ITEM_DRAIN
    );
    public static final BlockItem STEAM_WHISTLE = (BlockItem) registerBlock(
        AllBlockItemIds.STEAM_WHISTLE,
        AllBlocks.STEAM_WHISTLE
    );
    public static final AssemblyOperatorBlockItem DEPLOYER = (AssemblyOperatorBlockItem) registerBlock(
        AllBlockItemIds.DEPLOYER,
        AllBlocks.DEPLOYER,
        AssemblyOperatorBlockItem::new
    );
    public static final BlockItem TURNTABLE = (BlockItem) registerBlock(AllBlockItemIds.TURNTABLE, AllBlocks.TURNTABLE);
    public static final BlockItem MECHANICAL_DRILL = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_DRILL,
        AllBlocks.MECHANICAL_DRILL
    );
    public static final BlockItem CLOCKWORK_BEARING = (BlockItem) registerBlock(
        AllBlockItemIds.CLOCKWORK_BEARING,
        AllBlocks.CLOCKWORK_BEARING
    );
    public static final BlockItem CRUSHING_WHEEL = (BlockItem) registerBlock(
        AllBlockItemIds.CRUSHING_WHEEL,
        AllBlocks.CRUSHING_WHEEL
    );
    public static final BlockItem RAW_ZINC_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.RAW_ZINC_BLOCK,
        AllBlocks.RAW_ZINC_BLOCK
    );
    public static final BlockItem ZINC_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.ZINC_BLOCK,
        AllBlocks.ZINC_BLOCK
    );
    public static final BlockItem ZINC_ORE = (BlockItem) registerBlock(AllBlockItemIds.ZINC_ORE, AllBlocks.ZINC_ORE);
    public static final BlockItem DEEPSLATE_ZINC_ORE = (BlockItem) registerBlock(
        AllBlockItemIds.DEEPSLATE_ZINC_ORE,
        AllBlocks.DEEPSLATE_ZINC_ORE
    );
    public static final BlockItem BRASS_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_BLOCK,
        AllBlocks.BRASS_BLOCK
    );
    public static final BlockItem DISPLAY_BOARD = (BlockItem) registerBlock(
        AllBlockItemIds.DISPLAY_BOARD,
        AllBlocks.DISPLAY_BOARD
    );
    public static final ClipboardBlockItem CLIPBOARD = (ClipboardBlockItem) registerBlock(
        AllBlockItemIds.CLIPBOARD,
        AllBlocks.CLIPBOARD,
        ClipboardBlockItem::new
    );
    public static final DisplayLinkBlockItem DISPLAY_LINK = (DisplayLinkBlockItem) registerBlock(
        AllBlockItemIds.DISPLAY_LINK,
        AllBlocks.DISPLAY_LINK,
        DisplayLinkBlockItem::new
    );
    public static final BlockItem NIXIE_TUBE = (BlockItem) registerBlock(
        AllBlockItemIds.NIXIE_TUBE.orange(),
        AllBlocks.NIXIE_TUBE.orange()
    );
    public static final BracketBlockItem WOODEN_BRACKET = (BracketBlockItem) registerBlock(
        AllBlockItemIds.WOODEN_BRACKET,
        AllBlocks.WOODEN_BRACKET,
        BracketBlockItem::new
    );
    public static final BracketBlockItem METAL_BRACKET = (BracketBlockItem) registerBlock(
        AllBlockItemIds.METAL_BRACKET,
        AllBlocks.METAL_BRACKET,
        BracketBlockItem::new
    );
    public static final BlockItem METAL_GIRDER = (BlockItem) registerBlock(
        AllBlockItemIds.METAL_GIRDER,
        AllBlocks.METAL_GIRDER
    );
    public static final BlockItem FLUID_VALVE = (BlockItem) registerBlock(
        AllBlockItemIds.FLUID_VALVE,
        AllBlocks.FLUID_VALVE
    );
    public static final BlockItem SMART_FLUID_PIPE = (BlockItem) registerBlock(
        AllBlockItemIds.SMART_FLUID_PIPE,
        AllBlocks.SMART_FLUID_PIPE
    );
    public static final BlockItem ANALOG_LEVER = (BlockItem) registerBlock(
        AllBlockItemIds.ANALOG_LEVER,
        AllBlocks.ANALOG_LEVER
    );
    public static final RedstoneContactItem REDSTONE_CONTACT = (RedstoneContactItem) registerBlock(
        AllBlockItemIds.REDSTONE_CONTACT,
        AllBlocks.REDSTONE_CONTACT,
        RedstoneContactItem::new
    );
    public static final BlockItem REDSTONE_LINK = (BlockItem) registerBlock(
        AllBlockItemIds.REDSTONE_LINK,
        AllBlocks.REDSTONE_LINK
    );
    public static final BlockItem PULSE_REPEATER = (BlockItem) registerBlock(
        AllBlockItemIds.PULSE_REPEATER,
        AllBlocks.PULSE_REPEATER
    );
    public static final BlockItem PULSE_EXTENDER = (BlockItem) registerBlock(
        AllBlockItemIds.PULSE_EXTENDER,
        AllBlocks.PULSE_EXTENDER
    );
    public static final BlockItem PULSE_TIMER = (BlockItem) registerBlock(
        AllBlockItemIds.PULSE_TIMER,
        AllBlocks.PULSE_TIMER
    );
    public static final BlockItem POWERED_LATCH = (BlockItem) registerBlock(
        AllBlockItemIds.POWERED_LATCH,
        AllBlocks.POWERED_LATCH
    );
    public static final BlockItem POWERED_TOGGLE_LATCH = (BlockItem) registerBlock(
        AllBlockItemIds.POWERED_TOGGLE_LATCH,
        AllBlocks.POWERED_TOGGLE_LATCH
    );
    public static final BlockItem ROSE_QUARTZ_LAMP = (BlockItem) registerBlock(
        AllBlockItemIds.ROSE_QUARTZ_LAMP,
        AllBlocks.ROSE_QUARTZ_LAMP
    );
    public static final BlockItem SMART_OBSERVER = (BlockItem) registerBlock(
        AllBlockItemIds.SMART_OBSERVER,
        AllBlocks.SMART_OBSERVER
    );
    public static final BlockItem THRESHOLD_SWITCH = (BlockItem) registerBlock(
        AllBlockItemIds.THRESHOLD_SWITCH,
        AllBlocks.THRESHOLD_SWITCH
    );
    public static final BlockItem STICKER = (BlockItem) registerBlock(AllBlockItemIds.STICKER, AllBlocks.STICKER);
    public static final BlockItem CONTRAPTION_CONTROLS = (BlockItem) registerBlock(
        AllBlockItemIds.CONTRAPTION_CONTROLS,
        AllBlocks.CONTRAPTION_CONTROLS
    );
    public static final BlockItem ELEVATOR_PULLEY = (BlockItem) registerBlock(
        AllBlockItemIds.ELEVATOR_PULLEY,
        AllBlocks.ELEVATOR_PULLEY
    );
    public static final BlockItem ELEVATOR_CONTACT = (BlockItem) registerBlock(
        AllBlockItemIds.ELEVATOR_CONTACT,
        AllBlocks.ELEVATOR_CONTACT
    );
    public static final BlockItem ANDESITE_DOOR = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_DOOR,
        AllBlocks.ANDESITE_DOOR
    );
    public static final BlockItem BRASS_DOOR = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_DOOR,
        AllBlocks.BRASS_DOOR
    );
    public static final BlockItem COPPER_DOOR = (BlockItem) registerBlock(
        AllBlockItemIds.COPPER_DOOR,
        AllBlocks.COPPER_DOOR
    );
    public static final BlockItem TRAIN_DOOR = (BlockItem) registerBlock(
        AllBlockItemIds.TRAIN_DOOR,
        AllBlocks.TRAIN_DOOR
    );
    public static final BlockItem FRAMED_GLASS_DOOR = (BlockItem) registerBlock(
        AllBlockItemIds.FRAMED_GLASS_DOOR,
        AllBlocks.FRAMED_GLASS_DOOR
    );
    public static final BlockItem NOZZLE = (BlockItem) registerBlock(AllBlockItemIds.NOZZLE, AllBlocks.NOZZLE);
    public static final BlockItem DESK_BELL = (BlockItem) registerBlock(AllBlockItemIds.DESK_BELL, AllBlocks.DESK_BELL);
    public static final BlockItem MECHANICAL_CRAFTER = (BlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_CRAFTER,
        AllBlocks.MECHANICAL_CRAFTER
    );
    public static final BlockItem CREATIVE_CRATE = (BlockItem) registerBlock(
        AllBlockItemIds.CREATIVE_CRATE,
        AllBlocks.CREATIVE_CRATE,
        BlockItem::new,
        new Properties().rarity(Rarity.EPIC)
    );
    public static final ItemVaultItem ITEM_VAULT = (ItemVaultItem) registerBlock(
        AllBlockItemIds.ITEM_VAULT,
        AllBlocks.ITEM_VAULT,
        ItemVaultItem::new
    );
    public static final TrackBlockItem TRACK = (TrackBlockItem) registerBlock(
        AllBlockItemIds.TRACK,
        AllBlocks.TRACK,
        TrackBlockItem::new
    );
    public static final BlockItem TRAIN_CONTROLS = (BlockItem) registerBlock(
        AllBlockItemIds.TRAIN_CONTROLS,
        AllBlocks.TRAIN_CONTROLS
    );
    public static final TrackTargetingBlockItem TRACK_STATION = (TrackTargetingBlockItem) registerBlock(
        AllBlockItemIds.TRACK_STATION,
        AllBlocks.TRACK_STATION,
        TrackTargetingBlockItem::station
    );
    public static final TrackTargetingBlockItem TRACK_SIGNAL = (TrackTargetingBlockItem) registerBlock(
        AllBlockItemIds.TRACK_SIGNAL,
        AllBlocks.TRACK_SIGNAL,
        TrackTargetingBlockItem::signal
    );
    public static final TrackTargetingBlockItem TRACK_OBSERVER = (TrackTargetingBlockItem) registerBlock(
        AllBlockItemIds.TRACK_OBSERVER,
        AllBlocks.TRACK_OBSERVER,
        TrackTargetingBlockItem::observer
    );
    public static final ColorCollection<BlockItem> SEAT = (ColorCollection) ColorCollection.registerBlockItems(AllBlockItemIds.SEAT,
        (ColorCollection) AllBlocks.SEAT,
        AllItems::registerBlockIgnoreColor
    );
    public static final RollerBlockItem MECHANICAL_ROLLER = (RollerBlockItem) registerBlock(
        AllBlockItemIds.MECHANICAL_ROLLER,
        AllBlocks.MECHANICAL_ROLLER,
        RollerBlockItem::new
    );
    public static final BlockItem PACKAGER = (BlockItem) registerBlock(AllBlockItemIds.PACKAGER, AllBlocks.PACKAGER);
    public static final BlockItem CARDBOARD_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.CARDBOARD_BLOCK,
        AllBlocks.CARDBOARD_BLOCK
    );
    public static final LogisticallyLinkedBlockItem STOCK_LINK = (LogisticallyLinkedBlockItem) registerBlock(AllBlockItemIds.STOCK_LINK,
        AllBlocks.STOCK_LINK,
        LogisticallyLinkedBlockItem::new
    );
    public static final RedstoneRequesterBlockItem REDSTONE_REQUESTER = (RedstoneRequesterBlockItem) registerBlock(AllBlockItemIds.REDSTONE_REQUESTER,
        AllBlocks.REDSTONE_REQUESTER,
        RedstoneRequesterBlockItem::new
    );
    public static final BlockItem REPACKAGER = (BlockItem) registerBlock(
        AllBlockItemIds.REPACKAGER,
        AllBlocks.REPACKAGER
    );
    public static final LogisticallyLinkedBlockItem STOCK_TICKER = (LogisticallyLinkedBlockItem) registerBlock(AllBlockItemIds.STOCK_TICKER,
        AllBlocks.STOCK_TICKER,
        LogisticallyLinkedBlockItem::new
    );
    public static final ColorCollection<TableClothBlockItem> TABLE_CLOTH = (ColorCollection) ColorCollection.registerBlockItems(
        AllBlockItemIds.TABLE_CLOTH,
        (ColorCollection) AllBlocks.TABLE_CLOTH,
        registerBlockIgnoreColor(TableClothBlockItem::new)
    );
    public static final TableClothBlockItem ANDESITE_TABLE_CLOTH = (TableClothBlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_TABLE_CLOTH,
        AllBlocks.ANDESITE_TABLE_CLOTH,
        TableClothBlockItem::new
    );
    public static final TableClothBlockItem BRASS_TABLE_CLOTH = (TableClothBlockItem) registerBlock(
        AllBlockItemIds.BRASS_TABLE_CLOTH,
        AllBlocks.BRASS_TABLE_CLOTH,
        TableClothBlockItem::new
    );
    public static final TableClothBlockItem COPPER_TABLE_CLOTH = (TableClothBlockItem) registerBlock(
        AllBlockItemIds.COPPER_TABLE_CLOTH,
        AllBlocks.COPPER_TABLE_CLOTH,
        TableClothBlockItem::new
    );
    public static final ColorCollection<PackagePortItem> POSTBOX = (ColorCollection) ColorCollection.registerBlockItems(AllBlockItemIds.POSTBOX,
        (ColorCollection) AllBlocks.POSTBOX,
        registerBlockIgnoreColor(PackagePortItem::new)
    );
    public static final PackagePortItem PACKAGE_FROGPORT = (PackagePortItem) registerBlock(
        AllBlockItemIds.PACKAGE_FROGPORT,
        AllBlocks.PACKAGE_FROGPORT,
        PackagePortItem::new
    );
    public static final FactoryPanelBlockItem FACTORY_GAUGE = (FactoryPanelBlockItem) registerBlock(
        AllBlockItemIds.FACTORY_GAUGE,
        AllBlocks.FACTORY_GAUGE,
        FactoryPanelBlockItem::new
    );
    public static final BlockItem FLYWHEEL = (BlockItem) registerBlock(AllBlockItemIds.FLYWHEEL, AllBlocks.FLYWHEEL);
    public static final BlockItem ITEM_HATCH = (BlockItem) registerBlock(
        AllBlockItemIds.ITEM_HATCH,
        AllBlocks.ITEM_HATCH
    );
    public static final BlockItem PLACARD = (BlockItem) registerBlock(AllBlockItemIds.PLACARD, AllBlocks.PLACARD);
    public static final ColorCollection<UncontainableBlockItem> TOOLBOX = (ColorCollection) ColorCollection.registerBlockItems(
        AllBlockItemIds.TOOLBOX,
        (ColorCollection) AllBlocks.TOOLBOX,
        registerBlockIgnoreColor(UncontainableBlockItem::new)
    );
    public static final BlockItem SCHEMATIC_TABLE = (BlockItem) registerBlock(
        AllBlockItemIds.SCHEMATIC_TABLE,
        AllBlocks.SCHEMATIC_TABLE
    );
    public static final BlockItem SCHEMATICANNON = (BlockItem) registerBlock(
        AllBlockItemIds.SCHEMATICANNON,
        AllBlocks.SCHEMATICANNON
    );
    public static final BlockItem ANDESITE_ENCASED_SHAFT = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_ENCASED_SHAFT,
        AllBlocks.ANDESITE_ENCASED_SHAFT
    );
    public static final BlockItem BRASS_ENCASED_SHAFT = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_ENCASED_SHAFT,
        AllBlocks.BRASS_ENCASED_SHAFT
    );
    public static final BlockItem ORNATE_IRON_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.ORNATE_IRON_WINDOW,
        AllBlocks.ORNATE_IRON_WINDOW
    );
    public static final BlockItem ANDESITE_LADDER = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_LADDER,
        AllBlocks.ANDESITE_LADDER
    );
    public static final BlockItem BRASS_LADDER = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_LADDER,
        AllBlocks.BRASS_LADDER
    );
    public static final BlockItem COPPER_LADDER = (BlockItem) registerBlock(
        AllBlockItemIds.COPPER_LADDER,
        AllBlocks.COPPER_LADDER
    );
    public static final MetalScaffoldingBlockItem ANDESITE_SCAFFOLD = (MetalScaffoldingBlockItem) registerBlock(AllBlockItemIds.ANDESITE_SCAFFOLD,
        AllBlocks.ANDESITE_SCAFFOLD,
        MetalScaffoldingBlockItem::new
    );
    public static final MetalScaffoldingBlockItem BRASS_SCAFFOLD = (MetalScaffoldingBlockItem) registerBlock(AllBlockItemIds.BRASS_SCAFFOLD,
        AllBlocks.BRASS_SCAFFOLD,
        MetalScaffoldingBlockItem::new
    );
    public static final MetalScaffoldingBlockItem COPPER_SCAFFOLD = (MetalScaffoldingBlockItem) registerBlock(AllBlockItemIds.COPPER_SCAFFOLD,
        AllBlocks.COPPER_SCAFFOLD,
        MetalScaffoldingBlockItem::new
    );
    public static final BlockItem ANDESITE_BARS = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_BARS,
        AllBlocks.ANDESITE_BARS
    );
    public static final BlockItem BRASS_BARS = (BlockItem) registerBlock(
        AllBlockItemIds.BRASS_BARS,
        AllBlocks.BRASS_BARS
    );
    public static final BlockItem COPPER_BARS = (BlockItem) registerBlock(
        AllBlockItemIds.COPPER_BARS,
        AllBlocks.COPPER_BARS
    );
    public static final BlockItem TRAIN_TRAPDOOR = (BlockItem) registerBlock(
        AllBlockItemIds.TRAIN_TRAPDOOR,
        AllBlocks.TRAIN_TRAPDOOR
    );
    public static final BlockItem FRAMED_GLASS_TRAPDOOR = (BlockItem) registerBlock(
        AllBlockItemIds.FRAMED_GLASS_TRAPDOOR,
        AllBlocks.FRAMED_GLASS_TRAPDOOR
    );
    public static final BlockItem ANDESITE_ALLOY_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_ALLOY_BLOCK,
        AllBlocks.ANDESITE_ALLOY_BLOCK
    );
    public static final BlockItem BOUND_CARDBOARD_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.BOUND_CARDBOARD_BLOCK,
        AllBlocks.BOUND_CARDBOARD_BLOCK
    );
    public static final BlockItem EXPERIENCE_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.EXPERIENCE_BLOCK,
        AllBlocks.EXPERIENCE_BLOCK,
        BlockItem::new,
        new Properties().rarity(Rarity.UNCOMMON)
    );
    public static final BlockItem ROSE_QUARTZ_BLOCK = (BlockItem) registerBlock(
        AllBlockItemIds.ROSE_QUARTZ_BLOCK,
        AllBlocks.ROSE_QUARTZ_BLOCK
    );
    public static final BlockItem ROSE_QUARTZ_TILES = (BlockItem) registerBlock(
        AllBlockItemIds.ROSE_QUARTZ_TILES,
        AllBlocks.ROSE_QUARTZ_TILES
    );
    public static final BlockItem SMALL_ROSE_QUARTZ_TILES = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ROSE_QUARTZ_TILES,
        AllBlocks.SMALL_ROSE_QUARTZ_TILES
    );
    public static final WeatheringCopperCollection<BlockItem> COPPER_SHINGLES = (WeatheringCopperCollection) WeatheringCopperCollection.registerItems(AllBlockItemIds.COPPER_SHINGLES,
        AllBlocks.COPPER_SHINGLES,
        Items::registerBlock
    );
    public static final WeatheringCopperCollection<BlockItem> COPPER_SHINGLE_SLAB = (WeatheringCopperCollection) WeatheringCopperCollection.registerItems(AllBlockItemIds.COPPER_SHINGLE_SLAB,
        AllBlocks.COPPER_SHINGLE_SLAB,
        Items::registerBlock
    );
    public static final WeatheringCopperCollection<BlockItem> COPPER_SHINGLE_STAIRS = (WeatheringCopperCollection) WeatheringCopperCollection.registerItems(AllBlockItemIds.COPPER_SHINGLE_STAIRS,
        AllBlocks.COPPER_SHINGLE_STAIRS,
        Items::registerBlock
    );
    public static final WeatheringCopperCollection<BlockItem> COPPER_TILES = (WeatheringCopperCollection) WeatheringCopperCollection.registerItems(AllBlockItemIds.COPPER_TILES,
        AllBlocks.COPPER_TILES,
        Items::registerBlock
    );
    public static final WeatheringCopperCollection<BlockItem> COPPER_TILE_SLAB = (WeatheringCopperCollection) WeatheringCopperCollection.registerItems(AllBlockItemIds.COPPER_TILE_SLAB,
        AllBlocks.COPPER_TILE_SLAB,
        Items::registerBlock
    );
    public static final WeatheringCopperCollection<BlockItem> COPPER_TILE_STAIRS = (WeatheringCopperCollection) WeatheringCopperCollection.registerItems(AllBlockItemIds.COPPER_TILE_STAIRS,
        AllBlocks.COPPER_TILE_STAIRS,
        Items::registerBlock
    );
    public static final BlockItem TILED_GLASS = (BlockItem) registerBlock(
        AllBlockItemIds.TILED_GLASS,
        AllBlocks.TILED_GLASS
    );
    public static final BlockItem FRAMED_GLASS = (BlockItem) registerBlock(
        AllBlockItemIds.FRAMED_GLASS,
        AllBlocks.FRAMED_GLASS
    );
    public static final BlockItem HORIZONTAL_FRAMED_GLASS = (BlockItem) registerBlock(
        AllBlockItemIds.HORIZONTAL_FRAMED_GLASS,
        AllBlocks.HORIZONTAL_FRAMED_GLASS
    );
    public static final BlockItem VERTICAL_FRAMED_GLASS = (BlockItem) registerBlock(
        AllBlockItemIds.VERTICAL_FRAMED_GLASS,
        AllBlocks.VERTICAL_FRAMED_GLASS
    );
    public static final BlockItem TILED_GLASS_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.TILED_GLASS_PANE,
        AllBlocks.TILED_GLASS_PANE
    );
    public static final BlockItem FRAMED_GLASS_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.FRAMED_GLASS_PANE,
        AllBlocks.FRAMED_GLASS_PANE
    );
    public static final BlockItem HORIZONTAL_FRAMED_GLASS_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.HORIZONTAL_FRAMED_GLASS_PANE,
        AllBlocks.HORIZONTAL_FRAMED_GLASS_PANE
    );
    public static final BlockItem VERTICAL_FRAMED_GLASS_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.VERTICAL_FRAMED_GLASS_PANE,
        AllBlocks.VERTICAL_FRAMED_GLASS_PANE
    );
    public static final BlockItem OAK_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.OAK_WINDOW,
        AllBlocks.OAK_WINDOW
    );
    public static final BlockItem SPRUCE_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.SPRUCE_WINDOW,
        AllBlocks.SPRUCE_WINDOW
    );
    public static final BlockItem BIRCH_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.BIRCH_WINDOW,
        AllBlocks.BIRCH_WINDOW
    );
    public static final BlockItem JUNGLE_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.JUNGLE_WINDOW,
        AllBlocks.JUNGLE_WINDOW
    );
    public static final BlockItem ACACIA_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.ACACIA_WINDOW,
        AllBlocks.ACACIA_WINDOW
    );
    public static final BlockItem DARK_OAK_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.DARK_OAK_WINDOW,
        AllBlocks.DARK_OAK_WINDOW
    );
    public static final BlockItem MANGROVE_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.MANGROVE_WINDOW,
        AllBlocks.MANGROVE_WINDOW
    );
    public static final BlockItem CRIMSON_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.CRIMSON_WINDOW,
        AllBlocks.CRIMSON_WINDOW
    );
    public static final BlockItem WARPED_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.WARPED_WINDOW,
        AllBlocks.WARPED_WINDOW
    );
    public static final BlockItem CHERRY_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.CHERRY_WINDOW,
        AllBlocks.CHERRY_WINDOW
    );
    public static final BlockItem BAMBOO_WINDOW = (BlockItem) registerBlock(
        AllBlockItemIds.BAMBOO_WINDOW,
        AllBlocks.BAMBOO_WINDOW
    );
    public static final BlockItem OAK_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.OAK_WINDOW_PANE,
        AllBlocks.OAK_WINDOW_PANE
    );
    public static final BlockItem SPRUCE_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.SPRUCE_WINDOW_PANE,
        AllBlocks.SPRUCE_WINDOW_PANE
    );
    public static final BlockItem BIRCH_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.BIRCH_WINDOW_PANE,
        AllBlocks.BIRCH_WINDOW_PANE
    );
    public static final BlockItem JUNGLE_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.JUNGLE_WINDOW_PANE,
        AllBlocks.JUNGLE_WINDOW_PANE
    );
    public static final BlockItem ACACIA_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.ACACIA_WINDOW_PANE,
        AllBlocks.ACACIA_WINDOW_PANE
    );
    public static final BlockItem DARK_OAK_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.DARK_OAK_WINDOW_PANE,
        AllBlocks.DARK_OAK_WINDOW_PANE
    );
    public static final BlockItem MANGROVE_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.MANGROVE_WINDOW_PANE,
        AllBlocks.MANGROVE_WINDOW_PANE
    );
    public static final BlockItem CRIMSON_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.CRIMSON_WINDOW_PANE,
        AllBlocks.CRIMSON_WINDOW_PANE
    );
    public static final BlockItem WARPED_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.WARPED_WINDOW_PANE,
        AllBlocks.WARPED_WINDOW_PANE
    );
    public static final BlockItem CHERRY_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.CHERRY_WINDOW_PANE,
        AllBlocks.CHERRY_WINDOW_PANE
    );
    public static final BlockItem BAMBOO_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.BAMBOO_WINDOW_PANE,
        AllBlocks.BAMBOO_WINDOW_PANE
    );
    public static final BlockItem ORNATE_IRON_WINDOW_PANE = (BlockItem) registerBlock(
        AllBlockItemIds.ORNATE_IRON_WINDOW_PANE,
        AllBlocks.ORNATE_IRON_WINDOW_PANE
    );
    public static final BlockItem CUT_GRANITE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE,
        AllBlocks.CUT_GRANITE
    );
    public static final BlockItem CUT_GRANITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_STAIRS,
        AllBlocks.CUT_GRANITE_STAIRS
    );
    public static final BlockItem CUT_GRANITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_SLAB,
        AllBlocks.CUT_GRANITE_SLAB
    );
    public static final BlockItem CUT_GRANITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_WALL,
        AllBlocks.CUT_GRANITE_WALL
    );
    public static final BlockItem POLISHED_CUT_GRANITE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_GRANITE,
        AllBlocks.POLISHED_CUT_GRANITE
    );
    public static final BlockItem POLISHED_CUT_GRANITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_GRANITE_STAIRS,
        AllBlocks.POLISHED_CUT_GRANITE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_GRANITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_GRANITE_SLAB,
        AllBlocks.POLISHED_CUT_GRANITE_SLAB
    );
    public static final BlockItem POLISHED_CUT_GRANITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_GRANITE_WALL,
        AllBlocks.POLISHED_CUT_GRANITE_WALL
    );
    public static final BlockItem CUT_GRANITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_BRICKS,
        AllBlocks.CUT_GRANITE_BRICKS
    );
    public static final BlockItem CUT_GRANITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_BRICK_STAIRS,
        AllBlocks.CUT_GRANITE_BRICK_STAIRS
    );
    public static final BlockItem CUT_GRANITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_BRICK_SLAB,
        AllBlocks.CUT_GRANITE_BRICK_SLAB
    );
    public static final BlockItem CUT_GRANITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_GRANITE_BRICK_WALL,
        AllBlocks.CUT_GRANITE_BRICK_WALL
    );
    public static final BlockItem SMALL_GRANITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_GRANITE_BRICKS,
        AllBlocks.SMALL_GRANITE_BRICKS
    );
    public static final BlockItem SMALL_GRANITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_GRANITE_BRICK_STAIRS,
        AllBlocks.SMALL_GRANITE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_GRANITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_GRANITE_BRICK_SLAB,
        AllBlocks.SMALL_GRANITE_BRICK_SLAB
    );
    public static final BlockItem SMALL_GRANITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_GRANITE_BRICK_WALL,
        AllBlocks.SMALL_GRANITE_BRICK_WALL
    );
    public static final BlockItem LAYERED_GRANITE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_GRANITE,
        AllBlocks.LAYERED_GRANITE
    );
    public static final BlockItem GRANITE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.GRANITE_PILLAR,
        AllBlocks.GRANITE_PILLAR
    );
    public static final BlockItem CUT_DIORITE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE,
        AllBlocks.CUT_DIORITE
    );
    public static final BlockItem CUT_DIORITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_STAIRS,
        AllBlocks.CUT_DIORITE_STAIRS
    );
    public static final BlockItem CUT_DIORITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_SLAB,
        AllBlocks.CUT_DIORITE_SLAB
    );
    public static final BlockItem CUT_DIORITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_WALL,
        AllBlocks.CUT_DIORITE_WALL
    );
    public static final BlockItem POLISHED_CUT_DIORITE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DIORITE,
        AllBlocks.POLISHED_CUT_DIORITE
    );
    public static final BlockItem POLISHED_CUT_DIORITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DIORITE_STAIRS,
        AllBlocks.POLISHED_CUT_DIORITE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_DIORITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DIORITE_SLAB,
        AllBlocks.POLISHED_CUT_DIORITE_SLAB
    );
    public static final BlockItem POLISHED_CUT_DIORITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DIORITE_WALL,
        AllBlocks.POLISHED_CUT_DIORITE_WALL
    );
    public static final BlockItem CUT_DIORITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_BRICKS,
        AllBlocks.CUT_DIORITE_BRICKS
    );
    public static final BlockItem CUT_DIORITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_BRICK_STAIRS,
        AllBlocks.CUT_DIORITE_BRICK_STAIRS
    );
    public static final BlockItem CUT_DIORITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_BRICK_SLAB,
        AllBlocks.CUT_DIORITE_BRICK_SLAB
    );
    public static final BlockItem CUT_DIORITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DIORITE_BRICK_WALL,
        AllBlocks.CUT_DIORITE_BRICK_WALL
    );
    public static final BlockItem SMALL_DIORITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DIORITE_BRICKS,
        AllBlocks.SMALL_DIORITE_BRICKS
    );
    public static final BlockItem SMALL_DIORITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DIORITE_BRICK_STAIRS,
        AllBlocks.SMALL_DIORITE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_DIORITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DIORITE_BRICK_SLAB,
        AllBlocks.SMALL_DIORITE_BRICK_SLAB
    );
    public static final BlockItem SMALL_DIORITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DIORITE_BRICK_WALL,
        AllBlocks.SMALL_DIORITE_BRICK_WALL
    );
    public static final BlockItem LAYERED_DIORITE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_DIORITE,
        AllBlocks.LAYERED_DIORITE
    );
    public static final BlockItem DIORITE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.DIORITE_PILLAR,
        AllBlocks.DIORITE_PILLAR
    );
    public static final BlockItem CUT_ANDESITE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE,
        AllBlocks.CUT_ANDESITE
    );
    public static final BlockItem CUT_ANDESITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_STAIRS,
        AllBlocks.CUT_ANDESITE_STAIRS
    );
    public static final BlockItem CUT_ANDESITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_SLAB,
        AllBlocks.CUT_ANDESITE_SLAB
    );
    public static final BlockItem CUT_ANDESITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_WALL,
        AllBlocks.CUT_ANDESITE_WALL
    );
    public static final BlockItem POLISHED_CUT_ANDESITE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ANDESITE,
        AllBlocks.POLISHED_CUT_ANDESITE
    );
    public static final BlockItem POLISHED_CUT_ANDESITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ANDESITE_STAIRS,
        AllBlocks.POLISHED_CUT_ANDESITE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_ANDESITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ANDESITE_SLAB,
        AllBlocks.POLISHED_CUT_ANDESITE_SLAB
    );
    public static final BlockItem POLISHED_CUT_ANDESITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ANDESITE_WALL,
        AllBlocks.POLISHED_CUT_ANDESITE_WALL
    );
    public static final BlockItem CUT_ANDESITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_BRICKS,
        AllBlocks.CUT_ANDESITE_BRICKS
    );
    public static final BlockItem CUT_ANDESITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_BRICK_STAIRS,
        AllBlocks.CUT_ANDESITE_BRICK_STAIRS
    );
    public static final BlockItem CUT_ANDESITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_BRICK_SLAB,
        AllBlocks.CUT_ANDESITE_BRICK_SLAB
    );
    public static final BlockItem CUT_ANDESITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ANDESITE_BRICK_WALL,
        AllBlocks.CUT_ANDESITE_BRICK_WALL
    );
    public static final BlockItem SMALL_ANDESITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ANDESITE_BRICKS,
        AllBlocks.SMALL_ANDESITE_BRICKS
    );
    public static final BlockItem SMALL_ANDESITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ANDESITE_BRICK_STAIRS,
        AllBlocks.SMALL_ANDESITE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_ANDESITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ANDESITE_BRICK_SLAB,
        AllBlocks.SMALL_ANDESITE_BRICK_SLAB
    );
    public static final BlockItem SMALL_ANDESITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ANDESITE_BRICK_WALL,
        AllBlocks.SMALL_ANDESITE_BRICK_WALL
    );
    public static final BlockItem LAYERED_ANDESITE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_ANDESITE,
        AllBlocks.LAYERED_ANDESITE
    );
    public static final BlockItem ANDESITE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.ANDESITE_PILLAR,
        AllBlocks.ANDESITE_PILLAR
    );
    public static final BlockItem CUT_CALCITE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE,
        AllBlocks.CUT_CALCITE
    );
    public static final BlockItem CUT_CALCITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_STAIRS,
        AllBlocks.CUT_CALCITE_STAIRS
    );
    public static final BlockItem CUT_CALCITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_SLAB,
        AllBlocks.CUT_CALCITE_SLAB
    );
    public static final BlockItem CUT_CALCITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_WALL,
        AllBlocks.CUT_CALCITE_WALL
    );
    public static final BlockItem POLISHED_CUT_CALCITE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CALCITE,
        AllBlocks.POLISHED_CUT_CALCITE
    );
    public static final BlockItem POLISHED_CUT_CALCITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CALCITE_STAIRS,
        AllBlocks.POLISHED_CUT_CALCITE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_CALCITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CALCITE_SLAB,
        AllBlocks.POLISHED_CUT_CALCITE_SLAB
    );
    public static final BlockItem POLISHED_CUT_CALCITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CALCITE_WALL,
        AllBlocks.POLISHED_CUT_CALCITE_WALL
    );
    public static final BlockItem CUT_CALCITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_BRICKS,
        AllBlocks.CUT_CALCITE_BRICKS
    );
    public static final BlockItem CUT_CALCITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_BRICK_STAIRS,
        AllBlocks.CUT_CALCITE_BRICK_STAIRS
    );
    public static final BlockItem CUT_CALCITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_BRICK_SLAB,
        AllBlocks.CUT_CALCITE_BRICK_SLAB
    );
    public static final BlockItem CUT_CALCITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CALCITE_BRICK_WALL,
        AllBlocks.CUT_CALCITE_BRICK_WALL
    );
    public static final BlockItem SMALL_CALCITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CALCITE_BRICKS,
        AllBlocks.SMALL_CALCITE_BRICKS
    );
    public static final BlockItem SMALL_CALCITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CALCITE_BRICK_STAIRS,
        AllBlocks.SMALL_CALCITE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_CALCITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CALCITE_BRICK_SLAB,
        AllBlocks.SMALL_CALCITE_BRICK_SLAB
    );
    public static final BlockItem SMALL_CALCITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CALCITE_BRICK_WALL,
        AllBlocks.SMALL_CALCITE_BRICK_WALL
    );
    public static final BlockItem LAYERED_CALCITE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_CALCITE,
        AllBlocks.LAYERED_CALCITE
    );
    public static final BlockItem CALCITE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.CALCITE_PILLAR,
        AllBlocks.CALCITE_PILLAR
    );
    public static final BlockItem CUT_DRIPSTONE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE,
        AllBlocks.CUT_DRIPSTONE
    );
    public static final BlockItem CUT_DRIPSTONE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_STAIRS,
        AllBlocks.CUT_DRIPSTONE_STAIRS
    );
    public static final BlockItem CUT_DRIPSTONE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_SLAB,
        AllBlocks.CUT_DRIPSTONE_SLAB
    );
    public static final BlockItem CUT_DRIPSTONE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_WALL,
        AllBlocks.CUT_DRIPSTONE_WALL
    );
    public static final BlockItem POLISHED_CUT_DRIPSTONE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE,
        AllBlocks.POLISHED_CUT_DRIPSTONE
    );
    public static final BlockItem POLISHED_CUT_DRIPSTONE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE_STAIRS,
        AllBlocks.POLISHED_CUT_DRIPSTONE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_DRIPSTONE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE_SLAB,
        AllBlocks.POLISHED_CUT_DRIPSTONE_SLAB
    );
    public static final BlockItem POLISHED_CUT_DRIPSTONE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DRIPSTONE_WALL,
        AllBlocks.POLISHED_CUT_DRIPSTONE_WALL
    );
    public static final BlockItem CUT_DRIPSTONE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_BRICKS,
        AllBlocks.CUT_DRIPSTONE_BRICKS
    );
    public static final BlockItem CUT_DRIPSTONE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_BRICK_STAIRS,
        AllBlocks.CUT_DRIPSTONE_BRICK_STAIRS
    );
    public static final BlockItem CUT_DRIPSTONE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_BRICK_SLAB,
        AllBlocks.CUT_DRIPSTONE_BRICK_SLAB
    );
    public static final BlockItem CUT_DRIPSTONE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DRIPSTONE_BRICK_WALL,
        AllBlocks.CUT_DRIPSTONE_BRICK_WALL
    );
    public static final BlockItem SMALL_DRIPSTONE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICKS,
        AllBlocks.SMALL_DRIPSTONE_BRICKS
    );
    public static final BlockItem SMALL_DRIPSTONE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICK_STAIRS,
        AllBlocks.SMALL_DRIPSTONE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_DRIPSTONE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICK_SLAB,
        AllBlocks.SMALL_DRIPSTONE_BRICK_SLAB
    );
    public static final BlockItem SMALL_DRIPSTONE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DRIPSTONE_BRICK_WALL,
        AllBlocks.SMALL_DRIPSTONE_BRICK_WALL
    );
    public static final BlockItem LAYERED_DRIPSTONE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_DRIPSTONE,
        AllBlocks.LAYERED_DRIPSTONE
    );
    public static final BlockItem DRIPSTONE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.DRIPSTONE_PILLAR,
        AllBlocks.DRIPSTONE_PILLAR
    );
    public static final BlockItem CUT_DEEPSLATE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE,
        AllBlocks.CUT_DEEPSLATE
    );
    public static final BlockItem CUT_DEEPSLATE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_STAIRS,
        AllBlocks.CUT_DEEPSLATE_STAIRS
    );
    public static final BlockItem CUT_DEEPSLATE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_SLAB,
        AllBlocks.CUT_DEEPSLATE_SLAB
    );
    public static final BlockItem CUT_DEEPSLATE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_WALL,
        AllBlocks.CUT_DEEPSLATE_WALL
    );
    public static final BlockItem POLISHED_CUT_DEEPSLATE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE,
        AllBlocks.POLISHED_CUT_DEEPSLATE
    );
    public static final BlockItem POLISHED_CUT_DEEPSLATE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE_STAIRS,
        AllBlocks.POLISHED_CUT_DEEPSLATE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_DEEPSLATE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE_SLAB,
        AllBlocks.POLISHED_CUT_DEEPSLATE_SLAB
    );
    public static final BlockItem POLISHED_CUT_DEEPSLATE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_DEEPSLATE_WALL,
        AllBlocks.POLISHED_CUT_DEEPSLATE_WALL
    );
    public static final BlockItem CUT_DEEPSLATE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_BRICKS,
        AllBlocks.CUT_DEEPSLATE_BRICKS
    );
    public static final BlockItem CUT_DEEPSLATE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_BRICK_STAIRS,
        AllBlocks.CUT_DEEPSLATE_BRICK_STAIRS
    );
    public static final BlockItem CUT_DEEPSLATE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_BRICK_SLAB,
        AllBlocks.CUT_DEEPSLATE_BRICK_SLAB
    );
    public static final BlockItem CUT_DEEPSLATE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_DEEPSLATE_BRICK_WALL,
        AllBlocks.CUT_DEEPSLATE_BRICK_WALL
    );
    public static final BlockItem SMALL_DEEPSLATE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICKS,
        AllBlocks.SMALL_DEEPSLATE_BRICKS
    );
    public static final BlockItem SMALL_DEEPSLATE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICK_STAIRS,
        AllBlocks.SMALL_DEEPSLATE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_DEEPSLATE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICK_SLAB,
        AllBlocks.SMALL_DEEPSLATE_BRICK_SLAB
    );
    public static final BlockItem SMALL_DEEPSLATE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_DEEPSLATE_BRICK_WALL,
        AllBlocks.SMALL_DEEPSLATE_BRICK_WALL
    );
    public static final BlockItem LAYERED_DEEPSLATE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_DEEPSLATE,
        AllBlocks.LAYERED_DEEPSLATE
    );
    public static final BlockItem DEEPSLATE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.DEEPSLATE_PILLAR,
        AllBlocks.DEEPSLATE_PILLAR
    );
    public static final BlockItem CUT_TUFF = (BlockItem) registerBlock(AllBlockItemIds.CUT_TUFF, AllBlocks.CUT_TUFF);
    public static final BlockItem CUT_TUFF_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_STAIRS,
        AllBlocks.CUT_TUFF_STAIRS
    );
    public static final BlockItem CUT_TUFF_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_SLAB,
        AllBlocks.CUT_TUFF_SLAB
    );
    public static final BlockItem CUT_TUFF_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_WALL,
        AllBlocks.CUT_TUFF_WALL
    );
    public static final BlockItem POLISHED_CUT_TUFF = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_TUFF,
        AllBlocks.POLISHED_CUT_TUFF
    );
    public static final BlockItem POLISHED_CUT_TUFF_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_TUFF_STAIRS,
        AllBlocks.POLISHED_CUT_TUFF_STAIRS
    );
    public static final BlockItem POLISHED_CUT_TUFF_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_TUFF_SLAB,
        AllBlocks.POLISHED_CUT_TUFF_SLAB
    );
    public static final BlockItem POLISHED_CUT_TUFF_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_TUFF_WALL,
        AllBlocks.POLISHED_CUT_TUFF_WALL
    );
    public static final BlockItem CUT_TUFF_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_BRICKS,
        AllBlocks.CUT_TUFF_BRICKS
    );
    public static final BlockItem CUT_TUFF_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_BRICK_STAIRS,
        AllBlocks.CUT_TUFF_BRICK_STAIRS
    );
    public static final BlockItem CUT_TUFF_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_BRICK_SLAB,
        AllBlocks.CUT_TUFF_BRICK_SLAB
    );
    public static final BlockItem CUT_TUFF_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_TUFF_BRICK_WALL,
        AllBlocks.CUT_TUFF_BRICK_WALL
    );
    public static final BlockItem SMALL_TUFF_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_TUFF_BRICKS,
        AllBlocks.SMALL_TUFF_BRICKS
    );
    public static final BlockItem SMALL_TUFF_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_TUFF_BRICK_STAIRS,
        AllBlocks.SMALL_TUFF_BRICK_STAIRS
    );
    public static final BlockItem SMALL_TUFF_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_TUFF_BRICK_SLAB,
        AllBlocks.SMALL_TUFF_BRICK_SLAB
    );
    public static final BlockItem SMALL_TUFF_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_TUFF_BRICK_WALL,
        AllBlocks.SMALL_TUFF_BRICK_WALL
    );
    public static final BlockItem LAYERED_TUFF = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_TUFF,
        AllBlocks.LAYERED_TUFF
    );
    public static final BlockItem TUFF_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.TUFF_PILLAR,
        AllBlocks.TUFF_PILLAR
    );
    public static final BlockItem ASURINE = (BlockItem) registerBlock(AllBlockItemIds.ASURINE, AllBlocks.ASURINE);
    public static final BlockItem CUT_ASURINE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE,
        AllBlocks.CUT_ASURINE
    );
    public static final BlockItem CUT_ASURINE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_STAIRS,
        AllBlocks.CUT_ASURINE_STAIRS
    );
    public static final BlockItem CUT_ASURINE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_SLAB,
        AllBlocks.CUT_ASURINE_SLAB
    );
    public static final BlockItem CUT_ASURINE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_WALL,
        AllBlocks.CUT_ASURINE_WALL
    );
    public static final BlockItem POLISHED_CUT_ASURINE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ASURINE,
        AllBlocks.POLISHED_CUT_ASURINE
    );
    public static final BlockItem POLISHED_CUT_ASURINE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ASURINE_STAIRS,
        AllBlocks.POLISHED_CUT_ASURINE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_ASURINE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ASURINE_SLAB,
        AllBlocks.POLISHED_CUT_ASURINE_SLAB
    );
    public static final BlockItem POLISHED_CUT_ASURINE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_ASURINE_WALL,
        AllBlocks.POLISHED_CUT_ASURINE_WALL
    );
    public static final BlockItem CUT_ASURINE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_BRICKS,
        AllBlocks.CUT_ASURINE_BRICKS
    );
    public static final BlockItem CUT_ASURINE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_BRICK_STAIRS,
        AllBlocks.CUT_ASURINE_BRICK_STAIRS
    );
    public static final BlockItem CUT_ASURINE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_BRICK_SLAB,
        AllBlocks.CUT_ASURINE_BRICK_SLAB
    );
    public static final BlockItem CUT_ASURINE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_ASURINE_BRICK_WALL,
        AllBlocks.CUT_ASURINE_BRICK_WALL
    );
    public static final BlockItem SMALL_ASURINE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ASURINE_BRICKS,
        AllBlocks.SMALL_ASURINE_BRICKS
    );
    public static final BlockItem SMALL_ASURINE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ASURINE_BRICK_STAIRS,
        AllBlocks.SMALL_ASURINE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_ASURINE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ASURINE_BRICK_SLAB,
        AllBlocks.SMALL_ASURINE_BRICK_SLAB
    );
    public static final BlockItem SMALL_ASURINE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_ASURINE_BRICK_WALL,
        AllBlocks.SMALL_ASURINE_BRICK_WALL
    );
    public static final BlockItem LAYERED_ASURINE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_ASURINE,
        AllBlocks.LAYERED_ASURINE
    );
    public static final BlockItem ASURINE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.ASURINE_PILLAR,
        AllBlocks.ASURINE_PILLAR
    );
    public static final BlockItem CRIMSITE = (BlockItem) registerBlock(AllBlockItemIds.CRIMSITE, AllBlocks.CRIMSITE);
    public static final BlockItem CUT_CRIMSITE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE,
        AllBlocks.CUT_CRIMSITE
    );
    public static final BlockItem CUT_CRIMSITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_STAIRS,
        AllBlocks.CUT_CRIMSITE_STAIRS
    );
    public static final BlockItem CUT_CRIMSITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_SLAB,
        AllBlocks.CUT_CRIMSITE_SLAB
    );
    public static final BlockItem CUT_CRIMSITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_WALL,
        AllBlocks.CUT_CRIMSITE_WALL
    );
    public static final BlockItem POLISHED_CUT_CRIMSITE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE,
        AllBlocks.POLISHED_CUT_CRIMSITE
    );
    public static final BlockItem POLISHED_CUT_CRIMSITE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE_STAIRS,
        AllBlocks.POLISHED_CUT_CRIMSITE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_CRIMSITE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE_SLAB,
        AllBlocks.POLISHED_CUT_CRIMSITE_SLAB
    );
    public static final BlockItem POLISHED_CUT_CRIMSITE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_CRIMSITE_WALL,
        AllBlocks.POLISHED_CUT_CRIMSITE_WALL
    );
    public static final BlockItem CUT_CRIMSITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_BRICKS,
        AllBlocks.CUT_CRIMSITE_BRICKS
    );
    public static final BlockItem CUT_CRIMSITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_BRICK_STAIRS,
        AllBlocks.CUT_CRIMSITE_BRICK_STAIRS
    );
    public static final BlockItem CUT_CRIMSITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_BRICK_SLAB,
        AllBlocks.CUT_CRIMSITE_BRICK_SLAB
    );
    public static final BlockItem CUT_CRIMSITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_CRIMSITE_BRICK_WALL,
        AllBlocks.CUT_CRIMSITE_BRICK_WALL
    );
    public static final BlockItem SMALL_CRIMSITE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CRIMSITE_BRICKS,
        AllBlocks.SMALL_CRIMSITE_BRICKS
    );
    public static final BlockItem SMALL_CRIMSITE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CRIMSITE_BRICK_STAIRS,
        AllBlocks.SMALL_CRIMSITE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_CRIMSITE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CRIMSITE_BRICK_SLAB,
        AllBlocks.SMALL_CRIMSITE_BRICK_SLAB
    );
    public static final BlockItem SMALL_CRIMSITE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_CRIMSITE_BRICK_WALL,
        AllBlocks.SMALL_CRIMSITE_BRICK_WALL
    );
    public static final BlockItem LAYERED_CRIMSITE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_CRIMSITE,
        AllBlocks.LAYERED_CRIMSITE
    );
    public static final BlockItem CRIMSITE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.CRIMSITE_PILLAR,
        AllBlocks.CRIMSITE_PILLAR
    );
    public static final BlockItem LIMESTONE = (BlockItem) registerBlock(AllBlockItemIds.LIMESTONE, AllBlocks.LIMESTONE);
    public static final BlockItem CUT_LIMESTONE = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE,
        AllBlocks.CUT_LIMESTONE
    );
    public static final BlockItem CUT_LIMESTONE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_STAIRS,
        AllBlocks.CUT_LIMESTONE_STAIRS
    );
    public static final BlockItem CUT_LIMESTONE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_SLAB,
        AllBlocks.CUT_LIMESTONE_SLAB
    );
    public static final BlockItem CUT_LIMESTONE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_WALL,
        AllBlocks.CUT_LIMESTONE_WALL
    );
    public static final BlockItem POLISHED_CUT_LIMESTONE = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE,
        AllBlocks.POLISHED_CUT_LIMESTONE
    );
    public static final BlockItem POLISHED_CUT_LIMESTONE_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE_STAIRS,
        AllBlocks.POLISHED_CUT_LIMESTONE_STAIRS
    );
    public static final BlockItem POLISHED_CUT_LIMESTONE_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE_SLAB,
        AllBlocks.POLISHED_CUT_LIMESTONE_SLAB
    );
    public static final BlockItem POLISHED_CUT_LIMESTONE_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_LIMESTONE_WALL,
        AllBlocks.POLISHED_CUT_LIMESTONE_WALL
    );
    public static final BlockItem CUT_LIMESTONE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_BRICKS,
        AllBlocks.CUT_LIMESTONE_BRICKS
    );
    public static final BlockItem CUT_LIMESTONE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_BRICK_STAIRS,
        AllBlocks.CUT_LIMESTONE_BRICK_STAIRS
    );
    public static final BlockItem CUT_LIMESTONE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_BRICK_SLAB,
        AllBlocks.CUT_LIMESTONE_BRICK_SLAB
    );
    public static final BlockItem CUT_LIMESTONE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_LIMESTONE_BRICK_WALL,
        AllBlocks.CUT_LIMESTONE_BRICK_WALL
    );
    public static final BlockItem SMALL_LIMESTONE_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_LIMESTONE_BRICKS,
        AllBlocks.SMALL_LIMESTONE_BRICKS
    );
    public static final BlockItem SMALL_LIMESTONE_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_LIMESTONE_BRICK_STAIRS,
        AllBlocks.SMALL_LIMESTONE_BRICK_STAIRS
    );
    public static final BlockItem SMALL_LIMESTONE_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_LIMESTONE_BRICK_SLAB,
        AllBlocks.SMALL_LIMESTONE_BRICK_SLAB
    );
    public static final BlockItem SMALL_LIMESTONE_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_LIMESTONE_BRICK_WALL,
        AllBlocks.SMALL_LIMESTONE_BRICK_WALL
    );
    public static final BlockItem LAYERED_LIMESTONE = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_LIMESTONE,
        AllBlocks.LAYERED_LIMESTONE
    );
    public static final BlockItem LIMESTONE_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.LIMESTONE_PILLAR,
        AllBlocks.LIMESTONE_PILLAR
    );
    public static final BlockItem OCHRUM = (BlockItem) registerBlock(AllBlockItemIds.OCHRUM, AllBlocks.OCHRUM);
    public static final BlockItem CUT_OCHRUM = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM,
        AllBlocks.CUT_OCHRUM
    );
    public static final BlockItem CUT_OCHRUM_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_STAIRS,
        AllBlocks.CUT_OCHRUM_STAIRS
    );
    public static final BlockItem CUT_OCHRUM_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_SLAB,
        AllBlocks.CUT_OCHRUM_SLAB
    );
    public static final BlockItem CUT_OCHRUM_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_WALL,
        AllBlocks.CUT_OCHRUM_WALL
    );
    public static final BlockItem POLISHED_CUT_OCHRUM = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_OCHRUM,
        AllBlocks.POLISHED_CUT_OCHRUM
    );
    public static final BlockItem POLISHED_CUT_OCHRUM_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_OCHRUM_STAIRS,
        AllBlocks.POLISHED_CUT_OCHRUM_STAIRS
    );
    public static final BlockItem POLISHED_CUT_OCHRUM_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_OCHRUM_SLAB,
        AllBlocks.POLISHED_CUT_OCHRUM_SLAB
    );
    public static final BlockItem POLISHED_CUT_OCHRUM_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_OCHRUM_WALL,
        AllBlocks.POLISHED_CUT_OCHRUM_WALL
    );
    public static final BlockItem CUT_OCHRUM_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_BRICKS,
        AllBlocks.CUT_OCHRUM_BRICKS
    );
    public static final BlockItem CUT_OCHRUM_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_BRICK_STAIRS,
        AllBlocks.CUT_OCHRUM_BRICK_STAIRS
    );
    public static final BlockItem CUT_OCHRUM_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_BRICK_SLAB,
        AllBlocks.CUT_OCHRUM_BRICK_SLAB
    );
    public static final BlockItem CUT_OCHRUM_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_OCHRUM_BRICK_WALL,
        AllBlocks.CUT_OCHRUM_BRICK_WALL
    );
    public static final BlockItem SMALL_OCHRUM_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_OCHRUM_BRICKS,
        AllBlocks.SMALL_OCHRUM_BRICKS
    );
    public static final BlockItem SMALL_OCHRUM_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_OCHRUM_BRICK_STAIRS,
        AllBlocks.SMALL_OCHRUM_BRICK_STAIRS
    );
    public static final BlockItem SMALL_OCHRUM_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_OCHRUM_BRICK_SLAB,
        AllBlocks.SMALL_OCHRUM_BRICK_SLAB
    );
    public static final BlockItem SMALL_OCHRUM_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_OCHRUM_BRICK_WALL,
        AllBlocks.SMALL_OCHRUM_BRICK_WALL
    );
    public static final BlockItem LAYERED_OCHRUM = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_OCHRUM,
        AllBlocks.LAYERED_OCHRUM
    );
    public static final BlockItem OCHRUM_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.OCHRUM_PILLAR,
        AllBlocks.OCHRUM_PILLAR
    );
    public static final BlockItem SCORIA = (BlockItem) registerBlock(AllBlockItemIds.SCORIA, AllBlocks.SCORIA);
    public static final BlockItem CUT_SCORIA = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA,
        AllBlocks.CUT_SCORIA
    );
    public static final BlockItem CUT_SCORIA_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_STAIRS,
        AllBlocks.CUT_SCORIA_STAIRS
    );
    public static final BlockItem CUT_SCORIA_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_SLAB,
        AllBlocks.CUT_SCORIA_SLAB
    );
    public static final BlockItem CUT_SCORIA_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_WALL,
        AllBlocks.CUT_SCORIA_WALL
    );
    public static final BlockItem POLISHED_CUT_SCORIA = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORIA,
        AllBlocks.POLISHED_CUT_SCORIA
    );
    public static final BlockItem POLISHED_CUT_SCORIA_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORIA_STAIRS,
        AllBlocks.POLISHED_CUT_SCORIA_STAIRS
    );
    public static final BlockItem POLISHED_CUT_SCORIA_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORIA_SLAB,
        AllBlocks.POLISHED_CUT_SCORIA_SLAB
    );
    public static final BlockItem POLISHED_CUT_SCORIA_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORIA_WALL,
        AllBlocks.POLISHED_CUT_SCORIA_WALL
    );
    public static final BlockItem CUT_SCORIA_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_BRICKS,
        AllBlocks.CUT_SCORIA_BRICKS
    );
    public static final BlockItem CUT_SCORIA_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_BRICK_STAIRS,
        AllBlocks.CUT_SCORIA_BRICK_STAIRS
    );
    public static final BlockItem CUT_SCORIA_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_BRICK_SLAB,
        AllBlocks.CUT_SCORIA_BRICK_SLAB
    );
    public static final BlockItem CUT_SCORIA_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORIA_BRICK_WALL,
        AllBlocks.CUT_SCORIA_BRICK_WALL
    );
    public static final BlockItem SMALL_SCORIA_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORIA_BRICKS,
        AllBlocks.SMALL_SCORIA_BRICKS
    );
    public static final BlockItem SMALL_SCORIA_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORIA_BRICK_STAIRS,
        AllBlocks.SMALL_SCORIA_BRICK_STAIRS
    );
    public static final BlockItem SMALL_SCORIA_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORIA_BRICK_SLAB,
        AllBlocks.SMALL_SCORIA_BRICK_SLAB
    );
    public static final BlockItem SMALL_SCORIA_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORIA_BRICK_WALL,
        AllBlocks.SMALL_SCORIA_BRICK_WALL
    );
    public static final BlockItem LAYERED_SCORIA = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_SCORIA,
        AllBlocks.LAYERED_SCORIA
    );
    public static final BlockItem SCORIA_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.SCORIA_PILLAR,
        AllBlocks.SCORIA_PILLAR
    );
    public static final BlockItem SCORCHIA = (BlockItem) registerBlock(AllBlockItemIds.SCORCHIA, AllBlocks.SCORCHIA);
    public static final BlockItem CUT_SCORCHIA = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA,
        AllBlocks.CUT_SCORCHIA
    );
    public static final BlockItem CUT_SCORCHIA_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_STAIRS,
        AllBlocks.CUT_SCORCHIA_STAIRS
    );
    public static final BlockItem CUT_SCORCHIA_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_SLAB,
        AllBlocks.CUT_SCORCHIA_SLAB
    );
    public static final BlockItem CUT_SCORCHIA_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_WALL,
        AllBlocks.CUT_SCORCHIA_WALL
    );
    public static final BlockItem POLISHED_CUT_SCORCHIA = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA,
        AllBlocks.POLISHED_CUT_SCORCHIA
    );
    public static final BlockItem POLISHED_CUT_SCORCHIA_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA_STAIRS,
        AllBlocks.POLISHED_CUT_SCORCHIA_STAIRS
    );
    public static final BlockItem POLISHED_CUT_SCORCHIA_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA_SLAB,
        AllBlocks.POLISHED_CUT_SCORCHIA_SLAB
    );
    public static final BlockItem POLISHED_CUT_SCORCHIA_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_SCORCHIA_WALL,
        AllBlocks.POLISHED_CUT_SCORCHIA_WALL
    );
    public static final BlockItem CUT_SCORCHIA_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_BRICKS,
        AllBlocks.CUT_SCORCHIA_BRICKS
    );
    public static final BlockItem CUT_SCORCHIA_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_BRICK_STAIRS,
        AllBlocks.CUT_SCORCHIA_BRICK_STAIRS
    );
    public static final BlockItem CUT_SCORCHIA_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_BRICK_SLAB,
        AllBlocks.CUT_SCORCHIA_BRICK_SLAB
    );
    public static final BlockItem CUT_SCORCHIA_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_SCORCHIA_BRICK_WALL,
        AllBlocks.CUT_SCORCHIA_BRICK_WALL
    );
    public static final BlockItem SMALL_SCORCHIA_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORCHIA_BRICKS,
        AllBlocks.SMALL_SCORCHIA_BRICKS
    );
    public static final BlockItem SMALL_SCORCHIA_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORCHIA_BRICK_STAIRS,
        AllBlocks.SMALL_SCORCHIA_BRICK_STAIRS
    );
    public static final BlockItem SMALL_SCORCHIA_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORCHIA_BRICK_SLAB,
        AllBlocks.SMALL_SCORCHIA_BRICK_SLAB
    );
    public static final BlockItem SMALL_SCORCHIA_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_SCORCHIA_BRICK_WALL,
        AllBlocks.SMALL_SCORCHIA_BRICK_WALL
    );
    public static final BlockItem LAYERED_SCORCHIA = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_SCORCHIA,
        AllBlocks.LAYERED_SCORCHIA
    );
    public static final BlockItem SCORCHIA_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.SCORCHIA_PILLAR,
        AllBlocks.SCORCHIA_PILLAR
    );
    public static final BlockItem VERIDIUM = (BlockItem) registerBlock(AllBlockItemIds.VERIDIUM, AllBlocks.VERIDIUM);
    public static final BlockItem CUT_VERIDIUM = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM,
        AllBlocks.CUT_VERIDIUM
    );
    public static final BlockItem CUT_VERIDIUM_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_STAIRS,
        AllBlocks.CUT_VERIDIUM_STAIRS
    );
    public static final BlockItem CUT_VERIDIUM_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_SLAB,
        AllBlocks.CUT_VERIDIUM_SLAB
    );
    public static final BlockItem CUT_VERIDIUM_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_WALL,
        AllBlocks.CUT_VERIDIUM_WALL
    );
    public static final BlockItem POLISHED_CUT_VERIDIUM = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM,
        AllBlocks.POLISHED_CUT_VERIDIUM
    );
    public static final BlockItem POLISHED_CUT_VERIDIUM_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM_STAIRS,
        AllBlocks.POLISHED_CUT_VERIDIUM_STAIRS
    );
    public static final BlockItem POLISHED_CUT_VERIDIUM_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM_SLAB,
        AllBlocks.POLISHED_CUT_VERIDIUM_SLAB
    );
    public static final BlockItem POLISHED_CUT_VERIDIUM_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.POLISHED_CUT_VERIDIUM_WALL,
        AllBlocks.POLISHED_CUT_VERIDIUM_WALL
    );
    public static final BlockItem CUT_VERIDIUM_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_BRICKS,
        AllBlocks.CUT_VERIDIUM_BRICKS
    );
    public static final BlockItem CUT_VERIDIUM_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_BRICK_STAIRS,
        AllBlocks.CUT_VERIDIUM_BRICK_STAIRS
    );
    public static final BlockItem CUT_VERIDIUM_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_BRICK_SLAB,
        AllBlocks.CUT_VERIDIUM_BRICK_SLAB
    );
    public static final BlockItem CUT_VERIDIUM_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.CUT_VERIDIUM_BRICK_WALL,
        AllBlocks.CUT_VERIDIUM_BRICK_WALL
    );
    public static final BlockItem SMALL_VERIDIUM_BRICKS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_VERIDIUM_BRICKS,
        AllBlocks.SMALL_VERIDIUM_BRICKS
    );
    public static final BlockItem SMALL_VERIDIUM_BRICK_STAIRS = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_VERIDIUM_BRICK_STAIRS,
        AllBlocks.SMALL_VERIDIUM_BRICK_STAIRS
    );
    public static final BlockItem SMALL_VERIDIUM_BRICK_SLAB = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_VERIDIUM_BRICK_SLAB,
        AllBlocks.SMALL_VERIDIUM_BRICK_SLAB
    );
    public static final BlockItem SMALL_VERIDIUM_BRICK_WALL = (BlockItem) registerBlock(
        AllBlockItemIds.SMALL_VERIDIUM_BRICK_WALL,
        AllBlocks.SMALL_VERIDIUM_BRICK_WALL
    );
    public static final BlockItem LAYERED_VERIDIUM = (BlockItem) registerBlock(
        AllBlockItemIds.LAYERED_VERIDIUM,
        AllBlocks.LAYERED_VERIDIUM
    );
    public static final BlockItem VERIDIUM_PILLAR = (BlockItem) registerBlock(
        AllBlockItemIds.VERIDIUM_PILLAR,
        AllBlocks.VERIDIUM_PILLAR
    );
    public static final BlockItem COPYCAT_STEP = (BlockItem) registerBlock(
        AllBlockItemIds.COPYCAT_STEP,
        AllBlocks.COPYCAT_STEP
    );
    public static final BlockItem COPYCAT_PANEL = (BlockItem) registerBlock(
        AllBlockItemIds.COPYCAT_PANEL,
        AllBlocks.COPYCAT_PANEL
    );

    public static final BucketItem HONEY_BUCKET = AllFluidEntries.HONEY.bucket = (BucketItem) registerItem(
        AllItemIds.HONEY_BUCKET,
        AllFluids::honeyBucket,
        new Properties().craftRemainder(Items.BUCKET).stacksTo(1)
    );
    public static final BucketItem CHOCOLATE_BUCKET = (BucketItem) registerItem(
        AllItemIds.CHOCOLATE_BUCKET,
        AllFluids::chocolateBucket,
        new Properties().craftRemainder(Items.BUCKET).stacksTo(1)
    );

    public static final VerticalGearboxItem VERTICAL_GEARBOX = (VerticalGearboxItem) registerItem(
        AllItemIds.VERTICAL_GEARBOX,
        VerticalGearboxItem::new
    );
    public static final GogglesItem GOGGLES = (GogglesItem) registerItem(
        AllItemIds.GOGGLES,
        GogglesItem::new,
        new Properties().stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())
    );
    public static final Item BRASS_HAND = registerItem(AllItemIds.BRASS_HAND);
    public static final Item ANDESITE_ALLOY = registerItem(AllItemIds.ANDESITE_ALLOY);
    public static final WrenchItem WRENCH = (WrenchItem) registerItem(
        AllItemIds.WRENCH,
        WrenchItem::new,
        new Properties().stacksTo(1)
    );
    public static final BeltConnectorItem BELT_CONNECTOR = (BeltConnectorItem) registerItem(
        AllItemIds.BELT_CONNECTOR,
        BeltConnectorItem::new
    );
    public static final SuperGlueItem SUPER_GLUE = (SuperGlueItem) registerItem(
        AllItemIds.SUPER_GLUE,
        SuperGlueItem::new,
        new Properties().durability(99)
    );
    public static final BlazeBurnerBlockItem EMPTY_BLAZE_BURNER = (BlazeBurnerBlockItem) registerItem(
        AllItemIds.EMPTY_BLAZE_BURNER,
        BlazeBurnerBlockItem::empty
    );
    public static final BuildersTeaItem BUILDERS_TEA = (BuildersTeaItem) registerItem(
        AllItemIds.BUILDERS_TEA, BuildersTeaItem::new, new Properties().stacksTo(16).food(
            new Builder().nutrition(1).saturationModifier(0.6F).alwaysEdible().build(),
            Consumable.builder().consumeSeconds(2.1F).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK)
                .hasConsumeParticles(false).onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(
                    MobEffects.HASTE,
                    3 * 60 * 20,
                    0,
                    false,
                    false
                ))).build()
        )
    );
    public static final Item BLAZE_CAKE_BASE = registerItem(AllItemIds.BLAZE_CAKE_BASE);
    public static final Item BLAZE_CAKE = registerItem(AllItemIds.BLAZE_CAKE);
    public static final Item CREATIVE_BLAZE_CAKE = registerItem(
        AllItemIds.CREATIVE_BLAZE_CAKE,
        new Properties().rarity(Rarity.EPIC)
    );
    public static final Item COPPER_SHEET = registerItem(AllItemIds.COPPER_SHEET);
    public static final Item BRASS_SHEET = registerItem(AllItemIds.BRASS_SHEET);
    public static final Item IRON_SHEET = registerItem(AllItemIds.IRON_SHEET);
    public static final Item GOLDEN_SHEET = registerItem(AllItemIds.GOLDEN_SHEET);
    public static final Item PULP = registerItem(AllItemIds.PULP);
    public static final Item CARDBOARD = registerItem(AllItemIds.CARDBOARD);
    public static final Item BRASS_INGOT = registerItem(AllItemIds.BRASS_INGOT);
    public static final Item WHEAT_FLOUR = registerItem(AllItemIds.WHEAT_FLOUR);
    public static final Item DOUGH = registerItem(AllItemIds.DOUGH);
    public static final Item PROPELLER = registerItem(AllItemIds.PROPELLER);
    public static final Item ZINC_NUGGET = registerItem(AllItemIds.ZINC_NUGGET);
    public static final Item BRASS_NUGGET = registerItem(AllItemIds.BRASS_NUGGET);
    public static final Item CRUSHED_IRON = registerItem(AllItemIds.CRUSHED_IRON);
    public static final Item CRUSHED_GOLD = registerItem(AllItemIds.CRUSHED_GOLD);
    public static final Item CRUSHED_COPPER = registerItem(AllItemIds.CRUSHED_COPPER);
    public static final Item CRUSHED_ZINC = registerItem(AllItemIds.CRUSHED_ZINC);
    public static final ListFilterItem FILTER = (ListFilterItem) registerItem(AllItemIds.FILTER, FilterItem::regular);
    public static final AttributeFilterItem ATTRIBUTE_FILTER = (AttributeFilterItem) registerItem(
        AllItemIds.ATTRIBUTE_FILTER,
        FilterItem::attribute
    );
    public static final PackageFilterItem PACKAGE_FILTER = (PackageFilterItem) registerItem(
        AllItemIds.PACKAGE_FILTER,
        FilterItem::address
    );
    public static final MinecartCouplingItem MINECART_COUPLING = (MinecartCouplingItem) registerItem(
        AllItemIds.MINECART_COUPLING,
        MinecartCouplingItem::new
    );
    public static final MinecartContraptionItem MINECART_CONTRAPTION = (MinecartContraptionItem) registerItem(
        AllItemIds.MINECART_CONTRAPTION,
        MinecartContraptionItem::rideable
    );
    public static final MinecartContraptionItem FURNACE_MINECART_CONTRAPTION = (MinecartContraptionItem) registerItem(AllItemIds.FURNACE_MINECART_CONTRAPTION,
        MinecartContraptionItem::furnace
    );
    public static final MinecartContraptionItem CHEST_MINECART_CONTRAPTION = (MinecartContraptionItem) registerItem(AllItemIds.CHEST_MINECART_CONTRAPTION,
        MinecartContraptionItem::chest
    );
    public static final Item CINDER_FLOUR = registerItem(AllItemIds.CINDER_FLOUR);
    public static final Item BAR_OF_CHOCOLATE = registerItem(
        AllItemIds.BAR_OF_CHOCOLATE,
        new Properties().food(new Builder().nutrition(6).saturationModifier(0.3F).build())
    );
    public static final Item ZINC_INGOT = registerItem(AllItemIds.ZINC_INGOT);
    public static final Item STURDY_SHEET = registerItem(AllItemIds.STURDY_SHEET);
    public static final Item CHOCOLATE_BERRIES = registerItem(
        AllItemIds.CHOCOLATE_BERRIES,
        new Properties().food(new Builder().nutrition(7).saturationModifier(0.8F).build())
    );
    public static final Item HONEYED_APPLE = registerItem(
        AllItemIds.HONEYED_APPLE,
        new Properties().food(new Builder().nutrition(8).saturationModifier(0.8F).build())
    );
    public static final Item SWEET_ROLL = registerItem(
        AllItemIds.SWEET_ROLL,
        new Properties().food(new Builder().nutrition(6).saturationModifier(0.8F).build())
    );
    public static final DivingHelmetItem COPPER_DIVING_HELMET = (DivingHelmetItem) registerItem(
        AllItemIds.COPPER_DIVING_HELMET,
        DivingHelmetItem::new,
        new Properties().durability(ArmorType.HELMET.getDurability(AllArmorMaterials.COPPER.durability()))
            .attributes(DivingHelmetItem.createAttributeModifiers(AllArmorMaterials.COPPER))
            .enchantable(AllArmorMaterials.COPPER.enchantmentValue()).component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.HEAD).setEquipSound(AllArmorMaterials.COPPER.equipSound())
                    .setAsset(AllArmorMaterials.COPPER.assetId()).build()
            ).repairable(AllArmorMaterials.COPPER.repairIngredient())
    );
    public static final BacktankItem COPPER_BACKTANK = (BacktankItem) registerItem(
        AllItemIds.COPPER_BACKTANK,
        BacktankItem::copper,
        AllArmorMaterials.chest(AllArmorMaterials.COPPER)
    );
    public static final DivingBootsItem COPPER_DIVING_BOOTS = (DivingBootsItem) registerItem(
        AllItemIds.COPPER_DIVING_BOOTS,
        DivingBootsItem::new,
        new Properties().humanoidArmor(AllArmorMaterials.COPPER, ArmorType.BOOTS)
    );
    public static final DivingHelmetItem NETHERITE_DIVING_HELMET = (DivingHelmetItem) registerItem(
        AllItemIds.NETHERITE_DIVING_HELMET,
        DivingHelmetItem::new,
        new Properties().durability(ArmorType.HELMET.getDurability(AllArmorMaterials.NETHERITE.durability()))
            .attributes(DivingHelmetItem.createAttributeModifiers(AllArmorMaterials.NETHERITE))
            .enchantable(AllArmorMaterials.NETHERITE.enchantmentValue()).component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.HEAD).setEquipSound(AllArmorMaterials.NETHERITE.equipSound())
                    .setAsset(AllArmorMaterials.NETHERITE.assetId()).build()
            ).repairable(AllArmorMaterials.NETHERITE.repairIngredient()).fireResistant()
    );
    public static final BacktankItem NETHERITE_BACKTANK = (BacktankItem) registerItem(
        AllItemIds.NETHERITE_BACKTANK,
        BacktankItem::netherite,
        AllArmorMaterials.chest(AllArmorMaterials.NETHERITE).fireResistant()
    );
    public static final DivingBootsItem NETHERITE_DIVING_BOOTS = (DivingBootsItem) registerItem(
        AllItemIds.NETHERITE_DIVING_BOOTS,
        DivingBootsItem::new,
        new Properties().humanoidArmor(AllArmorMaterials.NETHERITE, ArmorType.BOOTS).fireResistant()
    );
    public static final Item ROSE_QUARTZ = registerItem(AllItemIds.ROSE_QUARTZ);
    public static final Item POLISHED_ROSE_QUARTZ = registerItem(AllItemIds.POLISHED_ROSE_QUARTZ);
    public static final SandPaperItem SAND_PAPER = (SandPaperItem) registerItem(
        AllItemIds.SAND_PAPER,
        SandPaperItem::new,
        new Properties().durability(8).enchantable(1)
    );
    public static final SandPaperItem RED_SAND_PAPER = (SandPaperItem) registerItem(
        AllItemIds.RED_SAND_PAPER,
        SandPaperItem::new,
        new Properties().durability(8).enchantable(1)
    );
    public static final Item PRECISION_MECHANISM = registerItem(AllItemIds.PRECISION_MECHANISM);
    public static final Item POWDERED_OBSIDIAN = registerItem(AllItemIds.POWDERED_OBSIDIAN);
    public static final Item WHISK = registerItem(AllItemIds.WHISK);
    public static final Item CRAFTER_SLOT_COVER = registerItem(AllItemIds.CRAFTER_SLOT_COVER);
    public static final Item ELECTRON_TUBE = registerItem(AllItemIds.ELECTRON_TUBE);
    public static final Item TRANSMITTER = registerItem(AllItemIds.TRANSMITTER);
    public static final Item RAW_ZINC = registerItem(AllItemIds.RAW_ZINC);
    public static final SequencedAssemblyItem INCOMPLETE_PRECISION_MECHANISM = (SequencedAssemblyItem) registerItem(AllItemIds.INCOMPLETE_PRECISION_MECHANISM,
        SequencedAssemblyItem::new,
        new Properties().stacksTo(1)
    );
    public static final SequencedAssemblyItem INCOMPLETE_REINFORCED_SHEET = (SequencedAssemblyItem) registerItem(AllItemIds.INCOMPLETE_REINFORCED_SHEET,
        SequencedAssemblyItem::new,
        new Properties().stacksTo(1)
    );
    public static final SequencedAssemblyItem INCOMPLETE_TRACK = (SequencedAssemblyItem) registerItem(
        AllItemIds.INCOMPLETE_TRACK,
        SequencedAssemblyItem::new,
        new Properties().stacksTo(1)
    );
    public static final ExperienceNuggetItem EXP_NUGGET = (ExperienceNuggetItem) registerItem(
        AllItemIds.EXP_NUGGET,
        ExperienceNuggetItem::new,
        new Properties().rarity(Rarity.UNCOMMON)
    );
    public static final ScheduleItem SCHEDULE = (ScheduleItem) registerItem(AllItemIds.SCHEDULE, ScheduleItem::new);
    public static final PotatoCannonItem POTATO_CANNON = (PotatoCannonItem) registerItem(
        AllItemIds.POTATO_CANNON,
        PotatoCannonItem::new,
        new Properties().durability(100).enchantable(1)
    );
    public static final ExtendoGripItem EXTENDO_GRIP = (ExtendoGripItem) registerItem(
        AllItemIds.EXTENDO_GRIP,
        ExtendoGripItem::new,
        new Properties().rarity(Rarity.UNCOMMON).durability(200).attributes(ExtendoGripItem.rangeModifier)
    );
    public static final LinkedControllerItem LINKED_CONTROLLER = (LinkedControllerItem) registerItem(
        AllItemIds.LINKED_CONTROLLER,
        LinkedControllerItem::new,
        new Properties().stacksTo(1)
    );
    public static final PackageItem CARDBOARD_PACKAGE_12X12 = (PackageItem) registerItem(
        AllItemIds.CARDBOARD_PACKAGE_12X12,
        PackageItem.styled(AllPackageStyles.CARDBOARD_12X12),
        new Properties().stacksTo(1).overrideDescription("item.create.package")
    );
    public static final PackageItem CARDBOARD_PACKAGE_10X12 = (PackageItem) registerItem(
        AllItemIds.CARDBOARD_PACKAGE_10X12,
        PackageItem.styled(AllPackageStyles.CARDBOARD_10X12),
        new Properties().stacksTo(1).overrideDescription("item.create.package")
    );
    public static final PackageItem CARDBOARD_PACKAGE_10X8 = (PackageItem) registerItem(
        AllItemIds.CARDBOARD_PACKAGE_10X8,
        PackageItem.styled(AllPackageStyles.CARDBOARD_10X8),
        new Properties().stacksTo(1).overrideDescription("item.create.package")
    );
    public static final PackageItem CARDBOARD_PACKAGE_12X10 = (PackageItem) registerItem(
        AllItemIds.CARDBOARD_PACKAGE_12X10,
        PackageItem.styled(AllPackageStyles.CARDBOARD_12X10),
        new Properties().stacksTo(1).overrideDescription("item.create.package")
    );
    public static final PackageItem RARE_CREEPER_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_CREEPER_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_CREEPER),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_DARCY_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_DARCY_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_DARCY),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_EVAN_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_EVAN_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_EVAN),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_JINX_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_JINX_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_JINX),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_KRYPPERS_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_KRYPPERS_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_KRYPPERS),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_SIMI_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_SIMI_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_SIMI),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_STARLOTTE_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_STARLOTTE_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_STARLOTTE),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_THUNDER_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_THUNDER_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_THUNDER),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_UP_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_UP_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_UP),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final PackageItem RARE_VECTOR_PACKAGE = (PackageItem) registerItem(
        AllItemIds.RARE_VECTOR_PACKAGE,
        PackageItem.styled(AllPackageStyles.RARE_VECTOR),
        new Properties().stacksTo(1).overrideDescription("item.create.rare_package")
    );
    public static final ShoppingListItem SHOPPING_LIST = (ShoppingListItem) registerItem(
        AllItemIds.SHOPPING_LIST,
        ShoppingListItem::new,
        new Properties().stacksTo(1)
    );
    public static final CardboardArmorItem CARDBOARD_HELMET = (CardboardArmorItem) registerItem(
        AllItemIds.CARDBOARD_HELMET,
        CardboardArmorItem::new,
        new Properties().durability(ArmorType.HELMET.getDurability(AllArmorMaterials.CARDBOARD.durability()))
            .attributes(AllArmorMaterials.CARDBOARD.createAttributes(ArmorType.HELMET))
            .enchantable(AllArmorMaterials.CARDBOARD.enchantmentValue()).component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(ArmorType.HELMET.getSlot()).setEquipSound(AllArmorMaterials.CARDBOARD.equipSound())
                    .setAsset(AllArmorMaterials.CARDBOARD.assetId())
                    .setCameraOverlay(Identifier.fromNamespaceAndPath(MOD_ID, "misc/package_blur")).build()
            ).repairable(AllArmorMaterials.CARDBOARD.repairIngredient())
    );
    public static final CardboardArmorItem CARDBOARD_CHESTPLATE = (CardboardArmorItem) registerItem(
        AllItemIds.CARDBOARD_CHESTPLATE,
        CardboardArmorItem::new,
        new Properties().humanoidArmor(AllArmorMaterials.CARDBOARD, ArmorType.CHESTPLATE)
    );
    public static final CardboardArmorItem CARDBOARD_LEGGINGS = (CardboardArmorItem) registerItem(
        AllItemIds.CARDBOARD_LEGGINGS,
        CardboardArmorItem::new,
        new Properties().humanoidArmor(AllArmorMaterials.CARDBOARD, ArmorType.LEGGINGS)
    );
    public static final CardboardArmorItem CARDBOARD_BOOTS = (CardboardArmorItem) registerItem(
        AllItemIds.CARDBOARD_BOOTS,
        CardboardArmorItem::new,
        new Properties().humanoidArmor(AllArmorMaterials.CARDBOARD, ArmorType.BOOTS)
    );
    @SuppressWarnings("deprecation")
    public static final CardboardSwordItem CARDBOARD_SWORD = (CardboardSwordItem) registerItem(
        AllItemIds.CARDBOARD_SWORD,
        CardboardSwordItem::new,
        new Properties().durability(AllToolMaterials.CARDBOARD.durability())
            .repairable(AllToolMaterials.CARDBOARD.repairItems())
            .enchantable(AllToolMaterials.CARDBOARD.enchantmentValue()).component(
                DataComponents.TOOL, new Tool(
                    List.of(
                        Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                        Rule.overrideSpeed(
                            BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK)
                                .getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE
                        ),
                        Rule.overrideSpeed(
                            BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK)
                                .getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F
                        )
                    ), 1.0F, 2, false
                )
            ).attributes(ItemAttributeModifiers.builder().add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    Item.BASE_ATTACK_DAMAGE_ID,
                    3.0F + AllToolMaterials.CARDBOARD.attackDamageBonus(),
                    Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            ).add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, 1.0F, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            ).add(Attributes.ATTACK_KNOCKBACK, CardboardSwordItem.KNOCKBACK_MODIFIER, EquipmentSlotGroup.MAINHAND).build())
            .component(DataComponents.WEAPON, new Weapon(1))
    );
    public static final BlueprintItem CRAFTING_BLUEPRINT = (BlueprintItem) registerItem(
        AllItemIds.CRAFTING_BLUEPRINT,
        BlueprintItem::new
    );
    public static final TreeFertilizerItem TREE_FERTILIZER = (TreeFertilizerItem) registerItem(
        AllItemIds.TREE_FERTILIZER,
        TreeFertilizerItem::new
    );
    public static final SymmetryWandItem WAND_OF_SYMMETRY = (SymmetryWandItem) registerItem(
        AllItemIds.WAND_OF_SYMMETRY,
        SymmetryWandItem::new,
        new Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );
    public static final Item EMPTY_SCHEMATIC = registerItem(AllItemIds.EMPTY_SCHEMATIC, new Properties().stacksTo(1));
    public static final SchematicAndQuillItem SCHEMATIC_AND_QUILL = (SchematicAndQuillItem) registerItem(
        AllItemIds.SCHEMATIC_AND_QUILL,
        SchematicAndQuillItem::new,
        new Properties().stacksTo(1)
    );
    public static final SchematicItem SCHEMATIC = (SchematicItem) registerItem(
        AllItemIds.SCHEMATIC,
        SchematicItem::new,
        new Properties().stacksTo(1)
    );
    public static final WorldshaperItem WORLDSHAPER = (WorldshaperItem) registerItem(
        AllItemIds.WORLDSHAPER,
        WorldshaperItem::new,
        new Properties().stacksTo(1).rarity(Rarity.EPIC)
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_OSMIUM = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_OSMIUM,
        TagDependentIngredientItem.tag("ores/osmium")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_PLATINUM = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_PLATINUM,
        TagDependentIngredientItem.tag("ores/platinum")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_SILVER = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_SILVER,
        TagDependentIngredientItem.tag("ores/silver")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_TIN = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_TIN,
        TagDependentIngredientItem.tag("ores/tin")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_LEAD = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_LEAD,
        TagDependentIngredientItem.tag("ores/lead")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_QUICKSILVER = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_QUICKSILVER,
        TagDependentIngredientItem.tag("ores/quicksilver")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_ALUMINUM = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_ALUMINUM,
        TagDependentIngredientItem.tag("ores/aluminum")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_URANIUM = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_URANIUM,
        TagDependentIngredientItem.tag("ores/uranium")
    );
    public static final TagDependentIngredientItem CRUSHED_RAW_NICKEL = (TagDependentIngredientItem) registerItem(AllItemIds.CRUSHED_RAW_NICKEL,
        TagDependentIngredientItem.tag("ores/nickel")
    );

    public static Item registerBlockIgnoreColor(BlockItemId id, Block block, DyeColor dyeColor) {
        return registerBlock(id, block);
    }

    public static TriFunction<BlockItemId, Block, DyeColor, Item> registerBlockIgnoreColor(BiFunction<Block, Properties, Item> factory) {
        return (id, block, _) -> registerBlock(id, block, factory);
    }

    public static void init() {
    }
}
