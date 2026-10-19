package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderRenderTypes {
    @SuppressWarnings("deprecation")
    private static final RenderType ENTITY_BLOCK_SOLID = CustomRenderType.markPriority(RenderType.create(
        createLayerName("entity_block_solid"),
        RenderSetup.builder(PonderRenderPipelines.ENTITY_BLOCK_SOLID)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    ));
    @SuppressWarnings("deprecation")
    private static final RenderType ENTITY_BLOCK_CUTOUT = RenderType.create(
        createLayerName("entity_block_cutout"),
        RenderSetup.builder(PonderRenderPipelines.ENTITY_BLOCK_CUTOUT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType ENTITY_BLOCK_TRANSLUCENT = RenderType.create(
        createLayerName("entity_block_translucent"),
        RenderSetup.builder(PonderRenderPipelines.ENTITY_BLOCK_TRANSLUCENT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOitPipelines(PonderRenderPipelines.OIT_ENTITY_BLOCK_TRANSLUCENT).useLightmap().affectsCrumbling()
            .sortOnUpload().setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType ENTITY_BLOCK_LIGHT_SOLID = CustomRenderType.markPriority(RenderType.create(
        createLayerName("entity_block_light_solid"),
        RenderSetup.builder(PonderRenderPipelines.ENTITY_BLOCK_LIGHT_SOLID)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    ));
    @SuppressWarnings("deprecation")
    private static final RenderType ENTITY_BLOCK_LIGHT_CUTOUT = RenderType.create(
        createLayerName("entity_block_light_cutout"),
        RenderSetup.builder(PonderRenderPipelines.ENTITY_BLOCK_LIGHT_CUTOUT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType ENTITY_BLOCK_LIGHT_TRANSLUCENT = RenderType.create(
        createLayerName("entity_block_light_translucent"),
        RenderSetup.builder(PonderRenderPipelines.ENTITY_BLOCK_LIGHT_TRANSLUCENT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOitPipelines(PonderRenderPipelines.OIT_ENTITY_BLOCK_LIGHT_TRANSLUCENT).useLightmap().affectsCrumbling()
            .sortOnUpload().setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType NETHER_ENTITY_BLOCK_SOLID = CustomRenderType.markPriority(RenderType.create(
        createLayerName("nether_entity_block_solid"),
        RenderSetup.builder(PonderRenderPipelines.NETHER_ENTITY_BLOCK_SOLID)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    ));
    @SuppressWarnings("deprecation")
    private static final RenderType NETHER_ENTITY_BLOCK_CUTOUT = RenderType.create(
        createLayerName("nether_entity_block_cutout"),
        RenderSetup.builder(PonderRenderPipelines.NETHER_ENTITY_BLOCK_CUTOUT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType NETHER_ENTITY_BLOCK_TRANSLUCENT = RenderType.create(
        createLayerName("nether_entity_block_translucent"),
        RenderSetup.builder(PonderRenderPipelines.NETHER_ENTITY_BLOCK_TRANSLUCENT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOitPipelines(PonderRenderPipelines.OIT_NETHER_ENTITY_BLOCK_TRANSLUCENT).useLightmap().affectsCrumbling()
            .sortOnUpload().setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType NETHER_ENTITY_BLOCK_LIGHT_SOLID = CustomRenderType.markPriority(RenderType.create(
        createLayerName("nether_entity_block_light_solid"),
        RenderSetup.builder(PonderRenderPipelines.NETHER_ENTITY_BLOCK_LIGHT_SOLID)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    ));
    @SuppressWarnings("deprecation")
    private static final RenderType NETHER_ENTITY_BLOCK_LIGHT_CUTOUT = RenderType.create(
        createLayerName("nether_entity_block_light_cutout"),
        RenderSetup.builder(PonderRenderPipelines.NETHER_ENTITY_BLOCK_LIGHT_CUTOUT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    @SuppressWarnings("deprecation")
    private static final RenderType NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT = RenderType.create(
        createLayerName("nether_entity_block_light_translucent"),
        RenderSetup.builder(PonderRenderPipelines.NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOitPipelines(PonderRenderPipelines.OIT_NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT).useLightmap()
            .affectsCrumbling().sortOnUpload().setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    private static final RenderType GUI = RenderType.create(
        createLayerName("gui"),
        RenderSetup.builder(PonderRenderPipelines.GUI).setLayeringTransform(new LayeringTransform(
            "view_offset_z_layering_gui",
            modelViewMatrix -> RenderSystem.getProjectionType().applyLayeringTransform(modelViewMatrix, -6.0F)
        )).createRenderSetup()
    );
    private static final RenderType OUTLINE_SOLID = RenderType.create(
        createLayerName("outline_solid"),
        RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
            .withTexture("Sampler0", PonderSpecialTextures.BLANK.getLocation()).useLightmap().useOverlay()
            .createRenderSetup()
    );
    private static final BiFunction<Identifier, Boolean, RenderType> OUTLINE_TRANSLUCENT = Util.memoize((texture, cull) -> RenderType.create(
        createLayerName("outline_translucent" + (cull ? "_cull" : "")),
        RenderSetup.builder(
                cull ? PonderRenderPipelines.ENTITY_TRANSLUCENT_CULL : PonderRenderPipelines.ENTITY_TRANSLUCENT)
            .setOitPipelines(cull ? RenderPipelines.OIT_ENTITY_CULL : RenderPipelines.OIT_ENTITY).sortOnUpload()
            .withTexture("Sampler0", texture).useLightmap().useOverlay().createRenderSetup()
    ));

    public static RenderType getEntityBlockSolid() {
        return ENTITY_BLOCK_SOLID;
    }

    public static RenderType getEntityBlockCutout() {
        return ENTITY_BLOCK_CUTOUT;
    }

    public static RenderType getEntityBlockTranslucent() {
        return ENTITY_BLOCK_TRANSLUCENT;
    }

    public static RenderType getNetherEntityBlockSolid() {
        return NETHER_ENTITY_BLOCK_SOLID;
    }

    public static RenderType getNetherEntityBlockCutout() {
        return NETHER_ENTITY_BLOCK_CUTOUT;
    }

    public static RenderType getNetherEntityBlockTranslucent() {
        return NETHER_ENTITY_BLOCK_TRANSLUCENT;
    }

    public static RenderType getEntityBlockLightSolid() {
        return ENTITY_BLOCK_LIGHT_SOLID;
    }

    public static RenderType getEntityBlockLightCutout() {
        return ENTITY_BLOCK_LIGHT_CUTOUT;
    }

    public static RenderType getEntityBlockLightTranslucent() {
        return ENTITY_BLOCK_LIGHT_TRANSLUCENT;
    }

    public static RenderType getNetherEntityBlockLightSolid() {
        return NETHER_ENTITY_BLOCK_LIGHT_SOLID;
    }

    public static RenderType getNetherEntityBlockLightCutout() {
        return NETHER_ENTITY_BLOCK_LIGHT_CUTOUT;
    }

    public static RenderType getNetherEntityBlockLightTranslucent() {
        return NETHER_ENTITY_BLOCK_LIGHT_TRANSLUCENT;
    }

    public static RenderType getGui() {
        return GUI;
    }

    public static RenderType outlineSolid() {
        return OUTLINE_SOLID;
    }

    public static RenderType outlineTranslucent(Identifier texture, boolean cull) {
        return OUTLINE_TRANSLUCENT.apply(texture, cull);
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }
}
