package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.math.Box;

public class ChaseAABBInstruction extends TickingInstruction {

    private final Box bb;
    private final Object slot;
    private final PonderPalette color;

    public ChaseAABBInstruction(PonderPalette color, Object slot, Box bb, int ticks) {
        super(false, ticks);
        this.color = color;
        this.slot = slot;
        this.bb = bb;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        scene.getOutliner().chaseAABB(slot, bb).lineWidth(1 / 16f).colored(color.getColor());
    }

}