package com.zurrtum.create.client.compat.eiv;

import com.zurrtum.create.client.foundation.gui.menu.ExclusionZoneSync;
import de.crafty.eiv.common.overlay.BlockingGuiComponent;
import de.crafty.eiv.common.overlay.OverlayManager;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public record EivExclusionZoneSync(OverlayManager manager) implements ExclusionZoneSync {
    @Override
    public void set(List<Rect2i> extraAreas) {
        List<BlockingGuiComponent> list = manager.allGuiBlockings();
        list.removeIf(comp -> comp.id().getNamespace().equals(MOD_ID));
        for (int i = 0, size = extraAreas.size(); i < size; i++) {
            Rect2i rect = extraAreas.get(i);
            list.add(new BlockingGuiComponent(
                Identifier.fromNamespaceAndPath(MOD_ID, "exclusion_zone_" + i),
                rect.getX(),
                rect.getY(),
                rect.getWidth(),
                rect.getHeight()
            ));
        }
        manager.updateOverlaysAndWidgets();
    }

    @Override
    public void clear() {
        manager.allGuiBlockings().removeIf(comp -> comp.id().getNamespace().equals(MOD_ID));
    }
}
