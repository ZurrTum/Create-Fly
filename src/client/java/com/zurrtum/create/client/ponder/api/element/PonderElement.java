package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.foundation.PonderScene;

public interface PonderElement {
    default void whileSkipping(PonderScene scene) {
    }

    default void tick(PonderScene scene) {
    }

    default void reset(PonderScene scene) {
    }

    boolean isVisible();

    void setVisible(boolean visible);
}