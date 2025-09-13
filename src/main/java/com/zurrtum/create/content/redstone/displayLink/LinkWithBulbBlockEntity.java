package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        if (isVirtual() || world.isClient())
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
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket && sendPulse) {
            sendPulse = false;
            view.putBoolean("Pulse", true);
        }
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (clientPacket && view.getBoolean("Pulse", false))
            pulse();
    }

    public Vec3d getBulbOffset(BlockState state) {
        return Vec3d.ZERO;
    }

    public Direction getBulbFacing(BlockState state) {
        return state.get(DisplayLinkBlock.FACING);
    }

}
