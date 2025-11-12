package com.zurrtum.create.client.content.kinetics.chainConveyor;


import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;

import java.lang.ref.WeakReference;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class ChainConveyorPackagePhysicsData {
    public Vec3 targetPos;
    public Vec3 prevTargetPos;
    public Vec3 prevPos;
    public Vec3 pos;

    public Vec3 motion;
    public int lastTick;
    public float yaw;
    public float prevYaw;
    public boolean flipped;
    public Identifier modelKey;

    public WeakReference<ChainConveyorBlockEntity> beReference;

    public ChainConveyorPackagePhysicsData() {
        this.targetPos = null;
        this.prevTargetPos = null;
        this.pos = null;
        this.prevPos = null;

        this.motion = Vec3.ZERO;
        this.lastTick = AnimationTickHolder.getTicks();
    }

    public boolean shouldTick() {
        if (lastTick == AnimationTickHolder.getTicks())
            return false;
        lastTick = AnimationTickHolder.getTicks();
        return true;
    }

    public void setBE(ChainConveyorBlockEntity ccbe) {
        if (beReference == null || beReference.get() != ccbe)
            beReference = new WeakReference<>(ccbe);
    }
}
