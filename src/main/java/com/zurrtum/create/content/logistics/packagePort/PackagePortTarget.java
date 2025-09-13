package com.zurrtum.create.content.logistics.packagePort;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectedPort;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public abstract class PackagePortTarget {
    public static final Codec<PackagePortTarget> CODEC = CreateRegistries.PACKAGE_PORT_TARGET_TYPE.getCodec()
        .dispatch(PackagePortTarget::getType, PackagePortTargetType::codec);
    public static final PacketCodec<? super RegistryByteBuf, PackagePortTarget> PACKET_CODEC = PacketCodecs.registryValue(CreateRegistryKeys.PACKAGE_PORT_TARGET_TYPE)
        .dispatch(PackagePortTarget::getType, PackagePortTargetType::packetCodec);

    public BlockPos relativePos;

    public PackagePortTarget(BlockPos relativePos) {
        this.relativePos = relativePos;
    }

    public abstract boolean export(WorldAccess level, BlockPos portPos, ItemStack box, boolean simulate);

    public void setup(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
    }

    public void register(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
    }

    public void deregister(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
    }

    public abstract Vec3d getExactTargetLocation(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos);

    public abstract ItemStack getIcon();

    public abstract boolean canSupport(BlockEntity be);

    public boolean depositImmediately() {
        return false;
    }

    protected abstract PackagePortTargetType getType();

    public BlockEntity be(WorldAccess level, BlockPos portPos) {
        if (level instanceof World l && !l.isPosLoaded(portPos.add(relativePos)))
            return null;
        return level.getBlockEntity(portPos.add(relativePos));
    }

    public static class ChainConveyorFrogportTarget extends PackagePortTarget {
        public static final MapCodec<ChainConveyorFrogportTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("relative_pos").forGetter(i -> i.relativePos),
            Codec.FLOAT.fieldOf("chain_pos").forGetter(i -> i.chainPos),
            BlockPos.CODEC.optionalFieldOf("connection").forGetter(i -> Optional.ofNullable(i.connection)),
            Codec.BOOL.fieldOf("flipped").forGetter(i -> i.flipped)
        ).apply(instance, ChainConveyorFrogportTarget::new));

        public static final PacketCodec<ByteBuf, ChainConveyorFrogportTarget> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            i -> i.relativePos,
            PacketCodecs.FLOAT,
            i -> i.chainPos,
            CatnipStreamCodecBuilders.nullable(BlockPos.PACKET_CODEC),
            i -> i.connection,
            PacketCodecs.BOOLEAN,
            i -> i.flipped,
            ChainConveyorFrogportTarget::new
        );

        public float chainPos;
        @Nullable
        public BlockPos connection;
        public boolean flipped;

        public ChainConveyorFrogportTarget(BlockPos relativePos, float chainPos, Optional<BlockPos> connection, boolean flipped) {
            this(relativePos, chainPos, connection.orElse(null), flipped);
        }

        public ChainConveyorFrogportTarget(BlockPos relativePos, float chainPos, @Nullable BlockPos connection, boolean flipped) {
            super(relativePos);
            this.chainPos = chainPos;
            this.connection = connection;
            this.flipped = flipped;
        }

        @Override
        public void setup(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            if (be(level, portPos) instanceof ChainConveyorBlockEntity clbe)
                flipped = clbe.getSpeed() < 0;
        }

        @Override
        public ItemStack getIcon() {
            return AllItems.CHAIN_CONVEYOR.getDefaultStack();
        }

        @Override
        public boolean export(WorldAccess level, BlockPos portPos, ItemStack box, boolean simulate) {
            if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
                return false;
            if (connection != null && !clbe.connections.contains(connection))
                return false;
            if (simulate)
                return clbe.getSpeed() != 0 && clbe.canAcceptPackagesFor(connection);
            ChainConveyorPackage box2 = new ChainConveyorPackage(chainPos, box.copy());
            if (connection == null)
                return clbe.addLoopingPackage(box2);
            return clbe.addTravellingPackage(box2, connection);
        }

        @Override
        public void register(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
                return;
            ChainConveyorBlockEntity actualBe = clbe;

            // Jump to opposite chain if motion reversed
            if (connection != null && clbe.getSpeed() < 0 != flipped) {
                deregister(ppbe, level, portPos);
                actualBe = AllBlocks.CHAIN_CONVEYOR.getBlockEntity(level, clbe.getPos().add(connection));
                if (actualBe == null)
                    return;
                clbe.prepareStats();
                ConnectionStats stats = clbe.connectionStats.get(connection);
                if (stats != null)
                    chainPos = stats.chainLength() - chainPos;
                connection = connection.multiply(-1);
                flipped = !flipped;
                relativePos = actualBe.getPos().subtract(portPos);
                ppbe.notifyUpdate();
            }

            if (connection != null && !actualBe.connections.contains(connection))
                return;
            String portFilter = ppbe.getFilterString();
            if (portFilter == null)
                return;
            actualBe.routingTable.receivePortInfo(portFilter, connection == null ? BlockPos.ORIGIN : connection);
            Map<BlockPos, ConnectedPort> portMap = connection == null ? actualBe.loopPorts : actualBe.travelPorts;
            portMap.put(relativePos.multiply(-1), new ConnectedPort(chainPos, connection, portFilter));
        }

        @Override
        public void deregister(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
                return;
            clbe.loopPorts.remove(relativePos.multiply(-1));
            clbe.travelPorts.remove(relativePos.multiply(-1));
            String portFilter = ppbe.getFilterString();
            if (portFilter == null)
                return;
            clbe.routingTable.entriesByDistance.removeIf(e -> e.endOfRoute() && e.port().equals(portFilter));
            clbe.routingTable.changed = true;
        }

        @Override
        public Vec3d getExactTargetLocation(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            if (!(be(level, portPos) instanceof ChainConveyorBlockEntity clbe))
                return Vec3d.ZERO;
            return clbe.getPackagePosition(chainPos, connection);
        }

        @Override
        public boolean canSupport(BlockEntity be) {
            return be.getType() == AllBlockEntityTypes.PACKAGE_FROGPORT;
        }

        @Override
        protected PackagePortTargetType getType() {
            return AllPackagePortTargetTypes.CHAIN_CONVEYOR;
        }

        public static class Type implements PackagePortTargetType {
            @Override
            public MapCodec<ChainConveyorFrogportTarget> codec() {
                return CODEC;
            }

            @Override
            public PacketCodec<ByteBuf, ChainConveyorFrogportTarget> packetCodec() {
                return PACKET_CODEC;
            }
        }
    }

    public static class TrainStationFrogportTarget extends PackagePortTarget {
        public static MapCodec<TrainStationFrogportTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf(
            "relative_pos").forGetter(i -> i.relativePos)).apply(instance, TrainStationFrogportTarget::new));

        public static final PacketCodec<ByteBuf, TrainStationFrogportTarget> PACKET_CODEC = BlockPos.PACKET_CODEC.xmap(
            TrainStationFrogportTarget::new,
            i -> i.relativePos
        );

        public TrainStationFrogportTarget(BlockPos relativePos) {
            super(relativePos);
        }

        @Override
        public ItemStack getIcon() {
            return AllItems.TRACK_STATION.getDefaultStack();
        }

        @Override
        public boolean export(WorldAccess level, BlockPos portPos, ItemStack box, boolean simulate) {
            return false;
        }

        @Override
        public Vec3d getExactTargetLocation(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            return Vec3d.ofCenter(portPos.add(relativePos));
        }

        @Override
        public void register(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            if (be(level, portPos) instanceof StationBlockEntity sbe)
                sbe.attachPackagePort(ppbe);
        }

        @Override
        public void deregister(PackagePortBlockEntity ppbe, WorldAccess level, BlockPos portPos) {
            if (be(level, portPos) instanceof StationBlockEntity sbe)
                sbe.removePackagePort(ppbe);
        }

        @Override
        public boolean depositImmediately() {
            return true;
        }

        @Override
        public boolean canSupport(BlockEntity be) {
            return be.getType() == AllBlockEntityTypes.PACKAGE_POSTBOX;
        }

        @Override
        protected PackagePortTargetType getType() {
            return AllPackagePortTargetTypes.TRAIN_STATION;
        }

        public static class Type implements PackagePortTargetType {
            @Override
            public MapCodec<TrainStationFrogportTarget> codec() {
                return CODEC;
            }

            @Override
            public PacketCodec<ByteBuf, TrainStationFrogportTarget> packetCodec() {
                return PACKET_CODEC;
            }
        }
    }
}
