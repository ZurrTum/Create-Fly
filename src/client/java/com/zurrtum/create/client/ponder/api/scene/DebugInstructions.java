package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;

import java.util.function.Consumer;

public interface DebugInstructions {
    void debugSchematic();

    void addInstructionInstance(PonderInstruction instruction);

    void enqueueCallback(Consumer<PonderScene> callback);
}
