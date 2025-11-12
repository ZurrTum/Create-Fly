package com.zurrtum.create.client.catnip.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class StitchedSprite {
    private static final Map<Identifier, List<StitchedSprite>> ALL = new HashMap<>();

    protected final Identifier atlasLocation;
    protected final Identifier location;
    protected TextureAtlasSprite sprite;

    public StitchedSprite(Identifier atlas, Identifier location) {
        atlasLocation = atlas;
        this.location = location;
        ALL.computeIfAbsent(atlasLocation, $ -> new ArrayList<>()).add(this);
    }

    @SuppressWarnings("deprecation")
    public StitchedSprite(Identifier location) {
        this(TextureAtlas.LOCATION_BLOCKS, location);
    }

    public static void onTextureStitchPost(TextureAtlas atlas) {
        Identifier atlasLocation = atlas.location();
        List<StitchedSprite> sprites = ALL.get(atlasLocation);
        if (sprites != null) {
            for (StitchedSprite sprite : sprites) {
                sprite.loadSprite(atlas);
            }
        }
    }

    protected void loadSprite(TextureAtlas atlas) {
        sprite = atlas.getSprite(location);
    }

    public Identifier getAtlasLocation() {
        return atlasLocation;
    }

    public Identifier getLocation() {
        return location;
    }

    public TextureAtlasSprite get() {
        return sprite;
    }
}
