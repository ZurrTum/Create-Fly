package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class LinkWithBulbBlockEntity extends SmartBlockEntity {

    private LerpedFloat glow;
    private boolean sendPulse;

    public LinkWithBulbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        glow = LerpedFloat.linear().startWithValue(0);
        glow.chase(0, 0.5f, Chaser.EXP);
    }

    @Override
    public void tick() {
        super.tick();
        if (isVirtual() || level.isClientSide())
            glow.tickChaser();
    }

    public float getGlow(float partialTicks) {
        return glow.getValue(partialTicks);
    }

    public void sendPulseNextSync() {
        sendPulse = true;
    }

    public void pulse() {
        glow.setValue(2);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket && sendPulse) {
            sendPulse = false;
            view.putBoolean("Pulse", true);
        }
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket && view.getBooleanOr("Pulse", false))
            pulse();
    }

    public Vec3 getBulbOffset(BlockState state) {
        return Vec3.ZERO;
    }

    public Direction getBulbFacing(BlockState state) {
        return state.getValue(DisplayLinkBlock.FACING);
    }

}
