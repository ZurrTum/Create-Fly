package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
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
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
    public static void onConfigureSchematicannon(ServerPlayNetworkHandler listener, ConfigureSchematicannonPacket packet) {
        if (!(listener.player.currentScreenHandler instanceof SchematicannonMenu menu))
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

    private static void onBlockEntityConfiguration(ServerPlayNetworkHandler listener, BlockPos pos, int distance, Predicate<BlockEntity> predicate) {
        ServerPlayerEntity player = listener.player;
        if (player.isSpectator() || !player.canModifyBlocks()) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        if (!world.isPosLoaded(pos)) {
            return;
        }
        if (!pos.isWithinDistance(player.getBlockPos(), distance)) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (predicate.test(blockEntity)) {
            world.getChunkManager().markForUpdate(pos);
            blockEntity.markDirty();
        }
    }

    public static void onConfigureThresholdSwitch(ServerPlayNetworkHandler listener, ConfigureThresholdSwitchPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onConfigureSequencedGearshift(ServerPlayNetworkHandler listener, ConfigureSequencedGearshiftPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof SequencedGearshiftBlockEntity be && !be.computerBehaviour.hasAttachedComputer()) {
                    be.run(-1);
                    be.instructions = packet.instructions();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onEjectorTrigger(ServerPlayNetworkHandler listener, EjectorTriggerPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onStationEdit(ServerPlayNetworkHandler listener, StationEditPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StationBlockEntity be) {
                    ServerPlayerEntity player = listener.player;
                    World level = be.getWorld();
                    BlockPos blockPos = be.getPos();
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

                    Boolean isAssemblyMode = blockState.get(StationBlock.ASSEMBLING);
                    boolean assemblyComplete = false;

                    if (packet.tryAssemble() != null) {
                        if (!isAssemblyMode)
                            return true;
                        if (packet.tryAssemble()) {
                            be.assemble(player.getUuid());
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

    public static void onDisplayLinkConfiguration(ServerPlayNetworkHandler listener, DisplayLinkConfigurationPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof DisplayLinkBlockEntity be) {
                    be.targetLine = packet.targetLine();

                    NbtCompound configData = packet.configData();
                    Identifier id = configData.get("Id", Identifier.CODEC).orElse(null);
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
                        be.getSourceConfig().copyFrom(configData);
                    }

                    be.updateGatheredData();
                    be.notifyUpdate();
                    return true;
                }
                return false;
            }
        );
    }

    public static void onCurvedTrackDestroy(ServerPlayNetworkHandler listener, CurvedTrackDestroyPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        BlockPos pos = packet.pos();
        onBlockEntityConfiguration(
            listener, pos, AllConfigs.server().trains.maxTrackPlacementLength.get() + 16, blockEntity -> {
                if (blockEntity instanceof TrackBlockEntity be) {
                    ServerPlayerEntity player = listener.player;
                    ServerWorld world = player.getEntityWorld();
                    int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 4;
                    if (!be.getPos().isWithinDistance(player.getBlockPos(), verifyDistance)) {
                        Create.LOGGER.warn(player.getNameForScoreboard() + " too far away from destroyed Curve track");
                        return true;
                    }

                    BlockPos targetPos = packet.targetPos();
                    BezierConnection bezierConnection = be.getConnections().get(targetPos);

                    be.removeConnection(targetPos);
                    if (world.getBlockEntity(targetPos) instanceof TrackBlockEntity other)
                        other.removeConnection(pos);

                    BlockState blockState = be.getCachedState();
                    TrackPropagator.onRailRemoved(world, pos, blockState);

                    if (packet.wrench()) {
                        AllSoundEvents.WRENCH_REMOVE.playOnServer(world, packet.soundSource(), 1, world.random.nextFloat() * .5f + .5f);
                        if (!player.isCreative() && bezierConnection != null)
                            bezierConnection.addItemsToPlayer(player);
                    } else if (!player.isCreative() && bezierConnection != null)
                        bezierConnection.spawnItems(world);

                    bezierConnection.spawnDestroyParticles(world);
                    BlockSoundGroup soundtype = blockState.getSoundGroup();
                    if (soundtype == null)
                        return true;

                    world.playSound(
                        null,
                        packet.soundSource(),
                        soundtype.getBreakSound(),
                        SoundCategory.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F
                    );
                    return true;
                }
                return false;
            }
        );
    }

    public static void onCurvedTrackSelection(ServerPlayNetworkHandler listener, CurvedTrackSelectionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        BlockPos pos = packet.pos();
        onBlockEntityConfiguration(
            listener, pos, AllConfigs.server().trains.maxTrackPlacementLength.get() + 16, blockEntity -> {
                if (blockEntity instanceof TrackBlockEntity be) {
                    ServerPlayerEntity player = listener.player;
                    ServerWorld world = player.getEntityWorld();
                    if (player.getInventory().getSelectedSlot() != packet.slot())
                        return true;
                    ItemStack stack = player.getInventory().getStack(packet.slot());
                    if (!(stack.getItem() instanceof TrackTargetingBlockItem))
                        return true;
                    if (player.isSneaking() && stack.contains(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS)) {
                        player.sendMessage(Text.translatable("create.track_target.clear"), true);
                        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
                        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
                        stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);
                        AllSoundEvents.CONTROLLER_CLICK.play(world, null, pos, 1, .5f);
                        return true;
                    }

                    EdgePointType<?> type = stack.isOf(AllItems.TRACK_SIGNAL) ? EdgePointType.SIGNAL : EdgePointType.STATION;
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
                        player.sendMessage(Text.translatable("create." + result.getValue().feedback).formatted(Formatting.RED), true);
                        AllSoundEvents.DENY.play(world, null, pos, .5f, 1);
                        return true;
                    }

                    stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
                    stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, packet.front());
                    stack.set(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER, bezierTrackPointLocation);

                    player.sendMessage(Text.translatable("create.track_target.set"), true);
                    AllSoundEvents.CONTROLLER_CLICK.play(world, null, pos, 1, 1);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onGaugeObserved(ServerPlayNetworkHandler listener, GaugeObservedPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StressGaugeBlockEntity be) {
                    be.onObserved();
                }
                return false;
            }
        );
    }

    public static void onEjectorAward(ServerPlayNetworkHandler listener, EjectorAwardPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onElevatorContactEdit(ServerPlayNetworkHandler listener, ElevatorContactEditPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onValueSettings(ServerPlayNetworkHandler listener, ValueSettingsPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof SmartBlockEntity be) {
                    ServerPlayerEntity player = listener.player;
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

    private static boolean handleValueSettings(ServerPlayerEntity player, ValueSettingsHandleBehaviour handle, ValueSettingsPacket packet) {
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

    public static void onLogisticalStockRequest(ServerPlayNetworkHandler listener, LogisticalStockRequestPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onPackageOrderRequest(ServerPlayNetworkHandler listener, PackageOrderRequestPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        BlockPos pos = packet.pos();
        onBlockEntityConfiguration(
            listener, pos, 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    PackageOrderWithCrafts order = packet.order();
                    if (packet.encodeRequester()) {
                        if (!order.isEmpty())
                            AllSoundEvents.CONFIRM.playOnServer(world, pos);
                        player.closeHandledScreen();
                        RedstoneRequesterBlock.programRequester(player, be, order, packet.address());
                        return true;
                    }

                    if (!order.isEmpty()) {
                        AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(world, pos);
                        AllAdvancements.STOCK_TICKER.trigger(player);
                        listener.server.getPlayerManager()
                            .sendToAround(null, pos.getX(), pos.getY(), pos.getZ(), 32, world.getRegistryKey(), new WiFiEffectPacket(pos));
                    }

                    be.broadcastPackageRequest(RequestType.PLAYER, order, null, packet.address());
                    return true;
                }
                return false;
            }
        );
    }

    public static void onChainConveyorConnection(ServerPlayNetworkHandler listener, ChainConveyorConnectionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        int maxRange = AllConfigs.server().kinetics.maxChainConveyorLength.get() + 16;
        onBlockEntityConfiguration(
            listener, packet.pos(), maxRange, blockEntity -> {
                if (blockEntity instanceof ChainConveyorBlockEntity be) {
                    BlockPos targetPos = packet.targetPos();
                    boolean connect = packet.connect();
                    if (!be.getPos().isWithinDistance(targetPos, maxRange - 16 + 1))
                        return true;
                    if (!(world.getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity clbe))
                        return true;

                    if (connect && !player.isCreative()) {
                        int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(be.getPos()));
                        boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, packet.chain(), chainCost, true);
                        if (!hasEnough)
                            return true;
                        ChainConveyorBlockEntity.getChainsFromInventory(player, packet.chain(), chainCost, false);
                    }

                    if (!connect) {
                        if (!player.isCreative()) {
                            int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos.subtract(packet.pos()));
                            while (chainCost > 0) {
                                player.getInventory().offerOrDrop(new ItemStack(Items.IRON_CHAIN, Math.min(chainCost, 64)));
                                chainCost -= 64;
                            }
                        }
                        be.chainDestroyed(targetPos.subtract(be.getPos()), false, true);
                        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS);
                    }

                    if (connect) {
                        if (!clbe.addConnectionTo(be.getPos()))
                            return true;
                    } else
                        clbe.removeConnectionTo(be.getPos());

                    if (connect) {
                        if (!be.addConnectionTo(targetPos))
                            clbe.removeConnectionTo(be.getPos());
                    } else
                        be.removeConnectionTo(targetPos);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onServerboundChainConveyorRiding(ServerPlayNetworkHandler listener, ServerboundChainConveyorRidingPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity sender = listener.player;
        onBlockEntityConfiguration(
            listener, packet.pos(), AllConfigs.server().kinetics.maxChainConveyorLength.get() * 2, blockEntity -> {
                if (blockEntity instanceof ChainConveyorBlockEntity be) {
                    sender.fallDistance = 0;
                    sender.networkHandler.floatingTicks = 0;
                    sender.networkHandler.vehicleFloatingTicks = 0;
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

    public static void onChainPackageInteraction(ServerPlayNetworkHandler listener, ChainPackageInteractionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
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

                        if (player.getMainHandStack().isEmpty()) {
                            player.setStackInHand(Hand.MAIN_HAND, best.item.copy());
                        } else {
                            player.getInventory().offerOrDrop(best.item.copy());
                        }

                        list.remove(best);
                        be.sendData();
                    } else {
                        ChainConveyorPackage chainConveyorPackage = new ChainConveyorPackage(chainPosition, player.getMainHandStack().copy());
                        if (!be.canAcceptPackagesFor(selectedConnection))
                            return true;

                        if (!player.isCreative()) {
                            player.getMainHandStack().decrement(1);
                            if (player.getMainHandStack().isEmpty()) {
                                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
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

    public static void onPackagePortConfiguration(ServerPlayNetworkHandler listener, PackagePortConfigurationPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onFactoryPanelConnection(ServerPlayNetworkHandler listener, FactoryPanelConnectionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        onBlockEntityConfiguration(
            listener, packet.toPos().pos(), 40, blockEntity -> {
                if (blockEntity instanceof FactoryPanelBlockEntity be) {
                    ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(be.getWorld(), packet.toPos());
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

    public static void onFactoryPanelConfiguration(ServerPlayNetworkHandler listener, FactoryPanelConfigurationPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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
                        ServerFactoryPanelBehaviour source = ServerFactoryPanelBehaviour.at(be.getWorld(), removeConnection);
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

    public static void onRedstoneRequesterConfiguration(ServerPlayNetworkHandler listener, RedstoneRequesterConfigurationPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onStockKeeperCategoryEdit(ServerPlayNetworkHandler listener, StockKeeperCategoryEditPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
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

    public static void onStockKeeperCategoryRefund(ServerPlayNetworkHandler listener, StockKeeperCategoryRefundPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    ItemStack filter = packet.filter();
                    if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
                        listener.player.getInventory().offerOrDrop(filter);
                    return true;
                }
                return false;
            }
        );
    }

    public static void onStockKeeperLock(ServerPlayNetworkHandler listener, StockKeeperLockPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
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

    public static void onStockKeeperCategoryHiding(ServerPlayNetworkHandler listener, StockKeeperCategoryHidingPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof StockTickerBlockEntity be) {
                    if (packet.indices().isEmpty()) {
                        be.hiddenCategoriesByPlayer.remove(listener.player.getUuid());
                        return false;
                    } else {
                        be.hiddenCategoriesByPlayer.put(listener.player.getUuid(), packet.indices());
                    }
                    return true;
                }
                return false;
            }
        );
    }

    public static void onSchematicPlace(ServerPlayNetworkHandler listener, SchematicPlacePacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        if (!player.isCreative()) {
            return;
        }
        ServerWorld world = player.getEntityWorld();
        SchematicPrinter printer = new SchematicPrinter();
        printer.loadSchematic(packet.stack(), world, !player.isCreativeLevelTwoOp());
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

                    NbtCompound data = BlockHelper.prepareBlockEntityData(world, state, blockEntity);
                    BlockHelper.placeSchematicBlock(world, state, pos, null, data);
                }, (pos, entity) -> {
                    world.spawnEntity(entity);
                }
            );
        }
    }

    public static void onSchematicUpload(ServerPlayNetworkHandler listener, SchematicUploadPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        String schematic = packet.schematic();
        if (packet.code() == SchematicUploadPacket.BEGIN) {
            BlockPos pos = ((SchematicTableMenu) player.currentScreenHandler).contentHolder.getPos();
            Create.SCHEMATIC_RECEIVER.handleNewUpload(player, schematic, packet.size(), pos);
        } else if (packet.code() == SchematicUploadPacket.WRITE) {
            Create.SCHEMATIC_RECEIVER.handleWriteRequest(player, schematic, packet.data());
        } else {
            Create.SCHEMATIC_RECEIVER.handleFinishedUpload(player, schematic);
        }
    }

    public static void onClearContainer(ServerPlayNetworkHandler listener) {
        if (!(listener.player.currentScreenHandler instanceof IClearableMenu menu))
            return;
        menu.clearContents();
    }

    public static void onFilterScreen(ServerPlayNetworkHandler listener, FilterScreenPacket packet) {
        ServerPlayerEntity player = listener.player;
        NbtCompound tag = packet.data() == null ? new NbtCompound() : packet.data();
        FilterScreenPacket.Option option = packet.option();

        if (player.currentScreenHandler instanceof FilterMenu c) {
            if (option == FilterScreenPacket.Option.WHITELIST)
                c.blacklist = false;
            if (option == FilterScreenPacket.Option.BLACKLIST)
                c.blacklist = true;
            if (option == FilterScreenPacket.Option.RESPECT_DATA)
                c.respectNBT = true;
            if (option == FilterScreenPacket.Option.IGNORE_DATA)
                c.respectNBT = false;
            if (option == FilterScreenPacket.Option.UPDATE_FILTER_ITEM)
                c.ghostInventory.setStack(tag.getInt("Slot", 0), tag.get("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        } else if (player.currentScreenHandler instanceof AttributeFilterMenu c) {
            if (option == FilterScreenPacket.Option.WHITELIST)
                c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
            if (option == FilterScreenPacket.Option.WHITELIST2)
                c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
            if (option == FilterScreenPacket.Option.BLACKLIST)
                c.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
            if (option == FilterScreenPacket.Option.ADD_TAG)
                c.appendSelectedAttribute(ItemAttribute.loadStatic(packet.data(), player.getRegistryManager()), false);
            if (option == FilterScreenPacket.Option.ADD_INVERTED_TAG)
                c.appendSelectedAttribute(ItemAttribute.loadStatic(packet.data(), player.getRegistryManager()), true);
        } else if (player.currentScreenHandler instanceof PackageFilterMenu c) {
            if (option == FilterScreenPacket.Option.UPDATE_ADDRESS)
                c.address = tag.getString("Address", "");
        }
    }

    public static void onContraptionInteraction(ServerPlayNetworkHandler listener, ContraptionInteractionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity sender = listener.player;
        Entity entityByID = sender.getEntityWorld().getEntityById(packet.target());
        if (!(entityByID instanceof AbstractContraptionEntity contraptionEntity))
            return;
        Box bb = contraptionEntity.getBoundingBox();
        double boundsExtra = Math.max(bb.getLengthX(), bb.getLengthY());
        double d = sender.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 10 + boundsExtra;
        if (!sender.canSee(entityByID))
            d -= 3;
        d *= d;
        if (sender.squaredDistanceTo(entityByID) > d)
            return;
        if (contraptionEntity.handlePlayerInteraction(sender, packet.localPos(), packet.face(), packet.hand()))
            sender.swingHand(packet.hand(), true);
    }

    public static void onClientMotion(ServerPlayNetworkHandler listener, ClientMotionPacket packet) {
        ServerPlayerEntity sender = listener.player;
        sender.setVelocity(packet.motion());
        sender.setOnGround(packet.onGround());
        if (packet.onGround()) {
            sender.handleFallDamage(sender.fallDistance, 1, sender.getDamageSources().fall());
            sender.fallDistance = 0;
            sender.networkHandler.floatingTicks = 0;
            sender.networkHandler.vehicleFloatingTicks = 0;
        }
        sender.getEntityWorld().getChunkManager()
            .sendToOtherNearbyPlayers(sender, new LimbSwingUpdatePacket(sender.getId(), sender.getEntityPos(), packet.limbSwing()));
    }

    public static void onArmPlacement(ServerPlayNetworkHandler listener, ArmPlacementPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        World world = listener.player.getEntityWorld();
        if (!world.isPosLoaded(packet.pos()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(packet.pos());
        if (!(blockEntity instanceof ArmBlockEntity arm))
            return;

        arm.interactionPointTag = packet.tag();
    }

    public static void onPackagePortPlacement(ServerPlayNetworkHandler listener, PackagePortPlacementPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        World world = listener.player.getEntityWorld();
        BlockPos pos = packet.pos();
        if (world == null || !world.isPosLoaded(pos))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof PackagePortBlockEntity ppbe))
            return;
        PackagePortTarget target = packet.target();
        if (!target.canSupport(ppbe))
            return;

        Vec3d targetLocation = target.getExactTargetLocation(ppbe, world, pos);
        if (targetLocation == Vec3d.ZERO || !targetLocation.isInRange(
            Vec3d.ofBottomCenter(pos),
            AllConfigs.server().logistics.packagePortRange.get() + 2
        ))
            return;

        target.setup(ppbe, world, pos);
        ppbe.target = target;
        ppbe.notifyUpdate();
        ppbe.use(listener.player);
    }

    public static void onCouplingCreation(ServerPlayNetworkHandler listener, CouplingCreationPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        CouplingHandler.tryToCoupleCarts(listener.player, listener.player.getEntityWorld(), packet.id1(), packet.id2());
    }

    public static void onInstantSchematic(ServerPlayNetworkHandler listener, InstantSchematicPacket packet) {
        Create.SCHEMATIC_RECEIVER.handleInstantSchematic(
            listener.player,
            packet.name(),
            listener.player.getEntityWorld(),
            packet.origin(),
            packet.bounds()
        );
    }

    public static void onSchematicSync(ServerPlayNetworkHandler listener, SchematicSyncPacket packet) {
        ServerPlayerEntity player = listener.player;
        ItemStack stack;
        if (packet.slot() == -1) {
            stack = player.getMainHandStack();
        } else {
            stack = player.getInventory().getStack(packet.slot());
        }
        if (!stack.isOf(AllItems.SCHEMATIC)) {
            return;
        }
        stack.set(AllDataComponents.SCHEMATIC_DEPLOYED, packet.deployed());
        stack.set(AllDataComponents.SCHEMATIC_ANCHOR, packet.anchor());
        stack.set(AllDataComponents.SCHEMATIC_ROTATION, packet.rotation());
        stack.set(AllDataComponents.SCHEMATIC_MIRROR, packet.mirror());
        SchematicInstances.clearHash(stack);
    }

    public static void onLeftClick(ServerPlayNetworkHandler listener) {
        ServerPlayerEntity player = listener.player;
        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() instanceof ZapperItem) {
            ZapperInteractionHandler.trySelect(stack, player);
        }
    }

    public static void onEjectorPlacement(ServerPlayNetworkHandler listener, EjectorPlacementPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerWorld world = listener.player.getEntityWorld();
        BlockPos pos = packet.pos();
        if (!world.isPosLoaded(pos))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        BlockState state = world.getBlockState(pos);
        if (blockEntity instanceof EjectorBlockEntity ejector)
            ejector.setTarget(packet.h(), packet.v());
        if (state.isOf(AllBlocks.WEIGHTED_EJECTOR))
            world.setBlockState(pos, state.with(EjectorBlock.HORIZONTAL_FACING, packet.facing()));
    }

    public static void onEjectorElytra(ServerPlayNetworkHandler listener, EjectorElytraPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerWorld world = listener.player.getEntityWorld();
        if (!world.isPosLoaded(packet.pos()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(packet.pos());
        if (blockEntity instanceof EjectorBlockEntity ejector)
            ejector.deployElytra(listener.player);
    }

    private static void onLinkedController(ServerPlayerEntity player, BlockPos pos, Consumer<BlockEntity> onLectern, Consumer<ItemStack> onStack) {
        if (pos != null) {
            if (onLectern != null) {
                onLectern.accept(player.getEntityWorld().getBlockEntity(pos));
            }
        } else if (onStack != null) {
            ItemStack controller = player.getMainHandStack();
            if (!controller.isOf(AllItems.LINKED_CONTROLLER)) {
                controller = player.getOffHandStack();
                if (!controller.isOf(AllItems.LINKED_CONTROLLER))
                    return;
            }
            onStack.accept(controller);
        }
    }

    public static void onLinkedControllerInput(ServerPlayNetworkHandler listener, LinkedControllerInputPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        Consumer<ItemStack> handleItem = stack -> {
            ServerWorld world = player.getEntityWorld();
            UUID uniqueID = player.getUuid();
            BlockPos pos = player.getBlockPos();

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

    public static void onLinkedControllerBind(ServerPlayNetworkHandler listener, LinkedControllerBindPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        if (player.isSpectator()) {
            return;
        }
        onLinkedController(
            player, null, null, stack -> {
                ItemStackHandler frequencyItems = LinkedControllerItem.getFrequencyItems(stack);
                ServerLinkBehaviour linkBehaviour = BlockEntityBehaviour.get(
                    player.getEntityWorld(),
                    packet.linkLocation(),
                    ServerLinkBehaviour.TYPE
                );
                if (linkBehaviour == null)
                    return;

                int button = packet.button();
                linkBehaviour.getNetworkKey()
                    .forEachWithContext((f, first) -> frequencyItems.setStack(button * 2 + (first ? 0 : 1), f.getStack().copy()));

                stack.set(AllDataComponents.LINKED_CONTROLLER_ITEMS, ItemHelper.containerContentsFromHandler(frequencyItems));
            }
        );
    }

    public static void onLinkedControllerStopLectern(ServerPlayNetworkHandler listener, LinkedControllerStopLecternPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        onLinkedController(
            player, packet.lecternPos(), blockEntity -> {
                if (blockEntity instanceof LecternControllerBlockEntity lectern) {
                    lectern.tryStopUsing(player);
                }
            }, null
        );
    }

    public static void onGhostItemSubmit(ServerPlayNetworkHandler listener, GhostItemSubmitPacket packet) {
        ScreenHandler containerMenu = listener.player.currentScreenHandler;
        int slot = packet.slot();
        ItemStack item = packet.item();
        if (containerMenu instanceof GhostItemMenu<?> menu) {
            menu.ghostInventory.setStack(slot, item);
            menu.getSlot(36 + slot).markDirty();
        } else if (containerMenu instanceof StockKeeperCategoryMenu menu && (item.isEmpty() || item.getItem() instanceof FilterItem)) {
            menu.proxyInventory.setStack(slot, item);
            menu.getSlot(36 + slot).markDirty();
        }
    }

    public static void onBlueprintAssignCompleteRecipe(ServerPlayNetworkHandler listener, BlueprintAssignCompleteRecipePacket packet) {
        ServerPlayerEntity player = listener.player;
        if (player.currentScreenHandler instanceof BlueprintMenu c) {
            ServerRecipeManager.ServerRecipe serverRecipe = listener.server.getRecipeManager().get(packet.recipeId());
            if (serverRecipe != null) {
                //TODO
                //                BlueprintItem.assignCompleteRecipe(player.getWorld(), c.ghostInventory, serverRecipe.parent().value());
            }
        }
    }

    public static void onConfigureSymmetryWand(ServerPlayNetworkHandler listener, ConfigureSymmetryWandPacket packet) {
        ItemStack stack = listener.player.getStackInHand(packet.hand());
        if (stack.getItem() instanceof SymmetryWandItem) {
            SymmetryWandItem.configureSettings(stack, packet.mirror());
        }
    }

    public static void onConfigureWorldshaper(ServerPlayNetworkHandler listener, ConfigureWorldshaperPacket packet) {
        ItemStack stack = listener.player.getStackInHand(packet.hand());
        if (stack.getItem() instanceof ZapperItem) {
            packet.configureZapper(stack);
        }
    }

    public static void onToolboxEquip(ServerPlayNetworkHandler listener, ToolboxEquipPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        BlockPos toolboxPos = packet.toolboxPos();
        int slot = packet.slot();
        int hotbarSlot = packet.hotbarSlot();
        if (toolboxPos == null) {
            ToolboxHandler.unequip(player, hotbarSlot, false);
            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
            return;
        }
        ServerWorld world = player.getEntityWorld();
        BlockEntity blockEntity = world.getBlockEntity(toolboxPos);
        double maxRange = ToolboxHandler.getMaxRange(player);
        if (player.squaredDistanceTo(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange * maxRange)
            return;
        if (!(blockEntity instanceof ToolboxBlockEntity toolboxBlockEntity))
            return;

        ToolboxHandler.unequip(player, hotbarSlot, false);

        if (slot < 0 || slot >= 8) {
            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
            return;
        }

        PlayerInventory playerInventory = player.getInventory();
        ItemStack playerStack = playerInventory.getStack(hotbarSlot);
        if (!playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack, toolboxBlockEntity.inventory.filters.get(slot))) {
            toolboxBlockEntity.inventory.inLimitedMode(inventory -> {
                int count = playerStack.getCount();
                int insert = inventory.insertExist(playerStack);
                if (insert != count) {
                    count -= insert;
                    insert = playerInventory.insert(playerStack, count, PlayerInventory.getHotbarSize(), PlayerInventory.MAIN_SIZE);
                }
                if (insert == count) {
                    playerInventory.setStack(hotbarSlot, ItemStack.EMPTY);
                } else {
                    playerStack.setCount(count - insert);
                }
            });
        }

        NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);
        String key = String.valueOf(hotbarSlot);

        NbtCompound data = new NbtCompound();
        data.putInt("Slot", slot);
        data.put("Pos", BlockPos.CODEC, toolboxPos);
        compound.put(key, data);

        toolboxBlockEntity.connectPlayer(slot, player, hotbarSlot);
        ToolboxHandler.syncData(player, compound);
    }

    public static void onToolboxDisposeAll(ServerPlayNetworkHandler listener, ToolboxDisposeAllPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        BlockPos toolboxPos = packet.toolboxPos();
        BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

        double maxRange = ToolboxHandler.getMaxRange(player);
        if (player.squaredDistanceTo(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange * maxRange)
            return;
        if (!(blockEntity instanceof ToolboxBlockEntity toolbox))
            return;

        NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);
        MutableBoolean sendData = new MutableBoolean(false);

        PlayerInventory playerInventory = player.getInventory();
        toolbox.inventory.inLimitedMode(inventory -> {
            for (int i = 0; i < 36; i++) {
                if (compound.getCompound(String.valueOf(i)).flatMap(nbt -> nbt.get("Pos", BlockPos.CODEC)).map(pos -> pos.equals(toolboxPos))
                    .orElse(false)) {
                    ToolboxHandler.unequip(player, i, true);
                    sendData.setTrue();
                }

                ItemStack itemStack = playerInventory.getStack(i);
                int count = itemStack.getCount();
                if (count == 0) {
                    continue;
                }
                int insert = toolbox.inventory.insertExist(itemStack, count);
                if (insert == count) {
                    playerInventory.setStack(i, ItemStack.EMPTY);
                } else {
                    itemStack.setCount(count - insert);
                }
            }
        });

        if (sendData.booleanValue())
            ToolboxHandler.syncData(player, compound);
    }

    public static void onScheduleEdit(ServerPlayNetworkHandler listener, ScheduleEditPacket packet) {
        ServerPlayerEntity sender = listener.player;
        ItemStack mainHandItem = sender.getMainHandStack();
        if (!mainHandItem.isOf(AllItems.SCHEDULE))
            return;

        if (packet.schedule().entries.isEmpty()) {
            mainHandItem.remove(AllDataComponents.TRAIN_SCHEDULE);
        } else {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleEdit", Create.LOGGER)) {
                NbtWriteView view = NbtWriteView.create(logging, sender.getRegistryManager());
                packet.schedule().write(view);
                mainHandItem.set(AllDataComponents.TRAIN_SCHEDULE, view.getNbt());
            }
        }

        sender.getItemCooldownManager().set(mainHandItem, 5);
    }

    public static void onTrainEdit(ServerPlayNetworkHandler listener, TrainEditPacket packet) {
        ServerPlayerEntity sender = listener.player;
        ServerWorld world = sender.getEntityWorld();
        Train train = Create.RAILWAYS.sided(world).trains.get(packet.id());
        if (train == null)
            return;
        if (!packet.name().isBlank()) {
            train.name = Text.literal(packet.name());
        }
        train.icon = TrainIconType.byId(packet.iconType());
        train.mapColorIndex = packet.mapColor();
        listener.server.getPlayerManager().sendToAll(new TrainEditReturnPacket(packet.id(), packet.name(), packet.iconType(), packet.mapColor()));
    }

    public static void onTrainRelocation(ServerPlayNetworkHandler listener, TrainRelocationPacket packet) {
        ServerPlayerEntity sender = listener.player;
        Train train = Create.RAILWAYS.trains.get(packet.trainId());
        Entity entity = sender.getEntityWorld().getEntityById(packet.entityId());

        String messagePrefix = sender.getName().getString() + " could not relocate Train ";

        if (train == null || !(entity instanceof CarriageContraptionEntity cce)) {
            Create.LOGGER.warn(messagePrefix + train.id.toString().substring(0, 5) + ": not present on server");
            return;
        }

        if (!train.id.equals(cce.trainId))
            return;

        int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 2;
        if (!sender.getEntityPos().isInRange(Vec3d.ofCenter(packet.pos()), verifyDistance)) {
            Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from clicked pos");
            return;
        }
        if (!sender.getEntityPos().isInRange(cce.getEntityPos(), verifyDistance + cce.getBoundingBox().getLengthX() / 2)) {
            Create.LOGGER.warn(messagePrefix + train.name.getString() + ": player too far from carriage entity");
            return;
        }

        if (TrainRelocator.relocate(
            train,
            sender.getEntityWorld(),
            packet.pos(),
            packet.hoveredBezier(),
            packet.direction(),
            packet.lookAngle(),
            null
        )) {
            sender.sendMessage(Text.translatable("create.train.relocate.success").formatted(Formatting.GREEN), true);
            train.carriages.forEach(c -> c.forEachPresentEntity(e -> {
                e.nonDamageTicks = 10;
                listener.player.getEntityWorld().getChunkManager().sendToOtherNearbyPlayers(e, new ContraptionRelocationPacket(e.getId()));
            }));
            return;
        }

        Create.LOGGER.warn(messagePrefix + train.name.getString() + ": relocation failed server-side");
    }

    public static void onControlsInput(ServerPlayNetworkHandler listener, ControlsInputPacket packet) {
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        UUID uniqueID = player.getUuid();

        if (player.isSpectator() && packet.press())
            return;

        Entity entity = world.getEntityById(packet.contraptionEntityId());
        if (!(entity instanceof AbstractContraptionEntity ace))
            return;
        if (packet.stopControlling()) {
            ace.stopControlling(packet.controlsPos());
            return;
        }

        if (ace.toGlobalVector(Vec3d.ofCenter(packet.controlsPos()), 0).isInRange(player.getEntityPos(), 16))
            ControlsServerHandler.receivePressed(world, ace, packet.controlsPos(), uniqueID, packet.activatedButtons(), packet.press());
    }

    public static void onPlaceExtendedCurve(ServerPlayNetworkHandler listener, PlaceExtendedCurvePacket packet) {
        ItemStack stack = listener.player.getStackInHand(packet.mainHand() ? Hand.MAIN_HAND : Hand.OFF_HAND);
        if (!stack.isIn(AllItemTags.TRACKS))
            return;
        stack.set(AllDataComponents.TRACK_EXTENDED_CURVE, true);
    }

    public static void onSuperGlueSelection(ServerPlayNetworkHandler listener, SuperGlueSelectionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        double range = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE) + 2;
        BlockPos to = packet.to();
        if (player.squaredDistanceTo(Vec3d.ofCenter(to)) > range * range)
            return;
        BlockPos from = packet.from();
        if (!to.isWithinDistance(from, 25))
            return;

        Set<BlockPos> group = SuperGlueSelectionHelper.searchGlueGroup(world, from, to, false);
        if (group == null)
            return;
        if (!group.contains(to))
            return;
        if (!SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, true))
            return;

        Box bb = SuperGlueEntity.span(from, to);
        SuperGlueSelectionHelper.collectGlueFromInventory(player, 1, false);
        SuperGlueEntity entity = new SuperGlueEntity(world, bb);
        world.spawnEntity(entity);
        entity.spawnParticles();

        AllAdvancements.SUPER_GLUE.trigger(player);
    }

    public static void onSuperGlueRemoval(ServerPlayNetworkHandler listener, SuperGlueRemovalPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        Entity entity = world.getEntityById(packet.entityId());
        if (!(entity instanceof SuperGlueEntity superGlue))
            return;
        double range = 32;
        if (player.squaredDistanceTo(superGlue.getEntityPos()) > range * range)
            return;
        AllSoundEvents.SLIME_ADDED.play(world, null, packet.soundSource(), 0.5F, 0.5F);
        superGlue.spawnParticles();
        entity.discard();
    }

    public static void onTrainCollision(ServerPlayNetworkHandler listener, TrainCollisionPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity player = listener.player;
        ServerWorld world = player.getEntityWorld();
        Entity entity = world.getEntityById(packet.contraptionEntityId());
        if (!(entity instanceof CarriageContraptionEntity cce))
            return;

        player.damage(world, AllDamageSources.get(world).runOver(cce), packet.damage());
        world.playSound(player, entity.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.NEUTRAL, 1, .75f);
    }

    public static void onTrainHUDUpdate(ServerPlayNetworkHandler listener, TrainHUDUpdatePacket packet) {
        ServerPlayerEntity player = listener.player;
        Train train = Create.RAILWAYS.sided(player.getEntityWorld()).trains.get(packet.trainId());
        if (train == null)
            return;

        if (packet.throttle() != null)
            train.throttle = packet.throttle();
    }

    public static void onTrainHonk(ServerPlayNetworkHandler listener, HonkPacket packet) {
        ServerPlayerEntity player = listener.player;
        Train train = Create.RAILWAYS.sided(player.getEntityWorld()).trains.get(packet.trainId());
        if (train == null)
            return;

        AllAdvancements.TRAIN_WHISTLE.trigger(player);
        listener.server.getPlayerManager().sendToAll(new HonkReturnPacket(train, packet.isHonk()));
    }

    public static void onTrackGraphRequest(ServerPlayNetworkHandler listener, TrackGraphRequestPacket packet) {
        ServerPlayerEntity player = listener.player;
        int netId = packet.netId();
        for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) {
            if (trackGraph.netId == netId) {
                Create.RAILWAYS.sync.sendFullGraphTo(trackGraph, player);
                break;
            }
        }
    }

    public static void onElevatorRequestFloorList(ServerPlayNetworkHandler listener, RequestFloorListPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        Entity entityByID = listener.player.getEntityWorld().getEntityById(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;
        if (!(ace.getContraption() instanceof ElevatorContraption ec))
            return;
        listener.sendPacket(new ElevatorFloorListPacket(ace, ec.namesList));
    }

    public static void onElevatorTargetFloor(ServerPlayNetworkHandler listener, ElevatorTargetFloorPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerPlayerEntity sender = listener.player;
        ServerWorld world = sender.getEntityWorld();
        Entity entityByID = world.getEntityById(packet.entityId());
        if (!(entityByID instanceof AbstractContraptionEntity ace))
            return;
        if (!(ace.getContraption() instanceof ElevatorContraption ec))
            return;
        if (ace.squaredDistanceTo(sender) > 50 * 50)
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

    public static void onClipboardEdit(ServerPlayNetworkHandler listener, ClipboardEditPacket packet) {
        ServerPlayerEntity sender = listener.player;
        ClipboardContent processedContent = clipboardProcessor(packet.clipboardContent());

        BlockPos targetedBlock = packet.targetedBlock();
        if (targetedBlock != null) {
            ServerWorld world = sender.getEntityWorld();
            if (!world.isPosLoaded(targetedBlock))
                return;
            if (!targetedBlock.isWithinDistance(sender.getBlockPos(), 20))
                return;
            if (world.getBlockEntity(targetedBlock) instanceof ClipboardBlockEntity cbe) {
                MergedComponentMap map = new MergedComponentMap(cbe.getComponents());
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

        ItemStack itemStack = sender.getInventory().getStack(packet.hotbarSlot());
        if (!itemStack.isOf(AllItems.CLIPBOARD))
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

    public static void onContraptionColliderLockRequest(ServerPlayNetworkHandler listener, ContraptionColliderLockPacketRequest packet) {
        ServerPlayerEntity player = listener.player;
        player.getEntityWorld().getChunkManager()
            .sendToOtherNearbyPlayers(player, new ContraptionColliderLockPacket(packet.contraption(), packet.offset(), player.getId()));
    }

    public static void onRadialWrenchMenuSubmit(ServerPlayNetworkHandler listener, RadialWrenchMenuSubmitPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        ServerWorld world = listener.player.getEntityWorld();
        BlockPos blockPos = packet.blockPos();
        BlockState newState = packet.newState();
        if (!world.getBlockState(blockPos).isOf(newState.getBlock()))
            return;

        BlockState updatedState = Block.postProcessState(newState, world, blockPos);
        KineticBlockEntity.switchToBlockState(world, blockPos, updatedState);

        IWrenchable.playRotateSound(world, blockPos);
    }

    public static void onTrainMapSyncRequest(ServerPlayNetworkHandler listener) {
    }

    public static void onLinkSettings(ServerPlayNetworkHandler listener, LinkSettingsPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        onBlockEntityConfiguration(
            listener, packet.pos(), 20, blockEntity -> {
                if (blockEntity instanceof SmartBlockEntity be) {
                    ServerLinkBehaviour behaviour = be.getBehaviour(ServerLinkBehaviour.TYPE);
                    if (behaviour != null) {
                        behaviour.setFrequency(packet.first(), listener.player.getStackInHand(packet.hand()));
                        return true;
                    }
                }
                return false;
            }
        );
    }

    public static void onBlueprintPreviewRequest(ServerPlayNetworkHandler listener, BlueprintPreviewRequestPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, listener, listener.server.getPacketApplyBatcher());
        Entity entity = listener.player.getEntityWorld().getEntityById(packet.entityId());
        if (!(entity instanceof BlueprintEntity blueprint)) {
            listener.sendPacket(BlueprintPreviewPacket.EMPTY);
            return;
        }
        listener.sendPacket(BlueprintEntity.getPreview(blueprint, packet.index(), listener.player, packet.sneaking()));
    }
}
