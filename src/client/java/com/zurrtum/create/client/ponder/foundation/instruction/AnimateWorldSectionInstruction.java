package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnimateWorldSectionInstruction extends AnimateElementInstruction<WorldSectionElement> {

    public static AnimateWorldSectionInstruction rotate(ElementLink<WorldSectionElement> link, Vec3d rotation, int ticks) {
        return new AnimateWorldSectionInstruction(
            link,
            rotation,
            ticks,
            (wse, v) -> wse.setAnimatedRotation(v, ticks == 0),
            WorldSectionElement::getAnimatedRotation
        );
    }

    public static AnimateWorldSectionInstruction move(ElementLink<WorldSectionElement> link, Vec3d offset, int ticks) {
        return new AnimateWorldSectionInstruction(
            link,
            offset,
            ticks,
            (wse, v) -> wse.setAnimatedOffset(v, ticks == 0),
            WorldSectionElement::getAnimatedOffset
        );
    }

    protected AnimateWorldSectionInstruction(
        ElementLink<WorldSectionElement> link,
        Vec3d totalDelta,
        int ticks,
        BiConsumer<WorldSectionElement, Vec3d> setter,
        Function<WorldSectionElement, Vec3d> getter
    ) {
        super(link, totalDelta, ticks, setter, getter);
    }

}