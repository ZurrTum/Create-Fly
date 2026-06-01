package com.zurrtum.create.client.foundation.gui.menu;

import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public interface ExclusionZoneSync {
    void set(List<Rect2i> extraAreas);

    void clear();
}
