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
import com.zurrtum.create.client.content.contraptions.render.ContraptionRenderInfo;
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
import com.zurrtum.create.client.foundation.entity.behaviour.PortalCutoffBehaviour;
import com.zurrtum.create.client.foundation.render.PlayerSkyhookRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.client.foundation.utility.ServerSpeedProvider;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.contraptions.gantry.GantryContraptionEntity;
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
import com.zurrtum.create.infrastructure.debugInfo.DebugInformation;
import com.zurrtum.create.infrastructure.debugInfo.element.DebugInfoSection;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.packet.c2s.TrackGraphRequestPacket;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import com.zurrtum.create.infrastructure.particle.AirFlowParticleData;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import com.zurrtum.create.infrastructure.particle.SteamJetParticleData;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.LOGGER;

public class AllHandle extends AllClientHandle<ClientPlayNetworkHandler> {

    public static void register() {
        AllClientHandle.INSTANCE = new AllHandle();
    }

    @Override
    protected void forceMainThread(ClientPlayNetworkHandler listener, S2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.client);
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public void onSymmetryEffect(ClientPlayNetworkHandler listener, SymmetryEffectPacket packet) {
        MinecraftClient client = listener.client;
        BlockPos mirror = packet.mirror();
        if (client.player.getPos().distanceTo(Vec3d.of(mirror)) > 100)
            return;
        for (BlockPos to : packet.positions())
            SymmetryHandlerClient.drawEffect(client, mirror, to);
    }

    @Override
    public void onLogisticalStockResponse(ClientPlayNetworkHandler listener, LogisticalStockResponsePacket packet) {
        if (listener.world.getBlockEntity(packet.pos()) instanceof StockTickerBlockEntity stbe) {
            stbe.receiveStockPacket(packet.items(), packet.lastPacket());
        }
    }

    @Override
    public void onTrainEditReturn(ClientPlayNetworkHandler clientPlayNetworkHandler, TrainEditReturnPacket packet) {
        Train train = Create.RAILWAYS.trains.get(packet.id());
        if (train == null)
            return;
        if (!packet.name().isBlank()) {
            train.name = Text.literal(packet.name());
        }
        train.icon = TrainIconType.byId(packet.iconType());
        train.mapColorIndex = packet.mapColor();
    }

    @Override
    public void onTrainHUDControlUpdate(ClientPlayNetworkHandler listener, TrainHUDControlUpdatePacket packet) {
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
    public void onTrainHonkReturn(ClientPlayNetworkHandler listener, HonkReturnPacket packet) {
        Train train = Create.RAILWAYS.trains.get(packet.trainId());
        if (train == null)
            return;

        if (packet.isHonk())
            train.honkTicks = train.honkTicks == 0 ? 20 : 13;
        else
            train.honkTicks = train.honkTicks > 5 ? 6 : 0;
    }

    @Override
    public void onElevatorFloorList(ClientPlayNetworkHandler listener, ElevatorFloorListPacket packet) {
        Entity entityByID = listener.world.getEntityById(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;
        if (!(ace.getContraption() instanceof ElevatorContraption ec))
            return;

        ec.namesList = packet.floors();
        ec.syncControlDisplays();
    }

    @Override
    public void onContraptionColliderLock(ClientPlayNetworkHandler listener, ContraptionColliderLockPacket packet) {
        ContraptionColliderClient.lockPacketReceived(packet.contraption(), packet.sender(), packet.offset());
    }

    @Override
    public void onWiFiEffect(ClientPlayNetworkHandler listener, WiFiEffectPacket packet) {
        BlockEntity blockEntity = listener.world.getBlockEntity(packet.pos());
        if (blockEntity instanceof PackagerLinkBlockEntity plbe)
            plbe.playEffect();
        if (blockEntity instanceof StockTickerBlockEntity plbe)
            plbe.playEffect();
    }

    @Override
    public void onControlsStopControlling(ClientPlayNetworkHandler listener) {
        ControlsHandler.stopControlling(listener.client.player);
    }

    @Override
    public void onServerSpeed(ClientPlayNetworkHandler listener, ServerSpeedPacket packet) {
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
        ClientPlayNetworkHandler listener,
        Vec3d location,
        Hand hand,
        boolean self,
        T handler,
        Consumer<T> handleAdditional
    ) {
        Entity renderViewEntity = listener.client.getCameraEntity();
        if (renderViewEntity == null)
            return;
        if (renderViewEntity.getPos().distanceTo(location) > 100)
            return;

        handleAdditional.accept(handler);
        if (self)
            handler.shoot(hand, location);
        else
            handler.playSound(hand, location);
    }

    @Override
    public void onZapperBeam(ClientPlayNetworkHandler listener, ZapperBeamPacket packet) {
        onShootGadget(
            listener, packet.location(), packet.hand(), packet.self(), Create.ZAPPER_RENDER_HANDLER, handler -> {
                handler.addBeam(listener.client, new LaserBeam(packet.location(), packet.target()));
            }
        );
    }

    @Override
    public void onPotatoCannon(ClientPlayNetworkHandler listener, PotatoCannonPacket packet) {
        onShootGadget(
            listener, packet.location(), packet.hand(), packet.self(), Create.POTATO_CANNON_RENDER_HANDLER, handler -> {
                handler.beforeShoot(packet.pitch(), packet.location(), packet.motion(), packet.item());
            }
        );
    }

    @Override
    public void onContraptionStall(ClientPlayNetworkHandler listener, ContraptionStallPacket packet) {
        if (listener.world.getEntityById(packet.entityId()) instanceof AbstractContraptionEntity ce) {
            ce.handleStallInformation(packet.x(), packet.y(), packet.z(), packet.angle());
        }
    }

    @Override
    public void onContraptionDisassembly(ClientPlayNetworkHandler listener, ContraptionDisassemblyPacket packet) {
        if (listener.world.getEntityById(packet.entityId()) instanceof AbstractContraptionEntity ce) {
            ce.moveCollidedEntitiesOnDisassembly(packet.transform());
        }
    }

    @Override
    public void onContraptionBlockChanged(ClientPlayNetworkHandler listener, ContraptionBlockChangedPacket packet) {
        if (listener.world.getEntityById(packet.entityId()) instanceof AbstractContraptionEntity ce) {
            ce.handleBlockChange(packet.localPos(), packet.newState());
        }
    }

    @Override
    public void onGlueEffect(ClientPlayNetworkHandler listener, GlueEffectPacket packet) {
        ClientPlayerEntity player = listener.client.player;
        if (!player.getBlockPos().isWithinDistance(packet.pos(), 100))
            return;
        SuperGlueSelectionHandler.spawnParticles(player.clientWorld, packet.pos(), packet.direction(), packet.fullBlock());
    }

    @Override
    public void onContraptionSeatMapping(ClientPlayNetworkHandler listener, ContraptionSeatMappingPacket packet) {
        ClientPlayerEntity player = listener.client.player;
        Entity entityByID = player.clientWorld.getEntityById(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
            return;

        if (packet.dismountedId() == player.getId()) {
            Vec3d transformedVector = contraptionEntity.getPassengerPosition(player, 1);
            if (transformedVector != null)
                AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(player, Optional.of(transformedVector));
        }

        contraptionEntity.getContraption().setSeatMapping(new HashMap<>(packet.mapping()));
    }

    @Override
    public void onLimbSwingUpdate(ClientPlayNetworkHandler listener, LimbSwingUpdatePacket packet) {
        ClientWorld world = listener.getWorld();
        Entity entity = world.getEntityById(packet.entityId());
        if (!(entity instanceof PlayerEntity player))
            return;
        AllSynchedDatas.LAST_OVERRIDE_LIMB_SWING_UPDATE.set(player, 0);
        AllSynchedDatas.OVERRIDE_LIMB_SWING.set(player, packet.limbSwing());
        Vec3d position = packet.position();
        player.updateTrackedPositionAndAngles(position, player.getYaw(), player.getPitch());
    }

    @Override
    public void onFluidSplash(ClientPlayNetworkHandler listener, FluidSplashPacket packet) {
        BlockPos pos = packet.pos();
        if (listener.client.player.getPos().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ())) > 100) {
            return;
        }
        FluidFX.splash(pos, packet.fluid());
    }

    @Override
    public void onMountedStorageSync(ClientPlayNetworkHandler listener, MountedStorageSyncPacket packet) {
        Entity entity = listener.world.getEntityById(packet.contraptionId());
        if (!(entity instanceof AbstractContraptionEntity contraption))
            return;

        contraption.getContraption().getStorage().handleSync(packet, contraption);
    }

    @Override
    public void onGantryContraptionUpdate(ClientPlayNetworkHandler listener, GantryContraptionUpdatePacket packet) {
        Entity entity = listener.world.getEntityById(packet.entityID());
        if (!(entity instanceof GantryContraptionEntity ce)) {
            return;
        }
        ce.axisMotion = packet.motion();
        ce.clientOffsetDiff = packet.coord() - ce.getAxisCoord();
        ce.sequencedOffsetLimit = packet.sequenceLimit();
    }

    @Override
    public void onHighlight(ClientPlayNetworkHandler listener, HighlightPacket packet) {
        if (!listener.world.isPosLoaded(packet.pos())) {
            return;
        }

        Outliner.getInstance().showAABB("highlightCommand", VoxelShapes.fullCube().getBoundingBox().offset(packet.pos()), 200).lineWidth(1 / 32f)
            .colored(0xEeEeEe)
            // .colored(0x243B50)
            .withFaceTexture(AllSpecialTextures.SELECTION);
    }

    @Override
    public void onTunnelFlap(ClientPlayNetworkHandler listener, TunnelFlapPacket packet) {
        if (listener.world.getBlockEntity(packet.pos()) instanceof BeltTunnelBlockEntity blockEntity) {
            packet.flaps().forEach(flap -> {
                blockEntity.flap(flap.getFirst(), flap.getSecond());
            });
        }
    }

    @Override
    public void onFunnelFlap(ClientPlayNetworkHandler listener, FunnelFlapPacket packet) {
        if (listener.world.getBlockEntity(packet.pos()) instanceof FunnelBlockEntity blockEntity) {
            blockEntity.flap(packet.inwards());
        }
    }

    @Override
    public void onSoulPulseEffect(ClientPlayNetworkHandler listener, SoulPulseEffectPacket packet) {
        Create.SOUL_PULSE_EFFECT_HANDLER.addPulse(new SoulPulseEffect(packet.pos(), packet.distance(), packet.canOverlap()));
    }

    @Override
    public void onSignalEdgeGroup(ClientPlayNetworkHandler listener, SignalEdgeGroupPacket packet) {
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
    public void onRemoveTrain(ClientPlayNetworkHandler listener, RemoveTrainPacket packet) {
        Create.RAILWAYS.trains.remove(packet.id());
    }

    @Override
    public void onRemoveBlockEntity(ClientPlayNetworkHandler listener, RemoveBlockEntityPacket packet) {
        if (listener.world.getBlockEntity(packet.pos()) instanceof SyncedBlockEntity be) {
            if (!be.hasWorld()) {
                be.markRemoved();
                return;
            }

            be.getWorld().removeBlockEntity(packet.pos());
        }
    }

    @Override
    public void onTrainPrompt(ClientPlayNetworkHandler listener, TrainPromptPacket packet) {
        TrainHUD.currentPrompt = packet.text();
        TrainHUD.currentPromptShadow = packet.shadow();
        TrainHUD.promptKeepAlive = 30;
    }

    @Override
    public void onContraptionRelocation(ClientPlayNetworkHandler listener, ContraptionRelocationPacket packet) {
        if (listener.world.getEntityById(packet.entityId()) instanceof OrientedContraptionEntity oce) {
            oce.nonDamageTicks = 10;
        }
    }

    @Override
    public void onTrackGraphRollCall(ClientPlayNetworkHandler listener, TrackGraphRollCallPacket packet) {
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

        for (Integer failed : failedIds)
            listener.sendPacket(new TrackGraphRequestPacket(failed));
        for (UUID unused : unusedIds)
            manager.trackNetworks.remove(unused);
    }

    @Override
    public void onArmPlacementRequest(ClientPlayNetworkHandler listener, ArmPlacementRequestPacket packet) {
        ArmInteractionPointHandler.flushSettings(listener.client.player, packet.pos());
    }

    @Override
    public void onEjectorPlacementRequest(ClientPlayNetworkHandler listener, EjectorPlacementRequestPacket packet) {
        EjectorTargetHandler.flushSettings(listener, packet.pos());
    }

    @Override
    public void onPackagePortPlacementRequest(ClientPlayNetworkHandler listener, PackagePortPlacementRequestPacket packet) {
        PackagePortTargetSelectionHandler.flushSettings(listener.client.player, packet.pos());
    }

    @Override
    public void onContraptionDisableActor(ClientPlayNetworkHandler listener, ContraptionDisableActorPacket packet) {
        Entity entityByID = listener.world.getEntityById(packet.entityId());
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
    public void onAttachedComputer(ClientPlayNetworkHandler listener, AttachedComputerPacket packet) {
        //TODO
        //        if (listener.world.getBlockEntity(packet.pos()) instanceof SmartBlockEntity be) {
        //            sbe.getBehaviour(AbstractComputerBehaviour.TYPE).setHasAttachedComputer(packet.hasAttachedComputer());
        //        }
    }

    @Override
    public void onServerDebugInfo(ClientPlayNetworkHandler listener, ServerDebugInfoPacket packet) {
        StringBuilder output = new StringBuilder();
        List<DebugInfoSection> clientInfo = DebugInformation.getClientInfo();

        ServerDebugInfoPacket.printInfo("Client", listener.client.player, clientInfo, output);
        output.append("\n\n");
        output.append(packet.serverInfo());

        String text = output.toString();
        listener.client.keyboard.setClipboard(text);
        listener.client.player.sendMessage(
            Text.translatable("create.command.debuginfo.saved_to_clipboard")
                .withColor(DyeHelper.getDyeColors(DyeColor.LIME).getFirst()), false
        );
    }

    @Override
    public void onPackageDestroy(ClientPlayNetworkHandler listener, PackageDestroyPacket packet) {
        ClientWorld world = listener.world;
        Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.getRandom(), .125f);
        Vec3d pos = packet.location().add(motion.multiply(4));
        world.addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, packet.box()), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    @Override
    public void onFactoryPanelEffect(ClientPlayNetworkHandler listener, FactoryPanelEffectPacket packet) {
        ClientWorld world = listener.world;
        BlockState blockState = world.getBlockState(packet.fromPos().pos());
        if (!blockState.isOf(AllBlocks.FACTORY_GAUGE))
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
    public void onRedstoneRequesterEffect(ClientPlayNetworkHandler listener, RedstoneRequesterEffectPacket packet) {
        if (listener.world.getBlockEntity(packet.pos()) instanceof RedstoneRequesterBlockEntity plbe) {
            plbe.playEffect(packet.success());
        }
    }

    @Override
    public void onClientboundChainConveyorRiding(ClientPlayNetworkHandler listener, ClientboundChainConveyorRidingPacket packet) {
        PlayerSkyhookRenderer.updatePlayerList(packet.uuids());
    }

    @Override
    public void onShopUpdate(ClientPlayNetworkHandler listener, ShopUpdatePacket packet) {
        if (listener.world.getBlockEntity(packet.pos()) instanceof TableClothBlockEntity blockEntity) {
            if (!blockEntity.hasWorld()) {
                return;
            }

            blockEntity.invalidateItemsForRender();
        }
    }

    @Override
    public void onTrackGraphSync(ClientPlayNetworkHandler listener, TrackGraphSyncPacket packet) {
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

        for (Map.Entry<Integer, Pair<TrackNodeLocation, Vec3d>> entry : packet.addedNodes.entrySet()) {
            Integer nodeId = entry.getKey();
            Pair<TrackNodeLocation, Vec3d> nodeLocation = entry.getValue();
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
    public void onAddTrain(ClientPlayNetworkHandler listener, AddTrainPacket packet) {
        Train train = packet.train();
        Create.RAILWAYS.trains.put(train.id, train);
    }

    @Override
    public void onOpenScreen(ClientPlayNetworkHandler listener, OpenScreenPacket packet) {
        RegistryByteBuf extraData = new RegistryByteBuf(Unpooled.wrappedBuffer(packet.data()), listener.getRegistryManager());
        AllMenuScreens.open(listener.client, packet.type(), packet.id(), packet.name(), extraData);
        extraData.release();
    }

    @Override
    public void onBlueprintPreview(ClientPlayNetworkHandler listener, BlueprintPreviewPacket packet) {
        BlueprintOverlayRenderer.updatePreview(packet.available(), packet.missing(), packet.result());
    }

    @Override
    public void buildDebugInfo() {
        DebugInfoSection.builder("Graphics").put("Flywheel Version", DebugInformation.getVersionOfMod(Flywheel.MOD_ID))
            .put("Flywheel Backend", () -> Backend.REGISTRY.getIdOrThrow(BackendManager.currentBackend()).toString())
            .put("OpenGL Renderer", GlStateManager._getString(GL11.GL_RENDERER)).put("OpenGL Version", GlStateManager._getString(GL11.GL_VERSION))
            .put("Graphics Mode", () -> MinecraftClient.getInstance().options.getGraphicsMode().getValue().name().toLowerCase(Locale.ROOT))
            .buildTo(DebugInformation::registerClientInfo);
    }

    @Override
    public PlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    @Override
    public void queueUpdate(BlockEntity entity) {
        VisualizationHelper.queueUpdate(entity);
    }

    @Override
    public void addAirFlowParticle(World world, BlockPos airCurrentPos, double x, double y, double z) {
        if (world.random.nextFloat() < AllConfigs.client().fanParticleDensity.get())
            world.addParticleClient(new AirFlowParticleData(airCurrentPos), x, y, z, 0, 0, 0);
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
    public void showWaterBounds(Axis axis, ItemPlacementContext context) {
        BlockPos pos = context.getBlockPos();
        Vec3d contract = Vec3d.of(Direction.get(AxisDirection.POSITIVE, axis).getVector());
        Outliner.getInstance().showAABB(Pair.of("waterwheel", pos), new Box(pos).expand(1).contract(contract.x, contract.y, contract.z))
            .colored(0xFF_ff5d6c);
        CreateLang.translate("large_water_wheel.not_enough_space").color(0xFF_ff5d6c).sendStatus(context.getPlayer());
    }

    @Override
    public float getServerSpeed() {
        return ServerSpeedProvider.get();
    }

    @Override
    public void invalidate(Contraption contraption) {
        // The visual will handle this with flywheel on.
        if (!contraption.deferInvalidate || BackendManager.isBackendOn())
            return;
        contraption.deferInvalidate = false;
        ContraptionRenderInfo.invalidate(contraption);
    }

    @Override
    public void spawnPipeParticles(World world, BlockPos pos, PipeConnection.Flow flow, boolean openEnd, Direction side, int amount) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (world == mc.world)
            if (isRenderEntityWithoutDistance(mc, pos))
                return;
        if (openEnd)
            spawnPouringLiquid(world, pos, flow, side, amount);
        else if (world.random.nextFloat() < PipeConnection.IDLE_PARTICLE_SPAWN_CHANCE)
            spawnRimParticles(world, pos, flow.fluid, side, amount);
    }

    private static boolean isRenderEntityWithoutDistance(MinecraftClient mc, BlockPos pos) {
        Entity renderViewEntity = mc.getCameraEntity();
        if (renderViewEntity == null)
            return true;
        Vec3d center = VecHelper.getCenterOf(pos);
        return renderViewEntity.getPos().distanceTo(center) > PipeConnection.MAX_PARTICLE_RENDER_DISTANCE;
    }

    private static void spawnRimParticles(World world, BlockPos pos, FluidStack fluid, Direction side, int amount) {
        ParticleEffect particle = FluidFX.getDrippingParticle(fluid);
        FluidFX.spawnRimParticles(world, pos, side, amount, particle, PipeConnection.RIM_RADIUS);
    }

    private static void spawnPouringLiquid(World world, BlockPos pos, PipeConnection.Flow flow, Direction side, int amount) {
        ParticleEffect particle = FluidFX.getFluidParticle(flow.fluid);
        Vec3d directionVec = Vec3d.of(side.getVector());
        FluidFX.spawnPouringLiquid(world, pos, amount, particle, PipeConnection.RIM_RADIUS, directionVec, flow.inbound);
    }

    @Override
    public void spawnSteamEngineParticles(SteamEngineBlockEntity be) {
        Float targetAngle = SteamEngineRenderer.getTargetAngle(be);
        PoweredShaftBlockEntity ste = be.target.get();
        if (ste == null)
            return;
        if (!ste.isPoweredBy(be.getPos()) || ste.engineEfficiency == 0)
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
                controller.boiler.queueSoundOnSide(be.getPos(), SteamEngineBlock.getFacing(be.getCachedState()));
            }
        }

        Direction facing = SteamEngineBlock.getFacing(be.getCachedState());

        World world = be.getWorld();
        Vec3d offset = VecHelper.rotate(
            new Vec3d(0, 0, 1).add(VecHelper.offsetRandomly(Vec3d.ZERO, world.random, 1).multiply(1, 1, 0).normalize().multiply(.5f)),
            AngleHelper.verticalAngle(facing),
            Axis.X
        );
        offset = VecHelper.rotate(offset, AngleHelper.horizontalAngle(facing), Axis.Y);
        Vec3d v = offset.multiply(.5f).add(Vec3d.ofCenter(be.getPos()));
        Vec3d m = offset.subtract(Vec3d.of(facing.getVector()).multiply(.75f));
        world.addParticleClient(new SteamJetParticleData(1), v.x, v.y, v.z, m.x, m.y, m.z);

        be.prevAngle = angle;
    }

    @Override
    public void spawnSuperGlueParticles(World world, BlockPos pos, Direction direction, boolean fullBlock) {
        SuperGlueSelectionHandler.spawnParticles(world, pos, direction, fullBlock);
    }

    @Override
    public void tickBlazeBurnerAnimation(BlazeBurnerBlockEntity be) {
        if (!VisualizationManager.supportsVisualization(be.getWorld())) {
            BlazeBurnerRenderer.tickAnimation(be);
        }
    }

    @Override
    public void sendPacket(Packet<ServerPlayPacketListener> packet) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.networkHandler.sendPacket(packet);
        }
    }

    @Override
    public void sendPacket(PlayerEntity player, Packet<ServerPlayPacketListener> packet) {
        if (player instanceof ClientPlayerEntity clientPlayer) {
            clientPlayer.networkHandler.sendPacket(packet);
        }
    }

    @Override
    public void createBasinFluidParticles(World world, BasinBlockEntity blockEntity) {
        Random r = world.random;

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
        float fluidLevel = MathHelper.clamp(totalUnits / 2000, 0, 1);
        float rim = 2 / 16f;
        float space = 12 / 16f;
        BlockPos pos = blockEntity.getPos();
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
                world.addImportantParticleClient(
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

    private static void createBasinOutputFluidParticles(World world, BasinBlockEntity blockEntity, Random r) {
        BlockState blockState = blockEntity.getCachedState();
        if (!(blockState.getBlock() instanceof BasinBlock))
            return;
        Direction direction = blockState.get(BasinBlock.FACING);
        if (direction == Direction.DOWN)
            return;
        Vec3d directionVec = Vec3d.of(direction.getVector());
        Vec3d outVec = VecHelper.getCenterOf(blockEntity.getPos()).add(directionVec.multiply(.65).subtract(0, 1 / 4f, 0));
        Vec3d outMotion = directionVec.multiply(1 / 16f).add(0, -1 / 16f, 0);

        for (int i = 0; i < 2; i++) {
            blockEntity.visualizedOutputFluids.forEach(ia -> {
                FluidStack fluidStack = ia.getValue();
                ParticleEffect fluidParticle = FluidFX.getFluidParticle(fluidStack);
                Vec3d m = VecHelper.offsetRandomly(outMotion, r, 1 / 16f);
                world.addImportantParticleClient(fluidParticle, outVec.x, outVec.y, outVec.z, m.x, m.y, m.z);
            });
        }
    }

    private static void createBasinMovingFluidParticles(World world, BasinBlockEntity blockEntity, float surface, int segments) {
        Vec3d pointer = new Vec3d(1, 0, 0).multiply(1 / 16f);
        float interval = 360f / segments;
        Vec3d centerOf = VecHelper.getCenterOf(blockEntity.getPos());
        float intervalOffset = (AnimationTickHolder.getTicks() * 18) % 360;

        int currentSegment = 0;
        for (SmartFluidTankBehaviour behaviour : blockEntity.getTanks()) {
            if (behaviour == null)
                continue;
            for (SmartFluidTankBehaviour.TankSegment tankSegment : behaviour.getTanks()) {
                if (tankSegment.isEmpty(0))
                    continue;
                float angle = interval * (1 + currentSegment) + intervalOffset;
                Vec3d vec = centerOf.add(VecHelper.rotate(pointer, angle, Axis.Y));
                FluidStack stack = tankSegment.getRenderedFluid();
                world.addImportantParticleClient(
                    new FluidParticleData(AllParticleTypes.BASIN_FLUID, stack.getFluid(), stack.getComponentChanges()),
                    vec.getX(),
                    surface,
                    vec.getZ(),
                    1,
                    0,
                    0
                );
                currentSegment++;
            }
        }
    }

    @Override
    public void cartClicked(PlayerEntity player, AbstractMinecartEntity minecart) {
        CouplingHandlerClient.onCartClicked((ClientPlayerEntity) player, minecart);
    }

    @Override
    public void advertiseToAddressHelper(ClipboardBlockEntity blockEntity) {
        AddressEditBoxHelper.advertiseClipboard(blockEntity);
    }

    @Override
    public void updateClipboardScreen(UUID lastEdit, BlockPos pos, ItemStack dataContainer) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.currentScreen instanceof ClipboardScreen cs))
            return;
        if (lastEdit != null && mc.player.getUuid().equals(lastEdit))
            return;
        if (!pos.equals(cs.targetedBlock))
            return;
        cs.reopenWith(dataContainer);
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
    public void invalidateCarriage(CarriageContraptionEntity entity) {
        entity.getContraption().deferInvalidate = true;
        PortalCutoffBehaviour behaviour = entity.getBehaviour(PortalCutoffBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.updateRenderedPortalCutoff();
        }
    }

    @Override
    public void startControlling(PlayerEntity player, AbstractContraptionEntity be, BlockPos pos) {
        ControlsHandler.startControlling((ClientPlayerEntity) player, be, pos);
    }

    @Override
    public void tickBlazeBurnerMovement(MovementContext context) {
        BlazeBurnerMovementRenderBehaviour render = AllMovementBehaviours.BLAZE_BURNER.getAttachRender();
        render.tick(context);
    }

    @Override
    public void cannonDontAnimateItem(Hand hand) {
        Create.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
    }

    @Override
    public void tryToggleActive(LecternControllerBlockEntity controller) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        UUID uuid = player.getUuid();
        if (controller.user == null && uuid.equals(controller.prevUser)) {
            LinkedControllerClientHandler.deactivateInLectern(player);
        } else if (controller.prevUser == null && uuid.equals(controller.user)) {
            LinkedControllerClientHandler.activateInLectern(controller.getPos());
        }
    }

    @Override
    public void toggleLinkedControllerBindMode(BlockPos pos) {
        LinkedControllerClientHandler.toggleBindMode(MinecraftClient.getInstance().player, pos);
    }

    @Override
    public void toggleLinkedControllerActive() {
        LinkedControllerClientHandler.toggle(MinecraftClient.getInstance().player);
    }

    @Override
    public void factoryPanelMoveToSlot(SmartBlockEntity be, PanelSlot slot) {
        FactoryPanelBehaviour behaviour = (FactoryPanelBehaviour) be.getBehaviour(FilteringBehaviour.TYPE);
        if (behaviour.getSlotPositioning() instanceof FactoryPanelSlotPositioning fpsp) {
            fpsp.slot = slot;
        }
    }

    @Override
    public boolean factoryPanelClicked(World world, PlayerEntity player, ServerFactoryPanelBehaviour behaviour) {
        return FactoryPanelConnectionHandler.panelClicked(world, player, behaviour);
    }

    @Override
    public void zapperDontAnimateItem(Hand hand) {
        Create.ZAPPER_RENDER_HANDLER.dontAnimateItem(hand);
    }

    @Override
    public void openSequencedGearshiftScreen(SequencedGearshiftBlockEntity be) {
        ScreenOpener.open(new SequencedGearshiftScreen(be));
    }

    @Override
    public void openClipboardScreen(PlayerEntity player, ItemStack stack, BlockPos pos) {
        if (MinecraftClient.getInstance().player == player)
            ScreenOpener.open(new ClipboardScreen(player.getInventory().getSelectedSlot(), stack, pos));
    }

    @Override
    public void openDisplayLinkScreen(DisplayLinkBlockEntity be, PlayerEntity player) {
        if (!(player instanceof ClientPlayerEntity))
            return;
        if (be.targetOffset.equals(BlockPos.ZERO)) {
            player.sendMessage(CreateLang.translateDirect("display_link.invalid"), true);
            return;
        }
        ScreenOpener.open(new DisplayLinkScreen(be));
    }

    @Override
    public void openThresholdSwitchScreen(ThresholdSwitchBlockEntity be, PlayerEntity player) {
        if (player instanceof ClientPlayerEntity)
            ScreenOpener.open(new ThresholdSwitchScreen(be));
    }

    @Override
    public void openElevatorContactScreen(ElevatorContactBlockEntity be, PlayerEntity player) {
        if (player instanceof ClientPlayerEntity)
            ScreenOpener.open(new ElevatorContactScreen(be.getPos(), be.shortName, be.longName, be.doorControls.mode));
    }

    @Override
    public void openStationScreen(World world, BlockPos pos, PlayerEntity player) {
        if (!(player instanceof ClientPlayerEntity)) {
            return;
        }
        if (world.getBlockEntity(pos) instanceof StationBlockEntity be) {
            GlobalStation station = be.getStation();
            BlockState blockState = be.getCachedState();
            if (station == null || blockState == null)
                return;
            boolean assembling = blockState.getBlock() == AllBlocks.TRACK_STATION && blockState.get(StationBlock.ASSEMBLING);
            ScreenOpener.open(assembling ? new AssemblyScreen(be, station) : new StationScreen(be, station));
        }
    }

    @Override
    public void openFactoryPanelScreen(ServerFactoryPanelBehaviour behaviour, PlayerEntity player) {
        if (player instanceof ClientPlayerEntity)
            ScreenOpener.open(new FactoryPanelScreen(behaviour));
    }

    @Override
    public void openSymmetryWandScreen(ItemStack stack, Hand hand) {
        ScreenOpener.open(new SymmetryWandScreen(stack, hand));
    }

    @Override
    public void openSchematicEditScreen() {
        ScreenOpener.open(new SchematicEditScreen());
    }

    @Override
    public void openWorldshaperScreen(ItemStack item, Hand hand) {
        ScreenOpener.open(new WorldshaperScreen(item, hand));
    }
}
