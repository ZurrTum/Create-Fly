package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;

public abstract class WorldModifyInstruction extends PonderInstruction {

    private final Selection selection;

    public WorldModifyInstruction(Selection selection) {
        this.selection = selection;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        runModification(selection, scene);
        if (needsRedraw())
            scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
    }

    protected abstract void runModification(Selection selection, PonderScene scene);

    protected abstract boolean needsRedraw();

}