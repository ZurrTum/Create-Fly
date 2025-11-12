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
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

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
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AllClientHandle<T> {
    public static AllClientHandle<? extends ClientGamePacketListener> INSTANCE = new AllClientHandle<>();

    @SuppressWarnings("unchecked")
    public <P> void call(TriConsumer<AllClientHandle<T>, T, P> fn, ClientGamePacketListener listener, S2CPacket packet) {
        if (packet.runInMain()) {
            forceMainThread((T) listener, packet);
        }
        fn.accept(this, (T) listener, (P) packet);
    }

    protected void forceMainThread(T listener, S2CPacket packet) {
        warn();
    }

    private static void warn() {
        Create.LOGGER.warn("Ignore the client call {}", Thread.currentThread().getStackTrace()[2].toString());
    }

    public boolean isClient() {
        return false;
    }

    public boolean shiftDown() {
        return false;
    }

    public void onSymmetryEffect(T listener, SymmetryEffectPacket packet) {
        warn();
    }

    public void onLimbSwingUpdate(T listener, LimbSwingUpdatePacket packet) {
        warn();
    }

    public void onLogisticalStockResponse(T listener, LogisticalStockResponsePacket packet) {
        warn();
    }

    public void onTrainEditReturn(T listener, TrainEditReturnPacket packet) {
        warn();
    }

    public void onTrainHUDControlUpdate(T listener, TrainHUDControlUpdatePacket packet) {
        warn();
    }

    public void onTrainHonkReturn(T listener, HonkReturnPacket packet) {
        warn();
    }

    public void onElevatorFloorList(T listener, ElevatorFloorListPacket packet) {
        warn();
    }

    public void onContraptionColliderLock(T listener, ContraptionColliderLockPacket packet) {
        warn();
    }

    public void onWiFiEffect(T listener, WiFiEffectPacket packet) {
        warn();
    }

    public void onControlsStopControlling(T listener) {
        warn();
    }

    public void onServerSpeed(T listener, ServerSpeedPacket packet) {
        warn();
    }

    public void onZapperBeam(T listener, ZapperBeamPacket packet) {
        warn();
    }

    public void onContraptionStall(T listener, ContraptionStallPacket packet) {
        warn();
    }

    public void onContraptionDisassembly(T listener, ContraptionDisassemblyPacket packet) {
        warn();
    }

    public void onContraptionBlockChanged(T listener, ContraptionBlockChangedPacket packet) {
        warn();
    }

    public void onGlueEffect(T listener, GlueEffectPacket packet) {
        warn();
    }

    public void onContraptionSeatMapping(T listener, ContraptionSeatMappingPacket packet) {
        warn();
    }

    public void onFluidSplash(T listener, FluidSplashPacket packet) {
        warn();
    }

    public void onMountedStorageSync(T listener, MountedStorageSyncPacket packet) {
        warn();
    }

    public void onGantryContraptionUpdate(T listener, GantryContraptionUpdatePacket packet) {
        warn();
    }

    public void onHighlight(T listener, HighlightPacket packet) {
        warn();
    }

    public void onTunnelFlap(T listener, TunnelFlapPacket packet) {
        warn();
    }

    public void onFunnelFlap(T listener, FunnelFlapPacket packet) {
        warn();
    }

    public void onPotatoCannon(T listener, PotatoCannonPacket packet) {
        warn();
    }

    public void onSoulPulseEffect(T listener, SoulPulseEffectPacket packet) {
        warn();
    }

    public void onSignalEdgeGroup(T listener, SignalEdgeGroupPacket packet) {
        warn();
    }

    public void onRemoveTrain(T listener, RemoveTrainPacket packet) {
        warn();
    }

    public void onRemoveBlockEntity(T listener, RemoveBlockEntityPacket packet) {
        warn();
    }

    public void onTrainPrompt(T listener, TrainPromptPacket packet) {
        warn();
    }

    public void onContraptionRelocation(T listener, ContraptionRelocationPacket packet) {
        warn();
    }

    public void onTrackGraphRollCall(T listener, TrackGraphRollCallPacket packet) {
        warn();
    }

    public void onArmPlacementRequest(T listener, ArmPlacementRequestPacket packet) {
        warn();
    }

    public void onEjectorPlacementRequest(T listener, EjectorPlacementRequestPacket packet) {
        warn();
    }

    public void onPackagePortPlacementRequest(T listener, PackagePortPlacementRequestPacket packet) {
        warn();
    }

    public void onContraptionDisableActor(T listener, ContraptionDisableActorPacket packet) {
        warn();
    }

    public void onAttachedComputer(T listener, AttachedComputerPacket packet) {
        warn();
    }

    public void onServerDebugInfo(T listener, ServerDebugInfoPacket packet) {
        warn();
    }

    public void onPackageDestroy(T listener, PackageDestroyPacket packet) {
        warn();
    }

    public void onFactoryPanelEffect(T listener, FactoryPanelEffectPacket packet) {
        warn();
    }

    public void onRedstoneRequesterEffect(T listener, RedstoneRequesterEffectPacket packet) {
        warn();
    }

    public void onClientboundChainConveyorRiding(T listener, ClientboundChainConveyorRidingPacket packet) {
        warn();
    }

    public void onShopUpdate(T listener, ShopUpdatePacket packet) {
        warn();
    }

    public void onTrackGraphSync(T listener, TrackGraphSyncPacket packet) {
        warn();
    }

    public void onAddTrain(T listener, AddTrainPacket packet) {
        warn();
    }

    public void onOpenScreen(T listener, OpenScreenPacket packet) {
        warn();
    }

    public void onBlueprintPreview(T listener, BlueprintPreviewPacket packet) {
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
