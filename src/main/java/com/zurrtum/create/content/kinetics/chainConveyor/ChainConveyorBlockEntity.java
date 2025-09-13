package com.zurrtum.create.content.kinetics.chainConveyor;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ChainConveyorBlockEntity extends KineticBlockEntity implements TransformableBlockEntity {
    public static final Codec<List<ChainConveyorPackage>> PACKAGE_CODEC = ChainConveyorPackage.CODEC.listOf();
    public static final Codec<List<ChainConveyorPackage>> CLIENT_PACKAGE_CODEC = ChainConveyorPackage.CLIENT_CODEC.listOf();
    public static final Codec<Map<BlockPos, List<ChainConveyorPackage>>> MAP_CODEC = CreateCodecs.getCodecMap(BlockPos.CODEC, PACKAGE_CODEC);
    public static final Codec<Map<BlockPos, List<ChainConveyorPackage>>> CLIENT_MAP_CODEC = CreateCodecs.getCodecMap(
        BlockPos.CODEC,
        CreateCodecs.getArrayListCodec(ChainConveyorPackage.CLIENT_CODEC)
    );

    public record ConnectionStats(float tangentAngle, float chainLength, Vec3d start, Vec3d end) {
    }

    public record ConnectedPort(float chainPosition, @Nullable BlockPos connection, String filter) {
    }

    public Set<BlockPos> connections = new HashSet<>();
    public Map<BlockPos, ConnectionStats> connectionStats;

    public Map<BlockPos, ConnectedPort> loopPorts = new HashMap<>();
    public Map<BlockPos, ConnectedPort> travelPorts = new HashMap<>();
    public ChainConveyorRoutingTable routingTable = new ChainConveyorRoutingTable();

    List<ChainConveyorPackage> loopingPackages = new ArrayList<>();
    Map<BlockPos, List<ChainConveyorPackage>> travellingPackages = new HashMap<>();

    public boolean reversed;
    public boolean cancelDrops;
    public boolean checkInvalid;

    BlockPos chainDestroyedEffectToSend;

    public ChainConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CHAIN_CONVEYOR, pos, state);
        checkInvalid = true;
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(pos).expand(connections.isEmpty() ? 3 : 64);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        updateChainShapes();
    }

    public boolean canAcceptMorePackages() {
        return loopingPackages.size() + travellingPackages.size() < AllConfigs.server().logistics.chainConveyorCapacity.get();
    }

    public boolean canAcceptPackagesFor(@Nullable BlockPos connection) {
        if (connection == null && !canAcceptMorePackages())
            return false;
        return connection == null || (world.getBlockEntity(pos.add(connection)) instanceof ChainConveyorBlockEntity otherClbe && otherClbe.canAcceptMorePackages());
    }

    public boolean canAcceptMorePackagesFromOtherConveyor() {
        return loopingPackages.size() < AllConfigs.server().logistics.chainConveyorCapacity.get();
    }

    //    @Override
    //    public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
    //        return super.addToTooltip(tooltip, isPlayerSneaking);
    //
    //        // debug routing info
    //        //		tooltip.addAll(routingTable.createSummary());
    //        //		if (!loopPorts.isEmpty())
    //        //			tooltip.add(Component.literal(loopPorts.size() + " Loop ports"));
    //        //		if (!travelPorts.isEmpty())
    //        //			tooltip.add(Component.literal(travelPorts.size() + " Travel ports"));
    //        //		return true;
    //    }

    @Override
    public void tick() {
        super.tick();

        if (checkInvalid && !world.isClient()) {
            checkInvalid = false;
            removeInvalidConnections();
        }

        float serverSpeed = world.isClient() && !isVirtual() ? AllClientHandle.INSTANCE.getServerSpeed() : 1f;
        float speed = getSpeed() / 360f;
        float radius = 1.5f;
        float distancePerTick = Math.abs(speed);
        float degreesPerTick = (speed / (MathHelper.PI * radius)) * 360f;
        boolean reversedPreviously = reversed;

        prepareStats();

        if (world.isClient()) {
            getBehaviour(ChainConveyorBehaviour.TYPE).blockEntityTickBoxVisuals();
        }

        if (!world.isClient()) {
            routingTable.tick();
            if (routingTable.shouldAdvertise()) {
                for (BlockPos pos : connections)
                    if (world.getBlockEntity(this.pos.add(pos)) instanceof ChainConveyorBlockEntity clbe)
                        routingTable.advertiseTo(pos, clbe.routingTable);
                routingTable.changed = false;
                routingTable.lastUpdate = 0;
            }
        }

        if (speed == 0) {
            updateBoxWorldPositions();
            return;
        }

        if (reversedPreviously != reversed) {
            for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : travellingPackages.entrySet()) {
                BlockPos offset = entry.getKey();
                if (!(world.getBlockEntity(pos.add(offset)) instanceof ChainConveyorBlockEntity otherLift))
                    continue;
                for (Iterator<ChainConveyorPackage> iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
                    ChainConveyorPackage box = iterator.next();
                    if (box.justFlipped)
                        continue;
                    box.justFlipped = true;
                    float length = (float) Vec3d.of(offset).length() - 22 / 16f;
                    box.chainPosition = length - box.chainPosition;
                    otherLift.addTravellingPackage(box, offset.multiply(-1));
                    iterator.remove();
                }
            }
            notifyUpdate();
        }

        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : travellingPackages.entrySet()) {
            BlockPos target = entry.getKey();
            ConnectionStats stats = connectionStats.get(target);
            if (stats == null)
                continue;

            Travelling:
            for (Iterator<ChainConveyorPackage> iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
                ChainConveyorPackage box = iterator.next();
                box.justFlipped = false;

                float prevChainPosition = box.chainPosition;
                box.chainPosition += serverSpeed * distancePerTick;
                box.chainPosition = Math.min(stats.chainLength, box.chainPosition);

                float anticipatePosition = box.chainPosition;
                anticipatePosition += serverSpeed * distancePerTick * 4;
                anticipatePosition = Math.min(stats.chainLength, anticipatePosition);

                if (world.isClient() && !isVirtual())
                    continue;

                for (Map.Entry<BlockPos, ConnectedPort> portEntry : travelPorts.entrySet()) {
                    ConnectedPort port = portEntry.getValue();
                    float chainPosition = port.chainPosition();

                    if (prevChainPosition > chainPosition)
                        continue;
                    if (!target.equals(port.connection))
                        continue;

                    boolean notAtPositionYet = box.chainPosition < chainPosition;
                    if (notAtPositionYet && anticipatePosition < chainPosition)
                        continue;
                    if (!PackageItem.matchAddress(box.item, port.filter()))
                        continue;
                    if (notAtPositionYet) {
                        notifyPortToAnticipate(portEntry.getKey());
                        continue;
                    }

                    if (!exportToPort(box, portEntry.getKey()))
                        continue;

                    iterator.remove();
                    notifyUpdate();
                    continue Travelling;
                }

                if (box.chainPosition < stats.chainLength)
                    continue;

                // transfer to other
                if (world.getBlockEntity(pos.add(target)) instanceof ChainConveyorBlockEntity clbe) {
                    box.chainPosition = wrapAngle(stats.tangentAngle + 180 + 2 * 35 * (reversed ? -1 : 1));
                    clbe.addLoopingPackage(box);
                    iterator.remove();
                    notifyUpdate();
                }
            }
        }

        Looping:
        for (Iterator<ChainConveyorPackage> iterator = loopingPackages.iterator(); iterator.hasNext(); ) {
            ChainConveyorPackage box = iterator.next();
            box.justFlipped = false;

            float prevChainPosition = box.chainPosition;
            box.chainPosition += serverSpeed * degreesPerTick;
            box.chainPosition = wrapAngle(box.chainPosition);

            float anticipatePosition = box.chainPosition;
            anticipatePosition += serverSpeed * degreesPerTick * 4;
            anticipatePosition = wrapAngle(anticipatePosition);

            if (world.isClient())
                continue;

            for (Map.Entry<BlockPos, ConnectedPort> portEntry : loopPorts.entrySet()) {
                ConnectedPort port = portEntry.getValue();
                float offBranchAngle = port.chainPosition();

                boolean notAtPositionYet = !loopThresholdCrossed(box.chainPosition, prevChainPosition, offBranchAngle);
                if (notAtPositionYet && !loopThresholdCrossed(anticipatePosition, prevChainPosition, offBranchAngle))
                    continue;
                if (!PackageItem.matchAddress(box.item, port.filter()))
                    continue;
                if (notAtPositionYet) {
                    notifyPortToAnticipate(portEntry.getKey());
                    continue;
                }

                if (!exportToPort(box, portEntry.getKey()))
                    continue;

                iterator.remove();
                notifyUpdate();
                continue Looping;
            }

            for (BlockPos connection : connections) {
                if (world.getBlockEntity(pos.add(connection)) instanceof ChainConveyorBlockEntity ccbe && !ccbe.canAcceptMorePackagesFromOtherConveyor())
                    continue;

                float offBranchAngle = connectionStats.get(connection).tangentAngle;

                if (!loopThresholdCrossed(box.chainPosition, prevChainPosition, offBranchAngle))
                    continue;
                if (!routingTable.getExitFor(box.item).equals(connection))
                    continue;

                box.chainPosition = 0;
                addTravellingPackage(box, connection);
                iterator.remove();
                continue Looping;
            }
        }

        updateBoxWorldPositions();
    }

    public void removeInvalidConnections() {
        boolean changed = false;
        for (Iterator<BlockPos> iterator = connections.iterator(); iterator.hasNext(); ) {
            BlockPos next = iterator.next();
            BlockPos target = pos.add(next);
            if (!world.isPosLoaded(target))
                continue;
            if (world.getBlockEntity(target) instanceof ChainConveyorBlockEntity ccbe && ccbe.connections.contains(next.multiply(-1)))
                continue;
            iterator.remove();
            changed = true;
        }
        if (changed)
            notifyUpdate();
    }

    public void notifyConnectedToValidate() {
        for (BlockPos blockPos : connections) {
            BlockPos target = pos.add(blockPos);
            if (!world.isPosLoaded(target))
                continue;
            if (world.getBlockEntity(target) instanceof ChainConveyorBlockEntity ccbe)
                ccbe.checkInvalid = true;
        }
    }

    public boolean loopThresholdCrossed(float chainPosition, float prevChainPosition, float offBranchAngle) {
        int sign1 = MathHelper.sign(AngleHelper.getShortestAngleDiff(offBranchAngle, prevChainPosition));
        int sign2 = MathHelper.sign(AngleHelper.getShortestAngleDiff(offBranchAngle, chainPosition));
        boolean notCrossed = sign1 >= sign2 && !reversed || sign1 <= sign2 && reversed;
        return !notCrossed;
    }

    private boolean exportToPort(ChainConveyorPackage box, BlockPos offset) {
        BlockPos globalPos = pos.add(offset);
        if (!(world.getBlockEntity(globalPos) instanceof FrogportBlockEntity ppbe))
            return false;

        if (ppbe.isAnimationInProgress())
            return false;
        if (ppbe.isBackedUp())
            return false;

        ppbe.startAnimation(box.item, false);
        return true;
    }

    private void notifyPortToAnticipate(BlockPos offset) {
        if (world.getBlockEntity(pos.add(offset)) instanceof FrogportBlockEntity ppbe)
            ppbe.sendAnticipate();
    }

    public boolean addTravellingPackage(ChainConveyorPackage box, BlockPos connection) {
        if (!connections.contains(connection))
            return false;
        travellingPackages.computeIfAbsent(connection, $ -> new ArrayList<>()).add(box);
        if (world.isClient)
            return true;
        notifyUpdate();
        return true;
    }

    @Override
    public void notifyUpdate() {
        world.markDirty(pos);
        sendData();
    }

    public boolean addLoopingPackage(ChainConveyorPackage box) {
        loopingPackages.add(box);
        notifyUpdate();
        return true;
    }

    public void prepareStats() {
        float speed = getSpeed();
        if (reversed != speed < 0 && speed != 0) {
            reversed = speed < 0;
            connectionStats = null;
        }
        if (connectionStats == null) {
            connectionStats = new HashMap<>();
            connections.forEach(this::calculateConnectionStats);
        }
    }

    public void updateBoxWorldPositions() {
        prepareStats();

        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : travellingPackages.entrySet()) {
            BlockPos target = entry.getKey();
            ConnectionStats stats = connectionStats.get(target);
            if (stats == null)
                continue;
            for (ChainConveyorPackage box : entry.getValue()) {
                box.worldPosition = getPackagePosition(box.chainPosition, target);
                if (world == null || !world.isClient())
                    continue;
                Vec3d diff = stats.end.subtract(stats.start).normalize();
                box.yaw = MathHelper.wrapDegrees((float) MathHelper.atan2(diff.x, diff.z) * MathHelper.DEGREES_PER_RADIAN - 90);
            }
        }

        for (ChainConveyorPackage box : loopingPackages) {
            box.worldPosition = getPackagePosition(box.chainPosition, null);
            box.yaw = MathHelper.wrapDegrees(box.chainPosition);
            if (reversed)
                box.yaw += 180;
        }
    }

    public Vec3d getPackagePosition(float chainPosition, @Nullable BlockPos travelTarget) {
        if (travelTarget == null)
            return Vec3d.ofBottomCenter(pos).add(VecHelper.rotate(new Vec3d(0, 6 / 16f, 0.875), chainPosition, Axis.Y));
        prepareStats();
        ConnectionStats stats = connectionStats.get(travelTarget);
        if (stats == null)
            return Vec3d.ZERO;
        Vec3d diff = stats.end.subtract(stats.start).normalize();
        return stats.start.add(diff.multiply(Math.min(stats.chainLength, chainPosition)));
    }

    private void calculateConnectionStats(BlockPos connection) {
        boolean reversed = getSpeed() < 0;
        float offBranchDistance = 35f;
        float direction = MathHelper.DEGREES_PER_RADIAN * (float) MathHelper.atan2(connection.getX(), connection.getZ());
        float angle = wrapAngle(direction - offBranchDistance * (reversed ? -1 : 1));
        float oppositeAngle = wrapAngle(angle + 180 + 2 * offBranchDistance * (reversed ? -1 : 1));

        Vec3d start = Vec3d.ofBottomCenter(pos).add(VecHelper.rotate(new Vec3d(0, 0, 1.25), angle, Axis.Y)).add(0, 6 / 16f, 0);

        Vec3d end = Vec3d.ofBottomCenter(pos.add(connection)).add(VecHelper.rotate(new Vec3d(0, 0, 1.25), oppositeAngle, Axis.Y)).add(0, 6 / 16f, 0);

        float length = (float) start.distanceTo(end);
        connectionStats.put(connection, new ConnectionStats(angle, length, start, end));
    }

    public boolean addConnectionTo(BlockPos target) {
        BlockPos localTarget = target.subtract(pos);
        boolean added = connections.add(localTarget);
        if (added) {
            notifyUpdate();
            calculateConnectionStats(localTarget);
            updateChainShapes();
        }

        detachKinetics();
        updateSpeed = true;

        return added;
    }

    public void chainDestroyed(BlockPos target, boolean spawnDrops, boolean sendEffect) {
        int chainCount = getChainCost(target);
        if (sendEffect) {
            chainDestroyedEffectToSend = target;
            sendData();
        }
        if (!spawnDrops)
            return;

        if (!forPointsAlongChains(
            target,
            chainCount,
            vec -> world.spawnEntity(new ItemEntity(world, vec.x, vec.y, vec.z, new ItemStack(Items.CHAIN)))
        )) {
            while (chainCount > 0) {
                Block.dropStack(world, pos, new ItemStack(Blocks.CHAIN.asItem(), Math.min(chainCount, 64)));
                chainCount -= 64;
            }
        }
    }

    public boolean removeConnectionTo(BlockPos target) {
        BlockPos localTarget = target.subtract(pos);
        if (!connections.contains(localTarget))
            return false;

        detachKinetics();
        connections.remove(localTarget);
        connectionStats.remove(localTarget);
        List<ChainConveyorPackage> packages = travellingPackages.remove(localTarget);
        if (packages != null)
            for (ChainConveyorPackage box : packages)
                drop(box);
        notifyUpdate();
        updateChainShapes();
        updateSpeed = true;

        return true;
    }

    private void updateChainShapes() {
        prepareStats();
        if (world != null && world.isClient()) {
            getBehaviour(ChainConveyorBehaviour.TYPE).updateChainShapes();
        }
    }

    @Override
    public void remove() {
        super.remove();
        destroy();
        if (world == null || !world.isClient())
            return;
        for (BlockPos blockPos : connections)
            spawnDestroyParticles(blockPos);
    }

    private void spawnDestroyParticles(BlockPos blockPos) {
        forPointsAlongChains(
            blockPos,
            (int) Math.round(Vec3d.of(blockPos).length() * 8),
            vec -> world.addParticleClient(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.CHAIN.getDefaultState()),
                vec.x,
                vec.y,
                vec.z,
                0,
                0,
                0
            )
        );
    }

    @Override
    public void destroy() {
        super.destroy();

        for (BlockPos blockPos : connections) {
            chainDestroyed(blockPos, !cancelDrops, false);
            if (world.getBlockEntity(pos.add(blockPos)) instanceof ChainConveyorBlockEntity clbe)
                clbe.removeConnectionTo(pos);
        }

        for (ChainConveyorPackage box : loopingPackages)
            drop(box);
        for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : travellingPackages.entrySet())
            for (ChainConveyorPackage box : entry.getValue())
                drop(box);
    }

    public boolean forPointsAlongChains(BlockPos connection, int positions, Consumer<Vec3d> callback) {
        prepareStats();
        ConnectionStats stats = connectionStats.get(connection);
        if (stats == null)
            return false;

        Vec3d start = stats.start;
        Vec3d direction = stats.end.subtract(start);
        Vec3d origin = Vec3d.ofCenter(pos);
        Vec3d normal = direction.crossProduct(new Vec3d(0, 1, 0)).normalize();
        Vec3d offset = start.subtract(origin);
        Vec3d start2 = origin.add(offset.add(normal.multiply(-2 * normal.dotProduct(offset))));

        for (boolean firstChain : Iterate.trueAndFalse) {
            int steps = positions / 2;
            if (firstChain)
                steps += positions % 2;
            for (int i = 0; i < steps; i++)
                callback.accept((firstChain ? start : start2).add(direction.multiply((0.5 + i) / steps)));
        }

        return true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (world != null && world.isClient()) {
            getBehaviour(ChainConveyorBehaviour.TYPE).invalidate();
        }
    }

    private void drop(ChainConveyorPackage box) {
        if (box.worldPosition != null)
            world.spawnEntity(PackageEntity.fromItemStack(world, box.worldPosition.subtract(0, 0.5, 0), box.item));
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        connections.forEach(p -> neighbours.add(pos.add(p)));
        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    public float propagateRotationTo(
        KineticBlockEntity target,
        BlockState stateFrom,
        BlockState stateTo,
        BlockPos diff,
        boolean connectedViaAxes,
        boolean connectedViaCogs
    ) {
        if (connections.contains(target.getPos().subtract(pos))) {
            if (!(target instanceof ChainConveyorBlockEntity))
                return 0;
            return 1;
        }
        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        view.put("Connections", CreateCodecs.BLOCKPOS_SET_CODEC, connections);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket && chainDestroyedEffectToSend != null) {
            view.put("DestroyEffect", BlockPos.CODEC, chainDestroyedEffectToSend);
            chainDestroyedEffectToSend = null;
        }

        view.put("Connections", CreateCodecs.BLOCKPOS_SET_CODEC, connections);
        view.put("TravellingPackages", clientPacket ? CLIENT_MAP_CODEC : MAP_CODEC, travellingPackages);
        view.put("LoopingPackages", clientPacket ? CLIENT_PACKAGE_CODEC : PACKAGE_CODEC, loopingPackages);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket) {
            view.read("DestroyEffect", BlockPos.CODEC).ifPresent(this::spawnDestroyParticles);
        }

        int sizeBefore = connections.size();
        connections.clear();
        travellingPackages.clear();
        loopingPackages.clear();
        view.read("Connections", CreateCodecs.BLOCKPOS_SET_CODEC).ifPresent(data -> connections.addAll(data));
        view.read("TravellingPackages", CLIENT_MAP_CODEC).ifPresent(map -> travellingPackages.putAll(map));
        view.read("LoopingPackages", CLIENT_PACKAGE_CODEC).ifPresent(list -> loopingPackages.addAll(list));
        connectionStats = null;
        updateBoxWorldPositions();
        updateChainShapes();

        if (connections.size() != sizeBefore && world != null && world.isClient)
            invalidateRenderBoundingBox();
    }

    public float wrapAngle(float angle) {
        angle %= 360;
        if (angle < 0)
            angle += 360;
        return angle;
    }

    public static int getChainCost(BlockPos connection) {
        return (int) Math.max(Math.round(Vec3d.of(connection).length() / 2.5), 1);
    }

    public static boolean getChainsFromInventory(PlayerEntity player, ItemStack chain, int cost, boolean simulate) {
        int found = 0;

        PlayerInventory inv = player.getInventory();
        int size = PlayerInventory.MAIN_SIZE;
        for (int j = 0; j <= size + 1; j++) {
            int i = j;
            boolean offhand = j == size + 1;
            if (j == size)
                i = inv.getSelectedSlot();
            else if (offhand)
                i = 0;
            else if (j == inv.getSelectedSlot())
                continue;

            ItemStack stackInSlot = offhand ? player.getStackInHand(Hand.OFF_HAND) : inv.getStack(i);
            if (!ItemStack.areItemsEqual(stackInSlot, chain))
                continue;
            if (found >= cost)
                continue;

            int count = stackInSlot.getCount();

            if (!simulate) {
                int remainingItems = count - Math.min(cost - found, count);
                ItemStack newItem = stackInSlot.copyWithCount(remainingItems);
                if (offhand)
                    player.setStackInHand(Hand.OFF_HAND, newItem);
                else
                    inv.setStack(i, newItem);
            }

            found += count;
        }

        return found >= cost;
    }

    public List<ChainConveyorPackage> getLoopingPackages() {
        return loopingPackages;
    }

    public Map<BlockPos, List<ChainConveyorPackage>> getTravellingPackages() {
        return travellingPackages;
    }

    //    @Override
    //    public ItemRequirement getRequiredItems(BlockState state) {
    //        // TODO: Uncomment when Schematicannon is able to print these with chains
    //        //		int totalCost = 0;
    //        //		for (BlockPos pos : connections)
    //        //			totalCost += getChainCost(pos);
    //        //		if (totalCost > 0)
    //        //			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(Items.CHAIN, Mth.ceil(totalCost / 2.0)));
    //        return super.getRequiredItems(state);
    //    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        if (connections == null || connections.isEmpty())
            return;

        connections = new HashSet<>(connections.stream().map(transform::applyWithoutOffset).toList());

        HashMap<BlockPos, List<ChainConveyorPackage>> newMap = new HashMap<>();
        travellingPackages.forEach((key, value) -> newMap.put(transform.applyWithoutOffset(key), value));
        travellingPackages = newMap;

        connectionStats = null;
        notifyUpdate();
    }

}
