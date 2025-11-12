package com.zurrtum.create.content.trains.signal;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.SignalBlock.SignalType;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SignalBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    public enum OverlayState implements StringRepresentable {
        RENDER,
        SKIP,
        DUAL;
        public static final Codec<OverlayState> CODEC = StringRepresentable.fromEnum(OverlayState::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum SignalState implements StringRepresentable {
        RED,
        YELLOW,
        GREEN,
        INVALID;

        public static final Codec<SignalState> CODEC = StringRepresentable.fromEnum(SignalState::values);

        @Override
        public String getSerializedName() {
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

    public SignalBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TRACK_SIGNAL, pos, state);
        this.state = SignalState.INVALID;
        this.overlay = OverlayState.SKIP;
        this.lastReportedPower = false;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store("State", SignalState.CODEC, state);
        view.store("Overlay", OverlayState.CODEC, overlay);
        view.putBoolean("Power", lastReportedPower);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        state = view.read("State", SignalState.CODEC).orElse(SignalState.RED);
        overlay = view.read("Overlay", OverlayState.CODEC).orElse(OverlayState.RENDER);
        lastReportedPower = view.getBooleanOr("Power", false);
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
        behaviours.add(edgePoint);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide())
            return;

        SignalBoundary boundary = getSignal();
        if (boundary == null) {
            enterState(SignalState.INVALID);
            setOverlay(OverlayState.RENDER);
            return;
        }

        BlockState blockState = getBlockState();

        blockState.getOptionalValue(SignalBlock.POWERED).ifPresent(powered -> {
            if (lastReportedPower == powered)
                return;
            lastReportedPower = powered;
            boundary.updateBlockEntityPower(this);
            notifyUpdate();
        });

        blockState.getOptionalValue(SignalBlock.TYPE).ifPresent(stateType -> {
            SignalType targetType = boundary.getTypeFor(worldPosition);
            if (stateType != targetType) {
                level.setBlock(worldPosition, blockState.setValue(SignalBlock.TYPE, targetType), Block.UPDATE_ALL);
                refreshBlockState();
            }
        });

        enterState(boundary.getStateFor(worldPosition));
        setOverlay(boundary.getOverlayFor(worldPosition));
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
        notifyUpdate();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(edgePoint.getGlobalPosition())).inflate(2);
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        edgePoint.transform(be, transform);
    }

}
