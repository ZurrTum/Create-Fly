package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;

public interface AnimatedOverlayElement extends PonderOverlayElement {

    void setFade(float fade);

    float getFade(float partialTicks);

    @Override
    default void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks) {
        render(scene, screen, graphics, partialTicks, getFade(partialTicks));
    }

    void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade);
}
