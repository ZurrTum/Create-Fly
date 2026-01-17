package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

public class RenderTypes extends RenderPhase {
    private static final RenderLayer ENTITY_SOLID_BLOCK_MIPPED = RenderLayer.of(
        createLayerName("entity_solid_block_mipped"),
        256,
        true,
        false,
        RenderPipelines.ENTITY_SOLID,
        RenderLayer.MultiPhaseParameters.builder().texture(MIPMAP_BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
            .build(true)
    );

    private static final RenderLayer ENTITY_CUTOUT_BLOCK_MIPPED = RenderLayer.of(
        createLayerName("entity_cutout_block_mipped"),
        256,
        true,
        false,
        RenderPipelines.ENTITY_CUTOUT,
        RenderLayer.MultiPhaseParameters.builder().texture(MIPMAP_BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
            .build(true)
    );

    private static final RenderLayer ENTITY_TRANSLUCENT_BLOCK_MIPPED = RenderLayer.of(
        createLayerName("entity_translucent_block_mipped"),
        256,
        true,
        true,
        RenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL,
        RenderLayer.MultiPhaseParameters.builder().texture(MIPMAP_BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
            .build(true)
    );

    private static final RenderLayer TRANSLUCENT = RenderLayer.of(
        createLayerName("translucent"),
        256,
        true,
        true,
        RenderPipelines.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK,
        RenderLayer.MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).build(true)
    );

    private static final RenderLayer ADDITIVE = RenderLayer.of(
        createLayerName("additive"),
        256,
        true,
        true,
        AllRenderPipelines.ADDITIVE,
        RenderLayer.MultiPhaseParameters.builder().texture(BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true)
    );

    private static final RenderLayer ADDITIVE2 = RenderLayer.of(
        createLayerName("additive2"),
        256,
        true,
        true,
        AllRenderPipelines.ADDITIVE2,
        RenderLayer.MultiPhaseParameters.builder().texture(BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true)
    );

    private static final RenderLayer ITEM_GLOWING_SOLID = RenderLayer.of(
        createLayerName("item_glowing_solid"),
        256,
        true,
        false,
        AllRenderPipelines.GLOWING,
        RenderLayer.MultiPhaseParameters.builder().texture(BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true)
    );

    private static final RenderLayer ITEM_GLOWING_TRANSLUCENT = RenderLayer.of(
        createLayerName("item_glowing_translucent"),
        256,
        true,
        true,
        AllRenderPipelines.GLOWING_TRANSLUCENT,
        RenderLayer.MultiPhaseParameters.builder().texture(BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(true)
    );

    private static final Function<Identifier, RenderLayer> CHAIN = Util.memoize((p_234330_) -> RenderLayer.of(
        "chain_conveyor_chain",
        256,
        false,
        true,
        RenderPipelines.CUTOUT_MIPPED,
        RenderLayer.MultiPhaseParameters.builder().texture(new Texture(p_234330_, true)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
            .build(false)
    ));

    public static RenderLayer entitySolidBlockMipped() {
        return ENTITY_SOLID_BLOCK_MIPPED;
    }

    public static RenderLayer entityCutoutBlockMipped() {
        return ENTITY_CUTOUT_BLOCK_MIPPED;
    }

    public static RenderLayer entityTranslucentBlockMipped() {
        return ENTITY_TRANSLUCENT_BLOCK_MIPPED;
    }

    public static RenderLayer translucent() {
        return TRANSLUCENT;
    }

    public static RenderLayer additive() {
        return ADDITIVE;
    }

    public static RenderLayer additive2() {
        return ADDITIVE2;
    }

    public static BiFunction<Identifier, Boolean, RenderLayer> TRAIN_MAP = Util.memoize(RenderTypes::getTrainMap);

    private static RenderLayer getTrainMap(Identifier locationIn, boolean linearFiltering) {
        RenderLayer.MultiPhaseParameters rendertype$state = RenderLayer.MultiPhaseParameters.builder()
            .texture(new FilterTexture(locationIn, linearFiltering, false)).lightmap(ENABLE_LIGHTMAP).build(false);
        return RenderLayer.of("create_train_map", 256, false, true, RenderPipelines.RENDERTYPE_TEXT, rendertype$state);
    }

    public static RenderLayer itemGlowingSolid() {
        return ITEM_GLOWING_SOLID;
    }

    public static RenderLayer itemGlowingTranslucent() {
        return ITEM_GLOWING_TRANSLUCENT;
    }

    public static RenderLayer chain(Identifier pLocation) {
        return CHAIN.apply(pLocation);
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }

    // Mmm gimme those protected fields
    private RenderTypes() {
        super(null, null, null);
    }

    private static class FilterTexture extends RenderPhase.TextureBase {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<Identifier> id;
        private final boolean mipmap;

        public FilterTexture(Identifier id, boolean bilinear, boolean mipmap) {
            super(
                () -> {
                    TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                    AbstractTexture abstractTexture = textureManager.getTexture(id);
                    abstractTexture.setFilter(bilinear, mipmap);
                    RenderSystem.setShaderTexture(0, abstractTexture.getGlTextureView());
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
        public Optional<Identifier> getId() {
            return this.id;
        }
    }
}
