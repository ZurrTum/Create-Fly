package com.zurrtum.create.client.compat.sodium;

import com.zurrtum.create.client.Create;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
public class SodiumCompat {
    private static final boolean DISABLE = !FabricLoader.getInstance().isModLoaded("sodium");
    public static final Identifier SAW_TEXTURE = Create.asResource("block/saw_reversed");
    public static final Identifier FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");

    public static void markSpriteActive(MinecraftClient mc) {
        if (DISABLE) {
            return;
        }
        Function<Identifier, Sprite> atlas = mc.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        Sprite sawSprite = atlas.apply(SAW_TEXTURE);
        SpriteUtil.INSTANCE.markSpriteActive(sawSprite);

        Sprite factoryPanelSprite = atlas.apply(FACTORY_PANEL_TEXTURE);
        SpriteUtil.INSTANCE.markSpriteActive(factoryPanelSprite);
    }
}