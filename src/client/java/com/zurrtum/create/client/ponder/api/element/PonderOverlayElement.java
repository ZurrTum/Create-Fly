package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;

public interface PonderOverlayElement extends PonderElement {

    void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks);

}
