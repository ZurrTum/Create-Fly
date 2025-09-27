package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class HighlightValueBoxInstruction extends TickingInstruction {

    private final Vec3d vec;
    private final Vec3d expands;

    public HighlightValueBoxInstruction(Vec3d vec, Vec3d expands, int duration) {
        super(false, duration);
        this.vec = vec;
        this.expands = expands;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        Box point = new Box(vec, vec);
        Box expanded = point.expand(expands.x, expands.y, expands.z);
        scene.getOutliner().chaseAABB(vec, remainingTicks + 1 >= totalTicks ? point : expanded).lineWidth(1 / 15f)
            .colored(PonderPalette.WHITE.getColor());
    }

}