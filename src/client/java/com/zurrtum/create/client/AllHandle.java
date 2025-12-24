package com.zurrtum.create.client;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.zurrtum.create.*;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.contraptions.ContraptionColliderClient;
import com.zurrtum.create.client.content.contraptions.actors.trainControls.ControlsHandler;
import com.zurrtum.create.client.content.contraptions.elevator.ElevatorContactScreen;
import com.zurrtum.create.client.content.contraptions.glue.SuperGlueSelectionHandler;
import com.zurrtum.create.client.content.contraptions.minecart.CouplingHandlerClient;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.content.equipment.bell.SoulPulseEffect;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.client.content.equipment.clipboard.ClipboardScreen;
import com.zurrtum.create.client.content.equipment.symmetryWand.SymmetryHandlerClient;
import com.zurrtum.create.client.content.equipment.symmetryWand.SymmetryWandScreen;
import com.zurrtum.create.client.content.equipment.zapper.ShootableGadgetRenderHandler;
import com.zurrtum.create.client.content.equipment.zapper.ZapperRenderHandler.LaserBeam;
import com.zurrtum.create.client.content.equipment.zapper.terrainzapper.WorldshaperScreen;
import com.zurrtum.create.client.content.fluids.FluidFX;
import com.zurrtum.create.client.content.kinetics.fan.AirCurrentClient;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.zurrtum.create.client.content.kinetics.steamEngine.SteamEngineRenderer;
import com.zurrtum.create.client.content.kinetics.transmission.sequencer.SequencedGearshiftScreen;
import com.zurrtum.create.client.content.logistics.AddressEditBoxHelper;
import com.zurrtum.create.client.content.logistics.depot.EjectorTargetHandler;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelScreen;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelSlotPositioning;
import com.zurrtum.create.client.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerMovementRenderBehaviour;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.content.redstone.displayLink.DisplayLinkScreen;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.redstone.thresholdSwitch.ThresholdSwitchScreen;
import com.zurrtum.create.client.content.schematics.client.SchematicEditScreen;
import com.zurrtum.create.client.content.trains.TrainHUD;
import com.zurrtum.create.client.content.trains.station.AssemblyScreen;
import com.zurrtum.create.client.content.trains.station.StationScreen;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.backend.BackendManager;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.Flywheel;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.render.PlayerSkyhookRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.client.foundation.utility.ServerSpeedProvider;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.contraptions.gantry.GantryContraptionEntity;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.zurrtum.create.content.fluids.PipeConnection;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import com.zurrtum.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TrainIconType;
import com.zurrtum.create.content.trains.graph.*;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.entity.behaviour.EntityBehaviour;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.debugInfo.DebugInformation;
import com.zurrtum.create.infrastructure.debugInfo.element.DebugInfoSection;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.packet.c2s.TrackGraphRequestPacket;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import com.zurrtum.create.infrastructure.particle.AirFlowParticleData;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.LOGGER;

public class AllHandle extends AllClientHandle {

    public static void register() {
        AllClientHandle.INSTANCE = new AllHandle();
    }

    protected void forceMainThread(ClientGamePacketListener listener, Minecraft mc, Packet<ClientGamePacketListener> packet) {
        if (listener instanceof ClientPacketListener) {
            PacketUtils.ensureRunningOnSameThread(packet, listener, mc.packetProcessor());
        }
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean shiftDown() {
        return AllKeys.hasShiftDown();
    }

    @Override
    public void onSymmetryEffect(ClientGamePacketListener listener, SymmetryEffectPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        BlockPos mirror = packet.mirror();
        if (mc.player.position().distanceTo(Vec3.atLowerCornerOf(mirror)) > 100)
            return;
        for (BlockPos to : packet.positions())
            SymmetryHandlerClient.drawEffect(mc, mirror, to);
    }

    @Override
    public void onLogisticalStockResponse(ClientGamePacketListener listener, LogisticalStockResponsePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        if (mc.level.getBlockEntity(packet.pos()) instanceof StockTickerBlockEntity stbe) {
            stbe.receiveStockPacket(packet.items(), packet.lastPacket());
        }
    }

    @Override
    public void onTrainEditReturn(TrainEditReturnPacket packet) {
        Train train = Create.RAILWAYS.trains.get(packet.id());
        if (train == null)
            return;
        if (!packet.name().isBlank()) {
            train.name = Component.literal(packet.name());
        }
        train.icon = TrainIconType.byId(packet.iconType());
        train.mapColorIndex = packet.mapColor();
    }

    @Override
    public void onTrainHUDControlUpdate(TrainHUDControlUpdatePacket packet) {
        Train train = Create.RAILWAYS.trains.get(packet.trainId());
        if (train == null)
            return;

        if (packet.throttle() != null) {
            train.throttle = packet.throttle();
        }

        train.speed = packet.speed();
        train.fuelTicks = packet.fuelTicks();
    }

    @Override
    public void onTrainHonkReturn(HonkReturnPacket packet) {
        Train train = Create.RAILWAYS.trains.get(packet.trainId());
        if (train == null)
            return;

        if (packet.isHonk())
            train.honkTicks = train.honkTicks == 0 ? 20 : 13;
        else
            train.honkTicks = train.honkTicks > 5 ? 6 : 0;
    }

    @Override
    public void onElevatorFloorList(ClientGamePacketListener listener, ElevatorFloorListPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        Entity entityByID = mc.level.getEntity(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;
        if (!(ace.getContraption() instanceof ElevatorContraption ec))
            return;

        ec.namesList = packet.floors();
        ec.syncControlDisplays();
    }

    @Override
    public void onContraptionColliderLock(ContraptionColliderLockPacket packet) {
        ContraptionColliderClient.lockPacketReceived(packet.contraption(), packet.sender(), packet.offset());
    }

    @Override
    public void onWiFiEffect(ClientGamePacketListener listener, WiFiEffectPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        BlockEntity blockEntity = mc.level.getBlockEntity(packet.pos());
        if (blockEntity instanceof PackagerLinkBlockEntity plbe)
            plbe.playEffect();
        if (blockEntity instanceof StockTickerBlockEntity plbe)
            plbe.playEffect();
    }

    @Override
    public void onControlsStopControlling() {
        ControlsHandler.stopControlling(Minecraft.getInstance().player);
    }

    @Override
    public void onServerSpeed(ServerSpeedPacket packet) {
        if (!ServerSpeedProvider.initialized) {
            ServerSpeedProvider.initialized = true;
            ServerSpeedProvider.clientTimer = 0;
            return;
        }
        float target = ((float) packet.speed()) / Math.max(ServerSpeedProvider.clientTimer, 1);
        ServerSpeedProvider.modifier.chase(Math.min(target, 1), .25, LerpedFloat.Chaser.EXP);
        // Set this to -1 because packets are processed before ticks.
        // ServerSpeedProvider#clientTick will increment it to 0 at the end of this tick.
        // Setting it to 0 causes consistent desync, as the client ends up counting too many ticks.
        ServerSpeedProvider.clientTimer = -1;
    }

    private <T extends ShootableGadgetRenderHandler> void onShootGadget(
        Entity renderViewEntity,
        Vec3 location,
        InteractionHand hand,
        boolean self,
        T handler,
        Consumer<T> handleAdditional
    ) {
        if (renderViewEntity == null)
            return;
        if (renderViewEntity.position().distanceTo(location) > 100)
            return;

        handleAdditional.accept(handler);
        if (self)
            handler.shoot(hand, location);
        else
            handler.playSound(hand, location);
    }

    @Override
    public void onZapperBeam(ClientGamePacketListener listener, ZapperBeamPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        onShootGadget(
            mc.getCameraEntity(), packet.location(), packet.hand(), packet.self(), Create.ZAPPER_RENDER_HANDLER, handler -> {
                handler.addBeam(mc, new LaserBeam(packet.location(), packet.target()));
            }
        );
    }

    @Override
    public void onPotatoCannon(ClientGamePacketListener listener, PotatoCannonPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        onShootGadget(
            mc.getCameraEntity(), packet.location(), packet.hand(), packet.self(), Create.POTATO_CANNON_RENDER_HANDLER, handler -> {
                handler.beforeShoot(packet.pitch(), packet.location(), packet.motion(), packet.item());
            }
        );
    }

    @Override
    public void onContraptionStall(ContraptionStallPacket packet) {
        if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity ce) {
            ce.handleStallInformation(packet.x(), packet.y(), packet.z(), packet.angle());
        }
    }

    @Override
    public void onContraptionDisassembly(ContraptionDisassemblyPacket packet) {
        if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity ce) {
            ce.moveCollidedEntitiesOnDisassembly(packet.transform());
        }
    }

    @Override
    public void onContraptionBlockChanged(ContraptionBlockChangedPacket packet) {
        if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof AbstractContraptionEntity ce) {
            Contraption contraption = ce.getContraption();
            if (contraption == null) {
                return;
            }
            Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = contraption.getBlocks();
            BlockPos localPos = packet.localPos();
            if (!blocks.containsKey(localPos)) {
                return;
            }
            StructureTemplate.StructureBlockInfo info = blocks.get(localPos);
            BlockState newState = packet.newState();
            blocks.put(localPos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));
            if (info.state() != newState && !(newState.getBlock() instanceof SlidingDoorBlock)) {
                ClientContraption.resetClientContraption(contraption);
            }
            contraption.invalidateColliders();
        }
    }

    @Override
    public void onGlueEffect(ClientGamePacketListener listener, GlueEffectPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        LocalPlayer player = mc.player;
        if (!player.blockPosition().closerThan(packet.pos(), 100))
            return;
        SuperGlueSelectionHandler.spawnParticles(player.level(), packet.pos(), packet.direction(), packet.fullBlock());
    }

    @Override
    public void onContraptionSeatMapping(ContraptionSeatMappingPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        Entity entityByID = player.level().getEntity(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
            return;

        if (packet.dismountedId() == player.getId()) {
            Vec3 transformedVector = contraptionEntity.getPassengerPosition(player, 1);
            if (transformedVector != null)
                AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(player, Optional.of(transformedVector));
        }

        contraptionEntity.getContraption().setSeatMapping(new HashMap<>(packet.mapping()));
    }

    @Override
    public void onLimbSwingUpdate(ClientGamePacketListener listener, LimbSwingUpdatePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        ClientLevel world = mc.level;
        Entity entity = world.getEntity(packet.entityId());
        if (!(entity instanceof Player player))
            return;
        AllSynchedDatas.LAST_OVERRIDE_LIMB_SWING_UPDATE.set(player, 0);
        AllSynchedDatas.OVERRIDE_LIMB_SWING.set(player, packet.limbSwing());
        Vec3 position = packet.position();
        player.moveOrInterpolateTo(position, player.getYRot(), player.getXRot());
    }

    @Override
    public void onFluidSplash(FluidSplashPacket packet) {
        BlockPos pos = packet.pos();
        if (Minecraft.getInstance().player.position().distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) > 100) {
            return;
        }
        FluidFX.splash(pos, packet.fluid());
    }

    @Override
    public void onMountedStorageSync(MountedStorageSyncPacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.contraptionId());
        if (!(entity instanceof AbstractContraptionEntity contraption))
            return;

        contraption.getContraption().getStorage().handleSync(packet, contraption);
    }

    @Override
    public void onGantryContraptionUpdate(GantryContraptionUpdatePacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID());
        if (!(entity instanceof GantryContraptionEntity ce)) {
            return;
        }
        ce.axisMotion = packet.motion();
        ce.clientOffsetDiff = packet.coord() - ce.getAxisCoord();
        ce.sequencedOffsetLimit = packet.sequenceLimit();
    }

    @Override
    public void onHighlight(HighlightPacket packet) {
        if (!Minecraft.getInstance().level.isLoaded(packet.pos())) {
            return;
        }

        Outliner.getInstance().showAABB("highlightCommand", Shapes.block().bounds().move(packet.pos()), 200).lineWidth(1 / 32f).colored(0xEeEeEe)
            // .colored(0x243B50)
            .withFaceTexture(AllSpecialTextures.SELECTION);
    }

    @Override
    public void onTunnelFlap(TunnelFlapPacket packet) {
        if (Minecraft.getInstance().level.getBlockEntity(packet.pos()) instanceof BeltTunnelBlockEntity blockEntity) {
            packet.flaps().forEach(flap -> {
                blockEntity.flap(flap.getFirst(), flap.getSecond());
            });
        }
    }

    @Override
    public void onFunnelFlap(ClientGamePacketListener listener, FunnelFlapPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        if (mc.level.getBlockEntity(packet.pos()) instanceof FunnelBlockEntity blockEntity) {
            blockEntity.flap(packet.inwards());
        }
    }

    @Override
    public void onSoulPulseEffect(SoulPulseEffectPacket packet) {
        Create.SOUL_PULSE_EFFECT_HANDLER.addPulse(new SoulPulseEffect(packet.pos(), packet.distance(), packet.canOverlap()));
    }

    @Override
    public void onSignalEdgeGroup(SignalEdgeGroupPacket packet) {
        Map<UUID, SignalEdgeGroup> signalEdgeGroups = Create.RAILWAYS.signalEdgeGroups;
        List<UUID> ids = packet.ids();
        for (int i = 0; i < ids.size(); i++) {
            UUID id = ids.get(i);
            if (!packet.add()) {
                signalEdgeGroups.remove(id);
                continue;
            }

            SignalEdgeGroup group = new SignalEdgeGroup(id);
            signalEdgeGroups.put(id, group);
            if (i < packet.colors().size())
                group.color = packet.colors().get(i);
        }
    }

    @Override
    public void onRemoveTrain(RemoveTrainPacket packet) {
        Create.RAILWAYS.trains.remove(packet.id());
    }

    @Override
    public void onRemoveBlockEntity(ClientGamePacketListener listener, RemoveBlockEntityPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        if (mc.level.getBlockEntity(packet.pos()) instanceof SyncedBlockEntity be) {
            if (!be.hasLevel()) {
                be.setRemoved();
                return;
            }

            be.getLevel().removeBlockEntity(packet.pos());
        }
    }

    @Override
    public void onTrainPrompt(TrainPromptPacket packet) {
        TrainHUD.currentPrompt = packet.text();
        TrainHUD.currentPromptShadow = packet.shadow();
        TrainHUD.promptKeepAlive = 30;
    }

    @Override
    public void onContraptionRelocation(ContraptionRelocationPacket packet) {
        if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof OrientedContraptionEntity oce) {
            oce.nonDamageTicks = 10;
        }
    }

    @Override
    public void onTrackGraphRollCall(TrackGraphRollCallPacket packet) {
        GlobalRailwayManager manager = Create.RAILWAYS;
        Set<UUID> unusedIds = new HashSet<>(manager.trackNetworks.keySet());
        List<Integer> failedIds = new ArrayList<>();
        Map<Integer, UUID> idByNetId = new HashMap<>();
        manager.trackNetworks.forEach((uuid, g) -> idByNetId.put(g.netId, uuid));

        for (TrackGraphRollCallPacket.Entry entry : packet.entries()) {
            UUID uuid = idByNetId.get(entry.netId());
            if (uuid == null) {
                failedIds.add(entry.netId());
                continue;
            }
            unusedIds.remove(uuid);
            TrackGraph trackGraph = manager.trackNetworks.get(uuid);
            if (trackGraph.getChecksum() == entry.checksum())
                continue;
            LOGGER.warn("Track network: {} failed its checksum; Requesting refresh", uuid.toString().substring(0, 6));
            failedIds.add(entry.netId());
        }

        ClientPacketListener networkHandler = Minecraft.getInstance().player.connection;
        for (Integer failed : failedIds)
            networkHandler.send(new TrackGraphRequestPacket(failed));
        for (UUID unused : unusedIds)
            manager.trackNetworks.remove(unused);
    }

    @Override
    public void onArmPlacementRequest(ArmPlacementRequestPacket packet) {
        ArmInteractionPointHandler.flushSettings(Minecraft.getInstance().player, packet.pos());
    }

    @Override
    public void onEjectorPlacementRequest(EjectorPlacementRequestPacket packet) {
        EjectorTargetHandler.flushSettings(packet.pos());
    }

    @Override
    public void onPackagePortPlacementRequest(PackagePortPlacementRequestPacket packet) {
        PackagePortTargetSelectionHandler.flushSettings(Minecraft.getInstance().player, packet.pos());
    }

    @Override
    public void onContraptionDisableActor(ClientGamePacketListener listener, ContraptionDisableActorPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        Entity entityByID = mc.level.getEntity(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;

        Contraption contraption = ace.getContraption();
        List<ItemStack> disabledActors = contraption.getDisabledActors();
        ItemStack filter = packet.filter();
        if (filter.isEmpty())
            disabledActors.clear();

        if (!packet.enable()) {
            disabledActors.add(filter);
            contraption.setActorsActive(filter, false);
            return;
        }

        disabledActors.removeIf(next -> ContraptionControlsMovement.isSameFilter(next, filter) || next.isEmpty());

        contraption.setActorsActive(filter, true);
    }

    @Override
    public void onAttachedComputer(ClientGamePacketListener listener, AttachedComputerPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        if (mc.level.getBlockEntity(packet.pos()) instanceof SmartBlockEntity be) {
            be.getBehaviour(AbstractComputerBehaviour.TYPE).setHasAttachedComputer(packet.hasAttachedComputer());
        }
    }

    @Override
    public void onServerDebugInfo(ServerDebugInfoPacket packet) {
        StringBuilder output = new StringBuilder();
        List<DebugInfoSection> clientInfo = DebugInformation.getClientInfo();

        Minecraft mc = Minecraft.getInstance();
        ServerDebugInfoPacket.printInfo("Client", mc.player, clientInfo, output);
        output.append("\n\n");
        output.append(packet.serverInfo());

        String text = output.toString();
        mc.keyboardHandler.setClipboard(text);
        mc.player.displayClientMessage(
            Component.translatable("create.command.debuginfo.saved_to_clipboard")
                .withColor(DyeHelper.getDyeColors(DyeColor.LIME).getFirst()), false
        );
    }

    @Override
    public void onPackageDestroy(ClientGamePacketListener listener, PackageDestroyPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        ClientLevel world = mc.level;
        Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, world.getRandom(), .125f);
        Vec3 pos = packet.location().add(motion.scale(4));
        world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, packet.box()), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    @Override
    public void onFactoryPanelEffect(ClientGamePacketListener listener, FactoryPanelEffectPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        ClientLevel world = mc.level;
        BlockState blockState = world.getBlockState(packet.fromPos().pos());
        if (!blockState.is(AllBlocks.FACTORY_GAUGE))
            return;
        ServerFactoryPanelBehaviour panelBehaviour = ServerFactoryPanelBehaviour.at(world, packet.toPos());
        if (panelBehaviour != null) {
            panelBehaviour.bulb.setValue(1);
            FactoryPanelConnection connection = panelBehaviour.targetedBy.get(packet.fromPos());
            if (connection != null)
                connection.success = packet.success();
        }
    }

    @Override
    public void onRedstoneRequesterEffect(ClientGamePacketListener listener, RedstoneRequesterEffectPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        if (mc.level.getBlockEntity(packet.pos()) instanceof RedstoneRequesterBlockEntity plbe) {
            plbe.playEffect(packet.success());
        }
    }

    @Override
    public void onClientboundChainConveyorRiding(ClientboundChainConveyorRidingPacket packet) {
        PlayerSkyhookRenderer.updatePlayerList(packet.uuids());
    }

    @Override
    public void onShopUpdate(ClientGamePacketListener listener, ShopUpdatePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        forceMainThread(listener, mc, packet);
        if (mc.level.getBlockEntity(packet.pos()) instanceof TableClothBlockEntity blockEntity) {
            if (!blockEntity.hasLevel()) {
                return;
            }

            blockEntity.invalidateItemsForRender();
        }
    }

    @Override
    public void onTrackGraphSync(TrackGraphSyncPacket packet) {
        GlobalRailwayManager manager = Create.RAILWAYS;
        TrackGraph graph = manager.getOrCreateGraph(packet.graphId, packet.netId);
        manager.version++;

        if (packet.packetDeletesGraph) {
            manager.removeGraph(graph);
            return;
        }

        if (packet.fullWipe) {
            manager.removeGraph(graph);
            graph = Create.RAILWAYS.sided(null).getOrCreateGraph(packet.graphId, packet.netId);
        }

        for (int nodeId : packet.removedNodes) {
            TrackNode node = graph.getNode(nodeId);
            if (node != null)
                graph.removeNode(null, node.getLocation());
        }

        for (Map.Entry<Integer, Pair<TrackNodeLocation, Vec3>> entry : packet.addedNodes.entrySet()) {
            Integer nodeId = entry.getKey();
            Pair<TrackNodeLocation, Vec3> nodeLocation = entry.getValue();
            graph.loadNode(nodeLocation.getFirst(), nodeId, nodeLocation.getSecond());
        }

        for (Pair<Pair<Couple<Integer>, TrackMaterial>, BezierConnection> pair : packet.addedEdges) {
            Couple<TrackNode> nodes = pair.getFirst().getFirst().map(graph::getNode);
            TrackNode node1 = nodes.getFirst();
            TrackNode node2 = nodes.getSecond();
            if (node1 != null && node2 != null)
                graph.putConnection(node1, node2, new TrackEdge(node1, node2, pair.getSecond(), pair.getFirst().getSecond()));
        }

        for (TrackEdgePoint edgePoint : packet.addedEdgePoints)
            graph.edgePoints.put(edgePoint.getType(), edgePoint);

        for (UUID uuid : packet.removedEdgePoints)
            for (EdgePointType<?> type : EdgePointType.TYPES.values())
                graph.edgePoints.remove(type, uuid);

        handleEdgeData(packet.updatedEdgeData, graph);

        if (!packet.splitSubGraphs.isEmpty())
            graph.findDisconnectedGraphs(null, packet.splitSubGraphs).forEach(manager::putGraph);
    }

    private void handleEdgeData(Map<Couple<Integer>, Pair<Integer, List<UUID>>> updatedEdgeData, TrackGraph graph) {
        for (Map.Entry<Couple<Integer>, Pair<Integer, List<UUID>>> entry : updatedEdgeData.entrySet()) {
            List<UUID> idList = entry.getValue().getSecond();
            int groupType = entry.getValue().getFirst();

            Couple<TrackNode> nodes = entry.getKey().map(graph::getNode);
            if (nodes.either(Objects::isNull))
                continue;
            TrackEdge edge = graph.getConnectionsFrom(nodes.getFirst()).get(nodes.getSecond());
            if (edge == null)
                continue;

            EdgeData edgeData = new EdgeData(edge);
            if (groupType == TrackGraphSyncPacket.NULL_GROUP)
                edgeData.setSingleSignalGroup(null, null, null);
            else if (groupType == TrackGraphSyncPacket.PASSIVE_GROUP)
                edgeData.setSingleSignalGroup(null, null, EdgeData.passiveGroup);
            else
                edgeData.setSingleSignalGroup(null, null, idList.getFirst());

            List<TrackEdgePoint> points = edgeData.getPoints();
            edge.edgeData = edgeData;

            for (int i = groupType == TrackGraphSyncPacket.GROUP ? 1 : 0; i < idList.size(); i++) {
                UUID uuid = idList.get(i);
                for (EdgePointType<?> type : EdgePointType.TYPES.values()) {
                    TrackEdgePoint point = graph.edgePoints.get(type, uuid);
                    if (point == null)
                        continue;
                    points.add(point);
                    break;
                }
            }
        }
    }

    @Override
    public void onAddTrain(AddTrainPacket packet) {
        Train train = packet.train();
        Create.RAILWAYS.trains.put(train.id, train);
    }

    @Override
    public void onOpenScreen(ClientGamePacketListener listener, OpenScreenPacket packet) {
        if (listener instanceof ClientPacketListener handler) {
            Minecraft mc = Minecraft.getInstance();
            PacketUtils.ensureRunningOnSameThread(packet, handler, mc.packetProcessor());
            RegistryFriendlyByteBuf extraData = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(packet.data()), handler.registryAccess());
            AllMenuScreens.open(mc, packet.menu(), packet.id(), packet.name(), extraData);
            extraData.release();
        }
    }

    @Override
    public void onBlueprintPreview(BlueprintPreviewPacket packet) {
        BlueprintOverlayRenderer.updatePreview(packet.available(), packet.missing(), packet.result());
    }

    @Override
    public void buildDebugInfo() {
        DebugInfoSection.builder("Graphics").put("Flywheel Version", DebugInformation.getVersionOfMod(Flywheel.MOD_ID))
            .put("Flywheel Backend", () -> Backend.REGISTRY.getIdOrThrow(BackendManager.currentBackend()).toString())
            .put("OpenGL Renderer", GlStateManager._getString(GL11.GL_RENDERER)).put("OpenGL Version", GlStateManager._getString(GL11.GL_VERSION))
            .put("Transparency", () -> Minecraft.getInstance().options.improvedTransparency().get() ? "shader" : "regular")
            .buildTo(DebugInformation::registerClientInfo);
    }

    @Override
    public Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public void queueUpdate(BlockEntity entity) {
        VisualizationHelper.queueUpdate(entity);
    }

    @Override
    public void addAirFlowParticle(Level world, BlockPos airCurrentPos, double x, double y, double z) {
        if (world.random.nextFloat() < AllConfigs.client().fanParticleDensity.get())
            world.addParticle(new AirFlowParticleData(airCurrentPos), x, y, z, 0, 0, 0);
    }

    @Override
    public void enableClientPlayerSound(Entity entity, float clamp) {
        AirCurrentClient.enableClientPlayerSound(entity, clamp);
    }

    @Override
    public void addBehaviours(SmartBlockEntity blockEntity, ArrayList<BlockEntityBehaviour<?>> behaviours) {
        AllBlockEntityBehaviours.addBehaviours(blockEntity, behaviours);
    }

    @Override
    public void addBehaviours(Entity entity, ArrayList<EntityBehaviour<?>> behaviours) {
        AllEntityBehaviours.addBehaviours(entity, behaviours);
    }

    @Override
    public void showWaterBounds(Axis axis, BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 contract = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis).getUnitVec3i());
        Outliner.getInstance().showAABB(Pair.of("waterwheel", pos), new AABB(pos).inflate(1).deflate(contract.x, contract.y, contract.z))
            .colored(0xFF_ff5d6c);
        CreateLang.translate("large_water_wheel.not_enough_space").color(0xFF_ff5d6c).sendStatus(context.getPlayer());
    }

    @Override
    public float getServerSpeed() {
        return ServerSpeedProvider.get();
    }

    @Override
    public void resetClientContraption(Contraption contraption) {
        ClientContraption.resetClientContraption(contraption);
    }

    @Override
    public void invalidateClientContraptionChildren(Contraption contraption) {
        ClientContraption.invalidateClientContraptionChildren(contraption);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntityClientSide(Contraption contraption, BlockPos localPos) {
        return ClientContraption.getBlockEntityClientSide(contraption, localPos);
    }

    @Override
    public void spawnPipeParticles(Level world, BlockPos pos, PipeConnection.Flow flow, boolean openEnd, Direction side, int amount) {
        Minecraft mc = Minecraft.getInstance();
        if (world == mc.level)
            if (isRenderEntityWithoutDistance(mc, pos))
                return;
        if (openEnd)
            spawnPouringLiquid(world, pos, flow, side, amount);
        else if (world.random.nextFloat() < PipeConnection.IDLE_PARTICLE_SPAWN_CHANCE)
            spawnRimParticles(world, pos, flow.fluid, side, amount);
    }

    private static boolean isRenderEntityWithoutDistance(Minecraft mc, BlockPos pos) {
        Entity renderViewEntity = mc.getCameraEntity();
        if (renderViewEntity == null)
            return true;
        Vec3 center = VecHelper.getCenterOf(pos);
        return renderViewEntity.position().distanceTo(center) > PipeConnection.MAX_PARTICLE_RENDER_DISTANCE;
    }

    private static void spawnRimParticles(Level world, BlockPos pos, FluidStack fluid, Direction side, int amount) {
        ParticleOptions particle = FluidFX.getDrippingParticle(fluid);
        FluidFX.spawnRimParticles(world, pos, side, amount, particle, PipeConnection.RIM_RADIUS);
    }

    private static void spawnPouringLiquid(Level world, BlockPos pos, PipeConnection.Flow flow, Direction side, int amount) {
        ParticleOptions particle = FluidFX.getFluidParticle(flow.fluid);
        Vec3 directionVec = Vec3.atLowerCornerOf(side.getUnitVec3i());
        FluidFX.spawnPouringLiquid(world, pos, amount, particle, PipeConnection.RIM_RADIUS, directionVec, flow.inbound);
    }

    @Override
    public void spawnSteamEngineParticles(SteamEngineBlockEntity be) {
        Float targetAngle = SteamEngineRenderer.getTargetAngle(be);
        PoweredShaftBlockEntity ste = be.target.get();
        if (ste == null)
            return;
        if (!ste.isPoweredBy(be.getBlockPos()) || ste.engineEfficiency == 0)
            return;
        if (targetAngle == null)
            return;

        float angle = AngleHelper.deg(targetAngle);
        angle += (angle < 0) ? -180 + 75 : 360 - 75;
        angle %= 360;

        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft == null || shaft.getSpeed() == 0)
            return;

        if (angle >= 0 && !(be.prevAngle > 180 && angle < 180)) {
            be.prevAngle = angle;
            return;
        }
        if (angle < 0 && !(be.prevAngle < -180 && angle > -180)) {
            be.prevAngle = angle;
            return;
        }

        FluidTankBlockEntity sourceBE = be.source.get();
        if (sourceBE != null) {
            FluidTankBlockEntity controller = sourceBE.getControllerBE();
            if (controller != null && controller.boiler != null) {
                controller.boiler.queueSoundOnSide(be.getBlockPos(), SteamEngineBlock.getFacing(be.getBlockState()));
            }
        }

        Direction facing = SteamEngineBlock.getFacing(be.getBlockState());

        Level world = be.getLevel();
        Vec3 offset = VecHelper.rotate(
            new Vec3(0, 0, 1).add(VecHelper.offsetRandomly(Vec3.ZERO, world.random, 1).multiply(1, 1, 0).normalize().scale(.5f)),
            AngleHelper.verticalAngle(facing),
            Axis.X
        );
        offset = VecHelper.rotate(offset, AngleHelper.horizontalAngle(facing), Axis.Y);
        Vec3 v = offset.scale(.5f).add(Vec3.atCenterOf(be.getBlockPos()));
        Vec3 m = offset.subtract(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(.75f));
        world.addParticle(AllParticleTypes.STEAM_JET, v.x, v.y, v.z, m.x, m.y, m.z);

        be.prevAngle = angle;
    }

    @Override
    public void spawnSuperGlueParticles(Level world, BlockPos pos, Direction direction, boolean fullBlock) {
        SuperGlueSelectionHandler.spawnParticles(world, pos, direction, fullBlock);
    }

    @Override
    public void tickBlazeBurnerAnimation(BlazeBurnerBlockEntity be) {
        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            BlazeBurnerRenderer.tickAnimation(be);
        }
    }

    @Override
    public void sendPacket(Packet<ServerGamePacketListener> packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.connection.send(packet);
        }
    }

    @Override
    public void sendPacket(Player player, Packet<ServerGamePacketListener> packet) {
        if (player instanceof LocalPlayer clientPlayer) {
            clientPlayer.connection.send(packet);
        }
    }

    @Override
    public void createBasinFluidParticles(Level world, BasinBlockEntity blockEntity) {
        RandomSource r = world.random;

        if (!blockEntity.visualizedOutputFluids.isEmpty())
            createBasinOutputFluidParticles(world, blockEntity, r);

        if (!blockEntity.areFluidsMoving && r.nextFloat() > 1 / 8f)
            return;

        int segments = 0;
        for (SmartFluidTankBehaviour behaviour : blockEntity.getTanks()) {
            if (behaviour == null)
                continue;
            for (SmartFluidTankBehaviour.TankSegment tankSegment : behaviour.getTanks())
                if (!tankSegment.isEmpty(0))
                    segments++;
        }
        if (segments < 2)
            return;

        float totalUnits = blockEntity.getTotalFluidUnits(0);
        if (totalUnits == 0)
            return;
        float fluidLevel = Mth.clamp(totalUnits / 162000, 0, 1);
        float rim = 2 / 16f;
        float space = 12 / 16f;
        BlockPos pos = blockEntity.getBlockPos();
        float surface = pos.getY() + rim + space * fluidLevel + 1 / 32f;

        if (blockEntity.areFluidsMoving) {
            createBasinMovingFluidParticles(world, blockEntity, surface, segments);
            return;
        }

        for (SmartFluidTankBehaviour behaviour : blockEntity.getTanks()) {
            if (behaviour == null)
                continue;
            for (SmartFluidTankBehaviour.TankSegment tankSegment : behaviour.getTanks()) {
                if (tankSegment.isEmpty(0))
                    continue;
                float x = pos.getX() + rim + space * r.nextFloat();
                float z = pos.getZ() + rim + space * r.nextFloat();
                FluidStack stack = tankSegment.getRenderedFluid();
                world.addAlwaysVisibleParticle(
                    new FluidParticleData(AllParticleTypes.BASIN_FLUID, stack.getFluid(), stack.getComponentChanges()),
                    x,
                    surface,
                    z,
                    0,
                    0,
                    0
                );
            }
        }
    }

    private static void createBasinOutputFluidParticles(Level world, BasinBlockEntity blockEntity, RandomSource r) {
        BlockState blockState = blockEntity.getBlockState();
        if (!(blockState.getBlock() instanceof BasinBlock))
            return;
        Direction direction = blockState.getValue(BasinBlock.FACING);
        if (direction == Direction.DOWN)
            return;
        Vec3 directionVec = Vec3.atLowerCornerOf(direction.getUnitVec3i());
        Vec3 outVec = VecHelper.getCenterOf(blockEntity.getBlockPos()).add(directionVec.scale(.65).subtract(0, 1 / 4f, 0));
        Vec3 outMotion = directionVec.scale(1 / 16f).add(0, -1 / 16f, 0);

        for (int i = 0; i < 2; i++) {
            blockEntity.visualizedOutputFluids.forEach(ia -> {
                FluidStack fluidStack = ia.getValue();
                ParticleOptions fluidParticle = FluidFX.getFluidParticle(fluidStack);
                Vec3 m = VecHelper.offsetRandomly(outMotion, r, 1 / 16f);
                world.addAlwaysVisibleParticle(fluidParticle, outVec.x, outVec.y, outVec.z, m.x, m.y, m.z);
            });
        }
    }

    private static void createBasinMovingFluidParticles(Level world, BasinBlockEntity blockEntity, float surface, int segments) {
        Vec3 pointer = new Vec3(1, 0, 0).scale(1 / 16f);
        float interval = 360f / segments;
        Vec3 centerOf = VecHelper.getCenterOf(blockEntity.getBlockPos());
        float intervalOffset = (AnimationTickHolder.getTicks() * 18) % 360;

        int currentSegment = 0;
        for (SmartFluidTankBehaviour behaviour : blockEntity.getTanks()) {
            if (behaviour == null)
                continue;
            for (SmartFluidTankBehaviour.TankSegment tankSegment : behaviour.getTanks()) {
                if (tankSegment.isEmpty(0))
                    continue;
                float angle = interval * (1 + currentSegment) + intervalOffset;
                Vec3 vec = centerOf.add(VecHelper.rotate(pointer, angle, Axis.Y));
                FluidStack stack = tankSegment.getRenderedFluid();
                world.addAlwaysVisibleParticle(
                    new FluidParticleData(AllParticleTypes.BASIN_FLUID, stack.getFluid(), stack.getComponentChanges()),
                    vec.x(),
                    surface,
                    vec.z(),
                    1,
                    0,
                    0
                );
                currentSegment++;
            }
        }
    }

    @Override
    public void cartClicked(Player player, AbstractMinecart minecart) {
        CouplingHandlerClient.onCartClicked((LocalPlayer) player, minecart);
    }

    @Override
    public void advertiseToAddressHelper(ClipboardBlockEntity blockEntity) {
        AddressEditBoxHelper.advertiseClipboard(blockEntity);
    }

    @Override
    public void updateClipboardScreen(UUID lastEdit, BlockPos pos, ClipboardContent content) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ClipboardScreen cs))
            return;
        if (lastEdit != null && mc.player.getUUID().equals(lastEdit))
            return;
        if (!pos.equals(cs.targetedBlock))
            return;
        cs.reopenWith(content);
    }

    @Override
    public GlobalRailwayManager getGlobalRailwayManager() {
        return Create.RAILWAYS;
    }

    @Override
    public void registerToCurveInteraction(TrackBlockEntity be) {
        TrackBlockOutline.registerToCurveInteraction(be);
    }

    @Override
    public void removeFromCurveInteraction(TrackBlockEntity be) {
        TrackBlockOutline.removeFromCurveInteraction(be);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invalidateCarriage(CarriageContraptionEntity entity) {
        // Update the portal cutoff first to ensure it's reflected in the updated mesh.
        entity.updateRenderedPortalCutoff();
        AtomicReference<ClientContraption> clientContraption = (AtomicReference<ClientContraption>) entity.getContraption().clientContraption;
        ClientContraption maybeNullClientContraption = clientContraption.getAcquire();
        // Nothing to invalidate if it hasn't been created yet.
        if (maybeNullClientContraption != null) {
            maybeNullClientContraption.invalidateStructure();
            maybeNullClientContraption.invalidateChildren();
        }
    }

    @Override
    public void startControlling(Player player, AbstractContraptionEntity be, BlockPos pos) {
        ControlsHandler.startControlling((LocalPlayer) player, be, pos);
    }

    @Override
    public void tickBlazeBurnerMovement(MovementContext context) {
        BlazeBurnerMovementRenderBehaviour render = AllMovementBehaviours.BLAZE_BURNER.getAttachRender();
        render.tick(context);
    }

    @Override
    public void cannonDontAnimateItem(InteractionHand hand) {
        Create.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
    }

    @Override
    public void tryToggleActive(LecternControllerBlockEntity controller) {
        LocalPlayer player = Minecraft.getInstance().player;
        UUID uuid = player.getUUID();
        if (controller.user == null && uuid.equals(controller.prevUser)) {
            LinkedControllerClientHandler.deactivateInLectern(player);
        } else if (controller.prevUser == null && uuid.equals(controller.user)) {
            LinkedControllerClientHandler.activateInLectern(controller.getBlockPos());
        }
    }

    @Override
    public void toggleLinkedControllerBindMode(BlockPos pos) {
        LinkedControllerClientHandler.toggleBindMode(Minecraft.getInstance().player, pos);
    }

    @Override
    public void toggleLinkedControllerActive() {
        LinkedControllerClientHandler.toggle(Minecraft.getInstance().player);
    }

    @Override
    public void factoryPanelMoveToSlot(SmartBlockEntity be, PanelSlot slot) {
        FactoryPanelBehaviour behaviour = (FactoryPanelBehaviour) be.getBehaviour(FilteringBehaviour.TYPE);
        if (behaviour.getSlotPositioning() instanceof FactoryPanelSlotPositioning fpsp) {
            fpsp.slot = slot;
        }
    }

    @Override
    public boolean factoryPanelClicked(Level world, Player player, ServerFactoryPanelBehaviour behaviour) {
        return FactoryPanelConnectionHandler.panelClicked(world, player, behaviour);
    }

    @Override
    public void zapperDontAnimateItem(InteractionHand hand) {
        Create.ZAPPER_RENDER_HANDLER.dontAnimateItem(hand);
    }

    @Override
    public void openSequencedGearshiftScreen(SequencedGearshiftBlockEntity be) {
        ScreenOpener.open(new SequencedGearshiftScreen(be));
    }

    @Override
    public void openClipboardScreen(Player player, DataComponentMap components, BlockPos pos) {
        if (Minecraft.getInstance().player == player)
            ScreenOpener.open(new ClipboardScreen(player.getInventory().getSelectedSlot(), components, pos));
    }

    @Override
    public void openDisplayLinkScreen(DisplayLinkBlockEntity be, Player player) {
        if (!(player instanceof LocalPlayer))
            return;
        if (be.targetOffset.equals(BlockPos.ZERO)) {
            player.displayClientMessage(CreateLang.translateDirect("display_link.invalid"), true);
            return;
        }
        ScreenOpener.open(new DisplayLinkScreen(be));
    }

    @Override
    public void openThresholdSwitchScreen(ThresholdSwitchBlockEntity be, Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new ThresholdSwitchScreen(be));
    }

    @Override
    public void openElevatorContactScreen(ElevatorContactBlockEntity be, Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new ElevatorContactScreen(be.getBlockPos(), be.shortName, be.longName, be.doorControls.mode));
    }

    @Override
    public void openStationScreen(Level world, BlockPos pos, Player player) {
        if (!(player instanceof LocalPlayer)) {
            return;
        }
        if (world.getBlockEntity(pos) instanceof StationBlockEntity be) {
            GlobalStation station = be.getStation();
            BlockState blockState = be.getBlockState();
            if (station == null || blockState == null)
                return;
            boolean assembling = blockState.getBlock() == AllBlocks.TRACK_STATION && blockState.getValue(StationBlock.ASSEMBLING);
            ScreenOpener.open(assembling ? new AssemblyScreen(be, station) : new StationScreen(be, station));
        }
    }

    @Override
    public void openFactoryPanelScreen(ServerFactoryPanelBehaviour behaviour, Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new FactoryPanelScreen(behaviour));
    }

    @Override
    public void openSymmetryWandScreen(ItemStack stack, InteractionHand hand) {
        ScreenOpener.open(new SymmetryWandScreen(stack, hand));
    }

    @Override
    public void openSchematicEditScreen() {
        ScreenOpener.open(new SchematicEditScreen());
    }

    @Override
    public void openWorldshaperScreen(ItemStack item, InteractionHand hand) {
        ScreenOpener.open(new WorldshaperScreen(item, hand));
    }
}
