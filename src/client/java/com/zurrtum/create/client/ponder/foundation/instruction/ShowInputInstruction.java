package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.InputWindowElement;

public class ShowInputInstruction extends FadeInOutInstruction {

    private final InputWindowElement element;

    public ShowInputInstruction(InputWindowElement element, int ticks) {
        super(ticks);
        this.element = element;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
    }

    @Override
    protected void show(PonderScene scene) {
        scene.addElement(element);
        element.setVisible(true);
    }

    @Override
    protected void hide(PonderScene scene) {
        element.setVisible(false);
    }

    @Override
    protected void applyFade(PonderScene scene, float fade) {
        element.setFade(fade);
    }

}