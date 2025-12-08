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
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.entity.behaviour.EntityBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    public void onSymmetryEffect(ClientGamePacketListener listener, SymmetryEffectPacket packet) {
        warn();
    }

    public void onLimbSwingUpdate(ClientGamePacketListener listener, LimbSwingUpdatePacket packet) {
        warn();
    }

    public void onLogisticalStockResponse(ClientGamePacketListener listener, LogisticalStockResponsePacket packet) {
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

    public void onElevatorFloorList(ClientGamePacketListener listener, ElevatorFloorListPacket packet) {
        warn();
    }

    public void onContraptionColliderLock(ContraptionColliderLockPacket packet) {
        warn();
    }

    public void onWiFiEffect(ClientGamePacketListener listener, WiFiEffectPacket packet) {
        warn();
    }

    public void onControlsStopControlling() {
        warn();
    }

    public void onServerSpeed(ServerSpeedPacket packet) {
        warn();
    }

    public void onZapperBeam(ClientGamePacketListener listener, ZapperBeamPacket packet) {
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

    public void onGlueEffect(ClientGamePacketListener listener, GlueEffectPacket packet) {
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

    public void onFunnelFlap(ClientGamePacketListener listener, FunnelFlapPacket packet) {
        warn();
    }

    public void onPotatoCannon(ClientGamePacketListener listener, PotatoCannonPacket packet) {
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

    public void onRemoveBlockEntity(ClientGamePacketListener listener, RemoveBlockEntityPacket packet) {
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

    public void onContraptionDisableActor(ClientGamePacketListener listener, ContraptionDisableActorPacket packet) {
        warn();
    }

    public void onAttachedComputer(AttachedComputerPacket packet) {
        warn();
    }

    public void onServerDebugInfo(ServerDebugInfoPacket packet) {
        warn();
    }

    public void onPackageDestroy(ClientGamePacketListener listener, PackageDestroyPacket packet) {
        warn();
    }

    public void onFactoryPanelEffect(ClientGamePacketListener listener, FactoryPanelEffectPacket packet) {
        warn();
    }

    public void onRedstoneRequesterEffect(ClientGamePacketListener listener, RedstoneRequesterEffectPacket packet) {
        warn();
    }

    public void onClientboundChainConveyorRiding(ClientboundChainConveyorRidingPacket packet) {
        warn();
    }

    public void onShopUpdate(ClientGamePacketListener listener, ShopUpdatePacket packet) {
        warn();
    }

    public void onTrackGraphSync(TrackGraphSyncPacket packet) {
        warn();
    }

    public void onAddTrain(AddTrainPacket packet) {
        warn();
    }

    public void onOpenScreen(ClientGamePacketListener listener, OpenScreenPacket packet) {
        warn();
    }

    public void onBlueprintPreview(BlueprintPreviewPacket packet) {
        warn();
    }

    public void buildDebugInfo() {
        warn();
    }

    public Player getPlayer() {
        warn();
        return null;
    }

    public void queueUpdate(BlockEntity entity) {
        warn();
    }

    public void addAirFlowParticle(Level world, BlockPos airCurrentPos, double x, double y, double z) {
        warn();
    }

    public void enableClientPlayerSound(Entity entity, float clamp) {
        warn();
    }

    public void addBehaviours(SmartBlockEntity blockEntity, ArrayList<BlockEntityBehaviour<?>> behaviours) {
    }

    public void addBehaviours(Entity entity, ArrayList<EntityBehaviour<?>> behaviours) {
    }

    public void showWaterBounds(Axis axis, BlockPlaceContext ctx) {
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

    public void spawnPipeParticles(Level world, BlockPos pos, PipeConnection.Flow flow, boolean openEnd, Direction side, int amount) {
        warn();
    }

    public void spawnSteamEngineParticles(SteamEngineBlockEntity be) {
        warn();
    }

    public void spawnSuperGlueParticles(Level world, BlockPos pos, Direction direction, boolean fullBlock) {
        warn();
    }

    public void tickBlazeBurnerAnimation(BlazeBurnerBlockEntity be) {
        warn();
    }

    public void sendPacket(Packet<ServerGamePacketListener> packet) {
        warn();
    }

    public void sendPacket(Player player, Packet<ServerGamePacketListener> packet) {
        warn();
    }

    public void createBasinFluidParticles(Level world, BasinBlockEntity blockEntity) {
        warn();
    }

    public void cartClicked(Player player, AbstractMinecart minecart) {
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

    public void startControlling(Player player, AbstractContraptionEntity be, BlockPos pos) {
        warn();
    }

    public void tickBlazeBurnerMovement(MovementContext context) {
        warn();
    }

    public void cannonDontAnimateItem(InteractionHand hand) {
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

    public boolean factoryPanelClicked(Level world, Player player, ServerFactoryPanelBehaviour behaviour) {
        warn();
        return false;
    }

    public void zapperDontAnimateItem(InteractionHand hand) {
        warn();
    }

    public void openSequencedGearshiftScreen(SequencedGearshiftBlockEntity be) {
        warn();
    }

    public void openClipboardScreen(Player player, DataComponentMap components, BlockPos pos) {
        warn();
    }

    public void openDisplayLinkScreen(DisplayLinkBlockEntity be, Player player) {
        warn();
    }

    public void openThresholdSwitchScreen(ThresholdSwitchBlockEntity be, Player player) {
        warn();
    }

    public void openElevatorContactScreen(ElevatorContactBlockEntity be, Player player) {
        warn();
    }

    public void openStationScreen(Level world, BlockPos pos, Player player) {
        warn();
    }

    public void openFactoryPanelScreen(ServerFactoryPanelBehaviour behaviour, Player player) {
        warn();
    }

    public void openSymmetryWandScreen(ItemStack stack, InteractionHand hand) {
        warn();
    }

    public void openSchematicEditScreen() {
        warn();
    }

    public void openWorldshaperScreen(ItemStack item, InteractionHand hand) {
        warn();
    }
}
