package com.zurrtum.create.client.content.kinetics.chainConveyor;


import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.lang.ref.WeakReference;

public class ChainConveyorPackagePhysicsData {
    public Vec3d targetPos;
    public Vec3d prevTargetPos;
    public Vec3d prevPos;
    public Vec3d pos;

    public Vec3d motion;
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

        this.motion = Vec3d.ZERO;
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
