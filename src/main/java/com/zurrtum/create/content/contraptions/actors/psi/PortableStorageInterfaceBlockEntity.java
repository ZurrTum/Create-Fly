package com.zurrtum.create.content.contraptions.actors.psi;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public abstract class PortableStorageInterfaceBlockEntity extends SmartBlockEntity {

    public static final int ANIMATION = 4;
    protected int transferTimer;
    protected float distance;
    protected LerpedFloat connectionAnimation;
    protected boolean powered;
    protected Entity connectedEntity;

    public int keepAlive = 0;

    public PortableStorageInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        transferTimer = 0;
        connectionAnimation = LerpedFloat.linear().startWithValue(0);
        powered = false;
    }

    public void startTransferringTo(Contraption contraption, float distance) {
        if (connectedEntity == contraption.entity)
            return;
        this.distance = Math.min(2, distance);
        connectedEntity = contraption.entity;
        startConnecting();
        notifyUpdate();
    }

    protected void stopTransferring() {
        connectedEntity = null;
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock(), null);
    }

    public boolean canTransfer() {
        if (connectedEntity != null && !connectedEntity.isAlive())
            stopTransferring();
        return connectedEntity != null && isConnected();
    }

    @Override
    public void initialize() {
        super.initialize();
        powered = level.hasNeighborSignal(worldPosition);
        if (!powered)
            notifyContraptions();
    }

    @Override
    public void tick() {
        super.tick();
        boolean wasConnected = isConnected();
        int timeUnit = getTransferTimeout();
        int animation = ANIMATION;

        if (keepAlive > 0) {
            keepAlive--;
            if (keepAlive == 0 && !level.isClientSide()) {
                stopTransferring();
                transferTimer = ANIMATION - 1;
                sendData();
                return;
            }
        }

        transferTimer = Math.min(transferTimer, ANIMATION * 2 + timeUnit);

        boolean timerCanDecrement = transferTimer > ANIMATION || transferTimer > 0 && keepAlive == 0 && (isVirtual() || !level.isClientSide() || transferTimer != ANIMATION);

        if (timerCanDecrement && (!isVirtual() || transferTimer != ANIMATION)) {
            transferTimer--;
            if (transferTimer == ANIMATION - 1)
                sendData();
            if (transferTimer <= 0 || powered)
                stopTransferring();
        }

        boolean isConnected = isConnected();
        if (wasConnected != isConnected && !level.isClientSide())
            setChanged();

        float progress = 0;
        if (isConnected)
            progress = 1;
        else if (transferTimer >= timeUnit + animation)
            progress = Mth.lerpInt((transferTimer - timeUnit - animation) / (float) animation, 1, 0);
        else if (transferTimer < animation)
            progress = Mth.lerpInt(transferTimer / (float) animation, 0, 1);
        connectionAnimation.setValue(progress);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        transferTimer = view.getIntOr("Timer", 0);
        distance = view.getFloatOr("Distance", 0);
        boolean poweredPreviously = powered;
        powered = view.getBooleanOr("Powered", false);
        if (clientPacket && powered != poweredPreviously && !powered)
            notifyContraptions();
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("Timer", transferTimer);
        view.putFloat("Distance", distance);
        view.putBoolean("Powered", powered);
    }

    public void neighbourChanged() {
        boolean isBlockPowered = level.hasNeighborSignal(worldPosition);
        if (isBlockPowered == powered)
            return;
        powered = isBlockPowered;
        if (!powered)
            notifyContraptions();
        if (powered)
            stopTransferring();
        sendData();
    }

    private void notifyContraptions() {
        level.getEntitiesOfClass(AbstractContraptionEntity.class, new AABB(worldPosition).inflate(3)).forEach(AbstractContraptionEntity::refreshPSIs);
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    public boolean isTransferring() {
        return transferTimer > ANIMATION;
    }

    public boolean isConnected() {
        int timeUnit = getTransferTimeout();
        return transferTimer >= ANIMATION && transferTimer <= timeUnit + ANIMATION;
    }

    public float getExtensionDistance(float partialTicks) {
        return (float) (Math.pow(connectionAnimation.getValue(partialTicks), 2) * distance / 2);
    }

    float getConnectionDistance() {
        return distance;
    }

    public void startConnecting() {
        transferTimer = 6 + ANIMATION * 2;
    }

    public void onContentTransferred() {
        int timeUnit = getTransferTimeout();
        transferTimer = timeUnit + ANIMATION;
        award(AllAdvancements.PSI);
        sendData();
    }

    protected Integer getTransferTimeout() {
        return AllConfigs.server().logistics.psiTimeout.get();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.PSI);
    }

}
