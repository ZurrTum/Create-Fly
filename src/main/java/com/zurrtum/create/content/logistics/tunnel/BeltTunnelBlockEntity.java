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

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BeltTunnelBlockEntity extends SmartBlockEntity {

    public Map<Direction, LerpedFloat> flaps;
    public Set<Direction> sides;

    public Container cap = null;
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

    protected void writeFlapsAndSides(ValueOutput view) {
        ValueOutput.TypedOutputList<Direction> flapsList = view.list("Flaps", Direction.CODEC);
        for (Direction direction : flaps.keySet())
            flapsList.add(direction);

        ValueOutput.TypedOutputList<Direction> sidesList = view.list("Sides", Direction.CODEC);
        for (Direction direction : sides)
            sidesList.add(direction);
    }

    @Override
    public void writeSafe(ValueOutput view) {
        writeFlapsAndSides(view);
        super.writeSafe(view);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        writeFlapsAndSides(view);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        Set<Direction> newFlaps = new HashSet<>(6);
        ValueInput.TypedInputList<Direction> flapsList = view.listOrEmpty("Flaps", Direction.CODEC);
        for (Direction direction : flapsList)
            newFlaps.add(direction);

        sides.clear();
        ValueInput.TypedInputList<Direction> sidesList = view.listOrEmpty("Sides", Direction.CODEC);
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
        BlockState tunnelState = getBlockState();
        for (Direction direction : Iterate.horizontalDirections) {
            if (direction.getAxis() != tunnelState.getValue(BlockStateProperties.HORIZONTAL_AXIS)) {
                boolean positive = direction.getAxisDirection() == AxisDirection.POSITIVE ^ direction.getAxis() == Axis.Z;
                Shape shape = tunnelState.getValue(BeltTunnelBlock.SHAPE);
                if (BeltTunnelBlock.isStraight(tunnelState))
                    continue;
                if (positive && shape == Shape.T_LEFT)
                    continue;
                if (!positive && shape == Shape.T_RIGHT)
                    continue;
            }

            sides.add(direction);

            // Flap might be occluded
            if (level == null)
                continue;
            BlockState nextState = level.getBlockState(worldPosition.relative(direction));
            if (nextState.getBlock() instanceof BeltTunnelBlock)
                continue;
            if (nextState.getBlock() instanceof BeltFunnelBlock)
                if (nextState.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED && nextState.getValue(BeltFunnelBlock.HORIZONTAL_FACING) == direction.getOpposite())
                    continue;

            flaps.put(direction, createChasingFlap());
        }
        sendData();
    }

    public void flap(Direction side, boolean inward) {
        if (level.isClientSide()) {
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
        if (!level.isClientSide()) {
            if (!flapsToSend.isEmpty())
                sendFlaps();
            return;
        }
        flaps.forEach((d, value) -> value.tickChaser());
    }

    private void sendFlaps() {
        if (level instanceof ServerLevel serverLevel) {
            TunnelFlapPacket packet = new TunnelFlapPacket(this, flapsToSend);
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)) {
                player.connection.send(packet);
            }
        }
        flapsToSend.clear();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }
}
