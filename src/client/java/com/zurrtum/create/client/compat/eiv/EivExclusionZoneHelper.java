package com.zurrtum.create.client.compat.eiv;

import de.crafty.eiv.common.overlay.BlockingGuiComponent;
import de.crafty.eiv.common.overlay.OverlayManager;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class EivExclusionZoneHelper {
    private static Consumer<List<Rect2i>> setExclusionZone;
    private static Runnable removeExclusionZone;

    public static void setExclusionZone(List<Rect2i> extraAreas) {
        if (setExclusionZone != null) {
            setExclusionZone.accept(extraAreas);
        }
    }

    public static void removeExclusionZone() {
        if (removeExclusionZone != null) {
            removeExclusionZone.run();
        }
    }

    public static void setRuntime(OverlayManager manager) {
        removeExclusionZone = () -> manager.allGuiBlockings().removeIf(comp -> comp.id().getNamespace().equals(MOD_ID));
        setExclusionZone = extraAreas -> {
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
        };
    }
}
