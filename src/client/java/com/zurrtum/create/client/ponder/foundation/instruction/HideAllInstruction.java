package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.AnimatedOverlayElementBase;
import com.zurrtum.create.client.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class HideAllInstruction extends TickingInstruction {

    private final Direction fadeOutTo;

    public HideAllInstruction(int fadeOutTicks, Direction fadeOutTo) {
        super(false, fadeOutTicks);
        this.fadeOutTo = fadeOutTo;
    }

    @Override
    protected void firstTick(PonderScene scene) {
        super.firstTick(scene);
        scene.getElements().forEach(element -> {
            if (element instanceof AnimatedSceneElementBase animatedSceneElement) {
                animatedSceneElement.setFade(1);
                animatedSceneElement.setFadeVec(fadeOutTo == null ? null : Vec3d.of(fadeOutTo.getVector()).multiply(.5f));
            } else if (element instanceof AnimatedOverlayElementBase animatedSceneElement) {
                animatedSceneElement.setFade(1);
            } else
                element.setVisible(false);
        });
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        float fade = (remainingTicks / (float) totalTicks);

        scene.forEach(
            AnimatedSceneElementBase.class, ase -> {
                ase.setFade(fade * fade);
                if (remainingTicks == 0)
                    ase.setFade(0);
            }
        );

        scene.forEach(
            AnimatedOverlayElementBase.class, aoe -> {
                aoe.setFade(fade * fade);
                if (remainingTicks == 0)
                    aoe.setFade(0);
            }
        );
    }

}
