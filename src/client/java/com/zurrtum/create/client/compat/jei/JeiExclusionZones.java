package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public class JeiExclusionZones implements IGuiContainerHandler<AbstractSimiContainerScreen<?>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(AbstractSimiContainerScreen<?> containerScreen) {
        return containerScreen.getExtraAreas();
    }
}
