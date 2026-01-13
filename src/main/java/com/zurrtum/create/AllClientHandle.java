package com.zurrtum.create;

import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.zurrtum.create.content.fluids.PipeConnection;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AllClientHandle {
    public static AllClientHandle INSTANCE = new AllClientHandle();

    private static void warn() {
        Create.LOGGER.warn("Ignore the client call {}", Thread.currentThread().getStackTrace()[2].toString());
    }

    public boolean isClient() {
        return false;
    }

    public boolean shiftDown() {
        return false;
    }

    public void onSymmetryEffect(ClientPlayPacketListener listener, SymmetryEffectPacket packet) {
        warn();
    }

    public void onLimbSwingUpdate(ClientPlayPacketListener listener, LimbSwingUpdatePacket packet) {
        warn();
    }

    public void onLogisticalStockResponse(ClientPlayPacketListener listener, LogisticalStockResponsePacket packet) {
        warn();
    }

    public void onTrainEditReturn(TrainEditReturnPacket packet) {
        warn();
    }

    public void onTrainHUDControlUpdate(TrainHUDControlUpdatePacket packet) {
        warn();
    }

    public void onTrainHonkReturn(HonkReturnPacket packet) {
        warn();
    }

    public void onElevatorFloorList(ClientPlayPacketListener listener, ElevatorFloorListPacket packet) {
        warn();
    }

    public void onContraptionColliderLock(ContraptionColliderLockPacket packet) {
        warn();
    }

    public void onWiFiEffect(ClientPlayPacketListener listener, WiFiEffectPacket packet) {
        warn();
    }

    public void onControlsStopControlling() {
        warn();
    }

    public void onServerSpeed(ServerSpeedPacket packet) {
        warn();
    }

    public void onZapperBeam(ClientPlayPacketListener listener, ZapperBeamPacket packet) {
        warn();
    }

    public void onContraptionStall(ContraptionStallPacket packet) {
        warn();
    }

    public void onContraptionDisassembly(ContraptionDisassemblyPacket packet) {
        warn();
    }

    public void onContraptionBlockChanged(ContraptionBlockChangedPacket packet) {
        warn();
    }

    public void onGlueEffect(ClientPlayPacketListener listener, GlueEffectPacket packet) {
        warn();
    }

    public void onContraptionSeatMapping(ContraptionSeatMappingPacket packet) {
        warn();
    }

    public void onFluidSplash(FluidSplashPacket packet) {
        warn();
    }

    public void onMountedStorageSync(MountedStorageSyncPacket packet) {
        warn();
    }

    public void onGantryContraptionUpdate(GantryContraptionUpdatePacket packet) {
        warn();
    }

    public void onHighlight(HighlightPacket packet) {
        warn();
    }

    public void onTunnelFlap(TunnelFlapPacket packet) {
        warn();
    }

    public void onFunnelFlap(ClientPlayPacketListener listener, FunnelFlapPacket packet) {
        warn();
    }

    public void onPotatoCannon(ClientPlayPacketListener listener, PotatoCannonPacket packet) {
        warn();
    }

    public void onSoulPulseEffect(SoulPulseEffectPacket packet) {
        warn();
    }

    public void onSignalEdgeGroup(SignalEdgeGroupPacket packet) {
        warn();
    }

    public void onRemoveTrain(RemoveTrainPacket packet) {
        warn();
    }

    public void onRemoveBlockEntity(ClientPlayPacketListener listener, RemoveBlockEntityPacket packet) {
        warn();
    }

    public void onTrainPrompt(TrainPromptPacket packet) {
        warn();
    }

    public void onContraptionRelocation(ContraptionRelocationPacket packet) {
        warn();
    }

    public void onTrackGraphRollCall(TrackGraphRollCallPacket packet) {
        warn();
    }

    public void onArmPlacementRequest(ArmPlacementRequestPacket packet) {
        warn();
    }

    public void onEjectorPlacementRequest(EjectorPlacementRequestPacket packet) {
        warn();
    }

    public void onPackagePortPlacementRequest(PackagePortPlacementRequestPacket packet) {
        warn();
    }

    public void onContraptionDisableActor(ClientPlayPacketListener listener, ContraptionDisableActorPacket packet) {
        warn();
    }

    public void onAttachedComputer(ClientPlayPacketListener listener, AttachedComputerPacket packet) {
        warn();
    }

    public void onServerDebugInfo(ServerDebugInfoPacket packet) {
        warn();
    }

    public void onPackageDestroy(ClientPlayPacketListener listener, PackageDestroyPacket packet) {
        warn();
    }

    public void onFactoryPanelEffect(ClientPlayPacketListener listener, FactoryPanelEffectPacket packet) {
        warn();
    }

    public void onRedstoneRequesterEffect(ClientPlayPacketListener listener, RedstoneRequesterEffectPacket packet) {
        warn();
    }

    public void onClientboundChainConveyorRiding(ClientboundChainConveyorRidingPacket packet) {
        warn();
    }

    public void onShopUpdate(ClientPlayPacketListener listener, ShopUpdatePacket packet) {
        warn();
    }

    public void onTrackGraphSync(TrackGraphSyncPacket packet) {
        warn();
    }

    public void onAddTrain(AddTrainPacket packet) {
        warn();
    }

    public void onOpenScreen(ClientPlayPacketListener listener, OpenScreenPacket packet) {
        warn();
    }

    public void onBlueprintPreview(BlueprintPreviewPacket packet) {
        warn();
    }

    public void buildDebugInfo() {
        warn();
    }

    public PlayerEntity getPlayer() {
        warn();
        return null;
    }

    public void queueUpdate(BlockEntity entity) {
        warn();
    }

    public void addAirFlowParticle(World world, BlockPos airCurrentPos, double x, double y, double z) {
        warn();
    }

    public void enableClientPlayerSound(Entity entity, float clamp) {
        warn();
    }

    public void showWaterBounds(Axis axis, ItemPlacementContext ctx) {
        warn();
    }

    public float getServerSpeed() {
        warn();
        return 1;
    }

    public void resetClientContraption(Contraption contraption) {
    }

    public void invalidateClientContraptionChildren(Contraption contraption) {
    }

    @Nullable
    public BlockEntity getBlockEntityClientSide(Contraption contraption, BlockPos localPos) {
        warn();
        return null;
    }

    public void spawnPipeParticles(World world, BlockPos pos, PipeConnection.Flow flow, boolean openEnd, Direction side, int amount) {
        warn();
    }

    public void spawnSteamEngineParticles(SteamEngineBlockEntity be) {
        warn();
    }

    public void spawnSuperGlueParticles(World world, BlockPos pos, Direction direction, boolean fullBlock) {
        warn();
    }

    public void tickBlazeBurnerAnimation(BlazeBurnerBlockEntity be) {
        warn();
    }

    public void sendPacket(Packet<ServerPlayPacketListener> packet) {
        warn();
    }

    public void sendPacket(PlayerEntity player, Packet<ServerPlayPacketListener> packet) {
        warn();
    }

    public void createBasinFluidParticles(World world, BasinBlockEntity blockEntity) {
        warn();
    }

    public void cartClicked(PlayerEntity player, AbstractMinecartEntity minecart) {
        warn();
    }

    public void advertiseToAddressHelper(ClipboardBlockEntity blockEntity) {
        warn();
    }

    public void updateClipboardScreen(UUID lastEdit, BlockPos pos, ClipboardContent content) {
        warn();
    }

    public GlobalRailwayManager getGlobalRailwayManager() {
        warn();
        return null;
    }

    public void registerToCurveInteraction(TrackBlockEntity be) {
        warn();
    }

    public void removeFromCurveInteraction(TrackBlockEntity be) {
        warn();
    }

    public void invalidateCarriage(CarriageContraptionEntity entity) {
        warn();
    }

    public void startControlling(PlayerEntity player, AbstractContraptionEntity be, BlockPos pos) {
        warn();
    }

    public void tickBlazeBurnerMovement(MovementContext context) {
        warn();
    }

    public void cannonDontAnimateItem(Hand hand) {
        warn();
    }

    public void tryToggleActive(LecternControllerBlockEntity controller) {
        warn();
    }

    public void toggleLinkedControllerBindMode(BlockPos pos) {
        warn();
    }

    public void toggleLinkedControllerActive() {
        warn();
    }

    public void factoryPanelMoveToSlot(SmartBlockEntity be, PanelSlot slot) {
        warn();
    }

    public boolean factoryPanelClicked(World world, PlayerEntity player, ServerFactoryPanelBehaviour behaviour) {
        warn();
        return false;
    }

    public void zapperDontAnimateItem(Hand hand) {
        warn();
    }

    public void openSequencedGearshiftScreen(SequencedGearshiftBlockEntity be) {
        warn();
    }

    public void openClipboardScreen(PlayerEntity player, ComponentMap components, BlockPos pos) {
        warn();
    }

    public void openDisplayLinkScreen(DisplayLinkBlockEntity be, PlayerEntity player) {
        warn();
    }

    public void openThresholdSwitchScreen(ThresholdSwitchBlockEntity be, PlayerEntity player) {
        warn();
    }

    public void openElevatorContactScreen(ElevatorContactBlockEntity be, PlayerEntity player) {
        warn();
    }

    public void openStationScreen(World world, BlockPos pos, PlayerEntity player) {
        warn();
    }

    public void openFactoryPanelScreen(ServerFactoryPanelBehaviour behaviour, PlayerEntity player) {
        warn();
    }

    public void openSymmetryWandScreen(ItemStack stack, Hand hand) {
        warn();
    }

    public void openSchematicEditScreen() {
        warn();
    }

    public void openWorldshaperScreen(ItemStack item, Hand hand) {
        warn();
    }
}
