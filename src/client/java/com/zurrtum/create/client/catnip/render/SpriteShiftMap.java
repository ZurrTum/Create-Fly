package com.zurrtum.create.client.catnip.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.resources.Identifier;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SpriteShiftMap {
    private final Map<Identifier, ShiftStitchedSprite> map = new Object2ObjectOpenHashMap<>();
    private final Map<TextureAtlasSprite, ShiftStitchedSprite> shifts;

    private SpriteShiftMap(Consumer<SpriteShiftMap> consumer) {
        consumer.accept(this);
        shifts = new IdentityHashMap<>(map.size());
    }

    public static SpriteShiftMap create(Consumer<SpriteShiftMap> consumer) {
        return new SpriteShiftMap(consumer);
    }

    public void add(Identifier originalLocation, Identifier targetLocation) {
        map.put(originalLocation, new TargetSprite(new OriginalSprite(originalLocation), targetLocation));
    }

    public void add(Identifier atlas, Identifier originalLocation, Identifier targetLocation) {
        map.put(originalLocation, new TargetSprite(atlas, new OriginalSprite(atlas, originalLocation), targetLocation));
    }

    public ShiftStitchedSprite get(Identifier id) {
        return map.get(id);
    }

    public BakedQuad replaceQuad(BakedQuad quad) {
        MaterialInfo info = quad.materialInfo();
        ShiftStitchedSprite sprite = shifts.get(info.sprite());
        if (sprite != null) {
            return sprite.replaceQuad(quad, info);
        }
        return quad;
    }

    public BakedQuad replaceQuad(BakedQuad quad, MaterialInfo info, TextureAtlasSprite infoSprite) {
        ShiftStitchedSprite sprite = shifts.get(infoSprite);
        if (sprite != null) {
            return sprite.replaceQuad(quad, info);
        }
        return quad;
    }

    private class OriginalSprite extends StitchedSprite {
        public OriginalSprite(Identifier atlas, Identifier location) {
            super(atlas, location);
        }

        public OriginalSprite(Identifier location) {
            super(location);
        }

        @Override
        protected void loadSprite(TextureAtlas atlas) {
            if (sprite != null) {
                shifts.remove(sprite);
            }
            super.loadSprite(atlas);
        }
    }

    private class TargetSprite extends ShiftStitchedSprite {
        public TargetSprite(Identifier atlas, OriginalSprite originalLocation, Identifier targetLocation) {
            super(atlas, originalLocation, targetLocation);
        }

        public TargetSprite(OriginalSprite originalLocation, Identifier targetLocation) {
            super(originalLocation, targetLocation);
        }

        @Override
        protected void loadSprite(TextureAtlas atlas) {
            super.loadSprite(atlas);
            shifts.put(original.get(), this);
        }
    }
}
