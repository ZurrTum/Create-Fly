package com.zurrtum.create.content.kinetics.fan;

import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CKinetics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface IAirCurrentSource {
    @Nullable AirCurrent getAirCurrent();

    @Nullable Level getAirCurrentWorld();

    BlockPos getAirCurrentPos();

    float getSpeed();

    Direction getAirflowOriginSide();

    @Nullable Direction getAirFlowDirection();

    default float getMaxDistance() {
        float speed = Math.abs(this.getSpeed());
        CKinetics config = AllConfigs.server().kinetics;
        float distanceFactor = Math.min(speed / config.fanRotationArgmax.get(), 1);
        float pushDistance = Mth.lerpInt(distanceFactor, 3, config.fanPushDistance.get());
        float pullDistance = Mth.lerp(distanceFactor, 3f, config.fanPullDistance.get());
        return this.getSpeed() > 0 ? pushDistance : pullDistance;
    }

    boolean isSourceRemoved();
}
