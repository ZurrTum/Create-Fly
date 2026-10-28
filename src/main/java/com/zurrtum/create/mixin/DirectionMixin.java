package com.zurrtum.create.mixin;

import com.mojang.math.Axis;
import com.zurrtum.create.infrastructure.pose.AxisHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Direction.class)
public class DirectionMixin implements AxisHolder {
    @Unique
    private final Axis mathAxis = switch (((Direction) (Object) this).ordinal()) {
        case 0 -> Axis.YN;
        case 1 -> Axis.YP;
        case 2 -> Axis.ZN;
        case 3 -> Axis.ZP;
        case 4 -> Axis.XN;
        case 5 -> Axis.XP;
        default -> throw new UnsupportedOperationException();
    };

    @Override
    public @NonNull Axis create$getMathAxis() {
        return mathAxis;
    }
}
