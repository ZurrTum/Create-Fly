package com.zurrtum.create.client.compat.rei;

import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;

import java.util.Collection;

public class ReiExclusionZones implements ExclusionZonesProvider<AbstractSimiContainerScreen<?>> {
    @Override
    public Collection<Rectangle> provide(AbstractSimiContainerScreen<?> containerScreen) {
        return containerScreen.getExtraAreas().stream()
            .map(rect2i -> new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight())).toList();
    }
}