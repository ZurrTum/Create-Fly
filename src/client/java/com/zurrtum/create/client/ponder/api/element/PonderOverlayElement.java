package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.DrawContext;

public interface PonderOverlayElement extends PonderElement {

    void render(PonderScene scene, PonderUI screen, DrawContext graphics, float partialTicks);

}
