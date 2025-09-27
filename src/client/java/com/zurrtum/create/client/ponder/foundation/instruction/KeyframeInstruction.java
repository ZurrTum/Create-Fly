package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.foundation.PonderScene;

public class KeyframeInstruction extends PonderInstruction {

    public static final KeyframeInstruction IMMEDIATE = new KeyframeInstruction(false);
    public static final KeyframeInstruction DELAYED = new KeyframeInstruction(true);

    private final boolean delayed;

    private KeyframeInstruction(boolean delayed) {
        this.delayed = delayed;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
    }

    @Override
    public void onScheduled(PonderScene scene) {
        scene.markKeyframe(delayed ? 6 : 0);
    }
}