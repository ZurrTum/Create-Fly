package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.MinecartElement;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnimateMinecartInstruction extends AnimateElementInstruction<MinecartElement> {

    public static AnimateMinecartInstruction rotate(ElementLink<MinecartElement> link, float rotation, int ticks) {
        return new AnimateMinecartInstruction(
            link,
            new Vec3d(0, rotation, 0),
            ticks,
            (wse, v) -> wse.setRotation((float) v.y, ticks == 0),
            MinecartElement::getRotation
        );
    }

    public static AnimateMinecartInstruction move(ElementLink<MinecartElement> link, Vec3d offset, int ticks) {
        return new AnimateMinecartInstruction(
            link,
            offset,
            ticks,
            (wse, v) -> wse.setPositionOffset(v, ticks == 0),
            MinecartElement::getPositionOffset
        );
    }

    protected AnimateMinecartInstruction(
        ElementLink<MinecartElement> link,
        Vec3d totalDelta,
        int ticks,
        BiConsumer<MinecartElement, Vec3d> setter,
        Function<MinecartElement, Vec3d> getter
    ) {
        super(link, totalDelta, ticks, setter, getter);
    }

}