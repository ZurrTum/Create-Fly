package com.zurrtum.create.content.logistics.tunnel;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.logistics.funnel.BeltFunnelBlock;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlock.Shape;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.s2c.TunnelFlapPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

import java.util.*;

public class BeltTunnelBlockEntity extends SmartBlockEntity {

    public Map<Direction, LerpedFloat> flaps;
    public Set<Direction> sides;

    public Inventory cap = null;
    protected List<Pair<Direction, Boolean>> flapsToSend;

    public BeltTunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        flaps = new EnumMap<>(Direction.class);
        sides = new HashSet<>();
        flapsToSend = new LinkedList<>();
    }

    public static BeltTunnelBlockEntity andesite(BlockPos pos, BlockState state) {
        return new BeltTunnelBlockEntity(AllBlockEntityTypes.ANDESITE_TUNNEL, pos, state);
    }

    protected void writeFlapsAndSides(WriteView view) {
        WriteView.ListAppender<Direction> flapsList = view.getListAppender("Flaps", Direction.CODEC);
        for (Direction direction : flaps.keySet())
            flapsList.add(direction);

        WriteView.ListAppender<Direction> sidesList = view.getListAppender("Sides", Direction.CODEC);
        for (Direction direction : sides)
            sidesList.add(direction);
    }

    @Override
    public void writeSafe(WriteView view) {
        writeFlapsAndSides(view);
        super.writeSafe(view);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        writeFlapsAndSides(view);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        Set<Direction> newFlaps = new HashSet<>(6);
        ReadView.TypedListReadView<Direction> flapsList = view.getTypedListView("Flaps", Direction.CODEC);
        for (Direction direction : flapsList)
            newFlaps.add(direction);

        sides.clear();
        ReadView.TypedListReadView<Direction> sidesList = view.getTypedListView("Sides", Direction.CODEC);
        for (Direction direction : sidesList)
            sides.add(direction);

        for (Direction d : Iterate.directions)
            if (!newFlaps.contains(d))
                flaps.remove(d);
            else if (!flaps.containsKey(d))
                flaps.put(d, createChasingFlap());

        super.read(view, clientPacket);
        if (clientPacket)
            AllClientHandle.INSTANCE.queueUpdate(this);
    }

    private LerpedFloat createChasingFlap() {
        return LerpedFloat.linear().startWithValue(.25f).chase(0, .05f, Chaser.EXP);
    }

    public void updateTunnelConnections() {
        flaps.clear();
        sides.clear();
        BlockState tunnelState = getCachedState();
        for (Direction direction : Iterate.horizontalDirections) {
            if (direction.getAxis() != tunnelState.get(Properties.HORIZONTAL_AXIS)) {
                boolean positive = direction.getDirection() == AxisDirection.POSITIVE ^ direction.getAxis() == Axis.Z;
                Shape shape = tunnelState.get(BeltTunnelBlock.SHAPE);
                if (BeltTunnelBlock.isStraight(tunnelState))
                    continue;
                if (positive && shape == Shape.T_LEFT)
                    continue;
                if (!positive && shape == Shape.T_RIGHT)
                    continue;
            }

            sides.add(direction);

            // Flap might be occluded
            if (world == null)
                continue;
            BlockState nextState = world.getBlockState(pos.offset(direction));
            if (nextState.getBlock() instanceof BeltTunnelBlock)
                continue;
            if (nextState.getBlock() instanceof BeltFunnelBlock)
                if (nextState.get(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED && nextState.get(BeltFunnelBlock.HORIZONTAL_FACING) == direction.getOpposite())
                    continue;

            flaps.put(direction, createChasingFlap());
        }
        sendData();
    }

    public void flap(Direction side, boolean inward) {
        if (world.isClient) {
            if (flaps.containsKey(side))
                flaps.get(side).setValue(inward ? -1 : 1);
            return;
        }

        flapsToSend.add(Pair.of(side, inward));
    }

    @Override
    public void initialize() {
        super.initialize();
        updateTunnelConnections();
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient) {
            if (!flapsToSend.isEmpty())
                sendFlaps();
            return;
        }
        flaps.forEach((d, value) -> value.tickChaser());
    }

    private void sendFlaps() {
        if (world instanceof ServerWorld serverLevel) {
            TunnelFlapPacket packet = new TunnelFlapPacket(this, flapsToSend);
            for (ServerPlayerEntity player : serverLevel.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(new ChunkPos(pos), false)) {
                player.networkHandler.sendPacket(packet);
            }
        }
        flapsToSend.clear();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }
}
