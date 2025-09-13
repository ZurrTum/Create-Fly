package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StitchedSprite {
    private static final Map<Identifier, List<StitchedSprite>> ALL = new HashMap<>();

    protected final Identifier atlasLocation;
    protected final Identifier location;
    protected Sprite sprite;

    public StitchedSprite(Identifier atlas, Identifier location) {
        atlasLocation = atlas;
        this.location = location;
        ALL.computeIfAbsent(atlasLocation, $ -> new ArrayList<>()).add(this);
    }

    @SuppressWarnings("deprecation")
    public StitchedSprite(Identifier location) {
        this(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, location);
    }

    public static void onTextureStitchPost(SpriteAtlasTexture atlas) {
        Identifier atlasLocation = atlas.getId();
        List<StitchedSprite> sprites = ALL.get(atlasLocation);
        if (sprites != null) {
            for (StitchedSprite sprite : sprites) {
                sprite.loadSprite(atlas);
            }
        }
    }

    protected void loadSprite(SpriteAtlasTexture atlas) {
        sprite = atlas.getSprite(location);
    }

    public Identifier getAtlasLocation() {
        return atlasLocation;
    }

    public Identifier getLocation() {
        return location;
    }

    public Sprite get() {
        return sprite;
    }
}
