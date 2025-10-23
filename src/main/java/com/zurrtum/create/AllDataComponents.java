package com.zurrtum.create;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecs;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import com.zurrtum.create.infrastructure.component.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static com.zurrtum.create.Create.MOD_ID;

public class AllDataComponents {
    public static final ComponentType<Integer> FLUID_MAX_CAPACITY = register(
        "fluid_max_capacity",
        builder -> builder.codec(Codecs.POSITIVE_INT).packetCodec(PacketCodecs.VAR_INT)
    );

    public static final ComponentType<Integer> BACKTANK_AIR = register(
        "banktank_air",
        builder -> builder.codec(Codecs.NON_NEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT)
    );

    public static final ComponentType<BlockPos> BELT_FIRST_SHAFT = register(
        "belt_first_shaft",
        builder -> builder.codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC)
    );

    public static final ComponentType<Boolean> INFERRED_FROM_RECIPE = register(
        "inferred_from_recipe",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<PlacementPatterns> PLACEMENT_PATTERN = register(
        "placement_pattern",
        builder -> builder.codec(PlacementPatterns.CODEC).packetCodec(PlacementPatterns.STREAM_CODEC)
    );

    public static final ComponentType<TerrainBrushes> SHAPER_BRUSH = register(
        "shaper_brush",
        builder -> builder.codec(TerrainBrushes.CODEC).packetCodec(TerrainBrushes.STREAM_CODEC)
    );

    public static final ComponentType<BlockPos> SHAPER_BRUSH_PARAMS = register(
        "shaper_brush_params",
        builder -> builder.codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC)
    );

    public static final ComponentType<PlacementOptions> SHAPER_PLACEMENT_OPTIONS = register(
        "shaper_placement_options",
        builder -> builder.codec(PlacementOptions.CODEC).packetCodec(PlacementOptions.STREAM_CODEC)
    );

    public static final ComponentType<TerrainTools> SHAPER_TOOL = register(
        "shaper_tool",
        builder -> builder.codec(TerrainTools.CODEC).packetCodec(TerrainTools.STREAM_CODEC)
    );

    public static final ComponentType<BlockState> SHAPER_BLOCK_USED = register(
        "shaper_block_used",
        builder -> builder.codec(BlockState.CODEC).packetCodec(PacketCodecs.entryOf(Block.STATE_IDS))
    );

    public static final ComponentType<Boolean> SHAPER_SWAP = register(
        "shaper_swap",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<NbtCompound> SHAPER_BLOCK_DATA = register(
        "shaper_block_data",
        builder -> builder.codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND)
    );

    public static final ComponentType<ContainerComponent> FILTER_ITEMS = register(
        "filter_items",
        builder -> builder.codec(ContainerComponent.CODEC).packetCodec(ContainerComponent.PACKET_CODEC)
    );

    // These 2 are placed on items inside filters and not the filter itself
    public static final ComponentType<Boolean> FILTER_ITEMS_RESPECT_NBT = register(
        "filter_items_respect_nbt",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<Boolean> FILTER_ITEMS_BLACKLIST = register(
        "filter_items_blacklist",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<AttributeFilterWhitelistMode> ATTRIBUTE_FILTER_WHITELIST_MODE = register(
        "attribute_filter_whitelist_mode",
        builder -> builder.codec(AttributeFilterWhitelistMode.CODEC).packetCodec(AttributeFilterWhitelistMode.STREAM_CODEC)
    );

    public static final ComponentType<List<ItemAttributeEntry>> ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES = register(
        "attribute_filter_matched_attributes",
        builder -> builder.codec(ItemAttributeEntry.CODEC.listOf()).packetCodec(CatnipStreamCodecBuilders.list(ItemAttributeEntry.STREAM_CODEC))
    );

    public static final ComponentType<ClipboardContent> CLIPBOARD_CONTENT = register(
        "clipboard_content",
        builder -> builder.codec(ClipboardContent.CODEC).packetCodec(ClipboardContent.STREAM_CODEC)
    );

    public static final ComponentType<ConnectingFrom> TRACK_CONNECTING_FROM = register(
        "track_connecting_from",
        builder -> builder.codec(ConnectingFrom.CODEC).packetCodec(ConnectingFrom.STREAM_CODEC)
    );

    public static final ComponentType<Boolean> TRACK_EXTENDED_CURVE = register(
        "track_extend_curve",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<BlockPos> TRACK_TARGETING_ITEM_SELECTED_POS = register(
        "track_targeting_item_selected_pos",
        builder -> builder.codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC)
    );

    public static final ComponentType<Boolean> TRACK_TARGETING_ITEM_SELECTED_DIRECTION = register(
        "track_targeting_item_selected_direction",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<BezierTrackPointLocation> TRACK_TARGETING_ITEM_BEZIER = register(
        "track_targeting_item_bezier",
        builder -> builder.codec(BezierTrackPointLocation.CODEC).packetCodec(BezierTrackPointLocation.STREAM_CODEC)
    );

    public static final ComponentType<Boolean> SCHEMATIC_DEPLOYED = register(
        "schematic_deployed",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<String> SCHEMATIC_OWNER = register(
        "schematic_owner",
        builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING)
    );

    public static final ComponentType<String> SCHEMATIC_FILE = register(
        "schematic_file",
        builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING)
    );

    public static final ComponentType<BlockPos> SCHEMATIC_ANCHOR = register(
        "schematic_anchor",
        builder -> builder.codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC)
    );

    public static final ComponentType<BlockRotation> SCHEMATIC_ROTATION = register(
        "schematic_rotation",
        builder -> builder.codec(BlockRotation.CODEC).packetCodec(BlockRotation.PACKET_CODEC)
    );

    public static final ComponentType<BlockMirror> SCHEMATIC_MIRROR = register(
        "schematic_mirror",
        builder -> builder.codec(BlockMirror.CODEC).packetCodec(CatnipStreamCodecs.MIRROR)
    );

    public static final ComponentType<Vec3i> SCHEMATIC_BOUNDS = register(
        "schematic_bounds",
        builder -> builder.codec(Vec3i.CODEC).packetCodec(Vec3i.PACKET_CODEC)
    );

    public static final ComponentType<Integer> SCHEMATIC_HASH = register(
        "schematic_hash",
        builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER)
    );

    public static final ComponentType<Integer> CHROMATIC_COMPOUND_COLLECTING_LIGHT = register(
        "chromatic_compound_collecting_light",
        builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER)
    );

    public static final ComponentType<SandPaperItemComponent> SAND_PAPER_POLISHING = register(
        "sand_paper_polishing",
        builder -> builder.codec(SandPaperItemComponent.CODEC).packetCodec(SandPaperItemComponent.STREAM_CODEC)
    );

    public static final ComponentType<Unit> SAND_PAPER_JEI = register(
        "sand_paper_jei",
        builder -> builder.codec(Unit.CODEC).packetCodec(PacketCodec.unit(Unit.INSTANCE))
    );

    // Holds contraption data when a minecraft contraption is picked up
    public static final ComponentType<NbtCompound> MINECRAFT_CONTRAPTION_DATA = register(
        "minecart_contraption_data",
        builder -> builder.codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND)
    );

    public static final ComponentType<ContainerComponent> LINKED_CONTROLLER_ITEMS = register(
        "linked_controller_items",
        builder -> builder.codec(ContainerComponent.CODEC).packetCodec(ContainerComponent.PACKET_CODEC)
    );

    public static final ComponentType<ToolboxInventory> TOOLBOX_INVENTORY = register(
        "toolbox_inventory",
        builder -> builder.codec(ToolboxInventory.CODEC).packetCodec(ToolboxInventory.STREAM_CODEC)
    );

    public static final ComponentType<UUID> TOOLBOX_UUID = register(
        "toolbox_uuid",
        builder -> builder.codec(Uuids.INT_STREAM_CODEC).packetCodec(Uuids.PACKET_CODEC)
    );

    public static final ComponentType<Float> SEQUENCED_ASSEMBLY_PROGRESS = register(
        "sequenced_assembly_progress",
        builder -> builder.codec(Codec.FLOAT).packetCodec(PacketCodecs.FLOAT)
    );

    public static final ComponentType<SequencedAssemblyJunk> SEQUENCED_ASSEMBLY_JUNK = register(
        "sequenced_assembly_junk",
        builder -> builder.codec(SequencedAssemblyJunk.CODEC).packetCodec(SequencedAssemblyJunk.PACKET_CODEC)
    );

    public static final ComponentType<NbtCompound> TRAIN_SCHEDULE = register(
        "train_schedule",
        builder -> builder.codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND)
    );

    public static final ComponentType<SymmetryMirror> SYMMETRY_WAND = register(
        "symmetry_wand",
        builder -> builder.codec(SymmetryMirror.CODEC).packetCodec(SymmetryMirror.STREAM_CODEC)
    );

    public static final ComponentType<Boolean> SYMMETRY_WAND_ENABLE = register(
        "symmetry_wand_enable",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<Boolean> SYMMETRY_WAND_SIMULATE = register(
        "symmetry_wand_simulate",
        builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN)
    );

    public static final ComponentType<BottleType> POTION_FLUID_BOTTLE_TYPE = register(
        "potion_fluid_bottle_type",
        builder -> builder.codec(BottleType.CODEC).packetCodec(BottleType.STREAM_CODEC)
    );

    public static final ComponentType<SchematicannonOptions> SCHEMATICANNON_OPTIONS = register(
        "schematicannon_options",
        builder -> builder.codec(SchematicannonOptions.CODEC).packetCodec(SchematicannonOptions.STREAM_CODEC)
    );

    public static final ComponentType<AutoRequestData> AUTO_REQUEST_DATA = register(
        "auto_request_data",
        builder -> builder.codec(AutoRequestData.CODEC).packetCodec(AutoRequestData.STREAM_CODEC)
    );

    public static final ComponentType<ShoppingList> SHOPPING_LIST = register(
        "shopping_list",
        builder -> builder.codec(ShoppingList.CODEC).packetCodec(ShoppingList.STREAM_CODEC)
    );

    public static final ComponentType<String> SHOPPING_LIST_ADDRESS = register(
        "shopping_list_address",
        builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING)
    );

    public static final ComponentType<String> PACKAGE_ADDRESS = register(
        "package_address",
        builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING)
    );

    public static final ComponentType<ContainerComponent> PACKAGE_CONTENTS = register(
        "package_contents",
        builder -> builder.codec(ContainerComponent.CODEC).packetCodec(ContainerComponent.PACKET_CODEC)
    );

    public static final ComponentType<PackageOrderData> PACKAGE_ORDER_DATA = register(
        "package_order_data",
        builder -> builder.codec(PackageOrderData.CODEC).packetCodec(PackageOrderData.STREAM_CODEC)
    );

    public static final ComponentType<PackageOrderWithCrafts> PACKAGE_ORDER_CONTEXT = register(
        "package_order_context",
        builder -> builder.codec(PackageOrderWithCrafts.CODEC).packetCodec(PackageOrderWithCrafts.STREAM_CODEC)
    );

    public static final ComponentType<ClickToLinkData> CLICK_TO_LINK_DATA = register(
        "click_to_link_data",
        builder -> builder.codec(ClickToLinkData.CODEC).packetCodec(ClickToLinkData.STREAM_CODEC)
    );

    /**
     * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.1+ Port")
    @Deprecated(since = "6.0.7", forRemoval = true)
    public static final ComponentType<ClipboardType> CLIPBOARD_TYPE = register(
        "clipboard_type",
        builder -> builder.codec(ClipboardType.CODEC).packetCodec(ClipboardType.STREAM_CODEC)
    );

    /**
     * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.1+ Port")
    @Deprecated(since = "6.0.7", forRemoval = true)
    public static final ComponentType<List<List<ClipboardEntry>>> CLIPBOARD_PAGES = register(
        "clipboard_pages",
        builder -> builder.codec(ClipboardEntry.CODEC.listOf().listOf())
            .packetCodec(CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.list(ClipboardEntry.STREAM_CODEC)))
    );

    /**
     * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.1+ Port")
    @Deprecated(since = "6.0.7", forRemoval = true)
    public static final ComponentType<Unit> CLIPBOARD_READ_ONLY = register(
        "clipboard_read_only",
        builder -> builder.codec(Unit.CODEC).packetCodec(PacketCodec.unit(Unit.INSTANCE))
    );

    /**
     * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.1+ Port")
    @Deprecated(since = "6.0.7", forRemoval = true)
    public static final ComponentType<NbtCompound> CLIPBOARD_COPIED_VALUES = register(
        "clipboard_copied_values",
        builder -> builder.codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND)
    );

    /**
     * @deprecated Use {@link AllDataComponents#CLIPBOARD_CONTENT} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.1+ Port")
    @Deprecated(since = "6.0.7", forRemoval = true)
    public static final ComponentType<Integer> CLIPBOARD_PREVIOUSLY_OPENED_PAGE = register(
        "clipboard_previously_opened_page",
        builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER)
    );

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, id), builderOperator.apply(ComponentType.builder()).build());
    }

    public static void register() {
    }
}
