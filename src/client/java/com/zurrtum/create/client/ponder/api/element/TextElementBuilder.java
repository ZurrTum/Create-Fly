package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public interface TextElementBuilder {

    TextElementBuilder colored(PonderPalette color);

    TextElementBuilder pointAt(Vec3 vec);

    TextElementBuilder independent(int y);

    default TextElementBuilder independent() {
        return independent(0);
    }

    TextElementBuilder text(String defaultText);

    TextElementBuilder text(String defaultText, Object... params);


    TextElementBuilder sharedText(ResourceLocation key);

    TextElementBuilder sharedText(ResourceLocation key, Object... params);

    TextElementBuilder sharedText(String key);

    TextElementBuilder sharedText(String key, Object... params);

    TextElementBuilder placeNearTarget();

    TextElementBuilder attachKeyFrame();
}