package com.zurrtum.create.client.compat.jei;

import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.util.math.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JeiExclusionZones implements IGuiContainerHandler<AbstractSimiContainerScreen<?>> {
    @Override
    @NotNull
    public List<Rect2i> getGuiExtraAreas(AbstractSimiContainerScreen<?> containerScreen) {
        return containerScreen.getExtraAreas();
    }
}
