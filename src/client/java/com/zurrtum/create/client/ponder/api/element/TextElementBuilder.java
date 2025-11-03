package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public interface TextElementBuilder {

    TextElementBuilder colored(PonderPalette color);

    TextElementBuilder pointAt(Vec3d vec);

    TextElementBuilder independent(int y);

    default TextElementBuilder independent() {
        return independent(0);
    }

    TextElementBuilder text(String defaultText);

    TextElementBuilder text(String defaultText, Object... params);


    TextElementBuilder sharedText(Identifier key);

    TextElementBuilder sharedText(Identifier key, Object... params);

    TextElementBuilder sharedText(String key);

    TextElementBuilder sharedText(String key, Object... params);

    TextElementBuilder placeNearTarget();

    TextElementBuilder attachKeyFrame();
}