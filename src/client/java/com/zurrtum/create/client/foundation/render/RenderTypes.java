package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class RenderTypes extends RenderStateShard {
    private static final RenderType ENTITY_SOLID_BLOCK_MIPPED = RenderType.create(
        createLayerName("entity_solid_block_mipped"),
        256,
        true,
        false,
        RenderPipelines.ENTITY_SOLID,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET_MIPPED).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final RenderType ENTITY_CUTOUT_BLOCK_MIPPED = RenderType.create(
        createLayerName("entity_cutout_block_mipped"),
        256,
        true,
        false,
        RenderPipelines.ENTITY_CUTOUT,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET_MIPPED).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final RenderType ENTITY_TRANSLUCENT_BLOCK_MIPPED = RenderType.create(
        createLayerName("entity_translucent_block_mipped"),
        256,
        true,
        true,
        RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET_MIPPED).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final RenderType TRANSLUCENT = RenderType.create(
        createLayerName("translucent"),
        256,
        true,
        true,
        RenderPipelines.TRANSLUCENT,
        RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true)
    );

    private static final RenderType ADDITIVE = RenderType.create(
        createLayerName("additive"),
        256,
        true,
        true,
        AllRenderPipelines.ADDITIVE,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final RenderType ADDITIVE2 = RenderType.create(
        createLayerName("additive2"),
        256,
        true,
        true,
        AllRenderPipelines.ADDITIVE2,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final RenderType ITEM_GLOWING_SOLID = RenderType.create(
        createLayerName("item_glowing_solid"),
        256,
        true,
        false,
        AllRenderPipelines.GLOWING,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final RenderType ITEM_GLOWING_TRANSLUCENT = RenderType.create(
        createLayerName("item_glowing_translucent"),
        256,
        true,
        true,
        AllRenderPipelines.GLOWING_TRANSLUCENT,
        RenderType.CompositeState.builder().setTextureState(BLOCK_SHEET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true)
    );

    private static final Function<Identifier, RenderType> CHAIN = Util.memoize((p_234330_) -> RenderType.create(
        "chain_conveyor_chain",
        256,
        false,
        true,
        RenderPipelines.CUTOUT_MIPPED,
        RenderType.CompositeState.builder().setTextureState(new TextureStateShard(p_234330_, true)).setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY).createCompositeState(false)
    ));

    public static RenderType entitySolidBlockMipped() {
        return ENTITY_SOLID_BLOCK_MIPPED;
    }

    public static RenderType entityCutoutBlockMipped() {
        return ENTITY_CUTOUT_BLOCK_MIPPED;
    }

    public static RenderType entityTranslucentBlockMipped() {
        return ENTITY_TRANSLUCENT_BLOCK_MIPPED;
    }

    public static RenderType translucent() {
        return TRANSLUCENT;
    }

    public static RenderType additive() {
        return ADDITIVE;
    }

    public static RenderType additive2() {
        return ADDITIVE2;
    }

    public static BiFunction<Identifier, Boolean, RenderType> TRAIN_MAP = Util.memoize(RenderTypes::getTrainMap);

    private static RenderType getTrainMap(Identifier locationIn, boolean linearFiltering) {
        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
            .setTextureState(new FilterTexture(locationIn, linearFiltering, false)).setLightmapState(LIGHTMAP).createCompositeState(false);
        return RenderType.create("create_train_map", 256, false, true, RenderPipelines.TEXT, rendertype$state);
    }

    public static RenderType itemGlowingSolid() {
        return ITEM_GLOWING_SOLID;
    }

    public static RenderType itemGlowingTranslucent() {
        return ITEM_GLOWING_TRANSLUCENT;
    }

    public static RenderType chain(Identifier pLocation) {
        return CHAIN.apply(pLocation);
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }

    // Mmm gimme those protected fields
    private RenderTypes() {
        super(null, null, null);
    }

    private static class FilterTexture extends RenderStateShard.EmptyTextureStateShard {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<Identifier> id;
        private final boolean mipmap;

        public FilterTexture(Identifier id, boolean bilinear, boolean mipmap) {
            super(
                () -> {
                    TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                    AbstractTexture abstractTexture = textureManager.getTexture(id);
                    abstractTexture.setFilter(bilinear, mipmap);
                    RenderSystem.setShaderTexture(0, abstractTexture.getTextureView());
                }, () -> {
                }
            );
            this.id = Optional.of(id);
            this.mipmap = mipmap;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.id + "(mipmap=" + this.mipmap + ")]";
        }

        @Override
        public Optional<Identifier> cutoutTexture() {
            return this.id;
        }
    }
}
