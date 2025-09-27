package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.PonderSceneElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class AnimateElementInstruction<T extends PonderSceneElement> extends TickingInstruction {

    protected Vec3d deltaPerTick;
    protected Vec3d totalDelta;
    protected Vec3d target;
    protected ElementLink<T> link;
    protected T element;

    private final BiConsumer<T, Vec3d> setter;
    private final Function<T, Vec3d> getter;

    protected AnimateElementInstruction(ElementLink<T> link, Vec3d totalDelta, int ticks, BiConsumer<T, Vec3d> setter, Function<T, Vec3d> getter) {
        super(false, ticks);
        this.link = link;
        this.setter = setter;
        this.getter = getter;
        this.deltaPerTick = totalDelta.multiply(1d / ticks);
        this.totalDelta = totalDelta;
        this.target = totalDelta;
    }

    @Override
    protected final void firstTick(PonderScene scene) {
        super.firstTick(scene);
        element = scene.resolve(link);
        if (element == null)
            return;
        target = getter.apply(element).add(totalDelta);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (element == null)
            return;
        if (remainingTicks == 0) {
            setter.accept(element, target);
            setter.accept(element, target);
            return;
        }
        setter.accept(element, getter.apply(element).add(deltaPerTick));
    }

}