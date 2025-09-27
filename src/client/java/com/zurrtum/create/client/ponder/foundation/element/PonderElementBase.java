package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.client.ponder.api.element.PonderElement;

public abstract class PonderElementBase implements PonderElement {

    boolean visible = true;

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}