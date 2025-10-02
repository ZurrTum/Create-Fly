package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;
import static net.minecraft.client.render.RenderPhase.*;

public class PonderRenderTypes {
    private static final Function<Identifier, RenderLayer> GUI_TEXTURED = Util.memoize(texture -> RenderLayer.of(
        createLayerName("gui_textured"),
        786432,
        false,
        false,
        RenderPipelines.GUI_TEXTURED,
        MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false)).build(false)
    ));
    private static final RenderLayer GUI = RenderLayer.of(
        createLayerName("gui"),
        786432,
        false,
        false,
        RenderPipelines.GUI,
        MultiPhaseParameters.builder().build(false)
    );
    private static final RenderLayer.MultiPhase GUI_INVERT = RenderLayer.of(
        "gui_text_highlight",
        1536,
        false,
        false,
        RenderPipelines.GUI_INVERT,
        MultiPhaseParameters.builder().build(false)
    );
    private static final RenderLayer.MultiPhase GUI_TEXT_HIGHLIGHT = RenderLayer.of(
        "gui_text_highlight",
        1536,
        false,
        false,
        RenderPipelines.GUI_TEXT_HIGHLIGHT,
        MultiPhaseParameters.builder().build(false)
    );
    private static final RenderLayer TRANSLUCENT = RenderLayer.of(
        createLayerName("translucent"),
        786432,
        true,
        true,
        RenderPipelines.TRANSLUCENT,
        MultiPhaseParameters.builder().lightmap(ENABLE_LIGHTMAP).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).target(TRANSLUCENT_TARGET).build(true)
    );

    private static final RenderLayer OUTLINE_SOLID = RenderLayer.of(
        createLayerName("outline_solid"),
        256,
        false,
        false,
        RenderPipelines.ENTITY_SOLID,
        MultiPhaseParameters.builder().texture(new Texture(PonderSpecialTextures.BLANK.getLocation(), false)).lightmap(ENABLE_LIGHTMAP)
            .overlay(ENABLE_OVERLAY_COLOR).build(false)
    );

    private static final BiFunction<Identifier, Boolean, RenderLayer> OUTLINE_TRANSLUCENT = Util.memoize((texture, cull) -> RenderLayer.of(
        createLayerName("outline_translucent" + (cull ? "_cull" : "")),
        256,
        false,
        true,
        cull ? PonderRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL : PonderRenderPipelines.ENTITY_TRANSLUCENT,
        MultiPhaseParameters.builder().texture(new Texture(texture, false)).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).build(false)
    ));

    private static final RenderLayer FLUID = RenderLayer.of(
        createLayerName("fluid"),
        256,
        false,
        true,
        RenderPipelines.TRANSLUCENT,
        MultiPhaseParameters.builder().texture(MIPMAP_BLOCK_ATLAS_TEXTURE).lightmap(ENABLE_LIGHTMAP).target(TRANSLUCENT_TARGET).build(true)
    );

    public static RenderLayer getGui() {
        return GUI;
    }

    public static RenderLayer getGuiInvert() {
        return GUI_INVERT;
    }

    public static RenderLayer getGuiTextHighlight() {
        return GUI_TEXT_HIGHLIGHT;
    }

    public static RenderLayer getGuiTextured(Identifier texture) {
        return GUI_TEXTURED.apply(texture);
    }

    public static RenderLayer translucent() {
        return TRANSLUCENT;
    }

    public static RenderLayer outlineSolid() {
        return OUTLINE_SOLID;
    }

    public static RenderLayer outlineTranslucent(Identifier texture, boolean cull) {
        return OUTLINE_TRANSLUCENT.apply(texture, cull);
    }

    //TODO vanilla uses the translucent render type for fluids, need to investigate if this is even needed
    public static RenderLayer fluid() {
        return FLUID;
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }
}
