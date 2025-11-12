package com.zurrtum.create;

import com.zurrtum.create.infrastructure.packet.c2s.*;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class AllPackets {
    public static final Map<PacketType<Packet<ServerGamePacketListener>>, StreamCodec<? super RegistryFriendlyByteBuf, Packet<ServerGamePacketListener>>> C2S = new LinkedHashMap<>();
    public static final Map<PacketType<Packet<ClientGamePacketListener>>, StreamCodec<? super RegistryFriendlyByteBuf, Packet<ClientGamePacketListener>>> S2C = new LinkedHashMap<>();
    public static final Map<PacketType<Packet<ClientConfigurationPacketListener>>, StreamCodec<? super RegistryFriendlyByteBuf, Packet<ClientConfigurationPacketListener>>> S2C_CONFIG = new LinkedHashMap<>();
    public static final PacketType<ConfigureSchematicannonPacket> CONFIGURE_SCHEMATICANNON = c2s(
        "configure_schematicannon",
        ConfigureSchematicannonPacket.CODEC
    );
    public static final PacketType<ConfigureThresholdSwitchPacket> CONFIGURE_STOCKSWITCH = c2s(
        "configure_stockswitch",
        ConfigureThresholdSwitchPacket.CODEC
    );
    public static final PacketType<ConfigureSequencedGearshiftPacket> CONFIGURE_SEQUENCER = c2s(
        "configure_sequencer",
        ConfigureSequencedGearshiftPacket.CODEC
    );
    public static final PacketType<SchematicPlacePacket> PLACE_SCHEMATIC = c2s("place_schematic", SchematicPlacePacket.CODEC);
    public static final PacketType<SchematicUploadPacket> UPLOAD_SCHEMATIC = c2s("upload_schematic", SchematicUploadPacket.CODEC);
    public static final C2SHoldPacket CLEAR_CONTAINER = c2s("clear_container", AllHandle::onClearContainer);
    public static final PacketType<FilterScreenPacket> CONFIGURE_FILTER = c2s("configure_filter", FilterScreenPacket.CODEC);
    public static final PacketType<ContraptionInteractionPacket> CONTRAPTION_INTERACT = c2s(
        "contraption_interact",
        ContraptionInteractionPacket.CODEC
    );
    public static final PacketType<ClientMotionPacket> CLIENT_MOTION = c2s("client_motion", ClientMotionPacket.CODEC);
    public static final PacketType<ArmPlacementPacket> PLACE_ARM = c2s("place_arm", ArmPlacementPacket.CODEC);
    public static final PacketType<PackagePortPlacementPacket> PLACE_PACKAGE_PORT = c2s("place_package_port", PackagePortPlacementPacket.CODEC);
    public static final PacketType<CouplingCreationPacket> MINECART_COUPLING_CREATION = c2s(
        "minecart_coupling_creation",
        CouplingCreationPacket.CODEC
    );
    public static final PacketType<InstantSchematicPacket> INSTANT_SCHEMATIC = c2s("instant_schematic", InstantSchematicPacket.CODEC);
    public static final PacketType<SchematicSyncPacket> SYNC_SCHEMATIC = c2s("sync_schematic", SchematicSyncPacket.CODEC);
    public static final C2SHoldPacket LEFT_CLICK = c2s("left_click", AllHandle::onLeftClick);
    public static final PacketType<EjectorPlacementPacket> PLACE_EJECTOR = c2s("place_ejector", EjectorPlacementPacket.CODEC);
    public static final PacketType<EjectorTriggerPacket> TRIGGER_EJECTOR = c2s("trigger_ejector", EjectorTriggerPacket.CODEC);
    public static final PacketType<EjectorElytraPacket> EJECTOR_ELYTRA = c2s("ejector_elytra", EjectorElytraPacket.CODEC);
    public static final PacketType<LinkedControllerInputPacket> LINKED_CONTROLLER_INPUT = c2s(
        "linked_controller_input",
        LinkedControllerInputPacket.CODEC
    );
    public static final PacketType<LinkedControllerBindPacket> LINKED_CONTROLLER_BIND = c2s(
        "linked_controller_bind",
        LinkedControllerBindPacket.CODEC
    );
    public static final PacketType<LinkedControllerStopLecternPacket> LINKED_CONTROLLER_USE_LECTERN = c2s(
        "linked_controller_use_lectern",
        LinkedControllerStopLecternPacket.CODEC
    );
    public static final PacketType<GhostItemSubmitPacket> SUBMIT_GHOST_ITEM = c2s("submit_ghost_item", GhostItemSubmitPacket.CODEC);
    public static final PacketType<BlueprintAssignCompleteRecipePacket> BLUEPRINT_COMPLETE_RECIPE = c2s(
        "blueprint_complete_recipe",
        BlueprintAssignCompleteRecipePacket.CODEC
    );
    public static final PacketType<ConfigureSymmetryWandPacket> CONFIGURE_SYMMETRY_WAND = c2s(
        "configure_symmetry_wand",
        ConfigureSymmetryWandPacket.CODEC
    );
    public static final PacketType<ConfigureWorldshaperPacket> CONFIGURE_WORLDSHAPER = c2s("configure_worldshaper", ConfigureWorldshaperPacket.CODEC);
    public static final PacketType<ToolboxEquipPacket> TOOLBOX_EQUIP = c2s("toolbox_equip", ToolboxEquipPacket.CODEC);
    public static final PacketType<ToolboxDisposeAllPacket> TOOLBOX_DISPOSE_ALL = c2s("toolbox_dispose_all", ToolboxDisposeAllPacket.CODEC);
    public static final PacketType<ScheduleEditPacket> CONFIGURE_SCHEDULE = c2s("configure_schedule", ScheduleEditPacket.CODEC);
    public static final PacketType<StationEditPacket> CONFIGURE_STATION = c2s("configure_station", StationEditPacket.CODEC);
    public static final PacketType<TrainEditPacket> C_CONFIGURE_TRAIN = c2s("c_configure_train", TrainEditPacket.CODEC);
    public static final PacketType<TrainRelocationPacket> RELOCATE_TRAIN = c2s("relocate_train", TrainRelocationPacket.CODEC);
    public static final PacketType<ControlsInputPacket> CONTROLS_INPUT = c2s("controls_input", ControlsInputPacket.CODEC);
    public static final PacketType<DisplayLinkConfigurationPacket> CONFIGURE_DATA_GATHERER = c2s(
        "configure_data_gatherer",
        DisplayLinkConfigurationPacket.CODEC
    );
    public static final PacketType<CurvedTrackDestroyPacket> DESTROY_CURVED_TRACK = c2s("destroy_curved_track", CurvedTrackDestroyPacket.CODEC);
    public static final PacketType<CurvedTrackSelectionPacket> SELECT_CURVED_TRACK = c2s("select_curved_track", CurvedTrackSelectionPacket.CODEC);
    public static final PacketType<PlaceExtendedCurvePacket> PLACE_CURVED_TRACK = c2s("place_curved_track", PlaceExtendedCurvePacket.CODEC);
    public static final PacketType<SuperGlueSelectionPacket> GLUE_IN_AREA = c2s("glue_in_area", SuperGlueSelectionPacket.CODEC);
    public static final PacketType<SuperGlueRemovalPacket> GLUE_REMOVED = c2s("glue_removed", SuperGlueRemovalPacket.CODEC);
    public static final PacketType<TrainCollisionPacket> TRAIN_COLLISION = c2s("train_collision", TrainCollisionPacket.CODEC);
    public static final PacketType<TrainHUDUpdatePacket> C_TRAIN_HUD = c2s("c_train_hud", TrainHUDUpdatePacket.CODEC);
    public static final PacketType<HonkPacket> C_TRAIN_HONK = c2s("c_train_honk", HonkPacket.CODEC);
    public static final PacketType<GaugeObservedPacket> OBSERVER_STRESSOMETER = c2s("observer_stressometer", GaugeObservedPacket.CODEC);
    public static final PacketType<EjectorAwardPacket> EJECTOR_AWARD = c2s("ejector_award", EjectorAwardPacket.CODEC);
    public static final PacketType<TrackGraphRequestPacket> TRACK_GRAPH_REQUEST = c2s("track_graph_request", TrackGraphRequestPacket.CODEC);
    public static final PacketType<ElevatorContactEditPacket> CONFIGURE_ELEVATOR_CONTACT = c2s(
        "configure_elevator_contact",
        ElevatorContactEditPacket.CODEC
    );
    public static final PacketType<RequestFloorListPacket> REQUEST_FLOOR_LIST = c2s("request_floor_list", RequestFloorListPacket.CODEC);
    public static final PacketType<ElevatorTargetFloorPacket> ELEVATOR_SET_FLOOR = c2s("elevator_set_floor", ElevatorTargetFloorPacket.CODEC);
    public static final PacketType<ValueSettingsPacket> VALUE_SETTINGS = c2s("value_settings", ValueSettingsPacket.CODEC);
    public static final PacketType<ClipboardEditPacket> CLIPBOARD_EDIT = c2s("clipboard_edit", ClipboardEditPacket.CODEC);
    public static final PacketType<ContraptionColliderLockPacketRequest> CONTRAPTION_COLLIDER_LOCK_REQUEST = c2s(
        "contraption_collider_lock_request",
        ContraptionColliderLockPacketRequest.CODEC
    );
    public static final PacketType<RadialWrenchMenuSubmitPacket> RADIAL_WRENCH_MENU_SUBMIT = c2s(
        "radial_wrench_menu_submit",
        RadialWrenchMenuSubmitPacket.CODEC
    );
    public static final PacketType<LogisticalStockRequestPacket> LOGISTICS_STOCK_REQUEST = c2s(
        "logistics_stock_request",
        LogisticalStockRequestPacket.CODEC
    );
    public static final PacketType<PackageOrderRequestPacket> LOGISTICS_PACKAGE_REQUEST = c2s(
        "logistics_package_request",
        PackageOrderRequestPacket.CODEC
    );
    public static final PacketType<ChainConveyorConnectionPacket> CHAIN_CONVEYOR_CONNECT = c2s(
        "chain_conveyor_connection",
        ChainConveyorConnectionPacket.CODEC
    );
    public static final PacketType<ServerboundChainConveyorRidingPacket> CHAIN_CONVEYOR_RIDING = c2s(
        "chain_conveyor_riding",
        ServerboundChainConveyorRidingPacket.CODEC
    );
    public static final PacketType<ChainPackageInteractionPacket> CHAIN_PACKAGE_INTERACTION = c2s(
        "chain_package_interaction",
        ChainPackageInteractionPacket.CODEC
    );
    public static final PacketType<PackagePortConfigurationPacket> PACKAGE_PORT_CONFIGURATION = c2s(
        "package_port_configuration",
        PackagePortConfigurationPacket.CODEC
    );
    public static final C2SHoldPacket TRAIN_MAP_REQUEST = c2s("train_map_request", AllHandle::onTrainMapSyncRequest);
    public static final PacketType<FactoryPanelConnectionPacket> CONNECT_FACTORY_PANEL = c2s(
        "connect_factory_panel",
        FactoryPanelConnectionPacket.CODEC
    );
    public static final PacketType<FactoryPanelConfigurationPacket> CONFIGURE_FACTORY_PANEL = c2s(
        "configure_factory_panel",
        FactoryPanelConfigurationPacket.CODEC
    );
    public static final PacketType<RedstoneRequesterConfigurationPacket> CONFIGURE_REDSTONE_REQUESTER = c2s(
        "configure_redstone_requester",
        RedstoneRequesterConfigurationPacket.CODEC
    );
    public static final PacketType<StockKeeperCategoryEditPacket> CONFIGURE_STOCK_KEEPER_CATEGORIES = c2s(
        "configure_stock_keeper_categories",
        StockKeeperCategoryEditPacket.CODEC
    );
    public static final PacketType<StockKeeperCategoryRefundPacket> REFUND_STOCK_KEEPER_CATEGORY = c2s(
        "refund_stock_keeper_category",
        StockKeeperCategoryRefundPacket.CODEC
    );
    public static final PacketType<StockKeeperLockPacket> LOCK_STOCK_KEEPER = c2s("lock_stock_keeper", StockKeeperLockPacket.CODEC);
    public static final PacketType<StockKeeperCategoryHidingPacket> STOCK_KEEPER_HIDE_CATEGORY = c2s(
        "stock_keeper_hide_category",
        StockKeeperCategoryHidingPacket.CODEC
    );
    public static final PacketType<LinkSettingsPacket> LINK_SETTINGS = c2s("link_settings", LinkSettingsPacket.CODEC);
    public static final PacketType<BlueprintPreviewRequestPacket> REQUEST_BLUEPRINT_PREVIEW = c2s(
        "request_blueprint_preview",
        BlueprintPreviewRequestPacket.CODEC
    );
    public static final PacketType<SymmetryEffectPacket> SYMMETRY_EFFECT = s2c("symmetry_effect", SymmetryEffectPacket.CODEC);
    public static final PacketType<ServerSpeedPacket> SERVER_SPEED = s2c("server_speed", ServerSpeedPacket.CODEC);
    public static final PacketType<ZapperBeamPacket> BEAM_EFFECT = s2c("beam_effect", ZapperBeamPacket.CODEC);
    public static final PacketType<ContraptionStallPacket> CONTRAPTION_STALL = s2c("contraption_stall", ContraptionStallPacket.CODEC);
    public static final PacketType<ContraptionDisassemblyPacket> CONTRAPTION_DISASSEMBLE = s2c(
        "contraption_disassemble",
        ContraptionDisassemblyPacket.CODEC
    );
    public static final PacketType<ContraptionBlockChangedPacket> CONTRAPTION_BLOCK_CHANGED = s2c(
        "contraption_block_changed",
        ContraptionBlockChangedPacket.CODEC
    );
    public static final PacketType<GlueEffectPacket> GLUE_EFFECT = s2c("glue_effect", GlueEffectPacket.CODEC);
    public static final PacketType<ContraptionSeatMappingPacket> CONTRAPTION_SEAT_MAPPING = s2c(
        "contraption_seat_mapping",
        ContraptionSeatMappingPacket.CODEC
    );
    public static final PacketType<LimbSwingUpdatePacket> LIMBSWING_UPDATE = s2c("limbswing_update", LimbSwingUpdatePacket.CODEC);
    public static final PacketType<FluidSplashPacket> FLUID_SPLASH = s2c("fluid_splash", FluidSplashPacket.CODEC);
    public static final PacketType<MountedStorageSyncPacket> MOUNTED_STORAGE_SYNC = s2c("mounted_storage_sync", MountedStorageSyncPacket.CODEC);
    public static final PacketType<GantryContraptionUpdatePacket> GANTRY_UPDATE = s2c("gantry_update", GantryContraptionUpdatePacket.CODEC);
    public static final PacketType<HighlightPacket> BLOCK_HIGHLIGHT = s2c("block_highlight", HighlightPacket.CODEC);
    public static final PacketType<TunnelFlapPacket> TUNNEL_FLAP = s2c("tunnel_flap", TunnelFlapPacket.CODEC);
    public static final PacketType<FunnelFlapPacket> FUNNEL_FLAP = s2c("funnel_flap", FunnelFlapPacket.CODEC);
    public static final PacketType<PotatoCannonPacket> POTATO_CANNON = s2c("potato_cannon", PotatoCannonPacket.CODEC);
    public static final PacketType<SoulPulseEffectPacket> SOUL_PULSE = s2c("soul_pulse", SoulPulseEffectPacket.CODEC);
    public static final PacketType<SignalEdgeGroupPacket> SYNC_EDGE_GROUP = s2c("sync_edge_group", SignalEdgeGroupPacket.CODEC);
    public static final PacketType<RemoveTrainPacket> REMOVE_TRAIN = s2c("remove_train", RemoveTrainPacket.CODEC);
    public static final PacketType<RemoveBlockEntityPacket> REMOVE_TE = s2c("remove_te", RemoveBlockEntityPacket.CODEC);
    public static final PacketType<TrainEditReturnPacket> S_CONFIGURE_TRAIN = s2c("s_configure_train", TrainEditReturnPacket.CODEC);
    public static final S2CHoldPacket<?> CONTROLS_ABORT = s2c("controls_abort", AllClientHandle::onControlsStopControlling);
    public static final PacketType<TrainHUDControlUpdatePacket> S_TRAIN_HUD = s2c("s_train_hud", TrainHUDControlUpdatePacket.CODEC);
    public static final PacketType<HonkReturnPacket> S_TRAIN_HONK = s2c("s_train_honk", HonkReturnPacket.CODEC);
    public static final PacketType<TrainPromptPacket> S_TRAIN_PROMPT = s2c("s_train_prompt", TrainPromptPacket.CODEC);
    public static final PacketType<ContraptionRelocationPacket> CONTRAPTION_RELOCATION = s2c(
        "contraption_relocation",
        ContraptionRelocationPacket.CODEC
    );
    public static final PacketType<TrackGraphRollCallPacket> TRACK_GRAPH_ROLL_CALL = s2c("track_graph_roll_call", TrackGraphRollCallPacket.CODEC);
    public static final PacketType<ArmPlacementRequestPacket> S_PLACE_ARM = s2c("s_place_arm", ArmPlacementRequestPacket.CODEC);
    public static final PacketType<EjectorPlacementRequestPacket> S_PLACE_EJECTOR = s2c("s_place_ejector", EjectorPlacementRequestPacket.CODEC);
    public static final PacketType<PackagePortPlacementRequestPacket> S_PLACE_PACKAGE_PORT = s2c(
        "s_place_package_port",
        PackagePortPlacementRequestPacket.CODEC
    );
    public static final PacketType<ElevatorFloorListPacket> UPDATE_ELEVATOR_FLOORS = s2c("update_elevator_floors", ElevatorFloorListPacket.CODEC);
    public static final PacketType<ContraptionDisableActorPacket> CONTRAPTION_ACTOR_TOGGLE = s2c(
        "contraption_actor_toggle",
        ContraptionDisableActorPacket.CODEC
    );
    public static final PacketType<ContraptionColliderLockPacket> CONTRAPTION_COLLIDER_LOCK = s2c(
        "contraption_collider_lock",
        ContraptionColliderLockPacket.CODEC
    );
    public static final PacketType<AttachedComputerPacket> ATTACHED_COMPUTER = s2c("attached_computer", AttachedComputerPacket.CODEC);
    public static final PacketType<ServerDebugInfoPacket> SERVER_DEBUG_INFO = s2c("server_debug_info", ServerDebugInfoPacket.CODEC);
    public static final PacketType<PackageDestroyPacket> PACKAGE_DESTROYED = s2c("package_destroyed", PackageDestroyPacket.CODEC);
    public static final PacketType<LogisticalStockResponsePacket> LOGISTICS_STOCK_RESPONSE = s2c(
        "logistics_stock_response",
        LogisticalStockResponsePacket.CODEC
    );
    public static final PacketType<FactoryPanelEffectPacket> FACTORY_PANEL_EFFECT = s2c("factory_panel_effect", FactoryPanelEffectPacket.CODEC);
    public static final PacketType<WiFiEffectPacket> PACKAGER_LINK_EFFECT = s2c("packager_link_effect", WiFiEffectPacket.CODEC);
    public static final PacketType<RedstoneRequesterEffectPacket> REDSTONE_REQUESTER_EFFECT = s2c(
        "redstone_requester_effect",
        RedstoneRequesterEffectPacket.CODEC
    );
    public static final PacketType<ClientboundChainConveyorRidingPacket> CLIENTBOUND_CHAIN_CONVEYOR = s2c(
        "clientbound_chain_conveyor",
        ClientboundChainConveyorRidingPacket.CODEC
    );
    public static final PacketType<ShopUpdatePacket> SHOP_UPDATE = s2c("shop_update", ShopUpdatePacket.CODEC);
    public static final PacketType<TrackGraphSyncPacket> SYNC_RAIL_GRAPH = s2c("sync_rail_graph", TrackGraphSyncPacket.CODEC);
    public static final PacketType<AddTrainPacket> ADD_TRAIN = s2c("add_train", AddTrainPacket.CODEC);
    public static final PacketType<OpenScreenPacket> OPEN_SCREEN = s2c("open_screen", OpenScreenPacket.CODEC);
    public static final PacketType<BlueprintPreviewPacket> BLUEPRINT_PREVIEW = s2c("blueprint_preview", BlueprintPreviewPacket.CODEC);
    public static final PacketType<SuperGlueSpawnPacket> SUPER_GLUE_SPAWN = s2c("super_glue_spawn", SuperGlueSpawnPacket.CODEC);
    public static final PacketType<NbtSpawnPacket> NBT_SPAWN = s2c("nbt_spawn", NbtSpawnPacket.CODEC);
    public static final PacketType<EjectorItemSpawnPacket> EJECTOR_ITEM_SPAWN = s2c("ejector_item_spawn", EjectorItemSpawnPacket.CODEC);
    public static final PacketType<PackageSpawnPacket> PACKAGE_SPAWN = s2c("package_spawn", PackageSpawnPacket.CODEC);
    public static final PacketType<ServerConfigPacket> SERVER_CONFIG = s2c_config("server_config", ServerConfigPacket.CODEC);

    @SuppressWarnings("unchecked")
    private static <T extends Packet<ClientConfigurationPacketListener>> PacketType<T> s2c_config(
        String id,
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        PacketType<T> type = new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.fromNamespaceAndPath(MOD_ID, id));
        S2C_CONFIG.put(
            (PacketType<Packet<ClientConfigurationPacketListener>>) type,
            (StreamCodec<? super RegistryFriendlyByteBuf, Packet<ClientConfigurationPacketListener>>) codec
        );
        return type;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Packet<ClientGamePacketListener>> PacketType<T> s2c(String id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PacketType<T> type = new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.fromNamespaceAndPath(MOD_ID, id));
        S2C.put(
            (PacketType<Packet<ClientGamePacketListener>>) type,
            (StreamCodec<? super RegistryFriendlyByteBuf, Packet<ClientGamePacketListener>>) codec
        );
        return type;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Packet<ServerGamePacketListener>> PacketType<T> c2s(String id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PacketType<T> type = new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.fromNamespaceAndPath(MOD_ID, id));
        C2S.put(
            (PacketType<Packet<ServerGamePacketListener>>) type,
            (StreamCodec<? super RegistryFriendlyByteBuf, Packet<ServerGamePacketListener>>) codec
        );
        return type;
    }

    private static C2SHoldPacket c2s(String id, Consumer<ServerGamePacketListenerImpl> callback) {
        C2SHoldPacket packet = new C2SHoldPacket(id, callback);
        C2S.put(packet.id(), packet.codec());
        return packet;
    }

    private static <T extends ClientGamePacketListener> S2CHoldPacket<T> s2c(String id, BiConsumer<AllClientHandle<T>, T> callback) {
        S2CHoldPacket<T> packet = new S2CHoldPacket<>(id, callback);
        S2C.put(packet.id(), packet.codec());
        return packet;
    }

    public static void register() {
    }
}
