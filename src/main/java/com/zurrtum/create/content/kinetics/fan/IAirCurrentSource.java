package com.zurrtum.create.content.kinetics.fan;

import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface IAirCurrentSource {
    @Nullable AirCurrent getAirCurrent();

    @Nullable World getAirCurrentWorld();

    BlockPos getAirCurrentPos();

    float getSpeed();

    Direction getAirflowOriginSide();

    @Nullable Direction getAirFlowDirection();

    default float getMaxDistance() {
        float speed = Math.abs(this.getSpeed());
        CKinetics config = AllConfigs.server().kinetics;
        float distanceFactor = Math.min(speed / config.fanRotationArgmax.get(), 1);
        float pushDistance = MathHelper.lerp(distanceFactor, 3, config.fanPushDistance.get());
        float pullDistance = MathHelper.lerp(distanceFactor, 3f, config.fanPullDistance.get());
        return this.getSpeed() > 0 ? pushDistance : pullDistance;
    }

    boolean isSourceRemoved();
}
