package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.util.math.Vec3d;

public class MovePoiInstruction extends PonderInstruction {

    private final Vec3d poi;

    public MovePoiInstruction(Vec3d poi) {
        this.poi = poi;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        scene.setPointOfInterest(poi);
    }

}