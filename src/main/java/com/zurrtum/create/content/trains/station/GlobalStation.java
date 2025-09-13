package com.zurrtum.create.content.trains.station;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.signal.SingleBlockEntityEdgePoint;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class GlobalStation extends SingleBlockEntityEdgePoint {
    public static final Codec<Map<BlockPos, GlobalPackagePort>> PORTS_CODEC = CreateCodecs.getCodecMap(BlockPos.CODEC, GlobalPackagePort.CODEC);

    public String name;
    public WeakReference<Train> nearestTrain;
    public boolean assembling;

    public Map<BlockPos, GlobalPackagePort> connectedPorts;

    public GlobalStation() {
        name = "Track Station";
        nearestTrain = new WeakReference<>(null);
        connectedPorts = new HashMap<>();
    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
        super.blockEntityAdded(blockEntity, front);
        BlockState state = blockEntity.getCachedState();
        assembling = state != null && state.contains(StationBlock.ASSEMBLING) && state.get(StationBlock.ASSEMBLING);
    }

    @Override
    public void read(ReadView view, boolean migration, DimensionPalette dimensions) {
        super.read(view, migration, dimensions);
        name = view.getString("Name", "");
        assembling = view.getBoolean("Assembling", false);
        nearestTrain = new WeakReference<>(null);
        view.read("Ports", PORTS_CODEC).ifPresentOrElse(ports -> connectedPorts = ports, connectedPorts::clear);
    }

    @Override
    public <T> void decode(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
        super.decode(ops, input, migration, dimensions);
        MapLike<T> map = ops.getMap(input).getOrThrow();
        name = ops.getStringValue(map.get("Name")).result().orElse("");
        assembling = ops.getBooleanValue(map.get("Assembling")).getOrThrow();
    }

    @Override
    public void read(PacketByteBuf buffer, DimensionPalette dimensions) {
        super.read(buffer, dimensions);
        name = buffer.readString();
        assembling = buffer.readBoolean();
        if (buffer.readBoolean())
            blockEntityPos = buffer.readBlockPos();
    }

    @Override
    public void write(WriteView view, DimensionPalette dimensions) {
        super.write(view, dimensions);
        view.putString("Name", name);
        view.putBoolean("Assembling", assembling);
        view.put("Ports", PORTS_CODEC, connectedPorts);
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, T empty, DimensionPalette dimensions) {
        DataResult<T> prefix = super.encode(ops, empty, dimensions);
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Name", ops.createString(name));
        map.add("Assembling", ops.createBoolean(assembling));
        map.add("Ports", connectedPorts, PORTS_CODEC);
        return map.build(prefix);
    }

    @Override
    public void write(PacketByteBuf buffer, DimensionPalette dimensions) {
        super.write(buffer, dimensions);
        buffer.writeString(name);
        buffer.writeBoolean(assembling);
        buffer.writeBoolean(blockEntityPos != null);
        if (blockEntityPos != null)
            buffer.writeBlockPos(blockEntityPos);
    }

    public boolean canApproachFrom(TrackNode side) {
        return isPrimary(side) && !assembling;
    }

    @Override
    public boolean canNavigateVia(TrackNode side) {
        return super.canNavigateVia(side) && !assembling;
    }

    public void reserveFor(Train train) {
        Train nearestTrain = getNearestTrain();
        if (nearestTrain == null || nearestTrain.navigation.distanceToDestination > train.navigation.distanceToDestination)
            this.nearestTrain = new WeakReference<>(train);
    }

    public void cancelReservation(Train train) {
        if (nearestTrain.get() == train)
            nearestTrain = new WeakReference<>(null);
    }

    public void trainDeparted(Train train) {
        cancelReservation(train);
    }

    @Nullable
    public Train getPresentTrain() {
        Train nearestTrain = getNearestTrain();
        if (nearestTrain == null || nearestTrain.getCurrentStation() != this)
            return null;
        return nearestTrain;
    }

    @Nullable
    public Train getImminentTrain() {
        Train nearestTrain = getNearestTrain();
        if (nearestTrain == null)
            return nearestTrain;
        if (nearestTrain.getCurrentStation() == this)
            return nearestTrain;
        if (!nearestTrain.navigation.isActive())
            return null;
        if (nearestTrain.navigation.distanceToDestination > 30)
            return null;
        return nearestTrain;
    }

    @Nullable
    public Train getNearestTrain() {
        return this.nearestTrain.get();
    }

    // Package Port integration
    public static class GlobalPackagePort {
        public static final Codec<GlobalPackagePort> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("address").forGetter(i -> i.address),
            ItemStackHandler.CODEC.fieldOf("offlineBuffer").forGetter(i -> i.offlineBuffer),
            Codec.BOOL.fieldOf("primed").forGetter(i -> i.primed)
        ).apply(instance, GlobalPackagePort::new));

        public String address;
        public ItemStackHandler offlineBuffer;
        public boolean primed;

        public GlobalPackagePort() {
            this("", new ItemStackHandler(18), false);
        }

        private GlobalPackagePort(String address, ItemStackHandler offlineBuffer, boolean primed) {
            this.address = address;
            this.offlineBuffer = offlineBuffer;
            this.primed = primed;
        }
    }

    public void runMailTransfer() {
        Train train = getPresentTrain();
        if (train == null || connectedPorts.isEmpty())
            return;
        World level = null;

        for (Carriage carriage : train.carriages) {
            if (level == null) {
                CarriageContraptionEntity entity = carriage.anyAvailableEntity();
                if (entity != null && entity.getWorld() instanceof ServerWorld sl)
                    level = sl.getServer().getWorld(getBlockEntityDimension());
            }

            Inventory carriageInventory = carriage.storage.getAllItems();
            if (carriageInventory == null)
                continue;

            // Import from station
            for (Map.Entry<BlockPos, GlobalPackagePort> entry : connectedPorts.entrySet()) {
                GlobalPackagePort port = entry.getValue();
                BlockPos pos = entry.getKey();
                PostboxBlockEntity box = null;

                Inventory postboxInventory = port.offlineBuffer;
                if (level != null && level.isPosLoaded(pos) && level.getBlockEntity(pos) instanceof PostboxBlockEntity ppbe) {
                    postboxInventory = ppbe.inventory;
                    box = ppbe;
                }

                for (int slot = 0, size = postboxInventory.size(); slot < size; slot++) {
                    ItemStack stack = postboxInventory.getStack(slot);
                    if (PackageItem.matchAddress(stack, port.address))
                        continue;

                    int insert = carriageInventory.insert(stack);
                    if (insert == 0)
                        continue;

                    int count = stack.getCount();
                    if (insert == count) {
                        postboxInventory.setStack(slot, ItemStack.EMPTY);
                    } else {
                        stack.setCount(count - insert);
                    }
                    Create.RAILWAYS.markTracksDirty();
                    if (box != null)
                        box.spawnParticles();
                }
            }

            // Export to station
            for (ItemStack stack : carriageInventory) {
                if (!PackageItem.isPackage(stack))
                    continue;

                for (Map.Entry<BlockPos, GlobalPackagePort> entry : connectedPorts.entrySet()) {
                    GlobalPackagePort port = entry.getValue();
                    BlockPos pos = entry.getKey();
                    PostboxBlockEntity box = null;

                    if (!PackageItem.matchAddress(stack, port.address))
                        continue;

                    Inventory postboxInventory = port.offlineBuffer;
                    if (level != null && level.isPosLoaded(pos) && level.getBlockEntity(pos) instanceof PostboxBlockEntity ppbe) {
                        postboxInventory = ppbe.inventory;
                        box = ppbe;
                        box.inventory.sendMode();
                    }
                    int insert = postboxInventory.insert(stack);
                    if (box != null) {
                        box.inventory.receiveMode();
                    }
                    if (insert == 0) {
                        continue;
                    }

                    int extract = carriageInventory.extract(stack, insert);
                    if (extract != insert) {
                        postboxInventory.extract(stack, insert - extract);
                    }
                    Create.RAILWAYS.markTracksDirty();
                    if (box != null)
                        box.spawnParticles();

                    break;
                }
            }

        }
    }

}
