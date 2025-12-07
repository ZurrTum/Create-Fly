package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.ComputerCraftProxy;
import com.zurrtum.create.compat.computercraft.events.SignalStateChangeEvent;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.SignalBlock.SignalType;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class SignalBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    public enum OverlayState implements StringIdentifiable {
        RENDER,
        SKIP,
        DUAL;
        public static final Codec<OverlayState> CODEC = StringIdentifiable.createCodec(OverlayState::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum SignalState implements StringIdentifiable {
        RED,
        YELLOW,
        GREEN,
        INVALID;

        public static final Codec<SignalState> CODEC = StringIdentifiable.createCodec(SignalState::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean isRedLight(float renderTime) {
            return this == RED || this == INVALID && renderTime % 40 < 3;
        }

        public boolean isYellowLight(float renderTime) {
            return this == YELLOW;
        }

        public boolean isGreenLight(float renderTime) {
            return this == GREEN;
        }
    }

    public TrackTargetingBehaviour<SignalBoundary> edgePoint;

    private SignalState state;
    private OverlayState overlay;
    private int switchToRedAfterTrainEntered;
    private boolean lastReportedPower;
    public AbstractComputerBehaviour computerBehaviour;

    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK_SIGNAL, pos, state);
        this.state = SignalState.INVALID;
        this.overlay = OverlayState.SKIP;
        this.lastReportedPower = false;
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.put("State", SignalState.CODEC, state);
        view.put("Overlay", OverlayState.CODEC, overlay);
        view.putBoolean("Power", lastReportedPower);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        state = view.read("State", SignalState.CODEC).orElse(SignalState.RED);
        overlay = view.read("Overlay", OverlayState.CODEC).orElse(OverlayState.RENDER);
        lastReportedPower = view.getBoolean("Power", false);
        invalidateRenderBoundingBox();
    }

    @Nullable
    public SignalBoundary getSignal() {
        return edgePoint.getEdgePoint();
    }

    public boolean isPowered() {
        return state == SignalState.RED;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.SIGNAL);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
        behaviours.add(edgePoint);
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient)
            return;

        SignalBoundary boundary = getSignal();
        if (boundary == null) {
            enterState(SignalState.INVALID);
            setOverlay(OverlayState.RENDER);
            return;
        }

        BlockState blockState = getCachedState();

        blockState.getOrEmpty(SignalBlock.POWERED).ifPresent(powered -> {
            if (lastReportedPower == powered)
                return;
            lastReportedPower = powered;
            boundary.updateBlockEntityPower(this);
            notifyUpdate();
        });

        blockState.getOrEmpty(SignalBlock.TYPE).ifPresent(stateType -> {
            SignalType targetType = boundary.getTypeFor(pos);
            if (stateType != targetType) {
                world.setBlockState(pos, blockState.with(SignalBlock.TYPE, targetType), Block.NOTIFY_ALL);
                refreshBlockState();
            }
        });

        enterState(boundary.getStateFor(pos));
        setOverlay(boundary.getOverlayFor(pos));
    }

    public boolean getReportedPower() {
        return lastReportedPower;
    }

    public SignalState getState() {
        return state;
    }

    public OverlayState getOverlay() {
        return overlay;
    }

    public void setOverlay(OverlayState state) {
        if (this.overlay == state)
            return;
        this.overlay = state;
        notifyUpdate();
    }

    public void enterState(SignalState state) {
        if (switchToRedAfterTrainEntered > 0)
            switchToRedAfterTrainEntered--;
        if (this.state == state)
            return;
        if (state == SignalState.RED && switchToRedAfterTrainEntered > 0)
            return;
        this.state = state;
        switchToRedAfterTrainEntered = state == SignalState.GREEN || state == SignalState.YELLOW ? 15 : 0;
        if (computerBehaviour.hasAttachedComputer())
            computerBehaviour.prepareComputerEvent(new SignalStateChangeEvent(state));
        notifyUpdate();
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(Vec3d.of(pos), Vec3d.of(edgePoint.getGlobalPosition())).expand(2);
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        edgePoint.transform(be, transform);
    }
}
