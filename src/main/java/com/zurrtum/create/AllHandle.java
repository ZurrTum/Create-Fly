package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import com.zurrtum.create.content.contraptions.glue.SuperGlueSelectionHelper;
import com.zurrtum.create.content.contraptions.minecart.CouplingHandler;
import com.zurrtum.create.content.equipment.blueprint.BlueprintEntity;
import com.zurrtum.create.content.equipment.blueprint.BlueprintMenu;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.equipment.zapper.ZapperInteractionHandler;
import com.zurrtum.create.content.equipment.zapper.ZapperItem;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.kinetics.chainConveyor.ServerChainConveyorHandler;
import com.zurrtum.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.logistics.filter.AttributeFilterMenu;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterMenu;
import com.zurrtum.create.content.logistics.filter.PackageFilterMenu;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.packagerLink.LogisticsNetwork;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockKeeperCategoryMenu;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerServerHandler;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.schematics.SchematicInstances;
import com.zurrtum.create.content.schematics.SchematicPrinter;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonMenu;
import com.zurrtum.create.content.schematics.table.SchematicTableMenu;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TrainIconType;
import com.zurrtum.create.content.trains.entity.TrainRelocator;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackPropagator;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettingsHandleBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.zurrtum.create.foundation.gui.menu.GhostItemMenu;
import com.zurrtum.create.foundation.gui.menu.IClearableMenu;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.*;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.packet.c2s.*;
import com.zurrtum.create.infrastructure.packet.s2c.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AllHandle {
    public static void onConfigureSchematicannon(ServerGamePacketListenerImpl listener, ConfigureSchematicannonPacket packet) {
        if (!(listener.player.containerMenu instanceof SchematicannonMenu menu))
            return;

        SchematicannonBlockEntity be = menu.contentHolder;
        ConfigureSchematicannonPacket.Option option = packet.option();
        switch (option) {
            case DONT_REPLACE:
            case REPLACE_ANY:
            case REPLACE_EMPTY:
            case REPLACE_SOLID:
                be.replaceMode = option.ordinal();
                break;
            case SKIP_MISSING:
                be.skipMissing = packet.set();
                break;
            case SKIP_BLOCK_ENTITIES:
                be.replaceBlockEntities = packet.set();
                break;

            case PLAY:
                be.state = SchematicannonBlockEntity.State.RUNNING;
                be.statusMsg = "running";
                break;
            case PAUSE:
                be.state = SchematicannonBlockEntity.State.PAUSED;
                be.statusMsg = "paused";
                break;
            case STOP:
                be.state = SchematicannonBlockEntity.State.STOPPED;
                be.statusMsg = "stopped";
                break;
            default:
                break;
        }

        be.sendUpdate = true;
    }

    private static void onBlockEntityConfiguration(
        ServerGamePacketListenerImpl listener,
        BlockPos pos,
        int distance,
        Predicate<BlockEntity> predicate
    ) {
        ServerPlayer player = listener.player;
        if (player.isSpectator() || !player.mayBuild()) {
            return;
        }
        ServerLevel world = player.level();
        if (!world.isLoaded(pos)) {
            return;
        }
        if (!pos.closerThan(player.blockPosition(), distance)) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (predicate.test(blockEntity)) {
            world.getChunkSource().blockChanged(pos);
            blockEntity.setChanged();
        }
    }

    public static void onConfigureThresholdSwitch(ServerGamePacketListenerImpl listener, ConfigureThresholdSwitchPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof ThresholdSwitchBlockEntity be) {
                    be.offWhenBelow = packet.offBelow();
                    be.onWhenAbove = packet.onAbove();
                    be.setInverted(packet.invert());
                    be.inStacks = packet.inStacks();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onConfigureSequencedGearshift(ServerGamePacketListenerImpl listener, ConfigureSequencedGearshiftPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof SequencedGearshiftBlockEntity be && !AbstractComputerBehaviour.contains(be)) {
                    be.run(-1);
                    be.instructions = packet.instructions();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onEjectorTrigger(ServerGamePacketListenerImpl listener, EjectorTriggerPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof EjectorBlockEntity be) {
                    be.activate();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onStationEdit(ServerGamePacketListenerImpl listener, StationEditPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StationBlockEntity be) {
                    ServerPlayer player = listener.player;
                    Level level = be.getLevel();
                    BlockPos blockPos = be.getBlockPos();
                    BlockState blockState = level.getBlockState(blockPos);
                    GlobalStation station = be.getStation();

                    if (packet.dropSchedule()) {
                        if (station == null)
                            return true;
                        be.dropSchedule(player, station.getPresentTrain());
                        return true;
                    }

                    if (packet.doorControl() != null)
                        be.doorControls.set(packet.doorControl());

                    if (packet.name() != null && !packet.name().isBlank())
                        be.updateName(packet.name());

                    if (!(blockState.getBlock() instanceof StationBlock))
                        return true;

                    Boolean isAssemblyMode = blockState.getValue(StationBlock.ASSEMBLING);
                    boolean assemblyComplete = false;

                    if (packet.tryAssemble() != null) {
                        if (!isAssemblyMode)
                            return true;
                        if (packet.tryAssemble()) {
                            be.assemble(player.getUUID());
                            assemblyComplete = station != null && station.getPresentTrain() != null;
                        } else {
                            if (be.tryDisassembleTrain(player) && be.tryEnterAssemblyMode())
                                be.refreshAssemblyInfo();
                        }
                        if (!assemblyComplete)
                            return true;
                    }

                    if (packet.assemblyMode())
                        be.enterAssemblyMode(player);
                    else
                        be.exitAssemblyMode();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onDisplayLinkConfiguration(ServerGamePacketListenerImpl listener, DisplayLinkConfigurationPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof DisplayLinkBlockEntity be) {
                    be.targetLine = packet.targetLine();

                    CompoundTag configData = packet.configData();
                    Identifier id = configData.read("Id", Identifier.CODEC).orElse(null);
                    if (id == null) {
                        be.notifyUpdate();
                        return true;
                    }

                    DisplaySource source = DisplaySource.get(id);
                    if (source == null) {
                        be.notifyUpdate();
                        return true;
                    }

                    if (be.activeSource == null || be.activeSource != source) {
                        be.activeSource = source;
                        be.setSourceConfig(configData.copy());
                    } else {
                        be.getSourceConfig().merge(configData);
                    }

                    be.updateGatheredData();
                    be.notifyUpdate();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onCurvedTrackDestroy(ServerGamePacketListenerImpl listener, CurvedTrackDestroyPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        BlockPos pos = packet.pos();
        onBlockEntityConfiguration(
            listener, pos, AllConfigs.server().trains.maxTrackPlacementLength.get() + 16, blockEntity -> {
                if (blockEntity instanceof TrackBlockEntity be) {
                    ServerPlayer player = listener.player;
                    ServerLevel world = player.level();
                    int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 4;
                    if (!be.getBlockPos().closerThan(player.blockPosition(), verifyDistance)) {
                        Create.LOGGER.warn(player.getScoreboardName() + " too far away from destroyed Curve track");
                        return true;
                    }

                    BlockPos targetPos = packet.targetPos();
                    BezierConnection bezierConnection = be.getConnections().get(targetPos);

                    be.removeConnection(targetPos);
                    if (world.getBlockEntity(targetPos) instanceof TrackBlockEntity other)
                        other.removeConnection(pos);

                    BlockState blockState = be.getBlockState();
                    TrackPropagator.onRailRemoved(world, pos, blockState);

                    if (packet.wrench()) {
                        AllSoundEvents.WRENCH_REMOVE.playOnServer(world, packet.soundSource(), 1, world.getRandom().nextFloat() * .5f + .5f);
                        if (!player.isCreative() && bezierConnection != null)
                            bezierConnection.addItemsToPlayer(player);
                    } else if (!player.isCreative() && bezierConnection != null)
                        bezierConnection.spawnItems(world);

                    bezierConnection.spawnDestroyParticles(world);
                    SoundType soundtype = blockState.getSoundType();
                    if (soundtype == null)
                        return true;

                    world.playSound(
                        null,
                        packet.soundSource(),
                        soundtype.getBreakSound(),
                        SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F
                    );
                    return true;
                }
                return false;
            }
        );
    }

    public static void onCurvedTrackSelection(ServerGamePacketListenerImpl listener, CurvedTrackSelectionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        BlockPos pos = packet.pos();
        onBlockEntityConfiguration(
            listener, pos, AllConfigs.server().trains.maxTrackPlacementLength.get() + 16, blockEntity -> {
                if (blockEntity instanceof TrackBlockEntity be) {
                    ServerPlayer player = listener.player;
                    ServerLevel world = player.level();
                    if (player.getInventory().getSelectedSlot() != packet.slot())
                        return true;
                    ItemStack stack = player.getInventory().getItem(packet.slot());
                    if (!(stack.getItem() instanceof TrackTargetingBlockItem))
                        return true;
                    if (player.isShiftKeyDown() && stack.has(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
                        player.displayClientMessage(Component.translatable("create.track_target.clear"), true);
                        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
                        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
                        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
                        AllSoundEvents.CONTROLLER_CLICK.play(world, null, pos, 1, .5f);
                        return true;
                    }

                    EdgePointType<?> type = stack.is(AllItems.TRACK_SIGNAL) ? EdgePointType.SIGNAL : EdgePointType.STATION;
                    MutableObject<OverlapResult> result = new MutableObject<>(null);
                    BezierTrackPointLocation bezierTrackPointLocation = new BezierTrackPointLocation(packet.targetPos(), packet.segment());
                    TrackTargetingBlockItem.withGraphLocation(
                        world,
                        pos,
                        packet.front(),
                        bezierTrackPointLocation,
                        type,
                        (overlap, location) -> result.setValue(overlap)
                    );

                    if (result.getValue().feedback != null) {
                        player.displayClientMessage(
                            Component.translatable("create." + result.getValue().feedback).withStyle(ChatFormatting.RED),
                            true
                        );
                        AllSoundEvents.DENY.play(world, null, pos, .5f, 1);
                        return true;
                    }

                    stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
                    stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, packet.front());
                    stack.set(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER, bezierTrackPointLocation);

                    player.displayClientMessage(Component.translatable("create.track_target.set"), true);
                    AllSoundEvents.CONTROLLER_CLICK.play(world, null, pos, 1, 1);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onGaugeObserved(ServerGamePacketListenerImpl listener, GaugeObservedPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StressGaugeBlockEntity be) {
                    be.onObserved();
                }
                return false;
            }
        );
    }

    public static void onEjectorAward(ServerGamePacketListenerImpl listener, EjectorAwardPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof EjectorBlockEntity) {
                    AllAdvancements.EJECTOR_MAXED.trigger(listener.player);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onElevatorContactEdit(ServerGamePacketListenerImpl listener, ElevatorContactEditPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof ElevatorContactBlockEntity be) {
                    be.updateName(packet.shortName(), packet.longName());
                    be.doorControls.set(packet.doorControl());
                    return true;
                }
                return false;
            }
        );
    }

    public static void onValueSettings(ServerGamePacketListenerImpl listener, ValueSettingsPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof SmartBlockEntity be) {
                    ServerPlayer player = listener.player;
                    if (be instanceof FactoryPanelBlockEntity fpbe) {
                        for (ServerFactoryPanelBehaviour behaviour : fpbe.panels.values()) {
                            if (handleValueSettings(player, behaviour, packet)) {
                                break;
                            }
                        }
                    } else {
                        ServerScrollValueBehaviour scrollValueBehaviour = be.getBehaviour(ServerScrollValueBehaviour.TYPE);
                        if (!handleValueSettings(player, scrollValueBehaviour, packet)) {
                            ServerFilteringBehaviour filteringBehaviour = be.getBehaviour(ServerFilteringBehaviour.TYPE);
                            handleValueSettings(player, filteringBehaviour, packet);
                        }
                    }
                    return true;
                }
                return false;
            }
        );
    }

    private static boolean handleValueSettings(ServerPlayer player, ValueSettingsHandleBehaviour handle, ValueSettingsPacket packet) {
        if (handle == null || !handle.acceptsValueSettings() || packet.behaviourIndex() != handle.netId()) {
            return false;
        }
        if (packet.interactHand() != null) {
            handle.onShortInteract(player, packet.interactHand(), packet.side(), packet.hitResult());
            return true;
        }
        handle.setValueSettings(player, new ValueSettings(packet.row(), packet.value()), packet.ctrlDown());
        return true;
    }

    public static void onLogisticalStockRequest(ServerGamePacketListenerImpl listener, LogisticalStockRequestPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 4096, blockEntity -> {
                if (blockEntity instanceof StockCheckingBlockEntity be) {
                    be.getRecentSummary().divideAndSendTo(listener.player, packet.pos());
                    return true;
                }
                return false;
            }
        );
    }

    public static void onPackageOrderRequest(ServerGamePacketListenerImpl listener, PackageOrderRequestPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        BlockPos pos = packet.pos();
        onBlockEntityConfiguration(
            listener, pos, 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    PackageOrderWithCrafts order = packet.order();
                    if (packet.encodeRequester()) {
                        if (!order.isEmpty())
                            AllSoundEvents.CONFIRM.playOnServer(world, pos);
                        player.closeContainer();
                        RedstoneRequesterBlock.programRequester(player, be, order, packet.address());
                        return true;
                    }

                    if (!order.isEmpty()) {
                        AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(world, pos);
                        AllAdvancements.STOCK_TICKER.trigger(player);
                        listener.server.getPlayerList()
                            .broadcast(null, pos.getX(), pos.getY(), pos.getZ(), 32, world.dimension(), new WiFiEffectPacket(pos));
                    }

                    be.broadcastPackageRequest(RequestType.PLAYER, order, null, packet.address());
                    return true;
                }
                return false;
            }
        );
    }

    public static void onChainConveyorConnection(ServerGamePacketListenerImpl listener, ChainConveyorConnectionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        int maxRange = AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
        onBlockEntityConfiguration(
            listener, packet.pos(), maxRange, blockEntity -> {
                if (blockEntity instanceof ChainConveyorBlockEntity be) {
                    BlockPos targetPos = packet.targetPos();
                    boolean connect = packet.connect();
                    if (!be.getBlockPos().closerThan(targetPos, maxRange - 16 + 1))
                        return true;
                    if (!(world.getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity clbe))
                        return true;

                    if (connect && !player.isCreative()) {
                        int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(be.getBlockPos()));
                        boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, packet.chain(), chainCost, true);
                        if (!hasEnough)
                            return true;
                        ChainConveyorBlockEntity.getChainsFromInventory(player, packet.chain(), chainCost, false);
                    }

                    if (!connect) {
                        if (!player.isCreative()) {
                            int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(packet.pos()));
                            while (chainCost > 0) {
                                player.getInventory().placeItemBackInInventory(new ItemStack(Items.IRON_CHAIN, Math.min(chainCost, 64)));
                                chainCost -= 64;
                            }
                        }
                        be.chainDestroyed(targetPos.subtract(be.getBlockPos()), false, true);
                        world.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS);
                    }

                    if (connect) {
                        if (!clbe.addConnectionTo(be.getBlockPos()))
                            return true;
                    } else
                        clbe.removeConnectionTo(be.getBlockPos());

                    if (connect) {
                        if (!be.addConnectionTo(targetPos))
                            clbe.removeConnectionTo(be.getBlockPos());
                    } else
                        be.removeConnectionTo(targetPos);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onServerboundChainConveyorRiding(ServerGamePacketListenerImpl listener, ServerboundChainConveyorRidingPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer sender = listener.player;
        onBlockEntityConfiguration(
            listener, packet.pos(), AllConfigs.server().kinetics.maxChainConveyorLength.get() * 2, blockEntity -> {
                if (blockEntity instanceof ChainConveyorBlockEntity be) {
                    sender.fallDistance = 0;
                    sender.connection.aboveGroundTickCount = 0;
                    sender.connection.aboveGroundVehicleTickCount = 0;
                    if (packet.stop()) {
                        ServerChainConveyorHandler.handleStopRidingPacket(listener.server, sender);
                    } else {
                        ServerChainConveyorHandler.handleTTLPacket(listener.server, sender);
                    }
                    return true;
                }
                return false;
            }
        );
    }

    public static void onChainPackageInteraction(ServerGamePacketListenerImpl listener, ChainPackageInteractionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        int maxRange = AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
        onBlockEntityConfiguration(
            listener, packet.pos(), maxRange, blockEntity -> {
                if (blockEntity instanceof ChainConveyorBlockEntity be) {
                    BlockPos selectedConnection = packet.selectedConnection();
                    float chainPosition = packet.chainPosition();
                    if (packet.removingPackage()) {

                        float bestDiff = Float.POSITIVE_INFINITY;
                        ChainConveyorPackage best = null;
                        List<ChainConveyorPackage> list = selectedConnection.equals(BlockPos.ZERO) ? be.getLoopingPackages() : be.getTravellingPackages()
                            .get(selectedConnection);

                        if (list == null || list.isEmpty())
                            return true;

                        for (ChainConveyorPackage liftPackage : list) {
                            float diff = Math.abs(selectedConnection == null ? AngleHelper.getShortestAngleDiff(
                                liftPackage.chainPosition,
                                chainPosition
                            ) : liftPackage.chainPosition - chainPosition);
                            if (diff > bestDiff)
                                continue;
                            bestDiff = diff;
                            best = liftPackage;
                        }

                        if (player.getMainHandItem().isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, best.item.copy());
                        } else {
                            player.getInventory().placeItemBackInInventory(best.item.copy());
                        }

                        list.remove(best);
                        be.sendData();
                    } else {
                        ChainConveyorPackage chainConveyorPackage = new ChainConveyorPackage(chainPosition, player.getMainHandItem().copy());
                        if (!be.canAcceptPackagesFor(selectedConnection))
                            return true;

                        if (!player.isCreative()) {
                            player.getMainHandItem().shrink(1);
                            if (player.getMainHandItem().isEmpty()) {
                                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (selectedConnection.equals(BlockPos.ZERO)) {
                            be.addLoopingPackage(chainConveyorPackage);
                        } else {
                            be.addTravellingPackage(chainConveyorPackage, selectedConnection);
                        }
                    }
                    return true;
                }
                return false;
            }
        );
    }

    public static void onPackagePortConfiguration(ServerGamePacketListenerImpl listener, PackagePortConfigurationPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof PackagePortBlockEntity be) {
                    if (be.addressFilter.equals(packet.newFilter()) && be.acceptsPackages == packet.acceptPackages())
                        return true;
                    be.addressFilter = packet.newFilter();
                    be.acceptsPackages = packet.acceptPackages();
                    be.filterChanged();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onFactoryPanelConnection(ServerGamePacketListenerImpl listener, FactoryPanelConnectionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        onBlockEntityConfiguration(
            listener, packet.toPos().pos(), 40, blockEntity -> {
                if (blockEntity instanceof FactoryPanelBlockEntity be) {
                    ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(be.getLevel(), packet.toPos());
                    if (behaviour != null)
                        if (packet.relocate())
                            behaviour.moveTo(packet.fromPos(), player);
                        else
                            behaviour.addConnection(packet.fromPos());
                    return true;
                }
                return false;
            }
        );
    }

    public static void onFactoryPanelConfiguration(ServerGamePacketListenerImpl listener, FactoryPanelConfigurationPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.position().pos(), 20, blockEntity -> {
                if (blockEntity instanceof FactoryPanelBlockEntity be) {
                    ServerFactoryPanelBehaviour behaviour = be.panels.get(packet.position().slot());
                    if (behaviour == null)
                        return false;

                    boolean reset = packet.reset();
                    behaviour.recipeAddress = reset ? "" : packet.address();
                    behaviour.recipeOutput = reset ? 1 : packet.outputAmount();
                    behaviour.promiseClearingInterval = reset ? -1 : packet.promiseClearingInterval();
                    behaviour.activeCraftingArrangement = reset ? List.of() : packet.craftingArrangement();

                    if (reset) {
                        behaviour.forceClearPromises = true;
                        behaviour.disconnectAll();
                        behaviour.setFilter(ItemStack.EMPTY);
                        behaviour.count = 0;
                        be.redraw = true;
                        return true;
                    }

                    if (packet.redstoneReset()) {
                        behaviour.disconnectAllLinks();
                        return true;
                    }

                    for (Map.Entry<FactoryPanelPosition, Integer> entry : packet.inputAmounts().entrySet()) {
                        FactoryPanelPosition key = entry.getKey();
                        FactoryPanelConnection connection = behaviour.targetedBy.get(key);
                        if (connection != null)
                            connection.amount = entry.getValue();
                    }

                    FactoryPanelPosition removeConnection = packet.removeConnection();
                    if (removeConnection != null) {
                        behaviour.targetedBy.remove(removeConnection);
                        behaviour.searchForCraftingRecipe();
                        ServerFactoryPanelBehaviour source = ServerFactoryPanelBehaviour.at(be.getLevel(), removeConnection);
                        if (source != null) {
                            source.targeting.remove(behaviour.getPanelPosition());
                            source.blockEntity.sendData();
                        }
                    }

                    if (packet.clearPromises())
                        behaviour.forceClearPromises = true;

                    return true;
                }
                return false;
            }
        );
    }

    public static void onRedstoneRequesterConfiguration(ServerGamePacketListenerImpl listener, RedstoneRequesterConfigurationPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof RedstoneRequesterBlockEntity be) {
                    be.encodedTargetAdress = packet.address();
                    List<BigItemStack> stacks = be.encodedRequest.stacks();
                    List<Integer> amounts = packet.amounts();
                    for (int i = 0; i < stacks.size() && i < amounts.size(); i++) {
                        ItemStack stack = stacks.get(i).stack;
                        if (!stack.isEmpty())
                            stacks.set(i, new BigItemStack(stack, amounts.get(i)));
                    }
                    if (!be.encodedRequest.orderedStacksMatchOrderedRecipes())
                        be.encodedRequest = PackageOrderWithCrafts.simple(be.encodedRequest.stacks());
                    be.allowPartialRequests = packet.allowPartial();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onStockKeeperCategoryEdit(ServerGamePacketListenerImpl listener, StockKeeperCategoryEditPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    be.categories = packet.schedule();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onStockKeeperCategoryRefund(ServerGamePacketListenerImpl listener, StockKeeperCategoryRefundPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    ItemStack filter = packet.filter();
                    if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
                        listener.player.getInventory().placeItemBackInInventory(filter);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onStockKeeperLock(ServerGamePacketListenerImpl listener, StockKeeperLockPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    if (!be.behaviour.mayAdministrate(player))
                        return true;
                    LogisticsNetwork network = Create.LOGISTICS.logisticsNetworks.get(be.behaviour.freqId);
                    if (network != null) {
                        network.locked = packet.lock();
                        Create.LOGISTICS.markDirty();
                    }
                    return true;
                }
                return false;
            }
        );
    }

    public static void onStockKeeperCategoryHiding(ServerGamePacketListenerImpl listener, StockKeeperCategoryHidingPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    if (packet.indices().isEmpty()) {
                        be.hiddenCategoriesByPlayer.remove(listener.player.getUUID());
                        return false;
                    } else {
                        be.hiddenCategoriesByPlayer.put(listener.player.getUUID(), packet.indices());
                    }
                    return true;
                }
                return false;
            }
        );
    }

    public static void onSchematicPlace(ServerGamePacketListenerImpl listener, SchematicPlacePacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        if (!player.isCreative()) {
            return;
        }
        ServerLevel world = player.level();
        SchematicPrinter printer = new SchematicPrinter();
        printer.loadSchematic(packet.stack(), world, !player.canUseGameMasterBlocks());
        if (!printer.isLoaded() || printer.isErrored()) {
            return;
        }

        boolean includeAir = AllConfigs.server().schematics.creativePrintIncludesAir.get();

        while (printer.advanceCurrentPos()) {
            if (!printer.shouldPlaceCurrent(world)) {
                continue;
            }

            printer.handleCurrentTarget(
                (pos, state, blockEntity) -> {
                    boolean placingAir = state.isAir();
                    if (placingAir && !includeAir) {
                        return;
                    }

                    CompoundTag data = BlockHelper.prepareBlockEntityData(world, state, blockEntity);
                    BlockHelper.placeSchematicBlock(world, state, pos, null, data);
                }, (pos, entity) -> {
                    world.addFreshEntity(entity);
                }
            );
        }
    }

    public static void onSchematicUpload(ServerGamePacketListenerImpl listener, SchematicUploadPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        String schematic = packet.schematic();
        if (packet.code() == SchematicUploadPacket.BEGIN) {
            BlockPos pos = ((SchematicTableMenu) player.containerMenu).contentHolder.getBlockPos();
            Create.SCHEMATIC_RECEIVER.handleNewUpload(player, schematic, packet.size(), pos);
        } else if (packet.code() == SchematicUploadPacket.WRITE) {
            Create.SCHEMATIC_RECEIVER.handleWriteRequest(player, schematic, packet.data());
        } else {
            Create.SCHEMATIC_RECEIVER.handleFinishedUpload(player, schematic);
        }
    }

    public static void onClearContainer(ServerGamePacketListenerImpl listener) {
        if (!(listener.player.containerMenu instanceof IClearableMenu menu))
            return;
        menu.clearContents();
    }

    public static void onFilterScreen(ServerGamePacketListenerImpl listener, FilterScreenPacket packet) {
        ServerPlayer player = listener.player;
        CompoundTag tag = packet.data() == null ? new CompoundTag() : packet.data();
        FilterScreenPacket.Option option = packet.option();

        if (player.containerMenu instanceof FilterMenu c) {
            if (option == FilterScreenPacket.Option.WHITELIST)
                c.blacklist = false;
            if (option == FilterScreenPacket.Option.BLACKLIST)
                c.blacklist = true;
            if (option == FilterScreenPacket.Option.RESPECT_DATA)
                c.respectNBT = true;
            if (option == FilterScreenPacket.Option.IGNORE_DATA)
                c.respectNBT = false;
            if (option == FilterScreenPacket.Option.UPDATE_FILTER_ITEM)
                c.ghostInventory.setItem(tag.getIntOr("Slot", 0), tag.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        } else if (player.containerMenu instanceof AttributeFilterMenu c) {
            if (option == FilterScreenPacket.Option.WHITELIST)
                c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
            if (option == FilterScreenPacket.Option.WHITELIST2)
                c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
            if (option == FilterScreenPacket.Option.BLACKLIST)
                c.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
            if (option == FilterScreenPacket.Option.ADD_TAG)
                c.appendSelectedAttribute(ItemAttribute.loadStatic(packet.data(), player.registryAccess()), false);
            if (option == FilterScreenPacket.Option.ADD_INVERTED_TAG)
                c.appendSelectedAttribute(ItemAttribute.loadStatic(packet.data(), player.registryAccess()), true);
        } else if (player.containerMenu instanceof PackageFilterMenu c) {
            if (option == FilterScreenPacket.Option.UPDATE_ADDRESS)
                c.address = tag.getStringOr("Address", "");
        }
    }

    public static void onContraptionInteraction(ServerGamePacketListenerImpl listener, ContraptionInteractionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer sender = listener.player;
        Entity entityByID = sender.level().getEntity(packet.target());
        if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
            return;
        AABB bb = contraptionEntity.getBoundingBox();
        double boundsExtra = Math.max(bb.getXsize(), bb.getYsize());
        double d = sender.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 10 + boundsExtra;
        if (!sender.hasLineOfSight(entityByID))
            d -= 3;
        d *= d;
        if (sender.distanceToSqr(entityByID) > d)
            return;
        if (contraptionEntity.handlePlayerInteraction(sender, packet.localPos(), packet.face(), packet.hand()))
            sender.swing(packet.hand(), true);
    }

    public static void onClientMotion(ServerGamePacketListenerImpl listener, ClientMotionPacket packet) {
        ServerPlayer sender = listener.player;
        sender.setDeltaMovement(packet.motion());
        sender.setOnGround(packet.onGround());
        if (packet.onGround()) {
            sender.causeFallDamage(sender.fallDistance, 1, sender.damageSources().fall());
            sender.fallDistance = 0;
            sender.connection.aboveGroundTickCount = 0;
            sender.connection.aboveGroundVehicleTickCount = 0;
        }
        sender.level().getChunkSource()
            .sendToTrackingPlayers(sender, new LimbSwingUpdatePacket(sender.getId(), sender.position(), packet.limbSwing()));
    }

    public static void onArmPlacement(ServerGamePacketListenerImpl listener, ArmPlacementPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        Level world = listener.player.level();
        if (!world.isLoaded(packet.pos()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(packet.pos());
        if (!(blockEntity instanceof ArmBlockEntity arm))
            return;

        arm.interactionPointTag = packet.tag();
    }

    public static void onPackagePortPlacement(ServerGamePacketListenerImpl listener, PackagePortPlacementPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        Level world = listener.player.level();
        BlockPos pos = packet.pos();
        if (world == null || !world.isLoaded(pos))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof PackagePortBlockEntity ppbe))
            return;
        PackagePortTarget target = packet.target();
        if (!target.canSupport(ppbe))
            return;

        Vec3 targetLocation = target.getExactTargetLocation(ppbe, world, pos);
        if (targetLocation == Vec3.ZERO || !targetLocation.closerThan(
            Vec3.atBottomCenterOf(pos),
            AllConfigs.server().logistics.packagePortRange.get() + 2
        ))
            return;

        target.setup(ppbe, world, pos);
        ppbe.target = target;
        ppbe.notifyUpdate();
        ppbe.use(listener.player);
    }

    public static void onCouplingCreation(ServerGamePacketListenerImpl listener, CouplingCreationPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        CouplingHandler.tryToCoupleCarts(listener.player, listener.player.level(), packet.id1(), packet.id2());
    }

    public static void onInstantSchematic(ServerGamePacketListenerImpl listener, InstantSchematicPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        Create.SCHEMATIC_RECEIVER.handleInstantSchematic(listener.player, packet.name(), listener.player.level(), packet.origin(), packet.bounds());
    }

    public static void onSchematicSync(ServerGamePacketListenerImpl listener, SchematicSyncPacket packet) {
        ServerPlayer player = listener.player;
        ItemStack stack;
        if (packet.slot() == -1) {
            stack = player.getMainHandItem();
        } else {
            stack = player.getInventory().getItem(packet.slot());
        }
        if (!stack.is(AllItems.SCHEMATIC)) {
            return;
        }
        stack.set(AllDataComponents.SCHEMATIC_DEPLOYED, packet.deployed());
        stack.set(AllDataComponents.SCHEMATIC_ANCHOR, packet.anchor());
        stack.set(AllDataComponents.SCHEMATIC_ROTATION, packet.rotation());
        stack.set(AllDataComponents.SCHEMATIC_MIRROR, packet.mirror());
        SchematicInstances.clearHash(stack);
    }

    public static void onLeftClick(ServerGamePacketListenerImpl listener) {
        ServerPlayer player = listener.player;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ZapperItem) {
            ZapperInteractionHandler.trySelect(stack, player);
        }
    }

    public static void onEjectorPlacement(ServerGamePacketListenerImpl listener, EjectorPlacementPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerLevel world = listener.player.level();
        BlockPos pos = packet.pos();
        if (!world.isLoaded(pos))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        BlockState state = world.getBlockState(pos);
        if (blockEntity instanceof EjectorBlockEntity ejector)
            ejector.setTarget(packet.h(), packet.v());
        if (state.is(AllBlocks.WEIGHTED_EJECTOR))
            world.setBlockAndUpdate(pos, state.setValue(EjectorBlock.HORIZONTAL_FACING, packet.facing()));
    }

    public static void onEjectorElytra(ServerGamePacketListenerImpl listener, EjectorElytraPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerLevel world = listener.player.level();
        if (!world.isLoaded(packet.pos()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(packet.pos());
        if (blockEntity instanceof EjectorBlockEntity ejector)
            ejector.deployElytra(listener.player);
    }

    private static void onLinkedController(ServerPlayer player, BlockPos pos, Consumer<BlockEntity> onLectern, Consumer<ItemStack> onStack) {
        if (pos != null) {
            if (onLectern != null) {
                onLectern.accept(player.level().getBlockEntity(pos));
            }
        } else if (onStack != null) {
            ItemStack controller = player.getMainHandItem();
            if (!controller.is(AllItems.LINKED_CONTROLLER)) {
                controller = player.getOffhandItem();
                if (!controller.is(AllItems.LINKED_CONTROLLER))
                    return;
            }
            onStack.accept(controller);
        }
    }

    public static void onLinkedControllerInput(ServerGamePacketListenerImpl listener, LinkedControllerInputPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        Consumer<ItemStack> handleItem = stack -> {
            ServerLevel world = player.level();
            UUID uniqueID = player.getUUID();
            BlockPos pos = player.blockPosition();

            if (player.isSpectator() && packet.press())
                return;

            LinkedControllerServerHandler.receivePressed(
                world,
                pos,
                uniqueID,
                packet.activatedButtons().stream().map(i -> LinkedControllerItem.toFrequency(stack, i)).collect(Collectors.toList()),
                packet.press()
            );
        };
        onLinkedController(
            player, packet.lecternPos(), blockEntity -> {
                if (blockEntity instanceof LecternControllerBlockEntity lectern) {
                    if (lectern.isUsedBy(player))
                        handleItem.accept(lectern.getController());
                }
            }, handleItem
        );
    }

    public static void onLinkedControllerBind(ServerGamePacketListenerImpl listener, LinkedControllerBindPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        if (player.isSpectator()) {
            return;
        }
        onLinkedController(
            player, null, null, stack -> {
                ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(stack);
                ServerLinkBehaviour linkBehaviour = BlockEntityBehaviour.get(player.level(), packet.linkLocation(), ServerLinkBehaviour.TYPE);
                if (linkBehaviour == null)
                    return;

                int button = packet.button();
                linkBehaviour.getNetworkKey()
                    .forEachWithContext((f, first) -> frequencyItems.setItem(button * 2 + (first ? 0 : 1), f.getStack().copy()));

                stack.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemHelper.containerContentsFromHandler(frequencyItems));
            }
        );
    }

    public static void onLinkedControllerStopLectern(ServerGamePacketListenerImpl listener, LinkedControllerStopLecternPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        onLinkedController(
            player, packet.lecternPos(), blockEntity -> {
                if (blockEntity instanceof LecternControllerBlockEntity lectern) {
                    lectern.tryStopUsing(player);
                }
            }, null
        );
    }

    public static void onGhostItemSubmit(ServerGamePacketListenerImpl listener, GhostItemSubmitPacket packet) {
        AbstractContainerMenu containerMenu = listener.player.containerMenu;
        int slot = packet.slot();
        ItemStack item = packet.item();
        if (containerMenu instanceof GhostItemMenu<?> menu) {
            menu.ghostInventory.setItem(slot, item);
            menu.getSlot(36 + slot).setChanged();
        } else if (containerMenu instanceof StockKeeperCategoryMenu menu && (item.isEmpty() || item.getItem() instanceof FilterItem)) {
            menu.proxyInventory.setItem(slot, item);
            menu.getSlot(36 + slot).setChanged();
        }
    }

    public static void onBlueprintAssignCompleteRecipe(ServerGamePacketListenerImpl listener, BlueprintAssignCompleteRecipePacket packet) {
        ServerPlayer player = listener.player;
        if (player.containerMenu instanceof BlueprintMenu menu) {
            Container inventory = menu.ghostInventory;
            List<ItemStack> input = packet.input();
            int size = Math.min(input.size(), 9);
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, input.get(i));
            }
            inventory.setItem(9, packet.output());
        }
    }

    public static void onConfigureSymmetryWand(ServerGamePacketListenerImpl listener, ConfigureSymmetryWandPacket packet) {
        ItemStack stack = listener.player.getItemInHand(packet.hand());
        if (stack.getItem() instanceof SymmetryWandItem) {
            SymmetryWandItem.configureSettings(stack, packet.mirror());
        }
    }

    public static void onConfigureWorldshaper(ServerGamePacketListenerImpl listener, ConfigureWorldshaperPacket packet) {
        ItemStack stack = listener.player.getItemInHand(packet.hand());
        if (stack.getItem() instanceof ZapperItem) {
            packet.configureZapper(stack);
        }
    }

    public static void onToolboxEquip(ServerGamePacketListenerImpl listener, ToolboxEquipPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        BlockPos toolboxPos = packet.toolboxPos();
        int slot = packet.slot();
        int hotbarSlot = packet.hotbarSlot();
        if (toolboxPos == null) {
            ToolboxHandler.unequip(player, hotbarSlot, false);
            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
            return;
        }
        ServerLevel world = player.level();
        BlockEntity blockEntity = world.getBlockEntity(toolboxPos);
        double maxRange = ToolboxHandler.getMaxRange(player);
        if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange * maxRange)
            return;
        if (!(blockEntity instanceof ToolboxBlockEntity toolboxBlockEntity))
            return;

        ToolboxHandler.unequip(player, hotbarSlot, false);

        if (slot < 0 || slot >= 8) {
            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
            return;
        }

        Inventory playerInventory = player.getInventory();
        ItemStack playerStack = playerInventory.getItem(hotbarSlot);
        if (!playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack, toolboxBlockEntity.inventory.filters.get(slot))) {
            toolboxBlockEntity.inventory.inLimitedMode(inventory -> {
                int count = playerStack.getCount();
                int insert = inventory.insertExist(playerStack);
                if (insert != count) {
                    count -= insert;
                    insert = playerInventory.insert(playerStack, count, Inventory.getSelectionSize(), Inventory.INVENTORY_SIZE);
                }
                if (insert == count) {
                    playerInventory.setItem(hotbarSlot, ItemStack.EMPTY);
                } else {
                    playerStack.setCount(count - insert);
                }
            });
        }

        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        String key = String.valueOf(hotbarSlot);

        CompoundTag data = new CompoundTag();
        data.putInt("Slot", slot);
        data.store("Pos", BlockPos.CODEC, toolboxPos);
        compound.put(key, data);

        toolboxBlockEntity.connectPlayer(slot, player, hotbarSlot);
        ToolboxHandler.syncData(player, compound);
    }

    public static void onToolboxDisposeAll(ServerGamePacketListenerImpl listener, ToolboxDisposeAllPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        BlockPos toolboxPos = packet.toolboxPos();
        BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

        double maxRange = ToolboxHandler.getMaxRange(player);
        if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange * maxRange)
            return;
        if (!(blockEntity instanceof ToolboxBlockEntity toolbox))
            return;

        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        MutableBoolean sendData = new MutableBoolean(false);

        Inventory playerInventory = player.getInventory();
        toolbox.inventory.inLimitedMode(inventory -> {
            for (int i = 0; i < 36; i++) {
                if (compound.getCompound(String.valueOf(i)).flatMap(nbt -> nbt.read("Pos", BlockPos.CODEC)).map(pos -> pos.equals(toolboxPos))
                    .orElse(false)) {
                    ToolboxHandler.unequip(player, i, true);
                    sendData.setTrue();
                }

                ItemStack itemStack = playerInventory.getItem(i);
                int count = itemStack.getCount();
                if (count == 0) {
                    continue;
                }
                int insert = toolbox.inventory.insertExist(itemStack, count);
                if (insert == count) {
                    playerInventory.setItem(i, ItemStack.EMPTY);
                } else {
                    itemStack.setCount(count - insert);
                }
            }
        });

        if (sendData.booleanValue())
            ToolboxHandler.syncData(player, compound);
    }

    public static void onScheduleEdit(ServerGamePacketListenerImpl listener, ScheduleEditPacket packet) {
        ServerPlayer sender = listener.player;
        ItemStack mainHandItem = sender.getMainHandItem();
        if (!mainHandItem.is(AllItems.SCHEDULE))
            return;

        if (packet.schedule().entries.isEmpty()) {
            mainHandItem.remove(AllDataComponents.TRAIN_SCHEDULE);
        } else {
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "ScheduleEdit", Create.LOGGER)) {
                TagValueOutput view = TagValueOutput.createWithContext(logging, sender.registryAccess());
                packet.schedule().write(view);
                mainHandItem.set(AllDataComponents.TRAIN_SCHEDULE, view.buildResult());
            }
        }

        sender.getCooldowns().addCooldown(mainHandItem, 5);
    }

    public static void onTrainEdit(ServerGamePacketListenerImpl listener, TrainEditPacket packet) {
        ServerPlayer sender = listener.player;
        ServerLevel world = sender.level();
        Train train = Create.RAILWAYS.sided(world).trains.get(packet.id());
        if (train == null)
            return;
        if (!packet.name().isBlank()) {
            train.name = Component.literal(packet.name());
        }
        train.icon = TrainIconType.byId(packet.iconType());
        train.mapColorIndex = packet.mapColor();
        listener.server.getPlayerList().broadcastAll(new TrainEditReturnPacket(packet.id(), packet.name(), packet.iconType(), packet.mapColor()));
    }

    public static void onTrainRelocation(ServerGamePacketListenerImpl listener, TrainRelocationPacket packet) {
        ServerPlayer sender = listener.player;
        Train train = Create.RAILWAYS.trains.get(packet.trainId());
        Entity entity = sender.level().getEntity(packet.entityId());

        String messagePrefix = sender.getName().getString() + " could not relocate Train ";

        if (train == null || !(entity instanceof CarriageContraptionEntity cce)) {
            Create.LOGGER.warn(messagePrefix + train.id.toString().substring(0, 5) + ": not present on server");
            return;
        }

        if (!train.id.equals(cce.trainId))
            return;

        int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 2;
        if (!sender.position().closerThan(Vec3.atCenterOf(packet.pos()), verifyDistance)) {
            Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from clicked pos");
            return;
        }
        if (!sender.position().closerThan(cce.position(), verifyDistance + cce.getBoundingBox().getXsize() / 2)) {
            Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from carriage entity");
            return;
        }

        if (TrainRelocator.relocate(train, sender.level(), packet.pos(), packet.hoveredBezier(), packet.direction(), packet.lookAngle(), null)) {
            sender.displayClientMessage(Component.translatable("create.train.relocate.success").withStyle(ChatFormatting.GREEN), true);
            train.carriages.forEach(c -> c.forEachPresentEntity(e -> {
                e.nonDamageTicks = 10;
                listener.player.level().getChunkSource().sendToTrackingPlayers(e, new ContraptionRelocationPacket(e.getId()));
            }));
            return;
        }

        Create.LOGGER.warn(messagePrefix + train.name.getString() + ": relocation failed server-side");
    }

    public static void onControlsInput(ServerGamePacketListenerImpl listener, ControlsInputPacket packet) {
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        UUID uniqueID = player.getUUID();

        if (player.isSpectator() && packet.press())
            return;

        Entity entity = world.getEntity(packet.contraptionEntityId());
        if (!(entity instanceof AbstractContraptionEntity ace))
            return;
        if (packet.stopControlling()) {
            ace.stopControlling(packet.controlsPos());
            return;
        }

        if (ace.toGlobalVector(Vec3.atCenterOf(packet.controlsPos()), 0).closerThan(player.position(), 16))
            ControlsServerHandler.receivePressed(world, ace, packet.controlsPos(), uniqueID, packet.activatedButtons(), packet.press());
    }

    public static void onPlaceExtendedCurve(ServerGamePacketListenerImpl listener, PlaceExtendedCurvePacket packet) {
        ItemStack stack = listener.player.getItemInHand(packet.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!stack.is(AllItemTags.TRACKS))
            return;
        stack.set(AllDataComponents.TRACK_EXTENDED_CURVE, true);
    }

    public static void onSuperGlueSelection(ServerGamePacketListenerImpl listener, SuperGlueSelectionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 2;
        BlockPos to = packet.to();
        if (player.distanceToSqr(Vec3.atCenterOf(to)) > range * range)
            return;
        BlockPos from = packet.from();
        if (!to.closerThan(from, 25))
            return;

        Set<BlockPos> group = SuperGlueSelectionHelper.searchGlueGroup(world, from, to, false);
        if (group == null)
            return;
        if (!group.contains(to))
            return;
        if (!SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, true))
            return;

        AABB bb = SuperGlueEntity.span(from, to);
        SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, false);
        SuperGlueEntity entity = new SuperGlueEntity(world, bb);
        world.addFreshEntity(entity);
        entity.spawnParticles();

        AllAdvancements.SUPER_GLUE.trigger(player);
    }

    public static void onSuperGlueRemoval(ServerGamePacketListenerImpl listener, SuperGlueRemovalPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        Entity entity = world.getEntity(packet.entityId());
        if (!(entity instanceof SuperGlueEntity superGlue))
            return;
        double range = 32;
        if (player.distanceToSqr(superGlue.position()) > range * range)
            return;
        AllSoundEvents.SLIME_ADDED.play(world, null, packet.soundSource(), 0.5F, 0.5F);
        superGlue.spawnParticles();
        entity.discard();
    }

    public static void onTrainCollision(ServerGamePacketListenerImpl listener, TrainCollisionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer player = listener.player;
        ServerLevel world = player.level();
        Entity entity = world.getEntity(packet.contraptionEntityId());
        if (!(entity instanceof CarriageContraptionEntity cce))
            return;

        player.hurtServer(world, AllDamageSources.get(world).runOver(cce), packet.damage());
        world.playSound(player, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1, .75f);
    }

    public static void onTrainHUDUpdate(ServerGamePacketListenerImpl listener, TrainHUDUpdatePacket packet) {
        ServerPlayer player = listener.player;
        Train train = Create.RAILWAYS.sided(player.level()).trains.get(packet.trainId());
        if (train == null)
            return;

        if (packet.throttle() != null)
            train.throttle = packet.throttle();
    }

    public static void onTrainHonk(ServerGamePacketListenerImpl listener, HonkPacket packet) {
        ServerPlayer player = listener.player;
        Train train = Create.RAILWAYS.sided(player.level()).trains.get(packet.trainId());
        if (train == null)
            return;

        AllAdvancements.TRAIN_WHISTLE.trigger(player);
        listener.server.getPlayerList().broadcastAll(new HonkReturnPacket(train, packet.isHonk()));
    }

    public static void onTrackGraphRequest(ServerGamePacketListenerImpl listener, TrackGraphRequestPacket packet) {
        ServerPlayer player = listener.player;
        int netId = packet.netId();
        for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) {
            if (trackGraph.netId == netId) {
                Create.RAILWAYS.sync.sendFullGraphTo(trackGraph, player);
                break;
            }
        }
    }

    public static void onElevatorRequestFloorList(ServerGamePacketListenerImpl listener, RequestFloorListPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        Entity entityByID = listener.player.level().getEntity(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;
        if (!(ace.getContraption() instanceof ElevatorContraption ec))
            return;
        listener.send(new ElevatorFloorListPacket(ace, ec.namesList));
    }

    public static void onElevatorTargetFloor(ServerGamePacketListenerImpl listener, ElevatorTargetFloorPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerPlayer sender = listener.player;
        ServerLevel world = sender.level();
        Entity entityByID = world.getEntity(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;
        if (!(ace.getContraption() instanceof ElevatorContraption ec))
            return;
        if (ace.distanceToSqr(sender) > 50 * 50)
            return;

        ElevatorColumn elevatorColumn = ElevatorColumn.get(world, ec.getGlobalColumn());
        if (elevatorColumn == null) {
            return;
        }
        int targetY = packet.targetY();
        if (!elevatorColumn.contacts.contains(targetY))
            return;
        if (ec.isTargetUnreachable(targetY))
            return;

        BlockPos pos = elevatorColumn.contactAt(targetY);
        BlockState blockState = world.getBlockState(pos);
        if (!(blockState.getBlock() instanceof ElevatorContactBlock ecb))
            return;

        ecb.callToContactAndUpdate(elevatorColumn, blockState, world, pos, false);
    }

    public static void onClipboardEdit(ServerGamePacketListenerImpl listener, ClipboardEditPacket packet) {
        ServerPlayer sender = listener.player;
        ClipboardContent processedContent = clipboardProcessor(packet.clipboardContent());

        BlockPos targetedBlock = packet.targetedBlock();
        if (targetedBlock != null) {
            ServerLevel world = sender.level();
            if (!world.isLoaded(targetedBlock))
                return;
            if (!targetedBlock.closerThan(sender.blockPosition(), 20))
                return;
            if (world.getBlockEntity(targetedBlock) instanceof ClipboardBlockEntity cbe) {
                PatchedDataComponentMap map = new PatchedDataComponentMap(cbe.components());
                if (processedContent == null) {
                    map.remove(AllDataComponents.CLIPBOARD_CONTENT);
                } else {
                    map.set(AllDataComponents.CLIPBOARD_CONTENT, processedContent);
                }
                cbe.setComponents(map);
                cbe.onEditedBy(sender);
            }
            return;
        }

        ItemStack itemStack = sender.getInventory().getItem(packet.hotbarSlot());
        if (!itemStack.is(AllItems.CLIPBOARD))
            return;
        if (processedContent == null) {
            itemStack.remove(AllDataComponents.CLIPBOARD_CONTENT);
        } else {
            itemStack.set(AllDataComponents.CLIPBOARD_CONTENT, processedContent);
        }
    }

    private static ClipboardContent clipboardProcessor(@Nullable ClipboardContent content) {
        if (content == null)
            return null;

        for (List<ClipboardEntry> page : content.pages()) {
            for (ClipboardEntry entry : page) {
                if (NBTProcessors.textComponentHasClickEvent(entry.text))
                    return null;
            }
        }

        return content;
    }

    public static void onContraptionColliderLockRequest(ServerGamePacketListenerImpl listener, ContraptionColliderLockPacketRequest packet) {
        ServerPlayer player = listener.player;
        player.level().getChunkSource()
            .sendToTrackingPlayers(player, new ContraptionColliderLockPacket(packet.contraption(), packet.offset(), player.getId()));
    }

    public static void onRadialWrenchMenuSubmit(ServerGamePacketListenerImpl listener, RadialWrenchMenuSubmitPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        ServerLevel world = listener.player.level();
        BlockPos blockPos = packet.blockPos();
        BlockState newState = packet.newState();
        if (!world.getBlockState(blockPos).is(newState.getBlock()))
            return;

        BlockState updatedState = Block.updateFromNeighbourShapes(newState, world, blockPos);
        KineticBlockEntity.switchToBlockState(world, blockPos, updatedState);

        IWrenchable.playRotateSound(world, blockPos);
    }

    public static void onTrainMapSyncRequest(ServerGamePacketListenerImpl listener) {
    }

    public static void onLinkSettings(ServerGamePacketListenerImpl listener, LinkSettingsPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof SmartBlockEntity be) {
                    ServerLinkBehaviour behaviour = be.getBehaviour(ServerLinkBehaviour.TYPE);
                    if (behaviour != null) {
                        behaviour.setFrequency(packet.first(), listener.player.getItemInHand(packet.hand()));
                        return true;
                    }
                }
                return false;
            }
        );
    }

    public static void onBlueprintPreviewRequest(ServerGamePacketListenerImpl listener, BlueprintPreviewRequestPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, listener, listener.server.packetProcessor());
        Entity entity = listener.player.level().getEntity(packet.entityId());
        if (!(entity instanceof BlueprintEntity blueprint)) {
            listener.send(BlueprintPreviewPacket.EMPTY);
            return;
        }
        listener.send(BlueprintEntity.getPreview(blueprint, packet.index(), listener.player, packet.sneaking()));
    }
}
