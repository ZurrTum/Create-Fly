package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnimateParrotInstruction extends AnimateElementInstruction<ParrotElement> {

    public static AnimateParrotInstruction rotate(ElementLink<ParrotElement> link, Vec3d rotation, int ticks) {
        return new AnimateParrotInstruction(link, rotation, ticks, (wse, v) -> wse.setRotation(v, ticks == 0), ParrotElement::getRotation);
    }

    public static AnimateParrotInstruction move(ElementLink<ParrotElement> link, Vec3d offset, int ticks) {
        return new AnimateParrotInstruction(link, offset, ticks, (wse, v) -> wse.setPositionOffset(v, ticks == 0), ParrotElement::getPositionOffset);
    }

    protected AnimateParrotInstruction(
        ElementLink<ParrotElement> link,
        Vec3d totalDelta,
        int ticks,
        BiConsumer<ParrotElement, Vec3d> setter,
        Function<ParrotElement, Vec3d> getter
    ) {
        super(link, totalDelta, ticks, setter, getter);
    }

}