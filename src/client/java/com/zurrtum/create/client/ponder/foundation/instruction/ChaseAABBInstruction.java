package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.world.phys.AABB;

public class ChaseAABBInstruction extends TickingInstruction {

    private final AABB bb;
    private final Object slot;
    private final PonderPalette color;

    public ChaseAABBInstruction(PonderPalette color, Object slot, AABB bb, int ticks) {
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