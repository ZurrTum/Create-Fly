package com.zurrtum.create.mixin;

import com.mojang.math.Axis;
import com.zurrtum.create.infrastructure.pose.AxisHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Direction.Axis.class)
public class DirectionAxisMixin implements AxisHolder {
    @Unique
    private final Axis mathAxis = switch (((Direction.Axis) (Object) this).ordinal()) {
        case 0 -> Axis.XP;
        case 1 -> Axis.YP;
        case 2 -> Axis.ZP;
        default -> throw new UnsupportedOperationException();
    };

    @Override
    public @NonNull Axis create$getMathAxis() {
        return mathAxis;
    }
}
